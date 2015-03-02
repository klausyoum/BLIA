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
public class AnalysisValue {
	protected String name;
	protected String version;
	protected String productName;
	protected String word;
	protected int sourceFileVersionID;
	protected int wordID;
	protected int termCount;
	protected int invDocCount;
	protected double tf;
	protected double idf;
	protected double vector;
	
	final private int INIT_VALUE = -1;

	public AnalysisValue() {
		name = "";
		version = "";
		productName = "";
		word = "";
		sourceFileVersionID = INIT_VALUE;
		setWordID(INIT_VALUE);
		termCount = INIT_VALUE;
		invDocCount = INIT_VALUE;
		tf = INIT_VALUE;
		idf = INIT_VALUE;
		vector = INIT_VALUE;
	}
	
	public AnalysisValue(String name, String productName, String word, double vector) {
		setName(name);
		version = "";
		setProductName(productName);
		setWord(word);
		setWordID(INIT_VALUE);
		termCount = INIT_VALUE;
		invDocCount = INIT_VALUE;
		tf = INIT_VALUE;
		idf = INIT_VALUE;
		this.vector = vector;
	}
	
	public AnalysisValue(String name, String productName, String word, int termCount, int invDocCount, double tf, double idf, double vector) {
		setName(name);
		version = "";
		setProductName(productName);
		setWord(word);
		setWordID(INIT_VALUE);
		setTermCount(termCount);
		setInvDocCount(invDocCount);
		setTf(tf);
		setIdf(idf);
		setVector(vector);
	}	
	
	/**
	 * 
	 */
	public AnalysisValue(String name, String productName, String version, 
			String word, int termCount, int invDocCount) {
		setName(name);
		setVersion(version);
		setProductName(productName);
		setWord(word);
		setSourceFileVersionID(INIT_VALUE);
		setWordID(INIT_VALUE);
		setTermCount(termCount);
		setInvDocCount(invDocCount);
		setTf(INIT_VALUE);
		setIdf(INIT_VALUE);
		setVector(INIT_VALUE);
	}
	
	/**
	 * 
	 */
	public AnalysisValue(String name, String productName, String version, 
			String word, int termCount, int invDocCount, double tf, double idf, double vector) {
		setName(name);
		setVersion(version);
		setProductName(productName);
		setWord(word);
		setSourceFileVersionID(INIT_VALUE);
		setWordID(INIT_VALUE);
		setTermCount(termCount);
		setInvDocCount(invDocCount);
		setTf(tf);
		setIdf(idf);
		setVector(vector);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
	 * @return the termCount
	 */
	public int getTermCount() {
		return termCount;
	}

	/**
	 * @param termCount the termCount to set
	 */
	public void setTermCount(int termCount) {
		this.termCount = termCount;
	}

	/**
	 * @return the invDocCount
	 */
	public int getInvDocCount() {
		return invDocCount;
	}

	/**
	 * @param invDocCount the invDocCount to set
	 */
	public void setInvDocCount(int invDocCount) {
		this.invDocCount = invDocCount;
	}

	/**
	 * @return the tf
	 */
	public double getTf() {
		return tf;
	}

	/**
	 * @param tf the tf to set
	 */
	public void setTf(double tf) {
		this.tf = tf;
	}

	/**
	 * @return the idf
	 */
	public double getIdf() {
		return idf;
	}

	/**
	 * @param idf the idf to set
	 */
	public void setIdf(double idf) {
		this.idf = idf;
	}

	/**
	 * @return the vector
	 */
	public double getVector() {
		return vector;
	}

	/**
	 * @param vector the vector to set
	 */
	public void setVector(double vector) {
		this.vector = vector;
	}

	/**
	 * @return the word
	 */
	public String getWord() {
		return word;
	}

	/**
	 * @param word the word to set
	 */
	public void setWord(String word) {
		this.word = word;
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
	 * @return the wordID
	 */
	public int getWordsID() {
		return wordID;
	}

	/**
	 * @param wordID the wordID to set
	 */
	public void setWordID(int wordID) {
		this.wordID = wordID;
	}
}
