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
public class ExtendedIntegratedAnalysisValue extends IntegratedAnalysisValue {
	private int methodID;
	private double bliaMethodScore;
	
	public ExtendedIntegratedAnalysisValue() {
		super();
		setMethodID(-1);
		setBliaMethodScore(0.0);
	}

	/**
	 * @return the methodID
	 */
	public int getMethodID() {
		return methodID;
	}

	/**
	 * @param methodID the methodID to set
	 */
	public void setMethodID(int methodID) {
		this.methodID = methodID;
	}

	/**
	 * @return the bliaMethodScore
	 */
	public double getBliaMethodScore() {
		return bliaMethodScore;
	}

	/**
	 * @param bliaMethodScore the bliaMethodScore to set
	 */
	public void setBliaMethodScore(double bliaMethodScore) {
		this.bliaMethodScore = bliaMethodScore;
	}
}
