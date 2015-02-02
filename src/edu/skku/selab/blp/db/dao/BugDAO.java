/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.h2.api.ErrorCode;
import org.h2.jdbc.JdbcSQLException;

import edu.skku.selab.blp.common.Bug;
import edu.skku.selab.blp.common.SourceFile;
import edu.skku.selab.blp.db.AnalysisValue;
import edu.skku.selab.blp.db.SimilarBugInfo;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugDAO extends BaseDAO {

	/**
	 * @throws Exception
	 */
	public BugDAO() throws Exception {
		super();
	}
	
	public int insertBug(Bug bug) {
		String sql = "INSERT INTO BUG_INFO (BUG_ID, PROD_NAME, FIXED_DATE, COR_SET, TOT_CNT) VALUES (?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		// releaseDate format : "2004-10-18 17:40:00"
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bug.getID());
			ps.setString(2, bug.getProductName());
			ps.setString(3, bug.getFixedDateString());
			ps.setString(4, bug.getCorpuses());
			ps.setInt(5, bug.getTotalCorpusCount());
			
			returnValue = ps.executeUpdate();
			
			ArrayList<String> stackTraceClasses = bug.getStackTraceClasses();
			if (null != stackTraceClasses) {
				for (int i = 0; i < stackTraceClasses.size(); i++) {
					insertStackTraceClass(bug.getID(), stackTraceClasses.get(i));				
				}
			}
		} catch (JdbcSQLException e) {
			if (ErrorCode.DUPLICATE_KEY_1 != e.getErrorCode()) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		return returnValue;
	}
	
	public int deleteAllBugs() {
		String sql = "DELETE FROM BUG_INFO";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public HashMap<String, Bug> getBugs() {
		HashMap<String, Bug> bugs = new HashMap<String, Bug>();
		
		String sql = "SELECT BUG_ID, PROD_NAME, FIXED_DATE, COR_SET FROM BUG_INFO";
		
		try {
			ps = conn.prepareStatement(sql);
			
			Bug bug = null;
			String bugID = "";
			rs = ps.executeQuery();
			while (rs.next()) {
				bug = new Bug();
				bugID = rs.getString("BUG_ID"); 
				bug.setID(bugID);
				bug.setProductName(rs.getString("PROD_NAME"));
				bug.setFixedDate(rs.getTimestamp("FIXED_DATE"));
				bug.setCorpuses(rs.getString("COR_SET"));
				bugs.put(bugID, bug);
			}
			
			Iterator<String> bugsIter = bugs.keySet().iterator();
			while (bugsIter.hasNext()) {
				bugID = bugsIter.next();
				bug = bugs.get(bugID);
				bug.setStackTraceClasses(getStackTraceClasses(bugID));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bugs;	
	}
	
	
	public ArrayList<Bug> getBugs(String productName, boolean orderedByFixedDate) {
		ArrayList<Bug> bugs = new ArrayList<Bug>();
		
		String sql = "SELECT BUG_ID, PROD_NAME, FIXED_DATE, COR_SET FROM BUG_INFO " +
				"WHERE PROD_NAME = ? ";
		
		if (orderedByFixedDate) {
			sql += "ORDER BY FIXED_DATE";
		}
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			
			Bug bug = null;
			String bugID = "";
			rs = ps.executeQuery();
			while (rs.next()) {
				bug = new Bug();
				bugID = rs.getString("BUG_ID"); 
				bug.setID(bugID);
				bug.setProductName(rs.getString("PROD_NAME"));
				bug.setFixedDate(rs.getTimestamp("FIXED_DATE"));
				bug.setCorpuses(rs.getString("COR_SET"));
				bugs.add(bug);
			}
			
			for (int i = 0; i < bugs.size(); i++) {
				bug = bugs.get(i);
				bug.setStackTraceClasses(getStackTraceClasses(bug.getID()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bugs;	
	}
	
	public Bug getBug(String bugID, String productName) {
		String sql = "SELECT FIXED_DATE, COR_SET FROM BUG_INFO WHERE BUG_ID = ? AND PROD_NAME = ?";
		Bug bug = null;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			ps.setString(2, productName);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				bug = new Bug();
				bug.setID(bugID);
				bug.setProductName(productName);
				bug.setFixedDate(rs.getTimestamp("FIXED_DATE"));
				bug.setCorpuses(rs.getString("COR_SET"));
			}
			
			bug.setStackTraceClasses(getStackTraceClasses(bugID));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bug;	
	}

	
	public int insertCorpus(String corpus, String productName) {
		String sql = "INSERT INTO BUG_COR_INFO (COR, PROD_NAME) VALUES (?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, corpus);
			ps.setString(2, productName);
			
			returnValue = ps.executeUpdate();
			
			sql = "SELECT BUG_COR_ID FROM BUG_COR_INFO WHERE COR = ? AND PROD_NAME = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, corpus);
			ps.setString(2, productName);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				returnValue = rs.getInt("BUG_COR_ID");	
			}
		} catch (JdbcSQLException e) {
			e.printStackTrace();
			
			if (ErrorCode.DUPLICATE_KEY_1 != e.getErrorCode()) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllCorpuses() {
		String sql = "DELETE FROM BUG_COR_INFO";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	/**
	 * Get <Source file name, Corpus sets> with product name and version
	 * 
	 * @param productName	Product name
	 * @return HashMap<String, String>	<Source file name, Corpus sets>
	 */
	public HashMap<String, String> getCorpusSets(String productName) {
		HashMap<String, String> corpusSets = new HashMap<String, String>();
		
		String sql = "SELECT BUG_ID, COR_SET " +
					"FROM BUG_INFO " +
					"WHERE PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				corpusSets.put(rs.getString("BUG_ID"), rs.getString("COR_SET"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return corpusSets;
	}

	
	public HashMap<String, Integer> getCorpuses(String productName) {
		HashMap<String, Integer> fileInfo = new HashMap<String, Integer>();
		
		String sql = "SELECT COR, BUG_COR_ID FROM BUG_COR_INFO WHERE PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1,  productName);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				fileInfo.put(rs.getString("COR"), rs.getInt("BUG_COR_ID"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileInfo;
	}
	
	public int getCorpusCount(String productName) {
		String sql = "SELECT COUNT(BUG_COR_ID) FROM BUG_COR_INFO WHERE PROD_NAME = ?";
		
		int corpusCount = 0;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1,  productName);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				corpusCount = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return corpusCount;
	}
	
	public int getBugCount(String productName) {
		String sql = "SELECT COUNT(BUG_ID) FROM BUG_INFO WHERE PROD_NAME = ?";
		
		int bugCount = 0;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1,  productName);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				bugCount = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bugCount;
	}
	
	public int getSfCorpusID(String corpus, String productName) {
		int returnValue = INVALID;
		String sql = "SELECT SF_COR_ID FROM SF_COR_INFO WHERE COR = ? AND PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, corpus);
			ps.setString(2, productName);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				returnValue = rs.getInt("SF_COR_ID");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;	
	}
	
	public int insertStackTraceClass(String bugID, String className) {
		String sql = "INSERT INTO BUG_STRACE_INFO (BUG_ID, STRACE_CLASS) VALUES (?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			ps.setString(2, className);
			
			returnValue = ps.executeUpdate();
		} catch (JdbcSQLException e) {
			e.printStackTrace();
			
			if (ErrorCode.DUPLICATE_KEY_1 != e.getErrorCode()) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllStackTraceClasses() {
		String sql = "DELETE FROM BUG_STRACE_INFO";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}

	public ArrayList<String> getStackTraceClasses(String bugID) {
		ArrayList<String> stackTraceClasses = null;

		String sql = "SELECT STRACE_CLASS "+
				"FROM BUG_STRACE_INFO " +
				"WHERE BUG_ID = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == stackTraceClasses) {
					stackTraceClasses = new ArrayList<String>();
				}
				
				stackTraceClasses.add(rs.getString("STRACE_CLASS"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return stackTraceClasses;
	}
	
	public int insertBugSfAnalysisValue(AnalysisValue analysisValue) {
		int corpusID = getSfCorpusID(analysisValue.getCorpus(), analysisValue.getProductName());
		
		String sql = "INSERT INTO BUG_SF_ANALYSIS (BUG_ID, SF_COR_ID, TERM_CNT, INV_DOC_CNT, TF, IDF, VEC) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, analysisValue.getName());
			ps.setInt(2, corpusID);
			ps.setInt(3, analysisValue.getTermCount());
			ps.setInt(4, analysisValue.getInvDocCount());
			ps.setDouble(5, analysisValue.getTf());
			ps.setDouble(6, analysisValue.getIdf());
			ps.setDouble(7, analysisValue.getVector());
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllBugSfAnalysisValues() {
		String sql = "DELETE FROM BUG_SF_ANALYSIS";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public AnalysisValue getBugSfAnalysisValue(String bugID, String productName, String corpus) {
		AnalysisValue returnValue = null;

		String sql = "SELECT C.TERM_CNT, C.INV_DOC_CNT, C.TF, C.IDF, C.VEC "+
				"FROM BUG_INFO A, SF_COR_INFO B, BUG_SF_ANALYSIS C " +
				"WHERE A.BUG_ID = ? AND A.PROD_NAME = ? AND " +
				"B.COR = ? AND " +
				"B.PROD_NAME = ? AND B.SF_COR_ID = C.SF_COR_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			ps.setString(2, productName);
			ps.setString(3, corpus);
			ps.setString(4, productName);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = new AnalysisValue();
				
				returnValue.setName(bugID);
				returnValue.setProductName(productName);
				returnValue.setCorpus(corpus);
				returnValue.setTermCount(rs.getInt("TERM_CNT"));
				returnValue.setInvDocCount(rs.getInt("INV_DOC_CNT"));
				returnValue.setTf(rs.getDouble("TF"));
				returnValue.setIdf(rs.getDouble("IDF"));
				returnValue.setVector(rs.getDouble("VEC"));				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int getBugCorpusID(String corpus, String productName) {
		int returnValue = INVALID;
		String sql = "SELECT BUG_COR_ID FROM BUG_COR_INFO WHERE COR = ? AND PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, corpus);
			ps.setString(2, productName);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				returnValue = rs.getInt("BUG_COR_ID");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;	
	}
	
	public int insertBugAnalysisValue(AnalysisValue analysisValue) {
		int corpusID = analysisValue.getCorpusID();
		if (INVALID == corpusID) {
			corpusID = getBugCorpusID(analysisValue.getCorpus(), analysisValue.getProductName());
		}
		
		String sql = "INSERT INTO BUG_ANALYSIS (BUG_ID, BUG_COR_ID, VEC) " +
				"VALUES (?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, analysisValue.getName());
			ps.setInt(2, corpusID);
			ps.setDouble(3, analysisValue.getVector());
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllBugAnalysisValues() {
		String sql = "DELETE FROM BUG_ANALYSIS";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public AnalysisValue getBugAnalysisValue(String bugID, String productName, String corpus) {
		AnalysisValue returnValue = null;

		String sql = "SELECT C.VEC "+
				"FROM BUG_INFO A, BUG_COR_INFO B, BUG_ANALYSIS C " +
				"WHERE A.BUG_ID = ? AND A.PROD_NAME = ? AND "+
				"A.BUG_ID = C.BUG_ID AND B.COR = ? AND B.BUG_COR_ID = C.BUG_COR_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			ps.setString(2, productName);
			ps.setString(3, corpus);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = new AnalysisValue();
				
				returnValue.setName(bugID);
				returnValue.setProductName(productName);
				returnValue.setCorpus(corpus);
				returnValue.setVector(rs.getDouble("VEC"));				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public ArrayList<AnalysisValue> getBugAnalysisValues(String bugID) {
		ArrayList<AnalysisValue> bugAnalysisValues = null;
		AnalysisValue returnValue = null;

		String sql = "SELECT B.COR, C.BUG_COR_ID, C.VEC "+
				"FROM BUG_COR_INFO B, BUG_ANALYSIS C " +
				"WHERE C.BUG_ID = ? AND B.BUG_COR_ID = C.BUG_COR_ID ORDER BY C.BUG_COR_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == bugAnalysisValues) {
					bugAnalysisValues = new ArrayList<AnalysisValue>();
				}
				
				returnValue = new AnalysisValue();
				returnValue.setName(bugID);
				returnValue.setCorpus(rs.getString("COR"));
				returnValue.setCorpusID(rs.getInt("BUG_COR_ID"));
				returnValue.setVector(rs.getDouble("VEC"));		
				
				bugAnalysisValues.add(returnValue);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bugAnalysisValues;
	}
	
	public int insertBugFixInfo(String bugID, String fileName, String functionName, String version, String productName) {
		String sql = "INSERT INTO BUG_FIX_INFO (BUG_ID, FIXED_SF_VER_ID, FIXED_FUNC_VER_ID) VALUES (?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			SourceFileDAO sourceFileDAO = new SourceFileDAO();
			int fixedSourceFileID = sourceFileDAO.getSourceFileVersionID(fileName, productName, version);
			
			// TODO: implement functionDAO.getFunctionVersionID() later
			int fixedFunctionID = INVALID;

			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			ps.setInt(2, fixedSourceFileID);
			ps.setInt(3, fixedFunctionID);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int insertBugFixedFileInfo(String bugID, String fileName, String version, String productName) {
		String sql = "INSERT INTO BUG_FIX_INFO (BUG_ID, FIXED_SF_VER_ID, FIXED_FUNC_VER_ID) VALUES (?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			SourceFileDAO sourceFileDAO = new SourceFileDAO();
			int fixedSourceFileID = sourceFileDAO.getSourceFileVersionID(fileName, productName, version);
			int fixedFunctionID = INVALID;

			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			ps.setInt(2, fixedSourceFileID);
			ps.setInt(3, fixedFunctionID);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}

	public int insertBugFixedFuncInfo(String bugID, String functionName, String version, String productName) {
		String sql = "INSERT INTO BUG_FIX_INFO (BUG_ID, FIXED_SF_VER_ID, FIXED_FUNC_VER_ID) VALUES (?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			int fixedSourceFileID = INVALID;
					
			// TODO: implement functionDAO.getFunctionVersionID() later
			int fixedFunctionID = INVALID;

			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			ps.setInt(2, fixedSourceFileID);
			ps.setInt(3, fixedFunctionID);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}

	
	public int deleteAllBugFixedInfo() {
		String sql = "DELETE FROM BUG_FIX_INFO";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public HashSet<SourceFile> getFixedFiles(String bugID) {
		HashSet<SourceFile> fixedFiles = null;
		
		String sql = "SELECT A.SF_NAME, B.VER, C.FIXED_SF_VER_ID FROM SF_INFO A, SF_VER_INFO B, BUG_FIX_INFO C " + 
				"WHERE C.BUG_ID = ? AND C.FIXED_SF_VER_ID = B.SF_VER_ID AND A.SF_ID = B.SF_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == fixedFiles) {
					fixedFiles = new HashSet<SourceFile>();
				}

				SourceFile sourceFile = new SourceFile();
				sourceFile.setName(rs.getString("SF_NAME"));
				sourceFile.setVersion(rs.getString("VER"));
				sourceFile.setSourceFileVersionID(rs.getInt("FIXED_SF_VER_ID"));

				fixedFiles.add(sourceFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fixedFiles;
	}
	
//	/**
//	 * 
//	 * @param productName
//	 * @return <BugID, SourceFileVersionID>
//	 */
//	public HashMap<String, Integer> getAllFixedFiles(String productName) {
//		HashMap<String, Integer> fixedFiles = null;
//		
//		String sql = "SELECT B.BUG_ID, B.FIXED_SF_VER_ID FROM BUG_INFO A, BUG_FIX_INFO B " + 
//				"WHERE A.BUG_ID = B.BUG_ID AND A.PROD_NAME = ?";
//		
//		try {
//			ps = conn.prepareStatement(sql);
//			ps.setString(1, productName);
//			
//			rs = ps.executeQuery();
//			
//			while (rs.next()) {
//				if (null == fixedFiles) {
//					fixedFiles = new HashMap<String, Integer>();
//				}
//
//				fixedFiles.put(rs.getString("BUG_ID"), rs.getInt("FIXED_SF_VER_ID"));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		return fixedFiles;
//	}
	
	public int insertSimilarBugInfo(String bugID, String similarBugID, double similarityScore) {
		String sql = "INSERT INTO SIMI_BUG_INFO (BUG_ID, SIMI_BUG_ID, SIMI_BUG_SCORE) VALUES (?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			ps.setString(2, similarBugID);
			ps.setDouble(3, similarityScore);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllSimilarBugInfo() {
		String sql = "DELETE FROM SIMI_BUG_INFO";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public HashSet<SimilarBugInfo> getSimilarBugInfos(String bugID) {
		HashSet<SimilarBugInfo> similarBugInfos = null;

		String sql = "SELECT SIMI_BUG_ID, SIMI_BUG_SCORE FROM SIMI_BUG_INFO " + 
				"WHERE BUG_ID = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == similarBugInfos) {
					similarBugInfos = new HashSet<SimilarBugInfo>();
				}

				SimilarBugInfo similarBugInfo = new SimilarBugInfo();
				similarBugInfo.setSimilarBugID(rs.getString("SIMI_BUG_ID"));
				similarBugInfo.setSimilarityScore(rs.getDouble("SIMI_BUG_SCORE"));

				similarBugInfos.add(similarBugInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return similarBugInfos;
	}
	
	public int updateTotalCoupusCount(String productName, String bugID, int totalCorpusCount) {
		String sql = "UPDATE BUG_INFO SET TOT_CNT = ? " +
				"WHERE BUG_ID = ? AND PROD_NAME = ?";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, totalCorpusCount);
			ps.setString(2, bugID);
			ps.setString(3, productName);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
}
