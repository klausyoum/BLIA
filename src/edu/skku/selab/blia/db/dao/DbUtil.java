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
public class DbUtil {
	protected static Connection conn = null;
	protected PreparedStatement ps = null;
	protected ResultSet rs = null;

	/**
	 * 
	 */
	public DbUtil() throws Exception {
		if (null == conn) {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection("jdbc:h2:file:./db/blia", "sa", "");
		}
	}
	
	private int createAllTables() throws Exception {
		String sql = "CREATE TABLE SF_INFO(SF_ID INT PRIMARY KEY AUTO_INCREMENT, SF_NAME VARCHAR(255), PROD_NAME VARCHAR(31)); " +
				"CREATE UNIQUE INDEX IDX_SF_INFO_NAME ON SF_INFO(SF_NAME); " +
				"CREATE INDEX IDX_SF_PROD_NAME ON SF_INFO(PROD_NAME); " +
				
				"CREATE TABLE SF_VER_INFO (SF_VER_ID INT PRIMARY KEY AUTO_INCREMENT, SF_ID INT, VER VARCHAR(15), COR_SET VARCHAR, TOT_CNT INT, LEN_SCORE DOUBLE); " +
				"CREATE INDEX COMP_IDX_SF_VER_ID ON SF_VER_INFO(SF_ID, VER); " +
				
				"CREATE TABLE SF_COR_INFO (SF_COR_ID INT PRIMARY KEY AUTO_INCREMENT, COR VARCHAR(255), PROD_NAME VARCHAR(31)); " + 
				"CREATE UNIQUE INDEX IDX_SF_COR ON SF_COR_INFO(COR); " +
				"CREATE INDEX IDX_SF_COR_PROD ON SF_COR_INFO(PROD_NAME); " +
				
				"CREATE TABLE SF_ANALYSIS (SF_VER_ID INT, SF_COR_ID INT, TERM_CNT INT, INV_DOC_CNT INT, TF DOUBLE, IDF DOUBLE, VEC DOUBLE); " +
				"CREATE UNIQUE INDEX COMP_IDX_SF_ANALYSIS ON SF_ANALYSIS(SF_VER_ID, SF_COR_ID); " +
				
//				"CREATE TABLE FUNC_INFO(FUNC_ID INT PRIMARY KEY AUTO_INCREMENT, FUNC_NAME VARCHAR(255), PROD_NAME VARCHAR(31)); " +
//				"CREATE TABLE FUNC_VER_INFO (FUNC_VER_ID INT PRIMARY KEY AUTO_INCREMENT, FUNC_ID INT, VER VARCHAR(15), COR_SET VARCHAR, TOT_CNT INT, LEN_SCORE DOUBLE); " +
//				"CREATE TABLE FUNC_COR_INFO (FUNC_COR_ID INT PRIMARY KEY AUTO_INCREMENT, COR VARCHAR(255), PROD_NAME VARCHAR(31)); " +
//				"CREATE TABLE FUNC_ANALYSIS (FUNC_VER_ID INT, FUNC_COR_ID INT, TERM_CNT INT, INV_DOC_CNT INT, TF DOUBLE, IDF DOUBLE, VEC DOUBLE); " +

				"CREATE TABLE BUG_INFO(BUG_ID VARCHAR(31) PRIMARY KEY, PROD_NAME VARCHAR(31), FIXED_DATE DATETIME, COR_SET VARCHAR, TOT_CNT INT, STRACE_SET VARCHAR(2047)); " +
				"CREATE INDEX IDX_BUG_INFO ON BUG_INFO(PROD_NAME); " +
				
				"CREATE TABLE BUG_COR_INFO(BUG_COR_ID INT PRIMARY KEY AUTO_INCREMENT, COR VARCHAR(255), PROD_NAME VARCHAR(31)); " +
				"CREATE UNIQUE INDEX IDX_BUG_COR ON BUG_COR_INFO(COR); " +
				"CREATE INDEX IDX_BUG_COR_PROD ON BUG_COR_INFO(PROD_NAME); " +
				
				"CREATE TABLE BUG_SF_ANALYSIS(BUG_ID VARCHAR(31), SF_COR_ID INT, TERM_CNT INT, INV_DOC_CNT INT, TF DOUBLE, IDF DOUBLE, VEC DOUBLE); " +
				"CREATE UNIQUE INDEX COMP_IDX_BUG_SF_ANALYSIS ON BUG_SF_ANALYSIS(BUG_ID, SF_COR_ID); " +

				"CREATE TABLE BUG_ANALYSIS(BUG_ID VARCHAR(31), BUG_COR_ID INT, VEC DOUBLE); " +
				"CREATE UNIQUE INDEX COMP_IDX_BUG_ANALYSIS ON BUG_ANALYSIS(BUG_ID, BUG_COR_ID); " +
				
				"CREATE TABLE BUG_FIX_INFO(BUG_ID VARCHAR(31), FIXED_SF_VER_ID INT, FIXED_FUNC_VER_ID INT); " +
				"CREATE INDEX IDX_BUG_FIX_INFO ON BUG_FIX_INFO(BUG_ID); " +
				
				"CREATE TABLE SIMI_BUG_INFO(BUG_ID VARCHAR(31), SIMI_BUG_ID VARCHAR(31), SIMI_BUG_SCORE DOUBLE); " +
				"CREATE INDEX IDX_SIMI_BUG_INFO ON SIMI_BUG_INFO(BUG_ID); " +
				

				"CREATE TABLE COMM_INFO(COMM_ID VARCHAR(31) PRIMARY KEY, PROD_NAME VARCHAR(31), COMM_DATE DATETIME, DESC VARCHAR); " +
				"CREATE INDEX IDX_COMM_PROD_NAME ON COMM_INFO(PROD_NAME); " +
				
				"CREATE TABLE COMM_FILE_INFO(COMM_ID VARCHAR(31), SF_ID INT); " +
				"CREATE INDEX IDX_COMM_FILE_INFO ON COMM_FILE_INFO(COMM_ID); " +
				
				
				"CREATE TABLE VER_INFO(VER VARCHAR(15) PRIMARY KEY, REL_DATE DATETIME); " +

				"CREATE TABLE INT_ANALYSIS(BUG_ID VARCHAR(31), SF_VER_ID INT, TERM_CNT INT, INV_DOC_CNT INT, TF DOUBLE, IDF DOUBLE, VEC DOUBLE, VSM_SCORE DOUBLE, SIMI_SCORE DOUBLE, BL_SCORE DOUBLE, STRACE_SCORE DOUBLE, BLIA_SCORE DOUBLE); " +
				"CREATE INDEX IDX_INT_ANALYSIS ON INT_ANALYSIS(BUG_ID); " +
				
				"CREATE TABLE EXP_INFO(TOP1 INT, TOP5 INT, TOP10 INT, MRR DOUBLE, MAP DOUBLE, PROD_NAME VARCHAR(31), ALG_NAME VARCHAR(31), ALG_DESC VARCHAR(255), EXP_DATE DATETIME); " +
				"CREATE INDEX IDX_EXP_INFO_PROD ON EXP_INFO(PROD_NAME); " +
				"CREATE INDEX IDX_EXP_INFO_ALG ON EXP_INFO(ALG_NAME); ";
								
				
		int returnValue = BaseDAO.INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	private int dropAllTables() throws Exception {
		String sql = "DROP TABLE SF_INFO; " +
				"DROP TABLE SF_VER_INFO; " +
				"DROP TABLE SF_COR_INFO; " +
				"DROP TABLE SF_ANALYSIS; " +

//				"DROP TABLE FUNC_INFO; " +
//				"DROP TABLE FUNC_VER_INFO; " +
//				"DROP TABLE FUNC_COR_INFO; " +
//				"DROP TABLE FUNC_ANALYSIS; " +

				"DROP TABLE BUG_INFO; " +
				"DROP TABLE BUG_COR_INFO; " +
				"DROP TABLE BUG_SF_ANALYSIS; " +
				"DROP TABLE BUG_ANALYSIS; " +
				"DROP TABLE BUG_FIX_INFO; " +
				"DROP TABLE SIMI_BUG_INFO; " +
				"DROP TABLE INT_ANALYSIS; " +

				"DROP TABLE COMM_INFO; " +
				"DROP TABLE COMM_FILE_INFO; " +
				"DROP TABLE VER_INFO; " +

				"DROP TABLE EXP_INFO; ";


		int returnValue = BaseDAO.INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;		
	}
	
	private void initializeDB() throws Exception {
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		sourceFileDAO.deleteAllSourceFiles();
		sourceFileDAO.deleteAllVersions();
		sourceFileDAO.deleteAllCorpusSets();
		sourceFileDAO.deleteAllCorpuses();
		sourceFileDAO.deleteAllAnalysisValues();
		
		BugDAO bugDAO = new BugDAO();
		bugDAO.deleteAllBugs();
		bugDAO.deleteAllCorpuses();
		bugDAO.deleteAllBugSfAnalysisValues();
		bugDAO.deleteAllBugFixedInfo();
		bugDAO.deleteAllSimilarBugInfo();
		
		CommitDAO commitDAO = new CommitDAO();
		commitDAO.deleteAllCommitInfo();
		
		IntegratedAnalysisDAO integratedAnalysisDAO = new IntegratedAnalysisDAO();
		integratedAnalysisDAO.deleteAllIntegratedAnalysisInfos();
		
		ExperimentResultDAO experimentDAO = new ExperimentResultDAO();
		experimentDAO.deleteAllExperimentResults();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		DbUtil dbUtil = new DbUtil();
		
		dbUtil.dropAllTables();
		dbUtil.createAllTables();
//		dbUtil.initializeDB();
	}

}
