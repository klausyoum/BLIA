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

import edu.skku.selab.blia.db.AnalysisValue;
import edu.skku.selab.blia.db.dao.SourceFileDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileDAOTest {

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

	@Test
	public void verifySourdeFileDAO() throws Exception {
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
		
		sourceFileDAO.deleteAllCorpusSets();
		String corpusSet1 = "acc contain constant us defin access";
		String corpusSet2 = "element listen event event result";
		double lengthScore1 = 0.32;
		double lengthScore2 = 0.1238;
		sourceFileDAO.insertCorpusSet(fileName1, productName, version1, corpusSet1, lengthScore1);
		sourceFileDAO.insertCorpusSet(fileName1, productName, version2, corpusSet2, lengthScore2);
		
		HashMap<String, String> corpusSets = sourceFileDAO.getCorpusSets(productName, version1);
		System.out.printf("File name: %s, CoupusSet: %s\n", fileName1, corpusSets.get(fileName1));
		
		HashMap<String, Double> lengthScores = sourceFileDAO.getLengthScores(productName, version1);
		System.out.printf("File name: %s, LengthScore: %f\n", fileName1, lengthScores.get(fileName1));
		
		sourceFileDAO.deleteAllCorpuses();
		String corpus1 = "acc";
		String corpus2 = "element";
		sourceFileDAO.insertCorpus(corpus1, productName);
		sourceFileDAO.insertCorpus(corpus2, productName);
		
		HashMap<String, Integer> corpuses = sourceFileDAO.getCorpuses(productName);
		System.out.printf("Corpus: %s, Corpus ID: %d\n", corpus1, corpuses.get(corpus1));
		System.out.printf("Corpus: %s, Corpus ID: %d\n", corpus2, corpuses.get(corpus2));
		
		sourceFileDAO.deleteAllAnalysisValues();
		AnalysisValue analysisValue = new AnalysisValue(fileName1, productName, version1,
				corpus1, 5, 20, 0.23, 0.42, 0.78);
		sourceFileDAO.insertSourceFileAnalysisValue(analysisValue);
		
		AnalysisValue returnValue = sourceFileDAO.getSourceFileAnalysisValue(fileName1, productName, version1, corpus1);
		System.out.printf("File name: %s, Version: %s, Produce name: %s, Coupus: %s\n", 
				returnValue.getName(), returnValue.getVersion(), returnValue.getProductName(), returnValue.getCorpus());
		System.out.printf("Term count: %d, Inv doc count: %d, Tf: %f, Idf: %f, Vector: %f\n",
				returnValue.getTermCount(), returnValue.getInvDocCount(), returnValue.getTf(), returnValue.getIdf(),
				returnValue.getVector());
		
		sourceFileDAO.closeConnection();
	}

}
