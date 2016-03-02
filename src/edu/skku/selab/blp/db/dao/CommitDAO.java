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

import edu.skku.selab.blp.common.CommitInfo;
import edu.skku.selab.blp.common.ExtendedCommitInfo;
import edu.skku.selab.blp.common.Method;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class CommitDAO extends BaseDAO {

	/**
	 * @throws Exception
	 */
	public CommitDAO() throws Exception {
		super();
	}
	
	public int insertCommitInfo(ExtendedCommitInfo extendedCommitInfo) {
		String sql = "INSERT INTO COMM_INFO (COMM_ID, COMM_DATE, MSG, COMMITTER) VALUES (?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			ps.setString(1, extendedCommitInfo.getCommitID());
			ps.setString(2, extendedCommitInfo.getCommitDateString());
			ps.setString(3, extendedCommitInfo.getMessage());
			ps.setString(4, extendedCommitInfo.getCommitter());
			
			returnValue = ps.executeUpdate();
			
			HashMap<Integer, HashSet<String>> allCommitFiles = extendedCommitInfo.getAllCommitFiles();
			Iterator<Integer> iter = allCommitFiles.keySet().iterator();
			
			while (iter.hasNext()) {
				int commitType = iter.next();
				Iterator<String> commitFilesIter = allCommitFiles.get(commitType).iterator();
				
				while (commitFilesIter.hasNext()) {
					String checkedInFileName = commitFilesIter.next();
					sql = "INSERT INTO COMM_SF_INFO (COMM_ID, COMM_SF, COMM_TYPE) VALUES (?, ?, ?)";
					
					ps = analysisDbConnection.prepareStatement(sql);
					ps.setString(1, extendedCommitInfo.getCommitID());
					ps.setString(2, checkedInFileName);
					ps.setInt(3, commitType);
					
					returnValue = ps.executeUpdate();
				}
			}
			
			HashMap<String, ArrayList<Method>> allFixedMethods = extendedCommitInfo.getAllFixedMethods();
			Iterator<String> allFixedMethodsIter = allFixedMethods.keySet().iterator();
			
			while (allFixedMethodsIter.hasNext()) {
				String fixedFile = allFixedMethodsIter.next();
				ArrayList<Method> fixedMethods = allFixedMethods.get(fixedFile);
				
				for (int i = 0; i < fixedMethods.size(); ++i) {
					Method method = fixedMethods.get(i);
					
					sql = "INSERT INTO COMM_MTH_INFO (COMM_ID, COMM_SF, COMM_MTH, COM_MTH_HASH_KEY) VALUES (?, ?, ?, ?)";
					
					ps = analysisDbConnection.prepareStatement(sql);
					ps.setString(1, extendedCommitInfo.getCommitID());
					ps.setString(2, fixedFile);
					ps.setString(3, method.getConcatenatedString());
					ps.setString(4, method.getHashKey());
					
					returnValue = ps.executeUpdate();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllCommitInfo() {
		String sql = "DELETE FROM COMM_INFO";
		int returnValue = INVALID;
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public HashMap<Integer, HashSet<String>> getCommitFiles(String commitID) {
		HashMap<Integer, HashSet<String>> allCommitFiles = null;

		String sql = "SELECT COMM_SF, COMM_TYPE FROM COMM_SF_INFO " + 
				"WHERE COMM_ID = ? ORDER BY COMM_TYPE";

		try {
			ps = analysisDbConnection.prepareStatement(sql);
			ps.setString(1, commitID);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == allCommitFiles) {
					allCommitFiles = new HashMap<Integer, HashSet<String>>();
				}

				int commitType = rs.getInt("COMM_TYPE");
				HashSet<String> commitFiles = allCommitFiles.get(commitType);
				if (null == commitFiles) {
					commitFiles = new HashSet<String>();
					allCommitFiles.put(commitType, commitFiles);
				}
				commitFiles.add(rs.getString("COMM_SF"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return allCommitFiles;
	}
	
	public HashMap<String, ArrayList<Method>> getCommitMethods(String commitID) {
		HashMap<String, ArrayList<Method>> allCommitMethods = null;

		String sql = "SELECT COMM_SF, COMM_MTH FROM COMM_MTH_INFO " + 
				"WHERE COMM_ID = ? ORDER BY COMM_SF";

		try {
			ps = analysisDbConnection.prepareStatement(sql);
			ps.setString(1, commitID);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == allCommitMethods) {
					allCommitMethods = new HashMap<String, ArrayList<Method>>();
				}

				String commitFile = rs.getString("COMM_SF");
				ArrayList<Method> commitMethods = allCommitMethods.get(commitFile);
				if (null == commitMethods) {
					commitMethods = new ArrayList<Method>();
					allCommitMethods.put(commitFile, commitMethods);
				}
				commitMethods.add(new Method(rs.getString("COMM_MTH")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return allCommitMethods;
	}
	
	public CommitInfo getCommitInfo(String commitID) {
		CommitInfo commitInfo = null;
		
		String sql = "SELECT COMM_DATE, MSG, COMMITTER FROM COMM_INFO " + 
				"WHERE COMM_ID = ?";
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			ps.setString(1, commitID);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				commitInfo = new CommitInfo();
				commitInfo.setCommitID(commitID);
				commitInfo.setCommitDate(rs.getTimestamp("COMM_DATE"));
				commitInfo.setMessage(rs.getString("MSG"));
				commitInfo.setCommitter(rs.getString("COMMITTER"));
				commitInfo.setCommitFiles(this.getCommitFiles(commitID));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return commitInfo;
	}
	
	public int getCommitInfoCount() {
		String sql = "SELECT count(COMM_ID) FROM COMM_INFO";
		
		int commitInfoCount = 0;
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			
			rs = ps.executeQuery();
			if (rs.next()) {
				commitInfoCount = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return commitInfoCount;
	}
	
	public ArrayList<CommitInfo> getAllCommitInfos() {
		ArrayList<CommitInfo> allCommitInfos = null;
		CommitInfo commitInfo = null;
		
		String sql = "SELECT COMM_ID, COMM_DATE, MSG, COMMITTER FROM COMM_INFO " + 
				"ORDER BY COMM_DATE";
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == allCommitInfos) {
					allCommitInfos = new ArrayList<CommitInfo>();
				}
				commitInfo = new CommitInfo();
				String commitID = rs.getString("COMM_ID");
				commitInfo.setCommitID(commitID);
				commitInfo.setCommitDate(rs.getTimestamp("COMM_DATE"));
				commitInfo.setMessage(rs.getString("MSG"));
				commitInfo.setCommitter(rs.getString("COMMITTER"));
				allCommitInfos.add(commitInfo);
			}
			
			for (int i = 0; i < allCommitInfos.size(); i++) {
				commitInfo = allCommitInfos.get(i);
				commitInfo.setCommitFiles(this.getCommitFiles(commitInfo.getCommitID()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return allCommitInfos;
	}
	
	public ArrayList<ExtendedCommitInfo> getCommitInfos(boolean filtered) {
		ArrayList<ExtendedCommitInfo> filteredCommitInfos = null;
		ExtendedCommitInfo commitInfo = null;
		
		String sql = "SELECT COMM_ID, COMM_DATE, MSG, COMMITTER FROM COMM_INFO " + 
				"ORDER BY COMM_DATE";
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == filteredCommitInfos) {
					filteredCommitInfos = new ArrayList<ExtendedCommitInfo>();
				}
				commitInfo = new ExtendedCommitInfo();
				String commitID = rs.getString("COMM_ID");
				commitInfo.setCommitID(commitID);
				commitInfo.setCommitDate(rs.getTimestamp("COMM_DATE"));
				commitInfo.setMessage(rs.getString("MSG"));
				commitInfo.setCommitter(rs.getString("COMMITTER"));
				
				if (filtered) {
					String pattern = "(?i)(.*fix.*)|(?i)(.*bug.*)";
			        Pattern r = Pattern.compile(pattern);
			        Matcher m = r.matcher(commitInfo.getMessage());

			        if (m.find()) {
			        	// debug code
//			        	System.out.printf("Commit Message: %s\n", commitInfo.getMessage());
						filteredCommitInfos.add(commitInfo);
			        }
				} else {
					filteredCommitInfos.add(commitInfo);
				}
			}
			
			for (int i = 0; i < filteredCommitInfos.size(); i++) {
				commitInfo = filteredCommitInfos.get(i);
				commitInfo.setCommitFiles(this.getCommitFiles(commitInfo.getCommitID()));
				commitInfo.setCommitMethodMap(this.getCommitMethods(commitInfo.getCommitID()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return filteredCommitInfos;
	}
	
	public int deleteAllCommitFileInfo() {
		String sql = "DELETE FROM COMM_SF_INFO";
		int returnValue = INVALID;
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public int deleteAllCommitMethodInfo() {
		String sql = "DELETE FROM COMM_MTH_INFO";
		int returnValue = INVALID;
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public ExtendedCommitInfo getFixedCommitInfo(String commitID) {
		CommitInfo commitInfo = getCommitInfo(commitID);
		ExtendedCommitInfo fixedCommitInfo = new ExtendedCommitInfo(commitInfo);
		
		String sql = "SELECT COMM_SF, COMM_MTH FROM COMM_MTH_INFO " + 
				"WHERE COMM_ID = ?";
		
		try {
			ps = analysisDbConnection.prepareStatement(sql);
			ps.setString(1, commitID);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				String fixedFile = rs.getString("COMM_SF");
				String fixedMethodInfo = rs.getString("COMM_MTH");
				Method fixedMethod = new Method(fixedMethodInfo);
				fixedCommitInfo.addFixedMethod(fixedFile, fixedMethod);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fixedCommitInfo;
	}
}
