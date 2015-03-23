// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Splitter.java

package edu.skku.selab.blp.utils;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class Splitter
{
	public static String[] splitNatureLanguage(String natureLanguage) {
		ArrayList<String> wordList = new ArrayList<String>();
		StringBuffer wordBuffer = new StringBuffer();
		char ac[] = natureLanguage.toCharArray();
		for (int i = 0; i < natureLanguage.toCharArray().length; i++) {
			char c = ac[i];
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '\'') {
				wordBuffer.append(c);
			} else {
				String word = wordBuffer.toString();
				if (!word.equals(""))
					wordList.add(word);
				wordBuffer = new StringBuffer();
			}
		}

		if (wordBuffer.length() != 0) {
			String word = wordBuffer.toString();
			if (!word.equals(""))
				wordList.add(word);
			wordBuffer = new StringBuffer();
		}
		return (String[]) wordList.toArray(new String[wordList.size()]);
	}
	
	public static String[] splitNatureLanguageEx(String natureLanguage) {
		ArrayList<String> wordList = new ArrayList<String>();
		StringBuffer wordBuffer = new StringBuffer();
		char ac[] = natureLanguage.toCharArray();
		for (int l = 0; l < natureLanguage.toCharArray().length; l++) {
			char c = ac[l];
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '\'') {
				wordBuffer.append(c);
			} else {
				String word = wordBuffer.toString();
				wordList.add(word);	// add full identifier
				
				String[] splitWords = StringUtils.splitByCharacterTypeCamelCase(word);
				if (splitWords.length > 1) {
					for (int i = 0; i < splitWords.length; i++) {
						if (splitWords[i].length() == 0) {
							System.out.println("Word: " + word);
						} else {
							wordList.add(splitWords[i]);
						}
					}
				}
				
				wordBuffer = new StringBuffer();
			}
		}

		if (wordBuffer.length() != 0) {
			String word = wordBuffer.toString();
			if (!word.equals(""))
				wordList.add(word);
			wordBuffer = new StringBuffer();
		}
		return (String[]) wordList.toArray(new String[wordList.size()]);
	}

	public static String[] splitSourceCode(String sourceCode) {
		StringBuffer contentBuf = new StringBuffer();
		StringBuffer wordBuf = new StringBuffer();
		sourceCode = (new StringBuilder(String.valueOf(sourceCode))).append("$").toString();
		char ac[] = sourceCode.toCharArray();
		for (int l = 0; l < sourceCode.toCharArray().length; l++) {
			char c = ac[l];
			if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
				wordBuf.append(c); // append a character to wordBuf
			} else {
				int length = wordBuf.length();
				if (length != 0) {
					int k = 0;
					int i = 0;
					
					// split words written in CamelCase style
					for (int j = 1; i < length - 1; j++) {
						char first = wordBuf.charAt(i);
						char second = wordBuf.charAt(j);
						if (first >= 'A' && first <= 'Z' && second >= 'a' && second <= 'z') {
							contentBuf.append(wordBuf.substring(k, i));
							contentBuf.append(' ');
							k = i;
						} else if (first >= 'a' && first <= 'z' && second >= 'A' && second <= 'Z') {
							contentBuf.append(wordBuf.substring(k, j));
							contentBuf.append(' ');
							k = j;
						}
						i++;
					}

					if (k < length) {
						contentBuf.append(wordBuf.substring(k));
						contentBuf.append(" ");
					}
					wordBuf = new StringBuffer();
				}
			}
		}

		String words[] = contentBuf.toString().split(" ");
		contentBuf = new StringBuffer();
		for (int i = 0; i < words.length; i++)
			if (!words[i].trim().equals("") && words[i].length() >= 2)
				contentBuf.append((new StringBuilder(String.valueOf(words[i])))
						.append(" ").toString());

		return contentBuf.toString().trim().split(" ");
	}
}
