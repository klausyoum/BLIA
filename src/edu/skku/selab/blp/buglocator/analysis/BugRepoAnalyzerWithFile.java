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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import edu.skku.selab.blp.Property;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugRepoAnalyzerWithFile {
    private int wordCount;
    private int bugReportCount;
    private String workDir;
    private int fileCount;
    
    public BugRepoAnalyzerWithFile() {
    	Property property = Property.getInstance();
    	
        fileCount = property.getFileCount();
        workDir = (new StringBuilder(String.valueOf(property.getWorkDir()))).append(property.getSeparator()).toString();
    	
        wordCount = property.getBugTermCount();
        bugReportCount = property.getBugReportCount();
        workDir = (new StringBuilder(String.valueOf(property.getWorkDir()))).append(property.getSeparator()).toString();
    }

	/**
	 * Analyze similarity between a bug report and its previous bug reports. Then write similarity scores to SimiScore.txt
	 * ex.) Bug ID; Target bug ID#1:Similarity score	Target big ID#2:Similarity score 
	 * 
	 * (non-Javadoc)
	 * @see edu.skku.selab.blp.analysis.IAnalyzer#analyze()
	 */
	public void analyze() throws Exception {
		computeSimilarity();
		
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("BugSimilarity.txt").toString()));
		String line = null;
		Hashtable fixedTable = getFixedTable();
		Hashtable idTable = getFileIdTable();
		FileWriter writer = new FileWriter((new StringBuilder(String.valueOf(workDir))).append("SimiScore.txt").toString());
		while ((line = reader.readLine()) != null) {
			float similarValues[] = new float[fileCount];
			String idStr = line.substring(0, line.indexOf(";"));
			String vectorStr = line.substring(line.indexOf(";") + 1).trim();
			Integer id = Integer.valueOf(Integer.parseInt(idStr));
			String values[] = vectorStr.split(" ");
			String as[];
			int k = (as = values).length;
			for (int j = 0; j < k; j++) {
				String value = as[j];
				String singleValues[] = value.split(":");
				if (singleValues.length == 2) {
					Integer simBugId = Integer.valueOf(Integer.parseInt(singleValues[0]));
					float sim = Float.parseFloat(singleValues[1]);
					TreeSet fileSet = (TreeSet) fixedTable.get(simBugId);
					if (fileSet == null) {
						System.out.println(simBugId);
					}
					Iterator fileSetIt = fileSet.iterator();
					int size = fileSet.size();
					float singleValue = sim / (float) size;
					while (fileSetIt.hasNext()) {
						String name = (String) fileSetIt.next();
						Integer fileId = (Integer) idTable.get(name);
						if (null == fileId) {
							System.err.println(name);
						}
						similarValues[fileId.intValue()] += singleValue;
					}
				}
			}

			String output = (new StringBuilder()).append(id).append(";").toString();
			for (int i = 0; i < fileCount; i++) {
				if (similarValues[i] != 0.0F) {
					output = (new StringBuilder(String.valueOf(output))).append(i).append(":").append(similarValues[i]).append(" ").toString();
				}
			}

			writer.write((new StringBuilder(String.valueOf(output.trim())))
					.append(Property.getInstance().getLineSeparator())
					.toString());
			writer.flush();
		}
		writer.close();
	}
	
	public void computeSimilarity() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("SortedId.txt").toString()));
        String line = null;
        int idArr[] = new int[bugReportCount];
        int index = 0;
        while((line = reader.readLine()) != null) 
        {
            String idStr = line.substring(0, line.indexOf("\t"));
            idArr[index++] = Integer.parseInt(idStr);
        }
        reader.close();
        
        Hashtable vectors = getVector();
        FileWriter writer = new FileWriter((new StringBuilder(String.valueOf(workDir))).append("BugSimilarity.txt").toString());
        for(int i = 0; i < bugReportCount; i++)
        {
            int firstId = idArr[i];
            float firstVector[] = (float[])vectors.get(Integer.valueOf(firstId));
            String output = (new StringBuilder(String.valueOf(firstId))).append(";").toString();
            for(int j = 0; j < i; j++)
            {
                int secondId = idArr[j];
                float secondVector[] = (float[])vectors.get(Integer.valueOf(secondId));
                float similarity = getCosineValue(firstVector, secondVector);
                output = (new StringBuilder(String.valueOf(output))).append(secondId).append(":").append(similarity).append(" ").toString();
            }

            writer.write((new StringBuilder(String.valueOf(output.trim()))).append(Property.getInstance().getLineSeparator()).toString());
            writer.flush();
        }

        writer.close();		
	}
	
	/**
	 * Get cosine value from two vectors
	 * 
	 * @param firstVector
	 * @param secondVector
	 * @return
	 */
	private float getCosineValue(float firstVector[], float secondVector[]) {
		float len1 = 0.0F;
		float len2 = 0.0F;
		float product = 0.0F;
		for (int i = 0; i < wordCount; i++) {
			len1 += firstVector[i] * firstVector[i];
			len2 += secondVector[i] * secondVector[i];
			product += firstVector[i] * secondVector[i];
		}

		return (float) ((double) product / (Math.sqrt(len1) * Math.sqrt(len2)));
	}
	
	/**
	 * Get bug vector value 
	 * 
	 * @return <bug ID, vector> 
	 * @throws IOException
	 */
	public Hashtable getVector() throws IOException {
		Hashtable vectors = new Hashtable();
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("BugVector.txt").toString()));
		for (String line = null; (line = reader.readLine()) != null;) {
			String idStr = line.substring(0, line.indexOf("."));
			String vectorStr = line.substring(line.indexOf(";") + 1).trim();
			Integer id = Integer.valueOf(Integer.parseInt(idStr));
			float vector[] = getVector(vectorStr);
			vectors.put(id, vector);
		}

		return vectors;
	}
	



	/**
	 * [Duplicated] Get vector value from vector string
	 * ex.) 129:0.04518431 569:0.040287323 677:0.10419967
	 * 
	 * @param vectorStr
	 * @return
	 */
	private float[] getVector(String vectorStr) {
		float vector[] = new float[wordCount];
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
	 * Get fixed source file table of each bug IDs  
	 * 
	 * @return <bugID, <fixed files>> 
	 * @throws IOException
	 */
	private Hashtable getFixedTable() throws IOException {
		Hashtable idTable = new Hashtable();
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("FixLink.txt").toString()));
		for (String line = null; (line = reader.readLine()) != null;) {
			String values[] = line.split("\t");
			Integer id = Integer.valueOf(Integer.parseInt(values[0]));
			String name = values[1].trim();
			if (!idTable.containsKey(id)) {
				idTable.put(id, new TreeSet());
			}
			
			((TreeSet) idTable.get(id)).add(name);
		}

		return idTable;
	}

	/**
	 * [Duplicated] Get file ID table from ClassName.txt
	 * 
	 * @return <fileName, ID>
	 * @throws IOException
	 */
	private Hashtable getFileIdTable() throws IOException {
		Hashtable idTable = new Hashtable();
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("ClassName.txt").toString()));
		for (String line = null; (line = reader.readLine()) != null;) {
			String values[] = line.split("\t");
			Integer id = Integer.valueOf(Integer.parseInt(values[0]));
			String name = values[1].trim();
			idTable.put(name, id);
		}

		return idTable;
	}
}
