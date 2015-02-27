/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.test.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import lemurproject.indri.*;
import lemurproject.indri.IndexStatus.action_code;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class IndriTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	// for processing offset files while indexing...
	private Vector dataFilesOffsetFiles = null;
	
	// Utilities
	/**
	 * Rewrite a shell filename pattern to a regular expression. <br>
	 * * -&gt; .*<br>
	 * ? -&gt; .?<br>
	 * Add ^ to beginning<br>
	 * Add $ to end<br>
	 * . -&gt; \.<br>
	 * 
	 * @param regexp
	 *            the filename pattern, eg "*.dat"
	 * @return a regular expression suitable for use with String.matches(), eg
	 *         "^.*\.dat$"
	 */
	private String encodeRegexp(String regexp) {
		// rewrite shell fname pattern to regexp.
		// * -> .*
		// ? -> .?
		// Add ^,$
		// . -> \.
		String retval = "^" + regexp + "$";
		retval = retval.replaceAll("\\.", "\\.");
		retval = retval.replaceAll("\\*", ".*");
		retval = retval.replaceAll("\\?", ".?");
		return retval;
	}
	
	/**
	 * Create the datafiles list of strings.
	 * 
	 * @return The list of files
	 */
	private String[] formatDataFiles() {
		// handle directories, recursion, filename patterns
		Vector accumulator = new Vector();
		String[] retval = new String[0];

		dataFilesOffsetFiles = new Vector();

		FileFilter filt = null;
		final String regexp = "*.xml";
		if (regexp.length() > 0) {
			final String filtString = encodeRegexp(regexp);
			filt = new FileFilter() {
				public boolean accept(File thisfile) {
					String name = thisfile.getName();
					return (thisfile.isDirectory() || name.matches(filtString));
				}
			};
		}

//		HashMap offsetFiles = offsetAnnotationFilesTableModel.getAllValues();

		String s = "C:\\Users\\Klaus\\Dropbox\\Workspace\\testSource";
		File file = new File(s);

		String thisOffsetFile = "";
//		if (offsetFiles.containsKey(s)) {
//			thisOffsetFile = (String) offsetFiles.get(s);
//		}
		formatDataFiles(file, filt, accumulator, thisOffsetFile);

		retval = (String[]) accumulator.toArray(retval);
		return retval;
	}
	
	/**
	 * Accumulate filenames for the input list. If the File is a directory,
	 * iterate over all of the files in that directory that satisfy the filter.
	 * If recurse into subdirectories is selected and the File is a directory,
	 * invoke recursivly on on all directories within the directory.
	 * 
	 * @param accum
	 *            Vector to accumulate file names recusively.
	 * @param file
	 *            a File (either a file or directory)
	 * @param f
	 *            the filename filter to apply.
	 */

	private void formatDataFiles(File file, FileFilter f, Vector accum,
			String offsetFile) {
		if (file.isDirectory()) {
			// handle directory
			File[] files = file.listFiles(f);
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
//					if (doRecurse.isSelected()) {
					if (true) {
						formatDataFiles(files[i], f, accum, offsetFile);
					}
				} else {
					accum.add(files[i].getAbsolutePath());
					if (dataFilesOffsetFiles != null) {
						dataFilesOffsetFiles.add(offsetFile);
					}
				}
			}
		} else {
			accum.add(file.getAbsolutePath());
			if (dataFilesOffsetFiles != null) {
				dataFilesOffsetFiles.add(offsetFile);
			}

		}
	}

	@Test
	public void testBuildIndex() throws Exception {
		int totalDocumentsIndexed = 0;
		
		IndexEnvironment env = new IndexEnvironment();
		IndexStatus stat = new UIIndexStatus();
		
		env.setMemory(1024000000);
		
		String[] fields = {"class", "method", "variable", "comments"};
		env.setIndexedFields(fields);
		
		String[] metafields = { "docno" };
		env.setMetadataIndexedFields(metafields, metafields);
		
		String[] stopWords = {"a", "the"};
		env.setStopwords(stopWords);
		env.setStemmer("krovetz");
		
		String fileClass = "trectext";
		Specification spec = env.getFileClassSpec(fileClass);
		
		java.util.Vector vec = new java.util.Vector();
		java.util.Vector incs = null;
		if (spec.include.length > 0)
			incs = new java.util.Vector();

		// indexed fields
		for (int i = 0; i < spec.index.length; i++) {
			vec.add(spec.index[i]);
		}
		for (int i = 0; i < fields.length; i++) {
			if (vec.indexOf(fields[i]) == -1) {
				vec.add(fields[i]);
			}
			// add to include tags only if there were some already.
			if (incs != null && incs.indexOf(fields[i]) == -1) {
				incs.add(fields[i]);
			}
		}

		if (vec.size() > spec.index.length) {
			// we added something.
			spec.index = new String[vec.size()];
			vec.copyInto(spec.index);
		}
		/*
		 * FIXME: forward/backward and plain metadata have to address the
		 * issue of inserting entries for all names that conflate to a given
		 * name.
		 */
		// metadata fields.
		vec.clear();
		for (int i = 0; i < spec.metadata.length; i++)
			vec.add(spec.metadata[i]);
		
		for (int i = 0; i < metafields.length; i++) {
			if (vec.indexOf(metafields[i]) == -1)
				vec.add(metafields[i]);
			// add to include tags only if there were some already.
			if (incs != null && incs.indexOf(metafields[i]) == -1)
				incs.add(metafields[i]);
		}

		if (vec.size() > spec.metadata.length) {
			// we added something.
			spec.metadata = new String[vec.size()];
			vec.copyInto(spec.metadata);
		}
		// update include if needed.
		if (incs != null && incs.size() > spec.include.length) {
			spec.include = new String[incs.size()];
			incs.copyInto(spec.include);
		}
//		// update the environment.
		env.addFileClass(spec);
		
		String[] datafiles = formatDataFiles();
		
		String indexName = "C:\\Users\\Klaus\\Dropbox\\Workspace\\test-index";
		env.create(indexName, stat);
		
		for (int i = 0; i < datafiles.length; i++) {
			String fname = datafiles[i];
			// if the fileClass is null, use
//			env.addFile(fname);

			env.addFile(fname, fileClass);
			totalDocumentsIndexed = env.documentsIndexed();
		}
		env.close();
	}
	
	/**
	 * Strip leading pathname, if any.
	 */
	private String trim(String s) {
		File f = new File(s);
		return f.getName();
	}
	
	@Test
	public void testQueryIndex() throws Exception {
		String index = "C:\\Users\\Klaus\\Dropbox\\Workspace\\test-index";
		QueryEnvironment env = new QueryEnvironment();

		env.addIndex(index);
		env.setMemory(1024000000);
		
		int maxDocs = 100;
		
		String question = "#combine[class](DB)";
		
		try {
			ScoredExtentResult[] scored = env.runQuery(question, maxDocs);
			
			String[] names = env.documentMetadata(scored, "docno");
			for (int i = 0; i < scored.length; i++) {
				System.out.printf("FileName: %s, Score: %f\n", trim(names[i]), scored[i].score);
			}
		} catch (Exception exc2) {
			System.err.println("No results: " + exc2.toString());
		}
	}
	
	class UIIndexStatus extends IndexStatus {
		public void status(int code, String documentFile, String error,
				int documentsIndexed, int documentsSeen) {
			String messages = "";
			if (code == action_code.FileOpen.swigValue()) {
				messages += "Documents: " + documentsIndexed + "\n";
				messages += "Opened " + documentFile + "\n";
			} else if (code == action_code.FileSkip.swigValue()) {
				messages += "Skipped " + documentFile + "\n";
			} else if (code == action_code.FileError.swigValue()) {
				messages += "Error in " + documentFile + " : " + error + "\n";
			} else if (code == action_code.DocumentCount.swigValue()) {
				if ((documentsIndexed % 500) == 0)
					messages += "Documents: " + documentsIndexed + "\n";
			}

			System.out.print(messages);
		}
	}


}
