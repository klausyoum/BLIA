/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.evaluation;

import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.skku.selab.blp.Property;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class EvaluationProperty {
	private String productName;
	private double alpha;   
	private double beta;
	private int pastDays;
	private String repoDir;
	private Calendar since;
	private Calendar until;
	private double candidateLimitRate; 
	private int candidateLimitSize;
	
	public EvaluationProperty(String productName, double alpha, double beta, int pastDays,
			String repoDir, Calendar since, Calendar until, double candidateLimitRate, int candidateLimitSize) {
		setProductName(productName);
		setAlpha(alpha);
		setBeta(beta);
		setPastDays(pastDays);
		setRepoDir(repoDir);
		setSince(since);
		setUntil(until);
		setCandidateLimitRate(candidateLimitRate);
		setCandidateLimitSize(candidateLimitSize);
	}
	
	/**
	 * @return the alpha
	 */
	public double getAlpha() {
		return alpha;
	}
	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	/**
	 * @return the beta
	 */
	public double getBeta() {
		return beta;
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
	 * @param pastDays the pastDays to set
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
	 * @return the candidateLimitSize
	 */
	public int getCandidateLimitSize() {
		return candidateLimitSize;
	}

	/**
	 * @param candidateLimitSize the candidateLimitSize to set
	 */
	public void setCandidateLimitSize(int candidateLimitSize) {
		this.candidateLimitSize = candidateLimitSize;
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
}
