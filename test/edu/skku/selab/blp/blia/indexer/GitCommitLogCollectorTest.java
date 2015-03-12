/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.indexer;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.db.dao.BaseDAO;
import edu.skku.selab.blp.db.dao.DbUtil;
import edu.skku.selab.blp.test.utils.TestConfiguration;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class GitCommitLogCollectorTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestConfiguration.setProperty();
		
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
	public void verifyCollectCommitLog() throws Exception {
		long startTime = System.currentTimeMillis();
		
		String repoDir = "D:\\workspace\\aspectj\\org.aspectj\\.git";
		String productName = "aspectj";
		Calendar since = new GregorianCalendar(2002, Calendar.DECEMBER, 1);
		Calendar until = new GregorianCalendar(2010, Calendar.MARCH, 15);
		GitCommitLogCollector gitCommitLogCollector = new GitCommitLogCollector(productName, repoDir);
		gitCommitLogCollector.collectCommitLog(since.getTime(), until.getTime());
		
//		String repoDir = "D:\\workspace\\eclipse.platform.swt\\.git";
//		String productName = Property.getInstance().getProductName();
//		Calendar since = new GregorianCalendar(2004, Calendar.OCTOBER, 1);
//		Calendar until = new GregorianCalendar(2010, Calendar.MAY, 1);
//		GitCommitLogCollector gitCommitLogCollector = new GitCommitLogCollector(productName, repoDir);
//		gitCommitLogCollector.collectCommitLog(since.getTime(), until.getTime());
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time of collectCommitLog() for %s: %d.%d sec\n", productName, elapsedTime / 1000, elapsedTime % 1000);		

	}

}
