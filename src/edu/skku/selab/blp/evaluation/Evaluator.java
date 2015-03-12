/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.evaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import edu.skku.selab.blp.common.Bug;
import edu.skku.selab.blp.common.SourceFile;
import edu.skku.selab.blp.db.ExperimentResult;
import edu.skku.selab.blp.db.IntegratedAnalysisValue;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.ExperimentResultDAO;
import edu.skku.selab.blp.db.dao.IntegratedAnalysisDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class Evaluator {
	public final static String ALG_BUG_LOCATOR = "BugLocator";
	public final static String ALG_BLIA = "BLIA";
	
	private ExperimentResult experimentResult;
	
	/**
	 * 
	 */
	public Evaluator(String productName, String algorithmName, String algorithmDescription, double alpha, double beta, int pastDays) {
		experimentResult = new ExperimentResult();
		experimentResult.setProductName(productName);
		experimentResult.setAlgorithmName(algorithmName);
		experimentResult.setAlgorithmDescription(algorithmDescription);
		experimentResult.setAlpha(alpha);
		experimentResult.setBeta(beta);
		experimentResult.setPastDays(pastDays);
	}
	
	public void evaluate() throws Exception {
		calculateTopN();
		calculateMRR();
		calulateMAP();
		
		experimentResult.setExperimentDate(new Date(System.currentTimeMillis()));
		ExperimentResultDAO experimentResultDAO = new ExperimentResultDAO();
		experimentResultDAO.insertExperimentResult(experimentResult);
	}
	
	private ArrayList<IntegratedAnalysisValue> getRankedValues(String bugID, int limit) throws Exception {
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		ArrayList<IntegratedAnalysisValue> rankedValues = null;
		if (experimentResult.getAlgorithmName().equalsIgnoreCase(Evaluator.ALG_BUG_LOCATOR)) {
			rankedValues = integratedAnalysisDAO.getBugLocatorRankedValues(bugID, limit);
		} else if (experimentResult.getAlgorithmName().equalsIgnoreCase(Evaluator.ALG_BLIA)) {
			rankedValues = integratedAnalysisDAO.getBLIARankedValues(bugID, limit);
		}
		
		return rankedValues;
	}
	
	private void calculateTopN() throws Exception {
		int top1 = 0;
		int top5 = 0;
		int top10 = 0;
		
		String productName = experimentResult.getProductName();
		BugDAO bugDAO = new BugDAO();
		ArrayList<Bug> bugs = bugDAO.getAllBugs(productName, true);
//		boolean isCounted = false;
		for (int i = 0; i < bugs.size(); i++) {
			String bugID = bugs.get(i).getID();
			HashSet<SourceFile> fixedFiles = bugDAO.getFixedFiles(bugID);
			// Exception handling
			if (null == fixedFiles) {
				continue;
			}

			HashSet<Integer> fixedFileVersionIDs = new HashSet<Integer>();
			
			// For test
			HashMap<Integer, SourceFile> fixedFileVersionMap = new HashMap<Integer, SourceFile>();

			Iterator<SourceFile> fixedFilesIter = fixedFiles.iterator();
			while (fixedFilesIter.hasNext()) {
				SourceFile fixedFile = fixedFilesIter.next();
				fixedFileVersionIDs.add(fixedFile.getSourceFileVersionID());
				fixedFileVersionMap.put(fixedFile.getSourceFileVersionID(), fixedFile);
			}
			
			ArrayList<IntegratedAnalysisValue> rankedValues = getRankedValues(bugID, 10);
			if (rankedValues == null) {
				// TODO: Start from HERE!
				System.out.printf("[ERROR] Bug ID: %s\n", bugID);
				continue;
			}
			for (int j = 0; j < rankedValues.size(); j++) {
				int sourceFileVersionID = rankedValues.get(j).getSourceFileVersionID();
				if (fixedFileVersionIDs.contains(sourceFileVersionID)) {
					if (j < 1) {
						top1++;
						top5++;
						top10++;
						System.out.printf("[TOP01] BugID %s fixedFile %s Rank %d\n",
								bugID, fixedFileVersionMap.get(sourceFileVersionID).getName(), j + 1);
						break;						
					} else if (j < 5) {
						top5++;
						top10++;
						System.out.printf("[TOP05] BugID %s fixedFile %s Rank %d\n",
								bugID, fixedFileVersionMap.get(sourceFileVersionID).getName(), j + 1);
						break;
					} else if (j < 10) {
						top10++;
						System.out.printf("[TOP10] BugID %s fixedFile %s Rank %d\n",
								bugID, fixedFileVersionMap.get(sourceFileVersionID).getName(), j + 1);
						break;
					}
				}
			}
		}
		
		experimentResult.setTop1(top1);
		experimentResult.setTop5(top5);
		experimentResult.setTop10(top10);
		
		int bugCount = bugs.size();
		experimentResult.setTop1Rate((double) top1 / bugCount);
		experimentResult.setTop5Rate((double) top5 / bugCount);
		experimentResult.setTop10Rate((double) top10 / bugCount);
		
		System.out.printf("Top1: %d, Top5: %d, Top10: %d, Top1Rate: %f, Top5Rate: %f, Top10Rate: %f\n",
				experimentResult.getTop1(), experimentResult.getTop5(), experimentResult.getTop10(),
				experimentResult.getTop1Rate(), experimentResult.getTop5Rate(), experimentResult.getTop10Rate());
	}
	
	private void calculateMRR() throws Exception {
		double MRR = 0;
		
		String productName = experimentResult.getProductName();
		BugDAO bugDAO = new BugDAO();
		ArrayList<Bug> bugs = bugDAO.getAllBugs(productName, true);
		double sumOfRRank = 0;
		for (int i = 0; i < bugs.size(); i++) {
			String bugID = bugs.get(i).getID();
			HashSet<SourceFile> fixedFiles = bugDAO.getFixedFiles(bugID);
			// Exception handling
			if (null == fixedFiles) {
				continue;
			}

			HashSet<Integer> fixedFileVersionIDs = new HashSet<Integer>();
			Iterator<SourceFile> fixedFilesIter = fixedFiles.iterator();
			while (fixedFilesIter.hasNext()) {
				SourceFile fixedFile = fixedFilesIter.next();
				fixedFileVersionIDs.add(fixedFile.getSourceFileVersionID());
			}
			
			ArrayList<IntegratedAnalysisValue> rankedValues = getRankedValues(bugID, 0);
			if (rankedValues == null) {
				// TODO: Start from HERE!
				System.out.printf("[ERROR] Bug ID: %s\n", bugID);
				continue;
			}
			for (int j = 0; j < rankedValues.size(); j ++) {
				int sourceFileVersionID = rankedValues.get(j).getSourceFileVersionID();
				
				if (fixedFileVersionIDs.contains(sourceFileVersionID)) {
//					System.out.printf("BugID: %s, Rank: %d\n", bugID, j + 1);
					sumOfRRank += (1.0 / (j + 1));
					break;
				}
			}
		}
		
		MRR = sumOfRRank / bugs.size();
		experimentResult.setMRR(MRR);
		
		System.out.printf("MRR: %f\n", experimentResult.getMRR());
	}
	
	private void calulateMAP() throws Exception {
		double MAP = 0;
		String productName = experimentResult.getProductName();
		BugDAO bugDAO = new BugDAO();
		ArrayList<Bug> bugs = bugDAO.getAllBugs(productName, true);
		double sumOfAP = 0;
		
		for (int i = 0; i < bugs.size(); i++) {
			sumOfAP = 0;
			String bugID = bugs.get(i).getID();
			HashSet<SourceFile> fixedFiles = bugDAO.getFixedFiles(bugID);
			// Exception handling
			if (null == fixedFiles) {
				continue;
			}

			HashSet<Integer> fixedFileVersionIDs = new HashSet<Integer>();
			Iterator<SourceFile> fixedFilesIter = fixedFiles.iterator();
			while (fixedFilesIter.hasNext()) {
				SourceFile fixedFile = fixedFilesIter.next();
				fixedFileVersionIDs.add(fixedFile.getSourceFileVersionID());
			}
			
			int numberOfFixedFiles = 0;
			int numberOfPositiveInstances = 0;
			ArrayList<IntegratedAnalysisValue> rankedValues = getRankedValues(bugID, 0);
			if (rankedValues == null) {
				// TODO: Start from HERE!
				System.out.printf("[ERROR] Bug ID: %s\n", bugID);
				continue;
			}
			for (int j = 0; j < rankedValues.size(); j ++) {
				int sourceFileVersionID = rankedValues.get(j).getSourceFileVersionID();
				if (fixedFileVersionIDs.contains(sourceFileVersionID)) {
					numberOfPositiveInstances++;
				}
			}

			double precision = 0.0;
			for (int j = 0; j < rankedValues.size(); j ++) {
				int sourceFileVersionID = rankedValues.get(j).getSourceFileVersionID();
				if (fixedFileVersionIDs.contains(sourceFileVersionID)) {
					numberOfFixedFiles++;
					precision = ((double) numberOfFixedFiles) / (j + 1);
					sumOfAP += (precision / numberOfPositiveInstances);
				}
			}
			
			MAP += sumOfAP;
		}
		
		MAP = MAP / bugs.size();
		experimentResult.setMAP(MAP);
		
		System.out.printf("MAP: %f\n", experimentResult.getMAP());
	}
}
