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
import java.util.HashSet;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blia.db.AnalysisValue;
import edu.skku.selab.blia.db.SimilarBugInfo;
import edu.skku.selab.blia.indexer.Bug;
import edu.skku.selab.blia.indexer.SourceFile;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugDAOTest {

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
	public void verifyBugDAO() throws Exception {
		String bugID1 = "BLIA-101";
		String bugID2 = "BLIA-102";
		String productName = "BLIA";
		String fixedDateString1 = "2004-12-01 17:40:00";
		String fixedDateString2 = "2014-03-27 07:12:00";
		String corpusSet1 = "acc contain constant us defin access";
		String corpusSet2 = "element listen event event result";
		String stackTraces1 = "edu.skku.selab.blia.java; edu.skku.selab.blia.java; ";
		String stackTraces2 = "org.blia.java; org.blia.java; ";
		
		Bug bug1 = new Bug();
		bug1.setID(bugID1);
		bug1.setProductName(productName);
		bug1.setFixedDateString(fixedDateString1);
		bug1.setCorpuses(corpusSet1);
		bug1.setStackTraces(stackTraces1);
		Bug bug2 = new Bug();
		bug2.setID(bugID2);
		bug2.setProductName(productName);
		bug2.setFixedDateString(fixedDateString2);
		bug2.setCorpuses(corpusSet2);
		bug2.setStackTraces(stackTraces2);		
				
		BugDAO bugDAO = new BugDAO();
		
		bugDAO.deleteAllBugs();
		assertEquals("Insertion failed!", 1, bugDAO.insertBug(bug1));
		assertEquals("Insertion failed!", 1, bugDAO.insertBug(bug2));
		
		HashMap<String, Bug> bugs = bugDAO.getBugs();
		
		Bug foundBug1 = bugs.get(bugID1);
		Bug foundBug2 = bugs.get(bugID2);
		System.out.printf("BugID: %s, prodName: %s, fixedDate: %s, corpus: %s, strace: %s\n",
				foundBug1.getID(), foundBug1.getProductName(), foundBug1.getFixedDateString(), foundBug1.getCorpuses(), foundBug1.getStackTraces());
		System.out.printf("BugID: %s, prodName: %s, fixedDate: %s, corpus: %s, strace: %s\n",
				foundBug2.getID(), foundBug2.getProductName(), foundBug2.getFixedDateString(), foundBug2.getCorpuses(), foundBug2.getStackTraces());
		
		Bug foundBug = bugDAO.getBug(bugID1, productName);
		System.out.printf("BugID: %s, prodName: %s, fixedDate: %s, corpus: %s, strace: %s\n",
				foundBug.getID(), foundBug.getProductName(), foundBug.getFixedDateString(), foundBug.getCorpuses(), foundBug.getStackTraces());
		
		
		bugDAO.deleteAllCorpuses();
		String corpus1 = "acc";
		String corpus2 = "element";
		bugDAO.insertCorpus(corpus1, productName);
		bugDAO.insertCorpus(corpus2, productName);
		
		HashMap<String, Integer> corpuses = bugDAO.getCorpuses(productName);
		System.out.printf("Corpus: %s, Corpus ID: %d\n", corpus1, corpuses.get(corpus1));
		System.out.printf("Corpus: %s, Corpus ID: %d\n", corpus2, corpuses.get(corpus2));
		
		bugDAO.deleteAllAnalysisValues();
		double vector = 0.4219;
		AnalysisValue analysisValue = new AnalysisValue(bugID1, productName, corpus1, vector);
		bugDAO.insertBugAnalysisValue(analysisValue);
		
		AnalysisValue returnValue = bugDAO.getBugAnalysisValue(bugID1, productName, corpus1);
		System.out.printf("Bug ID: %s, Produce name: %s, Coupus: %s\n", 
				returnValue.getName(), returnValue.getProductName(), returnValue.getCorpus());
		System.out.printf("Vector: %f\n", returnValue.getVector());

		// Test BUB_FIX_INFO
		bugDAO.deleteAllBugFixedInfo();
		String fileName1 = "test_10.java";
		String fileName2 = "test_11.java";
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
		double lengthScore1 = 0.32;
		double lengthScore2 = 0.1238;
		sourceFileDAO.insertCorpusSet(fileName1, productName, version1, corpusSet1, lengthScore1);
		sourceFileDAO.insertCorpusSet(fileName2, productName, version1, corpusSet2, lengthScore2);
		
		HashMap<String, String> corpusSets = sourceFileDAO.getCorpusSets(productName, version1);
		System.out.printf("File name: %s, CoupusSet: %s\n", fileName1, corpusSets.get(fileName1));
		
		HashMap<String, Double> lengthScores = sourceFileDAO.getLengthScores(productName, version1);
		System.out.printf("File name: %s, LengthScore: %f\n", fileName1, lengthScores.get(fileName1));
		
		bugDAO.insertBugFixedFileInfo(bugID1, fileName1, version1, productName);
		bugDAO.insertBugFixedFileInfo(bugID1, fileName2, version1, productName);
		
		HashSet<SourceFile> fixedFiles = bugDAO.getFixedFiles(bugID1);
		
		Iterator<SourceFile> iter1 = fixedFiles.iterator();
		
		while (iter1.hasNext()) {
			SourceFile sourceFile = iter1.next();
			System.out.printf("Bug ID: %s, Files: %s, Version: %s\n", bugID1, sourceFile.getName(), sourceFile.getVersion());			
		}
		
		bugDAO.deleteAllSimilarBugInfo();
		bugDAO.insertSimilarBugInfo(bugID1, bugID2, 0.82);
		String bugID3 = "BLIA-103";
		bugDAO.insertSimilarBugInfo(bugID1, bugID3, 0.24);
		HashSet<SimilarBugInfo> similarBugInfos = bugDAO.getSimilarBugInfos(bugID1);
		
		Iterator<SimilarBugInfo> iter2 = similarBugInfos.iterator();
		
		while (iter2.hasNext()) {
			SimilarBugInfo similarBugInfo = iter2.next();
			System.out.printf("Bug ID: %s, Similar Bug ID: %s, Similarity Score: %f\n", bugID1, similarBugInfo.getSimilarBugID(), similarBugInfo.getSimilarityScore());			
		}
		
		
		bugDAO.closeConnection();
	}

}
