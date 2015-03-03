/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.buglocator.analysis;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.db.IntegratedAnalysisValue;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.IntegratedAnalysisDAO;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.common.Bug;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugLocator {
 
    public BugLocator() throws Exception {
    }
	
    /**
     * Localize bug location with bug report and other information 
     * 
     * @throws Exception
     */
	public void analyze() throws Exception {
		String productName = Property.getInstance().getProductName();
		BugDAO bugDAO = new BugDAO();
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		
		ArrayList<Bug> bugs = bugDAO.getAllBugs(productName, false);
		
		double alpha = Property.getInstance().getAlpha();
		
		for (int i = 0; i < bugs.size(); i++) {
			String bugID = bugs.get(i).getID();
			HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues = integratedAnalysisDAO.getAnalysisValues(bugID);
			normalize(integratedAnalysisValues);
			combine(integratedAnalysisValues, alpha);
			
			Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
			while (integratedAnalysisValuesIter.hasNext()) {
				int sourceFileVersionID = integratedAnalysisValuesIter.next();
				
				IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
				int updatedColumenCount = integratedAnalysisDAO.updateBugLocatorScore(integratedAnalysisValue);
				
				if (0 == updatedColumenCount) {
					integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
				}

			}
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
	public void combine(HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues, double weightFactor) {
		Iterator<Integer> integratedAnalysisValuesIter = integratedAnalysisValues.keySet().iterator();
		while (integratedAnalysisValuesIter.hasNext()) {
			int sourceFileVersionID = integratedAnalysisValuesIter.next();
			IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValues.get(sourceFileVersionID);
			
			double vsmScore = integratedAnalysisValue.getVsmScore();
			double similarityScore = integratedAnalysisValue.getSimilarityScore();
			
			double finalScore = vsmScore * (1 - weightFactor) + similarityScore * weightFactor;
			integratedAnalysisValue.setBugLocatorScore(finalScore);
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
