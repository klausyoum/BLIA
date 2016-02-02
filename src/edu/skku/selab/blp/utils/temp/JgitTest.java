/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.utils.temp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList; 

/**
 * @author Jun Ahn(ahnjune@skku.edu)
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class JgitTest {
	private static String bugId(String bugId) throws FileNotFoundException {
		String userHomeDir = System.getProperty("user.home");
		String bugRepoPath = userHomeDir + "/git/BLIA/data/";
//		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "AspectJFixedCommits.txt"));
		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "SWTFixedCommits.txt"));
//		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "ZXingFixedCommits.txt"));
		
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
//		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "AspectJFixedCommits.txt"));
		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "SWTFixedCommits.txt"));
//		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "ZXingFixedCommits.txt"));
		
		while (bugRepoRead.hasNextLine()) {
    		String line1 = bugRepoRead.nextLine();
    		String[] items= line1.split(" ");
    		
    		for (int repoLength=0; repoLength < items.length; repoLength++){
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
	
    public static void main(String[] args) throws Exception, IOException, NoHeadException, GitAPIException {
		String userHomeDir = System.getProperty("user.home");
		String bugRepoPath = userHomeDir + "/git/BLIA/data/";
		
//		String GitRepoPath = userHomeDir + "/git/org.aspectj/.git";
		String GitRepoPath = userHomeDir + "/git/eclipse.platform.swt/.git";
//		String GitRepoPath = userHomeDir + "/git/zxing/.git";
		
//		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "AspectJFixedCommits.txt"));
		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "SWTFixedCommits.txt"));
//		Scanner bugRepoRead = new Scanner(new File(bugRepoPath + "ZXingFixedCommits.txt"));
		
//	XML
//		String productName = "AspectJ";
		String productName = "SWT";
//		String productName = "ZXing";
		
		String bugRepoFileName = productName + "BugRepository.xml";
		
			//No comment on the Bug repository file name (before Bug repository Name : ex : SWTBugrepository.xml)
			File bugRepoXmlFile = new File("./data/" + bugRepoFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(bugRepoXmlFile);
					
			doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
					
			NodeList bugNodeList = doc.getElementsByTagName("bug");
			
			String path = "./data/";
			//New Add Comment on the Bug repository file name (after Bug repository Name : ex : New_SWTBugrepository.xml)
			BufferedWriter outPut = new BufferedWriter(new FileWriter(path + "New" +bugRepoFileName));
			
			String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n\n";
			
			String bugrepositoryStartTag = "<bugrepository name=\"" + productName + "\">\n";
			
				String bugIdStartTag = "  <bug id=";
				String bugOpenDateAttr = "opendate=";
				String bugFixedDateAttr = "fixdate=";
				
					String bugInformationStartTag = "    <buginformation>\n";
						
						String bugSummaryStartTag = "      <summary>";
						String bugSummaryEndTag= "</summary>\n";
						String bugDescriptionStartTag = "      <description>";
						String bugDescriptionEndTag= "</description>\n";
						
						String commentsStartTag = "      <comments>\n";
							String commentIdTag = "        <comment id=";
							String commentDateAttr = "date=";
							String commentAuthorAttr = "author=";
							String commentEndTag = "</comment>\n";
						String commentsEndTag = "      </comments>\n";
					
					String bugInformationEndTag = "    </buginformation>\n";
				
					String fixedCommits = "    <fixedCommits>\n";
						String commitStartTag = "      <commit id=";
							String fileStartTag = "         <file name=";
								String methodStart = "            <method>";
								String methodEnd = "</method>\n";
							String fileEndTag = "         </file>\n";
						String commitEndTag = "      </commit>\n";
					String fixedCommitEnd = "    </fixedCommits>\n";
				
			String bugIdEndTag = "  </bug>\n";
			String bugRepositoryEndTag = "</bugrepository>\n";
			
		System.out.println("----------------------------");
		
		outPut.write(xml);
		outPut.write(bugrepositoryStartTag);

		for (int j = 0; j < bugNodeList.getLength(); j++) {
			Node bugNode = bugNodeList.item(j);

			if (bugNode.getNodeType() == Node.ELEMENT_NODE) {

				Element bugElement = (Element) bugNode;
				
				String bugId = bugElement.getAttribute("id");
				String bugOpenDate = bugElement.getAttribute("opendate");
				String bugFixDate = bugElement.getAttribute("fixdate");
				
				outPut.write(bugIdStartTag + "\"" + bugId + "\" ");
				outPut.write(bugOpenDateAttr + "\"" + bugOpenDate + "\" ");
				outPut.write(bugFixedDateAttr + "\"" + bugFixDate + "\">\n");
				
				outPut.write(bugInformationStartTag);
				
				outPut.write(bugSummaryStartTag);
				String bugSummary = bugElement.getElementsByTagName("summary").item(0).getTextContent();
				bugSummary = bugSummary.replaceAll("&", "&amp;");
				bugSummary = bugSummary.replaceAll("<", "&lt;");
				bugSummary = bugSummary.replaceAll(">", "&gt;");
				outPut.write(bugSummary);
				outPut.write(bugSummaryEndTag);
				
				outPut.write(bugDescriptionStartTag);
				String bugDescription = bugElement.getElementsByTagName("description").item(0).getTextContent();
				bugDescription = bugDescription.replaceAll("&", "&amp;");
				bugDescription = bugDescription.replaceAll("<", "&lt;");
				bugDescription = bugDescription.replaceAll(">", "&gt;");
//				bugDescription = bugDescription.replace(System.getProperty("line.separator"), ""); 
				
				outPut.write(bugDescription);
				outPut.write(bugDescriptionEndTag);
				
				System.out.println("Bug ID : " + bugElement.getAttribute("id")+ " Open date : " + bugElement.getAttribute("opendate") + " Fixed date : " + bugElement.getAttribute("fixdate"));
				System.out.println("Summary : " + bugElement.getElementsByTagName("summary").item(0).getTextContent() + "Description : " + bugElement.getElementsByTagName("description").item(0).getTextContent());
		
				outPut.write(commentsStartTag);
				NodeList comment = bugElement.getElementsByTagName("comment");
				int numComment = comment.getLength();
				for(int i = 0; i < numComment; i++){
					Node commentNode = comment.item(i);
					Element commentIdElement = (Element)commentNode;
					
					System.out.println("Comment ID : " + commentIdElement.getAttribute("id") + " Comment Date : " + commentIdElement.getAttribute("date") + " Comment Author : " + commentIdElement.getAttribute("author") + " Comment Description : " + bugElement.getElementsByTagName("comment").item(i).getTextContent());
					
					String commentId = commentIdElement.getAttribute("id");
					String commentDate = commentIdElement.getAttribute("date");
					String commentAuthor = commentIdElement.getAttribute("author");
					String commentDescription = bugElement.getElementsByTagName("comment").item(i).getTextContent();
					
					outPut.write("		<comment id=" + "\"" + commentId + "\" " + "date=" + "\"" + commentDate +"\"" + " author=" + "\"" + commentAuthor + "\">" + commentDescription + "</comment>\n");
				}	
				outPut.write(commentsEndTag);
				outPut.write(bugInformationEndTag);
				
				outPut.write(fixedCommits);
				
				NodeList sections = bugElement.getElementsByTagName("file");
		        int numSections = sections.getLength();
		        for(int i = 0; i < numSections; i++){
		        	
//		        	System.out.println("File : " + bugElement.getElementsByTagName("file").item(i).getTextContent());
		        	
		        	int classNameStart = bugElement.getElementsByTagName("file").item(i).getTextContent().lastIndexOf("/");
		        	int classNameEnd = bugElement.getElementsByTagName("file").item(i).getTextContent().indexOf(".java");
//		        	String className = bugElement.getElementsByTagName("file").item(i).getTextContent().substring(classNameStart+1, classNameEnd) + ".java";
		        	String className = bugElement.getElementsByTagName("file").item(i).getTextContent().replace(".", "/").replace("/java", ".java");
//		        	System.out.println(bugElement.getElementsByTagName("file").item(i).getTextContent().replace(".", "/").replace("/java", ".java"));
//		        	outPut.write(fileStartTag + "\"" + bugElement.getElementsByTagName("file").item(i).getTextContent() + ">\n");
		        	
		        	File gitWorkDir = new File(GitRepoPath);
					Git git = Git.open(gitWorkDir);
					Iterable<RevCommit> commits = git.log().all().call();
					try {
						
						for (RevCommit revCommit : commits){
							String commitId = listFixedCommits(revCommit.getName());
							String bugRepoId = bugId(revCommit.getName());
							String fullMessage = revCommit.getShortMessage();
							if (commitId != null && bugRepoId != null){
								if(bugRepoId.matches(bugId)){
									Date date = revCommit.getAuthorIdent().getWhen();
									String dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(date);
	//								System.out.println("bugID : " + bugRepoId);
	//								System.out.println("Commit ID : " + commitId);
	//								System.out.println("Author : " + revCommit.getAuthorIdent().getName());
	//								System.out.println("Date : " + dateFormat);
									
									System.out.println("<commit id="+ "\"" + commitId + "\" " + "author=" + "\"" + revCommit.getAuthorIdent().getName() + "\" " + "date=" + "\"" + dateFormat + "\">\n");
									System.out.println("<file name="+ "\"" + bugElement.getElementsByTagName("file").item(i).getTextContent() + "\">\n");
									
									outPut.write("      <commit id="+ "\"" + commitId + "\" " + "author=" + "\"" + revCommit.getAuthorIdent().getName() + "\" " + "date=" + "\"" + dateFormat + "\">\n");
									outPut.write("           <file name="+ "\"" + bugElement.getElementsByTagName("file").item(i).getTextContent() + "\">\n");
									
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
	//											System.out.println("New path : " + newPath);
												String methodData = diffText.substring(chunkHeaderIndex);
												BufferedReader chunk = new BufferedReader(new StringReader(methodData));
												String line = null;
												int chunkStartLineNum = 0;
												int chunkLineCount = 0;
												
												if ((line = chunk.readLine()) != null) {
	//												System.out.printf("Chunk header: %s\n", line);
					
													String[] splitWords = line.split(" ")[2].split("[+,]");
													if (splitWords.length >= 3) {
														chunkStartLineNum = Integer.parseInt(splitWords[1]);
														chunkLineCount = Integer.parseInt(splitWords[2]);
													} else if (splitWords.length == 2) { // "--0,0 +1"
														chunkStartLineNum = Integer.parseInt(splitWords[1]);
														chunkLineCount = Integer.parseInt(splitWords[1]);
													} else {
	//													System.out.printf("Diff Data: %s\n", diffText);
	//													System.err.println("ERROR: Invalid chunk header!");
														System.exit(-1);
													}
													
	//												System.out.printf("%s => %d, %d\n", line, chunkStartLineNum, chunkLineCount);
												} else {
	//												System.err.println("ERROR: Invalid chunk header!");
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
													
													for(int ii = (chunkStartLineNum + chunkLineCount - 2); ii >= 0; ii--){
	//													System.out.printf("[%d]: %s\n", i, codeLineArray.get(i));
														String foundMethod = JgitTest.containsMethodName(codeLineArray.get(ii));
														if (foundMethod != null){
															if(bugRepoId.matches(bugId)){
																if(newPath.contains(className)){
	//																System.out.println("	[MATHCED] " + "BugID : " + bugRepoId + " = " + bugId + " = " + newPath + " = " + commitId + " - " + foundMethod);
	//																System.out.println(codeLineArray.get(ii).replaceAll("\t", ""));
																	System.out.println("	[MATHCED] " + foundMethod + " - " + codeLineArray.get(ii).replaceAll("\t", ""));
																	
																	outPut.write("               <method>"+ codeLineArray.get(ii).replaceAll("\t", "").replace("    ", "") + "</method>\n");
																}
															}
														}	 
													}
												}
												
											}
											out.reset();
										}
									}
								}
							
							
							}
							
						}
						outPut.write("           </file>\n");
						outPut.write("      </commit>\n");
						//////////////////
//						System.out.println("File : " + bugElement.getElementsByTagName("file").item(i).getTextContent());
						//////////////////						

					} catch (Exception e){
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						String exceptionAsStrting = sw.toString();
				
						System.err.println("Unexpcted exception: "+ exceptionAsStrting);
					}
//					System.out.println("File : " + bugElement.getElementsByTagName("file").item(i).getTextContent());
		        }
		        outPut.write(fixedCommitEnd);
		        outPut.write(bugIdEndTag);    
		}
	}
	outPut.write(bugRepositoryEndTag);
	outPut.close();
	}		
}