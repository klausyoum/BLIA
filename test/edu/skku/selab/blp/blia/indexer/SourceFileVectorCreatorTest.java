/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.indexer;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.blia.indexer.SourceFileCorpusCreator;
import edu.skku.selab.blp.blia.indexer.SourceFileVectorCreator;
import edu.skku.selab.blp.buglocator.indexer.SourceFileCorpusCreatorWithFile;
import edu.skku.selab.blp.buglocator.indexer.SourceFileIndexerWithFile;
import edu.skku.selab.blp.buglocator.indexer.SourceFileVectorCreatorWithFile;
import edu.skku.selab.blp.db.dao.BaseDAO;
import edu.skku.selab.blp.db.dao.DbUtil;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.test.utils.TestConfiguration;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileVectorCreatorTest {
	
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
	public void verifyCreateWithSourceFileCorpusCreator() throws Exception {
		String version = SourceFileDAO.DEFAULT_VERSION_STRING;
		SourceFileCorpusCreator sourceFileCorpusCreator = new SourceFileCorpusCreator();
		sourceFileCorpusCreator.create(version);
		
		SourceFileVectorCreator sourceFileVectorCreator = new SourceFileVectorCreator();
		sourceFileVectorCreator.createIndex(version);
		sourceFileVectorCreator.create(version);
	}
	
	@Test
	public void verifyCreateWithStructuredSourceFileCorpusCreator() throws Exception {
		String version = SourceFileDAO.DEFAULT_VERSION_STRING;

		long startTime = System.currentTimeMillis();
		System.out.printf("[STARTED] StructuredSourceFileCorpusCreator.create()\n");
		StructuredSourceFileCorpusCreator sourceFileCorpusCreator = new StructuredSourceFileCorpusCreator();
		sourceFileCorpusCreator.create(version);
		System.out.printf("[DONE] StructuredSourceFileCorpusCreator.create().(Total %s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
		
		startTime = System.currentTimeMillis();
		System.out.printf("[STARTED] SourceFileVectorCreator.createIndex()\n");
		SourceFileVectorCreator sourceFileVectorCreator = new SourceFileVectorCreator();
		sourceFileVectorCreator.createIndex(version);
		System.out.printf("[DONE] SourceFileVectorCreator.createIndex().(Total %s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));

		startTime = System.currentTimeMillis();
		System.out.printf("[STARTED] SourceFileVectorCreator.computeLengthScore()\n");
		sourceFileVectorCreator.computeLengthScore(version);
		System.out.printf("[DONE] SourceFileVectorCreator.computeLengthScore().(Total %s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
		
		startTime = System.currentTimeMillis();
		System.out.printf("[STARTED] SourceFileVectorCreator.create()\n");
		sourceFileVectorCreator.create(version);
		System.out.printf("[DONE] SourceFileVectorCreator.create().(Total %s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
	}
	
	@Test
	public void verifyCreateIndex() throws Exception {
		// Following function is needed to set file count for Property.getFileCount() at BugRepoAnalyzer
		String version = SourceFileDAO.DEFAULT_VERSION_STRING;
		SourceFileCorpusCreator sourceFileCorpusCreator = new SourceFileCorpusCreator();
		sourceFileCorpusCreator.create(version);
		
		SourceFileVectorCreator sourceFileVectorCreator = new SourceFileVectorCreator();
		sourceFileVectorCreator.createIndex(version);
		sourceFileVectorCreator.computeLengthScore(version);
	}


}
