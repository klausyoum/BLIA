/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db.dao;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blp.common.Bug;
import edu.skku.selab.blp.common.SourceFile;
import edu.skku.selab.blp.db.AnalysisValue;
import edu.skku.selab.blp.db.SimilarBugInfo;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.SourceFileDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugDAOTest {
	private String bugID1 = "BLIA-101";
	private String bugID2 = "BLIA-102";
	private String bugID3 = "BLIA-103";
	private String productName = "BLIA";
	private String fixedDateString1 = "2004-12-01 17:40:00";
	private String fixedDateString2 = "2014-03-27 07:12:00";
	private String fixedDateString3 = "2015-01-27 02:48:00";
	private String corpusSet1 = "acc contain constant us defin access";
	private String corpusSet2 = "element listen event event result";
	private String corpusSet3 = "event blia result";
	private String stackTrace1 = "edu.skku.selab.blia";
	private String stackTrace2 = "edu.skku.selab.blp";
	private String stackTrace3 = "org.blia";
	private String stackTrace4 = "org.blp";
	private String corpus1 = "acc";
	private String corpus2 = "element";
	private String version = "v1.0";
	
	private int termCount = 10;
	private int idc = 32;
	private double tf = 0.53;
	private double idf = 0.259;
	private double vector = 0.4219;
	private double delta = 0.00005;
	
	private String fileName1 = "test_10.java";
	private String fileName2 = "test_11.java";
	
	private double similarityScore1 = 0.82;
	private double similarityScore2 = 0.24;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DbUtil dbUtil = new DbUtil();
		dbUtil.initializeAllAnalysisData();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		BaseDAO.closeConnection();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		Bug bug1 = new Bug();
		bug1.setID(bugID1);
		bug1.setProductName(productName);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date fixedDate1 = simpleDateFormat.parse(fixedDateString1);
		bug1.setFixedDate(fixedDate1);
		bug1.setCorpuses(corpusSet1);
		ArrayList<String> stackTraces1 = new ArrayList<String>();
		stackTraces1.add(stackTrace1);
		stackTraces1.add(stackTrace2);
		bug1.setStackTraceClasses(stackTraces1);
		bug1.setVersion(version);
		
		Bug bug2 = new Bug();
		bug2.setID(bugID2);
		bug2.setProductName(productName);
		bug2.setFixedDate(fixedDateString2);
		bug2.setCorpuses(corpusSet2);
		ArrayList<String> stackTraces2 = new ArrayList<String>();
		stackTraces2.add(stackTrace3);
		stackTraces2.add(stackTrace4);
		bug2.setStackTraceClasses(stackTraces2);
		bug2.setVersion(version);
		
		Bug bug3 = new Bug();
		bug3.setID(bugID3);
		bug3.setProductName(productName);
		bug3.setFixedDate(fixedDateString3);
		bug3.setCorpuses(corpusSet3);
		bug3.setVersion(version);
		
		BugDAO bugDAO = new BugDAO();
		
		bugDAO.deleteAllBugs();
		assertNotEquals("Bug insertion failed!", BaseDAO.INVALID, bugDAO.insertBug(bug1));
		assertNotEquals("Bug insertion failed!", BaseDAO.INVALID, bugDAO.insertBug(bug2));
		assertNotEquals("Bug insertion failed!", BaseDAO.INVALID, bugDAO.insertBug(bug3));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void verifyGetBug() throws Exception {
		BugDAO bugDAO = new BugDAO();
		HashMap<String, Bug> bugs = bugDAO.getBugs();
		
		Bug foundBug1 = bugs.get(bugID1);
		Bug foundBug2 = bugs.get(bugID2);
		assertEquals("bugID1 is wrong.", bugID1, foundBug1.getID());
		assertEquals("productName is wrong.", productName, foundBug1.getProductName());
		assertEquals("fixedDateString1 is wrong.", fixedDateString1, foundBug1.getFixedDateString());
		assertEquals("corpusSet1 is wrong.", corpusSet1, foundBug1.getCorpuses());
		assertEquals("stackTraces1 is wrong.", stackTrace1, foundBug1.getStackTraceClasses().get(0));
		assertEquals("stackTraces2 is wrong.", stackTrace2, foundBug1.getStackTraceClasses().get(1));
		assertEquals("version is wrong.", version, foundBug1.getVersion());

		assertEquals("bugID2 is wrong.", bugID2, foundBug2.getID());
		assertEquals("productName is wrong.", productName, foundBug2.getProductName());
		assertEquals("fixedDateString2 is wrong.", fixedDateString2, foundBug2.getFixedDateString());
		assertEquals("corpusSet2 is wrong.", corpusSet2, foundBug2.getCorpuses());
		assertEquals("stackTrace3 is wrong.", stackTrace3, foundBug2.getStackTraceClasses().get(0));
		assertEquals("stackTrace4 is wrong.", stackTrace4, foundBug2.getStackTraceClasses().get(1));
		assertEquals("version is wrong.", version, foundBug2.getVersion());
		
		Bug foundBug = bugDAO.getBug(bugID1, productName);
		assertEquals("bugID1 is wrong.", bugID1, foundBug.getID());
		assertEquals("productName is wrong.", productName, foundBug.getProductName());
		assertEquals("fixedDateString1 is wrong.", fixedDateString1, foundBug.getFixedDateString());
		assertEquals("corpusSet1 is wrong.", corpusSet1, foundBug.getCorpuses());
		assertEquals("stackTraces1 is wrong.", stackTrace1, foundBug.getStackTraceClasses().get(0));
		assertEquals("stackTraces2 is wrong.", stackTrace2, foundBug.getStackTraceClasses().get(1));
		assertEquals("version is wrong.", version, foundBug.getVersion());
		
	}

	@Test
	public void verifyGetBugSfAnalysisValue() throws Exception {
		BugDAO bugDAO = new BugDAO();

		bugDAO.deleteAllCorpuses();
		assertNotEquals("Corpus insertion failed!", BaseDAO.INVALID, bugDAO.insertCorpus(corpus1, productName));
		assertNotEquals("Corpus insertion failed!", BaseDAO.INVALID, bugDAO.insertCorpus(corpus2, productName));
		
		HashMap<String, Integer> corpuses = bugDAO.getCorpuses(productName);
		assertNotNull("Can't find corpus1.", corpuses.get(corpus1));
		assertNotNull("Can't find corpus2.", corpuses.get(corpus2));
		
		// preparation phase
		bugDAO.deleteAllBugSfAnalysisValues();
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		sourceFileDAO.deleteAllCorpuses();
		sourceFileDAO.insertCorpus(corpus1, productName);
		sourceFileDAO.insertCorpus(corpus2, productName);
		
		AnalysisValue analysisValue = new AnalysisValue(bugID1, productName, corpus1, termCount, idc, tf, idf, vector);
		assertNotEquals("BugSfAnalysisValue insertion failed!", BaseDAO.INVALID, bugDAO.insertBugSfAnalysisValue(analysisValue));
		
		AnalysisValue returnValue = bugDAO.getBugSfAnalysisValue(bugID1, productName, corpus1);
		assertEquals("Bug ID of AnalysisValue is wrong.", bugID1, returnValue.getName());
		assertEquals("productName of AnalysisValue is wrong.", productName, returnValue.getProductName());
		assertEquals("corpus1 of AnalysisValue is wrong.", corpus1, returnValue.getCorpus());
		assertEquals("termCount of AnalysisValue is wrong.", termCount, returnValue.getTermCount());
		assertEquals("idc of AnalysisValue is wrong.", idc, returnValue.getInvDocCount());
		assertEquals("tf of AnalysisValue is wrong.", tf, returnValue.getTf(), delta);
		assertEquals("idf of AnalysisValue is wrong.", idf, returnValue.getIdf(), delta);
		assertEquals("vector of AnalysisValue is wrong.", vector, returnValue.getVector(), delta);
	}

	@Test
	public void verifyGetBugAnalysisValue() throws Exception {
		BugDAO bugDAO = new BugDAO();

		bugDAO.deleteAllBugAnalysisValues();
		AnalysisValue analysisValue = new AnalysisValue(bugID1, productName, corpus1, termCount, idc, tf, idf, vector);
		assertNotEquals("BugAnalysisValue insertion failed!", BaseDAO.INVALID, bugDAO.insertBugAnalysisValue(analysisValue));
		
		AnalysisValue returnValue = bugDAO.getBugAnalysisValue(bugID1, productName, corpus1);
		assertEquals("Bug ID of AnalysisValue is wrong.", bugID1, returnValue.getName());
		assertEquals("productName of AnalysisValue is wrong.", productName, returnValue.getProductName());
		assertEquals("corpus1 of AnalysisValue is wrong.", corpus1, returnValue.getCorpus());
		assertEquals("vector of AnalysisValue is wrong.", vector, returnValue.getVector(), delta);
	}

	@Test
	public void verifyGetFixedFiles() throws Exception {
		BugDAO bugDAO = new BugDAO();
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		
		// preparation phase
		sourceFileDAO.deleteAllSourceFiles();
		assertNotEquals("fileName1 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertSourceFile(fileName1, productName));
		assertNotEquals("fileName2 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertSourceFile(fileName2, productName));

		sourceFileDAO.deleteAllVersions();
		String version1 = "v0.1";
		String releaseDate1 = "2004-10-18 17:40:00";
		String version2 = "v0.2";
		String releaseDate2 = "2014-02-12 07:12:00";
		assertNotEquals("version1 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertVersion(version1, releaseDate1));
		assertNotEquals("version2 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertVersion(version2, releaseDate1));
		
		int totalCorpusCount1 = 5;
		int totalCorpusCount2 = 34;
		double lengthScore1 = 0.32;
		double lengthScore2 = 0.1238;
		assertNotEquals("fileName1's corpus insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertCorpusSet(fileName1, productName, version1, corpusSet1, totalCorpusCount1, lengthScore1));
		assertNotEquals("fileName2's corpus insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertCorpusSet(fileName2, productName, version1, corpusSet2, totalCorpusCount2, lengthScore2));
		
		assertNotEquals("BugFixedFileInfo insertion failed!", BaseDAO.INVALID, bugDAO.insertBugFixedFileInfo(bugID1, fileName1, version1, productName));
		assertNotEquals("BugFixedFileInfo insertion failed!", BaseDAO.INVALID, bugDAO.insertBugFixedFileInfo(bugID1, fileName2, version1, productName));
		
		HashSet<SourceFile> fixedFiles = bugDAO.getFixedFiles(bugID1);
		assertEquals("Fixedfiles count is wrong.", 2, fixedFiles.size());
		Iterator<SourceFile> iter1 = fixedFiles.iterator();
		
		SourceFile sourceFile = iter1.next();
		if (fileName1.equalsIgnoreCase(sourceFile.getName())) {
			assertEquals("version1 is wrong.", version1, sourceFile.getVersion());
		} else if (fileName2.equalsIgnoreCase(sourceFile.getName())) {
			assertEquals("version1 is wrong.", version1, sourceFile.getVersion());
		} else {
			fail("SourceFile is wrong.");
		}
		
		sourceFile = iter1.next();
		if (fileName1.equalsIgnoreCase(sourceFile.getName())) {
			assertEquals("version1 is wrong.", version1, sourceFile.getVersion());
		} else if (fileName2.equalsIgnoreCase(sourceFile.getName())) {
			assertEquals("version1 is wrong.", version1, sourceFile.getVersion());
		} else {
			fail("SourceFile is wrong.");
		}
	}
	
	@Test
	public void verifyGetSimilarBugInfos() throws Exception {
		BugDAO bugDAO = new BugDAO();
		
		bugDAO.deleteAllSimilarBugInfo();
		assertNotEquals("BugFixedFileInfo insertion failed!", BaseDAO.INVALID, bugDAO.insertSimilarBugInfo(bugID1, bugID2, similarityScore1));
		assertNotEquals("BugFixedFileInfo insertion failed!", BaseDAO.INVALID, bugDAO.insertSimilarBugInfo(bugID1, bugID3, similarityScore2));
		HashSet<SimilarBugInfo> similarBugInfos = bugDAO.getSimilarBugInfos(bugID1);
		assertEquals("SimilarBugInfos count is wrong.", 2, similarBugInfos.size());
		Iterator<SimilarBugInfo> iter2 = similarBugInfos.iterator();
		
		SimilarBugInfo similarBugInfo = iter2.next();
		if (bugID2.equalsIgnoreCase(similarBugInfo.getSimilarBugID())) {
			assertEquals("similarityScore1 is wrong.", similarityScore1, similarBugInfo.getSimilarityScore(), delta);			
		} else if (bugID3.equalsIgnoreCase(similarBugInfo.getSimilarBugID())) {
			assertEquals("similarityScore2 is wrong.", similarityScore2, similarBugInfo.getSimilarityScore(), delta);
		} else {
			fail("SimilarBugInfo is wrong.");
		}
		
		similarBugInfo = iter2.next();
		if (bugID2.equalsIgnoreCase(similarBugInfo.getSimilarBugID())) {
			assertEquals("similarityScore1 is wrong.", similarityScore1, similarBugInfo.getSimilarityScore(), delta);			
		} else if (bugID3.equalsIgnoreCase(similarBugInfo.getSimilarBugID())) {
			assertEquals("similarityScore2 is wrong.", similarityScore2, similarBugInfo.getSimilarityScore(), delta);
		} else {
			fail("SimilarBugInfo is wrong.");
		}
	}
	
	@Test
	public void verifyGetStackTraceClasses() throws Exception {
		BugDAO bugDAO = new BugDAO();

		bugDAO.deleteAllStackTraceClasses();
		String className1 = "edu.skku.blp.blia.className1";
		String className2 = "edu.skku.blp.blia.className2";
		assertNotEquals("StackTraceClass insertion failed!", BaseDAO.INVALID, bugDAO.insertStackTraceClass(bugID3, className1));
		assertNotEquals("StackTraceClass insertion failed!", BaseDAO.INVALID, bugDAO.insertStackTraceClass(bugID3, className2));
		
		ArrayList<String> stackTraceClasses = bugDAO.getStackTraceClasses(bugID3);
		assertEquals("stackTraceClasses count is wrong.", 2, stackTraceClasses.size());
		
		assertEquals("className1 is wrong.", className1, stackTraceClasses.get(0));
		assertEquals("className2 is wrong.", className2, stackTraceClasses.get(1));		
	}

}
