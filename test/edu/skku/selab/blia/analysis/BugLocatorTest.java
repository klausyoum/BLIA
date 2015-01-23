/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.analysis;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blia.Property;
import edu.skku.selab.blia.anlaysis.BugLocator;
import edu.skku.selab.blia.anlaysis.BugRepoAnalyzer;
import edu.skku.selab.blia.anlaysis.SourceFileAnalyzer;
import edu.skku.selab.blia.indexer.BugCorpusCreator;
import edu.skku.selab.blia.indexer.BugVectorCreator;
import edu.skku.selab.blia.indexer.SourceFileCorpusCreator;
import edu.skku.selab.blia.indexer.SourceFileIndexer;
import edu.skku.selab.blia.indexer.SourceFileVectorCreator;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugLocatorTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String osName = System.getProperty("os.name");
		String productName = "swt-3.1";
		float alpha = 0.2f;
		float beta = 0.5f;
		
		if (osName.equals("Mac OS X")) {
			String bugFilePath = "./test_data/SWTBugRepository.xml";
			String sourceCodeDir = "../swt-3.1/src";
			String workDir = "./tmp";
			String outputFile = "./tmp/test_output.txt";
			
			Property.createInstance(productName, bugFilePath, sourceCodeDir, workDir, alpha, beta, outputFile);		
		} else {
			String bugFilePath = ".\\test_data\\SWTBugRepository.xml";
			String sourceCodeDir = "..\\swt-3.1\\src";
			String workDir = ".\\tmp";
			String outputFile = ".\\tmp\\test_output.txt";
			
			Property.createInstance(productName, bugFilePath, sourceCodeDir, workDir, alpha, beta, outputFile);
		}
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

	@Test
	public void verifyBugLocator() throws Exception {
		long startTime = System.currentTimeMillis();

		SourceFileCorpusCreator sourceFileCorpusCreator = new SourceFileCorpusCreator();
		sourceFileCorpusCreator.create();
		
		SourceFileIndexer sourceFileIndexer = new SourceFileIndexer();
		sourceFileIndexer.createIndex();
		sourceFileIndexer.computeLengthScore();
		
		SourceFileVectorCreator sourceFileVectorCreator = new SourceFileVectorCreator();
		sourceFileVectorCreator.create();

		// Create SordtedID.txt
		BugCorpusCreator bugCorpusCreator = new BugCorpusCreator();
		bugCorpusCreator.create();
		
		SourceFileAnalyzer sourceFileAnalyzer = new SourceFileAnalyzer();
		sourceFileAnalyzer.analyze();

		BugVectorCreator bugVectorCreator = new BugVectorCreator();
		bugVectorCreator.create();
		
		BugRepoAnalyzer bugRepoAnalyzer = new BugRepoAnalyzer();
		bugRepoAnalyzer.analyze();
		
		BugLocator bugLocator = new BugLocator();
		bugLocator.analyze();
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time of BugLocator: %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		
	}
}
