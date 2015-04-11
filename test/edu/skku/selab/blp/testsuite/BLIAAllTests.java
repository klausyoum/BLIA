package edu.skku.selab.blp.testsuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import edu.skku.selab.blp.blia.analysis.BliaTest;
import edu.skku.selab.blp.blia.analysis.BugRepoAnalyzerTest;
import edu.skku.selab.blp.blia.analysis.ScmRepoAnalyzerTest;
import edu.skku.selab.blp.blia.analysis.SourceFileAnalyzerTest;
import edu.skku.selab.blp.blia.analysis.StackTraceAnalyzerTest;
import edu.skku.selab.blp.blia.indexer.BugCorpusCreatorTest;
import edu.skku.selab.blp.blia.indexer.BugVectorCreatorTest;
import edu.skku.selab.blp.blia.indexer.GitCommitLogCollectorTest;
import edu.skku.selab.blp.blia.indexer.SourceFileCorpusCreatorTest;
import edu.skku.selab.blp.blia.indexer.SourceFileVectorCreatorTest;

@RunWith(Suite.class)
@SuiteClasses({
	BliaTest.class,
	BugRepoAnalyzerTest.class, ScmRepoAnalyzerTest.class,
	SourceFileAnalyzerTest.class,StackTraceAnalyzerTest.class,
	BugCorpusCreatorTest.class, BugVectorCreatorTest.class,
	GitCommitLogCollectorTest.class, 
	SourceFileCorpusCreatorTest.class, SourceFileVectorCreatorTest.class})
public class BLIAAllTests {

}
