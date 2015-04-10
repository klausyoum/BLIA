/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import edu.skku.selab.blp.Property;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BaseDAO {
	protected static Connection analysisDbConnection = null;
	protected static Connection evaluationDbConnection = null;
	protected PreparedStatement ps = null;
	protected ResultSet rs = null;
	
	final public static int INVALID = -1;	
	
	public BaseDAO() throws Exception {
		String dbName = Property.DEFAULT;	// default DB name
		
		Property property = Property.getInstance(); 
		if (null != property) {
			dbName = property.getProductName();
		}
		
		openConnection(dbName);
	}

	private static void openEvaluationDbConnection() throws Exception {
		if (null == evaluationDbConnection) {
			Class.forName("org.h2.Driver");
			evaluationDbConnection = DriverManager.getConnection("jdbc:h2:file:./db/evaluation", "sa", "");
		}
	}
	
	public static void openConnection() throws Exception {
		openEvaluationDbConnection();
		
		if (null == analysisDbConnection) {
			Class.forName("org.h2.Driver");
			String connectionURL = "jdbc:h2:file:./db/" + Property.DEFAULT;
			analysisDbConnection = DriverManager.getConnection(connectionURL, "sa", "");
		}
	}


	
	public static void openConnection(String dbName) throws Exception {
		if (null == analysisDbConnection) {
			Class.forName("org.h2.Driver");
			String connectionURL = "jdbc:h2:file:./db/" + dbName;
			analysisDbConnection = DriverManager.getConnection(connectionURL, "sa", "");
		}
		
		openEvaluationDbConnection();
	}
	public static void closeConnection() throws Exception {
		if (null != analysisDbConnection) {
			analysisDbConnection.close();
			analysisDbConnection = null;
		}
		
		if (null != evaluationDbConnection) {
			evaluationDbConnection.close();
			evaluationDbConnection = null;
		}
	}
	
	public static Connection getAnalysisDbConnection() {
		return analysisDbConnection;
	}
	
	public static Connection getEvaluationDbConnection() {
		return evaluationDbConnection;
	}
}
