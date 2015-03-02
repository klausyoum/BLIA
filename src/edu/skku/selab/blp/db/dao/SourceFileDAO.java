/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.h2.api.ErrorCode;
import org.h2.jdbc.JdbcSQLException;

import edu.skku.selab.blp.common.SourceFileCorpus;
import edu.skku.selab.blp.db.AnalysisValue;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileDAO extends BaseDAO {
	final static public String DEFAULT_VERSION_STRING = "v1.0";
	final static public double INIT_LENGTH_SCORE = 0.0;
	final static public int INIT_TOTAL_COUPUS_COUNT = 0;
	
	
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
	
	public int getSourceFileCount(String productName, String version) {
		String sql = "SELECT COUNT(SF_VER_ID) FROM SF_INFO A, SF_VER_INFO B WHERE A.PROD_NAME = ? AND A.SF_ID = B.SF_ID AND B.VER = ?";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, version);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				returnValue = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue;
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
	
	public HashSet<String> getSourceFileNames(String productName, String version) {
		HashSet<String> sourceFileNames = null;
		String sql = "SELECT A.SF_NAME FROM SF_INFO A, SF_VER_INFO B " +
				"WHERE A.PROD_NAME = ? AND B.VER = ? AND A.SF_ID = B.SF_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, version);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == sourceFileNames) {
					sourceFileNames = new HashSet<String>();
				}
				sourceFileNames.add(rs.getString("SF_NAME"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sourceFileNames;	
	}
	
	public HashSet<String> getClassNames(String productName, String version) {
		HashSet<String> sourceFileNames = null;
		String sql = "SELECT A.SF_NAME FROM SF_INFO A, SF_VER_INFO B " +
				"WHERE A.PROD_NAME = ? AND B.VER = ? AND A.SF_ID = B.SF_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, version);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == sourceFileNames) {
					sourceFileNames = new HashSet<String>();
				}
				
				String fileName = rs.getString("SF_NAME");
				String className = fileName.substring(0, fileName.lastIndexOf("."));
				sourceFileNames.add(className);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sourceFileNames;	
	}
	
	public int insertCorpusSet(String fileName, String productName, String version, SourceFileCorpus corpus, int totalCorpusCount, double lengthScore) {
		HashMap<String, Integer> fileInfo = getSourceFiles(productName);
		
		String sql = "INSERT INTO SF_VER_INFO (SF_ID, VER, COR, CLS_COR, MTH_COR, VAR_COR, CMT_COR, TOT_CNT, LEN_SCORE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, fileInfo.get(fileName));
			ps.setString(2, version);
			ps.setString(3, corpus.getContent());
			ps.setString(4, corpus.getClassPart());
			ps.setString(5, corpus.getMethodPart());
			ps.setString(6, corpus.getVariablePart());
			ps.setString(7, corpus.getCommentPart());
			ps.setInt(8, totalCorpusCount);
			ps.setDouble(9, lengthScore);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllCorpuses() {
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

	/**
	 * Get <Source file name, CorpusMap> with product name and version
	 * 
	 * @param productName	Product name
	 * @param version		Version
	 * @return HashMap<String, SourceFileCorpus>	<Source file name, Source file corpus>
	 */
	public HashMap<String, SourceFileCorpus> getCorpusMap(String productName, String version) {
		HashMap<String, SourceFileCorpus> corpusSets = new HashMap<String, SourceFileCorpus>();
		
		String sql = "SELECT A.SF_NAME, B.COR, B.CLS_COR, B.MTH_COR, B.VAR_COR, B.CMT_COR " +
					"FROM SF_INFO A, SF_VER_INFO B " +
					"WHERE A.SF_ID = B.SF_ID AND " +
					"A.PROD_NAME = ? AND B.VER = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, version);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				SourceFileCorpus corpus = new SourceFileCorpus();
				corpus.setContent(rs.getString("COR"));
				corpus.setClassPart(rs.getString("CLS_COR"));
				corpus.setMethodPart(rs.getString("MTH_COR"));
				corpus.setVariablePart(rs.getString("VAR_COR"));
				corpus.setCommentPart(rs.getString("CMT_COR"));
				corpusSets.put(rs.getString("SF_NAME"), corpus);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return corpusSets;
	}
	
	/**
	 * Get SourceFileCorpus with source file version ID
	 * 
	 * @param sourceFileVersionID	Source file version ID
	 * @return SourceFileCorpus		Source file corpus
	 */
	public SourceFileCorpus getCorpus(int sourceFileVersionID) {
		String sql = "SELECT COR, CLS_COR, MTH_COR, VAR_COR, CMT_COR " +
					"FROM SF_VER_INFO B " +
					"WHERE SF_VER_ID = ?";
		
		SourceFileCorpus corpus = null;
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, sourceFileVersionID);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				corpus = new SourceFileCorpus();
				corpus.setContent(rs.getString("COR"));
				corpus.setClassPart(rs.getString("CLS_COR"));
				corpus.setMethodPart(rs.getString("MTH_COR"));
				corpus.setVariablePart(rs.getString("VAR_COR"));
				corpus.setCommentPart(rs.getString("CMT_COR"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return corpus;
	}
	
	public HashMap<String, Integer> getSourceFileVersionIDs(String productName, String version) {
		HashMap<String, Integer> sourceFileVersionIDs = new HashMap<String, Integer>();
		
		String sql = "SELECT A.SF_NAME, B.SF_VER_ID " +
					"FROM SF_INFO A, SF_VER_INFO B " +
					"WHERE A.SF_ID = B.SF_ID AND " +
					"A.PROD_NAME = ? AND B.VER = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, version);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				sourceFileVersionIDs.put(rs.getString("SF_NAME"), rs.getInt("SF_VER_ID"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sourceFileVersionIDs;
	}
	
	public HashMap<String, Integer> getTotalCorpusLengths(String productName, String version) {
		HashMap<String, Integer> totalCorpusLengths = new HashMap<String, Integer>();
		
		String sql = "SELECT A.SF_NAME, B.TOT_CNT " +
					"FROM SF_INFO A, SF_VER_INFO B " +
					"WHERE A.SF_ID = B.SF_ID AND " +
					"A.PROD_NAME = ? AND B.VER = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, version);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				totalCorpusLengths.put(rs.getString("SF_NAME"), rs.getInt("TOT_CNT"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return totalCorpusLengths;
	}
	
	public int updateLengthScore(String productName, String fileName, String version, double lengthScore) {
		String sql = "UPDATE SF_VER_INFO SET LEN_SCORE = ? " +
				"WHERE SF_ID IN (SELECT A.SF_ID FROM SF_INFO A, SF_VER_INFO B WHERE  A.SF_ID = B.SF_ID AND A.PROD_NAME = ? " +
				"AND A.SF_NAME = ? AND B.VER = ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setDouble(1, lengthScore);
			ps.setString(2, productName);
			ps.setString(3, fileName);
			ps.setString(4, version);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int updateTotalCoupusCount(String productName, String fileName, String version, int totalCorpusCount) {
		String sql = "UPDATE SF_VER_INFO SET TOT_CNT = ? " +
				"WHERE SF_ID IN (SELECT A.SF_ID FROM SF_INFO A, SF_VER_INFO B WHERE A.SF_ID = B.SF_ID AND A.PROD_NAME = ? " +
				"AND A.SF_NAME = ? AND B.VER = ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, totalCorpusCount);
			ps.setString(2, productName);
			ps.setString(3, fileName);
			ps.setString(4, version);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
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
	
	public double getLengthScore(int sourceFileVersionID) {
		double lengthScore = INIT_LENGTH_SCORE;

		String sql = "SELECT LEN_SCORE " +
					"FROM SF_VER_INFO " +
					"WHERE SF_VER_ID = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, sourceFileVersionID);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				lengthScore = rs.getDouble("LEN_SCORE");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lengthScore;
	}
	
	public int insertCorpus(String corpus, String productName) {
		String sql = "INSERT INTO SF_WRD_INFO (WRD, PROD_NAME) VALUES (?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, corpus);
			ps.setString(2, productName);
			
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
	
	public int deleteAllWords() {
		String sql = "DELETE FROM SF_WRD_INFO";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public HashMap<String, Integer> getWordMap(String productName) {
		HashMap<String, Integer> fileInfo = new HashMap<String, Integer>();
		
		String sql = "SELECT WRD, SF_WRD_ID FROM SF_WRD_INFO WHERE PROD_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1,  productName);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				fileInfo.put(rs.getString("WRD"), rs.getInt("SF_WRD_ID"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileInfo;
	}
	
	public int getWordID(String word, String productName) {
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
	
	public int insertImportedClasses(String fileName, String productName, String version, ArrayList<String> importedClasses) {
		int sourceFileVersionID = this.getSourceFileVersionID(fileName, productName, version);
		
		String sql = "INSERT INTO SF_IMP_INFO (SF_VER_ID, IMP_CLASS) VALUES (?, ?)";
		int returnValue = INVALID;
		
		for (int i = 0; i < importedClasses.size(); i++) {
			try {
				String importedClass = importedClasses.get(i);
				ps = conn.prepareStatement(sql);
				ps.setInt(1, sourceFileVersionID);
				ps.setString(2, importedClass);
				
				returnValue = ps.executeUpdate();
			} catch (JdbcSQLException e) {
				e.printStackTrace();
				
				if (ErrorCode.DUPLICATE_KEY_1 != e.getErrorCode()) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (INVALID == returnValue) {
				break;
			}
		}
		
		return returnValue;
	}
	
	public int deleteAllImportedClasses() {
		String sql = "DELETE FROM SF_IMP_INFO";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public HashMap<String, ArrayList<String>> getAllImportedClasses(String productName, String version) {
		HashMap<String, ArrayList<String>> importedClassesMap = new HashMap<String, ArrayList<String>>();
		
		String sql = "SELECT A.SF_NAME, C.IMP_CLASS " +
					"FROM SF_INFO A, SF_VER_INFO B, SF_IMP_INFO C " +
					"WHERE A.SF_ID = B.SF_ID AND " +
					"B.SF_VER_ID = C.SF_VER_ID AND " +
					"A.PROD_NAME = ? AND B.VER = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, version);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				String sourceFilename = rs.getString("SF_NAME");
				if (importedClassesMap.containsKey(sourceFilename)) {
					ArrayList<String> importedClasses = importedClassesMap.get(sourceFilename);
					importedClasses.add(rs.getString("IMP_CLASS"));
				} else {
					ArrayList<String> importedClasses = new ArrayList<String>();
					importedClasses.add(rs.getString("IMP_CLASS"));
					
					importedClassesMap.put(sourceFilename, importedClasses);	
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return importedClassesMap;
	}
	
	public ArrayList<String> getImportedClasses(String productName, String version, String fileName) {
		ArrayList<String> importedClasses = null;
		
		String sql = "SELECT C.IMP_CLASS " +
					"FROM SF_INFO A, SF_VER_INFO B, SF_IMP_INFO C " +
					"WHERE A.SF_ID = B.SF_ID AND " +
					"B.SF_VER_ID = C.SF_VER_ID AND " +
					"A.PROD_NAME = ? AND B.VER = ? AND A.SF_NAME = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productName);
			ps.setString(2, version);
			ps.setString(3, fileName);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				if (null == importedClasses) {
					importedClasses = new ArrayList<String>();
				}
				
				importedClasses.add(rs.getString("IMP_CLASS"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return importedClasses;
	}
	
	public int insertSourceFileAnalysisValue(AnalysisValue analysisValue) {
		
		int fileVersionID = getSourceFileVersionID(analysisValue.getName(),
				analysisValue.getProductName(), analysisValue.getVersion());
		int wordID = getWordID(analysisValue.getWord(), analysisValue.getProductName());
		
		String sql = "INSERT INTO SF_VEC (SF_VER_ID, SF_WRD_ID, TERM_CNT, INV_DOC_CNT, TF, IDF, VEC) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, fileVersionID);
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
	
	public int updateSourceFileAnalysisValue(AnalysisValue analysisValue) {
		String sql = "UPDATE SF_VEC SET TERM_CNT = ?, INV_DOC_CNT = ?, TF = ?, IDF = ?, VEC = ? " +
				"WHERE SF_VER_ID = ? AND SF_WRD_ID = ?";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, analysisValue.getTermCount());
			ps.setInt(2, analysisValue.getInvDocCount());
			ps.setDouble(3, analysisValue.getTf());
			ps.setDouble(4, analysisValue.getIdf());
			ps.setDouble(5, analysisValue.getVector());
			ps.setInt(6, analysisValue.getSourceFileVersionID());
			ps.setInt(7, analysisValue.getWordsID());
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllAnalysisValues() {
		String sql = "DELETE FROM SF_VEC";
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
			String word) {
		AnalysisValue returnValue = null;

		String sql = "SELECT D.TERM_CNT, D.INV_DOC_CNT, D.TF, D.IDF, D.VEC "+
				"FROM SF_INFO A, SF_VER_INFO B, SF_WRD_INFO C, SF_VEC D " +
				"WHERE A.SF_NAME = ? AND A.PROD_NAME = ? AND A.SF_ID = B.SF_ID AND " +
				"B.VER = ? AND B.SF_VER_ID = D.SF_VER_ID AND C.WRD = ? AND " +
				"C.PROD_NAME = ? AND C.SF_WRD_ID = D.SF_WRD_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, fileName);
			ps.setString(2, productName);
			ps.setString(3, version);
			ps.setString(4, word);
			ps.setString(5, productName);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				returnValue = new AnalysisValue();
				
				returnValue.setName(fileName);
				returnValue.setProductName(productName);
				returnValue.setVersion(version);
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
	
	public HashMap<String, AnalysisValue> getSourceFileAnalysisValues(String productName, String fileName, String version) {
		HashMap<String, AnalysisValue> sourceFileAnalysisValues = null;

		String sql = "SELECT C.WRD, D.SF_VER_ID, D.SF_WRD_ID, D.TERM_CNT, D.INV_DOC_CNT, D.TF, D.IDF, D.VEC "+
				"FROM SF_INFO A, SF_VER_INFO B, SF_WRD_INFO C, SF_VEC D " +
				"WHERE A.SF_NAME = ? AND A.PROD_NAME = ? AND A.SF_ID = B.SF_ID AND " +
				"B.VER = ? AND B.SF_VER_ID = D.SF_VER_ID AND " +
				"C.PROD_NAME = ? AND C.SF_WRD_ID = D.SF_WRD_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, fileName);
			ps.setString(2, productName);
			ps.setString(3, version);
			ps.setString(4, productName);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == sourceFileAnalysisValues) {
					sourceFileAnalysisValues = new HashMap<String, AnalysisValue>();
				}
				AnalysisValue analysisValue = new AnalysisValue();
				
				String word = rs.getString("WRD");
				analysisValue.setName(fileName);
				analysisValue.setProductName(productName);
				analysisValue.setVersion(version);
				analysisValue.setWord(word);
				analysisValue.setSourceFileVersionID(rs.getInt("SF_VER_ID"));
				analysisValue.setWordID(rs.getInt("SF_WRD_ID"));
				analysisValue.setTermCount(rs.getInt("TERM_CNT"));
				analysisValue.setInvDocCount(rs.getInt("INV_DOC_CNT"));
				analysisValue.setTf(rs.getDouble("TF"));
				analysisValue.setIdf(rs.getDouble("IDF"));
				analysisValue.setVector(rs.getDouble("VEC"));
				
				sourceFileAnalysisValues.put(word, analysisValue);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sourceFileAnalysisValues;
	}
	
	public HashMap<String, AnalysisValue> getSourceFileAnalysisValues(int sourceFileVersionID) {
		HashMap<String, AnalysisValue> sourceFileAnalysisValues = null;

		String sql = "SELECT C.WRD, D.SF_VER_ID, D.SF_WRD_ID, D.TERM_CNT, D.INV_DOC_CNT, D.TF, D.IDF, D.VEC "+
				"FROM SF_WRD_INFO C, SF_VEC D " +
				"WHERE D.SF_VER_ID = ? AND C.SF_WRD_ID = D.SF_WRD_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, sourceFileVersionID);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == sourceFileAnalysisValues) {
					sourceFileAnalysisValues = new HashMap<String, AnalysisValue>();
				}
				AnalysisValue analysisValue = new AnalysisValue();
				
				String word = rs.getString("WRD");
				analysisValue.setWord(word);
				analysisValue.setSourceFileVersionID(rs.getInt("SF_VER_ID"));
				analysisValue.setWordID(rs.getInt("SF_WRD_ID"));
				analysisValue.setTermCount(rs.getInt("TERM_CNT"));
				analysisValue.setInvDocCount(rs.getInt("INV_DOC_CNT"));
				analysisValue.setTf(rs.getDouble("TF"));
				analysisValue.setIdf(rs.getDouble("IDF"));
				analysisValue.setVector(rs.getDouble("VEC"));
				
				sourceFileAnalysisValues.put(word, analysisValue);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return sourceFileAnalysisValues;
	}
}
