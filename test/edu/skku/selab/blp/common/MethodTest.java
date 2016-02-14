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
public class MethodTest {

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
		String func1 = "func1";
		String returnType1 = "void";
		String params1 = "int int";
		String concatenatedMethodInfo = func1 + "|" + returnType1 + "|" + params1; 
		Method method1 = new Method(concatenatedMethodInfo);
		
		assertEquals("Method name is NOT equal.", func1, method1.getName());
		assertEquals("ReturnType is NOT equal.", returnType1, method1.getReturnType());
		assertEquals("Parameters is NOT equal.", params1, method1.getParams());
		
		String func2 = "func2";
		String returnType2 = "String";
		String params2 = "";
		concatenatedMethodInfo = func2 + "|" + returnType2 + "|" + params2; 
		Method method2 = new Method(concatenatedMethodInfo);
		
		assertEquals("Method name is NOT equal.", func2, method2.getName());
		assertEquals("ReturnType is NOT equal.", returnType2, method2.getReturnType());
		assertEquals("Parameters is NOT equal.", params2, method2.getParams());
	}
}
