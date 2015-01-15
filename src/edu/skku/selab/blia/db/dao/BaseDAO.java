/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.db.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BaseDAO {

	protected static Connection conn = null;
	protected PreparedStatement ps = null;
	protected ResultSet rs = null;
	
	final static int INVALID = -1;	
	
	public BaseDAO() throws Exception {
		openConnection();
	}
	
	public void openConnection() throws Exception {
		if (null == conn) {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection("jdbc:h2:file:./db/blia", "sa", "");
		}
	}
	public void closeConnection() throws Exception {
		conn.close();
	}
}
