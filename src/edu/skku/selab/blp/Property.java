// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Property.java

package edu.skku.selab.blp;


public class Property {
	final static public String ASPECTJ = "aspectj";
	final static public String ECLIPSE = "eclipse";
	final static public String SWT = "swt";
	final static public String ZXING = "zxing";
	
	final static public String ASPECTJ_PRODUCT = "aspectj";
	final static public String ECLIPSE_PRODUCT = "eclipse-3.1";
	final static public String SWT_PRODUCT = "swt-3.1";
	final static public String ZXING_PRODUCT = "ZXing-1.6";
	
	final static public String ASPECTJ_REPO_DIR = "D:\\workspace\\aspectj\\org.aspectj\\.git";
	final static public String ECLIPSE_REPO_DIR = "D:\\workspace\\eclipse.platform.swt\\.git";
	final static public String SWT_REPO_DIR = "D:\\workspace\\eclipse.platform.swt\\.git";
	final static public String ZXING_REPO_DIR = "D:\\workspace\\zxing\\.git";
	
	public String bugFilePath;
	public String sourceCodeDir;
	private String workDir;
	private int fileCount;
	private int wordCount;
	private int bugReportCount;
	private int bugTermCount;
	private float alpha;
	private float beta;
	private String outputFile;
	private String separator = System.getProperty("file.separator");
	private String lineSeparator = System.getProperty("line.separator");
	private static Property p = null;
	private String productName;
	private int pastDays;
	private String repoDir;

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
		return workDir;
	}

	public static void createInstance(String productName, String bugFilePath, String sourceCodeDir, String workDir, float alpha, float beta, int pastDays, String repoDir, String outputFile) {
		if (null == p) {
			p = new Property(productName, bugFilePath, sourceCodeDir, workDir, alpha, beta, pastDays, repoDir, outputFile);
		}
	}

	public static Property getInstance() {
		return p;
	}
	
	private Property(String productName, String bugFilePath, String sourceCodeDir, String workDir, float alpha, float beta, int pastDays, String repoDir, String outputFile) {
		this.productName = productName;
		this.bugFilePath = bugFilePath;
		this.sourceCodeDir = sourceCodeDir;
		this.workDir = workDir;
		this.setAlpha(alpha);
		this.setBeta(beta);
		this.setPastDays(pastDays);
		this.setRepoDir(repoDir);
		this.outputFile = outputFile;
	}
	
	public float getAlpha() {
		return alpha;
	}

	public float getBeta() {
		return beta;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public String getBugFilePath() {
		return bugFilePath;
	}

	public String getSourceCodeDir() {
		return sourceCodeDir;
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
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	/**
	 * @param beta the beta to set
	 */
	public void setBeta(float beta) {
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
}
