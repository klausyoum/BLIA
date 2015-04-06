/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.test.utils;

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
	

	
	public static void setProperty() {
		setProperty(DEFAULT_ALPHA, DEFAULT_BETA, DEFAULT_PAST_DATE);
	}
	
	public static void setProperty(String productName, String algorithmName, double alpha, double beta, int pastDays, String repoDir) {
		String osName = System.getProperty("os.name");
		String bugFilePath = "";
		String bugFileName = getBugFileName(productName);
		String sourceCodeDirName = getSourceCodeDirName(productName);
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
		
		Property.createInstance(productName, bugFilePath, sourceCodePath, workDir, alpha, beta, pastDays, repoDir, outputFile);
	}
	
	public static void setProperty(String productName, String algorithmName, double alpha, double beta, int pastDays) {
		setProperty(productName, algorithmName, alpha, beta, pastDays, "");
	}
	
	public static void setProperty(double alpha, double beta, int pastDays) {
		setProperty(DEFAULT_PROJECT, DEFAULT_ALGORITHM, alpha, beta, pastDays);
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
		}
		return bugFileName;
	}
	
	private static String getSourceCodeDirName(String productName) {
		String sourceCodeDirName;
		
		switch (productName) {
		case Property.ASPECTJ:
			sourceCodeDirName = Property.ASPECTJ_SOURCE_DIR_NAME;
			break;
		case Property.ECLIPSE:
			sourceCodeDirName = Property.ECLIPSE_SOURCE_DIR_NAME;
			break;
		case Property.SWT:
			sourceCodeDirName = Property.SWT_SOURCE_DIR_NAME;
			break;
		case Property.ZXING:
			sourceCodeDirName = Property.ZXING_SOURCE_DIR_NAME;
			break;
		default:
			sourceCodeDirName = Property.SWT_SOURCE_DIR_NAME;
			break;
		}
		
		return sourceCodeDirName;
	}
}
