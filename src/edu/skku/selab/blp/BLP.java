/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp;

import edu.skku.selab.blp.blia.analysis.BLIA;
import edu.skku.selab.blp.evaluation.Evaluator;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BLP {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Load properties data to run BLIA
		Property prop = Property.loadInstance();

		// Run BLIA algorithm
		BLIA blia = new BLIA();
		blia.run();

		String algorithmDescription = "[BLIA] alpha: " + prop.getAlpha()
				+ ", beta: " + prop.getBeta() + ", pastDays: "
				+ prop.getPastDays() + ", cadidateLimitRate: "
				+ prop.getCandidateLimitRate();

		// Evaluate the accuracy result of BLIA
		Evaluator evaluator = new Evaluator(prop.getProductName(),
				Evaluator.ALG_BLIA, algorithmDescription, prop.getAlpha(),
				prop.getBeta(), prop.getPastDays(),
				prop.getCandidateLimitRate());
		evaluator.evaluate();
	}
}
