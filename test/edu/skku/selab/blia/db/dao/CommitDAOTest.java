/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.db.dao;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blia.db.CommitInfo;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class CommitDAOTest {

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
	public void verifyCommitDAO() throws Exception {
		CommitDAO commitDAO = new CommitDAO();
		String commitID = "COMMIT-001";
		String productName = "BLIA";
		String commitDateString = "2015-01-15 15:19:00";
		String description = "Commited by Klaus for BLIA testing";
		String fileName1 = "test_10.java";
		String fileName2 = "test_11.java";

		CommitInfo commitInfo = new CommitInfo();
		
		commitInfo.setCommitID(commitID);
		commitInfo.setProductName(productName);
		commitInfo.setCommitDateString(commitDateString);
		commitInfo.setDescription(description);
		commitInfo.addCommitFile(fileName1);
		commitInfo.addCommitFile(fileName2);		
		
		commitDAO.deleteAllCommitInfo();
		commitDAO.insertCommitInfo(commitInfo);
		CommitInfo returnedCommitInfo = commitDAO.getCommitInfo(commitID);
		System.out.printf("Commit ID: %s, Product Name: %s, Commit date: %s\n", returnedCommitInfo.getCommitID(),
				returnedCommitInfo.getProductName(), returnedCommitInfo.getCommitDateString());
		System.out.printf("Description: %s\n", returnedCommitInfo.getDescription());
		
		Iterator<String> iter = returnedCommitInfo.getCommitFiles().iterator();
		while (iter.hasNext()) {
			System.out.printf("Commit file: %s\n", iter.next());
		}
		
		commitDAO.closeConnection();
	}

}
