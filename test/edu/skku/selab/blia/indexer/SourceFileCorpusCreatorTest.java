/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.indexer;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blia.Property;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileCorpusCreatorTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String osName = System.getProperty("os.name");
		
		if (osName.equals("Mac OS X")) {
			String bugFilePath = "./test_data/SWTBugRepository.xml";
			String sourceCodeDir = "../swt-3.1/src";
			String workDir = "./tmp";
			float alpha = 0.2f;
			float beta = 0.5f;
			String outputFile = "./tmp/test_output.txt";
			
			Property.createInstance(bugFilePath, sourceCodeDir, workDir, alpha, beta, outputFile);		
		} else {
			String bugFilePath = ".\\test_data\\SWTBugRepository.xml";
			String sourceCodeDir = "..\\swt-3.1\\src";
			String workDir = ".\\tmp";
			float alpha = 0.2f;
			float beta = 0.5f;
			String outputFile = ".\\tmp\\test_output.txt";
			
			Property.createInstance(bugFilePath, sourceCodeDir, workDir, alpha, beta, outputFile);
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
	public void verifySourceFileCorpusCreator() throws Exception {
		SourceFileCorpusCreator sourceFileCorpusCreator = new SourceFileCorpusCreator();
		sourceFileCorpusCreator.create();
	}

}
