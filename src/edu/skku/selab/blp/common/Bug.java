/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class Bug {

    private String ID;
    private String productName;
	private String openDateString;
    private String fixedDateString;
    private Date fixedDate;
    private String summary;
    private String description;
    private TreeSet<String> fixedFiles;
    private String corpuses;
    private int	totalCorpusCount;
    private String stackTraces;
    
    public Bug() {
    	this.ID = "";
    	this.openDateString = "";
    	this.fixedDateString = "";
    	this.summary = "";
    	this.description = "";
    	this.fixedFiles = new TreeSet<String>();
    }
    
    public Bug(String ID, String openDateString, String fixedDateString, String summary, String description, TreeSet<String> fixedFiles) {
    	this.ID = ID;
    	this.productName = "";
    	this.openDateString = openDateString;
    	this.fixedDateString = fixedDateString;
    	this.summary = summary;
    	this.description = description;
    	this.fixedFiles = fixedFiles;
    }
    
    public Bug(String ID, String productName, String openDateString, String fixedDateString, String summary, String description, TreeSet<String> fixedFiles) {
    	this.ID = ID;
    	this.productName = productName;
    	this.openDateString = openDateString;
    	this.fixedDateString = fixedDateString;
    	this.summary = summary;
    	this.description = description;
    	this.fixedFiles = fixedFiles;
    }

    public String getID() {
		return ID;
	}
    
	public void setID(String ID) {
		this.ID = ID;
	}
	
	public String getOpenDateString() {
		return openDateString;
	}
	
	public void setOpenDateString(String openDateString) {
		this.openDateString = openDateString;
	}
	
	public String getFixedDateString() {
		return fixedDateString;
	}
	
	public void setFixedDateString(String fixDateString) {
		this.fixedDateString = fixDateString;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			this.fixedDate = simpleDateFormat.parse(fixDateString);			
		} catch (Exception e) {
			this.fixedDate = null;
			e.printStackTrace();
		}

	}
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public TreeSet<String> getFixedFiles() {
		return fixedFiles;
	}
	
	public void setFixedFiles(TreeSet<String> fixedFiles) {
		this.fixedFiles = fixedFiles;
	}    

	public void addFixedFile(String fixedFile) {
		this.fixedFiles.add(fixedFile);
	}

	/**
	 * @return the corpuses
	 */
	public String getCorpuses() {
		return corpuses;
	}

	/**
	 * @param corpuses the corpuses to set
	 */
	public void setCorpuses(String corpuses) {
		this.corpuses = corpuses;
	}

	/**
	 * @return the stackTraces
	 */
	public String getStackTraces() {
		return stackTraces;
	}

	/**
	 * @param stackTraces the stackTraces to set
	 */
	public void setStackTraces(String stackTraces) {
		this.stackTraces = stackTraces;
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

	/**
	 * @param fixedDate the fixedDate to set
	 */
	public void setFixedDate(Date fixedDate) {
		this.fixedDate = fixedDate;
		this.fixedDateString = this.fixedDate.toString();
	}

	/**
	 * @return the fixedDate
	 */
	public Date getFixedDate() {
		return fixedDate;
	}

	/**
	 * @return the totalCorpusCount
	 */
	public int getTotalCorpusCount() {
		return totalCorpusCount;
	}

	/**
	 * @param totalCorpusCount the totalCorpusCount to set
	 */
	public void setTotalCorpusCount(int totalCorpusCount) {
		this.totalCorpusCount = totalCorpusCount;
	}    
}
