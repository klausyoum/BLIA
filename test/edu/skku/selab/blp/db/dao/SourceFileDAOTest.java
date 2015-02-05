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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blp.db.AnalysisValue;
import edu.skku.selab.blp.db.dao.SourceFileDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileDAOTest {
	private String fileName1 = "test_10.java";
	private String fileName2 = "test_11.java";
	private String productName = "BLIA";
	private String version1 = "v0.1";
	private String releaseDate1 = "2004-10-18 17:40:00";
	private String version2 = "v0.2";
	private String releaseDate2 = "2014-02-12 07:12:00";
	private String corpus1 = "acc";
	private String corpus2 = "element";
	private double delta = 0.00001;


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
		DbUtil dbUtil = new DbUtil();
		dbUtil.initializeAllData();
		
		prepareTestingData();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		BaseDAO.closeConnection();
	}
	
	private void prepareTestingData() throws Exception {
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		
		sourceFileDAO.deleteAllSourceFiles();
		assertNotEquals("fileName1 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertSourceFile(fileName1, productName));
		assertNotEquals("fileName2 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertSourceFile(fileName2, productName));
		
		HashMap<String, Integer> fileInfo = sourceFileDAO.getSourceFiles(productName);
		assertEquals("fileInfo size is wrong.", 2, fileInfo.size());
		assertNotNull("fileName1 can't be found.", fileInfo.get(fileName1));
		assertNotNull("fileName2 can't be found.", fileInfo.get(fileName2));
		
		sourceFileDAO.deleteAllVersions();
		assertNotEquals("version1 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertVersion(version1, releaseDate1));
		assertNotEquals("version2 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertVersion(version2, releaseDate2));

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		HashMap<String, Date> versions = sourceFileDAO.getVersions();
		assertEquals("versions size is wrong.", 2, versions.size());
		assertEquals("releaseDate1 is NOT same!", releaseDate1, simpleDateFormat.format(versions.get(version1)));
		assertEquals("releaseDate2 is NOT same!", releaseDate2, simpleDateFormat.format(versions.get(version2)));
		
		sourceFileDAO.deleteAllCorpusSets();
		String corpusSet1 = "acc contain constant us defin access";
		String corpusSet2 = "element listen event event result";
		String corpusSet3 = "blia constant defin";
		int totalCorpusCount1 = 5;
		int totalCorpusCount2 = 34;
		int totalCorpusCount3 = 867;
		double lengthScore1 = 0.32;
		double lengthScore2 = 0.1238;
		double lengthScore3 = 0.738;
		assertNotEquals("fileName1's corpusSet insertion failed!", BaseDAO.INVALID, 
				sourceFileDAO.insertCorpusSet(fileName1, productName, version1, corpusSet1, totalCorpusCount1, lengthScore1));
		assertNotEquals("fileName1's corpusSet insertion failed!", BaseDAO.INVALID,
				sourceFileDAO.insertCorpusSet(fileName1, productName, version2, corpusSet2, totalCorpusCount2, lengthScore2));
		assertNotEquals("fileName2's corpusSet insertion failed!", BaseDAO.INVALID,
				sourceFileDAO.insertCorpusSet(fileName2, productName, version1, corpusSet3, totalCorpusCount3, lengthScore3));
		
		
		assertEquals("Source file count is WRONG!", 2, sourceFileDAO.getSourceFileCount(productName, version1));
		assertEquals("Source file count is WRONG!", 1, sourceFileDAO.getSourceFileCount(productName, version2));
		
		HashMap<String, String> corpusSets = sourceFileDAO.getCorpusSets(productName, version1);
		assertEquals("corpusSets size is wrong.", 2, corpusSets.size());
		assertEquals("corpusSet1 is NOT same!", corpusSet1, corpusSets.get(fileName1));
		assertEquals("corpusSet1 is NOT same!", corpusSet3, corpusSets.get(fileName2));
		
		HashMap<String, Double> lengthScores = sourceFileDAO.getLengthScores(productName, version1);
		assertEquals("lengthScores size is wrong.", 2, lengthScores.size());
		assertEquals("lengthScore1 is NOT same!", lengthScore1, lengthScores.get(fileName1), delta);
		assertEquals("lengthScore1 is NOT same!", lengthScore3, lengthScores.get(fileName2), delta);

		sourceFileDAO.deleteAllCorpuses();
		assertNotEquals("corpus1 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertCorpus(corpus1, productName));
		assertNotEquals("corpus2 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertCorpus(corpus2, productName));
		
		HashMap<String, Integer> corpuses = sourceFileDAO.getCorpuses(productName);
		assertEquals("corpuses size is wrong.", 2, corpuses.size());
		assertNotNull("corpus1 can't be found.", corpuses.get(corpus1));
		assertNotNull("corpus2 can't be found.", corpuses.get(corpus2));
	}

	@Test
	public void verifyGetSourceFileAnalysisValue() throws Exception {
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		
		sourceFileDAO.deleteAllAnalysisValues();
		int termCount = 5;
		int idvDocCount = 20;
		double tf = 0.23;
		double idf = 0.42;
		double vector = 0.78;
		AnalysisValue analysisValue1 = new AnalysisValue(fileName1, productName, version1,
				corpus1, termCount, idvDocCount, tf, idf, vector);
		assertNotEquals("analysisValue1 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertSourceFileAnalysisValue(analysisValue1));
		
		AnalysisValue returnValue = sourceFileDAO.getSourceFileAnalysisValue(fileName1, productName, version1, corpus1);
		assertEquals("fileName1 is wrong.", fileName1, returnValue.getName());
		assertEquals("productName is wrong.", productName, returnValue.getProductName());
		assertEquals("version1 is wrong.", version1, returnValue.getVersion());
		assertEquals("corpus1 is wrong.", corpus1, returnValue.getCorpus());
		assertEquals("termCount is wrong.", termCount, returnValue.getTermCount());
		assertEquals("idvDocCount is wrong.", idvDocCount, returnValue.getInvDocCount());
		assertEquals("tf is wrong.", tf, returnValue.getTf(), delta);
		assertEquals("idf is wrong.", idf, returnValue.getIdf(), delta);
		assertEquals("vector is wrong.", vector, returnValue.getVector(), delta);
	}
	
	@Test
	public void verifyGetImportedClasses() throws Exception {
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		
		String importedClass1 = "edu.skku.blia.class1";
		String importedClass2 = "edu.skku.blia.class2";
		String importedClass3 = "edu.skku.blia.class2";
		
		sourceFileDAO.deleteAllImportedClasses();
		ArrayList<String> importedClasses = new ArrayList<String>();
		importedClasses.add(importedClass1);
		importedClasses.add(importedClass2);
		assertNotEquals("importedClass insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertImportedClasses(fileName1, productName, version1, importedClasses));
		importedClasses = new ArrayList<String>();
		importedClasses.add(importedClass3);
		assertNotEquals("importedClass insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertImportedClasses(fileName2, productName, version1, importedClasses));
		
		HashMap<String, ArrayList<String>> importedClassesMap = sourceFileDAO.getAllImportedClasses(productName, version1);
		assertEquals("importedClassesMap size is wrong.", 2, importedClassesMap.size());

		importedClasses = importedClassesMap.get(fileName1);
		assertTrue(importedClasses.contains(importedClass1) && importedClasses.contains(importedClass2));

		importedClasses = importedClassesMap.get(fileName2);
		assertTrue(importedClasses.contains(importedClass3));
	}

}
