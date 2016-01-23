package edu.skku.selab.blp.utils.temp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk; 

public class JgitTest {
	public static int addMethodnum1 = 0;
	public static int addMethodnum2 = 0;

	
	public static String containsMethodName(String line) {
		line = line.trim();
		
		String foundResult = null;
		String[] wordArray = null;
		String foundMethod = null;
		
		// check that a line is comment.
		if (line.startsWith("//") || line.startsWith("/*") || line.startsWith("*/") || line.startsWith("*"))
			return null;
		
		// split code and comment if the line has comment.
		String methodCandidate = null;
		methodCandidate = line;
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
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setGitDir(new File("/Users/ahnjun/git/org.aspectj/.git"))
		  .readEnvironment() // scan environment GIT_* variables
		  .findGitDir() // scan up the file system tree
		  .build();
		
		File gitWorkDir = new File("/Users/ahnjun/git/org.aspectj/.git");
		Git git=Git.open(gitWorkDir);
		Git git1 = new Git(repository);
		Iterable<RevCommit> commits = git.log().all().call();
		ObjectReader reader = repository.newObjectReader();
		 
		try {
			for (RevCommit revCommit : commits){	
				String hashId = revCommit.getName();
				String oldHash = revCommit.getName();
				String fullMessage = revCommit.getShortMessage();
				List<String> Array2 = new ArrayList<String>();
    			List<Integer> Array3 = new ArrayList<Integer>();
    			int num1 = 0;
    			
				System.out.println("Hash ID : " + oldHash); // 해쉬 ID

				if (fullMessage.matches(".*fix.*") == true || fullMessage.matches(".*bug.*") == true
						|| fullMessage.matches(".*Fix.*") == true || fullMessage.matches(".*Bug.*") == true){
					
					ObjectId oldId = git.getRepository().resolve(oldHash + "~1^{tree}");
				    ObjectId headId = git.getRepository().resolve(oldHash + "^{tree}");
				    ObjectReader reader1 = git.getRepository().newObjectReader();

				    CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
				    oldTreeIter.reset(reader1, oldId);
				    CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
				    newTreeIter.reset(reader1, headId);
				 
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
						int diffTimeing = diffText.indexOf("@@");
						
					    RevTree tree = revCommit.getTree();
					    String newPath = diff.getNewPath();
					    String oldPath = diff.getOldPath();
					    TreeWalk treeWalk = TreeWalk.forPath(reader1, newPath, tree);
					    
					    if (newPath.length() == newPath.lastIndexOf(".java")+5){
						    	System.out.println("File Name : " + newPath);
					    		String methodData = diffText.substring(diffTimeing).replace("\n+", "\n");
					    		BufferedReader methodRead = new BufferedReader(new StringReader(methodData));
					    		String i = null;
					    		int iii = 0;
					    		
					    		while((i = methodRead.readLine()) != null){
					    			iii++;
					    				Pattern addMethod = Pattern.compile("([+][0-9]*)([,][0-9]*)");
					    				Matcher match1 = addMethod.matcher(i);
					    				
					    				while(match1.find()) {
					    					if (i.indexOf("@@") != -1){
					    						
					    						addMethodnum1 = Integer.parseInt(match1.group(1).replace("+", ""));
						    		            addMethodnum2 = Integer.parseInt(match1.group(2).replace(",", ""));
						    		            
						    		            if (addMethod.matcher(i) != null){
											        if (treeWalk != null) {
											        	// use the blob id to read the file's data
											        	byte[] data = reader1.open(treeWalk.getObjectId(0)).getBytes();
											        	String sourceCode = new String(data, "UTF-8");	
											        	BufferedReader codeReader = new BufferedReader(new StringReader(sourceCode));
											        	String ii = null;
											        	
											        	List<String> Array1 = new ArrayList<String>();
											        	
											        	int codeNum = 0;
											        	while((ii = codeReader.readLine()) != null){
											        		codeNum++;
											        		Array1.add(ii);
											        		int q = 0;						        		
											        		if(codeNum == addMethodnum1+addMethodnum2){
											        			for(int ee=codeNum-2; ee > 0; ee--){	
											        				String foundMethod = null;
											        				foundMethod = JgitTest.containsMethodName(Array1.get(ee));
											        				if (foundMethod != null){
											        					System.out.println("	[MATHCED] " + foundMethod + " - " + Array1.get(ee).replaceAll("\t", ""));
											        				}
										        				}
										        			}			            
										        		}
										        	}
						    					}
					    					} 
					    				}
					    		}
					    }
					out.reset();
					}	
				}
				System.out.println("-------------------------");
			}
		
		} catch (Exception e){
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String exceptionAsStrting = sw.toString();
			
			System.err.println("Error : "+ exceptionAsStrting);
		}
	}
}
