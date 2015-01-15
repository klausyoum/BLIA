/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.db.dao;

import java.util.HashMap;
import java.util.HashSet;

import edu.skku.selab.blia.db.AnalysisValue;
import edu.skku.selab.blia.db.SimilarBugInfo;
import edu.skku.selab.blia.indexer.Bug;
import edu.skku.selab.blia.indexer.SourceFile;

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
		String sql = "INSERT INTO BUG_INFO (BUG_ID, PROD_NAME, FIXED_DATE, COR_SET, STRACE_SET) VALUES (?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		// releaseDate format : "2004-10-18 17:40:00"
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, bug.getID());
			ps.setString(2, bug.getProductName());
			ps.setString(3, bug.getFixedDateString());
			ps.setString(4, bug.getCorpuses());
			ps.setString(5, bug.getStackTraces());
			
			returnValue = ps.executeUpdate();
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
		
		String sql = "SELECT BUG_ID, PROD_NAME, FIXED_DATE, COR_SET, STRACE_SET FROM BUG_INFO";
		
		try {
			ps = conn.prepareStatement(sql);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				Bug bug = new Bug();
				bug.setID(rs.getString("BUG_ID"));
				bug.setProductName(rs.getString("PROD_NAME"));
				bug.setFixedDate(rs.getTimestamp("FIXED_DATE"));
				bug.setCorpuses(rs.getString("COR_SET"));
				bug.setStackTraces(rs.getString("STRACE_SET"));
				bugs.put(rs.getString("BUG_ID"), bug);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bugs;	
	}
	
	public Bug getBug(String bugID, String productName) {
		String sql = "SELECT FIXED_DATE, COR_SET, STRACE_SET FROM BUG_INFO WHERE BUG_ID = ? AND PROD_NAME = ?";
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
				bug.setStackTraces(rs.getString("STRACE_SET"));
			}
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
	
	public int getCorpusID(String corpus, String productName) {
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
		int corpusID = getCorpusID(analysisValue.getCorpus(), analysisValue.getProductName());
		
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
	
	public int deleteAllAnalysisValues() {
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
				"WHERE A.BUG_ID = ? AND A.PROD_NAME = ? AND " +
				"B.COR = ? AND " +
				"B.PROD_NAME = ? AND B.BUG_COR_ID = C.BUG_COR_ID";
		
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
				returnValue.setVector(rs.getDouble("VEC"));				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int insertBugFixInfo(String bugID, String fileName, String functionName, String version, String productName) {
		String sql = "INSERT INTO BUG_FIX_INFO (BUG_ID, FIXED_SF_VER_ID, FIXED_FUNC_VER_ID) VALUES (?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			SourceFileDAO sourceFileDAO = new SourceFileDAO();
			int fixedSourceFileID = sourceFileDAO.getSourceFileVersionID(fileName, productName, version);
			
			// TODO: implement functionDAO.getFunctionVersionID()
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
					
			// TODO: implement functionDAO.getFunctionVersionID()
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
		
		String sql = "SELECT A.SF_NAME, B.VER FROM SF_INFO A, SF_VER_INFO B, BUG_FIX_INFO C " + 
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

				fixedFiles.add(sourceFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fixedFiles;
	}
	
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

}
