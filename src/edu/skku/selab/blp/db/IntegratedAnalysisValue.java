/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class IntegratedAnalysisValue {
	protected int bugID;
	protected String fileName;
	protected String version;
	protected int sourceFileVersionID;
	protected double vsmScore;
	protected double similarityScore;
	protected double bugLocatorScore;
	protected double stackTraceScore;
	protected double commitLogScore;
	protected double bliaScore;

	/**
	 * 
	 */
	public IntegratedAnalysisValue() {
		bugID = 0;
		fileName = "";
		version = "";
		sourceFileVersionID = -1;
		vsmScore = 0.0;
		similarityScore = 0.0;
		bugLocatorScore = 0.0;
		stackTraceScore = 0.0;
		commitLogScore = 0.0;
		bliaScore = 0.0;
	}

	/**
	 * @return the bugID
	 */
	public int getBugID() {
		return bugID;
	}

	/**
	 * @param bugID the bugID to set
	 */
	public void setBugID(int bugID) {
		this.bugID = bugID;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the vsmScore
	 */
	public double getVsmScore() {
		return vsmScore;
	}

	/**
	 * @param vsmScore the vsmScore to set
	 */
	public void setVsmScore(double vsmScore) {
		this.vsmScore = vsmScore;
	}

	/**
	 * @return the similatiryScore
	 */
	public double getSimilarityScore() {
		return similarityScore;
	}

	/**
	 * @param similarityScore the similarityScore to set
	 */
	public void setSimilarityScore(double similarityScore) {
		this.similarityScore = similarityScore;
	}

	/**
	 * @return the bugLocatorScore
	 */
	public double getBugLocatorScore() {
		return bugLocatorScore;
	}

	/**
	 * @param bugLocatorScore the bugLocatorScore to set
	 */
	public void setBugLocatorScore(double bugLocatorScore) {
		this.bugLocatorScore = bugLocatorScore;
	}

	/**
	 * @return the stackTraceScore
	 */
	public double getStackTraceScore() {
		return stackTraceScore;
	}

	/**
	 * @param stackTraceScore the stackTraceScore to set
	 */
	public void setStackTraceScore(double stackTraceScore) {
		this.stackTraceScore = stackTraceScore;
	}

	/**
	 * @return the bliaScore
	 */
	public double getBliaScore() {
		return bliaScore;
	}

	/**
	 * @param bliaScore the bliaScore to set
	 */
	public void setBliaScore(double bliaScore) {
		this.bliaScore = bliaScore;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the sourceFileVersionID
	 */
	public int getSourceFileVersionID() {
		return sourceFileVersionID;
	}

	/**
	 * @param sourceFileVersionID the sourceFileVersionID to set
	 */
	public void setSourceFileVersionID(int sourceFileVersionID) {
		this.sourceFileVersionID = sourceFileVersionID;
	}

	/**
	 * @return the commitLogScore
	 */
	public double getCommitLogScore() {
		return commitLogScore;
	}

	/**
	 * @param commitLogScore the commitLogScore to set
	 */
	public void setCommitLogScore(double commitLogScore) {
		this.commitLogScore = commitLogScore;
	}
}
