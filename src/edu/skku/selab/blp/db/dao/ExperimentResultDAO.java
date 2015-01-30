/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db.dao;

import edu.skku.selab.blp.db.ExperimentResult;
import edu.skku.selab.blp.db.IntegratedAnalysisValue;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class ExperimentResultDAO extends BaseDAO {

	/**
	 * @throws Exception
	 */
	public ExperimentResultDAO() throws Exception {
		super();
	}
	
	public int insertExperimentResult(ExperimentResult experimentResult) {
		String sql = "INSERT INTO EXP_INFO (TOP1, TOP5, TOP10, MRR, MAP, PROD_NAME, ALG_NAME, ALG_DESC, EXP_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, experimentResult.getTop1());
			ps.setInt(2, experimentResult.getTop5());
			ps.setInt(3, experimentResult.getTop10());
			ps.setDouble(4, experimentResult.getMRR());
			ps.setDouble(5, experimentResult.getMAP());
			ps.setString(6, experimentResult.getProductName());
			ps.setString(7, experimentResult.getAlgorithmName());
			ps.setString(8, experimentResult.getAlgorithmDescription());
			ps.setString(9, experimentResult.getExperimentDateString());
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}

	public int deleteAllExperimentResults() {
		String sql = "DELETE FROM EXP_INFO";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public ExperimentResult getExperimentResult(String productName, String algorithmName) {
		ExperimentResult returnValue = null;

		String sql = "SELECT TOP1, TOP5, TOP10, MRR, MAP, ALG_DESC, EXP_DATE "+
				"FROM EXP_INFO " +
				"WHERE PROD_NAME = ? AND ALG_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, algorithmName);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = new ExperimentResult();
				
				returnValue.setTop1(rs.getInt("TOP1"));
				returnValue.setTop5(rs.getInt("TOP5"));
				returnValue.setTop10(rs.getInt("TOP10"));
				returnValue.setMRR(rs.getDouble("MRR"));
				returnValue.setMAP(rs.getDouble("MAP"));
				returnValue.setProductName(productName);
				returnValue.setAlgorithmName(algorithmName);
				returnValue.setAlgorithmDescription(rs.getString("ALG_DESC"));
				returnValue.setExperimentDate(rs.getTimestamp("EXP_DATE"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
}
