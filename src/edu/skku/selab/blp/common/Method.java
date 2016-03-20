/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class Method {
    private int ID;
	private int sourceFileVersionID;
    private String name;
    private String returnType;
    private String params;
    private String hashKey;
    
    public Method() {
    	this.setID(0);
    	this.setSourceFileVersionID(-1);
    	this.setName("");
    	this.setReturnType("");
    	this.setParams("");
    	this.setHashKey("");
    }
    
    public Method(String concatenatedMethodInfo) {
    	this.setID(0);
    	this.setSourceFileVersionID(-1);
    	
    	String splitLines[] = concatenatedMethodInfo.split("\\|");
    	this.setName(splitLines[0]);
    	this.setReturnType("");
    	this.setParams("");
    	if (splitLines.length > 1) {
        	this.returnType = splitLines[1];
        	
        	this.params = (splitLines.length < 3) ? "" : splitLines[2];
    	}
    	this.setHashKey(calculateMD5(name + " " + returnType + " " + params));
    }
    
    public Method(String name, String returnType, String params) {
    	this.setID(0);
    	this.setSourceFileVersionID(-1);

    	this.name = name;
    	this.returnType = returnType;
    	this.params = params;
    	this.setHashKey(calculateMD5(name + " " + returnType + " " + params));
    }
    
    public Method(int sourceFileVersionID, String name, String returnType, String params) {
    	this.setID(0);
    	this.setSourceFileVersionID(sourceFileVersionID);

    	this.name = name;
    	this.returnType = returnType;
    	this.params = params;
    	this.setHashKey(calculateMD5(name + " " + returnType + " " + params));
    }
    
    public Method(int methodID, int sourceFileVersionID, String name, String returnType, String params, String hashKey) {
    	this.setID(methodID);
    	this.setSourceFileVersionID(sourceFileVersionID);

    	this.name = name;
    	this.returnType = returnType;
    	this.params = params;
    	this.setHashKey(hashKey);
    }
    
    private String calculateMD5(String str){
    	String MD5 = ""; 
    	try{
    		MessageDigest md = MessageDigest.getInstance("MD5"); 
    		md.update(str.getBytes()); 
    		byte byteData[] = md.digest();
    		StringBuffer sb = new StringBuffer(); 
    		for(int i = 0 ; i < byteData.length ; i++){
    			sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
    		}
    		MD5 = sb.toString();
    		
    	}catch(NoSuchAlgorithmException e){
    		e.printStackTrace(); 
    		MD5 = null; 
    	}
    	return MD5;
    }

	/**
	 * @return the ID
	 */
	public int getID() {
		return ID;
	}

	/**
	 * @param ID the ID to set
	 */
	public void setID(int ID) {
		this.ID = ID;
	}

	/**
	 * @return the sourceFileVersionID
	 */
	public int getSourceFileVersionID() {
		return sourceFileVersionID;
	}

	/**
	 * @param sourceFileVersionID the sourceFileVersionID to set
	 */
	public void setSourceFileVersionID(int sourceFileVersionID) {
		this.sourceFileVersionID = sourceFileVersionID;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the returnType
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * @param returnType the returnType to set
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * @return the argTypes
	 */
	public String getParams() {
		return params;
	}

	/**
	 * @param params the argTypes to set
	 */
	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * @return the hashKey
	 */
	public String getHashKey() {
		return hashKey;
	}

	/**
	 * @param hashKey the hashKey to set
	 */
	public void setHashKey(String hashKey) {
		this.hashKey = hashKey;
	}
	
	public String getConcatenatedString() {
		return name + "|" + returnType + "|" + params;
	}
	
	public boolean equals(Object obj) {
		Method targetMethod = (Method) obj;
		return (this.getName().equals(targetMethod.getName()) &&
				this.getReturnType().equals(targetMethod.getReturnType()) &&
				this.getParams().equals(targetMethod.getParams()));
	}
}
