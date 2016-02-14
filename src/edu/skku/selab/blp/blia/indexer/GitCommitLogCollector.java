/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.indexer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.Edit.Type;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import edu.skku.selab.blp.common.CommitInfo;
import edu.skku.selab.blp.common.ExtendedCommitInfo;
import edu.skku.selab.blp.common.Method;
import edu.skku.selab.blp.common.MethodVisitor;
import edu.skku.selab.blp.db.dao.CommitDAO;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class GitCommitLogCollector implements ICommitLogCollector {
	private String repoDir;
	private boolean DEBUG_MODE = true;
	
	/**
	 * 
	 */
	public GitCommitLogCollector(String repoDir) {
		this.repoDir = repoDir;
	}
	
	private CompilationUnit getCompilationUnit(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		parser.setCompilerOptions(options);
		parser.setSource(source.toCharArray());
		CompilationUnit cu = (CompilationUnit)parser.createAST(null);

		return cu;
	}
	
	
	private ArrayList<Method> extractModifiedMethods(RevCommit revCommit, DiffEntry diff, Git git) {
		if (ChangeType.MODIFY != diff.getChangeType()) {
			return null;
		}
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DiffFormatter df = new DiffFormatter(out);
			df.setRepository(git.getRepository());
			
//			String oldPath = diff.getOldPath();
			String newPath = diff.getNewPath();
			if (!newPath.endsWith(".java")) {
				return null;
			}
			System.out.println("FileName : " + newPath);
			
			RevTree tree = revCommit.getTree();
			ObjectReader newObjectReader = git.getRepository().newObjectReader();
			TreeWalk treeWalk = TreeWalk.forPath(newObjectReader, newPath, tree);
			MethodVisitor visitor = null;
			CompilationUnit cu = null;
			String newSource = null;
			if (treeWalk != null) {
				// use the blob id to read the file's data
				byte[] data = newObjectReader.open(treeWalk.getObjectId(0)).getBytes();
				newSource = new String(data);
				cu = getCompilationUnit(newSource);
				visitor = new MethodVisitor();
				cu.accept(visitor);
			}
			
			df.format(diff);
			String diffText = out.toString("UTF-8");
			
			ArrayList<Method> foundMethods = new ArrayList<Method>();
			FileHeader diffHeader = df.toFileHeader(diff);
			EditList editList = diffHeader.toEditList();
			for (int i = 0; i < editList.size(); ++i) {
				Edit edit = editList.get(i);
				if (DEBUG_MODE) {
					System.out.printf("Type: %s, A[%d, %d], B[%d, %d]\n", edit.getType().toString(),
							edit.getBeginA(), edit.getEndA(), edit.getBeginB(), edit.getEndB());
				}
				int actualModifiedStartLine = edit.getBeginB();
				int actualModifiedEndLine = edit.getEndB();
				
				if (edit.getType() == Type.INSERT) {
					String sourceLines[] = newSource.split("\n");

					boolean isEmptyLines = true;
					for (int l = edit.getBeginB() + 1; l <= edit.getEndB(); ++l) {
						if ((l - 1) < sourceLines.length) {
							String insertedLine = sourceLines[l - 1];
							if (insertedLine.trim().length() != 0) {
								isEmptyLines = false;
								break;
							}
						}
					}
					
					if (isEmptyLines)
						continue;
				}
				
				if (edit.getType() != Type.DELETE) {
					actualModifiedStartLine = edit.getBeginB() + 1;
				} else {
					actualModifiedStartLine = edit.getBeginB() + 1;
					actualModifiedEndLine = actualModifiedStartLine;
				}
				
				extractMethodInfo(foundMethods, actualModifiedStartLine, actualModifiedEndLine, visitor, cu);
			}
			
			return foundMethods;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void extractMethodInfo(ArrayList<Method> commitMethodList, int actualModifiedStartLine, int actualModifiedEndLine,
			MethodVisitor visitor, CompilationUnit cu) {
		if (DEBUG_MODE) {
			System.out.printf("actualModifiedStartLine: %d, actualModifiedEndLine: %d\n", actualModifiedStartLine, actualModifiedEndLine);
		}
		
		if (visitor != null && cu != null) {
			for (MethodDeclaration md : visitor.methods) {
				// comment lines before a method are ignored.
				int methodStartLine = cu.getLineNumber(md.getName().getStartPosition());
				int methodEndLine = cu.getLineNumber(md.getStartPosition() + md.getLength());
				if (DEBUG_MODE) {
					System.out.printf("methodStartLine: %d, methodEndLine: %d\n", methodStartLine, methodEndLine);
				}
				
				if ((methodEndLine < actualModifiedStartLine) || 
						(methodStartLine != methodEndLine &&
						methodEndLine == actualModifiedStartLine && actualModifiedStartLine == actualModifiedEndLine)) {
					continue;
				} else if ((methodStartLine >= actualModifiedStartLine && methodStartLine <= actualModifiedEndLine) ||
						(methodStartLine <= actualModifiedStartLine && methodEndLine >= actualModifiedStartLine) || 
						(methodStartLine <= actualModifiedEndLine)) {
					String methodName = md.getName().toString();
					String returnType = (null != md.getReturnType2()) ? md.getReturnType2().toString() : "";
					String parameters = "";
					for (int l = 0; l < md.parameters().size(); l++) {
						parameters += ((SingleVariableDeclaration) md.parameters().get(l)).getType().toString();
						parameters += " ";
					}
					parameters = parameters.trim();

					Method foundMethod = new Method(methodName, returnType, parameters);
					if (!commitMethodList.contains(foundMethod)) {
						commitMethodList.add(foundMethod);
						System.out.printf("Method: %s, Return Type: %s, Parameter: %s\n", methodName, returnType, parameters);
					}
				} else if (methodStartLine > actualModifiedEndLine) {
					break;
				}
			}
		}
	}
	
	public void collectCommitLog(Date since, Date until, boolean collectForcely) throws Exception {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setGitDir(new File(repoDir))
		  .readEnvironment() // scan environment GIT_* variables
		  .findGitDir() // scan up the file system tree
		  .build();
		
		CommitDAO commitDAO = new CommitDAO();
		
		if (collectForcely) {
			commitDAO.deleteAllCommitInfo();
			commitDAO.deleteAllCommitFileInfo();
		}
		
		if (commitDAO.getCommitInfoCount() == 0) {
			Git git = new Git(repository);
			Iterator<RevCommit> commitLogs = git.log().call().iterator();
			
			while (commitLogs.hasNext()) {
				RevCommit currentCommit = commitLogs.next();
				if (currentCommit.getParentCount() == 0) {
					break;
				}
				RevCommit parentCommit = currentCommit.getParent(0);
				
				// prepare the two iterators to compute the diff between
				ObjectReader reader = repository.newObjectReader();
				CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
				oldTreeIter.reset(reader, parentCommit.getTree().getId());
				CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
				newTreeIter.reset(reader, currentCommit.getTree().getId());

				long timestamp = (long) currentCommit.getCommitTime() * 1000;
				Date commitDate = new Date(timestamp);

				if (commitDate.after(until)) {
					continue;
				}
				
				if (commitDate.before(since)) {
					break;
				}
				
				ExtendedCommitInfo extendedcommitInfo = new ExtendedCommitInfo();
				extendedcommitInfo.setCommitID(currentCommit.getId().name());
				extendedcommitInfo.setCommitter(currentCommit.getCommitterIdent().getName());
				extendedcommitInfo.setMessage(currentCommit.getShortMessage());
				extendedcommitInfo.setCommitDate(commitDate);

				// debug code
//				System.out.printf("Committer: %s, Time: %s, Msg: %s\n",
//						commitInfo.getCommitter(),
//						commitInfo.getCommitDateString(), 
//						commitInfo.getMessage());
//				System.out.printf(">> Commit ID: %s\n", commitInfo.getCommitID());
				
				// finally get the list of changed files
				List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
				for (DiffEntry entry : diffs) {
					int commitType = convertCommitType(entry.getChangeType());
					String updatedFileName = entry.getPath(DiffEntry.Side.NEW);
				
					// ONLLY java files added to save computing time and space
					if (updatedFileName.contains(".java")) {
						extendedcommitInfo.addCommitFile(commitType, updatedFileName);
						
						// debug code
//						System.out.printf("ChagngeType: %d, Path: %s\n", commitType, updatedFileName);
						
						ArrayList<Method> modifedMethods = extractModifiedMethods(currentCommit, entry, git);
						if (modifedMethods != null && modifedMethods.size() != 0) {
							for (int i = 0; i < modifedMethods.size(); ++i) {
								Method fixedMethod = modifedMethods.get(i);
								extendedcommitInfo.addFixedMethod(updatedFileName, fixedMethod);
							}
						}
					}
				}
				
				if (extendedcommitInfo.getAllCommitFilesWithoutCommitType().size() > 0) {
					commitDAO.insertCommitInfo(extendedcommitInfo);
				}
			}
			git.close();
		}
		repository.close();
	}
	
	private int convertCommitType(ChangeType changeType) {
		int commitType = -1;
		switch (changeType) {
		case ADD:
			commitType = CommitInfo.ADD_COMMIT;
			break;
		case MODIFY:
			commitType = CommitInfo.MODIFY_COMMIT;
			break;
		case DELETE:
			commitType = CommitInfo.DELETE_COMMIT;
			break;
		case RENAME:
			commitType = CommitInfo.RENAME_COMMIT;
			break;
		case COPY:
			commitType = CommitInfo.COPY_COMMIT;
			break;
		}
		
		return commitType;
	}

}
