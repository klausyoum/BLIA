/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.indexer;

import java.util.HashMap;
import java.util.Iterator;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.db.AnalysisValue;
import edu.skku.selab.blp.db.dao.SourceFileDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileVectorCreator {
    
    public SourceFileVectorCreator() {
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

		while (fileNameIter.hasNext()) {
			String fileName = fileNameIter.next();
			
//			if (fileName.equalsIgnoreCase("org.eclipse.swt.internal.win32.NMCUSTOMDRAW.java")) {
				Integer totalTermCount = totalCorpusLengths.get(fileName);
				
				HashMap<String, AnalysisValue> analysisVaules = sourceFileDAO.getSourceFileAnalysisValues(productName, fileName, version);
				
				if (analysisVaules == null) {
					System.out.printf("Wrong file name: %s\n", fileName);
					continue;
				}

				double norm = 0.0D;
				Iterator<String> analysisVaulesIter = analysisVaules.keySet().iterator();
				while (analysisVaulesIter.hasNext()) {
					String corpus = analysisVaulesIter.next();
					AnalysisValue analysisValue = analysisVaules.get(corpus);
					double tf = getTfValue(analysisValue.getTermCount(),totalTermCount.intValue());
					double idf = getIdfValue(analysisValue.getInvDocCount(), fileCount);
					analysisValue.setVector(tf* idf);
					
//					System.out.printf("corpus: %s, termCount: %d, documentCount: %d, tf: %f, idf: %f, vector: %f\n",
//							corpus, analysisValue.getTermCount(), analysisValue.getInvDocCount(), tf, idf, analysisValue.getVector());
					norm += (analysisValue.getVector() * analysisValue.getVector());
				}
				
//				System.out.printf(">>>> norm: %f\n", norm);

				norm = Math.sqrt(norm);
				analysisVaulesIter = analysisVaules.keySet().iterator();
				while (analysisVaulesIter.hasNext()) {
					String corpus = analysisVaulesIter.next();
					AnalysisValue analysisValue = analysisVaules.get(corpus);
					analysisValue.setVector(analysisValue.getVector() / norm);
					
					sourceFileDAO.updateSourceFileAnalysisValue(analysisValue);
				}
//			}
		}
	}
	
	private float getTfValue(int freq, int totalTermCount) {
		return (float) Math.log(freq) + 1.0F;
	}

	private float getIdfValue(double docCount, double totalCount) {
		return (float) Math.log(totalCount / docCount);
	}

}
