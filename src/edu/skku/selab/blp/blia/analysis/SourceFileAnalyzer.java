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
import edu.skku.selab.blp.common.BugCorpus;
import edu.skku.selab.blp.common.SourceFileCorpus;
import edu.skku.selab.blp.db.AnalysisValue;
import edu.skku.selab.blp.db.IntegratedAnalysisValue;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.IntegratedAnalysisDAO;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.blia.indexer.SourceFileVectorCreator;

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
	public void analyze(String version, boolean useStructuredInfo) throws Exception {
		BugDAO bugDAO = new BugDAO();
		HashMap<String, Bug> bugs = bugDAO.getBugs();
		Iterator<Bug> bugsIter = bugs.values().iterator();
		while (bugsIter.hasNext()) {
			// calculate term count, IDC, TF and IDF
			Bug bug = bugsIter.next();

			// Compute similarity between Bug report & source files
			if (useStructuredInfo) {
				computeSimilarityWithStructuredInfo(bug, version);
			} else {
				computeSimilarity(bug, version);
			}
		}
	}
	
//	public void createVector(String version) throws Exception {
//		BugDAO bugDAO = new BugDAO();
//		HashMap<String, Bug> bugs = bugDAO.getBugs();
//		Property property = Property.getInstance();
//		String productName = property.getProductName();
//		
//		SourceFileDAO sourceFileDAO = new SourceFileDAO();
//		HashMap<String, Integer> sourceFileTermMap = sourceFileDAO.getTermMap(productName);
//		
//		SourceFileIndexer sourceFileIndexer = new SourceFileIndexer(); 
//		Hashtable<String, Integer> inverseDocCountTable = sourceFileIndexer.getInverseDocCountTable(version);
//		int fileCount = sourceFileDAO.getSourceFileCount(productName, version);
//		
//		String bugID = "";
//		int totalTermCount = 0;
//		int bugTermCount = 0;
//		int inverseDocCount = 0;
//		double tf = 0.0;
//		double idf = 0.0;
//		double termWeight = 0.0;
//		String bugTerm = "";
//		Iterator<String> bugsIter = bugs.keySet().iterator();
//		while (bugsIter.hasNext()) {
//			// calculate term count, IDC, TF and IDF
//			bugID = bugsIter.next();
//			
////			if (bugID.equalsIgnoreCase("75739")) {
//				Bug bug = bugs.get(bugID);
//				
//				String bugCorpusContent = bug.getCorpusContent();
//				
//				// get term count
//				String bugTermArray[] = bugCorpusContent.split(" ");
//				Hashtable<String, Integer> bugTermTable = new Hashtable<String, Integer>();
//				for (int i = 0; i < bugTermArray.length; i++) {
//					bugTerm = bugTermArray[i];
//					if (!bugTerm.trim().equals("")) {
//						if (bugTermTable.containsKey(bugTerm)) {
//							Integer count = (Integer) bugTermTable.get(bugTerm);
//							count = Integer.valueOf(count.intValue() + 1);
//							bugTermTable.remove(bugTerm);
//							bugTermTable.put(bugTerm, count);
//						} else {
//							bugTermTable.put(bugTerm, Integer.valueOf(1));
//						}
//					}
//				}
//				
//				// calculate totalTermCount
//				Iterator<String> bugTermTableIter = bugTermTable.keySet().iterator();
//				while (bugTermTableIter.hasNext()) {
//					bugTerm = bugTermTableIter.next();
//					bugTermCount = bugTermTable.get(bugTerm);
//					
//					if (sourceFileTermMap.containsKey(bugTerm)) {
//						totalTermCount += bugTermCount;
//					}
////						System.out.printf("Corpus: %s, termCount: %d\n", corpus, termCount);
//				}
//				
//				bugDAO.updateTotalTermCount(productName, bugID, totalTermCount);
////				System.out.printf("totalTermCount: %d\n", totalTermCount);
//				
//				double corpusNorm = 0.0D;
//				double summaryCorpusNorm = 0.0D;
//				double descriptionCorpusNorm = 0.0D;
//
//				HashSet<String> summaryTermSet = SourceFileVectorCreator.CorpusToSet(bug.getCorpus().getSummaryPart());
//				HashSet<String> descriptionTermSet = SourceFileVectorCreator.CorpusToSet(bug.getCorpus().getDescriptionPart());
//
//				bugTermTableIter = bugTermTable.keySet().iterator();
//				while (bugTermTableIter.hasNext()) {
//					bugTerm = bugTermTableIter.next();
//					if (sourceFileTermMap.containsKey(bugTerm)) {
//						bugTermCount = bugTermTable.get(bugTerm);
//						inverseDocCount = inverseDocCountTable.get(bugTerm).intValue();
//						
//						// calculate TF, IDF, Vector
//						tf = getTfValue(bugTermCount, totalTermCount);
//						idf = getIdfValue(inverseDocCount, fileCount);
//						termWeight = tf * idf;
//						double termWeightSquare = termWeight * termWeight;
//						corpusNorm += termWeightSquare;
//						
//						if (summaryTermSet.contains(bugTerm)) {
//							summaryCorpusNorm += termWeightSquare;
//						}
//
//						if (descriptionTermSet.contains(bugTerm)) {
//							descriptionCorpusNorm += termWeightSquare;
//						}
//						
//						AnalysisValue bugSfTermWeight = new AnalysisValue(bugID, productName, bugTerm, bugTermCount, inverseDocCount, tf, idf);						
//						bugDAO.insertBugSfTermWeight(bugSfTermWeight);
//					}
//				}
//
////				System.out.printf("word: %f\n", word);
//				corpusNorm = Math.sqrt(corpusNorm);
//				summaryCorpusNorm = Math.sqrt(summaryCorpusNorm);
//				descriptionCorpusNorm = Math.sqrt(descriptionCorpusNorm);
//
//				bugDAO.updateNormValues(productName, bugID, corpusNorm, summaryCorpusNorm, descriptionCorpusNorm);					
////			} 	// for testing
//		}
//	}
	
	private void computeSimilarity(Bug bug, String version) throws Exception {
		Property property = Property.getInstance();
		String productName = property.getProductName();

		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		HashMap<String, Integer> sourceFileVersionIDs = sourceFileDAO.getSourceFileVersionIDs(productName, version);
		
		BugDAO bugDAO = new BugDAO();
		HashMap<String, AnalysisValue> bugSfTermMap = bugDAO.getSfTermMap(bug.getID());
		
		Iterator<String> sourceFileVersionIDIter = sourceFileVersionIDs.keySet().iterator();
		while(sourceFileVersionIDIter.hasNext()) {
			int sourceFileVersionID = sourceFileVersionIDs.get(sourceFileVersionIDIter.next());

			// corpus, analysisValue
			HashMap<String, AnalysisValue> sourceFileTermMap = sourceFileDAO.getTermMap(sourceFileVersionID);
			if (sourceFileTermMap == null) {
//				System.err.printf("Wrong source file version ID: %d\n", sourceFileVersionID);
				continue;
			}
			
			double vsmScore = 0.0;
			Iterator<String> sourceFileTermIter = sourceFileTermMap.keySet().iterator();
			while (sourceFileTermIter.hasNext()) {
				String sourceFileTerm = sourceFileTermIter.next();
				double sourceFileTermWeight = sourceFileTermMap.get(sourceFileTerm).getTf() * sourceFileTermMap.get(sourceFileTerm).getIdf();
				
				double bugTermWeight = 0;
				AnalysisValue bugTermValue = bugSfTermMap.get(sourceFileTerm);
				if (null != bugTermValue) {
					bugTermWeight = bugTermValue.getTf() * bugTermValue.getIdf();
					
					// ASSERT code: IDF values must be same!
//					if (bugTermValue.getIdf() != sourceFileTermMap.get(sourceFileTerm).getIdf()) {
//						System.out.printf("Bug IDF: %f, Source IDF: %f\n", bugTermValue.getIdf(), sourceFileTermMap.get(sourceFileTerm).getIdf());
//					}
				} 
				
				vsmScore += (bugTermWeight * sourceFileTermWeight);
			}

			double sourceFileNorm = sourceFileDAO.getNormValue(sourceFileVersionID);
			double bugNorm = bugDAO.getNormValue(bug.getID());
			vsmScore = (vsmScore / (sourceFileNorm * bugNorm));
			vsmScore = vsmScore * sourceFileDAO.getLengthScore(sourceFileVersionID);
			
			IntegratedAnalysisValue integratedAnalysisValue = new IntegratedAnalysisValue();
			integratedAnalysisValue.setBugID(bug.getID());
			integratedAnalysisValue.setSourceFileVersionID(sourceFileVersionID);
			integratedAnalysisValue.setVsmScore(vsmScore);
			integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
		}
	}
	
	private void computeSimilarityWithStructuredInfo(Bug bug, String version) throws Exception {
		Property property = Property.getInstance();
		String productName = property.getProductName();

		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		HashMap<String, Integer> sourceFileVersionIDs = sourceFileDAO.getSourceFileVersionIDs(productName, version);
		
		BugDAO bugDAO = new BugDAO();
		HashMap<String, AnalysisValue> bugSfTermMap = bugDAO.getSfTermMap(bug.getID());
		
		Iterator<String> sourceFileVersionIDIter = sourceFileVersionIDs.keySet().iterator();
		while(sourceFileVersionIDIter.hasNext()) {
			int sourceFileVersionID = sourceFileVersionIDs.get(sourceFileVersionIDIter.next());

			double vsmScore = 0.0;
			// corpus, analysisValue
			HashMap<String, AnalysisValue> sourceFileTermMap = sourceFileDAO.getTermMap(sourceFileVersionID);
			if (sourceFileTermMap == null) {
//				System.err.printf("Wrong source file version ID: %d\n", sourceFileVersionID);
				continue;
			}
			
			SourceFileCorpus corpus = sourceFileDAO.getCorpus(sourceFileVersionID);
			String[] sourceFileCorpusSet = new String[4];
			sourceFileCorpusSet[0] = corpus.getClassPart();
			sourceFileCorpusSet[1] = corpus.getMethodPart();
			sourceFileCorpusSet[2] = corpus.getVariablePart();
			sourceFileCorpusSet[3] = corpus.getCommentPart();
			double[] sourceFileNormSet = new double[4];
			sourceFileNormSet[0] = corpus.getClassCorpusNorm();
			sourceFileNormSet[1] = corpus.getMethodCorpusNorm();
			sourceFileNormSet[2] = corpus.getVariableCorpusNorm();
			sourceFileNormSet[3] = corpus.getCommentCorpusNorm();
			
			BugCorpus bugCorpus = bug.getCorpus();
			String[] bugCorpusParts = new String[2];
			bugCorpusParts[0] = bugCorpus.getSummaryPart();
			bugCorpusParts[1] = bugCorpus.getDescriptionPart();
			double[] bugNormSet = new double[2];
			bugNormSet[0] = bugCorpus.getSummaryCorpusNorm();
			bugNormSet[1] = bugCorpus.getDecriptionCorpusNorm();
					
			for (int i = 0; i < sourceFileCorpusSet.length; i++) {
				for (int j = 0; j < bugCorpusParts.length; j++) {
					if (sourceFileCorpusSet[i] == "" || bugCorpusParts[j] == "") {
						continue;
					}
					
					String[] sourceFileTerms = sourceFileCorpusSet[i].split(" ");
					String[] bugTerms = bugCorpusParts[j].split(" ");
					HashSet<String> bugTermSet = new HashSet<String>();
					for (int k = 0; k < bugTerms.length; k++) {
						bugTermSet.add(bugTerms[k]);
					}
					
					double cosineSimilarityScore = 0.0;
					for (int k = 0; k < sourceFileTerms.length; k++) {
						if (bugTermSet.contains(sourceFileTerms[k])) {
							if (null == sourceFileTermMap.get(sourceFileTerms[k])) {
								System.out.printf("Exception occurred term: %s\n", sourceFileTerms[k]);
								continue;
							}
							
							AnalysisValue sourceFileTermValue = sourceFileTermMap.get(sourceFileTerms[k]);
							double sourceFileTermWeight = sourceFileTermValue.getIdf() * sourceFileTermValue.getIdf();
							
							double bugTermWeight = 0;
							AnalysisValue bugTermValue = bugSfTermMap.get(sourceFileTerms[k]);
							if (null != bugTermValue) {
								bugTermWeight = bugTermValue.getTf() * bugTermValue.getIdf();
							}
							
							cosineSimilarityScore += (bugTermWeight * sourceFileTermWeight);
						}
					}
					
					if (sourceFileNormSet[i] != 0 && bugNormSet[j] != 0) {
//						System.out.printf("cosineSimilarityScore: %f, sourceFileNormSet[%d]: %f, bugNormSet[%d]: %f\n",
//								cosineSimilarityScore, i, sourceFileNormSet[i], j, bugNormSet[j]);
						vsmScore += (cosineSimilarityScore / (sourceFileNormSet[i] * bugNormSet[j]));
					}
				}
			}
			
			vsmScore = vsmScore * sourceFileDAO.getLengthScore(sourceFileVersionID);
			IntegratedAnalysisValue integratedAnalysisValue = new IntegratedAnalysisValue();
			integratedAnalysisValue.setBugID(bug.getID());
			integratedAnalysisValue.setSourceFileVersionID(sourceFileVersionID);
			integratedAnalysisValue.setVsmScore(vsmScore);
			integratedAnalysisDAO.insertAnalysisVaule(integratedAnalysisValue);
		}
	}

//	/**
//	 * Get term frequency value
//	 * 
//	 * @param freq
//	 * @param totalTermCount
//	 * @return
//	 */
//	private float getTfValue(int freq, int totalTermCount) {
//		return (float) Math.log(freq) + 1.0F;
//	}
//
//	/**
//	 * Get inverse document frequency value
//	 * 
//	 * @param docCount
//	 * @param totalCount
//	 * @return
//	 */
//	private float getIdfValue(double docCount, double totalCount) {
//		return (float) Math.log(totalCount / docCount);
//	}
}
