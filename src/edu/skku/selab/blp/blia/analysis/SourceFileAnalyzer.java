/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.blia.indexer.SourceFileIndexer;
import edu.skku.selab.blp.common.Bug;
import edu.skku.selab.blp.db.AnalysisValue;
import edu.skku.selab.blp.db.IntegratedAnalysisValue;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.IntegratedAnalysisDAO;
import edu.skku.selab.blp.db.dao.SourceFileDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileAnalyzer {
    public SourceFileAnalyzer() {
    }
	
	/**
	 * Calculate VSM score between source files and each bug report 
	 * 
	 * @see edu.skku.selab.blp.analysis.IAnalyzer#analyze()
	 */
	public void analyze(String version) throws Exception {
		BugDAO bugDAO = new BugDAO();
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		HashMap<String, Bug> bugs = bugDAO.getBugs();
		Property property = Property.getInstance();
		String productName = property.getProductName();
		
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		HashMap<String, Integer> corpusTable = sourceFileDAO.getCorpuses(productName);
		
		SourceFileIndexer sourceFileIndexer = new SourceFileIndexer(); 
		Hashtable<String, Integer> inverseDocCountTable = sourceFileIndexer.getInverseDocCountTable(version);
		int fileCount = sourceFileDAO.getSourceFileCount(productName, version);
		
		String bugID = "";
		int totalTermCount = 0;
		int termCount = 0;
		int inverseDocCount = 0;
		double tf = 0.0;
		double idf = 0.0;
		double vector = 0.0;
		String corpusSet = "";
		String corpus = "";
		Iterator<String> bugsIter = bugs.keySet().iterator();
		while (bugsIter.hasNext()) {
			// calculate term count, IDC, TF and IDF
			bugID = bugsIter.next();
			
//			if (bugID.equalsIgnoreCase("75739")) {
				Bug bug = bugs.get(bugID);
				corpusSet = bug.getCorpuses();
				
				// get term count
				String termArray[] = corpusSet.split(" ");
				Hashtable<String, Integer> termTable = new Hashtable<String, Integer>();
				for (int i = 0; i < termArray.length; i++) {
					corpus = termArray[i];
					if (!corpus.trim().equals("")) {
						if (termTable.containsKey(corpus)) {
							Integer count = (Integer) termTable.get(corpus);
							count = Integer.valueOf(count.intValue() + 1);
							termTable.remove(corpus);
							termTable.put(corpus, count);
						} else {
							termTable.put(corpus, Integer.valueOf(1));
						}
					}
				}
				
				// calculate totalTermCount
				Iterator<String> termTableIter = termTable.keySet().iterator();
				while (termTableIter.hasNext()) {
					corpus = termTableIter.next();
					termCount = termTable.get(corpus);
					
					if (corpusTable.containsKey(corpus)) {
						totalTermCount += termCount;
					}
//						System.out.printf("Corpus: %s, termCount: %d\n", corpus, termCount);
				}
				
				bugDAO.updateTotalCoupusCount(productName, bugID, totalTermCount);
//				System.out.printf("totalTermCount: %d\n", totalTermCount);

				HashMap<String, AnalysisValue> analysisValues = new HashMap<String, AnalysisValue>();
				termTableIter = termTable.keySet().iterator();
				while (termTableIter.hasNext()) {
					corpus = termTableIter.next();
					if (corpusTable.containsKey(corpus)) {
						termCount = termTable.get(corpus);
						inverseDocCount = inverseDocCountTable.get(corpus).intValue();

						
						// calculate TF, IDF, Vector
						tf = getTfValue(termCount, totalTermCount);
						idf = getIdfValue(inverseDocCount, fileCount);
						vector = tf * idf;
						
//						System.out.printf("corpus: %s, termCount: %d, idc: %d, tf: %f, idf: %f, vector: %f\n",
//								corpus, termCount, inverseDocCount, tf, idf, vector);
						
						AnalysisValue analysisValue = new AnalysisValue(bugID, productName, corpus, termCount, inverseDocCount, tf, idf, vector);
						analysisValues.put(corpus, analysisValue); // corpus bugVectors
					}
				}

				Iterator<String> analysisValuesIter = analysisValues.keySet().iterator();
				double word = 0.0D;
				while (analysisValuesIter.hasNext()) {
					AnalysisValue analysisValue = analysisValues.get(analysisValuesIter.next());
					word += (analysisValue.getVector() * analysisValue.getVector());
				}
//				System.out.printf("word: %f\n", word);
				
				word = Math.sqrt(word);
				analysisValuesIter = analysisValues.keySet().iterator();
				while (analysisValuesIter.hasNext()) {
					AnalysisValue analysisValue = analysisValues.get(analysisValuesIter.next());
					analysisValue.setVector(analysisValue.getVector() / word);
					
//					if (analysisValue.getVector() != 0.0f) {
//						System.out.printf("corpus: %s, bugVector: %f\n",
//								analysisValue.getCorpus(), analysisValue.getVector());
//					}
				}
				
				// Compute similarity between Bug report & source files
				HashSet<IntegratedAnalysisValue> integratedAnalysisValues = computeSimilarity(bugID, analysisValues, version);
				Iterator<IntegratedAnalysisValue> integratedAnalysisValuesIter = integratedAnalysisValues.iterator();
				while (integratedAnalysisValuesIter.hasNext()) {
					IntegratedAnalysisValue integratedAnalysisValue = integratedAnalysisValuesIter.next();
					integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
				}	
//			}
		}
	}
	
	private HashSet<IntegratedAnalysisValue> computeSimilarity(String bugID, HashMap<String, AnalysisValue> bugVectors, String version) throws Exception {
		Property property = Property.getInstance();
		String productName = property.getProductName();

		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		HashMap<String, Integer> sourceFileVersionIDs = sourceFileDAO.getSourceFileVersionIDs(productName, version);
		
		HashSet<IntegratedAnalysisValue> integratedAnalysisValues = new HashSet<IntegratedAnalysisValue>();
		Iterator<String> sourceFileVersionIDIter = sourceFileVersionIDs.keySet().iterator();
		while(sourceFileVersionIDIter.hasNext()) {
			int sourceFileVersionID = sourceFileVersionIDs.get(sourceFileVersionIDIter.next());

			double vsmScore = 0.0;
			// corpus, analysisValue
			HashMap<String, AnalysisValue> codeVectors = sourceFileDAO.getSourceFileAnalysisValues(sourceFileVersionID);
			if (codeVectors == null) {
//				System.err.printf("Wrong source file version ID: %d\n", sourceFileVersionID);
				continue;
			}
			
			Iterator<String> codeVectorsIter = codeVectors.keySet().iterator();
			while (codeVectorsIter.hasNext()) {
				String corpus = codeVectorsIter.next();
				double codeVector = codeVectors.get(corpus).getVector();
				
				double bugVector = 0;
				AnalysisValue bugVectorObj = bugVectors.get(corpus);
				if (null != bugVectorObj) {
					bugVector = bugVectorObj.getVector();	
				} 
				
				vsmScore += (bugVector * codeVector);
			}
			
			IntegratedAnalysisValue integratedAnalysisValue = new IntegratedAnalysisValue();
			integratedAnalysisValue.setBugID(bugID);
			integratedAnalysisValue.setSourceFileVersionID(sourceFileVersionID);
			integratedAnalysisValue.setVsmScore(vsmScore);
			integratedAnalysisValues.add(integratedAnalysisValue);
		}
		
		return integratedAnalysisValues;
	}

	/**
	 * Get term frequency value
	 * 
	 * @param freq
	 * @param totalTermCount
	 * @return
	 */
	private float getTfValue(int freq, int totalTermCount) {
		return (float) Math.log(freq) + 1.0F;
	}

	/**
	 * Get inverse document frequency value
	 * 
	 * @param docCount
	 * @param totalCount
	 * @return
	 */
	private float getIdfValue(double docCount, double totalCount) {
		return (float) Math.log(totalCount / docCount);
	}
}
