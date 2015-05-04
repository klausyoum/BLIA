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
import edu.skku.selab.blp.blia.indexer.SourceFileVectorCreator;
import edu.skku.selab.blp.db.dao.BaseDAO;
import edu.skku.selab.blp.db.dao.DbUtil;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.evaluation.Evaluator;
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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		String projectName = Property.ASPECTJ;
		String algorithmName = "BLIA";
		double alpha = 0.41;
		double beta = 0.13;
		int pastDate = 60;
		String repoDir = Property.ASPECTJ_REPO_DIR;
		TestConfiguration.setProperty(projectName, algorithmName, alpha, beta, pastDate, repoDir);

		DbUtil dbUtil = new DbUtil();
		String dbName = Property.getInstance().getProductName();
		dbUtil.openConnetion(dbName);
		dbUtil.initializeAllData();
		dbUtil.closeConnection();
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
		
		Calendar since = new GregorianCalendar(2004, Calendar.OCTOBER, 1);
		Calendar until = new GregorianCalendar(2010, Calendar.MAY, 1);
		blia.prepareAnalysisData(useStrucrutedInfo, since.getTime(), until.getTime());
		blia.preAnalyze();
		blia.analyze(version, true);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time of BLIA: %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);
	}
	
	private String getElapsedTimeSting(long startTime) {
		long elapsedTime = System.currentTimeMillis() - startTime;
		String elpsedTimeString = (elapsedTime / 1000) + "." + (elapsedTime % 1000);
		return elpsedTimeString;
	}
	
	@Test
	public void verifyBLIAPerformance() throws Exception {
		String version = SourceFileDAO.DEFAULT_VERSION_STRING;

		BLIA blia = new BLIA();
		boolean useStrucrutedInfo = true;
		
		// for AspectJ
		Calendar since = new GregorianCalendar(2002, Calendar.JULY, 1);
		Calendar until = new GregorianCalendar(2010, Calendar.MAY, 15);
		
		System.out.printf("[STARTED] BLIA pre-analysis.\n");
		long startTime = System.currentTimeMillis();
		blia.prepareAnalysisData(useStrucrutedInfo, since.getTime(), until.getTime());
		blia.preAnalyze();
		System.out.printf("[DONE] BLIA pre-analysis.(%s sec)\n", getElapsedTimeSting(startTime));
		
		System.out.printf("[STARTED] BLIA analysis.\n");
		startTime = System.currentTimeMillis();
		blia.analyze(version, true);
		System.out.printf("[DONE] BLIA analysis.(%s sec)\n", getElapsedTimeSting(startTime));
	}
}
