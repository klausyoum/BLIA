/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.utils.temp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class MethodUtil {
	
	public static String containsMethodName(String line) {
		line = line.trim();
		
		// check that a line is comment.
		if (line.startsWith("//") || line.startsWith("/*") || line.startsWith("*/") || line.startsWith("*"))
			return null;
		
		// split code and comment if the line has comment.
		String methodCandidate = line;
		if (methodCandidate.indexOf("//") > 0) {
			methodCandidate = methodCandidate.substring(0, methodCandidate.indexOf("//"));	
		}
		
		int index = methodCandidate.indexOf('(');
		if (index == -1) {
			return null;
		}
		else {
			methodCandidate = methodCandidate.substring(0, index + 1);
		}
		
		if (methodCandidate.contains("=") || methodCandidate.contains(" new ") ||
				methodCandidate.contains(" class ") || methodCandidate.contains(" extends "))
			return null;		
	
		String regExp = "(public|private|protected)*\\s+"
				+ "(abstract|static|final|native|strictfp|synchronized)*\\s*"
				+ "([A-z0-9_,.<>\\[\\]]*\\s*)*" + "\\(";
		Pattern pattern = Pattern.compile(regExp);
		Matcher matcher = pattern.matcher(methodCandidate);
		if (matcher.find()) {
			// debug code
//			System.out.printf(">> [Method]: %s, %s\n", methodCandidate, matcher.group());
			
			String foundResult = matcher.group();
			String wordArray[] = foundResult.split("[ \\(]");
			String foundMethod = wordArray[wordArray.length - 1];
			return foundMethod;
		} else {
			return null;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Read file lines
		// Test regular expressions
		
		String testInputFile = "data/test/methodExample.txt";

		try {
			BufferedReader in = new BufferedReader(new FileReader(testInputFile));
			String line;
			int lineCount = 0;
			int matchedCount = 0;
			int unmatchedCount = 0;

			while ((line = in.readLine()) != null) {
				++lineCount;

				String foundMethod = MethodUtil.containsMethodName(line); 
				if (null != foundMethod) {
					System.out.printf("[MATHCED] %d: %s << %s\n", lineCount, foundMethod, line);
					matchedCount++;
				} else {
//					System.out.printf("[UNMATHCED] %d: %s\n", lineCount, line);
					unmatchedCount++;
				}
			}
			
			System.out.printf("Matched Count: %d, Unmatched Count: %d\n",  matchedCount, unmatchedCount);
			in.close();
		} catch (IOException e) {
			System.err.println(e); // 에러가 있다면 메시지 출력
			System.exit(1);
		}	
	}
}
