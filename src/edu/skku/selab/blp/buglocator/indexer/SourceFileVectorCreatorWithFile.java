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

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.indexer.IVectorCreator;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileVectorCreatorWithFile implements IVectorCreator {
    private String workDir;
    private String lineSparator;
    public int fileCount;
    public int codeTermCount;
    
    public SourceFileVectorCreatorWithFile() {
        workDir = (new StringBuilder(String.valueOf(Property.getInstance().getWorkDir()))).append(Property.getInstance().getSeparator()).toString();
        lineSparator = Property.getInstance().getLineSeparator();
        fileCount = Property.getInstance().getFileCount();
        codeTermCount = Property.getInstance().getWordCount();
    }
    
	/* (non-Javadoc)
	 * @see edu.skku.selab.blia.indexer.IVectorCreator#create()
	 */
	@Override
	public void create() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader((new StringBuilder(String.valueOf(workDir))).append("TermInfo.txt").toString()));
		String line = null;
		FileWriter writer = new FileWriter((new StringBuilder(String.valueOf(workDir))).append("CodeVector.txt").toString());
		while ((line = reader.readLine()) != null) {
			String values[] = line.split(";");
			String name = values[0].substring(0, values[0].indexOf("\t"));
			
//			if (name.equalsIgnoreCase("org.eclipse.swt.internal.win32.NMCUSTOMDRAW.java")) {
				if (values.length == 1) {
					System.out.println((new StringBuilder(String.valueOf(name))).append(";").toString());
				} else {
					Integer totalTermCount = Integer.valueOf(Integer.parseInt(values[0].substring(values[0].indexOf("\t") + 1)));
//					System.out.printf("totalTermCount: %d\n", totalTermCount);
					String termInfos[] = values[1].split("\t");
					float vector[] = new float[codeTermCount];
					String as[];
					int k = (as = termInfos).length;
					for (int j = 0; j < k; j++) {
						String str = as[j];
						String strs[] = str.split(":");
						Integer termId = Integer.valueOf(Integer.parseInt(strs[0]));
						Integer termCount = Integer.valueOf(Integer.parseInt(strs[1].substring(0, strs[1].indexOf(" "))));
						Integer documentCount = Integer.valueOf(Integer.parseInt(strs[1].substring(strs[1].indexOf(" ") + 1)));
						float tf = getTfValue(termCount.intValue(), totalTermCount.intValue());
						float idf = getIdfValue(documentCount.intValue(), fileCount);
						vector[termId.intValue()] = tf * idf;
//						System.out.printf("termId: %d, termCount: %d, documentCount: %d, tf: %f, idf: %f, vector: %f\n",
//								termId, termCount, documentCount, tf, idf, vector[termId.intValue()]);
					}

					double norm = 0.0D;
					for (int i = 0; i < vector.length; i++) {
						norm += vector[i] * vector[i];
					}

//					System.out.printf(">>>> norm: %f\n", norm);
					norm = Math.sqrt(norm);
					StringBuffer buf = new StringBuffer();
					buf.append((new StringBuilder(String.valueOf(name))).append(";").toString());
					for (int i = 0; i < vector.length; i++) {
						if (vector[i] != 0.0F) {
							vector[i] = vector[i] / (float) norm;
							buf.append((new StringBuilder(String.valueOf(i))).append(":").append(vector[i]).append(" ").toString());
						}
					}

					writer.write((new StringBuilder(String.valueOf(buf.toString()))).append(lineSparator).toString());
					writer.flush();
				}
//			}

		}
		writer.close();
	}

	private float getTfValue(int freq, int totalTermCount) {
		return (float) Math.log(freq) + 1.0F;
	}

	private float getIdfValue(double docCount, double totalCount) {
		return (float) Math.log(totalCount / docCount);
	}

}
