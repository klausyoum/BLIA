/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.indexer;

import java.io.File;
import java.util.TreeSet;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.common.Corpus;
import edu.skku.selab.blp.common.FileDetector;
import edu.skku.selab.blp.common.FileParser;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.utils.Stem;
import edu.skku.selab.blp.utils.Stopword;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class SourceFileCorpusCreator {

	public Corpus create(File file) {
		FileParser parser = new FileParser(file);
		String fileName = parser.getPackageName();
		if (fileName.trim().equals("")) {
			fileName = file.getName();
		} else {
			fileName = (new StringBuilder(String.valueOf(fileName)))
					.append(".").append(file.getName()).toString();
		}
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		String content[] = parser.getContent();
		StringBuffer contentBuf = new StringBuffer();
		String as[];
		int j = (as = content).length;
		for (int i = 0; i < j; i++) {
			String word = as[i];
			String stemWord = Stem.stem(word.toLowerCase());
			if (!Stopword.isKeyword(word) && !Stopword.isEnglishStopword(word)) {
				contentBuf.append(stemWord);
				contentBuf.append(" ");
			}
		}

		String sourceCodeContent = contentBuf.toString();
		String classNameAndMethodName[] = parser.getClassNameAndMethodName();
		StringBuffer nameBuf = new StringBuffer();
		String as1[];
		int l = (as1 = classNameAndMethodName).length;
		for (int k = 0; k < l; k++) {
			String word = as1[k];
			String stemWord = Stem.stem(word.toLowerCase());
			nameBuf.append(stemWord);
			nameBuf.append(" ");
		}

		String names = nameBuf.toString();
		Corpus corpus = new Corpus();
		corpus.setJavaFilePath(file.getAbsolutePath());
		corpus.setJavaFileFullClassName(fileName);
		corpus.setContent((new StringBuilder(String.valueOf(sourceCodeContent)))
				.append(" ").append(names).toString());
		return corpus;
    }
	
	////////////////////////////////////////////////////////////////////	
	/* (non-Javadoc)
	 * @see edu.skku.selab.blia.indexer.ICorpus#create()
	 */
	public void create(String version) throws Exception {
		Property property = Property.getInstance();
		FileDetector detector = new FileDetector("java");
		File files[] = detector.detect(property.getSourceCodeDir());
		
		SourceFileDAO sourceFileDAO = new SourceFileDAO();
		String productName = property.getProductName();
		int totalCoupusCount = SourceFileDAO.INIT_TOTAL_COUPUS_COUNT;
		double lengthScore = SourceFileDAO.INIT_LENGTH_SCORE;

		int count = 0;
		TreeSet<String> nameSet = new TreeSet<String>();
		File afile[];
		int j = (afile = files).length;
		for (int i = 0; i < j; i++) {
			File file = afile[i];
			Corpus corpus = create(file);
			if (corpus != null && !nameSet.contains(corpus.getJavaFileFullClassName())) {
				String fileName = corpus.getJavaFileFullClassName();
				if (corpus.getJavaFileFullClassName().endsWith(".java")) {
				} else {
					fileName += ".java";
				}
				String corpusSet = corpus.getContent();
				sourceFileDAO.insertSourceFile(fileName, productName);
				sourceFileDAO.insertCorpusSet(fileName, productName, version, corpusSet, totalCoupusCount, lengthScore);
				nameSet.add(corpus.getJavaFileFullClassName());
				count++;
			}
		}

		property.setFileCount(count);
	}
}
