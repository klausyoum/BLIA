/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class ExtendedCommitInfo extends CommitInfo {
	private HashMap<String, ArrayList<Method>> commitMethodMap;
	
	/**
	 * 
	 */
	public ExtendedCommitInfo() {
		super();
		setCommitMethodMap(new HashMap<String, ArrayList<Method>>());
	}
	
	public ExtendedCommitInfo(CommitInfo commitInfo) {
		this.setCommitID(commitInfo.getCommitID());
		this.setCommitDate(commitInfo.getCommitDate());
		this.setMessage(commitInfo.getMessage());
		this.setCommitter(commitInfo.getCommitter());
		this.setCommitFiles(commitInfo.getAllCommitFiles());
		
		setCommitMethodMap(new HashMap<String, ArrayList<Method>>());
	}
	
	public void addFixedMethod(String fixedFile, Method fixedMethod) {
		if (!getCommitMethodMap().containsKey(fixedFile)) {
			ArrayList<Method> fixedMethodList = new ArrayList<Method>();
			fixedMethodList.add(fixedMethod);
			getCommitMethodMap().put(fixedFile, fixedMethodList);
		} else {
			ArrayList<Method> fixedMethodList = getCommitMethodMap().get(fixedFile);
			fixedMethodList.add(fixedMethod);
		}
	}

	public ArrayList<Method> getFixedMethodList(String fixedFile) {
		return commitMethodMap.get(fixedFile);
	}
	
	public HashMap<String, ArrayList<Method>> getAllFixedMethods() {
		return commitMethodMap;
	}

	/**
	 * @return the fixedMethodMap
	 */
	public HashMap<String, ArrayList<Method>> getCommitMethodMap() {
		return commitMethodMap;
	}

	/**
	 * @param commitMethodMap the commitMethodMap to set
	 */
	public void setCommitMethodMap(HashMap<String, ArrayList<Method>> commitMethodMap) {
		this.commitMethodMap = commitMethodMap;
	}
}
