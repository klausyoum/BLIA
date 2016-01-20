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

public class GitTest {
	public static int addMethodnum1 = 0;
	public static int addMethodnum2 = 0;
	
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
		
//		System.out.println(commits);
		//commits.hashCode(5219b4af2c6ed2c477d91d3ea0a364a0e5fc3652);
		
		ObjectReader reader = repository.newObjectReader();
		
//		Repository gitrepo = git.getRepository();
//		System.out.println(gitrepo.getTags());
		
		 
		try {
			for (RevCommit revCommit : commits){	
				String hashId = revCommit.getName();
				String oldHash = revCommit.getName();
				String fullMessage = revCommit.getShortMessage();
				
				System.out.println("Hash ID : " + oldHash); // 해쉬 ID

				if (fullMessage.matches(".*fix.*") == true || fullMessage.matches(".*bug.*") == true
						|| fullMessage.matches(".*Fix.*") == true || fullMessage.matches(".*Bug.*") == true){
//					ObjectId headId = git.getRepository().resolve("HEAD^{tree}");
					ObjectId headId = git.getRepository().resolve(oldHash + "~1^{tree}");
				    ObjectId oldId = git.getRepository().resolve(oldHash + "^{tree}");
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
						diff.getOldId();
						String diffText = out.toString("UTF-8");
						int diffTimeing = diffText.indexOf("@@");
						
						// Get the revision's file tree
					    RevTree tree = revCommit.getTree();
					    // .. and narrow it down to the single file's path
					    String newPath = diff.getNewPath();
					    TreeWalk treeWalk = TreeWalk.forPath(reader1, newPath, tree);
					    if (newPath.length() == newPath.lastIndexOf(".java")+5){
						    if (newPath.lastIndexOf(".java") != -1){
						    	
						    	System.out.println("File Name : " + newPath);
	//				    		System.out.println("Method Diff : \n" + diffText.substring(diffTimeing).replace("\n+", "\n"));
					    		String methodData = diffText.substring(diffTimeing).replace("\n+", "\n");
					    		BufferedReader methodRead = new BufferedReader(new StringReader(methodData));
					    		String i = null;
					    		int iii = 0;
					    		while((i = methodRead.readLine()) != null){
					    			iii++;
//					    			if (iii != 0){
					    			
					    				Pattern addMethod = Pattern.compile("([+][0-9]*)([,][0-9]*)");
					    				Matcher match1 = addMethod.matcher(i);
					    				
					    				while(match1.find()) {
					    					if (i.indexOf("@@") != -1){
					    						addMethodnum1 = Integer.parseInt(match1.group(1).replace("+", ""));
						    		            addMethodnum2 = Integer.parseInt(match1.group(2).replace(",", ""));
						    		            
						    		            if (addMethod.matcher(i) != null){
//						    						System.out.println("addmethodnum1 : " + addMethodnum1 + " addmethodnum2 : " + addMethodnum2);
						    						
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
											        			System.out.println("codenum : " + codeNum + "" + " addmethodnum1 : " + addMethodnum1 + "" + " addmethodnum2 : " +  addMethodnum2);
											        			
//											        			Array1.add(ii);
//											        			Pattern patten = Pattern.compile("(public|private|protected).*\\s+(class|void|abstract|static|final|native|strictfp|synchronized)*[a-zA-Z].*");
											        			Pattern patten = Pattern.compile("(public|private|protected).*");
//											        			Pattern patten = Pattern.compile("(public|private|protected)*\\s+(abstract|static|final|native|strictfp|synchronized)*\\s*([A-z0-9_$,<>]*\\s*)*[A-z]\\w*\\s*[(]+");
											        			for(int ee=codeNum; ee > 0; ee--){
											        				Matcher matcher = patten.matcher(Array1.get(ee-1).replaceAll("\t", ""));
//											        				Pattern patten = Pattern.compile("(public|private|protected)*\\s+(abstract|static|final|native|strictfp|synchronized)*\\s*([A-z0-9_$,<>]*\\s*)*[A-z]\\w*\\s*[(]+");        							        			
//												        			Matcher matcher = patten.matcher("public void doSupertypesFirst(ReferenceBinding rb, Collection<? extends ReferenceBinding> yetToProcess) {");
												        			
												        			if(matcher.matches()){
//												        				if (Array1.get(ee-1).length() == Array1.get(ee-1).lastIndexOf("{")+1){
//												        				if (Array1.get(ee-1).length() == Array1.get(ee-1).lastIndexOf("{")+1){
												        					q++;
													        				if (q == 1){
						//							        					System.out.println(true);
													        					System.out.println("	Method Name : " +Array1.get(ee-1).replaceAll("\t", "") + "\n");
														        			}
													        				
//												        				}
													        				
												        			}
												        			
										        				}
							
										        			}
											        						            
										        		}
										        	}
						    					}
					    					} 
					    				}
//					    			}
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
