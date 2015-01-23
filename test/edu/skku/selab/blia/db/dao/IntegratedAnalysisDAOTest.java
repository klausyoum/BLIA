/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.db.dao;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blia.db.IntegratedAnalysisValue;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class IntegratedAnalysisDAOTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String fileName1 = "test_10.java";
		String fileName2 = "test_11.java";
		String productName = "BLIA";
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		
		sourceFileDAO.deleteAllSourceFiles();
		assertEquals("Insertion failed!", 1, sourceFileDAO.insertSourceFile(fileName1, productName));
		assertEquals("Insertion failed!", 1, sourceFileDAO.insertSourceFile(fileName2, productName));
		
		HashMap<String, Integer> fileInfo = sourceFileDAO.getSourceFiles(productName);
		
		System.out.printf("File name: %s, file ID: %d\n", fileName1, fileInfo.get(fileName1));
		System.out.printf("File name: %s, file ID: %d\n", fileName2, fileInfo.get(fileName2));
		
		sourceFileDAO.deleteAllVersions();
		String version1 = "v0.1";
		String releaseDate1 = "2004-10-18 17:40:00";
		String version2 = "v0.2";
		String releaseDate2 = "2014-02-12 07:12:00";
		sourceFileDAO.insertVersion(version1, releaseDate1);
		sourceFileDAO.insertVersion(version2, releaseDate2);
		
		HashMap<String, Date> versions = sourceFileDAO.getVersions();
		System.out.println("Version: " + version1 + " Date: " + versions.get(version1).toString());
		System.out.println("Version: " + version2 + " Date: " + versions.get(version2).toString());
		
		String corpusSet1 = "acc contain constant us defin access";
		String corpusSet2 = "element listen event event result";
		int totalCorpusCount1 = 5;
		int totalCorpusCount2 = 34;
		double lengthScore1 = 0.32;
		double lengthScore2 = 0.1238;
		sourceFileDAO.insertCorpusSet(fileName1, productName, version1, corpusSet1, totalCorpusCount1, lengthScore1);
		sourceFileDAO.insertCorpusSet(fileName1, productName, version2, corpusSet2, totalCorpusCount2, lengthScore2);
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
	public void verifyIntegratedAnalaysisDAO() throws Exception {
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		
		integratedAnalysisDAO.deleteAllIntegratedAnalysisInfos();
		String bugID1 = "BLIA-101";
		String productName = "BLIA";
		String fileName1 = "test_10.java";
		double vsmScore = 0.321;
		double similarityScore = 0.6281;
		double bugLocatorScore = 0.5833;
		double stackTraceScore = 0.8321;
		double bliaScore = 0.7329;
		final double delta = 0.00001;
		String version1 = "v0.1";
//		String releaseDate1 = "2004-10-18 17:40:00";
//		String version2 = "v0.2";
//		String releaseDate2 = "2014-02-12 07:12:00";


		IntegratedAnalysisValue integratedAnalysisValue = new IntegratedAnalysisValue();
		integratedAnalysisValue.setBugID(bugID1);
		integratedAnalysisValue.setFileName(fileName1);
		integratedAnalysisValue.setVersion(version1);
		integratedAnalysisValue.setProductName(productName);
		integratedAnalysisValue.setVsmScore(vsmScore);
		integratedAnalysisValue.setSimilarityScore(similarityScore);
		integratedAnalysisValue.setBugLocatorScore(bugLocatorScore);
		integratedAnalysisValue.setStackTraceScore(stackTraceScore);
		integratedAnalysisValue.setBliaScore(bliaScore);
		
		integratedAnalysisDAO.insertIntegratedAnalysisVaule(integratedAnalysisValue);
		
		IntegratedAnalysisValue returnValue = integratedAnalysisDAO.getIntegratedAnalysisValue(bugID1);
		assertEquals("Bug ID is NOT same!", bugID1, returnValue.getBugID());
		assertEquals("File Name is NOT same!", fileName1, returnValue.getFileName());
		assertEquals("ProductName is NOT same!", productName, returnValue.getProductName());
		
		assertEquals("VSM Score is NOT same!", vsmScore, returnValue.getVsmScore(), delta);
		assertEquals("similarityScore is NOT same!", similarityScore, returnValue.getSimilarityScore(), delta);
		assertEquals("bugLocatorScore is NOT same!", bugLocatorScore, returnValue.getBugLocatorScore(), delta);
		assertEquals("stackTraceScore is NOT same!", stackTraceScore, returnValue.getStackTraceScore(), delta);
		assertEquals("bliaScore is NOT same!", bliaScore, returnValue.getBliaScore(), delta);
		
		integratedAnalysisDAO.closeConnection();
	}

}
