/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.analysis;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.blia.indexer.BugCorpusCreator;
import edu.skku.selab.blp.blia.indexer.BugVectorCreator;
import edu.skku.selab.blp.blia.indexer.GitCommitLogCollector;
import edu.skku.selab.blp.blia.indexer.SourceFileCorpusCreator;
import edu.skku.selab.blp.blia.indexer.SourceFileIndexer;
import edu.skku.selab.blp.blia.indexer.SourceFileVectorCreator;
import edu.skku.selab.blp.db.CommitInfo;
import edu.skku.selab.blp.db.dao.BaseDAO;
import edu.skku.selab.blp.db.dao.DbUtil;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.test.utils.TestConfiguration;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class ScmRepoAnalyzerTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestConfiguration.setProperty();
		
		DbUtil dbUtil = new DbUtil();
		dbUtil.initializeAllData();
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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void verifyGetDiffDays() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ScmRepoAnalyzer scmRepoAnalyzer = new ScmRepoAnalyzer();
		
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(2015, 2, 1);
		Date sourceDate = calendar.getTime();
		calendar.set(2015, 2, 12);
		Date targetDate = calendar.getTime();
		
		Method getDiffDaysMethod = ScmRepoAnalyzer.class.getDeclaredMethod("getDiffDays", Date.class, Date.class);
		getDiffDaysMethod.setAccessible(true);
		
		Double returnValue = (Double) getDiffDaysMethod.invoke(scmRepoAnalyzer, sourceDate, targetDate);
		assertEquals(11.0, returnValue.doubleValue(), 0.001);
	}
	
	@Test
	public void verifyFindCommitInfoWithinDays() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ScmRepoAnalyzer scmRepoAnalyzer = new ScmRepoAnalyzer();

		Method findCommitInfoWithinDaysMethod = ScmRepoAnalyzer.class.getDeclaredMethod("findCommitInfoWithinDays", ArrayList.class, Date.class, Integer.class);
		findCommitInfoWithinDaysMethod.setAccessible(true);
		
		CommitInfo commitInfo1 = new CommitInfo();
		commitInfo1.setCommitDate("2004-10-07 01:02:22");

		CommitInfo commitInfo2 = new CommitInfo();
		commitInfo2.setCommitDate("2004-10-15 04:28:35");
		
		CommitInfo commitInfo3 = new CommitInfo();
		commitInfo3.setCommitDate("2004-10-26 05:00:41");

		CommitInfo commitInfo4 = new CommitInfo();
		commitInfo4.setCommitDate("2005-01-21 12:33:02");

		ArrayList<CommitInfo> commitInfos = new ArrayList<CommitInfo>(); 
		commitInfos.add(commitInfo1);
		commitInfos.add(commitInfo2);
		commitInfos.add(commitInfo3);
		commitInfos.add(commitInfo4);
		
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(2004, Calendar.OCTOBER, 27, 9, 10, 22);
		Date openDate = calendar.getTime();
		
		@SuppressWarnings("unchecked")
		ArrayList<CommitInfo> foundCommitInfos = (ArrayList<CommitInfo>) findCommitInfoWithinDaysMethod.invoke(scmRepoAnalyzer, commitInfos, openDate, new Integer(15));
		assertEquals(2, foundCommitInfos.size());
		assertEquals(commitInfo2.getCommitDate(), foundCommitInfos.get(0).getCommitDate());
		assertEquals(commitInfo3.getCommitDate(), foundCommitInfos.get(1).getCommitDate());
	}
	
	@Test
	public void verifyCalculateCommitLogScore() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ScmRepoAnalyzer scmRepoAnalyzer = new ScmRepoAnalyzer();

		Method calculateCommitLogScoreMethod = ScmRepoAnalyzer.class.getDeclaredMethod("calculateCommitLogScore", Date.class, Date.class, Integer.class);
		calculateCommitLogScoreMethod.setAccessible(true);
		
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(2004, Calendar.OCTOBER, 7, 1, 2, 22);
		Date commitDate = calendar.getTime();
		calendar.set(2004, Calendar.OCTOBER, 12, 21, 53, 0);
		Date openDate = calendar.getTime();
		Integer pastDays = new Integer(15);

		Double commitLogScore = (Double) calculateCommitLogScoreMethod.invoke(scmRepoAnalyzer, commitDate, openDate, pastDays);
		assertEquals(commitLogScore.doubleValue(), 0.009, 0.0001);
	}
	
	@Test
	public void verifyAnalyze() throws Exception {
		long startTime = System.currentTimeMillis();
		
		String version = SourceFileDAO.DEFAULT_VERSION_STRING;
		// Following function is needed to set file count for Property.getFileCount() and fixed files information at BugRepoAnalyzer
		SourceFileCorpusCreator sourceFileCorpusCreator = new SourceFileCorpusCreator();
		sourceFileCorpusCreator.create(version);
		
		SourceFileIndexer sourceFileIndexer = new SourceFileIndexer();
		sourceFileIndexer.createIndex(version);
		
		SourceFileVectorCreator sourceFileVectorCreator = new SourceFileVectorCreator();
		sourceFileVectorCreator.create(version);
		
		BugCorpusCreator bugCorpusCreator = new BugCorpusCreator();
		boolean stackTraceAnalysis = true;
		bugCorpusCreator.create(stackTraceAnalysis);
		
		SourceFileAnalyzer sourceFileAnalyzer = new SourceFileAnalyzer();
		boolean useStructuredInformation = false;
		sourceFileAnalyzer.analyze(version, useStructuredInformation);
		
		BugVectorCreator bugVectorCreator = new BugVectorCreator();
		bugVectorCreator.create();
		
		BugRepoAnalyzer bugRepoAnalyzer = new BugRepoAnalyzer();
//		bugRepoAnalyzer.computeSimilarity();
		bugRepoAnalyzer.analyze();

		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time1 of ScmRepoAnalyzer.analyze(): %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		

		String repoDir = "D:\\workspace\\eclipse.platform.swt\\.git";
		String productName = Property.getInstance().getProductName();
		Calendar since = new GregorianCalendar(2004, Calendar.OCTOBER, 1);
		Calendar until = new GregorianCalendar(2010, Calendar.MAY, 1);
		GitCommitLogCollector gitCommitLogCollector = new GitCommitLogCollector(productName, repoDir);
		gitCommitLogCollector.collectCommitLog(since.getTime(), until.getTime());

		elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time2 of ScmRepoAnalyzer.analyze(): %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		
		
		ScmRepoAnalyzer scmRepoAnalyzer = new ScmRepoAnalyzer();
		scmRepoAnalyzer.analyze(version);
		
		elapsedTime = System.currentTimeMillis() - startTime;
		System.out.printf("Elapsed time3 of ScmRepoAnalyzer.analyze(): %d.%d sec\n", elapsedTime / 1000, elapsedTime % 1000);		
	}
}
