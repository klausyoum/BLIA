/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */

package edu.skku.selab.blp;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;


public class Property {
	final static public String ASPECTJ = "aspectj";
	final static public String ECLIPSE = "eclipse";
	final static public String SWT = "swt";
	final static public String ZXING = "zxing";
	
	final static public int THREAD_COUNT = Integer.parseInt(Property.readProperty("THREAD_COUNT"));
	final static private String WORK_DIR = Property.readProperty("WORK_DIR");
	final static private String OUTPUT_FILE = Property.readProperty("OUTPUT_FILE");
	
	final static public String RUN_LEVEL_FILE = "FILE";
	final static public String RUN_LEVEL_METHOD = "METHOD";
	
	private String targetProduct;
	private String bugFilePath;
	private String sourceCodeDir;
	private String[] sourceCodeDirList;

	private int fileCount;
	private int wordCount;
	private int bugReportCount;
	private int bugTermCount;
	private double alpha;
	private double beta;
	private String separator = System.getProperty("file.separator");
	private String lineSeparator = System.getProperty("line.separator");
	private static Property p = null;
	private String productName;
	private int pastDays;
	private Calendar since = null;
	private Calendar until = null;
	private String repoDir;
	private double candidateLimitRate = 1.0;
	
	private String runLevel;

	public int getBugTermCount() {
		return bugTermCount;
	}

	public void setBugTermCount(int bugTermCount) {
		this.bugTermCount = bugTermCount;
	}

	public int getBugReportCount() {
		return bugReportCount;
	}

	public void setBugReportCount(int bugReportCount) {
		this.bugReportCount = bugReportCount;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public int getWordCount() {
		return wordCount;
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}

	public String getWorkDir() {
		return WORK_DIR;
	}
	
	private static String readProperty(String key) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("blp.properties"));
		} catch (IOException e) {
		}

		return properties.getProperty(key);
	}
	
	public static void createInstance(String productName, String bugFilePath, String sourceCodeDir, String workDir,
			double alpha, double beta, int pastDays, String repoDir, String outputFile, double candidateLimitRate) {
		if (null == p) {
			p = new Property(productName, bugFilePath, sourceCodeDir, workDir,
					alpha, beta, pastDays, repoDir, outputFile, candidateLimitRate);
		} else {
			p.setValues(productName, bugFilePath, sourceCodeDir, workDir, alpha,
					beta, pastDays, repoDir, outputFile, candidateLimitRate);
		}
	}
	
	private Property() {
		// Do nothing
	}
	
	public static Property loadInstance(String targetProduct) throws Exception {
		if (null == p) {
			p = new Property();
		}
		
		targetProduct = targetProduct.toUpperCase();
		
		String productName = Property.readProperty(targetProduct + "_" + "PRODUCT");
		String sourceCodeDir = Property.readProperty(targetProduct + "_" + "SOURCE_DIR");
		double alpha = Double.parseDouble(Property.readProperty(targetProduct + "_" + "ALPHA"));
		double beta = Double.parseDouble(Property.readProperty(targetProduct + "_" + "BETA"));
		int pastDays = Integer.parseInt(Property.readProperty(targetProduct + "_" + "PAST_DAYS"));
		String repoDir = Property.readProperty(targetProduct + "_" + "REPO_DIR");
		String bugFilePath = Property.readProperty(targetProduct + "_" + "BUG_REPO_FILE");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date sinceDate = dateFormat.parse(Property.readProperty(targetProduct + "_" + "COMMIT_SINCE"));
		Calendar since = new GregorianCalendar();
		since.setTime(sinceDate);
		Date untilDate = dateFormat.parse(Property.readProperty(targetProduct + "_" + "COMMIT_UNTIL"));
		Calendar until = new GregorianCalendar();
		until.setTime(untilDate);
		double candidateLimitRate = Double.parseDouble(Property.readProperty(targetProduct + "_" + "CANDIDATE_LIMIT_RATE"));

		p.setValues(productName, sourceCodeDir, alpha, beta, pastDays, repoDir,
				bugFilePath, since, until, candidateLimitRate);
		p.setRunLevel(Property.readProperty("RUN_LEVEL"));
		
		return p;
	}
	
	public static Property loadInstance() throws Exception {
		String targetProduct = Property.readProperty("TARGET_PRODUCT");
		return loadInstance(targetProduct);
	}
	
	public static Property getInstance() {
		return p;
	}
	
	private Property(String productName, String bugFilePath, String sourceCodeDir, String workDir,
			double alpha, double beta, int pastDays, String repoDir, String outputFile, double candidateLimitRate) {
		setValues(productName, bugFilePath, sourceCodeDir, workDir, alpha,
				beta, pastDays, repoDir, outputFile, candidateLimitRate);
	}

	private void setValues(String productName, String bugFilePath,
			String sourceCodeDir, String workDir, double alpha, double beta,
			int pastDays, String repoDir, String outputFile, double candidateLimitRate) {
		setCandidateLimitRate(candidateLimitRate);
		setValues(productName, bugFilePath, sourceCodeDir, workDir, alpha,
				beta, pastDays, repoDir, outputFile);
	}
	
	private void setValues(String productName, String sourceCodeDir,
			double alpha, double beta, int pastDays, String repoDir,
			String bugFilePath, Calendar since, Calendar until, double candidateLimitRate) {
		
		setProductName(productName);
		setSourceCodeDir(sourceCodeDir)
;		sourceCodeDirList = new String[1];
		sourceCodeDirList[0] = sourceCodeDir;
		setAlpha(alpha);
		setBeta(beta);
		setPastDays(pastDays);
		setRepoDir(repoDir);
		setBugFilePath(bugFilePath);
		setSince(since);
		setUntil(until);
		setCandidateLimitRate(candidateLimitRate);
	}
	
	public void printValues() {
		System.out.printf("WORK_DIR: %s\n", Property.WORK_DIR);
		System.out.printf("THREAD_COUNT: %d\n", Property.THREAD_COUNT);
		System.out.printf("OUTPUT_FILE: %s\n\n", Property.OUTPUT_FILE);
		
		System.out.printf("Product name: %s\n", getProductName());
		System.out.printf("Source code dir: %s\n", getSourceCodeDir());
		System.out.printf("Alpha: %f\n", getAlpha());
		System.out.printf("Beta: %f\n", getBeta());
		System.out.printf("Past days: %s\n", getPastDays());
		System.out.printf("Repo dir: %s\n", getRepoDir());
		System.out.printf("Bug file path: %s\n", getBugFilePath());
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		System.out.printf("Since: %s\n", dateFormat.format(getSince().getTime()));
		System.out.printf("Until: %s\n", dateFormat.format(getUntil().getTime()));
		System.out.printf("candidateLimitRate: %f\n", getCandidateLimitRate());
	}
	
	private void setValues(String productName, String bugFilePath,
			String sourceCodeDir, String workDir, double alpha, double beta,
			int pastDays, String repoDir, String outputFile) {
		setProductName(productName);
		setBugFilePath(bugFilePath);
		sourceCodeDirList = new String[1];
		sourceCodeDirList[0] = sourceCodeDir;
		setAlpha(alpha);
		setBeta(beta);
		setPastDays(pastDays);
		setRepoDir(repoDir);
	}
	
	public double getAlpha() {
		return alpha;
	}

	public double getBeta() {
		return beta;
	}

	public String getOutputFile() {
		return OUTPUT_FILE;
	}

	public String getBugFilePath() {
		return bugFilePath;
	}

	public String[] getSourceCodeDirList() {
		return sourceCodeDirList;
	}

	public String getSeparator() {
		return separator;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	/**
	 * @param beta the beta to set
	 */
	public void setBeta(double beta) {
		this.beta = beta;
	}

	/**
	 * @return the pastDays
	 */
	public int getPastDays() {
		return pastDays;
	}

	/**
	 * @param pastDate the pastDays to set
	 */
	public void setPastDays(int pastDays) {
		this.pastDays = pastDays;
	}

	/**
	 * @return the repoDir
	 */
	public String getRepoDir() {
		return repoDir;
	}

	/**
	 * @param repoDir the repoDir to set
	 */
	public void setRepoDir(String repoDir) {
		this.repoDir = repoDir;
	}
	
	/**
	 * @return the candidateLimitRate
	 */
	public double getCandidateLimitRate() {
		return candidateLimitRate;
	}

	/**
	 * @param candidateLimitRate the candidateLimitRate to set
	 */
	public void setCandidateLimitRate(double candidateLimitRate) {
		this.candidateLimitRate = candidateLimitRate;
	}

	/**
	 * @return the since
	 */
	public Calendar getSince() {
		return since;
	}

	/**
	 * @param since the since to set
	 */
	public void setSince(Calendar since) {
		this.since = since;
	}

	/**
	 * @return the until
	 */
	public Calendar getUntil() {
		return until;
	}

	/**
	 * @param until the until to set
	 */
	public void setUntil(Calendar until) {
		this.until = until;
	}

	/**
	 * @return the targetProduct
	 */
	public String getTargetProduct() {
		return targetProduct;
	}

	/**
	 * @param targetProduct the targetProduct to set
	 */
	public void setTargetProduct(String targetProduct) {
		this.targetProduct = targetProduct;
	}

	/**
	 * @return the sourceCodeDir
	 */
	public String getSourceCodeDir() {
		return sourceCodeDir;
	}

	/**
	 * @param sourceCodeDir the sourceCodeDir to set
	 */
	public void setSourceCodeDir(String sourceCodeDir) {
		this.sourceCodeDir = sourceCodeDir;
	}

	/**
	 * @param bugFilePath the bugFilePath to set
	 */
	public void setBugFilePath(String bugFilePath) {
		this.bugFilePath = bugFilePath;
	}

	/**
	 * @return the runLevel
	 */
	public String getRunLevel() {
		return runLevel;
	}

	/**
	 * @param runLevel the runLevel to set
	 */
	public void setRunLevel(String runLevel) {
		this.runLevel = runLevel;
	}
	
	public boolean isMethodLevel() {
		return runLevel.equals(RUN_LEVEL_METHOD);
	}
	
	public boolean isFileLevel() {
		return runLevel.equals(RUN_LEVEL_FILE);
	}
}
