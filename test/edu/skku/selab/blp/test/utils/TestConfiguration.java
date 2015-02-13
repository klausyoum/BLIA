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
	final static private float DEFAULT_ALPHA = 0.2f;
	final static private float DEFAULT_BETA = 0.5f;
	final static private int DEFAULT_PAST_DATE = 15;
	final static private String DEFAULT_PROJECT = Property.SWT;
	final static private String DEFAULT_ALGORITHM = "BLIA";
	
	public static void setProperty() {
		setProperty(DEFAULT_ALPHA, DEFAULT_BETA, DEFAULT_PAST_DATE);
	}
	
	public static void setProperty(String projectName, String algorithmName, float alpha, float beta, int pastDate) {
		String osName = System.getProperty("os.name");
		String productName = getProductName(projectName);
		String bugFilePath = "";
		String bugFileName = getBugFileName(projectName);
		String sourceCodeDirName = getSourceCodeDirName(projectName);
		String sourceCodePath = "";
		String workDir = "";
		String outputFileName = algorithmName + "-" + projectName + "-" + Float.toString(alpha) + ".txt"; 
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
		
		Property.createInstance(productName, bugFilePath, sourceCodePath, workDir, alpha, beta, pastDate, outputFile);
		Property.getInstance().setAlpha(alpha);
		Property.getInstance().setBeta(beta);
	}
	
	public static void setProperty(float alpha, float beta, int pastDate) {
		setProperty(DEFAULT_PROJECT, DEFAULT_ALGORITHM, alpha, beta, pastDate);
	}
	
	private static String getBugFileName(String projectName) {
		String bugFileName = ""; 
		if (projectName.equalsIgnoreCase("aspectj")) {
			bugFileName = "AspectJBugRepository.xml";
		} else if (projectName.equalsIgnoreCase("eclipse")) {
			bugFileName = "EclipseBugRepository.xml";
		} else if (projectName.equalsIgnoreCase("swt")) {
			bugFileName = "SWTBugRepository.xml";
		} else if (projectName.equalsIgnoreCase("zxing")) {
			bugFileName = "ZXingBugRepository.xml";
		}
		return bugFileName;
	}
	
	public static String getProductName(String projectName) {
		String productName = "";
		
		switch (projectName) {
		case Property.ASPECTJ:
			productName = Property.ASPECTJ_PRODUCT;
			break;
		case Property.ECLIPSE:
			productName = Property.ECLIPSE_PRODUCT;
			break;
		case Property.SWT:
			productName = Property.SWT_PRODUCT;
			break;
		case Property.ZXING:
			productName = Property.ZXING_PRODUCT;
			break;
		default:
			productName = Property.SWT_PRODUCT;
			break;
		}
		
		return productName;
	}
	
	private static String getSourceCodeDirName(String projectName) {
		String sourceCodeDirName = getProductName(projectName);
		if (projectName.equalsIgnoreCase("swt")) {
			String osName = System.getProperty("os.name");
			if (osName.equals("Mac OS X")) {
				sourceCodeDirName += "/src";
			} else {
				sourceCodeDirName += "\\src";
			}
		}
		
		return sourceCodeDirName;
	}
}
