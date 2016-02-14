/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
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
import edu.skku.selab.blp.db.IntegratedMethodAnalysisValue;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.DbUtil;
import edu.skku.selab.blp.db.dao.IntegratedAnalysisDAO;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.utils.Util;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BLIA {
	private final String version = SourceFileDAO.DEFAULT_VERSION_STRING;
	private ArrayList<Bug> bugs = null;
	private double alpha = 0;
	private double beta = 0;
	private static Integer completeBugIdCount = 0;
	
	public BLIA() {
		prepareWorkingDir();
	}
	
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
		String repoDir = Property.getInstance().getRepoDir();
		GitCommitLogCollector gitCommitLogCollector = new GitCommitLogCollector(repoDir);
		
		// TODO: default value of collectForcely is false. After testing the value SHOULD be fixed.
		boolean collectForcely = true;
		gitCommitLogCollector.collectCommitLog(commitSince, commitUntil, collectForcely);
		System.out.printf("[DONE] Commit log collecting.(%s sec)\n", getElapsedTimeSting(startTime));
		
		System.out.printf("[STARTED] Bug-Source file vector creating.\n");
		startTime = System.currentTimeMillis();
		BugSourceFileVectorCreator bugSourceFileVectorCreator = new BugSourceFileVectorCreator(); 
		bugSourceFileVectorCreator.create(version);
		System.out.printf("[DONE] Bug-Source file vector creating.(%s sec)\n", getElapsedTimeSting(startTime));
	}
	
	public void preAnalyze() throws Exception {
		BugDAO bugDAO = new BugDAO();
		boolean orderedByFixedDate = true;
		bugs = bugDAO.getAllBugs(orderedByFixedDate);

		// VSM_SCORE
		System.out.printf("[STARTED] Source file analysis.\n");
		long startTime = System.currentTimeMillis();
		SourceFileAnalyzer sourceFileAnalyzer = new SourceFileAnalyzer(bugs);
		boolean useStructuredInformation = true;
		sourceFileAnalyzer.analyze(version, useStructuredInformation);
		System.out.printf("[DONE] Source file analysis.(%s sec)\n", getElapsedTimeSting(startTime));

		// SIMI_SCORE
		System.out.printf("[STARTED] Bug repository analysis.\n");
		startTime = System.currentTimeMillis();
		BugRepoAnalyzer bugRepoAnalyzer = new BugRepoAnalyzer(bugs);
		bugRepoAnalyzer.analyze();
		System.out.printf("[DONE] Bug repository analysis.(%s sec)\n", getElapsedTimeSting(startTime));
		
		// STRACE_SCORE
		System.out.printf("[STARTED] Stack-trace analysis.\n");
		startTime = System.currentTimeMillis();
		StackTraceAnalyzer stackTraceAnalyzer = new StackTraceAnalyzer(bugs);
		stackTraceAnalyzer.analyze();
		System.out.printf("[DONE] Stack-trace analysis.(%s sec)\n", getElapsedTimeSting(startTime));
		
		// COMM_SCORE
		System.out.printf("[STARTED] Scm repository analysis.\n");
		startTime = System.currentTimeMillis();
		ScmRepoAnalyzer scmRepoAnalyzer = new ScmRepoAnalyzer(bugs);
		scmRepoAnalyzer.analyze(version);
		System.out.printf("[DONE] Scm repository analysis.(%s sec)\n", getElapsedTimeSting(startTime));
	}
	
    @SuppressWarnings("unused")
	private class WorkerThread implements Runnable {
    	private int bugID;
    	private boolean includeStackTrace;
    	
        public WorkerThread(int bugID, boolean includeStackTrace){
            this.bugID = bugID;
            this.includeStackTrace = includeStackTrace;
        }
     
        @Override
        public void run() {
			// Compute similarity between Bug report & source files
        	
        	try {
        		insertDataToDb();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
        private void insertDataToDb() throws Exception {
			long startTime = System.currentTimeMillis();

        	IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
    		HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues = integratedAnalysisDAO.getAnalysisValues(bugID);
    		HashMap<Integer, IntegratedMethodAnalysisValue> integratedMethodAnalysisValues = integratedAnalysisDAO.getMethodAnalysisValues(bugID);
    		if (null == integratedMethodAnalysisValues) {
    			return;
    		}

//			HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues = integratedAnalysisValuesMap.get(bugID);
			// AmaLgam doesn't use normalize
			normalize(integratedAnalysisValues);
			combine(integratedAnalysisValues, alpha, beta, includeStackTrace);
			combineForMethodLevel(integratedAnalysisValues, integratedMethodAnalysisValues, alpha, beta, includeStackTrace);
			
			int sourceFileCount = integratedAnalysisValues.keySet().size();
//			System.out.printf("After combine(), integratedAnalysisValues: %d\n", sourceFileCount);
			Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
			while (integratedAnalysisValuesIter.hasNext()) {
				int sourceFileVersionID = integratedAnalysisValuesIter.next();
				
				IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
				int updatedColumnCount = integratedAnalysisDAO.updateBLIAScore(integratedAnalysisValue);
				if (0 == updatedColumnCount) {
					System.err.printf("[ERROR] BLIA.analyze(): BLIA and BugLocator score update failed! BugID: %s, sourceFileVersionID: %d\n",
							integratedAnalysisValue.getBugID(), integratedAnalysisValue.getSourceFileVersionID());

					// remove following line after testing.
//					integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
				}
			}
			
			Iterator<Integer> integratedMethodAnalysisValuesIter = integratedMethodAnalysisValues.keySet().iterator();
			while (integratedMethodAnalysisValuesIter.hasNext()) {
				int methodID = integratedMethodAnalysisValuesIter.next();
				
				IntegratedMethodAnalysisValue integratedMethodAnalysisValue = integratedMethodAnalysisValues.get(methodID);
//				System.out.printf("Before updateBLIAScore(), count: %d/%d\n", count++, sourceFileCount);
				int updatedColumnCount = integratedAnalysisDAO.updateBLIAMethodScore(integratedMethodAnalysisValue);
//				System.out.printf("After updateBLIAScore(), count: %d/%d\n", count, sourceFileCount);
				if (0 == updatedColumnCount) {
					System.err.printf("[ERROR] BLIA.analyze(): BLIA and BugLocator score update failed! BugID: %s, methodID: %d\n",
							integratedMethodAnalysisValue.getBugID(), integratedMethodAnalysisValue.getMethodID());

					// remove following line after testing.
//					integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
				}
			}

			synchronized (completeBugIdCount) {
				completeBugIdCount++;
				System.out.printf("[Thread()] [%d] Bug ID: %s (%s sec)\n", completeBugIdCount, bugID, Util.getElapsedTimeSting(startTime));
			}
        }
    }
    
    private void calculateBLIAScore(int bugID, boolean includeStackTrace) throws Exception {
//		HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues = integratedAnalysisValuesMap.get(bugID);
    	IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
    	
//    	System.out.printf("Before integratedAnalysisDAO.getAnalysisValues() \n");
		HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues = integratedAnalysisDAO.getAnalysisValues(bugID);
		if (null == integratedAnalysisValues) {
			return;
		}
		
		HashMap<Integer, IntegratedMethodAnalysisValue> integratedMethodAnalysisValues = integratedAnalysisDAO.getMethodAnalysisValues(bugID);
		if (null == integratedMethodAnalysisValues) {
			integratedMethodAnalysisValues = new HashMap<Integer, IntegratedMethodAnalysisValue>();
		}
		
//		System.out.printf("After integratedAnalysisDAO.getAnalysisValues() \n");
		// AmaLgam doesn't use normalize
		normalize(integratedAnalysisValues);
		combine(integratedAnalysisValues, alpha, beta, includeStackTrace);
//		combineForMethodLevel(integratedAnalysisValues, integratedMethodAnalysisValues, alpha, beta, includeStackTrace);
		
		@SuppressWarnings("unused")
		int sourceFileCount = integratedAnalysisValues.keySet().size();
//		System.out.printf("After combine(), integratedAnalysisValues: %d\n", sourceFileCount);
//		int count = 0;
		Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
		while (integratedAnalysisValuesIter.hasNext()) {
			int sourceFileVersionID = integratedAnalysisValuesIter.next();
			
			IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
//			System.out.printf("Before updateBLIAScore(), count: %d/%d\n", count++, sourceFileCount);
			int updatedColumnCount = integratedAnalysisDAO.updateBLIAScore(integratedAnalysisValue);
//			System.out.printf("After updateBLIAScore(), count: %d/%d\n", count, sourceFileCount);
			if (0 == updatedColumnCount) {
				System.err.printf("[ERROR] BLIA.analyze(): BLIA and BugLocator score update failed! BugID: %s, sourceFileVersionID: %d\n",
						integratedAnalysisValue.getBugID(), integratedAnalysisValue.getSourceFileVersionID());

				// remove following line after testing.
//				integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
			}
		}
		
//		Iterator<Integer> integratedMethodAnalysisValuesIter = integratedMethodAnalysisValues.keySet().iterator();
//		while (integratedMethodAnalysisValuesIter.hasNext()) {
//			int methodID = integratedMethodAnalysisValuesIter.next();
//			
//			IntegratedMethodAnalysisValue integratedMethodAnalysisValue = integratedMethodAnalysisValues.get(methodID);
////			System.out.printf("Before updateBLIAScore(), count: %d/%d\n", count++, sourceFileCount);
//			int updatedColumnCount = integratedAnalysisDAO.updateBLIAMethodScore(integratedMethodAnalysisValue);
////			System.out.printf("After updateBLIAScore(), count: %d/%d\n", count, sourceFileCount);
//			if (0 == updatedColumnCount) {
//				System.err.printf("[ERROR] BLIA.analyze(): BLIA and BugLocator score update failed! BugID: %s, methodID: %d\n",
//						integratedMethodAnalysisValue.getBugID(), integratedMethodAnalysisValue.getMethodID());
//
//				// remove following line after testing.
////				integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
//			}
//		}
    }
	
	public void analyze(String version, boolean includeStackTrace) throws Exception {
		if (null == bugs) {
			BugDAO bugDAO = new BugDAO();
			bugs = bugDAO.getAllBugs(false);			
		}
		
		Property property = Property.getInstance();
		alpha = property.getAlpha();
		beta = property.getBeta();
		
//		integratedAnalysisValuesMap = new HashMap<String, HashMap<Integer, IntegratedAnalysisValue>>();
//		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
//		for (int i = 0; i < bugs.size(); i++) {
//			String bugID = bugs.get(i).getID();
//			System.out.printf("[getAnalysisValues()] [%d] Bug ID: %s\n", i, bugID);
//			// DB closed because of out of memory!!!
//			
//			try {
//				integratedAnalysisValuesMap.put(bugID, integratedAnalysisDAO.getAnalysisValues(bugID));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		
		System.out.printf("[STARTED] BLIA.anlayze()\n");
//		ExecutorService executor = Executors.newFixedThreadPool(Property.THREAD_COUNT);
//		ExecutorService executor = Executors.newFixedThreadPool(4);
		for (int i = 0; i < bugs.size(); i++) {
			long startTime = System.currentTimeMillis();
			int bugID = bugs.get(i).getID();
			calculateBLIAScore(bugID, includeStackTrace);
			System.out.printf("[calculateBLIAScore()] [%d] Bug ID: %d (%s sec)\n", i, bugID, Util.getElapsedTimeSting(startTime));
//			Runnable worker = new WorkerThread(bugs.get(i).getID());
//			executor.execute(worker);
		}
//		executor.shutdown();
//		while (!executor.isTerminated()) {
//		}
		
		System.out.printf("[DONE] BLIA.anlayze()\n");
	}
	
	/**
	 * 
	 * @param integratedAnalysisValues
	 * @param alpha
	 * @param beta
	 * @param includeStackTrace
	 */
	private void combine(HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues, double alpha, double beta,
			boolean includeStackTrace) {
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
			
			double bliaScore = bugLocatorScore;
			if (includeStackTrace) {
				bliaScore += stackTraceScore;
			}
			
			if (bliaScore > 0) {
				bliaScore = (1 - beta) * bliaScore + beta * commitLogScore;
			} else {
				bliaScore = 0;
			}

//			if (vsmScore > 0.5) {
//				bliaScore = (1 - beta) * bliaScore + beta * commitLogScore;
//			} else if (bugLocatorScore <= 0){
//				bliaScore = 0;
//			}
			
			integratedAnalysisValue.setBliaScore(bliaScore);
		}
	}
	
	/**
	 * 
	 * @param integratedAnalysisValues
	 * @param integratedMethodAnalysisValues
	 * @param alpha
	 * @param beta
	 * @param includeStackTrace
	 */
	private void combineForMethodLevel(HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues,
			HashMap<Integer, IntegratedMethodAnalysisValue> integratedMethodAnalysisValues, double alpha, double beta,
			boolean includeStackTrace) {
		Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
		while (integratedAnalysisValuesIter.hasNext()) {
			int sourceFileVersionID = integratedAnalysisValuesIter.next();
			IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
			
			double vsmScore = integratedAnalysisValue.getVsmScore();
			double similarityScore = integratedAnalysisValue.getSimilarityScore();
			double stackTraceScore = integratedAnalysisValue.getStackTraceScore();
			
			double bugLocatorScore = (1 - alpha) * (vsmScore) + alpha * similarityScore;
			integratedAnalysisValue.setBugLocatorScore(bugLocatorScore);
			
			double bliaMethodScore = bugLocatorScore;
			if (includeStackTrace) {
				bliaMethodScore += stackTraceScore;
			}

			IntegratedMethodAnalysisValue integratedMethodAnalysisValue = integratedMethodAnalysisValues.get(sourceFileVersionID);
			double commitMethodLogScore = integratedMethodAnalysisValue.getCommitLogScore();
			if (bliaMethodScore > 0) {
				bliaMethodScore = (1 - beta) * bliaMethodScore + beta * commitMethodLogScore;
			} else {
				bliaMethodScore = 0;
			}
			
			integratedMethodAnalysisValue.setBliaMethodScore(bliaMethodScore);
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
	
    private boolean deleteDirectory(File path) {
        if(!path.exists()) {
            return false;
        }
         
        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
         
        return path.delete();
    }
	
	private void prepareWorkingDir() {
		Property prop = Property.getInstance();
		String workDir = prop.getWorkDir();
		
		File dir = new File(workDir);
		if (dir.exists()) {
			deleteDirectory(dir);
		}
		
		if (false == dir.mkdir()) {
			System.err.println(workDir + " can't be created!");
			
			if (false == dir.mkdir()) {
				System.err.println(workDir + " can't be created again");
			}
		}
	}
	
	public void run() throws Exception {
		Property prop = Property.getInstance();
		prepareWorkingDir();
				
		long startTime = System.currentTimeMillis();
		BLIA blia = new BLIA();
		
		boolean useStrucrutedInfo = true;
		boolean includeStackTrace = true;	

		DbUtil dbUtil = new DbUtil();
		String dbName = prop.getProductName();
		dbUtil.openConnetion(dbName);
		boolean commitDataIncluded = true;
		dbUtil.initializeAllData(commitDataIncluded);
		dbUtil.closeConnection();

		startTime = System.currentTimeMillis();	
		System.out.printf("[STARTED] BLIA prepareAnalysisData().\n");
		blia.prepareAnalysisData(useStrucrutedInfo, prop.getSince().getTime(), prop.getUntil().getTime());
		System.out.printf("[DONE] BLIA prepareAnalysisData().(Total %s sec)\n", Util.getElapsedTimeSting(startTime));
			
		System.out.printf("[STARTED] BLIA pre-anlaysis.\n");
		blia.preAnalyze();
		System.out.printf("[DONE] BLIA pre-anlaysis.(Total %s sec)\n", Util.getElapsedTimeSting(startTime));
		
		System.out.printf("[STARTED] BLIA anlaysis.\n");
		startTime = System.currentTimeMillis();
		blia.analyze(version, includeStackTrace);
		System.out.printf("[DONE] BLIA anlaysis.(Total %s sec)\n", Util.getElapsedTimeSting(startTime));
	}
}
