/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp;

import edu.skku.selab.blp.blia.analysis.BLIA;
import edu.skku.selab.blp.db.dao.DbUtil;
import edu.skku.selab.blp.evaluation.Evaluator;
import edu.skku.selab.blp.evaluation.EvaluatorForMethodLevel;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BLP {
	private static void initializeDB() throws Exception {
		Property prop = Property.getInstance();
		
		DbUtil dbUtil = new DbUtil();
		String productName[] = {
				Property.ASPECTJ,
				Property.ECLIPSE,
				Property.SWT,
				Property.ZXING};
		
		for (int i = 0; i < productName.length; i++) {
			dbUtil.openConnetion(productName[i]);

			dbUtil.dropAllAnalysisTables();
			dbUtil.createAllAnalysisTables();

			prop.setProductName(productName[i]);
			dbUtil.initializeAllData();

			dbUtil.closeConnection();
		}
		
		dbUtil.openEvaluationDbConnection();

		dbUtil.dropEvaluationTable();
		dbUtil.createEvaluationTable();
		
		dbUtil.initializeExperimentResultData();
		
		dbUtil.closeConnection();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Load properties data to run BLIA
		Property prop = Property.loadInstance();
		
		// initialize DB and create all tables.
		initializeDB();

		// Run BLIA algorithm
		BLIA blia = new BLIA();
		blia.run();
		
		String algorithmDescription = "[BLIA] alpha: " + prop.getAlpha() +
				", beta: " + prop.getBeta() + ", gamma: " + prop.getGamma() + ", pastDays: " + prop.getPastDays() +
				", cadidateLimitRate: " + prop.getCandidateLimitRate();

		// Evaluate the accuracy result of BLIA
		Evaluator evaluator1 = new Evaluator(prop.getProductName(),
				Evaluator.ALG_BLIA_FILE, algorithmDescription, prop.getAlpha(),
				prop.getBeta(), prop.getGamma(), prop.getPastDays(),
				prop.getCandidateLimitRate());
		evaluator1.evaluate();
		
		Evaluator evaluator2 = new EvaluatorForMethodLevel(prop.getProductName(),
				EvaluatorForMethodLevel.ALG_BLIA_METHOD, algorithmDescription, prop.getAlpha(),
				prop.getBeta(), prop.getGamma(), prop.getPastDays(),
				prop.getCandidateLimitRate());
		evaluator2.evaluate();
	}
}
