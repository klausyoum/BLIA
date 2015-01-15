/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.db.dao;

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
		String sql = "INSERT INTO INT_ANALYSIS (BUG_ID, SF_VER_ID, VSM_SCORE, SIMI_SCORE, BL_SCORE, STRACE_SCORE, BLIA_SCORE) VALUES (?, ?, ?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			SourceFileDAO sourceFileDAO = new SourceFileDAO();
			int sourceFileVersionID = sourceFileDAO.getSourceFileVersionID(integratedAnalysisValue.getFileName(), integratedAnalysisValue.getProductName(), integratedAnalysisValue.getVersion());
			
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
	
	public IntegratedAnalysisValue getIntegratedAnalysisValue(String bugID) {
		IntegratedAnalysisValue returnValue = null;

		String sql = "SELECT C.SF_NAME, B.VER, C.PROD_NAME, A.VSM_SCORE, A.SIMI_SCORE, A.BL_SCORE, A.STRACE_SCORE, A.BLIA_SCORE "+
				"FROM INT_ANALYSIS A, SF_VER_INFO B, SF_INFO C " +
				"WHERE A.BUG_ID = ? AND A.SF_VER_ID = B.SF_VER_ID AND B.SF_ID = C.SF_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = new IntegratedAnalysisValue();
				
				returnValue.setBugID(bugID);
				returnValue.setFileName(rs.getString("SF_NAME"));
				returnValue.setProductName(rs.getString("PROD_NAME"));
				returnValue.setVsmScore(rs.getDouble("VSM_SCORE"));
				returnValue.setSimilarityScore(rs.getDouble("SIMI_SCORE"));
				returnValue.setBugLocatorScore(rs.getDouble("BL_SCORE"));
				returnValue.setStackTraceScore(rs.getDouble("STRACE_SCORE"));
				returnValue.setBliaScore(rs.getDouble("BLIA_SCORE"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}

}
