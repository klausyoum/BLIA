/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.db.dao;

import java.util.HashMap;

import edu.skku.selab.blia.db.IntegratedAnalysisValue;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class IntegratedAnalysisDAO extends BaseDAO {

	/**
	 * @throws Exception
	 */
	public IntegratedAnalysisDAO() throws Exception {
		super();
	}
	
	public int insertIntegratedAnalysisVaule(IntegratedAnalysisValue integratedAnalysisValue) {
		String sql = "INSERT INTO INT_ANALYSIS (BUG_ID, SF_VER_ID, VSM_SCORE, SIMI_SCORE, BL_SCORE, STRACE_SCORE, BLIA_SCORE) "+
				"VALUES (?, ?, ?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			SourceFileDAO sourceFileDAO = new SourceFileDAO();
			int sourceFileVersionID = integratedAnalysisValue.getSourceFileVersionID();
			
			if (INVALID == sourceFileVersionID) {
				sourceFileVersionID = sourceFileDAO.getSourceFileVersionID(integratedAnalysisValue.getFileName(), integratedAnalysisValue.getProductName(), integratedAnalysisValue.getVersion());
			}
			
			ps = conn.prepareStatement(sql);
			ps.setString(1, integratedAnalysisValue.getBugID());
			ps.setInt(2, sourceFileVersionID);
			ps.setDouble(3, integratedAnalysisValue.getVsmScore());
			ps.setDouble(4, integratedAnalysisValue.getSimilarityScore());
			ps.setDouble(5, integratedAnalysisValue.getBugLocatorScore());
			ps.setDouble(6, integratedAnalysisValue.getStackTraceScore());
			ps.setDouble(7, integratedAnalysisValue.getBliaScore());
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int updateSimilarScore(String bugID, int sourceFileVersionID, double similarScore) {
		String sql = "UPDATE INT_ANALYSIS SET SIMI_SCORE = ? WHERE BUG_ID = ? AND SF_VER_ID = ?";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setDouble(1, similarScore);
			ps.setString(2, bugID);
			ps.setInt(3, sourceFileVersionID);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int updateBugLocatorScore(IntegratedAnalysisValue integratedAnalysisValue) {
		String sql = "UPDATE INT_ANALYSIS SET VSM_SCORE = ?, SIMI_SCORE = ?, BL_SCORE = ? WHERE BUG_ID = ? AND SF_VER_ID = ?";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setDouble(1, integratedAnalysisValue.getVsmScore());
			ps.setDouble(2, integratedAnalysisValue.getSimilarityScore());
			ps.setDouble(3, integratedAnalysisValue.getBugLocatorScore());
			ps.setString(4, integratedAnalysisValue.getBugID());
			ps.setInt(5, integratedAnalysisValue.getSourceFileVersionID());
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;		
	}
	
	public int deleteAllIntegratedAnalysisInfos() {
		String sql = "DELETE FROM INT_ANALYSIS";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public HashMap<Integer, IntegratedAnalysisValue> getIntegratedAnalysisValues(String bugID) {
		HashMap<Integer, IntegratedAnalysisValue> integratedAnalysisValues = null;
		IntegratedAnalysisValue resultValue = null;

		String sql = "SELECT C.SF_NAME, B.VER, C.PROD_NAME, A.SF_VER_ID, A.VSM_SCORE, A.SIMI_SCORE, A.BL_SCORE, A.STRACE_SCORE, A.BLIA_SCORE "+
				"FROM INT_ANALYSIS A, SF_VER_INFO B, SF_INFO C " +
				"WHERE A.BUG_ID = ? AND A.SF_VER_ID = B.SF_VER_ID AND B.SF_ID = C.SF_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == integratedAnalysisValues) {
					integratedAnalysisValues = new HashMap<Integer, IntegratedAnalysisValue>();
				}
				
				resultValue = new IntegratedAnalysisValue();
				resultValue.setBugID(bugID);
				resultValue.setFileName(rs.getString("SF_NAME"));
				resultValue.setProductName(rs.getString("PROD_NAME"));
				resultValue.setSourceFileVersionID(rs.getInt("SF_VER_ID"));
				resultValue.setVsmScore(rs.getDouble("VSM_SCORE"));
				resultValue.setSimilarityScore(rs.getDouble("SIMI_SCORE"));
				resultValue.setBugLocatorScore(rs.getDouble("BL_SCORE"));
				resultValue.setStackTraceScore(rs.getDouble("STRACE_SCORE"));
				resultValue.setBliaScore(rs.getDouble("BLIA_SCORE"));
				
				integratedAnalysisValues.put(resultValue.getSourceFileVersionID(), resultValue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return integratedAnalysisValues;
	}
}
