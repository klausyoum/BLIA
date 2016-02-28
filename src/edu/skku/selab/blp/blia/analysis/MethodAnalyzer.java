/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.IntegratedAnalysisDAO;
import edu.skku.selab.blp.db.dao.MethodDAO;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.blia.indexer.BugMethodVectorCreator;
import edu.skku.selab.blp.blia.indexer.SourceFileCorpusCreator;
import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.common.Bug;
import edu.skku.selab.blp.common.FileParser;
import edu.skku.selab.blp.common.Method;
import edu.skku.selab.blp.db.AnalysisValue;
import edu.skku.selab.blp.db.ExtendedIntegratedAnalysisValue;
import edu.skku.selab.blp.db.IntegratedAnalysisValue;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class MethodAnalyzer extends SourceFileAnalyzer {
	private HashMap<Integer, ArrayList<IntegratedAnalysisValue>> rankedSuspFilesMap;
	private HashMap<Integer, ArrayList<Method>> methodMap;
	private HashMap<Integer, Double> bugNormMap;
	
	public MethodAnalyzer() {
		super();
		rankedSuspFilesMap = null;
		methodMap = null;
		bugNormMap = null;
	}
	
    public MethodAnalyzer(ArrayList<Bug> bugs) {
    	super(bugs);
		rankedSuspFilesMap = null;
		methodMap = null;
		bugNormMap = null;
    }
    
	/**
	 * Calculate VSM score between source files and each bug report 
	 * 
	 */
	public void analyze() throws Exception {
		MethodDAO methodDAO = new MethodDAO();
		BugDAO bugDAO = new BugDAO();
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		
		methodMap = methodDAO.getAllMethods();
		bugNormMap = bugDAO.getAllNorms();

		int limit = 10;
		rankedSuspFilesMap = new HashMap<Integer, ArrayList<IntegratedAnalysisValue>>();
		for (int i = 0; i < bugs.size(); i++) {
			int bugID = bugs.get(i).getID();
			ArrayList<IntegratedAnalysisValue> rankedSuspiciousFileValues = integratedAnalysisDAO.getMiddleSourceFileRankedValues(bugID, limit);
			if (null == rankedSuspiciousFileValues) {
				System.err.println("[ERROR] Can't load rankedSuspiciousFileValues at MethodAnalyzer.analyze()");
				return;
			}
			
			rankedSuspFilesMap.put(bugID, rankedSuspiciousFileValues);
		}
		
		bugDAO.deleteAllBugMthTermWeights();
		BugMethodVectorCreator bugMethodVectorCreator = new BugMethodVectorCreator(methodMap);
		bugMethodVectorCreator.create(SourceFileDAO.DEFAULT_VERSION_STRING, rankedSuspFilesMap);
		
		ExecutorService executor = Executors.newFixedThreadPool(Property.THREAD_COUNT);

		for (int i = 0; i < bugs.size(); i++) {
			// calculate term count, IDC, TF and IDF
			Runnable worker = new WorkerThread(bugs.get(i));
			executor.execute(worker);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
	}
	
    private class WorkerThread implements Runnable {
    	private Bug bug;
    	
        public WorkerThread(Bug bug){
            this.bug = bug;
        }
     
        @Override
        public void run() {
        	try {
        		computeSimilarity(bug);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
        
    	private void computeSimilarity(Bug bug) throws Exception {
    		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
    		ArrayList<IntegratedAnalysisValue> integratedAnalysisValues = rankedSuspFilesMap.get(bug.getID());
    		BugDAO bugDAO = new BugDAO();
    		
    		int bugID = bug.getID();
    		for (int i = 0; i < integratedAnalysisValues.size(); ++i) {
    			int sourceFileVersionID = integratedAnalysisValues.get(i).getSourceFileVersionID();
    			ArrayList<Method> methods = methodMap.get(sourceFileVersionID);
    			if (methods == null) {
    				SourceFileDAO sourceFileDAO = new SourceFileDAO();
    				System.err.printf("MethodAnalyzer.computeSimilarity()> File name without methods: %s, SF_VER_ID: %d\n",
    						sourceFileDAO.getSourceFileName(sourceFileVersionID), sourceFileVersionID);
    				continue;
    			}
    			for (int j = 0; j < methods.size(); ++j) {
    				Method method = methods.get(j);
    				int methodID = method.getID();
    				String methodName = method.getName();
    				
    				String methodInfo[] = FileParser.splitContent(methodName);
    				String stems = SourceFileCorpusCreator.stemContent(methodName) + SourceFileCorpusCreator.stemContent(methodInfo);
    				String terms[] = stems.split(" ");
    				
    				double vsmScore = 0.0;
    				double bugNorm = bugNormMap.get(bugID);
    				for (int k = 0; k < terms.length; ++k) {
    					AnalysisValue analysisValue = bugDAO.getBugSfTermWeight(bugID, terms[k]);
    					if (analysisValue != null) {
    						AnalysisValue methodTermAnalysisValue = bugDAO.getBugMthTermWeight(bugID, terms[k]);
    						if (methodTermAnalysisValue == null) {
    							System.err.printf("MethodAnalyzer.computeSimilarity()> Can't find bugID: %s, term: %s\n",
    									bugID, terms[k]);
    							return;
    						}
    						double methodTermWeight = methodTermAnalysisValue.getTf() * methodTermAnalysisValue.getIdf();
    						vsmScore += (analysisValue.getTf() * analysisValue.getIdf()) * methodTermWeight;
    					}
    				}
    				
    				double methodNorm = bugDAO.getNormValue(bugID);
    				vsmScore /= (bugNorm * methodNorm);
    				if (vsmScore == 0) continue;
    				
        			ExtendedIntegratedAnalysisValue integratedMethodAnalysisValue = new ExtendedIntegratedAnalysisValue();
        			integratedMethodAnalysisValue.setBugID(bugID);
        			integratedMethodAnalysisValue.setMethodID(methodID);
        			integratedMethodAnalysisValue.setVsmScore(vsmScore);
        			if (0 == integratedAnalysisDAO.updateMethodVsmScore(integratedMethodAnalysisValue)) {
        				integratedAnalysisDAO.insertMethodAnalysisVaule(integratedMethodAnalysisValue);	
        			}
    			}
    		}
    	}
    }
}
