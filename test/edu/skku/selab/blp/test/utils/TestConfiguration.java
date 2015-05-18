/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.test.utils;

import java.io.File;

import edu.skku.selab.blp.Property;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class TestConfiguration {
	final static public String BLIA_ALGORITHM = "BLIA";
	
	final static private float DEFAULT_ALPHA = 0.2f;
	final static private float DEFAULT_BETA = 0.3f;
	final static private int DEFAULT_PAST_DATE = 15;
	final static private String DEFAULT_PROJECT = Property.SWT;
	final static private String DEFAULT_ALGORITHM = BLIA_ALGORITHM;
	
	public static String getElapsedTimeSting(long startTime) {
		long elapsedTime = System.currentTimeMillis() - startTime;
		String elpsedTimeString = (elapsedTime / 1000) + "." + (elapsedTime % 1000);
		return elpsedTimeString;
	}
	
	public static void setProperty() {
		int candidateLimitSize = Integer.MAX_VALUE;
		setProperty(DEFAULT_ALPHA, DEFAULT_BETA, DEFAULT_PAST_DATE, candidateLimitSize);
	}

	public static void setProperty(String productName, String algorithmName, double alpha, double beta, int pastDays,
			String repoDir) {
		double candidateLimitRate = 1.0;
		int candidateLimitSize = Integer.MAX_VALUE;
		setProperty(productName, algorithmName, alpha, beta, pastDays,
				repoDir, candidateLimitRate, candidateLimitSize);
	}
	
	public static void setProperty(String productName, String algorithmName, double alpha, double beta, int pastDays,
			String repoDir, double candidateLimitRate, int candidateLimitSize) {
		String osName = System.getProperty("os.name");
		String bugFilePath = "";
		String bugFileName = getBugFileName(productName);
		String sourceCodeDirName = Property.getSourceCodeDirName(productName);
		String sourceCodePath = "";
		String workDir = "";
		String outputFileName = algorithmName + "-" + productName + "-" + Double.toString(alpha) + ".txt"; 
		String outputFile = "";

		if (osName.equals("Mac OS X")) {
			bugFilePath = "../Dataset/" + bugFileName;
			sourceCodePath = "../Dataset/" + sourceCodeDirName;
			workDir = "./tmp";
			outputFile = "../Results/" + outputFileName;
		} else {
			bugFilePath = "..\\Dataset\\" + bugFileName;
			sourceCodePath = "..\\Dataset\\" + sourceCodeDirName;
			workDir = ".\\tmp";
			outputFile = "..\\Results\\" + outputFileName;
		}
		
		File dir = new File(workDir);
		if (dir.exists()) {
			deleteDirectory(dir);
		}
		
		if (false == dir.mkdir()) {
			System.err.println(workDir + " can't be created!");
			
			if (false == dir.mkdir()) {
				System.err.println(workDir + " can't be created again");
			}
		}
		
		Property.createInstance(productName, bugFilePath, sourceCodePath, workDir, alpha, beta, pastDays,
				repoDir, outputFile, candidateLimitRate, candidateLimitSize);
	}
	
    private static boolean deleteDirectory(File path) {
        if(!path.exists()) {
            return false;
        }
         
        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
         
        return path.delete();
    }
    
	public static void setProperty(String productName, String algorithmName,
			double alpha, double beta, int pastDays) {
		double candidateLimitRate = 1.0;
		int candidateLimitSize = Integer.MAX_VALUE;
		setProperty(productName, algorithmName, alpha, beta, pastDays, "", candidateLimitRate, candidateLimitSize);
	}
	
	public static void setProperty(String productName, String algorithmName,
			double alpha, double beta, int pastDays, int candidateLimitSize) {
		double candidateLimitRate = 1.0;
		setProperty(productName, algorithmName, alpha, beta, pastDays, "", candidateLimitRate, candidateLimitSize);
	}

	public static void setProperty(double alpha, double beta, int pastDays) {
		int candidateLimitSize = Integer.MAX_VALUE;
		setProperty(DEFAULT_PROJECT, DEFAULT_ALGORITHM, alpha, beta, pastDays, candidateLimitSize);
	}

	public static void setProperty(double alpha, double beta, int pastDays, int candidateLimitSize) {
		setProperty(DEFAULT_PROJECT, DEFAULT_ALGORITHM, alpha, beta, pastDays, candidateLimitSize);
	}
	
	private static String getBugFileName(String productName) {
		String bugFileName = ""; 
		if (productName.equalsIgnoreCase(Property.ASPECTJ)) {
			bugFileName = "AspectJBugRepository.xml";
		} else if (productName.equalsIgnoreCase(Property.ECLIPSE)) {
			bugFileName = "EclipseBugRepository.xml";
		} else if (productName.equalsIgnoreCase(Property.SWT)) {
			bugFileName = "SWTBugRepository.xml";
		} else if (productName.equalsIgnoreCase(Property.ZXING)) {
			bugFileName = "ZXingBugRepository.xml";
		} else if (productName.equalsIgnoreCase(Property.JODA_TIME)){
			bugFileName = "JodatimeBugRepository.xml";
		}
		return bugFileName;
	}
}
