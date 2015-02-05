package edu.skku.selab.blp.testsuite;

import org.junit.runner.RunWith;

import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.skku.selab.blp.blia.analysis.BugRepoAnalyzerTest;
import edu.skku.selab.blp.blia.analysis.SourceFileAnalyzerTest;

import edu.skku.selab.blp.blia.indexer.BugCorpusCreatorTest;
import edu.skku.selab.blp.blia.indexer.BugVectorCreatorTest;
import edu.skku.selab.blp.blia.indexer.SourceFileCorpusCreatorTest;
import edu.skku.selab.blp.blia.indexer.SourceFileIndexderTest;
import edu.skku.selab.blp.blia.indexer.SourceFileVectorCreatorTest;

@RunWith(Suite.class)
@SuiteClasses({
	BugRepoAnalyzerTest.class, SourceFileAnalyzerTest.class,
	BugCorpusCreatorTest.class, BugVectorCreatorTest.class,
	SourceFileCorpusCreatorTest.class, SourceFileIndexderTest.class,
	SourceFileVectorCreatorTest.class})
public class BLIAAllTests {

}
