/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db.dao;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.skku.selab.blp.db.ExperimentResult;
import edu.skku.selab.blp.db.dao.ExperimentResultDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class ExperimentResultDAOTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DbUtil dbUtil = new DbUtil();
		dbUtil.initializeAllData();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		BaseDAO.closeConnection();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void verify() {
		// empty
	}
	
	/**
	 * Ignored because all experimental results can be deleted. 
	 * 
	 * @throws Exception
	 */
	@Ignore
//	@Test
	public void verifyGetExperimentResult() throws Exception {
		ExperimentResultDAO experimentResultDAO = new ExperimentResultDAO();
		experimentResultDAO.deleteAllExperimentResults();

		int top1 = 1;
		int top5 = 5;
		int top10 = 10;
		double MRR = 0.324;
		double MAP = 0.432;
		String productName = "BLIA";
		String algorithmName = "BLIA";
		String algorithmDescription = "[TEST] Bug Localization with Integrated Analysis";
		Date experimentDate = new Date(System.currentTimeMillis());
		final double delta = 0.00001;
		
		ExperimentResult experimentResult = new ExperimentResult();
		experimentResult.setTop1(top1);
		experimentResult.setTop5(top5);
		experimentResult.setTop10(top10);
		experimentResult.setMRR(MRR);
		experimentResult.setMAP(MAP);
		experimentResult.setProductName(productName);
		experimentResult.setAlgorithmName(algorithmName);
		experimentResult.setAlgorithmDescription(algorithmDescription);
		experimentResult.setExperimentDate(experimentDate);		
		
		experimentResultDAO.insertExperimentResult(experimentResult);
		ExperimentResult returnValue = experimentResultDAO.getExperimentResult(productName, algorithmName);
		assertEquals(top1, returnValue.getTop1());
		assertEquals(top5, returnValue.getTop5());
		assertEquals(top10, returnValue.getTop10());
		assertEquals(MRR, returnValue.getMRR(), delta);
		assertEquals(MAP, returnValue.getMAP(), delta);
		assertEquals(productName, returnValue.getProductName());
		assertEquals(algorithmName, returnValue.getAlgorithmName());
		assertEquals(algorithmDescription, returnValue.getAlgorithmDescription());
		assertTrue((experimentDate.getTime() / 1000) == (returnValue.getExperimentDate().getTime() / 1000));
	}

}
