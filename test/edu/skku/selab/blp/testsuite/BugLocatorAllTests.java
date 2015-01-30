package edu.skku.selab.blp.testsuite;

import org.junit.runner.RunWith;


import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.skku.selab.blp.buglocator.analysis.BugLocatorTest;
import edu.skku.selab.blp.buglocator.analysis.BugLocatorWithFileTest;
import edu.skku.selab.blp.buglocator.analysis.BugRepoAnalyzerWithFileTest;
import edu.skku.selab.blp.buglocator.analysis.SourceFileAnalyzerWithFileTest;

import edu.skku.selab.blp.buglocator.indexer.BugCorpusCreatorWithFileTest;
import edu.skku.selab.blp.buglocator.indexer.BugVectorCreatorWithFileTest;
import edu.skku.selab.blp.buglocator.indexer.SourceFileCorpusCreatorWithFileTest;
import edu.skku.selab.blp.buglocator.indexer.SourceFileIndexderWithFileTest;
import edu.skku.selab.blp.buglocator.indexer.SourceFileVectorCreatorWithFileTest;

@RunWith(Suite.class)
@SuiteClasses({
	BugLocatorTest.class, BugLocatorWithFileTest.class,
	BugRepoAnalyzerWithFileTest.class, SourceFileAnalyzerWithFileTest.class,
	BugCorpusCreatorWithFileTest.class, BugVectorCreatorWithFileTest.class,
	SourceFileCorpusCreatorWithFileTest.class, SourceFileIndexderWithFileTest.class,
	SourceFileVectorCreatorWithFileTest.class})
public class BugLocatorAllTests {

}
