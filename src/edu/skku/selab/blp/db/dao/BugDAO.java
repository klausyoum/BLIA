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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.h2.api.ErrorCode;
import org.h2.jdbc.JdbcSQLException;

import edu.skku.selab.blp.common.Bug;
import edu.skku.selab.blp.common.BugCorpus;
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
		String sql = "INSERT INTO BUG_INFO (BUG_ID, PROD_NAME, OPEN_DATE, FIXED_DATE, COR, SMR_COR, DESC_COR, TOT_CNT, VER) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		// releaseDate format : "2004-10-18 17:40:00"
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bug.getID());
			ps.setString(2, bug.getProductName());
			ps.setString(3, bug.getOpenDateString());
			ps.setString(4, bug.getFixedDateString());
			BugCorpus bugCorpus = bug.getCorpus();
			ps.setString(5, bugCorpus.getContent());
			ps.setString(6, bugCorpus.getSummaryPart());
			ps.setString(7, bugCorpus.getDescriptionPart());
			ps.setInt(8, bug.getTotalCorpusCount());
			ps.setString(9, bug.getVersion());
			
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
		
		String sql = "SELECT BUG_ID, PROD_NAME, OPEN_DATE, FIXED_DATE, COR, SMR_COR, DESC_COR, TOT_CNT, VER FROM BUG_INFO";
		
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
				bug.setOpenDate(rs.getTimestamp("OPEN_DATE"));
				bug.setFixedDate(rs.getTimestamp("FIXED_DATE"));
				
				BugCorpus bugCorpus = new BugCorpus();
				bugCorpus.setContent(rs.getString("COR"));
				bugCorpus.setSummaryPart(rs.getString("SMR_COR"));
				bugCorpus.setDescriptionPart(rs.getString("DESC_COR"));
				bug.setCorpus(bugCorpus);
				
				bug.setTotalCorpusCount(rs.getInt("TOT_CNT"));
				bug.setVersion(rs.getString("VER"));
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
	
	
	public ArrayList<Bug> getAllBugs(String productName, boolean orderedByFixedDate) {
		ArrayList<Bug> bugs = new ArrayList<Bug>();
		
		String sql = "SELECT BUG_ID, PROD_NAME, OPEN_DATE, FIXED_DATE, COR, SMR_COR, DESC_COR, TOT_CNT, VER FROM BUG_INFO " +
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
				bug.setOpenDate(rs.getTimestamp("OPEN_DATE"));
				bug.setFixedDate(rs.getTimestamp("FIXED_DATE"));

				BugCorpus bugCorpus = new BugCorpus();
				bugCorpus.setContent(rs.getString("COR"));
				bugCorpus.setSummaryPart(rs.getString("SMR_COR"));
				bugCorpus.setDescriptionPart(rs.getString("DESC_COR"));
				bug.setCorpus(bugCorpus);

				bug.setTotalCorpusCount(rs.getInt("TOT_CNT"));
				bug.setVersion(rs.getString("VER"));
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
		String sql = "SELECT OPEN_DATE, FIXED_DATE, COR, SMR_COR, DESC_COR, TOT_CNT, VER FROM BUG_INFO WHERE BUG_ID = ? AND PROD_NAME = ?";
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
				bug.setOpenDate(rs.getTimestamp("OPEN_DATE"));
				bug.setFixedDate(rs.getTimestamp("FIXED_DATE"));

				BugCorpus bugCorpus = new BugCorpus();
				bugCorpus.setContent(rs.getString("COR"));
				bugCorpus.setSummaryPart(rs.getString("SMR_COR"));
				bugCorpus.setDescriptionPart(rs.getString("DESC_COR"));
				bug.setCorpus(bugCorpus);

				bug.setTotalCorpusCount(rs.getInt("TOT_CNT"));
				bug.setVersion(rs.getString("VER"));
			}
			
			bug.setStackTraceClasses(getStackTraceClasses(bugID));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bug;	
	}

	
	public int insertWord(String word, String productName) {
		String sql = "INSERT INTO BUG_WRD_INFO (WRD, PROD_NAME) VALUES (?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, word);
			ps.setString(2, productName);
			
			returnValue = ps.executeUpdate();
			
			sql = "SELECT BUG_WRD_ID FROM BUG_WRD_INFO WHERE WRD = ? AND PROD_NAME = ?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, word);
			ps.setString(2, productName);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				returnValue = rs.getInt("BUG_WRD_ID");	
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
	
	public int deleteAllWords() {
		String sql = "DELETE FROM BUG_WRD_INFO";
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
	public HashMap<String, String> getCorpusMap(String productName) {
		HashMap<String, String> corpusMap = new HashMap<String, String>();
		
		String sql = "SELECT BUG_ID, COR " +
					"FROM BUG_INFO " +
					"WHERE PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				corpusMap.put(rs.getString("BUG_ID"), rs.getString("COR"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return corpusMap;
	}

	
	public HashMap<String, Integer> getWordMap(String productName) {
		HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
		
		String sql = "SELECT WRD, BUG_WRD_ID FROM BUG_WRD_INFO WHERE PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1,  productName);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				wordMap.put(rs.getString("WRD"), rs.getInt("BUG_WRD_ID"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wordMap;
	}
	
	public int getWordCount(String productName) {
		String sql = "SELECT COUNT(BUG_WRD_ID) FROM BUG_WRD_INFO WHERE PROD_NAME = ?";
		
		int wordCount = 0;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1,  productName);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				wordCount = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wordCount;
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
	
	public int getSfWordID(String word, String productName) {
		int returnValue = INVALID;
		String sql = "SELECT SF_WRD_ID FROM SF_WRD_INFO WHERE WRD = ? AND PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, word);
			ps.setString(2, productName);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				returnValue = rs.getInt("SF_WRD_ID");
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
		int wordID = getSfWordID(analysisValue.getWord(), analysisValue.getProductName());
		
		String sql = "INSERT INTO BUG_SF_VEC (BUG_ID, SF_WRD_ID, TERM_CNT, INV_DOC_CNT, TF, IDF, VEC) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, analysisValue.getName());
			ps.setInt(2, wordID);
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
		String sql = "DELETE FROM BUG_SF_VEC";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public AnalysisValue getBugSfAnalysisValue(String bugID, String productName, String word) {
		AnalysisValue returnValue = null;

		String sql = "SELECT C.TERM_CNT, C.INV_DOC_CNT, C.TF, C.IDF, C.VEC "+
				"FROM BUG_INFO A, SF_WRD_INFO B, BUG_SF_VEC C " +
				"WHERE A.BUG_ID = ? AND A.PROD_NAME = ? AND " +
				"B.WRD = ? AND " +
				"B.PROD_NAME = ? AND B.SF_WRD_ID = C.SF_WRD_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			ps.setString(2, productName);
			ps.setString(3, word);
			ps.setString(4, productName);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = new AnalysisValue();
				
				returnValue.setName(bugID);
				returnValue.setProductName(productName);
				returnValue.setWord(word);
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
	
	public int getBugWordID(String word, String productName) {
		int returnValue = INVALID;
		String sql = "SELECT BUG_WRD_ID FROM BUG_WRD_INFO WHERE WRD = ? AND PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, word);
			ps.setString(2, productName);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				returnValue = rs.getInt("BUG_WRD_ID");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;	
	}
	
	public int insertBugAnalysisValue(AnalysisValue analysisValue) {
		int wordID = analysisValue.getWordsID();
		if (INVALID == wordID) {
			wordID = getBugWordID(analysisValue.getWord(), analysisValue.getProductName());
		}
		
		String sql = "INSERT INTO BUG_VEC (BUG_ID, BUG_WRD_ID, VEC) " +
				"VALUES (?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, analysisValue.getName());
			ps.setInt(2, wordID);
			ps.setDouble(3, analysisValue.getVector());
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllBugAnalysisValues() {
		String sql = "DELETE FROM BUG_VEC";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public AnalysisValue getBugAnalysisValue(String bugID, String productName, String word) {
		AnalysisValue returnValue = null;

		String sql = "SELECT C.VEC "+
				"FROM BUG_INFO A, BUG_WRD_INFO B, BUG_VEC C " +
				"WHERE A.BUG_ID = ? AND A.PROD_NAME = ? AND "+
				"A.BUG_ID = C.BUG_ID AND B.WRD = ? AND B.BUG_WRD_ID = C.BUG_WRD_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bugID);
			ps.setString(2, productName);
			ps.setString(3, word);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = new AnalysisValue();
				
				returnValue.setName(bugID);
				returnValue.setProductName(productName);
				returnValue.setWord(word);
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

		String sql = "SELECT B.WRD, C.BUG_WRD_ID, C.VEC "+
				"FROM BUG_WRD_INFO B, BUG_VEC C " +
				"WHERE C.BUG_ID = ? AND B.BUG_WRD_ID = C.BUG_WRD_ID ORDER BY C.BUG_WRD_ID";
		
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
				returnValue.setWord(rs.getString("WRD"));
				returnValue.setWordID(rs.getInt("BUG_WRD_ID"));
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
		String sql = "INSERT INTO SIMI_BUG_ANAYSIS (BUG_ID, SIMI_BUG_ID, SIMI_BUG_SCORE) VALUES (?, ?, ?)";
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
		String sql = "DELETE FROM SIMI_BUG_ANAYSIS";
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

		String sql = "SELECT SIMI_BUG_ID, SIMI_BUG_SCORE FROM SIMI_BUG_ANAYSIS " + 
				"WHERE BUG_ID = ? AND SIMI_BUG_SCORE != 0.0";
		
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
	
	public int updateTotalCoupusCount(String productName, String bugID, int totalWordCount) {
		String sql = "UPDATE BUG_INFO SET TOT_CNT = ? " +
				"WHERE BUG_ID = ? AND PROD_NAME = ?";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, totalWordCount);
			ps.setString(2, bugID);
			ps.setString(3, productName);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
}
