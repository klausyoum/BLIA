/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db.dao;

import java.util.HashMap;

import edu.skku.selab.blp.common.Method;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class MethodDAO extends BaseDAO {

	/**
	 * @throws Exception
	 */
	public MethodDAO() throws Exception {
		super();
	}
	
	public int insertMethod(Method method) {
		String sql = "INSERT INTO MTH_INFO (SF_VER_ID, MTH_NAME, RET_TYPE, PARAMS, HASH_KEY) VALUES (?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			ps.setInt(1, method.getSourceFileVersionID());
			ps.setString(2, method.getName());
			ps.setString(3, method.getReturnType());
			ps.setString(4, method.getParams());
			ps.setString(5, method.getHashKey());
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (INVALID != returnValue) {
			returnValue = getMethodID(method);
		} 

		return returnValue;
	}
	
	public int getMethodID(Method method) {
		int returnValue = INVALID;
		String sql = "SELECT MTH_ID FROM MTH_INFO " +
				"WHERE HASH_KEY = ? AND SF_VER_ID = ?";
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			ps.setString(1, method.getHashKey());
			ps.setInt(2, method.getSourceFileVersionID());
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = rs.getInt("MTH_ID");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;	
	}
	
	// <Hash key, Method>
	public HashMap<String, Method> getMethods(int sourceFileVersionID) {
		HashMap<String, Method> methodInfo = new HashMap<String, Method>();
		
		String sql = "SELECT MTH_ID, MTH_NAME, RET_TYPE, PARAMS, HASH_KEY FROM MTH_INFO WHERE SF_VER_ID = ?";
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			ps.setInt(1, sourceFileVersionID);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				Method method = new Method(rs.getInt("MTH_ID"), sourceFileVersionID,
						rs.getString("MTH_NAME"), rs.getString("RET_TYPE"), rs.getString("PARAMS"),
						rs.getString("HASH_KEY"));
				methodInfo.put(rs.getString("HASH_KEY"), method);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return methodInfo;
	}
	
	// <Hash key, Method>
	public HashMap<String, Method> getAllMethods() {
		HashMap<String, Method> methodInfo = new HashMap<String, Method>();
		
		String sql = "SELECT MTH_ID, SF_VER_ID, MTH_NAME, RET_TYPE, PARAMS, HASH_KEY FROM MTH_INFO";
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				Method method = new Method(rs.getInt("MTH_ID"), rs.getInt("SF_VER_ID"),
						rs.getString("MTH_NAME"), rs.getString("RET_TYPE"), rs.getString("PARAMS"),
						rs.getString("HASH_KEY"));
				methodInfo.put(rs.getString("HASH_KEY"), method);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return methodInfo;
	}
	
	public int deleteAllMethods() {
		String sql = "DELETE FROM MTH_INFO";
		int returnValue = INVALID;
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
}
