/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.db;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class ExperimentResult {
	private int top1;
	private int top5;
	private int top10;
	private double MRR;
	private double MAP;
	private String productName;
	private String algorithmName;
	private String algorithmDescription;
	private Date experimentDate;
	private String experimentDateString;

	/**
	 * 
	 */
	public ExperimentResult() {
		top1 = 0;
		top5 = 0;
		top10 = 0;
		MRR = 0.0;
		MAP = 0.0;
		productName = "";
		algorithmName = "";
		algorithmDescription = "";
		experimentDate = new Date();
		experimentDateString = "";
	}

	/**
	 * @return the top1
	 */
	public int getTop1() {
		return top1;
	}

	/**
	 * @param top1 the top1 to set
	 */
	public void setTop1(int top1) {
		this.top1 = top1;
	}

	/**
	 * @return the top5
	 */
	public int getTop5() {
		return top5;
	}

	/**
	 * @param top5 the top5 to set
	 */
	public void setTop5(int top5) {
		this.top5 = top5;
	}

	/**
	 * @return the top10
	 */
	public int getTop10() {
		return top10;
	}

	/**
	 * @param top10 the top10 to set
	 */
	public void setTop10(int top10) {
		this.top10 = top10;
	}

	/**
	 * @return the mRR
	 */
	public double getMRR() {
		return MRR;
	}

	/**
	 * @param MRR the MRR to set
	 */
	public void setMRR(double MRR) {
		this.MRR = MRR;
	}

	/**
	 * @return the MAP
	 */
	public double getMAP() {
		return MAP;
	}

	/**
	 * @param MAP the MAP to set
	 */
	public void setMAP(double MAP) {
		this.MAP = MAP;
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
	 * @return the algorithmName
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}

	/**
	 * @param algorithmName the algorithmName to set
	 */
	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	/**
	 * @return the algorithmDescription
	 */
	public String getAlgorithmDescription() {
		return algorithmDescription;
	}

	/**
	 * @param algorithmDescription the algorithmDescription to set
	 */
	public void setAlgorithmDescription(String algorithmDescription) {
		this.algorithmDescription = algorithmDescription;
	}

	/**
	 * @return the experimentDate
	 */
	public Date getExperimentDate() {
		return experimentDate;
	}

	/**
	 * @param experimentDate the experimentDate to set
	 */
	public void setExperimentDate(Date experimentDate) {
		this.experimentDate = experimentDate;
		this.experimentDateString = experimentDate.toString();
	}

	/**
	 * @return the experimentDateString
	 */
	public String getExperimentDateString() {
		return experimentDateString;
	}

	/**
	 * @param experimentDateString the experimentDateString to set
	 */
	public void setExperimentDateString(String experimentDateString) {
		this.experimentDateString = experimentDateString;
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			this.experimentDate = simpleDateFormat.parse(experimentDateString);			
		} catch (Exception e) {
			this.experimentDate = null;
			e.printStackTrace();
		}		
	}

}
