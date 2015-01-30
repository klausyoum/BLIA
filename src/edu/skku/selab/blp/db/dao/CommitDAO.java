/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.db.dao;

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
		String sql = "INSERT INTO COMM_INFO (COMM_ID, COMM_DATE, DESC, PROD_NAME) VALUES (?, ?, ?, ?)";
		int returnValue = INVALID;
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, commitInfo.getCommitID());
			ps.setString(2, commitInfo.getCommitDateString());
			ps.setString(3, commitInfo.getDescription());
			ps.setString(4, commitInfo.getProductName());
			
			returnValue = ps.executeUpdate();
			
			Iterator<String> iter = commitInfo.getCommitFiles().iterator();
			
			while (iter.hasNext()) {
				String checkedInFileName = iter.next();
				SourceFileDAO sourceFileDao = new SourceFileDAO();
				int fileID = sourceFileDao.getSourceFileID(checkedInFileName, commitInfo.getProductName());

				sql = "INSERT INTO COMM_FILE_INFO (COMM_ID, SF_ID) VALUES (?, ?)";
				
				ps = conn.prepareStatement(sql);
				ps.setString(1, commitInfo.getCommitID());
				ps.setInt(2, fileID);
				
				returnValue = ps.executeUpdate();
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
	
	public CommitInfo getCommitInfo(String commitID) {
		CommitInfo commitInfo = null;
		
		String sql = "SELECT A.COMM_ID, A.PROD_NAME, A.COMM_DATE, A.DESC, C.SF_NAME FROM COMM_INFO A, COMM_FILE_INFO B, SF_INFO C " + 
				"WHERE A.COMM_ID = ? AND A.COMM_ID = B.COMM_ID AND B.SF_ID = C.SF_ID";
		
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, commitID);
			
			rs = ps.executeQuery();
			
			while (rs.next()) {
				if (null == commitInfo) {
					commitInfo = new CommitInfo();
					commitInfo.setCommitID(commitID);
					commitInfo.setProductName(rs.getString("PROD_NAME"));
					commitInfo.setCommitDate(rs.getTimestamp("COMM_DATE"));
					commitInfo.setDescription(rs.getString("DESC"));
				}

				commitInfo.addCommitFile(rs.getString("SF_NAME"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return commitInfo;
	}
}
