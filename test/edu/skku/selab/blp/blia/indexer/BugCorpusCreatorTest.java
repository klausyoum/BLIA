/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.blia.indexer;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.skku.selab.blp.*;
import edu.skku.selab.blp.blia.indexer.SourceFileCorpusCreator;
import edu.skku.selab.blp.blia.indexer.BugCorpusCreator;
import edu.skku.selab.blp.buglocator.indexer.BugCorpusCreatorWithFile;
import edu.skku.selab.blp.buglocator.indexer.SourceFileCorpusCreatorWithFile;
import edu.skku.selab.blp.db.dao.BaseDAO;
import edu.skku.selab.blp.db.dao.DbUtil;
import edu.skku.selab.blp.db.dao.SourceFileDAO;
import edu.skku.selab.blp.test.utils.TestConfiguration;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugCorpusCreatorTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DbUtil dbUtil = new DbUtil();
		dbUtil.initializeAllData();

		TestConfiguration.setProperty();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		BaseDAO.closeConnection();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void verifyCreate() throws Exception {
		String version = SourceFileDAO.DEFAULT_VERSION_STRING;
		SourceFileCorpusCreator sourceFileCorpusCreator = new SourceFileCorpusCreator();
		sourceFileCorpusCreator.create(version);
		
		BugCorpusCreator bugCorpusCreator = new BugCorpusCreator();
		boolean stackTraceAnalysis = false;
		bugCorpusCreator.create(stackTraceAnalysis);
	}
	
	@Test
	public void verifyExtractClassName() {
		String content = "GTK= 2.4.9, Linux 2.6.8.1, I200410050800, KDE 3.3.0 I'm not sure if this is the right component for this, and I'm just about to head home. Just found this in my log, and it didn't seem to have a noticeable side effects. java.lang.NullPointerException at org.eclipse.ui.internal.console.IOConsolePartitioner.getRegion(IOConsolePartitioner.java:288) at org.eclipse.ui.internal.console.IOConsoleViewer.paintControl(IOConsoleViewer.java:203) at org.eclipse.swt.widgets.TypedListener.handleEvent(TypedListener.java:82) at org.eclipse.swt.widgets.EventTable.sendEvent(EventTable.java:82) at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:977) at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1001) at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:986) at org.eclipse.swt.widgets.Control.gtk_expose_event(Control.java:1801) at org.eclipse.swt.widgets.Composite.gtk_expose_event(Composite.java:421) at org.eclipse.swt.widgets.Canvas.gtk_expose_event(Canvas.java:106) at org.eclipse.swt.widgets.Widget.windowProc(Widget.java:1308) at org.eclipse.swt.widgets.Display.windowProc(Display.java:3169) at ";
		
		BugCorpusCreator bugCorpusCreator = new BugCorpusCreator();
		ArrayList<String> classNames = bugCorpusCreator.extractClassName(content);
		
		assertEquals("org.eclipse.ui.internal.console.IOConsolePartitioner", classNames.get(0));
		assertEquals("org.eclipse.ui.internal.console.IOConsoleViewer", classNames.get(1));
		assertEquals("org.eclipse.swt.widgets.TypedListener", classNames.get(2));
		assertEquals("org.eclipse.swt.widgets.EventTable", classNames.get(3));
		assertEquals("org.eclipse.swt.widgets.Widget", classNames.get(4));
		assertEquals("org.eclipse.swt.widgets.Widget", classNames.get(5));
		assertEquals("org.eclipse.swt.widgets.Widget", classNames.get(6));
		assertEquals("org.eclipse.swt.widgets.Control", classNames.get(7));
		assertEquals("org.eclipse.swt.widgets.Composite", classNames.get(8));
		assertEquals("org.eclipse.swt.widgets.Canvas", classNames.get(9));
		assertEquals("org.eclipse.swt.widgets.Widget", classNames.get(10));
		assertEquals("org.eclipse.swt.widgets.Display", classNames.get(11));
		
		
		content = "I200411041200, GTK+ 2.4.9, KDE 3.3.0, Linux 2.6.9 I was creating new simple files in existing projects, and then deleting them. I was using the keyboard heavily for navigation. I found the exception below in the log. There was no major effect from this null pointer, but it might have been responsible for some buttons not disabling when they should. I'll investiage more. !ENTRY org.eclipse.ui 4 4 2004-11-05 08:51:21.199 !MESSAGE Unhandled event loop exception !ENTRY org.eclipse.ui 4 0 2004-11-05 08:51:21.235 !MESSAGE java.lang.NullPointerException !STACK 0 java.lang.NullPointerException at org.eclipse.swt.custom.CLabel.findMnemonic(CLabel.java:194) at org.eclipse.swt.custom.CLabel.onMnemonic(CLabel.java:334) at org.eclipse.swt.custom.CLabel$3.keyTraversed(CLabel.java:126) at org.eclipse.swt.widgets.TypedListener.handleEvent(TypedListener.java:221) at ";
		
		classNames = bugCorpusCreator.extractClassName(content);
		assertEquals("org.eclipse.swt.custom.CLabel", classNames.get(0));
		assertEquals("org.eclipse.swt.custom.CLabel", classNames.get(1));
		assertEquals("org.eclipse.swt.custom.CLabel", classNames.get(2));
		assertEquals("org.eclipse.swt.widgets.TypedListener", classNames.get(3));
		
		content = "Here is a stack trace I found when trying to kill a running process by pressing the &amp;quot;kill&amp;quot; button in the console view. I use 3.1M5a. !ENTRY org.eclipse.ui 4 0 2005-03-12 14:26:25.58 !MESSAGE java.lang.NullPointerException !STACK 0 java.lang.NullPointerException at org.eclipse.swt.widgets.Table.callWindowProc(Table.java:156) at org.eclipse.swt.widgets.Table.sendMouseDownEvent(Table.java:2084) at org.eclipse.swt.widgets.Table.WM_LBUTTONDOWN(Table.java:3174) at org.eclipse.swt.widgets.Control.windowProc(Control.java:3057) at org.eclipse.swt.widgets.Display.windowProc(Display.java:3480) at org.eclipse.swt.internal.win32.OS.DispatchMessageW(Native Method) at org.eclipse.swt.internal.win32.OS.DispatchMessage(OS.java:1619) at ";
		classNames = bugCorpusCreator.extractClassName(content);
		assertEquals("org.eclipse.swt.widgets.Table", classNames.get(0));
		assertEquals("org.eclipse.swt.widgets.Table", classNames.get(1));
		assertEquals("org.eclipse.swt.widgets.Table", classNames.get(2));
		assertEquals("org.eclipse.swt.widgets.Control", classNames.get(3));
		assertEquals("org.eclipse.swt.widgets.Display", classNames.get(4));
		assertEquals("org.eclipse.swt.internal.win32.OS", classNames.get(5));
		assertEquals("org.eclipse.swt.internal.win32.OS", classNames.get(6));
	}
}
