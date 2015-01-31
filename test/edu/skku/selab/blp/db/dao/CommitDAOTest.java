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
import java.util.Date;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blp.db.CommitInfo;
import edu.skku.selab.blp.db.dao.CommitDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class CommitDAOTest {
	private String productName = "BLIA";
	private String fileName1 = "test_10.java";
	private String fileName2 = "test_11.java";
	private String fileName3 = "test_20.java";
	private String fileName4 = "test_21.java";
	private String fileName5 = "test_22.java";


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
		
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		
		// preparation phase
		sourceFileDAO.deleteAllSourceFiles();
		assertNotEquals("fileName1 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertSourceFile(fileName1, productName));
		assertNotEquals("fileName2 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertSourceFile(fileName2, productName));
		assertNotEquals("fileName3 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertSourceFile(fileName3, productName));
		assertNotEquals("fileName4 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertSourceFile(fileName4, productName));
		assertNotEquals("fileName5 insertion failed!", BaseDAO.INVALID, sourceFileDAO.insertSourceFile(fileName5, productName));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void verifyGetCommitInfo() throws Exception {
		String commitID1 = "COMMIT-001";
		String commitDateString1 = "2015-01-15 15:19:00";
		String description1 = "[1] Commited by Klaus for BLIA testing";

		String commitID2 = "COMMIT-002";
		String commitDateString2 = "2015-01-31 15:19:00";
		String description2 = "[2] Commited by Klaus for BLIA testing";
		
		CommitInfo commitInfo1 = new CommitInfo();
		commitInfo1.setCommitID(commitID1);
		commitInfo1.setProductName(productName);
		commitInfo1.setCommitDate(commitDateString1);
		commitInfo1.setDescription(description1);
		commitInfo1.addCommitFile(fileName1);
		commitInfo1.addCommitFile(fileName2);
		
		CommitInfo commitInfo2 = new CommitInfo();
		commitInfo2.setCommitID(commitID2);
		commitInfo2.setProductName(productName);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date commitDate = simpleDateFormat.parse(commitDateString2);
		commitInfo2.setCommitDate(commitDate);
		commitInfo2.setDescription(description2);
		commitInfo2.addCommitFile(fileName3);
		commitInfo2.addCommitFile(fileName4);
		commitInfo2.addCommitFile(fileName5);

		CommitDAO commitDAO = new CommitDAO();		
		commitDAO.deleteAllCommitInfo();
		assertNotEquals("CommitInfo insertion failed!", BaseDAO.INVALID, commitDAO.insertCommitInfo(commitInfo1));
		assertNotEquals("CommitInfo insertion failed!", BaseDAO.INVALID, commitDAO.insertCommitInfo(commitInfo2));
		
		CommitInfo returnedCommitInfo = commitDAO.getCommitInfo(commitID1);
		assertEquals("commitID1 is wrong.", commitID1, returnedCommitInfo.getCommitID());
		assertEquals("productName is wrong.", productName, returnedCommitInfo.getProductName());
		assertEquals("commitDateString1 is wrong.", commitDateString1, returnedCommitInfo.getCommitDateString());
		assertEquals("description1 is wrong.", description1, returnedCommitInfo.getDescription());
		
		assertEquals("CommitFiles count is wrong.", 2, returnedCommitInfo.getCommitFiles().size());
		Iterator<String> iter = returnedCommitInfo.getCommitFiles().iterator();
		String commitFile = iter.next();
		if ( (!commitFile.equalsIgnoreCase(fileName1)) && (!commitFile.equalsIgnoreCase(fileName2))) {
			fail("commitFiles are wrong.");
		}
		
		commitFile = iter.next();
		if ( (!commitFile.equalsIgnoreCase(fileName1)) && (!commitFile.equalsIgnoreCase(fileName2))) {
			fail("commitFiles are wrong.");
		}
		
		returnedCommitInfo = commitDAO.getCommitInfo(commitID2);
		assertEquals("commitID2 is wrong.", commitID2, returnedCommitInfo.getCommitID());
		assertEquals("productName is wrong.", productName, returnedCommitInfo.getProductName());
		assertEquals("commitDateString2 is wrong.", commitDateString2, returnedCommitInfo.getCommitDateString());
		assertEquals("description2 is wrong.", description2, returnedCommitInfo.getDescription());

		assertEquals("CommitFiles count is wrong.", 3, returnedCommitInfo.getCommitFiles().size());
		iter = returnedCommitInfo.getCommitFiles().iterator();
		commitFile = iter.next();
		if ((!commitFile.equalsIgnoreCase(fileName3)) && (!commitFile.equalsIgnoreCase(fileName4)) && (!commitFile.equalsIgnoreCase(fileName5))) {
			fail("commitFiles are wrong.");
		}

		commitFile = iter.next();
		if ((!commitFile.equalsIgnoreCase(fileName3)) && (!commitFile.equalsIgnoreCase(fileName4)) && (!commitFile.equalsIgnoreCase(fileName5))) {
			fail("commitFiles are wrong.");
		}
		
		commitFile = iter.next();
		if ((!commitFile.equalsIgnoreCase(fileName3)) && (!commitFile.equalsIgnoreCase(fileName4)) && (!commitFile.equalsIgnoreCase(fileName5))) {
			fail("commitFiles are wrong.");
		}
	}

}
