/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blia.indexer;

import java.io.FileWriter;

import edu.udo.cs.wvtool.config.WVTConfigException;
import edu.udo.cs.wvtool.config.WVTConfiguration;
import edu.udo.cs.wvtool.config.WVTConfigurationFact;
import edu.udo.cs.wvtool.config.WVTConfigurationRule;
import edu.udo.cs.wvtool.generic.output.WordVectorWriter;
import edu.udo.cs.wvtool.generic.stemmer.LovinsStemmerWrapper;
import edu.udo.cs.wvtool.generic.stemmer.PorterStemmerWrapper;
import edu.udo.cs.wvtool.generic.stemmer.WVTStemmer;
import edu.udo.cs.wvtool.generic.vectorcreation.TFIDF;
import edu.udo.cs.wvtool.main.WVTDocumentInfo;
import edu.udo.cs.wvtool.main.WVTFileInputList;
import edu.udo.cs.wvtool.main.WVTool;
import edu.udo.cs.wvtool.wordlist.WVTWordList;
import edu.skku.selab.blia.Property;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class BugVectorCreator implements IVectorCreator {

	private static final String HOME_FOLDER = (new StringBuilder(String.valueOf(Property.getInstance().getWorkDir()))).append(Property.getInstance().getSeparator()).toString();
    private static final String BUG_CORPUS_FOLDER = (new StringBuilder("BugCorpus")).append(Property.getInstance().getSeparator()).toString();
	
	
	/* (non-Javadoc)
	 * @see edu.skku.selab.blia.indexer.IVectorCreator#create()
	 */
	@Override
	public void create() throws Exception {
		WVTool wvt = new WVTool(false);
		WVTConfiguration config = new WVTConfiguration();
		final WVTStemmer porterStemmer = new PorterStemmerWrapper();
		config.setConfigurationRule("stemmer", new WVTConfigurationRule() {
			public Object getMatchingComponent(WVTDocumentInfo d)
					throws WVTConfigException {
				return porterStemmer;
			}
		});
		WVTStemmer stemmer = new LovinsStemmerWrapper();
		config.setConfigurationRule("stemmer", new WVTConfigurationFact(stemmer));
		WVTFileInputList list = new WVTFileInputList(1);
		list.addEntry(new WVTDocumentInfo((new StringBuilder(String.valueOf(HOME_FOLDER))).append(BUG_CORPUS_FOLDER).toString(), "txt", "", "english", 0));
		WVTWordList wordList = wvt.createWordList(list, config);
		wordList.pruneByFrequency(1, 0x7fffffff);
		
		int termCount = wordList.getNumWords();
		Property.getInstance().setBugTermCount(termCount);
		wordList.storePlain(new FileWriter((new StringBuilder(String.valueOf(HOME_FOLDER))).append("BugTermList.txt").toString()));
		FileWriter outFile = new FileWriter((new StringBuilder(String.valueOf(HOME_FOLDER))).append("BugVector.txt").toString());
		WordVectorWriter wvw = new WordVectorWriter(outFile, true);
		config.setConfigurationRule("output", new WVTConfigurationFact(wvw));
		config.setConfigurationRule("vectorcreation", new WVTConfigurationFact(new TFIDF()));
		wvt.createVectors(list, config, wordList);
		wvw.close();
		outFile.close();
	}
}
