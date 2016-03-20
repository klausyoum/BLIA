/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.common;

import static org.junit.Assert.*;

import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import edu.skku.selab.blp.common.Bug;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class CommentTest {

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
	public void verifyConstructor() {
		int ID = 1;
		String author = "BLIA Plus";
		String commentedDate = "2016-01-16 23:16:16 EDT";
		String commentedDateForVerfication = "2016-01-16 23:16:16";
		String commentCorpus = "coment corpus";
		
		Comment comment = new Comment(ID, commentedDate, author, commentCorpus);
		
		assertEquals("ID is NOT equal.", ID, comment.getID());
		assertEquals("commentedDate is NOT equal.", commentedDateForVerfication, comment.getCommentedDateString());
		assertEquals("author is NOT equal.", author, comment.getAuthor());
		assertEquals("commentCorpus is NOT equal.", commentCorpus, comment.getCommentCorpus());
	}

}
