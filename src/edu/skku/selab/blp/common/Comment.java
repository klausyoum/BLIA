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

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class Comment {
    private int ID;
	private Date commentedDate;
    private String author;
    private String commentCorpus;
    
    public Comment() {
    	this.ID = 0;
    	this.commentedDate = new Date(System.currentTimeMillis());
    	this.author = "";
    	this.commentCorpus = "";
    }
    
    public Comment(int ID, String commentedDateString, String author, String commentCorpus) {
    	this.ID = ID;
    	setCommentedDate(commentedDateString);
    	this.author = author;
    	this.setCommentCorpus(commentCorpus);
    }
    
    public Comment(int ID, Date commentedDate, String author, String commentCorpus) {
    	this.ID = ID;
    	setCommentedDate(commentedDate);
    	this.author = author;
    	this.setCommentCorpus(commentCorpus);
    }
    
	public int getID() {
		return ID;
	}
	
	public void setID(int ID) {
		this.ID = ID;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public Date getCommentedDate() {
		return commentedDate;
	}
	
	public void setCommentedDate(Date commentedDate) {
		this.commentedDate = commentedDate;
	}
	
	public String getCommentedDateString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return simpleDateFormat.format(commentedDate);
	}
	
	public void setCommentedDate(String commentedDate) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			this.commentedDate = simpleDateFormat.parse(commentedDate);			
		} catch (Exception e) {
			this.commentedDate = null;
			e.printStackTrace();
		}
	}
	
	public String getCommentCorpus() {
		return commentCorpus;
	}
	
	public void setCommentCorpus(String commentCorpus) {
		this.commentCorpus = commentCorpus;
	}

}
