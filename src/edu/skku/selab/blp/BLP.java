/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp;

import java.io.File;


import edu.skku.selab.blp.buglocator.analysis.BugLocatorWithFile;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BLP {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (0 == args.length) {
			showUsage();
		} else {
			boolean areVaildArgs = parseArgs(args);
			if (areVaildArgs) {
				// test code to check validity of arguments
				// printProperty();

				BugLocatorWithFile bugLocator = new BugLocatorWithFile();
				bugLocator.analyze();
			}
		}
	}

	private static void showUsage() {
		String usage = "Usage:java -jar BLIA [-options]\r\n"
				+ "where options must include:\r\n"
				+ "-p\tindicates the product name\r\n"
				+ "-u\tindicates the bug information files directory\r\n"
				+ "-s\tindicates the source code directory\r\n"
				+ "-a\tindicates the alpha value for combining source similarity score and bug report similarity score\r\n"
				+ "-b\tindicates the beta value for combining relevant commits history score\r\n"
				+ "-o\tindicates the result file";
		System.out.println(usage);
	}

	private static boolean parseArgs(String args[]) {
		String productName = "";
		String bugFileDir = "";
		String sourceCodeDir = "";
		String alphaStr = "";
		float alpha = 0.0F;
		String betaStr = "";
		float beta = 0.0F;
		String outputFile = "";

		for (int i = 0; i < args.length - 1; i++)
			if (args[i].equals("-p")) {
				i++; // to move next actual argument
				productName = args[i];
			} else if (args[i].equals("-u")) {
				i++; // to move next actual argument
				bugFileDir = args[i];
			} else if (args[i].equals("-s")) {
				i++; // to move next actual argument
				sourceCodeDir = args[i];
			} else if (args[i].equals("-a")) {
				i++; // to move next actual argument
				alphaStr = args[i];
			} else if (args[i].equals("-b")) {
				i++; // to move next actual argument
				betaStr = args[i];
			} else if (args[i].equals("-o")) {
				i++; // to move next actual argument
				outputFile = args[i];
			}

		boolean isValid = true;
		if (bugFileDir.equals("") || null == bugFileDir) {
			isValid = false;
			System.out
					.println("The bug information file directory is invalid path");
		}

		if (sourceCodeDir.equals("") || null == sourceCodeDir) {
			isValid = false;
			System.out.println("The source code directory is invalid path");
		}

		if (!alphaStr.equals("") && null != alphaStr) {
			try {
				alpha = Float.parseFloat(alphaStr);
			} catch (Exception ex) {
				isValid = false;
				System.out
						.println("-a argument is invalid, it must be a float value");
			}
		}

		if (!betaStr.equals("") && null != betaStr) {
			try {
				beta = Float.parseFloat(betaStr);
			} catch (Exception ex) {
				isValid = false;
				System.out
						.println("-b argument is invalid, it must be a float value");
			}
		}

		if (outputFile.equals("") || null == outputFile) {
			isValid = false;
			System.out.println("you must indicate the output file");
		} else {
			File file = new File(outputFile);
			if (file.isDirectory()) {
				if (!file.exists())
					file.mkdir();
				outputFile = (new StringBuilder(String.valueOf(outputFile)))
						.append("output.txt").toString();
			}
		}

		if (!isValid) {
			showUsage();
		} else {
			File file = new File(System.getProperty("user.dir"));
			if (file.getFreeSpace() / 1024L / 1024L / 1024L < 2L) {
				System.out.println("Not enough free disk space, please ensure your current disk space are bigger than 2G.");
				isValid = false;
			} else {
				File dir = new File("tmp");
				if (!dir.exists()) {
					dir.mkdir();
				}
				Property.createInstance(productName, bugFileDir, sourceCodeDir, dir.getAbsolutePath(), alpha, beta, outputFile);
			}
		}
		return isValid;
	}

	private static void printProperty() {
		Property prop = Property.getInstance();

		if (null != prop) {
			System.out.printf("Bug file path: %s\n", prop.getBugFilePath());
			System.out.printf("Source code dir: %s\n", prop.getSourceCodeDir());
			System.out.printf("Alpha: %f\n", prop.getAlpha());
			System.out.printf("Beta: %f\n", prop.getBeta());
			System.out.printf("Output file: %s\n", prop.getOutputFile());
		}
	}

}
