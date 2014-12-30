/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.indexer;

import java.util.TreeSet;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class Bug {

    private String ID;
	private String openDate;
    private String fixDate;
    private String summary;
    private String description;
    private TreeSet<String> fixedFiles;
    
    public Bug() {
    	this.ID = "";
    	this.openDate = "";
    	this.fixDate = "";
    	this.summary = "";
    	this.description = "";
    	this.fixedFiles = new TreeSet<String>();
    }
    
    public Bug(String ID, String openDate, String fixDate, String summary, String description, TreeSet<String> fixedFiles) {
    	this.ID = ID;
    	this.openDate = openDate;
    	this.fixDate = fixDate;
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
	
	public String getOpenDate() {
		return openDate;
	}
	
	public void setOpenDate(String openDate) {
		this.openDate = openDate;
	}
	
	public String getFixDate() {
		return fixDate;
	}
	
	public void setFixDate(String fixDate) {
		this.fixDate = fixDate;
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
}
