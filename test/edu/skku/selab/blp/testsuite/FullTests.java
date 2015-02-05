package edu.skku.selab.blp.testsuite;

import org.junit.runner.RunWith;

import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import edu.skku.selab.blp.common.BugTest;
import edu.skku.selab.blp.evaluation.EvaluatorTest;

@RunWith(Suite.class)
@SuiteClasses({
	BugTest.class,
	DAOAllTests.class,
	BugLocatorAllTests.class,
	BLIAAllTests.class,
	EvaluatorTest.class})
public class FullTests {

}
