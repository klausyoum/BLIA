/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import edu.skku.selab.blp.db.CommitInfo;

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
	
	
	public int insertCommitInfo(CommitInfo commitInfo) {
		String sql = "INSERT INTO COMM_INFO (COMM_ID, COMM_DATE, MSG, COMMITTER, PROD_NAME) VALUES (?, ?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, commitInfo.getCommitID());
			ps.setString(2, commitInfo.getCommitDateString());
			ps.setString(3, commitInfo.getMessage());
			ps.setString(4, commitInfo.getCommitter());
			ps.setString(5, commitInfo.getProductName());
			
			returnValue = ps.executeUpdate();
			
			HashMap<Integer, HashSet<String>> allCommitFiles = commitInfo.getAllCommitFiles();
			Iterator<Integer> iter = allCommitFiles.keySet().iterator();
			
			while (iter.hasNext()) {
				int commitType = iter.next();
				Iterator<String> commitFilesIter = allCommitFiles.get(commitType).iterator();
				
				while (commitFilesIter.hasNext()) {
					String checkedInFileName = commitFilesIter.next();
					sql = "INSERT INTO COMM_FILE_INFO (COMM_ID, COMM_FILE, COMM_TYPE) VALUES (?, ?, ?)";
					
					ps = conn.prepareStatement(sql);
					ps.setString(1, commitInfo.getCommitID());
					ps.setString(2, checkedInFileName);
					ps.setInt(3, commitType);
					
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
			ps = conn.prepareStatement(sql);
			
			returnValue = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return returnValue;
	}
	
	public HashMap<Integer, HashSet<String>> getCommitFiles(String commitID) {
		HashMap<Integer, HashSet<String>> allCommitFiles = null;

		String sql = "SELECT COMM_FILE, COMM_TYPE FROM COMM_FILE_INFO " + 
				"WHERE COMM_ID = ? ORDER BY COMM_TYPE";

		try {
			ps = conn.prepareStatement(sql);
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
				commitFiles.add(rs.getString("COMM_FILE"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return allCommitFiles;
	}
	
	public CommitInfo getCommitInfo(String commitID) {
		CommitInfo commitInfo = null;
		
		String sql = "SELECT PROD_NAME, COMM_DATE, MSG, COMMITTER FROM COMM_INFO " + 
				"WHERE COMM_ID = ?";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, commitID);
			
			rs = ps.executeQuery();
			
			if (rs.next()) {
				commitInfo = new CommitInfo();
				commitInfo.setCommitID(commitID);
				commitInfo.setProductName(rs.getString("PROD_NAME"));
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
}
