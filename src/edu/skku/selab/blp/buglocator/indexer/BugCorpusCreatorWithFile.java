/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.buglocator.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.skku.selab.blp.Property;
import edu.skku.selab.blp.common.Bug;
import edu.skku.selab.blp.db.dao.BugDAO;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.utils.Splitter;
import edu.skku.selab.blp.utils.Stem;
import edu.skku.selab.blp.utils.Stopword;

/**
 * Create each bug corpus from each bug report.  
 * 
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugCorpusCreatorWithFile {
	
	final private String SORTED_BUG_ID_FILE = "SortedId.txt";
	final private String FIXED_SOURCE_FILE_LINK = "FixLink.txt";

	/* (non-Javadoc)
	 * @see edu.skku.selab.blia.indexer.ICorpus#create()
	 */
	public void create() throws IOException {
		Property property = Property.getInstance();
		String dirPath = (new StringBuilder(String.valueOf(property.getWorkDir())))
				.append(property.getSeparator())
				.append("BugCorpus")
				.append(property.getSeparator()).toString();
		File file = new File(dirPath);
		if (!file.exists())
			file.mkdir();

		ArrayList<Bug> list = parseXML();
		property.setBugReportCount(list.size());
		
		Bug bug;
		for (Iterator<Bug> iterator = list.iterator(); iterator.hasNext(); ) {
			bug = (Bug) iterator.next();
			writeCorpus(bug, dirPath);
		}

		
		FileWriter sortedBugIdWriter = new FileWriter((new StringBuilder(String.valueOf(property.getWorkDir())))
				.append(property.getSeparator())
				.append(SORTED_BUG_ID_FILE).toString());
		FileWriter fixedSourceFileLinkWriter = new FileWriter((new StringBuilder(String.valueOf(property.getWorkDir())))
				.append(property.getSeparator())
				.append(FIXED_SOURCE_FILE_LINK).toString());
		
		for (Iterator<Bug> iterator1 = list.iterator(); iterator1.hasNext();) {
			bug = (Bug) iterator1.next();
			sortedBugIdWriter.write((new StringBuilder(String.valueOf(bug.getID())))
					.append("\t").append(bug.getFixedDateString())
					.append(property.getLineSeparator())
					.toString());
			sortedBugIdWriter.flush();
			
			TreeSet<String> fixedFiles = bug.getFixedFiles();
			for (Iterator<String> iterator2 = fixedFiles.iterator(); iterator2.hasNext(); fixedSourceFileLinkWriter.flush()) {
				String fixName = (String) iterator2.next();
				fixedSourceFileLinkWriter.write((new StringBuilder(String.valueOf(bug.getID())))
						.append("\t").append(fixName)
						.append(property.getLineSeparator())
						.toString());
			}
		}

		sortedBugIdWriter.close();
		fixedSourceFileLinkWriter.close();
	}
	
	protected void writeCorpus(Bug bug, String storeDir) throws IOException {
		String content = (new StringBuilder(String.valueOf(bug.getSummary())))
				.append(" ").append(bug.getDescription()).toString();
		String splitWords[] = Splitter.splitNatureLanguage(content);
		StringBuffer corpus = new StringBuffer();
		String as[];
		int j = (as = splitWords).length;
		for (int i = 0; i < j; i++) {
			String word = as[i];
			word = Stem.stem(word.toLowerCase());
			if (!Stopword.isEnglishStopword(word))
				corpus.append((new StringBuilder(String.valueOf(word))).append(" ").toString());
		}

		FileWriter writer = new FileWriter((new StringBuilder(
				String.valueOf(storeDir))).append(bug.getID())
				.append(".txt").toString());
		writer.write(corpus.toString().trim());
		writer.flush();
		writer.close();
	}

	private ArrayList<Bug> parseXML() {
		ArrayList<Bug> list = new ArrayList<Bug>();
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		Property property = Property.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			InputStream is = new FileInputStream(property.getBugFilePath());
			Document doc = domBuilder.parse(is);
			Element root = doc.getDocumentElement();
			NodeList bugRepository = root.getChildNodes();
			if (null != bugRepository) {
				for (int i = 0; i < bugRepository.getLength(); i++) {
					Node bugNode = bugRepository.item(i);
					if (bugNode.getNodeType() == 1) {
						String bugId = bugNode.getAttributes().getNamedItem("id").getNodeValue();
						String openDateString = bugNode.getAttributes().getNamedItem("opendate").getNodeValue();
						String fixDateString = bugNode.getAttributes().getNamedItem("fixdate").getNodeValue();
						Bug bug = new Bug();
						bug.setID(bugId);
						bug.setOpenDateString(openDateString);
						bug.setFixedDate(simpleDateFormat.parse(fixDateString));
						for (Node node = bugNode.getFirstChild(); node != null; node = node.getNextSibling()) {
							if (node.getNodeType() == 1) {
								if (node.getNodeName().equals("buginformation")) {
									NodeList _l = node.getChildNodes();
									for (int j = 0; j < _l.getLength(); j++) {
										Node _n = _l.item(j);
										if (_n.getNodeName().equals("summary")) {
											String summary = _n.getTextContent();
											bug.setSummary(summary);
										}
										if (_n.getNodeName().equals("description")) {
											String description = _n.getTextContent();
											bug.setDescription(description);
										}
									}
								}
								if (node.getNodeName().equals("fixedFiles")) {
									NodeList _l = node.getChildNodes();
									for (int j = 0; j < _l.getLength(); j++) {
										Node _n = _l.item(j);
										if (_n.getNodeName().equals("file")) {
											String fileName = _n.getTextContent();
											
											String checkingString = "org.aspectj/modules/"; 
											if (fileName.contains(checkingString)) {
												fileName = fileName.substring(checkingString.length(), fileName.length());
												fileName = fileName.replace('/', '.');
												
												int index = fileName.lastIndexOf("org.");
												if (index > 0) {
													fileName = fileName.substring(index, fileName.length());
												}
												
												System.err.printf("fixed file name: %s\n", fileName);
											}

											bug.addFixedFile(fileName);
										}
									}
								}
							}
						}
						
						list.add(bug);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}
}
