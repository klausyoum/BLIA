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
import edu.skku.selab.blp.blia.indexer.SourceFileIndexer;
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
	
	public void preAnalyze(boolean useStrucrutedInfo, Date commitSince, Date commitUntil) throws Exception {
		if (!useStrucrutedInfo) {
			SourceFileCorpusCreator sourceFileCorpusCreator = new SourceFileCorpusCreator();
			sourceFileCorpusCreator.create(version);
		} else {
			StructuredSourceFileCorpusCreator structuredSourceFileCorpusCreator = new StructuredSourceFileCorpusCreator();
			structuredSourceFileCorpusCreator.create(version);
		}
		
		SourceFileIndexer sourceFileIndexer = new SourceFileIndexer();
		sourceFileIndexer.createIndex(version);
		sourceFileIndexer.computeLengthScore(version);
		
		SourceFileVectorCreator sourceFileVectorCreator = new SourceFileVectorCreator();
		sourceFileVectorCreator.create(version);
		
		// Create SordtedID.txt
		BugCorpusCreator bugCorpusCreator = new BugCorpusCreator();
		boolean stackTraceAnaysis = true;
		bugCorpusCreator.create(stackTraceAnaysis);
		
		BugVectorCreator bugVectorCreator = new BugVectorCreator();
		bugVectorCreator.create();

		String productName = Property.getInstance().getProductName();
		String repoDir = Property.getInstance().getRepoDir();
		GitCommitLogCollector gitCommitLogCollector = new GitCommitLogCollector(productName, repoDir);
		gitCommitLogCollector.collectCommitLog(commitSince, commitUntil);
		
		BugSourceFileVectorCreator bugSourceFileVectorCreator = new BugSourceFileVectorCreator(); 
		bugSourceFileVectorCreator.create(version);
		
		// VSM_SCORE
		SourceFileAnalyzer sourceFileAnalyzer = new SourceFileAnalyzer();
		boolean useStructuredInformation = true;
		sourceFileAnalyzer.analyze(version, useStructuredInformation);
		
		// SIMI_SCORE
		BugRepoAnalyzer bugRepoAnalyzer = new BugRepoAnalyzer();
		bugRepoAnalyzer.analyze();
		
		// STRACE_SCORE
		StackTraceAnalyzer stackTraceAnalyzer = new StackTraceAnalyzer();
		stackTraceAnalyzer.analyze();
		
		// COMM_SCORE
		ScmRepoAnalyzer scmRepoAnalyzer = new ScmRepoAnalyzer();
		scmRepoAnalyzer.analyze(version);
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
					integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
				}
				
				updatedColumenCount = integratedAnalysisDAO.updateBugLocatorScore(integratedAnalysisValue);
				if (0 == updatedColumenCount) {
					integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
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
			
			double bliaScore = (1 - alpha) * vsmScore + alpha * similarityScore + stackTraceScore;
			if (bliaScore > 0) {
				bliaScore = (1 - beta) * bliaScore + beta * commitLogScore;
			} else {
				bliaScore = 0;
			}
			
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
