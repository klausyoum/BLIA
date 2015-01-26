/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.anlaysis;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import edu.skku.selab.blia.Property;
import edu.skku.selab.blia.db.AnalysisValue;
import edu.skku.selab.blia.db.IntegratedAnalysisValue;
import edu.skku.selab.blia.db.dao.BugDAO;
import edu.skku.selab.blia.db.dao.IntegratedAnalysisDAO;
import edu.skku.selab.blia.db.dao.SourceFileDAO;
import edu.skku.selab.blia.indexer.Bug;
import edu.skku.selab.blia.indexer.SourceFileIndexer;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileAnalyzer implements IAnalyzer {
    Hashtable fileIdTable;
    private String workDir;
    private String lineSparator;
    public int fileCount;
    public int codeTermCount;
	
    public SourceFileAnalyzer() {
        fileIdTable = null;
        
        Property property = Property.getInstance();
        workDir = (new StringBuilder(String.valueOf(property.getWorkDir()))).append(property.getSeparator()).toString();
        lineSparator = property.getLineSeparator();
        fileCount = property.getFileCount();
        codeTermCount = property.getWordCount();
    }
    
	/**
	 * Calculate VSM score between source files and each bug report 
	 * 
	 * @see edu.skku.selab.blia.anlaysis.IAnalyzer#analyze()
	 */	
	public void analyze() throws IOException {
		fileIdTable = getSourceFileId();
		Hashtable wordIdTable = getWordId();
		Hashtable idcTable = getIDCTable();
		Property property = Property.getInstance();
		FileWriter writer = new FileWriter((new StringBuilder(String.valueOf(workDir))).append("VSMScore.txt").toString());
		BufferedReader readerId = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("SortedId.txt").toString()));
		
		for (String idLine = null; (idLine = readerId.readLine()) != null;) {
			Integer bugId = Integer.valueOf(Integer.parseInt(idLine.substring(0, idLine.indexOf("\t"))));
			
//			if (bugId == 75739) {
				BufferedReader readerBug = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("BugCorpus").append(property.getSeparator())
						.append(bugId).append(".txt").toString()));
				String line = readerBug.readLine();
				String words[] = line.split(" ");
				Hashtable wordTable = new Hashtable();
				String as[];
				double norm = (as = words).length;
				for (int j = 0; j < norm; j++) {
					String word = as[j];
					if (!word.trim().equals(""))
						if (wordTable.containsKey(word)) {
							Integer count = (Integer) wordTable.get(word);
							count = Integer.valueOf(count.intValue() + 1);
							wordTable.remove(word);
							wordTable.put(word, count);
						} else {
							wordTable.put(word, Integer.valueOf(1));
						}
				}

				int totalTermCount = 0;
				for (Iterator iterator = wordTable.keySet().iterator(); iterator.hasNext();) {
					String word = (String) iterator.next();
					Integer id = (Integer) wordIdTable.get(word);
					if (id != null) {
						totalTermCount += ((Integer) wordTable.get(word)).intValue();
					}
					
//					System.out.printf("Corpus: %s, termCount: %d\n", word, wordTable.get(word));
				}
				
//				System.out.printf("totalTermCount: %d\n", totalTermCount);

				float bugVector[] = new float[codeTermCount];
				for (Iterator iterator1 = wordTable.keySet().iterator(); iterator1.hasNext();) {
					String word = (String) iterator1.next();
					Integer id = (Integer) wordIdTable.get(word);
					if (id != null) {
						Integer idc = (Integer) idcTable.get(word);
						Integer count = (Integer) wordTable.get(word);
						float tf = getTfValue(count.intValue(), totalTermCount);
						float idf = getIdfValue(idc.intValue(), fileCount);
						bugVector[id.intValue()] = tf * idf;
						
//						System.out.printf("word: %s, count: %d, idc: %d, tf: %f, idf: %f, vector: %f\n",
//								word, count, idc, tf, idf, bugVector[id.intValue()]);
					}
				}

				double word = 0.0D;
				for (int i = 0; i < bugVector.length; i++) {
					word += (double) (bugVector[i] * bugVector[i]);
				}
				
//				System.out.printf("word: %f\n", word);

				word = Math.sqrt(word);
				for (int i = 0; i < bugVector.length; i++) {
					bugVector[i] = bugVector[i] / (float) word;
//					if (bugVector[i] != 0.0f) {
//						for (Iterator iterator1 = wordTable.keySet().iterator(); iterator1.hasNext();) {
//							String corpus = (String) iterator1.next();
//							Integer id = (Integer) wordIdTable.get(corpus);
//							
//							if ((id != null) && (i == id.intValue())) {
//								System.out.printf("corpus: %s, bugVector[%d]: %f\n", corpus, i, bugVector[i]);
//							}
//						}
//					}
				}

				float simValues[] = computeSimilarity(bugVector);
				StringBuffer buf = new StringBuffer();
				buf.append((new StringBuilder()).append(bugId).append(";").toString());
				for (int i = 0; i < simValues.length; i++) {
					if (simValues[i] != 0.0F) {
//						System.out.printf("simValues[%d]: %f\n", i, simValues[i]);
						buf.append((new StringBuilder(String.valueOf(i))).append(":").append(simValues[i]).append(" ").toString());
					}
				}

				writer.write((new StringBuilder(String.valueOf(buf.toString().trim()))).append(lineSparator).toString());
				writer.flush();
			}

//		}

		writer.close();
	}
	
	/**
	 * Calculate VSM score between source files and each bug report 
	 * 
	 * @see edu.skku.selab.blia.anlaysis.IAnalyzer#analyze()
	 */	
	public void analyzeWithDB(String version) throws Exception {
		BugDAO bugDAO = new BugDAO();
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		HashMap<String, Bug> bugs = bugDAO.getBugs();
		Property property = Property.getInstance();
		String productName = property.getProductName();
		
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		HashMap<String, Integer> corpusTable = sourceFileDAO.getCorpuses(productName);
		
		SourceFileIndexer sourceFileIndexer = new SourceFileIndexer(); 
		Hashtable<String, Integer> inverseDocCountTable = sourceFileIndexer.getInverseDocCountTable(version);
		
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
	 * Compute similarity between code vector and bug vector 
	 * 
	 * @param bugVector
	 * @return
	 * @throws IOException
	 */
	private float[] computeSimilarity(float bugVector[]) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("CodeVector.txt").toString()));
		String line = null;
		float simValues[] = new float[fileCount];
		while ((line = reader.readLine()) != null) {
			String values[] = line.split(";");
			String name = values[0];
			Integer fileId = (Integer) fileIdTable.get(name);
			if (null == fileId) {
				System.out.println(name);
			}
			float codeVector[] = (float[]) null;
			if (values.length != 1)
				codeVector = getVector(values[1]);
			else
				codeVector = getVector(null);
			float sim = 0.0F;
			for (int i = 0; i < codeVector.length; i++) {
				sim += bugVector[i] * codeVector[i];
			}

			simValues[fileId.intValue()] = sim;
		}
		return simValues;
	}

	/**
	 * Get vector value from vector string
	 * ex.) 129:0.04518431 569:0.040287323 677:0.10419967
	 * 
	 * @param vecStr
	 * @return
	 */
	private float[] getVector(String vecStr) {
		float vector[] = new float[codeTermCount];
		if (null == vecStr) {
			return vector;
		}
		
		String values[] = vecStr.split(" ");
		String as[];
		int j = (as = values).length;
		for (int i = 0; i < j; i++) {
			String str = as[i];
			Integer id = Integer.valueOf(Integer.parseInt(str.substring(0,str.indexOf(":"))));
			float w = Float.parseFloat(str.substring(str.indexOf(":") + 1));
			vector[id.intValue()] = w;
		}

		return vector;
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

	/**
	 * Get corpus table from Wordlist.txt
	 * 
	 * @return <Corpus, ID>
	 * @throws IOException
	 */
	private Hashtable getWordId() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("Wordlist.txt").toString()));
		String line = null;
		Hashtable wordIdTable = new Hashtable();
		while ((line = reader.readLine()) != null) {
			String values[] = line.split("\t");
			wordIdTable.put(values[0],Integer.valueOf(Integer.parseInt(values[1])));
		}
		reader.close();
		return wordIdTable;
	}

	/**
	 * Get inverse document count(?) table from IDC.txt
	 * 
	 * @return <Corpus, IDC>
	 * @throws IOException
	 */
	private Hashtable getIDCTable() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("IDC.txt").toString()));
		String line = null;
		Hashtable idcTable = new Hashtable();
		while ((line = reader.readLine()) != null) {
			String values[] = line.split("\t");
			idcTable.put(values[0],Integer.valueOf(Integer.parseInt(values[1])));
		}
		reader.close();
		return idcTable;
	}

	/**
	 * Get source file and ID from ClassName.txt
	 * 
	 * @return <fileName, fileId>
	 * @throws IOException
	 */
	private Hashtable getSourceFileId() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("ClassName.txt").toString()));
		String line = null;
		Hashtable table = new Hashtable();
		while ((line = reader.readLine()) != null) {
			String values[] = line.split("\t");
			Integer fileId = Integer.valueOf(Integer.parseInt(values[0]));
			String fileName = values[1].trim();
			table.put(fileName, fileId);
		}
		return table;
	}
}