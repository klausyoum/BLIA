/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.buglocator.analysis;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.buglocator.analysis.BugLocatorWithFile;
import edu.skku.selab.blp.buglocator.analysis.BugRepoAnalyzerWithFile;
import edu.skku.selab.blp.buglocator.analysis.SourceFileAnalyzerWithFile;
import edu.skku.selab.blp.buglocator.indexer.BugCorpusCreatorWithFile;
import edu.skku.selab.blp.buglocator.indexer.BugVectorCreatorWithFile;
import edu.skku.selab.blp.buglocator.indexer.SourceFileCorpusCreatorWithFile;
import edu.skku.selab.blp.buglocator.indexer.SourceFileIndexerWithFile;
import edu.skku.selab.blp.buglocator.indexer.SourceFileVectorCreatorWithFile;
import edu.skku.selab.blp.test.utils.TestConfiguration;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugLocatorWithFileTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String productName = "swt";
		String algorithmName = "BugLocatorWithFile";
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
	public void verifyBugLocatorWithFile() throws Exception {
		long startTime = System.currentTimeMillis();

		SourceFileCorpusCreatorWithFile sourceFileCorpusCreator = new SourceFileCorpusCreatorWithFile();
		sourceFileCorpusCreator.create();
		
		SourceFileIndexerWithFile sourceFileIndexer = new SourceFileIndexerWithFile();
		sourceFileIndexer.createIndex();
		sourceFileIndexer.computeLengthScore();
		
		SourceFileVectorCreatorWithFile sourceFileVectorCreator = new SourceFileVectorCreatorWithFile();
		sourceFileVectorCreator.create();

		// Create SordtedID.txt
		BugCorpusCreatorWithFile bugCorpusCreatorWithFile = new BugCorpusCreatorWithFile();
		bugCorpusCreatorWithFile.create();
		
		SourceFileAnalyzerWithFile sourceFileAnalyzer = new SourceFileAnalyzerWithFile();
		sourceFileAnalyzer.analyze();

		BugVectorCreatorWithFile bugVectorCreator = new BugVectorCreatorWithFile();
		bugVectorCreator.create();
		
		BugRepoAnalyzerWithFile bugRepoAnalyzer = new BugRepoAnalyzerWithFile();
		bugRepoAnalyzer.analyze();
		
		BugLocatorWithFile bugLocator = new BugLocatorWithFile();
		bugLocator.analyze();
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time of BugLocatorWithFile: %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		
	}
}
