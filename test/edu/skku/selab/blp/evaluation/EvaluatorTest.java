/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.evaluation;

import static org.junit.Assert.*;

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
	
	private static void setProperty(float alpha, float beta) {
		String osName = System.getProperty("os.name");
		String productName = "swt-3.1";
		String bugFilePath = "";
		String sourceCodeDir = "";
		String workDir = "";
		String outputFile = "";

		if (osName.equals("Mac OS X")) {
			bugFilePath = "../Dataset/SWTBugRepository.xml";
			sourceCodeDir = "../Dataset/swt-3.1/src";
			workDir = "./tmp";
			outputFile = "../Results/Blia-swt-0.2.txt";
		} else {
			bugFilePath = "..\\Dataset\\SWTBugRepository.xml";
			sourceCodeDir = "..\\Dataset\\swt-3.1\\src";
			workDir = ".\\tmp";
			outputFile = "..\\Results\\Blia-swt-0.2.txt";
		}
		
		Property.createInstance(productName, bugFilePath, sourceCodeDir, workDir, alpha, beta, outputFile);
		Property.getInstance().setAlpha(alpha);
		Property.getInstance().setBeta(beta);
	}
	
	public void runBugLocator() throws Exception {
		long startTime = System.currentTimeMillis();

		BugLocator bugLocator = new BugLocator();
		bugLocator.analyze();
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time of BugLocator for evaluation: %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		
	}
	
	@Test
	public void verifyBugLocatorEvaluate() throws Exception {
		DbUtil dbUtil = new DbUtil();
		dbUtil.initializeAllData();

		float alpha = 0.2f;
		float beta = 0.5f;
		TestConfiguration.setProperty(alpha, beta);
		runBugLocator();
		String productName = "swt-3.1";
		String algorithmName = Evaluator.ALG_BUG_LOCATOR;
		String algorithmDescription = "[BugLocator] alpha: " + alpha;
		Evaluator evaluator1 = new Evaluator(productName, algorithmName, algorithmDescription);
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
	public void verifyBLIAEvaluate() throws Exception {
		boolean isNeededToPrepare = false;
		
		float alpha = 0.2f;
		float beta = 0.5f;
		TestConfiguration.setProperty(alpha, beta);
		
		long startTime = System.currentTimeMillis();

		BLIA blia = new BLIA();

		DbUtil dbUtil = new DbUtil();		
		if (isNeededToPrepare) {
			dbUtil.initializeAllData();
			blia.prepareIndexData();
		} else {
			dbUtil.initializeAllAnalysisData();
		}
		
		blia.prepareAnalysisData();
		blia.analyze();
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time of BLIA for evaluation: %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		
		
		String productName = "swt-3.1";
		String algorithmName = Evaluator.ALG_BLIA;
		String algorithmDescription = "[BLIA] alpha: " + alpha;
		Evaluator evaluator1 = new Evaluator(productName, algorithmName, algorithmDescription);
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

}
