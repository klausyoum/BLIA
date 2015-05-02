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
		System.out.printf("[STARTED] Source file corpus creating.\n");
		SourceFileCorpusCreator sourceFileCorpusCreator = new SourceFileCorpusCreator();
		sourceFileCorpusCreator.create(version);
		System.out.printf("[DONE] Source file corpus creating.(%s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
		
		System.out.printf("[STARTED] Source file vector creating.\n");
		SourceFileVectorCreator sourceFileVectorCreator = new SourceFileVectorCreator();
		sourceFileVectorCreator.createIndex(version);
		sourceFileVectorCreator.computeLengthScore(version);
		sourceFileVectorCreator.create(version);
		System.out.printf("[DONE] Source file vector creating.(%s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
		
		// Create SordtedID.txt
		System.out.printf("[STARTED] Bug corpus creating.\n");
		BugCorpusCreator bugCorpusCreator = new BugCorpusCreator();
		boolean stackTraceAnalysis = false;
		bugCorpusCreator.create(stackTraceAnalysis);
		System.out.printf("[DONE] Bug corpus creating.(%s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
		
		System.out.printf("[STARTED] Bug-Source file vector creating.\n");
		BugSourceFileVectorCreator bugSourceFileVectorCreator = new BugSourceFileVectorCreator(); 
		bugSourceFileVectorCreator.create(version);
		System.out.printf("[DONE] Bug-Source file vector creating.(%s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
		
		System.out.printf("[STARTED] Source file analysis.\n");
		SourceFileAnalyzer sourceFileAnalyzer = new SourceFileAnalyzer();
		boolean useStructuredInformation = false;
		sourceFileAnalyzer.analyze(version, useStructuredInformation);
		System.out.printf("[DONE] Source file analysis.(%s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));

		System.out.printf("[STARTED] Bug vector creating.\n");
		BugVectorCreator bugVectorCreator = new BugVectorCreator();
		bugVectorCreator.create();
		System.out.printf("[DONE] Bug vector creating.(%s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));

		System.out.printf("[STARTED] Bug report analysis.\n");
		BugRepoAnalyzer bugRepoAnalyzer = new BugRepoAnalyzer();
		bugRepoAnalyzer.analyze();
		System.out.printf("[DONE] Bug report analysis.(%s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
		
		System.out.printf("[STARTED] BugLocator analysis.\n");
		BugLocator bugLocator = new BugLocator();
		bugLocator.analyze();
		System.out.printf("[DONE] BugLocator analysis.(%s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
	}
	
	@Test
	public void verifyEvaluateBugLocator() throws Exception {
		String productName = Property.ZXING;
		String algorithmName = Evaluator.ALG_BUG_LOCATOR;
		double alpha = 0.2;
		TestConfiguration.setProperty(productName, algorithmName, alpha, 0.0, 0);

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
	
	private void runBLIA(boolean useStrucrutedInfo, boolean prepareAnalysisData, boolean preAnalyze, boolean analyze, EvaluationProperty evaluationProperty) throws Exception {
		String algorithmName = Evaluator.ALG_BLIA;
		TestConfiguration.setProperty(evaluationProperty.getProductName(),
				algorithmName, evaluationProperty.getAlpha(), evaluationProperty.getBeta(),
				evaluationProperty.getPastDays(), evaluationProperty.getRepoDir());
		
		String version = SourceFileDAO.DEFAULT_VERSION_STRING;
		long startTime = System.currentTimeMillis();

		BLIA blia = new BLIA();

		if (prepareAnalysisData) {
			DbUtil dbUtil = new DbUtil();
			String dbName = Property.getInstance().getProductName();
			dbUtil.openConnetion(dbName);
			boolean commitDataIncluded = false;
			dbUtil.initializeAllData(commitDataIncluded);
			dbUtil.closeConnection();

			System.out.printf("[STARTED] BLIA prepareAnalysisData().\n");
			startTime = System.currentTimeMillis();
			blia.prepareAnalysisData(useStrucrutedInfo, evaluationProperty.getSince().getTime(), evaluationProperty.getUntil().getTime());
			System.out.printf("[DONE] BLIA prepareAnalysisData().(Total %s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
		}

		if (preAnalyze) {
			if (!prepareAnalysisData) {
				DbUtil dbUtil = new DbUtil();
				String dbName = Property.getInstance().getProductName();
				dbUtil.openConnetion(dbName);
				dbUtil.initializeAnalysisData();
				dbUtil.closeConnection();
			}
			
			System.out.printf("[STARTED] BLIA pre-anlaysis.\n");
			blia.preAnalyze();
			System.out.printf("[DONE] BLIA pre-anlaysis.(Total %s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
		}
		
		if (analyze) {
			System.out.printf("[STARTED] BLIA anlaysis.\n");
			startTime = System.currentTimeMillis();
			blia.analyze(version);
			System.out.printf("[DONE] BLIA anlaysis.(Total %s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
		}
	}
	
	
	@Test
	public void verifyEvaluateBLIAOnce() throws Exception {
		boolean useStrucrutedInfo = true;
		
		boolean prepareAnalysisData = true;
		boolean preAnalyze = true;
		boolean analyze = true;
		
		// Change target project for experiment if you want
		String productName = Property.SWT;
//		productName = Property.ASPECTJ;
//		productName = Property.ZXING;
//		productName = Property.ECLIPSE;
		
		long totalStartTime = System.currentTimeMillis();
		System.out.printf("[STARTED] BLIA Evaluation once.\n");
		
		EvaluationProperty evaluationProerpty = EvaluationPropertyFactory.getEvaluationProperty(productName);
		runBLIA(useStrucrutedInfo, prepareAnalysisData, preAnalyze, analyze, evaluationProerpty);
		
		if (analyze) {
			String algorithmDescription = "[BLIA] alpha: " + evaluationProerpty.getAlpha() +
					", beta: " + evaluationProerpty.getBeta() + ", pastDays: " + evaluationProerpty.getPastDays();
			if (useStrucrutedInfo) {
				algorithmDescription += " with structured info";
			}
			
			long startTime = System.currentTimeMillis();
			System.out.printf("[STARTED] Evaluator.evaluate().\n");
			Evaluator evaluator = new Evaluator(productName, Evaluator.ALG_BLIA, algorithmDescription,
					evaluationProerpty.getAlpha(), evaluationProerpty.getBeta(), evaluationProerpty.getPastDays());
			evaluator.evaluate();
			System.out.printf("[DONE] Evaluator.evaluate().(Total %s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
		}
		
		System.out.printf("[DONE] BLIA Evaluation once(Total %s sec)\n", TestConfiguration.getElapsedTimeSting(totalStartTime));
	}
	
	@Test
	public void verifyEvaluateBLIARepeatedly() throws Exception {
		// Before this method running, verifyEvaluateBLIAOnce() should be called to create indexing DB
		boolean useStrucrutedInfo = true;
		
		boolean prepareAnalysisData = false;
		boolean preAnalyze = false;
		boolean analyze = true;
		
		// Change target project for experiment if you want
		String productName = Property.SWT;
//		productName = Property.ASPECTJ;
//		productName = Property.ZXING;
		productName = Property.ECLIPSE;

		long startTime = System.currentTimeMillis();
		System.out.printf("[STARTED] BLIA Evaluation repeatedly.\n");
		
		EvaluationProperty evaluationProerpty = EvaluationPropertyFactory.getEvaluationProperty(productName);
		
		for (double alpha = 0.1; alpha <= 0.5; alpha += 0.1) {
			for (double beta = 0.0; beta <= 0.4; beta += 0.1) {
				evaluationProerpty.setAlpha(alpha);
				evaluationProerpty.setBeta(beta);
				runBLIA(useStrucrutedInfo, prepareAnalysisData, preAnalyze, analyze, evaluationProerpty);

				if (analyze) {
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
		
		System.out.printf("[DONE] BLIA Evaluation repeatedly(Total %s sec)\n", TestConfiguration.getElapsedTimeSting(startTime));
	}	
}
