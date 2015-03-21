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
import org.junit.Ignore;
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
	public void verifyEvaluateBugLocator() throws Exception {
		String productName = Property.SWT;
		String algorithmName = Evaluator.ALG_BUG_LOCATOR;
		float alpha = 0.2f;
		TestConfiguration.setProperty(productName, algorithmName, alpha, 0, 0);

		DbUtil dbUtil = new DbUtil();
		String dbName = Property.getInstance().getProductName();
		dbUtil.openConnetion(dbName);
		dbUtil.initializeAllData();
		dbUtil.closeConnection();
		
		runBugLocator();

		String algorithmDescription = "[BugLocator] alpha: " + alpha;
		Evaluator evaluator1 = new Evaluator(productName, algorithmName, algorithmDescription, alpha, 0, 0);
		evaluator1.evaluate();
	}
	
	private void runBLIA(boolean useStrucrutedInfo, boolean preAnalyze, EvaluationProperty evaluationProperty) throws Exception {
		String algorithmName = Evaluator.ALG_BLIA;
		TestConfiguration.setProperty(evaluationProperty.getProductName(),
				algorithmName, evaluationProperty.getAlpha(), evaluationProperty.getBeta(),
				evaluationProperty.getPastDays(), evaluationProperty.getRepoDir());
		
		String version = SourceFileDAO.DEFAULT_VERSION_STRING;
		long startTime = System.currentTimeMillis();

		BLIA blia = new BLIA();

		if (preAnalyze) {
			DbUtil dbUtil = new DbUtil();
			String dbName = Property.getInstance().getProductName();
			dbUtil.openConnetion(dbName);
			dbUtil.initializeAllData();
			dbUtil.closeConnection();
			
			blia.preAnalyze(useStrucrutedInfo, evaluationProperty.getSince().getTime(), evaluationProperty.getUntil().getTime());
		}

		blia.analyze(version);

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time of BLIA for evaluation: %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		
	}
	
	
	@Test
	public void verifyEvaluateBLIAOnce() throws Exception {
		boolean preAnalyze = true;
		boolean useStrucrutedInfo = true;
		
		// Change target project for experiment if you want
//		String productName = Property.SWT;
//		String productName = Property.ASPECTJ;
//		String productName = Property.ZXING;
		String productName = Property.ECLIPSE;
		
		EvaluationProperty evaluationProerpty = EvaluationPropertyFactory.getEvaluationProperty(productName);
		runBLIA(useStrucrutedInfo, preAnalyze, evaluationProerpty);
		
		String algorithmDescription = "[BLIA] alpha: " + evaluationProerpty.getAlpha() +
				", beta: " + evaluationProerpty.getBeta() + ", pastDays: " + evaluationProerpty.getPastDays();
		if (useStrucrutedInfo) {
			algorithmDescription += " with structured info";
		}
		Evaluator evaluator = new Evaluator(productName, Evaluator.ALG_BLIA, algorithmDescription,
				evaluationProerpty.getAlpha(), evaluationProerpty.getBeta(), evaluationProerpty.getPastDays());
		evaluator.evaluate();
	}
	
	@Test
	public void verifyEvaluateBLIARepeatedly() throws Exception {
		// Before this method running, verifyEvaluateBLIAOnce() should be called to create indexing DB
		boolean preAnalyze = false;
		boolean useStrucrutedInfo = true;
		
		// Change target project for experiment if you want
//		String productName = Property.SWT;
//		String productName = Property.ASPECTJ;
		String productName = Property.ZXING;
//		String productName = Property.ECLIPSE;

		EvaluationProperty evaluationProerpty = EvaluationPropertyFactory.getEvaluationProperty(productName);
		
		for (double alpha = 0.0; alpha < 1.0; alpha += 0.1) {
			for (double beta = 0.0; beta < 1.0; beta += 0.1) {
				evaluationProerpty.setAlpha(alpha);
				evaluationProerpty.setBeta(beta);
				runBLIA(useStrucrutedInfo, preAnalyze, evaluationProerpty);

				String algorithmDescription = "[BLIA] alpha: " + evaluationProerpty.getAlpha() +
						", beta: " + evaluationProerpty.getBeta() + ", pastDays: " + evaluationProerpty.getPastDays();
				if (useStrucrutedInfo) {
					algorithmDescription += " with structured info";
				}
				Evaluator evaluator = new Evaluator(productName, Evaluator.ALG_BLIA, algorithmDescription,
						evaluationProerpty.getAlpha(), evaluationProerpty.getBeta(), evaluationProerpty.getPastDays());
				evaluator.evaluate();
			}
		}
	}	
}
