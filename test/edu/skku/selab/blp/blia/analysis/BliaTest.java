/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.analysis;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.buglocator.analysis.BugLocator;
import edu.skku.selab.blp.blia.analysis.BugRepoAnalyzer;
import edu.skku.selab.blp.blia.analysis.SourceFileAnalyzer;
import edu.skku.selab.blp.blia.indexer.BugCorpusCreator;
import edu.skku.selab.blp.blia.indexer.BugVectorCreator;
import edu.skku.selab.blp.blia.indexer.SourceFileCorpusCreator;
import edu.skku.selab.blp.blia.indexer.SourceFileIndexer;
import edu.skku.selab.blp.blia.indexer.SourceFileVectorCreator;
import edu.skku.selab.blp.db.dao.BaseDAO;
import edu.skku.selab.blp.db.dao.DbUtil;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.test.utils.TestConfiguration;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BliaTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DbUtil dbUtil = new DbUtil();
		dbUtil.initializeAllData();

		String productName = "swt";
		String algorithmName = "BLIA";
		float alpha = 0.2f;
		float beta = 0.5f;
		int pastDate = 15;
		TestConfiguration.setProperty(productName, algorithmName, alpha, beta, pastDate);
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
	public void verifyBLIA() throws Exception {
		long startTime = System.currentTimeMillis();
		
		String version = SourceFileDAO.DEFAULT_VERSION_STRING;

		BLIA blia = new BLIA();
		boolean useStrucrutedInfo = true;
		boolean useCommitLogAnalysis = true;
		
		Calendar since = new GregorianCalendar(2004, Calendar.OCTOBER, 1);
		Calendar until = new GregorianCalendar(2010, Calendar.MAY, 1);
		blia.prepareIndexData(useStrucrutedInfo, since.getTime(), until.getTime());
		blia.prepareAnalysisData();
		blia.analyze(version, useCommitLogAnalysis);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time of BLIA: %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		
	}
}
