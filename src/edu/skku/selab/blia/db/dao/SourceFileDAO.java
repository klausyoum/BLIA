/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.db.dao;

import java.util.Date;
import java.util.HashMap;

import edu.skku.selab.blia.db.AnalysisValue;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileDAO extends BaseDAO {
	
	/**
	 * @throws Exception
	 */
	public SourceFileDAO() throws Exception {
		super();
	}
	
	public int insertSourceFile(String fileName, String productName) {
		String sql = "INSERT INTO SF_INFO (SF_NAME, PROD_NAME) VALUES (?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, fileName);
			ps.setString(2, productName);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllSourceFiles() {
		String sql = "DELETE FROM SF_INFO";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public HashMap<String, Integer> getSourceFiles(String productName) {
		HashMap<String, Integer> fileInfo = new HashMap<String, Integer>();
		
		String sql = "SELECT SF_NAME, SF_ID FROM SF_INFO WHERE PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				fileInfo.put(rs.getString("SF_NAME"), rs.getInt("SF_ID"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileInfo;
	}

	public int insertVersion(String version, String releaseDate) {
		String sql = "INSERT INTO VER_INFO (VER, REL_DATE) VALUES (?, ?)";
		int returnValue = INVALID;
		
		// releaseDate format : "2004-10-18 17:40:00"
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, version);
			ps.setString(2, releaseDate);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllVersions() {
		String sql = "DELETE FROM VER_INFO";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public HashMap<String, Date> getVersions() {
		HashMap<String, Date> versions = new HashMap<String, Date>();
		
		String sql = "SELECT VER, REL_DATE FROM VER_INFO";
		
		try {
			ps = conn.prepareStatement(sql);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				versions.put(rs.getString("VER"), rs.getTimestamp("REL_DATE"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versions;	
	}
	
	public int getSourceFileID(String fileName, String productName) {
		int returnValue = INVALID;
		String sql = "SELECT SF_ID FROM SF_INFO " +
				"WHERE SF_NAME = ? AND PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, fileName);
			ps.setString(2, productName);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = rs.getInt("SF_ID");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;	
	}

	
	public int getSourceFileVersionID(String fileName, String productName, String version) {
		int returnValue = INVALID;
		String sql = "SELECT B.SF_VER_ID FROM SF_INFO A, SF_VER_INFO B " +
				"WHERE A.SF_NAME = ? AND A.PROD_NAME = ? AND B.VER = ? AND A.SF_ID = B.SF_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, fileName);
			ps.setString(2, productName);
			ps.setString(3, version);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = rs.getInt("SF_VER_ID");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;	
	}
	
	
	public int insertCorpusSet(String fileName, String prodName, String version, String corpusSet, double lengthScore) {
		HashMap<String, Integer> fileInfo = getSourceFiles(prodName);
		
		String sql = "INSERT INTO SF_VER_INFO (SF_ID, VER, COR_SET, LEN_SCORE) VALUES (?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, fileInfo.get(fileName));
			ps.setString(2, version);
			ps.setString(3, corpusSet);
			ps.setDouble(4, lengthScore);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllCorpusSets() {
		String sql = "DELETE FROM SF_VER_INFO";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}

	public HashMap<String, String> getCorpusSets(String productName, String version) {
		HashMap<String, String> corpusSets = new HashMap<String, String>();
		
		String sql = "SELECT A.SF_NAME, B.COR_SET " +
					"FROM SF_INFO A, SF_VER_INFO B " +
					"WHERE A.SF_ID = B.SF_ID AND " +
					"A.PROD_NAME = ? AND B.VER = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, version);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				corpusSets.put(rs.getString("SF_NAME"), rs.getString("COR_SET"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return corpusSets;
	}
	
	public HashMap<String, Double> getLengthScores(String productName, String version) {
		HashMap<String, Double> lengthScores = new HashMap<String, Double>();
		
		String sql = "SELECT A.SF_NAME, B.LEN_SCORE " +
					"FROM SF_INFO A, SF_VER_INFO B " +
					"WHERE A.SF_ID = B.SF_ID AND " +
					"A.PROD_NAME = ? AND B.VER = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, version);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				lengthScores.put(rs.getString("SF_NAME"), rs.getDouble("LEN_SCORE"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lengthScores;
	}
	
	public int insertCorpus(String corpus, String productName) {
		String sql = "INSERT INTO SF_COR_INFO (COR, PROD_NAME) VALUES (?, ?)";
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
		String sql = "DELETE FROM SF_COR_INFO";
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
		
		String sql = "SELECT COR, SF_COR_ID FROM SF_COR_INFO WHERE PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1,  productName);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				fileInfo.put(rs.getString("COR"), rs.getInt("SF_COR_ID"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileInfo;
	}
	
	public int getCorpusID(String corpus, String productName) {
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
	
	public int insertSourceFileAnalysisValue(AnalysisValue analysisValue) {
		
		int fileVersionID = getSourceFileVersionID(analysisValue.getName(),
				analysisValue.getProductName(), analysisValue.getVersion());
		int corpusID = getCorpusID(analysisValue.getCorpus(), analysisValue.getProductName());
		
		String sql = "INSERT INTO SF_ANALYSIS (SF_VER_ID, SF_COR_ID, TERM_CNT, INV_DOC_CNT, TF, IDF, VEC) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, fileVersionID);
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
	
	public int deleteAllAnalysisValues() {
		String sql = "DELETE FROM SF_ANALYSIS";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public AnalysisValue getSourceFileAnalysisValue(String fileName, String productName, String version,
			String corpus) {
		AnalysisValue returnValue = null;

		String sql = "SELECT D.TERM_CNT, D.INV_DOC_CNT, D.TF, D.IDF, D.VEC "+
				"FROM SF_INFO A, SF_VER_INFO B, SF_COR_INFO C, SF_ANALYSIS D " +
				"WHERE A.SF_NAME = ? AND A.PROD_NAME = ? AND A.SF_ID = B.SF_ID AND " +
				"B.VER = ? AND B.SF_VER_ID = D.SF_VER_ID AND C.COR = ? AND " +
				"C.PROD_NAME = ? AND C.SF_COR_ID = D.SF_COR_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, fileName);
			ps.setString(2, productName);
			ps.setString(3, version);
			ps.setString(4, corpus);
			ps.setString(5, productName);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = new AnalysisValue();
				
				returnValue.setName(fileName);
				returnValue.setProductName(productName);
				returnValue.setVersion(version);
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
	
}
