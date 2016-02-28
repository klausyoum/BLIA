/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import edu.skku.selab.blp.common.FileParser;
import edu.skku.selab.blp.common.Method;
import edu.skku.selab.blp.db.AnalysisValue;
import edu.skku.selab.blp.db.IntegratedAnalysisValue;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.SourceFileDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugMethodVectorCreator extends BugSourceFileVectorCreator {
	private HashMap<Integer, ArrayList<Method>> methodMap = null;
	
	public BugMethodVectorCreator(HashMap<Integer, ArrayList<Method>> methodMap) {
		this.methodMap = methodMap;
	}
	
	private Hashtable<String, Integer> getInverseDocCountTable(ArrayList<String[]> docList) {
		Hashtable<String, Integer> inverseDocCountTable = new Hashtable<String, Integer>();
		
		for (int i = 0; i < docList.size(); ++i) {
			String[] corpuses = docList.get(i);
			
			TreeSet<String> wordSet = new TreeSet<String>();
			for (int j = 0; j < corpuses.length; j++) {
				String word = corpuses[j];
				if (!word.trim().equals("") && !wordSet.contains(word)) {
					wordSet.add(word);
				}
			}
			
			Iterator<String> iterator = wordSet.iterator();
			while (iterator.hasNext()) {
				String word = iterator.next();
				if (inverseDocCountTable.containsKey(word)) {
					Integer count = (Integer) inverseDocCountTable.get(word) + 1;
					inverseDocCountTable.remove(word);
					inverseDocCountTable.put(word, count);
				} else {
					inverseDocCountTable.put(word, 1);
				}
			}
		}
		
		return inverseDocCountTable;
	}
	
	public void create(String version, HashMap<Integer, ArrayList<IntegratedAnalysisValue>> rankedSuspFilesMap) throws Exception {
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		HashMap<String, Integer> sourceFileTermMap = sourceFileDAO.getTermMap();
		BugDAO bugDAO = new BugDAO();
		
		String methodTerm = "";
		int totalTermCount = 0;
		int methodTermCount = 0;
		int inverseDocCount = 0;
		double tf = 0.0;
		double idf = 0.0;
		double termWeight = 0.0;
		int methodCount = 0;
		
		Iterator<Integer> bugIdIter = rankedSuspFilesMap.keySet().iterator();
		while (bugIdIter.hasNext()) {
			int bugID = bugIdIter.next();
			methodCount = 0;
			
			ArrayList<String[]> docList = new ArrayList<String[]>();
			ArrayList<IntegratedAnalysisValue> integratedAnalysisValues = rankedSuspFilesMap.get(bugID);
			Hashtable<String, Integer> methodTermTable = new Hashtable<String, Integer>();
			for (int i = 0; i < integratedAnalysisValues.size(); ++i) {
				int sourceFileVersionID = integratedAnalysisValues.get(i).getSourceFileVersionID();
				ArrayList<Method> methods = methodMap.get(sourceFileVersionID);
				if (methods == null) {
					System.err.printf("BugMethodVectorCreator.create()> File name without methods: %s, SF_VER_ID: %d\n",
							sourceFileDAO.getSourceFileName(sourceFileVersionID), sourceFileVersionID);
					continue;
				}
				
				methodCount += methods.size();				
				for (int j = 0; j < methods.size(); ++j) {
					Method method = methods.get(j);
					String methodName = method.getName();
					
					String methodInfo[] = FileParser.splitContent(methodName);
					String stems = SourceFileCorpusCreator.stemContent(methodName) + SourceFileCorpusCreator.stemContent(methodInfo);
					String terms[] = stems.split(" ");
					
					// get term count
					for (int k = 0; k < terms.length; k++) {
						methodTerm = terms[k];
						if (!methodTerm.trim().equals("")) {
							if (methodTermTable.containsKey(methodTerm)) {
								Integer count = (Integer) methodTermTable.get(methodTerm);
								count = Integer.valueOf(count.intValue() + 1);
								methodTermTable.remove(methodTerm);
								methodTermTable.put(methodTerm, count);
							} else {
								methodTermTable.put(methodTerm, Integer.valueOf(1));
							}
						}
					}
					
					docList.add(terms);
				}
			}
			
			totalTermCount = 0;
			// calculate totalTermCount
			Iterator<String> methodTermTableIter = methodTermTable.keySet().iterator();
			while (methodTermTableIter.hasNext()) {
				methodTerm = methodTermTableIter.next();
				methodTermCount = methodTermTable.get(methodTerm);
				
				if (sourceFileTermMap.containsKey(methodTerm)) {
					totalTermCount += methodTermCount;
				}
//						System.out.printf("Corpus: %s, termCount: %d\n", corpus, termCount);
			}
	
			bugDAO.updateMethodTotalTermCount(bugID, totalTermCount);
//			System.out.printf("totalTermCount: %d\n", totalTermCount);
			
			
			Hashtable<String, Integer> inverseDocCountTable = getInverseDocCountTable(docList);
			double methodNorm = 0.0D;

			methodTermTableIter = methodTermTable.keySet().iterator();
			while (methodTermTableIter.hasNext()) {
				methodTerm = methodTermTableIter.next();
				
				// test code
//				System.out.println("methodTerm:" + methodTerm);
				if (sourceFileTermMap.containsKey(methodTerm)) {
					methodTermCount = methodTermTable.get(methodTerm);
					inverseDocCount = inverseDocCountTable.get(methodTerm).intValue();
					
					// calculate TF, IDF, Vector
					tf = getTfValue(methodTermCount, totalTermCount);
					idf = getIdfValue(inverseDocCount, methodCount);
					termWeight = tf * idf;
					double termWeightSquare = termWeight * termWeight;
					methodNorm += termWeightSquare;
					
					AnalysisValue bugMethodTermWeight = new AnalysisValue(bugID, methodTerm, methodTermCount, inverseDocCount, tf, idf);						
					bugDAO.insertBugMthTermWeight(bugMethodTermWeight);
				}
			}

//				System.out.printf("word: %f\n", word);
			methodNorm = Math.sqrt(methodNorm);
			bugDAO.updateMthNormValues(bugID, methodNorm);			
		}
		
////////////////////////////////////////////////////////////////////////////////////////
//		HashMap<Integer, Bug> bugs = bugDAO.getBugs();
//		
//		SourceFileDAO sourceFileDAO = new SourceFileDAO();
//		HashMap<String, Integer> sourceFileTermMap = sourceFileDAO.getTermMap();
//		
//		SourceFileVectorCreator sourceFileVectorCreator = new SourceFileVectorCreator(); 
//		Hashtable<String, Integer> inverseDocCountTable = sourceFileVectorCreator.getInverseDocCountTable(version);
//		int fileCount = sourceFileDAO.getSourceFileCount(version);
//		
//		int bugID = 0;
//		int totalTermCount = 0;
//		int bugTermCount = 0;
//		int inverseDocCount = 0;
//		double tf = 0.0;
//		double idf = 0.0;
//		double termWeight = 0.0;
//		String bugTerm = "";
//		Iterator<Integer> bugsIter = bugs.keySet().iterator();
//		while (bugsIter.hasNext()) {
//			// calculate term count, IDC, TF and IDF
//			bugID = bugsIter.next();
//			
//			// debug code
////			if (bugID.contains("99145")) {
////				System.out.println("BugSourceFileVectorCreator.create(): " + bugID);
////			}
//
//			Bug bug = bugs.get(bugID);
//			
//			String bugCorpusContent = bug.getCorpusContent();
//			
//			// get term count
//			String bugTermArray[] = bugCorpusContent.split(" ");
//			Hashtable<String, Integer> bugTermTable = new Hashtable<String, Integer>();
//			for (int i = 0; i < bugTermArray.length; i++) {
//				bugTerm = bugTermArray[i];
//				if (!bugTerm.trim().equals("")) {
//					if (bugTermTable.containsKey(bugTerm)) {
//						Integer count = (Integer) bugTermTable.get(bugTerm);
//						count = Integer.valueOf(count.intValue() + 1);
//						bugTermTable.remove(bugTerm);
//						bugTermTable.put(bugTerm, count);
//					} else {
//						bugTermTable.put(bugTerm, Integer.valueOf(1));
//					}
//				}
//			}
//			
//			totalTermCount = 0;
//			// calculate totalTermCount
//			Iterator<String> bugTermTableIter = bugTermTable.keySet().iterator();
//			while (bugTermTableIter.hasNext()) {
//				bugTerm = bugTermTableIter.next();
//				bugTermCount = bugTermTable.get(bugTerm);
//				
//				if (sourceFileTermMap.containsKey(bugTerm)) {
//					totalTermCount += bugTermCount;
//				}
////						System.out.printf("Corpus: %s, termCount: %d\n", corpus, termCount);
//			}
//			
//			bugDAO.updateTotalTermCount(bugID, totalTermCount);
////				System.out.printf("totalTermCount: %d\n", totalTermCount);
//			
//			double corpusNorm = 0.0D;
//			double summaryCorpusNorm = 0.0D;
//			double descriptionCorpusNorm = 0.0D;
//
//			HashSet<String> summaryTermSet = SourceFileVectorCreator.CorpusToSet(bug.getCorpus().getSummaryPart());
//			HashSet<String> descriptionTermSet = SourceFileVectorCreator.CorpusToSet(bug.getCorpus().getDescriptionPart());
//
//			bugTermTableIter = bugTermTable.keySet().iterator();
//			while (bugTermTableIter.hasNext()) {
//				bugTerm = bugTermTableIter.next();
//				
//				// test code
////				System.out.println("bugTerm:" + bugTerm);
//				if (sourceFileTermMap.containsKey(bugTerm)) {
//					bugTermCount = bugTermTable.get(bugTerm);
//					inverseDocCount = inverseDocCountTable.get(bugTerm).intValue();
//					
//					// calculate TF, IDF, Vector
//					tf = getTfValue(bugTermCount, totalTermCount);
//					idf = getIdfValue(inverseDocCount, fileCount);
//					termWeight = tf * idf;
//					double termWeightSquare = termWeight * termWeight;
//					corpusNorm += termWeightSquare;
//					
//					if (summaryTermSet.contains(bugTerm)) {
//						summaryCorpusNorm += termWeightSquare;
//					}
//
//					if (descriptionTermSet.contains(bugTerm)) {
//						descriptionCorpusNorm += termWeightSquare;
//					}
//					
//					AnalysisValue bugSfTermWeight = new AnalysisValue(bugID, bugTerm, bugTermCount, inverseDocCount, tf, idf);						
//					bugDAO.insertBugSfTermWeight(bugSfTermWeight);
//				}
//			}
//
////				System.out.printf("word: %f\n", word);
//			corpusNorm = Math.sqrt(corpusNorm);
//			summaryCorpusNorm = Math.sqrt(summaryCorpusNorm);
//			descriptionCorpusNorm = Math.sqrt(descriptionCorpusNorm);
//
//			bugDAO.updateNormValues(bugID, corpusNorm, summaryCorpusNorm, descriptionCorpusNorm);					
//		}
	}
}
