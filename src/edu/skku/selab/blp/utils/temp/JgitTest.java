/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.utils.temp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk; 

/**
 * @author Jun Ahn(ahnjune@skku.edu)
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class JgitTest {
	
	private static String bugId(String bugId) throws FileNotFoundException {
		String userHomeDir = System.getProperty("user.home");
		String bugRepoPath = userHomeDir + "/git/BLIA/data/";
		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "AspectJFixedCommits.txt"));
		String foundCommit = null;
		
		while (bugRepoRead.hasNextLine()) {
    		String line1 = bugRepoRead.nextLine();
    		String[] items= line1.split(" ");
    		for (int repoLength=1; repoLength < items.length; repoLength++){
    			if(bugId.matches(items[repoLength])){
    				return items[0];	
    			}
    		}    		
		}
		return null;
		
	}
	
	private static String listFixedCommits(String commitName) throws FileNotFoundException {
		String userHomeDir = System.getProperty("user.home");
		String bugRepoPath = userHomeDir + "/git/BLIA/data/";
		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "AspectJFixedCommits.txt"));
		
		while (bugRepoRead.hasNextLine()) {
    		String line1 = bugRepoRead.nextLine();
    		String[] items= line1.split(" ");
    		
    		for (int repoLength=1; repoLength < items.length; repoLength++){
    			if(commitName.matches(items[repoLength])){
//    				System.out.println(items[repoLength] +" "+ commitName);
    				return items[repoLength];
    			}	
			}
    	}
		return null;
	}
	
	public static String containsMethodName(String line) {
		String foundResult = null;
		String[] wordArray = null;
		String foundMethod = null;
		
		// check that a line is comment.
		String trimmedLine = line.trim();
		if (trimmedLine.startsWith("//") || trimmedLine.startsWith("/*") ||
				trimmedLine.startsWith("*/") || trimmedLine.startsWith("*"))
			return null;
		
		// split code and comment if the line has comment.
		String methodCandidate = null;
		methodCandidate = line;
		if (methodCandidate.indexOf("//") > 0) {
			methodCandidate = methodCandidate.substring(0, methodCandidate.indexOf("//"));	
		}
		
		if (methodCandidate.contains(";")) {
			return null;
		}
		
		int index = methodCandidate.indexOf('(');
		if (index == -1) {
			return null;
		}
		else {
			methodCandidate = methodCandidate.substring(0, index + 1);
		}
		
		Pattern keywordPattern = Pattern.compile("(\\s)+(=|new|class|extends|if|else|return)(\\s)+");
		Matcher keywordMatcher = keywordPattern.matcher(methodCandidate);
		if (keywordMatcher.find()) {
			return null;
		}
	
		String regExp = "(public|private|protected)+\\s+"
				+ "(abstract|static|final|native|strictfp|synchronized)*\\s*"
				+ "([A-z0-9_,.$<>\\[\\]]*\\s*)*" + "\\(";
		Pattern pattern = Pattern.compile(regExp);
		Matcher matcher = pattern.matcher(methodCandidate);
		if (matcher.find()) {
			// debug code
//			System.out.printf(">> [Method]: %s, %s\n", methodCandidate, matcher.group());
			
			foundResult = matcher.group();
			wordArray = foundResult.split("[ \\(]");
			if(wordArray.length-1 == -1){
//				System.out.println("-1");
			} else {
				foundMethod = wordArray[wordArray.length-1];
			}
			return foundMethod;
		} else {
			return null;
		}
	}
	
    public static void main(String[] args) throws IOException, NoHeadException, GitAPIException {
		String userHomeDir = System.getProperty("user.home");
		String GitRepoPath = userHomeDir + "/git/org.aspectj/.git";
		String bugRepoPath = userHomeDir + "/git/BLIA/data/";
//		String GitRepoPath = userHomeDir + "/git/eclipse.platform.swt/.git";
//		String GitRepoPath = userHomeDir + "/git/zxing/.git";
		
		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "AspectJFixedCommits.txt"));
//		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "SWTFixedCommits.txt"));
//		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "ZXingFixedCommits.txt"));
		

		File gitWorkDir = new File(GitRepoPath);
		Git git = Git.open(gitWorkDir);
		Iterable<RevCommit> commits = git.log().all().call();
		try {
			
			for (RevCommit revCommit : commits){
				String commitId = listFixedCommits(revCommit.getName());
				String bugId = bugId(revCommit.getName());
				String fullMessage = revCommit.getShortMessage();
				
				if (commitId != null && bugId != null){
					Date date = revCommit.getAuthorIdent().getWhen();
					String dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date);
					System.out.println("bugID : " + bugId);
					System.out.println("Commit ID : " + commitId);
					System.out.println("Author : " + revCommit.getAuthorIdent().getName());
					System.out.println("Date : " + dateFormat);
					
					if (fullMessage.matches(".*fix.*") || fullMessage.matches(".*Fix.*")
							|| fullMessage.matches(".*bug.*") || fullMessage.matches(".*Bug.*")) {
						ObjectId oldId = git.getRepository().resolve(commitId + "~1^{tree}");
						ObjectId headId = git.getRepository().resolve(commitId + "^{tree}");
						ObjectReader newObjectReader = git.getRepository().newObjectReader();
	
						CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
						oldTreeIter.reset(newObjectReader, oldId);
						CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
						newTreeIter.reset(newObjectReader, headId);
	
						List<DiffEntry> diffs= git.diff()
								.setNewTree(newTreeIter)
								.setOldTree(oldTreeIter)
								.call();
	
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						DiffFormatter df = new DiffFormatter(out);
						df.setRepository(git.getRepository());
	
						for(DiffEntry diff : diffs) {			    	
							df.format(diff);
							String diffText = out.toString("UTF-8");
							int chunkHeaderIndex = diffText.indexOf("@@");
							// in case of deleted file
							if (chunkHeaderIndex == -1) {
								continue;
							}
	
							RevTree tree = revCommit.getTree();
							String oldPath = diff.getOldPath();
							String newPath = diff.getNewPath();
							TreeWalk treeWalk = TreeWalk.forPath(newObjectReader, newPath, tree);
	
							if (newPath.endsWith(".java")) {
								System.out.println("New path : " + newPath);
								String methodData = diffText.substring(chunkHeaderIndex);
								BufferedReader chunk = new BufferedReader(new StringReader(methodData));
								String line = null;
								int chunkStartLineNum = 0;
								int chunkLineCount = 0;
								
								if ((line = chunk.readLine()) != null) {
									System.out.printf("Chunk header: %s\n", line);
	
									String[] splitWords = line.split(" ")[2].split("[+,]");
									if (splitWords.length >= 3) {
										chunkStartLineNum = Integer.parseInt(splitWords[1]);
										chunkLineCount = Integer.parseInt(splitWords[2]);
									} else if (splitWords.length == 2) { // "--0,0 +1"
										chunkStartLineNum = Integer.parseInt(splitWords[1]);
										chunkLineCount = Integer.parseInt(splitWords[1]);
									} else {
										System.out.printf("Diff Data: %s\n", diffText);
										System.err.println("ERROR: Invalid chunk header!");
										System.exit(-1);
									}
									
									System.out.printf("%s => %d, %d\n", line, chunkStartLineNum, chunkLineCount);
								} else {
									System.err.println("ERROR: Invalid chunk header!");
									System.exit(-1);
								}
								
								if (treeWalk != null) {
									// use the blob id to read the file's data
									byte[] data = newObjectReader.open(treeWalk.getObjectId(0)).getBytes();
									String sourceCode = new String(data, "UTF-8");	
									BufferedReader codeReader = new BufferedReader(new StringReader(sourceCode));
	
									String codeLine = null;
									List<String> codeLineArray = new ArrayList<String>();
									while((codeLine = codeReader.readLine()) != null){
										codeLineArray.add(codeLine);
									}
									
									for(int i = (chunkStartLineNum + chunkLineCount - 2); i >= 0; i--){	
//										System.out.printf("[%d]: %s\n", i, codeLineArray.get(i));
										
										String foundMethod = JgitTest.containsMethodName(codeLineArray.get(i));
										if (foundMethod != null){
											System.out.println("	[MATHCED] " + foundMethod + " - " + codeLineArray.get(i).replaceAll("\t", ""));
										}
									}
								}
							}
							out.reset();
						}	
					}
					System.out.println("-------------------------");
				}
			}
		} catch (Exception e){
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsStrting = sw.toString();
	
			System.err.println("Unexpcted exception: "+ exceptionAsStrting);
		}
	}		
}