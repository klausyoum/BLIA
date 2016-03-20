/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.utils.temp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Jun Ahn(ahnjune@skku.edu)
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class CommentReaderUtil {
	private WebDriver driver;

	@Before
	public void setUp() throws Exception {
		driver = new FirefoxDriver();
		Thread.sleep(3000);
	}
	
	@Test
	public void addCommentsToRepository() throws Exception {
		// Select one product to generate extended bug repository.
//		String productName = "AspectJ";
		String productName = "SWT";
		
		String bugRepoFileName = productName + "BugRepository.xml";
		
			//No comment on the Bug repository file name (before Bug repository Name : ex : SWTBugrepository.xml)
			File bugRepoXmlFile = new File("./data/old/" + bugRepoFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(bugRepoXmlFile);
					
			doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
					
			NodeList bugNodeList = doc.getElementsByTagName("bug");
			
			String path = "./data/";
			//New Add Comment on the Bug repository file name (after Bug repository Name : ex : New_SWTBugrepository.xml)
			BufferedWriter out = new BufferedWriter(new FileWriter(path + bugRepoFileName));
			
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
				
					String fixedFileStartTag = "    <fixedFiles>\n";
						String fileStartTag = "      <file>";
						String fileEndTag = "</file>\n";
					String fixedFileEndTag = "    </fixedFiles>\n";
				
			String bugIdEndTag = "  </bug>\n";
			String bugRepositoryEndTag = "</bugrepository>\n";
			
		System.out.println("----------------------------");
		
		out.write(xml);
		out.write(bugrepositoryStartTag);
	
//		for (int j = 0; j < 2; j++) {
		for (int j = 0; j < bugNodeList.getLength(); j++) {
			Node bugNode = bugNodeList.item(j);

			if (bugNode.getNodeType() == Node.ELEMENT_NODE) {

				Element bugElement = (Element) bugNode;
				
				String bugId = bugElement.getAttribute("id");
				String bugOpenDate = bugElement.getAttribute("opendate");
				String bugFixDate = bugElement.getAttribute("fixdate");
				
				out.write(bugIdStartTag + "\"" + bugId + "\" ");
				out.write(bugOpenDateAttr + "\"" + bugOpenDate + "\" ");
				out.write(bugFixedDateAttr + "\"" + bugFixDate + "\">\n");
				
				out.write(bugInformationStartTag);
				
				out.write(bugSummaryStartTag);
				String bugSummary = bugElement.getElementsByTagName("summary").item(0).getTextContent();
				bugSummary = bugSummary.replaceAll("&", "&amp;");
				bugSummary = bugSummary.replaceAll("<", "&lt;");
				bugSummary = bugSummary.replaceAll(">", "&gt;");
				out.write(bugSummary);
				out.write(bugSummaryEndTag);
				
				out.write(bugDescriptionStartTag);
				String bugDescription = bugElement.getElementsByTagName("description").item(0).getTextContent();
				bugDescription = bugDescription.replaceAll("&", "&amp;");
				bugDescription = bugDescription.replaceAll("<", "&lt;");
				bugDescription = bugDescription.replaceAll(">", "&gt;");
//				bugDescription = bugDescription.replace(System.getProperty("line.separator"), ""); 
				
				out.write(bugDescription);
				out.write(bugDescriptionEndTag);
				
				System.out.println("Bug ID : " + bugElement.getAttribute("id"));
				System.out.println("Open date : " + bugElement.getAttribute("opendate"));
				System.out.println("Fixed date : " + bugElement.getAttribute("fixdate"));
				
				System.out.println("Summary : " + bugElement.getElementsByTagName("summary").item(0).getTextContent());
				System.out.println("Description : " + bugElement.getElementsByTagName("description").item(0).getTextContent());
		
				out.write(commentsStartTag);
						driver.get("https://bugs.eclipse.org/bugs/show_bug.cgi?id=" + bugElement.getAttribute("id"));
						Thread.sleep(1000);
						int commentCount = driver.findElements(By.xpath("/html/body/div[2]/form/div[2]/table/tbody/tr/td[1]/div")).size();
						Thread.sleep(1000);
						for (int commentIndex=2; commentIndex <= commentCount; commentIndex++){
							
							String commentID= driver.findElement(By.xpath("/html/body/div[2]/form/div[2]/table/tbody/tr/td[1]/div[" + commentIndex + "]/div/span[1]/a")).getText();
							String author = driver.findElement(By.xpath("/html/body/div[2]/form/div[2]/table/tbody/tr/td[1]/div[" + commentIndex +"]/div/span[2]")).getText();
							String committedDate = driver.findElement(By.xpath("/html/body/div[2]/form/div[2]/table/tbody/tr/td[1]/div["+ commentIndex + "]/div/span[4]")).getText();
							String commentDescription = driver.findElement(By.xpath("/html/body/div[2]/form/div[2]/table/tbody/tr/td[1]/div[" + commentIndex + "]/pre")).getText();
							commentDescription = commentDescription.replaceAll("&", "&amp;");
							commentDescription = commentDescription.replaceAll("<", "&lt;");
							commentDescription = commentDescription.replaceAll(">", "&gt;");
							commentDescription = commentDescription.replaceAll("\"", "&quot;");
//							description = description.replace(System.getProperty("line.separator"), ""); 
							
//							String replacedDate = committedDate.replaceAll("[a-zA-Z]", "").trim();
							String replaceComment = commentID.replaceAll("[a-zA-Z]", "").trim();
							
							out.write(commentIdTag + "\"" + replaceComment + "\" ");
							out.write(commentDateAttr + "\"" + committedDate + "\" ");
							out.write(commentAuthorAttr + "\"" + author + "\">" + commentDescription);
							out.write(commentEndTag);
							
							//CommentID
							System.out.println(replaceComment + "\n");
							//Comment Author
							System.out.println(author + "\n");
							//Date
							System.out.println(committedDate + "\n");
							//Comment
							System.out.println(commentDescription + "\n");
						}
				out.write(commentsEndTag);
				out.write(bugInformationEndTag);
				out.write(fixedFileStartTag);
				
				NodeList sections = bugElement.getElementsByTagName("file");
		        int numSections = sections.getLength();
		        for(int i = 0; i < numSections; i++){
		        	System.out.println("File : " + bugElement.getElementsByTagName("file").item(i).getTextContent());
		        	
		        	out.write(fileStartTag + bugElement.getElementsByTagName("file").item(i).getTextContent() + fileEndTag);
		        	
		        }
		        out.write(fixedFileEndTag);
		        out.write(bugIdEndTag);
		}		
	}
	out.write(bugRepositoryEndTag);
	out.close();
}
	
	@After
	public void tearDown() throws Exception {
		driver.quit();
	}	
}
