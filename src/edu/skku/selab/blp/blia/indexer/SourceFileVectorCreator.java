/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.indexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.common.SourceFileCorpus;
import edu.skku.selab.blp.db.AnalysisValue;
import edu.skku.selab.blp.db.dao.SourceFileDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileVectorCreator {
    
    public SourceFileVectorCreator() {
    }
    
    public static HashSet<String> CorpusToSet(String corpus) {
    	HashSet<String> termSet = new HashSet<String>();
    	
    	String[] stringArray = corpus.split(" ");
   		for (int i = 0; i < stringArray.length; i++) {
   			termSet.add(stringArray[i]);
    	}
    	
    	return termSet;
    }

	/* (non-Javadoc)
	 * @see edu.skku.selab.blia.indexer.IVectorCreator#create()
	 */
	public void create(String version) throws Exception {
		Property property = Property.getInstance();
		String productName = property.getProductName();
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		HashMap<String, Integer> totalCorpusLengths = sourceFileDAO.getTotalCorpusLengths(productName, version);
		
		// Calculate vector
		Iterator<String> fileNameIter = totalCorpusLengths.keySet().iterator();
		int fileCount = sourceFileDAO.getSourceFileCount(productName, version);

		HashMap<String, SourceFileCorpus> sourceFileCorpusMap = sourceFileDAO.getCorpusMap(productName, version);
		while (fileNameIter.hasNext()) {
			String fileName = fileNameIter.next();
			
//			if (fileName.equalsIgnoreCase("org.eclipse.swt.internal.win32.NMCUSTOMDRAW.java")) {
				Integer totalTermCount = totalCorpusLengths.get(fileName);
				
				HashMap<String, AnalysisValue> sourceFileTermMap = sourceFileDAO.getTermMap(productName, fileName, version);
				if (sourceFileTermMap == null) {
					// debug code
					System.out.printf("[SourceFileVectorCreator.create()] The file name that has no valid terms: %s\n", fileName);
					continue;
				}

				double corpusNorm = 0.0D;
				double classCorpusNorm = 0.0D;
				double methodCorpusNorm = 0.0D;
				double variableNorm = 0.0D;
				double commentNorm = 0.0D;
				
				SourceFileCorpus sourceFileCorpus = sourceFileCorpusMap.get(fileName);
				HashSet<String> classTermSet = CorpusToSet(sourceFileCorpus.getClassPart());
				HashSet<String> methodTermSet = CorpusToSet(sourceFileCorpus.getMethodPart());
				HashSet<String> variableTermSet = CorpusToSet(sourceFileCorpus.getVariablePart());
				HashSet<String> commentTermSet = CorpusToSet(sourceFileCorpus.getCommentPart());				
				
				
				Iterator<String> sourceFileTermIter = sourceFileTermMap.keySet().iterator();
				while (sourceFileTermIter.hasNext()) {
					String term = sourceFileTermIter.next();
					AnalysisValue termWeight = sourceFileTermMap.get(term);
					double tf = getTfValue(termWeight.getTermCount(), totalTermCount.intValue());
					double idf = getIdfValue(termWeight.getInvDocCount(), fileCount);
					double termWeightValue = (tf * idf);
					double termWeightValueSquare = termWeightValue * termWeightValue;
					
//					System.out.printf("term: %s, termCount: %d, documentCount: %d, tf: %f, idf: %f, termWeight: %f\n",
//							term, termWeight.getTermCount(), termWeight.getInvDocCount(), tf, idf, termWeightValue);
					corpusNorm += termWeightValueSquare;
					
					if (classTermSet.contains(term)) {
						classCorpusNorm += termWeightValueSquare;
					}

					if (methodTermSet.contains(term)) {
						methodCorpusNorm += termWeightValueSquare;
					}

					if (variableTermSet.contains(term)) {
						variableNorm += termWeightValueSquare;
					}

					if (commentTermSet.contains(term)) {
						commentNorm += termWeightValueSquare;
					}

					termWeight.setTf(tf);
					termWeight.setIdf(idf);
					sourceFileDAO.updateTermWeight(termWeight);
				}
				corpusNorm = Math.sqrt(corpusNorm);
				classCorpusNorm = Math.sqrt(classCorpusNorm);
				methodCorpusNorm = Math.sqrt(methodCorpusNorm);
				variableNorm = Math.sqrt(variableNorm);
				commentNorm = Math.sqrt(commentNorm);
//				System.out.printf(">>>> corpusNorm: %f, classCorpusNorm: %f, methodCorpusNorm: %f, variableNorm: %f, commentNorm: %f\n",
//						corpusNorm, classCorpusNorm, methodCorpusNorm, variableNorm, variableNorm);
				
				sourceFileDAO.updateNormValues(productName, fileName, version, corpusNorm, classCorpusNorm, methodCorpusNorm, variableNorm, commentNorm);
		}
	}
	
	private float getTfValue(int freq, int totalTermCount) {
		return (float) Math.log(freq) + 1.0F;
	}

	private float getIdfValue(double docCount, double totalCount) {
		return (float) Math.log(totalCount / docCount);
	}

}
