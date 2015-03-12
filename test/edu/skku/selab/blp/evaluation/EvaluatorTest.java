/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.evaluation;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.blia.analysis.BLIA;
import edu.skku.selab.blp.blia.analysis.BugRepoAnalyzer;
import edu.skku.selab.blp.blia.analysis.SourceFileAnalyzer;
import edu.skku.selab.blp.blia.indexer.BugCorpusCreator;
import edu.skku.selab.blp.blia.indexer.BugSourceFileVectorCreator;
import edu.skku.selab.blp.blia.indexer.BugVectorCreator;
import edu.skku.selab.blp.blia.indexer.SourceFileCorpusCreator;
import edu.skku.selab.blp.blia.indexer.SourceFileIndexer;
import edu.skku.selab.blp.blia.indexer.SourceFileVectorCreator;
import edu.skku.selab.blp.buglocator.analysis.BugLocator;
import edu.skku.selab.blp.buglocator.analysis.BugLocatorWithFile;
import edu.skku.selab.blp.buglocator.analysis.BugRepoAnalyzerWithFile;
import edu.skku.selab.blp.buglocator.analysis.SourceFileAnalyzerWithFile;
import edu.skku.selab.blp.buglocator.indexer.BugCorpusCreatorWithFile;
import edu.skku.selab.blp.buglocator.indexer.BugVectorCreatorWithFile;
import edu.skku.selab.blp.buglocator.indexer.SourceFileCorpusCreatorWithFile;
import edu.skku.selab.blp.buglocator.indexer.SourceFileIndexerWithFile;
import edu.skku.selab.blp.buglocator.indexer.SourceFileVectorCreatorWithFile;
import edu.skku.selab.blp.db.dao.DbUtil;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.evaluation.Evaluator;
import edu.skku.selab.blp.test.utils.TestConfiguration;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class EvaluatorTest {

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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	public void runBugLocator() throws Exception {
		long startTime = System.currentTimeMillis();

		String version = SourceFileDAO.DEFAULT_VERSION_STRING;
		SourceFileCorpusCreator sourceFileCorpusCreator = new SourceFileCorpusCreator();
		sourceFileCorpusCreator.create(version);
		
		SourceFileIndexer sourceFileIndexer = new SourceFileIndexer();
		sourceFileIndexer.createIndex(version);
		sourceFileIndexer.computeLengthScore(version);
		
		SourceFileVectorCreator sourceFileVectorCreator = new SourceFileVectorCreator();
		sourceFileVectorCreator.create(version);

		// Create SordtedID.txt
		BugCorpusCreator bugCorpusCreator = new BugCorpusCreator();
		boolean stackTraceAnalysis = false;
		bugCorpusCreator.create(stackTraceAnalysis);
		
		BugSourceFileVectorCreator bugSourceFileVectorCreator = new BugSourceFileVectorCreator(); 
		bugSourceFileVectorCreator.create(version);
		
		SourceFileAnalyzer sourceFileAnalyzer = new SourceFileAnalyzer();
		boolean useStructuredInformation = false;
		sourceFileAnalyzer.analyze(version, useStructuredInformation);

		BugVectorCreator bugVectorCreator = new BugVectorCreator();
		bugVectorCreator.create();

		BugRepoAnalyzer bugRepoAnalyzer = new BugRepoAnalyzer();
		bugRepoAnalyzer.analyze();
		
		BugLocator bugLocator = new BugLocator();
		bugLocator.analyze();
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time of BugLocator for evaluation: %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		
	}
	
	@Test
	public void verifyBugLocatorEvaluate() throws Exception {
		DbUtil dbUtil = new DbUtil();
		dbUtil.initializeAllData();

		String projectName = "swt";
		String productName = TestConfiguration.getProductName(projectName);
		String algorithmName = Evaluator.ALG_BUG_LOCATOR;
		float alpha = 0.2f;
		TestConfiguration.setProperty(projectName, algorithmName, alpha, 0, 0);
		runBugLocator();

		String algorithmDescription = "[BugLocator] alpha: " + alpha;
		Evaluator evaluator1 = new Evaluator(productName, algorithmName, algorithmDescription, alpha, 0, 0);
		evaluator1.evaluate();

//		dbUtil.initializeAllAnalysisData();
//		
//		alpha = 0.5f;
//		setProperty(alpha, beta);
//		runBugLocator();
//		algorithmDescription = "[BugLocator] alpha: " + alpha;
//		Evaluator evaluator2 = new Evaluator(productName, algorithmName, algorithmDescription);
//		evaluator2.evaluate();
//		
//		
//		dbUtil.initializeAllAnalysisData();
//		
//		alpha = 0.7f;
//		setProperty(alpha, beta);
//		runBugLocator();
//		algorithmDescription = "[BugLocator] alpha: " + alpha;
//		Evaluator evaluator3 = new Evaluator(productName, algorithmName, algorithmDescription);
//		evaluator3.evaluate();
	}
	
	@Test
	public void verifyBLIAEvaluateForSwt() throws Exception {
		boolean isNeededToPrepare = false;

		String projectName = "swt";
		String productName = TestConfiguration.getProductName(projectName);
		String algorithmName = Evaluator.ALG_BLIA;
		float alpha = 0.2f;   
		float beta = 0.3f;
		int pastDays = 15;
		String repoDir = Property.SWT_REPO_DIR;
		TestConfiguration.setProperty(projectName, algorithmName, alpha, beta, pastDays, repoDir);
		
		String version = SourceFileDAO.DEFAULT_VERSION_STRING;
		long startTime = System.currentTimeMillis();

		BLIA blia = new BLIA();

		DbUtil dbUtil = new DbUtil();
		boolean useStrucrutedInfo = true;
		if (isNeededToPrepare) {
			dbUtil.initializeAllData();
			
			// for swt project ONLY
			Calendar since = new GregorianCalendar(2002, Calendar.APRIL, 1);
			Calendar until = new GregorianCalendar(2010, Calendar.MAY, 1);
			blia.preAnalyze(useStrucrutedInfo, since.getTime(), until.getTime());
		}

		blia.analyze(version);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time of BLIA for evaluation: %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		
		
		String algorithmDescription = "[BLIA] alpha: " + alpha + ", beta: " + beta + ", pastDays: " + pastDays;
		if (useStrucrutedInfo) {
			algorithmDescription += " with structured info";
		}
		Evaluator evaluator = new Evaluator(productName, algorithmName, algorithmDescription, alpha, beta, pastDays);
		evaluator.evaluate();

//		dbUtil.initializeAllAnalysisData();
//		
//		alpha = 0.5f;
//		setProperty(alpha, beta);
//		runBugLocator();
//		algorithmDescription = "[BugLocator] alpha: " + alpha;
//		Evaluator evaluator2 = new Evaluator(productName, algorithmName, algorithmDescription);
//		evaluator2.evaluate();
//		
//		
//		dbUtil.initializeAllAnalysisData();
//		
//		alpha = 0.7f;
//		setProperty(alpha, beta);
//		runBugLocator();
//		algorithmDescription = "[BugLocator] alpha: " + alpha;
//		Evaluator evaluator3 = new Evaluator(productName, algorithmName, algorithmDescription);
//		evaluator3.evaluate();
	}
	
	@Test
	public void verifyBLIAEvaluateForAspectJ() throws Exception {
		boolean isNeededToPrepare = false;

		String projectName = "aspectj";
		String productName = TestConfiguration.getProductName(projectName);
		String algorithmName = Evaluator.ALG_BLIA;
		float alpha = 0.3f;   
		float beta = 0.3f;
		int pastDays = 15;
		String repoDir = Property.ASPECTJ_REPO_DIR;
		TestConfiguration.setProperty(projectName, algorithmName, alpha, beta, pastDays, repoDir);
		
		String version = SourceFileDAO.DEFAULT_VERSION_STRING;
		long startTime = System.currentTimeMillis();

		BLIA blia = new BLIA();

		DbUtil dbUtil = new DbUtil();
		boolean useStrucrutedInfo = true;
		if (isNeededToPrepare) {
			dbUtil.initializeAllData();
			
			// for aspectj project ONLY
			Calendar since = new GregorianCalendar(2002, Calendar.DECEMBER, 1);
			Calendar until = new GregorianCalendar(2010, Calendar.MAY, 15);
			blia.preAnalyze(useStrucrutedInfo, since.getTime(), until.getTime());
		}

		blia.analyze(version);
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time of BLIA for evaluation: %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		
		
		String algorithmDescription = "[BLIA] alpha: " + alpha + ", beta: " + beta + ", pastDays: " + pastDays;
		if (useStrucrutedInfo) {
			algorithmDescription += " with structured info";
		}
		Evaluator evaluator = new Evaluator(productName, algorithmName, algorithmDescription, alpha, beta, pastDays);
		evaluator.evaluate();
	}

}
