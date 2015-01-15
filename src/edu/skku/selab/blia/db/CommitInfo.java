/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class CommitInfo {
	private String commitID;
	private String productName;
    private String commitDateString;
    private Date commitDate;
    private String description;
    private HashSet<String> commitFiles;

	/**
	 * 
	 */
	public CommitInfo() {
		commitID = "";
		commitDateString = "";
		commitDate = null;
		description = "";
		commitFiles = new HashSet<String>();
	}

	/**
	 * @return the commitID
	 */
	public String getCommitID() {
		return commitID;
	}

	/**
	 * @param commitID the commitID to set
	 */
	public void setCommitID(String commitID) {
		this.commitID = commitID;
	}

	/**
	 * @return the commitDateString
	 */
	public String getCommitDateString() {
		return commitDateString;
	}

	/**
	 * @param commitDateString the commitDateString to set
	 */
	public void setCommitDateString(String commitDateString) {
		this.commitDateString = commitDateString;
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			this.commitDate = simpleDateFormat.parse(commitDateString);			
		} catch (Exception e) {
			this.commitDate = null;
			e.printStackTrace();
		}		
	}

	/**
	 * @return the checkedInDate
	 */
	public Date getCommitDate() {
		return commitDate;
	}

	/**
	 * @param commitDate the commitDate to set
	 */
	public void setCommitDate(Date commitDate) {
		this.commitDate = commitDate;
		this.commitDateString = this.commitDate.toString();
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the sourceFileName
	 */
	public HashSet<String> getCommitFiles() {
		return commitFiles;
	}

	/**
	 * @param commitFiles the commitFiles to set
	 */
	public void setCommitFiles(HashSet<String> commitFiles) {
		this.commitFiles = commitFiles;
	}
	
	public void addCommitFile(String fileName) {
		this.commitFiles.add(fileName);
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

}
