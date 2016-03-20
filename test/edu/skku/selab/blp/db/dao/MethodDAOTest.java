/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.skku.selab.blp.common.Method;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class MethodDAOTest {
	private String[] methodNames = { "setUpBeforeClass", "tearDownAfterClass", "setUp", "tearDown" };
	private String[] returnTypes = { "void", "", "String", "HashMap<String, Integer>" };
	private String[] argTypes = { "", "String int", "int boolean", "String String"};
	private int sourceFileVersions[] = { 1, 1, 2, 2 };
	private String[] hashKeys = { "", "", "", "" };

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
		dbUtil.openConnetion();
		dbUtil.initializeAllData();
		dbUtil.closeConnection();
		
		prepareTestingData();
	}
	
	private void prepareTestingData() throws Exception {
		MethodDAO methodDAO = new MethodDAO();


		for (int i = 0; i < 4; ++i) {
			Method method = new Method(sourceFileVersions[i], methodNames[i], returnTypes[i], argTypes[i]);
			hashKeys[i] = method.getHashKey();

			methodDAO.insertMethod(method);
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void verifyGetMethods() throws Exception{
		MethodDAO methodDAO = new MethodDAO();
		
		int sourceFileVersionID = 1;
		HashMap<String, Method> methodMap = methodDAO.getMethods(sourceFileVersionID);
		
		for (int i = 0; i < 2; ++i) {
			Method method = methodMap.get(hashKeys[i]);
			assertNotNull("Retured method is NULL!", method);
			
			assertEquals(sourceFileVersionID, method.getSourceFileVersionID());
			assertEquals(methodNames[i], method.getName());
			assertEquals(returnTypes[i], method.getReturnType());
			assertEquals(argTypes[i], method.getParams());
			assertEquals(hashKeys[i], method.getHashKey());
		}
		
		sourceFileVersionID = 2;
		methodMap = methodDAO.getMethods(sourceFileVersionID);
		
		for (int i = 2; i < 4; ++i) {
			Method method = methodMap.get(hashKeys[i]);
			assertNotNull("Retured method is NULL!", method);
			
			assertEquals(sourceFileVersionID, method.getSourceFileVersionID());
			assertEquals(methodNames[i], method.getName());
			assertEquals(returnTypes[i], method.getReturnType());
			assertEquals(argTypes[i], method.getParams());
			assertEquals(hashKeys[i], method.getHashKey());
		}
	}

	@Test
	public void verifyGetAllMethods() throws Exception{
		MethodDAO methodDAO = new MethodDAO();
		
		HashMap<Integer, ArrayList<Method>> methodMap = methodDAO.getAllMethods();
		ArrayList<Method> methods = methodMap.get(1);
		assertEquals(2, methods.size());
		assertEquals(sourceFileVersions[0], methods.get(0).getSourceFileVersionID());
		assertEquals(methodNames[0], methods.get(0).getName());
		assertEquals(returnTypes[0], methods.get(0).getReturnType());
		assertEquals(argTypes[0], methods.get(0).getParams());
		assertEquals(hashKeys[0], methods.get(0).getHashKey());
		
		assertEquals(sourceFileVersions[1], methods.get(1).getSourceFileVersionID());
		assertEquals(methodNames[1], methods.get(1).getName());
		assertEquals(returnTypes[1], methods.get(1).getReturnType());
		assertEquals(argTypes[1], methods.get(1).getParams());
		assertEquals(hashKeys[1], methods.get(1).getHashKey());
		
		methods = methodMap.get(2);
		assertEquals(2, methods.size());
		assertEquals(sourceFileVersions[2], methods.get(0).getSourceFileVersionID());
		assertEquals(methodNames[2], methods.get(0).getName());
		assertEquals(returnTypes[2], methods.get(0).getReturnType());
		assertEquals(argTypes[2], methods.get(0).getParams());
		assertEquals(hashKeys[2], methods.get(0).getHashKey());
		
		assertEquals(sourceFileVersions[3], methods.get(1).getSourceFileVersionID());
		assertEquals(methodNames[3], methods.get(1).getName());
		assertEquals(returnTypes[3], methods.get(1).getReturnType());
		assertEquals(argTypes[3], methods.get(1).getParams());
		assertEquals(hashKeys[3], methods.get(1).getHashKey());
	}
}
