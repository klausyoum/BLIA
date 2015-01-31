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
import edu.skku.selab.blp.common.Bug;
import edu.skku.selab.blp.common.Rank;
import edu.skku.selab.blp.db.IntegratedAnalysisValue;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.IntegratedAnalysisDAO;
import edu.skku.selab.blp.db.dao.SourceFileDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugLocatorWithFile {
    private String workDir;
    private String outputFile;
    private int fileCount;
    private int bugCount;
    private float alpha;
    private String lineSparator;
    Hashtable idTable;
    Hashtable fixTable;
    Hashtable lenTable;
    
    public BugLocatorWithFile() throws Exception {
    	Property property = Property.getInstance();
        workDir = (new StringBuilder(String.valueOf(property.getWorkDir()))).append(property.getSeparator()).toString();
        outputFile = property.getOutputFile();
        fileCount = property.getFileCount();
        bugCount = property.getBugReportCount();
        alpha = property.getAlpha();
        lineSparator = property.getLineSeparator();
        idTable = getFileId();
        fixTable = getFixLinkTable();
        lenTable = getLengthScore();
    }
	
    /**
     * Localize bug location with bug report and other information 
     * 
     * @throws Exception
     */
	public void analyze() throws Exception {
		BufferedReader VSMReader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("VSMScore.txt").toString()));
		BufferedReader GraphReader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("SimiScore.txt").toString()));
		int count = 0;
		FileWriter writer = new FileWriter(outputFile);
		
		int top1 = 0;
		while (count < bugCount) {
			count++;
			String vsmLine = VSMReader.readLine();
			String vsmIdStr = vsmLine.substring(0, vsmLine.indexOf(";"));
			Integer vsmId = Integer.valueOf(Integer.parseInt(vsmIdStr));
			String vsmVectorStr = vsmLine.substring(vsmLine.indexOf(";") + 1);
			float vsmVector[] = getVector(vsmVectorStr);
			for (Iterator iterator = lenTable.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				Integer id = (Integer) idTable.get(key);
				Double score = (Double) lenTable.get(key);
				vsmVector[id.intValue()] = vsmVector[id.intValue()] * score.floatValue();
			}

			vsmVector = normalize(vsmVector);
			String graphLine = GraphReader.readLine();
			String graphIdStr = graphLine.substring(0, graphLine.indexOf(";"));
			Integer graphId = Integer.valueOf(Integer.parseInt(graphIdStr));
			String graphVectorStr = graphLine.substring(graphLine.indexOf(";") + 1);
			float graphVector[] = getVector(graphVectorStr);
			graphVector = normalize(graphVector);
			float finalR[] = combine(vsmVector, graphVector, alpha);
			Rank sort[] = sort(finalR);
			TreeSet fileSet = (TreeSet) fixTable.get(vsmId);
			Iterator fileIt = fileSet.iterator();
			Hashtable fileIdTable = new Hashtable();
			String fileName;
			Integer fileId;
			for (; fileIt.hasNext(); fileIdTable.put(fileId, fileName)) {
				fileName = (String) fileIt.next();
				fileId = (Integer) idTable.get(fileName);
			}

			for (int i = 0; i < sort.length; i++) {
				Rank rank = sort[i];
				if (!fileIdTable.isEmpty() && fileIdTable.containsKey(Integer.valueOf(rank.id))) {
					if (i == 0) {
						top1++;
					}
					writer.write((new StringBuilder()).append(vsmId).append(",")
							.append((String) fileIdTable.get(Integer.valueOf(rank.id))).append(",")
//							.append(i).append(",")
							.append(rank.rank).append(lineSparator).toString());
					writer.flush();
				}
			}
		}
		
//		System.out.printf("Top1: %d\n", top1);
		writer.close();
	}
	
	/**
	 * Get file ID from ClassName.txt
	 * 
	 * @return <sourceFileName, #>
	 * @throws IOException
	 */
	public Hashtable getFileId() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("ClassName.txt").toString()));
		String line = null;
		Hashtable table = new Hashtable();
		while ((line = reader.readLine()) != null) {
			String values[] = line.split("\t");
			Integer idInteger = Integer.valueOf(Integer.parseInt(values[0]));
			String nameString = values[1].trim();
			table.put(nameString, idInteger);
		}
		return table;
	}

	/**
	 * Get source file list from ClassName.txt
	 * 
	 * @return <#, sourceFileName>
	 * @throws IOException
	 */
	public Hashtable getFile() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("ClassName.txt").toString()));
		String line = null;
		Hashtable table = new Hashtable();
		while ((line = reader.readLine()) != null) {
			String values[] = line.split("\t");
			Integer idInteger = Integer.valueOf(Integer.parseInt(values[0]));
			String nameString = values[1].trim();
			table.put(idInteger, nameString);
		}
		return table;
	}

	/**
	 * Get fixed link table info from FixLink.txt
	 * 
	 * @return <Bug ID, <fixed source files>>
	 * @throws IOException
	 */
	public Hashtable getFixLinkTable() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("FixLink.txt").toString()));
		String line = null;
		Hashtable table = new Hashtable();
		while ((line = reader.readLine()) != null) {
			String valueStrings[] = line.split("\t");
			Integer id = Integer.valueOf(Integer.parseInt(valueStrings[0]));
			String fileName = valueStrings[1].trim();
			if (!table.containsKey(id)) {
				table.put(id, new TreeSet());
			}
			((TreeSet) table.get(id)).add(fileName);
		}
		return table;
	}

	/**
	 * Sort Rank array
	 * 
	 * @param finalR
	 * @return
	 */
	private Rank[] sort(float finalR[]) {
		Rank R[] = new Rank[finalR.length];
		for (int i = 0; i < R.length; i++) {
			Rank rank = new Rank();
			rank.rank = finalR[i];
			rank.id = i;
			R[i] = rank;
		}

		R = insertionSort(R);
		return R;
	}

	/**
	 * Do insertion sort of Rank array
	 * 
	 * @param R
	 * @return
	 */
	private Rank[] insertionSort(Rank R[]) {
		for (int i = 0; i < R.length; i++) {
			int maxIndex = i;
			for (int j = i; j < R.length; j++) {
				if (R[j].rank > R[maxIndex].rank) {
					maxIndex = j;
				}
			}

			Rank tmpRank = R[i];
			R[i] = R[maxIndex];
			R[maxIndex] = tmpRank;
		}

		return R;
	}

	/**
	 * Combine rVSMScore(vsmVector) and SimiScore(graphVector)
	 * 
	 * @param vsmVector
	 * @param graphVector
	 * @param f
	 * @return
	 */
	public float[] combine(float vsmVector[], float graphVector[], float f) {
		float results[] = new float[fileCount];
		for (int i = 0; i < fileCount; i++) {
			results[i] = vsmVector[i] * (1.0F - f) + graphVector[i] * f;
		}

		return results;
	}
	
	/**
	 * Normalize values in array from max. to min of array
	 * 
	 * @param array
	 * @return
	 */
	private float[] normalize(float array[]) {
		float max = 1.401298E-045F;
		float min = 3.402823E+038F;
		for (int i = 0; i < array.length; i++) {
			if (max < array[i]) {
				max = array[i];
			}
			if (min > array[i]) {
				min = array[i];
			}
		}

		float span = max - min;
		for (int i = 0; i < array.length; i++) {
			array[i] = (array[i] - min) / span;
		}

		return array;
	}
	
	/**
	 * [Duplicated] Get vector value from vector string
	 * ex.) 129:0.04518431 569:0.040287323 677:0.10419967
	 * 
	 * @param vectorStr
	 * @return
	 */
	private float[] getVector(String vectorStr) {
		float vector[] = new float[fileCount];
		String values[] = vectorStr.split(" ");
		String as[];
		int j = (as = values).length;
		for (int i = 0; i < j; i++) {
			String value = as[i];
			String singleValues[] = value.split(":");
			if (singleValues.length == 2) {
				int index = Integer.parseInt(singleValues[0]);
				float sim = Float.parseFloat(singleValues[1]);
				vector[index] = sim;
			}
		}

		return vector;
	}

	/**
	 * Get length score of each source files from LengthScore.txt
	 * 
	 * @return <fileName, lengthScore>
	 * @throws IOException
	 */
	private Hashtable getLengthScore() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("LengthScore.txt").toString()));
		String line = null;
		Hashtable lenTable = new Hashtable();
		while ((line = reader.readLine()) != null) {
			String values[] = line.split("\t");
			String name = values[0];
			Double score = Double.valueOf(Double.parseDouble(values[1]));
			lenTable.put(name, score);
		}
		reader.close();
		return lenTable;
	}
}
