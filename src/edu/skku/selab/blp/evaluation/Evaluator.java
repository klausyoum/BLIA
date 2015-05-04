/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.evaluation;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.skku.selab.blp.Property;
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
	private ArrayList<Bug> bugs = null;
	private HashMap<Integer, HashSet<SourceFile>> realFixedFilesMap = null;;
	private HashMap<Integer, ArrayList<IntegratedAnalysisValue>> rankedValuesMap = null;
	private FileWriter writer = null; 
	
	private Integer syncLock = 0;
	private int top1 = 0;
	private int top5 = 0;
	private int top10 = 0;
	
	private Double sumOfRRank = 0.0;
	private Double MAP = 0.0;
	
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
		bugs = null;
		realFixedFilesMap = null;
	}
	
	public void evaluate() throws Exception {
		String productName = experimentResult.getProductName();
		BugDAO bugDAO = new BugDAO();
		bugs = bugDAO.getAllBugs(productName, true);
		
		realFixedFilesMap = new HashMap<Integer, HashSet<SourceFile>>();
		rankedValuesMap = new HashMap<Integer, ArrayList<IntegratedAnalysisValue>>();
		for (int i = 0; i < bugs.size(); i++) {
			int bugID = bugs.get(i).getID();
			HashSet<SourceFile> fixedFiles = bugDAO.getFixedFiles(bugID);
			realFixedFilesMap.put(bugID, fixedFiles);
			rankedValuesMap.put(bugID, getRankedValues(bugID, 0));
		}

		calculateMetrics();
		
		experimentResult.setExperimentDate(new Date(System.currentTimeMillis()));
		ExperimentResultDAO experimentResultDAO = new ExperimentResultDAO();
		experimentResultDAO.insertExperimentResult(experimentResult);
	}
	
	private ArrayList<IntegratedAnalysisValue> getRankedValues(int bugID, int limit) throws Exception {
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		ArrayList<IntegratedAnalysisValue> rankedValues = null;
		if (experimentResult.getAlgorithmName().equalsIgnoreCase(Evaluator.ALG_BUG_LOCATOR)) {
			rankedValues = integratedAnalysisDAO.getBugLocatorRankedValues(bugID, limit);
		} else if (experimentResult.getAlgorithmName().equalsIgnoreCase(Evaluator.ALG_BLIA)) {
			rankedValues = integratedAnalysisDAO.getBLIARankedValues(bugID, limit);
		}
		
		return rankedValues;
	}
	
    private class WorkerThread implements Runnable {
    	private int bugID;
    	
        public WorkerThread(int bugID) {
            this.bugID = bugID;
        }
     
        @Override
        public void run() {
			// Compute similarity between Bug report & source files
        	
        	try {
        		calculateTopN();
        		calculateMRR();
        		calulateMAP();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
        private void calculateTopN() throws Exception {
			HashSet<SourceFile> realFixedFiles = realFixedFilesMap.get(bugID);
			// Exception handling
			if (null == realFixedFiles) {
				return;
			}

			HashSet<Integer> fixedFileVersionIDs = new HashSet<Integer>();
			HashMap<Integer, SourceFile> fixedFileVersionMap = new HashMap<Integer, SourceFile>();

			Iterator<SourceFile> fixedFilesIter = realFixedFiles.iterator();
			while (fixedFilesIter.hasNext()) {
				SourceFile fixedFile = fixedFilesIter.next();
				fixedFileVersionIDs.add(fixedFile.getSourceFileVersionID());
				fixedFileVersionMap.put(fixedFile.getSourceFileVersionID(), fixedFile);
			}
			
			int limitedCount = 10;
			
			// test code
			limitedCount = 30;
			
			ArrayList<IntegratedAnalysisValue> rankedValues = rankedValuesMap.get(bugID);
			if (rankedValues == null) {
				System.out.printf("[ERROR] Bug ID: %d\n", bugID);
				return;
			}
			for (int j = 0; j < rankedValues.size(); j++) {
				int sourceFileVersionID = rankedValues.get(j).getSourceFileVersionID();
				if (fixedFileVersionIDs.contains(sourceFileVersionID)) {
					synchronized(syncLock) {
						if (j < 1) {
							top1++;
							top5++;
							top10++;
						
							String log = bugID + " " + fixedFileVersionMap.get(sourceFileVersionID).getName() + " " + (j + 1) + "\n";
							writer.write(log);
	//						System.out.printf("%d %s %d\n",
	//								bugID, fixedFileVersionMap.get(sourceFileVersionID).getName(), j + 1);
							break;						
						} else if (j < 5) {
							top5++;
							top10++;
							
							String log = bugID + " " + fixedFileVersionMap.get(sourceFileVersionID).getName() + " " + (j + 1) + "\n";
							writer.write(log);
	//						System.out.printf("%d %s %d\n",
	//								bugID, fixedFileVersionMap.get(sourceFileVersionID).getName(), j + 1);
							break;
						} else if (j < 10) {
							top10++;

							String log = bugID + " " + fixedFileVersionMap.get(sourceFileVersionID).getName() + " " + (j + 1) + "\n";
							writer.write(log);
	//						System.out.printf("%d %s %d\n",
	//								bugID, fixedFileVersionMap.get(sourceFileVersionID).getName(), j + 1);
							break;
						}
						// debug code
						else if (j < limitedCount) {
							String log = bugID + " " + fixedFileVersionMap.get(sourceFileVersionID).getName() + " " + (j + 1) + "\n";
							writer.write(log);
	//						System.out.printf("%d %s %d\n",
	//								bugID, fixedFileVersionMap.get(sourceFileVersionID).getName(), j + 1);
							break;
						}
					}
				}
			}
        }
        
        private void calculateMRR() throws Exception {
			HashSet<SourceFile> fixedFiles = realFixedFilesMap.get(bugID);
			// Exception handling
			if (null == fixedFiles) {
				return;
			}

			HashSet<Integer> fixedFileVersionIDs = new HashSet<Integer>();
			Iterator<SourceFile> fixedFilesIter = fixedFiles.iterator();
			while (fixedFilesIter.hasNext()) {
				SourceFile fixedFile = fixedFilesIter.next();
				fixedFileVersionIDs.add(fixedFile.getSourceFileVersionID());
			}
			
			ArrayList<IntegratedAnalysisValue> rankedValues = rankedValuesMap.get(bugID);
			if (rankedValues == null) {
				System.out.printf("[ERROR] Bug ID: %d\n", bugID);
				return;
			}
			for (int j = 0; j < rankedValues.size(); j ++) {
				int sourceFileVersionID = rankedValues.get(j).getSourceFileVersionID();
				
				if (fixedFileVersionIDs.contains(sourceFileVersionID)) {
//					System.out.printf("BugID: %s, Rank: %d\n", bugID, j + 1);
					synchronized(sumOfRRank) {
						sumOfRRank += (1.0 / (j + 1));
					}
					break;
				}
			}
        }
        
        private void calulateMAP() throws Exception {
        	double sumOfAP = 0;
        	
			HashSet<SourceFile> fixedFiles = realFixedFilesMap.get(bugID);
			// Exception handling
			if (null == fixedFiles) {
				return;
			}

			HashSet<Integer> fixedFileVersionIDs = new HashSet<Integer>();
			Iterator<SourceFile> fixedFilesIter = fixedFiles.iterator();
			while (fixedFilesIter.hasNext()) {
				SourceFile fixedFile = fixedFilesIter.next();
				fixedFileVersionIDs.add(fixedFile.getSourceFileVersionID());
			}
			
			int numberOfFixedFiles = 0;
			int numberOfPositiveInstances = 0;
			ArrayList<IntegratedAnalysisValue> rankedValues = rankedValuesMap.get(bugID);
			if (rankedValues == null) {
				System.out.printf("[ERROR] Bug ID: %d\n", bugID);
				return;
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
			
			synchronized(MAP) {
				MAP += sumOfAP;
			}
        }
    }
	
	private void calculateMetrics() throws Exception {
		writer = new FileWriter(Property.OUTPUT_FILE, false);
		
		ExecutorService executor = Executors.newFixedThreadPool(Property.THREAD_COUNT);
//		boolean isCounted = false;
		for (int i = 0; i < bugs.size(); i++) {
			Runnable worker = new WorkerThread(bugs.get(i).getID());
			executor.execute(worker);
		}
		
		executor.shutdown();
		while (!executor.isTerminated()) {
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
		String log = "Top1: " + experimentResult.getTop1() + ", " +
				"Top5: " + experimentResult.getTop5() + ", " +
				"Top10: " + experimentResult.getTop10() + ", " +
				"Top1Rate: " + experimentResult.getTop1Rate() + ", " +
				"Top5Rate: " + experimentResult.getTop5Rate() + ", " +
				"Top10Rate: " + experimentResult.getTop10Rate() + "\n";
		writer.write(log);
		
////////////////////////////////////////////////////////////////////////////
		double MRR = sumOfRRank / bugs.size();
		experimentResult.setMRR(MRR);
		
		System.out.printf("MRR: %f\n", experimentResult.getMRR());
		log = "MRR: " + experimentResult.getMRR() + "\n";
		writer.write(log);

////////////////////////////////////////////////////////////////////////////
		MAP = MAP / bugs.size();
		experimentResult.setMAP(MAP);
		
		System.out.printf("MAP: %f\n", experimentResult.getMAP());
		log = "MAP: " + experimentResult.getMAP() + "\n";
		writer.write(log);
		
		writer.flush();
		writer.close();
	}
}
