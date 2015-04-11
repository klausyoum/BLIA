/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.analysis;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.common.Bug;
import edu.skku.selab.blp.db.CommitInfo;
import edu.skku.selab.blp.db.IntegratedAnalysisValue;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.CommitDAO;
import edu.skku.selab.blp.db.dao.IntegratedAnalysisDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class ScmRepoAnalyzer {
	private ArrayList<Bug> bugs;
	
	public ScmRepoAnalyzer() {
		bugs = null;
	}
	
    public ScmRepoAnalyzer(ArrayList<Bug> bugs) {
    	this.bugs = bugs;
    }
    
	public void analyze(String version) throws Exception {
		// Do loop from the oldest bug,
		Property property = Property.getInstance();
		String productName = property.getProductName();
		int pastDays = property.getPastDays();
		
		CommitDAO commitDAO = new CommitDAO();
		ArrayList<CommitInfo> filteredCommitInfos = commitDAO.getFilteredCommitInfos(productName);
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		
		for (int i = 0; i < bugs.size(); i++) {
			Bug bug = bugs.get(i);
			
			// <fileName, analysisValue>
			HashMap<String, IntegratedAnalysisValue> analysisValues = new HashMap<String, IntegratedAnalysisValue>();
			ArrayList<CommitInfo> relatedCommitInfos = findCommitInfoWithinDays(filteredCommitInfos, bug.getOpenDate(), pastDays);
			if (null == relatedCommitInfos) {
				continue;
			}
			
			for (int j = 0; j < relatedCommitInfos.size(); j++) {
				CommitInfo relatedCommitInfo = relatedCommitInfos.get(j);
				HashSet<String> commitFiles = relatedCommitInfo.getAllCommitFilesWithoutCommitType();
				Iterator<String> commitFilesIter = commitFiles.iterator();
				while (commitFilesIter.hasNext()) {
					String commitFileName = commitFilesIter.next();

					// Calculate CommitLogScore
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
			}
			
			// Then save the score for the fixed files
			Iterator<IntegratedAnalysisValue> analysisValueIter = analysisValues.values().iterator();
			while (analysisValueIter.hasNext()) {
				IntegratedAnalysisValue analysisValue = analysisValueIter.next();
				int updatedColumenCount = integratedAnalysisDAO.updateCommitLogScore(analysisValue);
				
				if (0 == updatedColumenCount) {
					System.err.printf("[ERROR] ScmRepoAnalyzer.analyze(): CommitLog score update failed! BugID: %s, sourceFileVersionID: %d\n",
							analysisValue.getBugID(), analysisValue.getSourceFileVersionID());
					// remove following line after testing.
//					integratedAnalysisDAO.insertAnalysisVaule(analysisValue);
				}

			}
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

	private ArrayList<CommitInfo> findCommitInfoWithinDays(ArrayList<CommitInfo> allCommitInfo, Date openDate, Integer pastDays) {
		ArrayList<CommitInfo> foundCommitInfos = null;
		for (int i = 0; i < allCommitInfo.size(); i++) {
			CommitInfo commitInfo = allCommitInfo.get(i);
			
			Date commitDate = commitInfo.getCommitDate();
		    double diffDays = getDiffDays(commitDate, openDate);
			
		    if (diffDays > pastDays) {
		    	continue;
		    }

	        if ((diffDays > 0) && (diffDays <= pastDays)) {
				if (null == foundCommitInfos) {
					foundCommitInfos = new ArrayList<CommitInfo>();
				}

				foundCommitInfos.add(commitInfo);						
			} else {
				break;
			}			
		}
		
		return foundCommitInfos;
	}
}
