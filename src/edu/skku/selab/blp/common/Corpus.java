/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.common;

import java.util.ArrayList;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class Corpus {
	private String javaFileFullClassName;
	private String javaFilePath;
	private String content;
	private ArrayList<String> importedClasses;

	public Corpus() {
	}

	public String getJavaFileFullClassName() {
		return javaFileFullClassName;
	}

	public void setJavaFileFullClassName(String javaFileFullClassName) {
		this.javaFileFullClassName = javaFileFullClassName;
	}

	public String getJavaFilePath() {
		return javaFilePath;
	}

	public void setJavaFilePath(String javaFilePath) {
		this.javaFilePath = javaFilePath;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the importedClasses
	 */
	public ArrayList<String> getImportedClasses() {
		return importedClasses;
	}

	/**
	 * @param importedClasses the importedClasses to set
	 */
	public void setImportedClasses(ArrayList<String> importedClasses) {
		this.importedClasses = importedClasses;
	}
}
