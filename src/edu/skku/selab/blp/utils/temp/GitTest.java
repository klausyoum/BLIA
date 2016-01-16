package edu.skku.selab.blp.utils.temp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;


public class GitTest {
	public static void main(String[] args) throws IOException, NoHeadException, GitAPIException {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setGitDir(new File("/Users/ahnjun/git/org.aspectj/.git"))
		  .readEnvironment() // scan environment GIT_* variables
		  .findGitDir() // scan up the file system tree
		  .build();
		
		Git git = new Git(repository);
		Iterable<RevCommit> commits = git.log().all().call();
		
		//commits.hashCode(5219b4af2c6ed2c477d91d3ea0a364a0e5fc3652);
		try {
			for (RevCommit revCommit : commits){	
				String hashId = revCommit.getName();
				String oldHash = revCommit.getName();
				String fullMessage = revCommit.getShortMessage();
				
				System.out.println("Hash ID : " + hashId); // 해쉬 ID
				
//				System.out.println("file read : " + revCommit.getId().);

				if (fullMessage.matches(".*fix.*") == true || fullMessage.matches(".*bug.*") == true
						|| fullMessage.matches(".*Fix.*") == true || fullMessage.matches(".*Bug.*") == true){
					ObjectId headId = git.getRepository().resolve("HEAD^{tree}");
				    ObjectId oldId = git.getRepository().resolve(oldHash + "^{tree}");
				    ObjectReader reader = git.getRepository().newObjectReader();

				    CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
				    oldTreeIter.reset(reader, oldId);
				    CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
				    newTreeIter.reset(reader, headId);
				 
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
						int lineIndex1 = diffText.indexOf(".java b/");
						
						if (diffText.indexOf(".java b/") != -1){
							if (diffText.indexOf(".java") != -1){
								BufferedReader bufReader = new BufferedReader(new StringReader(diffText));
								diffText = bufReader.readLine();
								diffText = diffText.substring(lineIndex1);
								diffText = diffText.replaceFirst(".java b/", ":");
								System.out.println("Files : " + hashId + diffText);
							}
						}
				      out.reset();
				  }
				}
				System.out.println("-------------------------");
				}
		
		
		} catch (Exception e){
			
			System.err.println("Error : "+e);
		}
	}
}
