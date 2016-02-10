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

import edu.skku.selab.blp.common.Method;
import edu.skku.selab.blp.db.IntegratedMethodAnalysisValue;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.ExperimentResultDAO;
import edu.skku.selab.blp.db.dao.IntegratedAnalysisDAO;
import edu.skku.selab.blp.utils.Util;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class EvaluatorForMethodLevel extends Evaluator {
	public final static String ALG_BLIA_METHOD = "Method level's BLIA";
	
	protected HashMap<Integer, HashSet<Method>> realFixedMethodsMap = null;
	protected HashMap<Integer, ArrayList<IntegratedMethodAnalysisValue>> rankedMethodValuesMap = null;
	
	/**
	 * 
	 */
	public EvaluatorForMethodLevel(String productName, String algorithmName, String algorithmDescription,
			double alpha, double beta, int pastDays) {
		super(productName, algorithmName, algorithmDescription, alpha, beta, pastDays);
		realFixedMethodsMap = null;
	}
	
	/**
	 * 
	 */
	public EvaluatorForMethodLevel(String productName, String algorithmName, String algorithmDescription, double alpha, double beta, int pastDays, double candidateRate) {
		this(productName, algorithmName, algorithmDescription, alpha, beta, pastDays);
		experimentResult.setCandidateRate(candidateRate);
	}
	
	public void evaluate() throws Exception {
		long startTime = System.currentTimeMillis();
		System.out.printf("[STARTED] Evaluator.evaluate().\n");
		
		BugDAO bugDAO = new BugDAO();
		bugs = bugDAO.getAllBugs(true);
		
		realFixedMethodsMap = new HashMap<Integer, HashSet<Method>>();
		rankedMethodValuesMap = new HashMap<Integer, ArrayList<IntegratedMethodAnalysisValue>>();
		for (int i = 0; i < bugs.size(); i++) {
			int bugID = bugs.get(i).getID();
			HashSet<Method> fixedMethods = bugDAO.getFixedMethods(bugID);
			realFixedMethodsMap.put(bugID, fixedMethods);
			rankedMethodValuesMap.put(bugID, getRankedValues(bugID, 0));
		}

		calculateMetrics();
		
		experimentResult.setExperimentDate(new Date(System.currentTimeMillis()));
		ExperimentResultDAO experimentResultDAO = new ExperimentResultDAO();
		experimentResultDAO.insertExperimentResult(experimentResult);
		
		System.out.printf("[DONE] Evaluator.evaluate().(Total %s sec)\n", Util.getElapsedTimeSting(startTime));
	}
	
	private ArrayList<IntegratedMethodAnalysisValue> getRankedValues(int bugID, int limit) throws Exception {
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		ArrayList<IntegratedMethodAnalysisValue> rankedValues = null;
		if (experimentResult.getAlgorithmName().equalsIgnoreCase(EvaluatorForMethodLevel.ALG_BLIA_METHOD)) {
			rankedValues = integratedAnalysisDAO.getBLIAMethodRankedValues(bugID, limit);
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
        	try {
        		calculateTopN();
        		calculateMRR();
        		calulateMAP();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
        private void calculateTopN() throws Exception {
			HashSet<Method> realFixedMethods = realFixedMethodsMap.get(bugID);
			// Exception handling
			if (null == realFixedMethods) {
				return;
			}

			HashSet<Integer> fixedMethodIDs = new HashSet<Integer>();
			HashMap<Integer, Method> fixedMethodMap = new HashMap<Integer, Method>();

			Iterator<Method> fixedMethodsIter = realFixedMethods.iterator();
			while (fixedMethodsIter.hasNext()) {
				Method fixedMethod = fixedMethodsIter.next();
				fixedMethodIDs.add(fixedMethod.getID());
				fixedMethodMap.put(fixedMethod.getID(), fixedMethod);
			}
			
			int limitedCount = 10;
			
			// test code
			limitedCount = 30;
			
			ArrayList<IntegratedMethodAnalysisValue> rankedMethodValues = rankedMethodValuesMap.get(bugID);
			if (rankedMethodValues == null) {
				System.out.printf("[ERROR] Bug ID: %d\n", bugID);
				return;
			}
			for (int j = 0; j < rankedMethodValues.size(); j++) {
				int methodID = rankedMethodValues.get(j).getMethodID();
				if (fixedMethodIDs.contains(methodID)) {
					synchronized(syncLock) {
						if (j < 1) {
							top1++;
							top5++;
							top10++;
						
							String log = bugID + " " + fixedMethodMap.get(methodID).getName() + " " + (j + 1) + "\n";
							writer.write(log);
	//						System.out.printf("%d %s %d\n",
	//								bugID, fixedFileVersionMap.get(sourceFileVersionID).getName(), j + 1);
							break;						
						} else if (j < 5) {
							top5++;
							top10++;
							
							String log = bugID + " " + fixedMethodMap.get(methodID).getName() + " " + (j + 1) + "\n";
							writer.write(log);
	//						System.out.printf("%d %s %d\n",
	//								bugID, fixedFileVersionMap.get(sourceFileVersionID).getName(), j + 1);
							break;
						} else if (j < 10) {
							top10++;

							String log = bugID + " " + fixedMethodMap.get(methodID).getName() + " " + (j + 1) + "\n";
							writer.write(log);
	//						System.out.printf("%d %s %d\n",
	//								bugID, fixedFileVersionMap.get(sourceFileVersionID).getName(), j + 1);
							break;
						}
						// debug code
						else if (j < limitedCount) {
							String log = bugID + " " + fixedMethodMap.get(methodID).getName() + " " + (j + 1) + "\n";
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
			HashSet<Method> fixedMethods = realFixedMethodsMap.get(bugID);
			// Exception handling
			if (null == fixedMethods) {
				return;
			}

			HashSet<Integer> fixedMethodsIDs = new HashSet<Integer>();
			Iterator<Method> fixedMethodsIter = fixedMethods.iterator();
			while (fixedMethodsIter.hasNext()) {
				Method fixedMethod = fixedMethodsIter.next();
				fixedMethodsIDs.add(fixedMethod.getID());
			}
			
			ArrayList<IntegratedMethodAnalysisValue> rankedMethodValues = rankedMethodValuesMap.get(bugID);
			if (rankedMethodValues == null) {
				System.out.printf("[ERROR] Bug ID: %d\n", bugID);
				return;
			}
			for (int j = 0; j < rankedMethodValues.size(); j ++) {
				int methodID = rankedMethodValues.get(j).getMethodID();
				
				if (fixedMethodsIDs.contains(methodID)) {
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
        	
			HashSet<Method> fixedMethods = realFixedMethodsMap.get(bugID);
			// Exception handling
			if (null == fixedMethods) {
				return;
			}

			HashSet<Integer> fixedMethodIDs = new HashSet<Integer>();
			Iterator<Method> fixedMethodsIter = fixedMethods.iterator();
			while (fixedMethodsIter.hasNext()) {
				Method fixedMethod = fixedMethodsIter.next();
				fixedMethodIDs.add(fixedMethod.getID());
			}
			
			int numberOfFixedFiles = 0;
			int numberOfPositiveInstances = 0;
			ArrayList<IntegratedMethodAnalysisValue> rankedMethodValues = rankedMethodValuesMap.get(bugID);
			if (rankedMethodValues == null) {
				System.out.printf("[ERROR] Bug ID: %d\n", bugID);
				return;
			}
			for (int j = 0; j < rankedMethodValues.size(); j ++) {
				int methodID = rankedMethodValues.get(j).getMethodID();
				if (fixedMethodIDs.contains(methodID)) {
					numberOfPositiveInstances++;
				}
			}

			double precision = 0.0;
			for (int j = 0; j < rankedMethodValues.size(); j ++) {
				int methodID = rankedMethodValues.get(j).getMethodID();
				if (fixedMethodIDs.contains(methodID)) {
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
}
