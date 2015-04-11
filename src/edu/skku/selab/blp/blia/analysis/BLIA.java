/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.analysis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.blia.indexer.BugCorpusCreator;
import edu.skku.selab.blp.blia.indexer.BugVectorCreator;
import edu.skku.selab.blp.blia.indexer.GitCommitLogCollector;
import edu.skku.selab.blp.blia.indexer.SourceFileCorpusCreator;
import edu.skku.selab.blp.blia.indexer.SourceFileVectorCreator;
import edu.skku.selab.blp.blia.indexer.BugSourceFileVectorCreator;
import edu.skku.selab.blp.blia.indexer.StructuredSourceFileCorpusCreator;
import edu.skku.selab.blp.common.Bug;
import edu.skku.selab.blp.db.IntegratedAnalysisValue;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.IntegratedAnalysisDAO;
import edu.skku.selab.blp.db.dao.SourceFileDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BLIA {
	private final String version = SourceFileDAO.DEFAULT_VERSION_STRING;

	private String getElapsedTimeSting(long startTime) {
		long elapsedTime = System.currentTimeMillis() - startTime;
		String elpsedTimeString = (elapsedTime / 1000) + "." + (elapsedTime % 1000);
		return elpsedTimeString;
	}
	
	public void prepareAnalysisData(boolean useStrucrutedInfo, Date commitSince, Date commitUntil) throws Exception {
		System.out.printf("[STARTED] Source file corpus creating.\n");
		long startTime = System.currentTimeMillis();
		if (!useStrucrutedInfo) {
			SourceFileCorpusCreator sourceFileCorpusCreator = new SourceFileCorpusCreator();
			sourceFileCorpusCreator.create(version);
		} else {
			StructuredSourceFileCorpusCreator structuredSourceFileCorpusCreator = new StructuredSourceFileCorpusCreator();
			structuredSourceFileCorpusCreator.create(version);
		}
		System.out.printf("[DONE] Source file corpus creating.(%s sec)\n", getElapsedTimeSting(startTime));

		System.out.printf("[STARTED] Source file vector creating.\n");
		startTime = System.currentTimeMillis();
		SourceFileVectorCreator sourceFileVectorCreator = new SourceFileVectorCreator();
		sourceFileVectorCreator.createIndex(version);
		sourceFileVectorCreator.computeLengthScore(version);
		sourceFileVectorCreator.create(version);
		System.out.printf("[DONE] Source file vector creating.(%s sec)\n", getElapsedTimeSting(startTime));
		
		// Create SordtedID.txt
		System.out.printf("[STARTED] Bug corpus creating.\n");
		startTime = System.currentTimeMillis();
		BugCorpusCreator bugCorpusCreator = new BugCorpusCreator();
		boolean stackTraceAnaysis = true;
		bugCorpusCreator.create(stackTraceAnaysis);
		System.out.printf("[DONE] Bug corpus creating.(%s sec)\n", getElapsedTimeSting(startTime));
		
		System.out.printf("[STARTED] Bug vector creating.\n");
		startTime = System.currentTimeMillis();
		BugVectorCreator bugVectorCreator = new BugVectorCreator();
		bugVectorCreator.create();
		System.out.printf("[DONE] Bug vector creating.(%s sec)\n", getElapsedTimeSting(startTime));

		System.out.printf("[STARTED] Commit log collecting.\n");
		startTime = System.currentTimeMillis();
		String productName = Property.getInstance().getProductName();
		String repoDir = Property.getInstance().getRepoDir();
		GitCommitLogCollector gitCommitLogCollector = new GitCommitLogCollector(productName, repoDir);
		boolean collectForcely = false;
		gitCommitLogCollector.collectCommitLog(commitSince, commitUntil, collectForcely);
		System.out.printf("[DONE] Commit log collecting.(%s sec)\n", getElapsedTimeSting(startTime));
		
		System.out.printf("[STARTED] Bug-Source file vector creating.\n");
		startTime = System.currentTimeMillis();
		BugSourceFileVectorCreator bugSourceFileVectorCreator = new BugSourceFileVectorCreator(); 
		bugSourceFileVectorCreator.create(version);
		System.out.printf("[DONE] Bug-Source file vector creating.(%s sec)\n", getElapsedTimeSting(startTime));
	}
	
	public void preAnalyze() throws Exception {
		Property property = Property.getInstance();
		String productName = property.getProductName();
		BugDAO bugDAO = new BugDAO();
		boolean orderedByFixedDate = true;
		ArrayList<Bug> orderedBugs = bugDAO.getAllBugs(productName, orderedByFixedDate);

		// VSM_SCORE
		System.out.printf("[STARTED] Source file analysis.\n");
		long startTime = System.currentTimeMillis();
		SourceFileAnalyzer sourceFileAnalyzer = new SourceFileAnalyzer(orderedBugs);
		boolean useStructuredInformation = true;
		sourceFileAnalyzer.analyze(version, useStructuredInformation);
		System.out.printf("[DONE] Source file analysis.(%s sec)\n", getElapsedTimeSting(startTime));

		// SIMI_SCORE
		System.out.printf("[STARTED] Bug repository analysis.\n");
		startTime = System.currentTimeMillis();
		BugRepoAnalyzer bugRepoAnalyzer = new BugRepoAnalyzer(orderedBugs);
		bugRepoAnalyzer.analyze();
		System.out.printf("[DONE] Bug repository analysis.(%s sec)\n", getElapsedTimeSting(startTime));
		
		// STRACE_SCORE
		System.out.printf("[STARTED] Stack-trace analysis.\n");
		startTime = System.currentTimeMillis();
		StackTraceAnalyzer stackTraceAnalyzer = new StackTraceAnalyzer(orderedBugs);
		stackTraceAnalyzer.analyze();
		System.out.printf("[DONE] Stack-trace analysis.(%s sec)\n", getElapsedTimeSting(startTime));
		
		// COMM_SCORE
		System.out.printf("[STARTED] Scm repository analysis.\n");
		startTime = System.currentTimeMillis();
		ScmRepoAnalyzer scmRepoAnalyzer = new ScmRepoAnalyzer(orderedBugs);
		scmRepoAnalyzer.analyze(version);
		System.out.printf("[DONE] Scm repository analysis.(%s sec)\n", getElapsedTimeSting(startTime));
	}
	
	public void analyze(String version) throws Exception {
		String productName = Property.getInstance().getProductName();
		BugDAO bugDAO = new BugDAO();
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		
		ArrayList<Bug> bugs = bugDAO.getAllBugs(productName, false);
		
		Property property = Property.getInstance();
		double alpha = property.getAlpha();
		double beta = property.getBeta();
		
		for (int i = 0; i < bugs.size(); i++) {
			String bugID = bugs.get(i).getID();
			HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues = integratedAnalysisDAO.getAnalysisValues(bugID);
			// AmaLgam doesn't use normalize
			normalize(integratedAnalysisValues);
			
			combine(integratedAnalysisValues, alpha, beta);
			Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
			while (integratedAnalysisValuesIter.hasNext()) {
				int sourceFileVersionID = integratedAnalysisValuesIter.next();
				
				IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
				int updatedColumenCount = integratedAnalysisDAO.updateBLIAScore(integratedAnalysisValue);
				if (0 == updatedColumenCount) {
					System.err.printf("[ERROR] BLIA.analyze(): CommitLog score update failed! BugID: %s, sourceFileVersionID: %d\n",
							integratedAnalysisValue.getBugID(), integratedAnalysisValue.getSourceFileVersionID());

					// remove following line after testing.
//					integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
				}
				
				updatedColumenCount = integratedAnalysisDAO.updateBugLocatorScore(integratedAnalysisValue);
				if (0 == updatedColumenCount) {
					System.err.printf("[ERROR] BLIA.analyze(): CommitLog score update failed! BugID: %s, sourceFileVersionID: %d\n",
							integratedAnalysisValue.getBugID(), integratedAnalysisValue.getSourceFileVersionID());

					// remove following line after testing.
//					integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param integratedAnalysisValues
	 * @param alpha
	 * @param beta
	 */
	private void combine(HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues, double alpha, double beta) {
		Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
		while (integratedAnalysisValuesIter.hasNext()) {
			int sourceFileVersionID = integratedAnalysisValuesIter.next();
			IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
			
			double vsmScore = integratedAnalysisValue.getVsmScore();
			double similarityScore = integratedAnalysisValue.getSimilarityScore();
			double stackTraceScore = integratedAnalysisValue.getStackTraceScore();
			double commitLogScore = integratedAnalysisValue.getCommitLogScore();
			
			double bugLocatorScore = (1 - alpha) * (vsmScore) + alpha * similarityScore;
			integratedAnalysisValue.setBugLocatorScore(bugLocatorScore);
			
			double bliaScore = bugLocatorScore + stackTraceScore;
			if (bugLocatorScore > 0) {
				bliaScore = (1 - beta) * bliaScore + beta * commitLogScore;
			} else {
				bliaScore = 0;
			}

//			if (vsmScore > 0.5) {
//				bliaScore = (1 - beta) * bliaScore + beta * commitLogScore;
//			} else if (bugLocatorScore <= 0){
//				bliaScore = 0;
//			}
			
			integratedAnalysisValue.setBLIAScore(bliaScore);
		}
	}

//	/**
//	 * Combine rVSMScore(vsmVector) and SimiScore(graphVector)
//	 * 
//	 * @param vsmVector
//	 * @param graphVector
//	 * @param f
//	 * @return
//	 */
//	public void combine(HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues, double alpha) {
//		Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
//		while (integratedAnalysisValuesIter.hasNext()) {
//			int sourceFileVersionID = integratedAnalysisValuesIter.next();
//			IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
//			
//			double vsmScore = integratedAnalysisValue.getVsmScore();
//			double similarityScore = integratedAnalysisValue.getSimilarityScore();
////			double stackTraceScore = integratedAnalysisValue.getStackTraceScore();
//			
//			double bugLocatorScore = vsmScore * (1 - alpha) + similarityScore * alpha;
//			integratedAnalysisValue.setBugLocatorScore(bugLocatorScore);
//
//			// Only Stack Trace analysis included.
////			integratedAnalysisValue.setBLIAScore(bugLocatorScore + stackTraceScore);
//		}
//	}

	/**
	 * Normalize values in array from max. to min of array
	 * 
	 * @param array
	 * @return
	 */
	private void normalize(HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues) {
		double maxVsmScore = Double.MIN_VALUE;
		double minVsmScore = Double.MAX_VALUE;;
		double maxSimiScore = Double.MIN_VALUE;
		double minSimiScore = Double.MAX_VALUE;;
//		double maxCommitLogScore = Double.MIN_VALUE;
//		double minCommitLogScore = Double.MAX_VALUE;;

		
		Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
		while (integratedAnalysisValuesIter.hasNext()) {
			int sourceFileVersionID = integratedAnalysisValuesIter.next();
			IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
			double vsmScore = integratedAnalysisValue.getVsmScore();
			double simiScore = integratedAnalysisValue.getSimilarityScore();
//			double commitLogScore = integratedAnalysisValue.getCommitLogScore();
			if (maxVsmScore < vsmScore) {
				maxVsmScore = vsmScore;
			}
			if (minVsmScore > vsmScore) {
				minVsmScore = vsmScore;
			}
			if (maxSimiScore < simiScore) {
				maxSimiScore = simiScore;
			}
			if (minSimiScore > simiScore) {
				minSimiScore = simiScore;
			}
//			if (maxCommitLogScore < commitLogScore) {
//				maxCommitLogScore = commitLogScore;
//			}
//			if (minCommitLogScore > commitLogScore) {
//				minCommitLogScore = commitLogScore;
//			}	
		}
		
		double spanVsmScore = maxVsmScore - minVsmScore;
		double spanSimiScore = maxSimiScore - minSimiScore;
//		double spanCommitLogScore = maxCommitLogScore - minCommitLogScore;
		integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
		while (integratedAnalysisValuesIter.hasNext()) {
			int sourceFileVersionID = integratedAnalysisValuesIter.next();
			IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
			double normalizedVsmScore = (integratedAnalysisValue.getVsmScore() - minVsmScore) / spanVsmScore;
			double normalizedSimiScore = (integratedAnalysisValue.getSimilarityScore() - minSimiScore) / spanSimiScore;
//			double normalizedCommitLogScore = (integratedAnalysisValue.getCommitLogScore() - minCommitLogScore) / spanCommitLogScore;
			integratedAnalysisValue.setVsmScore(normalizedVsmScore);
			integratedAnalysisValue.setSimilarityScore(normalizedSimiScore);
//			integratedAnalysisValue.setCommitLogScore(normalizedCommitLogScore);
		}
	}
}
