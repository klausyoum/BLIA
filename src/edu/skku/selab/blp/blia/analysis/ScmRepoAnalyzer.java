/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.common.Bug;
import edu.skku.selab.blp.common.CommitInfo;
import edu.skku.selab.blp.common.ExtendedCommitInfo;
import edu.skku.selab.blp.common.Method;
import edu.skku.selab.blp.db.IntegratedAnalysisValue;
import edu.skku.selab.blp.db.ExtendedIntegratedAnalysisValue;
import edu.skku.selab.blp.db.dao.BaseDAO;
import edu.skku.selab.blp.db.dao.CommitDAO;
import edu.skku.selab.blp.db.dao.IntegratedAnalysisDAO;
import edu.skku.selab.blp.db.dao.MethodDAO;
import edu.skku.selab.blp.db.dao.SourceFileDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class ScmRepoAnalyzer {
	private ArrayList<Bug> bugs;
	private int pastDays;
	private ArrayList<ExtendedCommitInfo> filteredCommitInfos = null;
	
	public ScmRepoAnalyzer() {
		bugs = null;
	}
	
    public ScmRepoAnalyzer(ArrayList<Bug> bugs) {
    	this.bugs = bugs;
		pastDays = Property.getInstance().getPastDays();
    }
    
    private class WorkerThread implements Runnable {
    	private Bug bug;
    	private String version;
    	
        public WorkerThread(Bug bug, String version) {
            this.bug = bug;
            this.version = version;
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
    		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
    		MethodDAO methodDAO = new MethodDAO();
    		SourceFileDAO sourceFileDAO = new SourceFileDAO();
    		
			// <fileName, analysisValue>
			HashMap<String, IntegratedAnalysisValue> analysisValues = new HashMap<String, IntegratedAnalysisValue>();
			HashMap<Integer, ExtendedIntegratedAnalysisValue> methodAnalysisValues = new HashMap<Integer, ExtendedIntegratedAnalysisValue>();
			ArrayList<ExtendedCommitInfo> relatedCommitInfos = findCommitInfoWithinDays(filteredCommitInfos, bug.getOpenDate(), pastDays);
			if (null == relatedCommitInfos) {
				return;
			}
			
			for (int j = 0; j < relatedCommitInfos.size(); j++) {
				ExtendedCommitInfo relatedCommitInfo = relatedCommitInfos.get(j);
				HashSet<String> commitFiles = relatedCommitInfo.getAllCommitFilesWithoutCommitType();
				Iterator<String> commitFilesIter = commitFiles.iterator();
				while (commitFilesIter.hasNext()) {
					String commitFileName = commitFilesIter.next();

					// Calculate CommitLogScore for file level
					IntegratedAnalysisValue analysisValue = analysisValues.get(commitFileName);
					if (null == analysisValue) {
						analysisValue = new IntegratedAnalysisValue();
						analysisValue.setBugID(bug.getID());
						analysisValue.setVersion(version);
						analysisValue.setFileName(commitFileName);
					}
					
					double commitLogScore = analysisValue.getCommitLogScore();
					commitLogScore += calculateCommitLogScore(relatedCommitInfo.getCommitDate(), bug.getOpenDate(), pastDays);
					analysisValue.setCommitLogScore(commitLogScore);
					
					if (null == analysisValues.get(commitFileName)) {
						analysisValues.put(commitFileName, analysisValue);		
					}
				}
				
				HashMap<String, ArrayList<Method>> allCommitMethods = relatedCommitInfo.getAllFixedMethods();
				if (allCommitMethods == null) {
//					System.err.printf("[NO fixed method!] BugID: %d, Commit ID: %s\n", bug.getID(), relatedCommitInfo.getCommitID());
					continue;
				}
				
				commitFilesIter = allCommitMethods.keySet().iterator();
				while (commitFilesIter.hasNext()) {
					String commitFileName = commitFilesIter.next();
					ArrayList<Method> commitMethods = allCommitMethods.get(commitFileName);
					
					for (int i = 0; i < commitMethods.size(); ++i) {
						Method method = commitMethods.get(i);
						int methodID = methodDAO.getMethodID(method);

						if (methodID == BaseDAO.INVALID) {
							int sourceFileVersionID = sourceFileDAO.getSourceFileVersionID(commitFileName, SourceFileDAO.DEFAULT_VERSION_STRING);
							method.setSourceFileVersionID(sourceFileVersionID);
							methodID = methodDAO.insertMethod(method);
						}

						ExtendedIntegratedAnalysisValue methodAnalysisValue = methodAnalysisValues.get(methodID);
						if (null == methodAnalysisValue) {
							methodAnalysisValue = new ExtendedIntegratedAnalysisValue();
							methodAnalysisValue.setBugID(bug.getID());
							methodAnalysisValue.setMethodID(methodID);
						}

						// Calculate CommitLogScore for method level
						double commitLogScore = methodAnalysisValue.getCommitLogScore();
						commitLogScore += calculateCommitLogScore(relatedCommitInfo.getCommitDate(), bug.getOpenDate(), pastDays);
						methodAnalysisValue.setCommitLogScore(commitLogScore);
						
						if (null == methodAnalysisValues.get(methodID)) {
							methodAnalysisValues.put(methodID, methodAnalysisValue);		
						}
					}
				}
			}
			
			// Then save the score for the fixed files
			Iterator<IntegratedAnalysisValue> analysisValueIter = analysisValues.values().iterator();
			while (analysisValueIter.hasNext()) {
				IntegratedAnalysisValue analysisValue = analysisValueIter.next();
				integratedAnalysisDAO.updateCommitLogScore(analysisValue);

				// DEBUG code
//				if (0 == updatedColumenCount) {
//					System.err.printf("[ERROR] ScmRepoAnalyzer.analyze(): CommitLog score update failed! BugID: %s, sourceFileVersionID: %d\n",
//							analysisValue.getBugID(), analysisValue.getSourceFileVersionID());
//				}
			}
			
			// Then save the score for the fixed methods
			Iterator<ExtendedIntegratedAnalysisValue> methodAnalysisValueIter = methodAnalysisValues.values().iterator();
			while (methodAnalysisValueIter.hasNext()) {
				ExtendedIntegratedAnalysisValue methodAnalysisValue = methodAnalysisValueIter.next();
				integratedAnalysisDAO.insertMethodAnalysisVaule(methodAnalysisValue);

				// DEBUG code
//				if (0 == insertedColumnCount) {
//					System.err.printf("[ERROR] ScmRepoAnalyzer.analyze(): CommitLog score insertion failed! BugID: %s, methodID: %d\n",
//							methodAnalysisValue.getBugID(), methodAnalysisValue.getMethodID());
//				}
			}
        }
    }
    
	public void analyze(String version) throws Exception {
		// Do loop from the oldest bug,
		CommitDAO commitDAO = new CommitDAO();
		
		// Checked the "filtered". This variable is valid when it is true
		boolean filtered = true;
		filteredCommitInfos = commitDAO.getCommitInfos(filtered);

		ExecutorService executor = Executors.newFixedThreadPool(Property.THREAD_COUNT);
		for (int i = 0; i < bugs.size(); i++) {
			Runnable worker = new WorkerThread(bugs.get(i), version);
			executor.execute(worker);
		}
		
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
	}
	
	private double calculateCommitLogScore(Date commitDate, Date openDate, Integer pastDays) {
		double diffDays = getDiffDays(commitDate, openDate);
		double returnValue = 1.0 / (1 + Math.exp(12 * (1 - ((pastDays - diffDays) / pastDays))));		
		return returnValue;
	}

	private double getDiffDays(Date sourceDate, Date targetDate) {
		long diff = targetDate.getTime() - sourceDate.getTime();
	    double diffDays = diff / (24.0 * 60 * 60 * 1000);
		
	    return diffDays;
	}

	private ArrayList<ExtendedCommitInfo> findCommitInfoWithinDays(ArrayList<ExtendedCommitInfo> allCommitInfo, Date openDate, Integer pastDays) {
		ArrayList<ExtendedCommitInfo> foundCommitInfos = null;
		for (int i = 0; i < allCommitInfo.size(); i++) {
			ExtendedCommitInfo commitInfo = allCommitInfo.get(i);
			
			Date commitDate = commitInfo.getCommitDate();
		    double diffDays = getDiffDays(commitDate, openDate);
			
		    if (diffDays > pastDays) {
		    	continue;
		    }

	        if ((diffDays > 0) && (diffDays <= pastDays)) {
				if (null == foundCommitInfos) {
					foundCommitInfos = new ArrayList<ExtendedCommitInfo>();
				}

				foundCommitInfos.add(commitInfo);						
			} else {
				break;
			}			
		}
		
		return foundCommitInfos;
	}
}
