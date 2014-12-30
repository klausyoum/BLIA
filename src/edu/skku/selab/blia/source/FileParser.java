/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.source;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.skku.selab.blia.utils.Splitter;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class FileParser {
	private CompilationUnit compilationUnit;

	public FileParser(File file) {
		compilationUnit = null;
		ASTCreator creator = new ASTCreator();
		creator.getFileContent(file);
		compilationUnit = creator.getCompilationUnit();
	}

	public int getLinesOfCode() {
		deleteNoNeededNode();
		String lines[] = compilationUnit.toString().split("\n");
		int len = 0;
		String as[];
		int j = (as = lines).length;
		for (int i = 0; i < j; i++) {
			String strLine = as[i];
			if (!strLine.trim().equals(""))
				len++;
		}

		return len;
	}

	public String[] getContent() {
		String tokensInSourceCode[] = Splitter.splitSourceCode(deleteNoNeededNode());
		StringBuffer sourceCodeContentBuffer = new StringBuffer();
		String as[];
		int j = (as = tokensInSourceCode).length;
		for (int i = 0; i < j; i++) {
			String token = as[i];
			sourceCodeContentBuffer.append((new StringBuilder(String
					.valueOf(token))).append(" ").toString());
		}

		String content = sourceCodeContentBuffer.toString().toLowerCase();
		return content.split(" ");
	}

	public String[] getClassNameAndMethodName() {
		String content = (new StringBuilder(String.valueOf(getAllClassName()))).append(" ")
				.append(getAllMethodName()).toString().toLowerCase();
		return content.split(" ");
	}

	public String getPackageName() {
		return compilationUnit.getPackage() != null ?
				compilationUnit.getPackage().getName().getFullyQualifiedName() :
					"";
	}

	private String getAllMethodName() {
		ArrayList<String> methodNameList = new ArrayList<String>();
		for (int i = 0; i < compilationUnit.types().size(); i++) {
			TypeDeclaration type = (TypeDeclaration) compilationUnit.types().get(i);
			MethodDeclaration methodDecls[] = type.getMethods();
			MethodDeclaration amethoddeclaration[];
			int k = (amethoddeclaration = methodDecls).length;
			for (int j = 0; j < k; j++) {
				MethodDeclaration methodDecl = amethoddeclaration[j];
				String methodName = methodDecl.getName().getFullyQualifiedName();
				methodNameList.add(methodName);
			}

		}

		String allMethodName = "";
		for (Iterator<String> iterator = methodNameList.iterator(); iterator.hasNext();) {
			String methodName = (String) iterator.next();
			allMethodName = (new StringBuilder(String.valueOf(allMethodName)))
					.append(methodName).append(" ").toString();
		}

		return allMethodName.trim();
	}

	private String getAllClassName() {
		ArrayList<String> classNameList = new ArrayList<String>();
		for (int i = 0; i < compilationUnit.types().size(); i++) {
			TypeDeclaration type = (TypeDeclaration) compilationUnit.types().get(i);
			String name = type.getName().getFullyQualifiedName();
			classNameList.add(name);
		}

		String allClassName = "";
		for (Iterator<String> iterator = classNameList.iterator(); iterator.hasNext();) {
			String className = (String) iterator.next();
			allClassName = (new StringBuilder(String.valueOf(allClassName)))
					.append(className).append(" ").toString();
		}

		return allClassName.trim();
	}

	private String deleteNoNeededNode() {
		compilationUnit.accept(new ASTVisitor() {
			public boolean visit(AnnotationTypeDeclaration node) {
				if (node.isPackageMemberTypeDeclaration())
					node.delete();
				return super.visit(node);
			}
		});
		
		compilationUnit.accept(new ASTVisitor() {
			public boolean visit(PackageDeclaration node) {
				node.delete();
				return super.visit(node);
			}
		});
		
		compilationUnit.accept(new ASTVisitor() {
			public boolean visit(ImportDeclaration node) {
				node.delete();
				return super.visit(node);
			}
		});
		
		return compilationUnit.toString();
	}
}
