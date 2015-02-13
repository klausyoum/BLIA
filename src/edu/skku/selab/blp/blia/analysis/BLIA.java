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
	
	public void prepareIndexData(boolean useStrucrutedInfo, Date commitSince, Date commitUntil) throws Exception {
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
	}
	
	public void prepareAnalysisData() throws Exception {
		SourceFileAnalyzer sourceFileAnalyzer = new SourceFileAnalyzer();
		sourceFileAnalyzer.analyze(version);
		
		BugRepoAnalyzer bugRepoAnalyzer = new BugRepoAnalyzer();
		bugRepoAnalyzer.analyze();
	}
	
	public void analyze(String version, boolean useCommitLogAnalysis) throws Exception {
		String productName = Property.getInstance().getProductName();
		BugDAO bugDAO = new BugDAO();
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		
		ArrayList<Bug> bugs = bugDAO.getAllBugs(productName, false);
		
		Property property = Property.getInstance();
		double alpha = property.getAlpha();
		double beta = property.getBeta();
		
		StackTraceAnalyzer stackTraceAnalyzer = new StackTraceAnalyzer();
		stackTraceAnalyzer.analyze();
		
		if (useCommitLogAnalysis) {
			ScmRepoAnalyzer scmRepoAnalyzer = new ScmRepoAnalyzer();
			scmRepoAnalyzer.analyze(version);
		}
		
		for (int i = 0; i < bugs.size(); i++) {
			String bugID = bugs.get(i).getID();
			HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues = integratedAnalysisDAO.getAnalysisValues(bugID);
			
			Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
			while (integratedAnalysisValuesIter.hasNext()) {
				int sourceFileVersionID = integratedAnalysisValuesIter.next();
				
				IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
				double vsmScore = integratedAnalysisValue.getVsmScore();
				vsmScore *= sourceFileDAO.getLengthScore(sourceFileVersionID);
				integratedAnalysisValue.setVsmScore(vsmScore);
			}
			
			normalize(integratedAnalysisValues);
			
			if (!useCommitLogAnalysis) {
				combine(integratedAnalysisValues, alpha);
			} else {
				combine(integratedAnalysisValues, alpha, beta);
			}
			
			integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
			while (integratedAnalysisValuesIter.hasNext()) {
				int sourceFileVersionID = integratedAnalysisValuesIter.next();
				
				IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
				int updatedColumenCount = integratedAnalysisDAO.updateBLIAScore(integratedAnalysisValue);
				
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
			
			double bugLocatorScore = vsmScore * (1 - alpha) + similarityScore * alpha;
			integratedAnalysisValue.setBugLocatorScore(bugLocatorScore);
			
			double bliaScore = vsmScore * (1 - alpha) + similarityScore * alpha;
			if (bliaScore > 0) {
				bliaScore = bliaScore * (1 - beta) + commitLogScore * beta + stackTraceScore;
				
				if (commitLogScore > 0) {
					System.out.printf("CommitScore: %f, bugLocatorScore: %f, bliaScore: %f\n", commitLogScore, bugLocatorScore, bliaScore);
				}
			} else {
				bliaScore = 0;
			}
			
			integratedAnalysisValue.setBLIAScore(bliaScore);
		}
	}

	/**
	 * Combine rVSMScore(vsmVector) and SimiScore(graphVector)
	 * 
	 * @param vsmVector
	 * @param graphVector
	 * @param f
	 * @return
	 */
	public void combine(HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues, double alpha) {
		Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
		while (integratedAnalysisValuesIter.hasNext()) {
			int sourceFileVersionID = integratedAnalysisValuesIter.next();
			IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
			
			double vsmScore = integratedAnalysisValue.getVsmScore();
			double similarityScore = integratedAnalysisValue.getSimilarityScore();
			double stackTraceScore = integratedAnalysisValue.getStackTraceScore();
			
			double bugLocatorScore = vsmScore * (1 - alpha) + similarityScore * alpha;
			integratedAnalysisValue.setBugLocatorScore(bugLocatorScore);

			// Only Stack Trace analysis included.
//			integratedAnalysisValue.setBLIAScore(bugLocatorScore + stackTraceScore);
		}
	}

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
		
		Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
		while (integratedAnalysisValuesIter.hasNext()) {
			int sourceFileVersionID = integratedAnalysisValuesIter.next();
			IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
			double vsmScore = integratedAnalysisValue.getVsmScore();
			double simiScore = integratedAnalysisValue.getSimilarityScore();
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
		}
		
		double spanVsmScore = maxVsmScore - minVsmScore;
		double spanSimiScore = maxSimiScore - minSimiScore;
		integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
		while (integratedAnalysisValuesIter.hasNext()) {
			int sourceFileVersionID = integratedAnalysisValuesIter.next();
			IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
			double vsmScore = (integratedAnalysisValue.getVsmScore() - minVsmScore) / spanVsmScore;
			double simiScore = (integratedAnalysisValue.getSimilarityScore() - minSimiScore) / spanSimiScore;
			integratedAnalysisValue.setVsmScore(vsmScore);
			integratedAnalysisValue.setSimilarityScore(simiScore);
		}
	}
}
