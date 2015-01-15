/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.db;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class AnalysisValue {
	private String name;
	private String version;
	private String productName;
	private String corpus;
	private int termCount;
	private int invDocCount;
	private double tf;
	private double idf;
	private double vector;

	public AnalysisValue() {
		name = "";
		version = "";
		productName = "";
		corpus = "";
		termCount = -1;
		invDocCount = -1;
		tf = -1.0F;
		idf = -1.0F;
		vector = -1.0F;
	}
	
	public AnalysisValue(String name, String productName, String corpus, double vector) {
		this.name = name;
		this.productName = productName;
		this.corpus = corpus;
		termCount = -1;
		invDocCount = -1;
		tf = -1.0F;
		idf = -1.0F;
		this.vector = vector;
	}	
	
	/**
	 * 
	 */
	public AnalysisValue(String name, String productName, String version, 
			String corpus, int termCount, int invDocCount, double tf, double idf, double vector) {
		setName(name);
		setVersion(version);
		setProductName(productName);
		setCorpus(corpus);
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
	 * @return the corpus
	 */
	public String getCorpus() {
		return corpus;
	}

	/**
	 * @param corpus the corpus to set
	 */
	public void setCorpus(String corpus) {
		this.corpus = corpus;
	}

}
