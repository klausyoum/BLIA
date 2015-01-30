/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.buglocator.indexer;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import edu.skku.selab.blp.Property;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileIndexerWithFile {
    private String workDir;
    private String lineSparator;
    private int fileCount;
    
    public SourceFileIndexerWithFile() {
    	Property property = Property.getInstance();
        workDir = (new StringBuilder(String.valueOf(property.getWorkDir()))).append(property.getSeparator()).toString();
        lineSparator = property.getLineSeparator();
        fileCount = Property.getInstance().getFileCount();
    }

	/* (non-Javadoc)
	 * @see edu.skku.selab.blia.indexer.IIndexer#createIndex()
	 */
	public void createIndex() throws IOException {
		Hashtable countTable = countDoc();
		Hashtable idSet = new Hashtable();
		
		int id = 0;
		FileWriter wordWriter = new FileWriter((new StringBuilder(String.valueOf(workDir))).append("Wordlist.txt").toString());
		int wordCount = 0;
		for (Iterator iterator = countTable.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			idSet.put(key, Integer.valueOf(id));
			
			// write "word"	and #orderedNumber from countTable
			wordWriter.write((new StringBuilder(String.valueOf(key))).append("\t").append(id).append(lineSparator).toString());
			wordWriter.flush();
			id++;
			wordCount++;
		}
		wordWriter.close();
		
		// set total word count
		Property property = Property.getInstance();
		property.setWordCount(wordCount);		
		
		FileWriter docWriter = new FileWriter((new StringBuilder(String.valueOf(workDir))).append("IDC.txt").toString());
		for (Iterator iterator1 = countTable.keySet().iterator(); iterator1.hasNext(); docWriter.flush()) {
			String key = (String) iterator1.next();
			
			// write "word"	and #countNumber from countTable
			docWriter.write((new StringBuilder(String.valueOf(key))).append("\t").append(countTable.get(key)).append(lineSparator).toString());
		}
		docWriter.close();
		
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("CodeCorpus.txt").toString()));
		String line = null;
		FileWriter writer = new FileWriter((new StringBuilder(String.valueOf(workDir))).append("TermInfo.txt").toString());
		while ((line = reader.readLine()) != null) {
			String values[] = line.split("\t");
			String words[] = values[1].split(" ");
			int totalCount = 0;
			Hashtable termTable = new Hashtable();
			String as[];
			int j = (as = words).length;
			for (int i = 0; i < j; i++) {
				String word = as[i];
				if (!word.trim().equals("")) {
					totalCount++;
					Integer termId = (Integer) idSet.get(word);
					if (termTable.containsKey(termId)) {
						Integer count = (Integer) termTable.get(termId);
						count = Integer.valueOf(count.intValue() + 1);
						termTable.remove(termId);
						termTable.put(termId, count);
					} else {
						termTable.put(termId, Integer.valueOf(1));
					}
				}
			}

			StringBuffer output = new StringBuffer();
			
			// source file name		totalCount;termId#1:termCount#1 documentCount#1		termId#2:termCount#2 documentCount#2
			output.append((new StringBuilder(String.valueOf(values[0]))).append("\t").append(totalCount).append(";").toString());
			TreeSet tmp = new TreeSet();
			String as1[];
			int l = (as1 = words).length;
			for (int k = 0; k < l; k++) {
				String word = as1[k];
				if (!word.trim().equals("")) {
					Integer termId = (Integer) idSet.get(word);
					if (!tmp.contains(termId)) {
						tmp.add(termId);
						int termCount = ((Integer) termTable.get(termId)).intValue();
						int documentCount = ((Integer) countTable.get(word)).intValue();
						output.append((new StringBuilder()).append(termId).append(":").append(termCount).append(" ").append(documentCount).append("\t").toString());
					}
				}
			}

			writer.write((new StringBuilder(String.valueOf(output.toString()))).append(lineSparator).toString());
			writer.flush();
		}
		writer.close();
	}

	/**
	 * Calculate document counts from source code corpus data
	 *  
	 * @return
	 * @throws IOException
	 */
	public Hashtable countDoc() throws IOException {
		
		// example of CodeCorpus.txt
		// org.eclipse.swt.accessibility.ACC.java	acc contain constant us defin access object  
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("CodeCorpus.txt").toString()));
		String line = null;
		
		Hashtable countTable = new Hashtable();
		while ((line = reader.readLine()) != null) {
			String values[] = line.split("\t");
			String words[] = values[1].split(" ");
			TreeSet wordSet = new TreeSet();
			String as[];
			int j = (as = words).length;
			for (int i = 0; i < j; i++) {
				String word = as[i];
				if (!word.trim().equals("") && !wordSet.contains(word))
					wordSet.add(word);
			}

			for (Iterator iterator = wordSet.iterator(); iterator.hasNext();) {
				String word = (String) iterator.next();
				if (countTable.containsKey(word)) {
					Integer count = (Integer) countTable.get(word);
					count = Integer.valueOf(count.intValue() + 1);
					countTable.remove(word);
					countTable.put(word, count);
				} else {
					countTable.put(word, Integer.valueOf(1));
				}
			}

		}
		return countTable;
	}
	
	/**
	 * Compute length score of each source file then write them to LengthScore.txt file  
	 * 
	 * @throws IOException
	 */
	public void computeLengthScore() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("TermInfo.txt").toString()));
		String line = null;
		int max = 0x80000000;
		int lens[] = new int[fileCount];
		int i = 0;
		Hashtable lensTable = new Hashtable();
		int count = 0;
		while ((line = reader.readLine()) != null) {
			String values[] = line.split(";");
			String name = values[0].substring(0, values[0].indexOf("\t"));
			Integer len = Integer.valueOf(Integer.parseInt(values[0]
					.substring(values[0].indexOf("\t") + 1)));
			lensTable.put(name, len);
			lens[i++] = len.intValue();
			if (len.intValue() != 0)
				count++;
			if (len.intValue() > max)
				max = len.intValue();
		}
		int sum = 0;
		for (int j = 0; j < lens.length; j++)
			sum += lens[j];

		double average = (double) sum / (double) count;
		double squareDevi = 0.0D;
		Hashtable statTable = new Hashtable();
		for (int j = 0; j < lens.length; j++)
			if (lens[j] != 0) {
				int index = lens[j] / 10;
				if (statTable.containsKey(Integer.valueOf(index))) {
					int l = ((Integer) statTable.get(Integer.valueOf(index))).intValue();
					l++;
					statTable.remove(Integer.valueOf(index));
					statTable.put(Integer.valueOf(index), Integer.valueOf(l));
				} else {
					statTable.put(Integer.valueOf(index), Integer.valueOf(1));
				}
			}

		for (int j = 0; j < lens.length; j++)
			if (lens[j] != 0)
				squareDevi += ((double) lens[j] - average) * ((double) lens[j] - average);

		double standardDevi = Math.sqrt(squareDevi / (double) count);
		double low = average - 3D * standardDevi;
		double high = average + 3D * standardDevi;
		int min = 0;
		if (low > 0.0D)
			min = (int) low;
		int n = 0;
		FileWriter writer = new FileWriter((new StringBuilder(String.valueOf(workDir))).append("LengthScore.txt").toString());
		int count1 = 0;
		for (Iterator iterator = lensTable.keySet().iterator(); iterator
				.hasNext(); writer.flush()) {
			String key = (String) iterator.next();
			int len = ((Integer) lensTable.get(key)).intValue();
			double score = 0.0D;
			double nor = getNormalizedValue(len, high, min);
			if (len != 0) {
				if ((double) len > low && (double) len < high) {
					score = getLengthScore(nor);
					n++;
				} else if ((double) len < low) {
					score = 0.5D;
				} else {
					score = 1.0D;
				}
			} else {
				score = 0.0D;
			}
			if (nor > 6D) {
				nor = 6D;
			}
			if (score < 0.5D) {
				score = 0.5D;
			}
			if (score < 0.90000000000000002D) {
				count1++;
			}
			writer.write((new StringBuilder(String.valueOf(key))).append("\t").append(score).append(Property.getInstance().getLineSeparator()).toString());
		}

		writer.close();
	}
	
	/**
	 * Get normalized value of x from Max. to min.
	 * 
	 * @param x
	 * @param max
	 * @param min
	 * @return
	 */
	private float getNormalizedValue(int x, double max, double min) {
		return (6F * (float) ((double) x - min)) / (float) (max - min);
	}

	/**
	 * Get length score of BugLocator
	 * 
	 * @param len
	 * @return
	 */
	public double getLengthScore(double len) {
		return Math.exp(len) / (1.0D + Math.exp(len));
	}
}
