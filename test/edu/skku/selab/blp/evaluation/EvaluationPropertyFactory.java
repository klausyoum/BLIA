/**
 * Copyright (c) 2014 by Software Engineering Lab. of Sungkyunkwan University. All Rights Reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its documentation for
 * educational, research, and not-for-profit purposes, without fee and without a signed licensing agreement,
 * is hereby granted, provided that the above copyright notice appears in all copies, modifications, and distributions.
 */
package edu.skku.selab.blp.evaluation;

import java.util.Calendar;
import java.util.GregorianCalendar;

import edu.skku.selab.blp.Property;

/**
 * @author Klaus Changsun Youm(klausyoum@skku.edu)
 *
 */
public class EvaluationPropertyFactory {
	public static EvaluationProperty getEvaluationProperty(String productName) {
		EvaluationProperty evaluationProperty = null;
		
		double alpha;   
		double beta;
		int pastDays;
		String repoDir;
		Calendar since;
		Calendar until;
		
		switch(productName) {
		case Property.SWT:
			alpha = 0.24;  
			beta = 0.1;
			pastDays = 15;
			repoDir = Property.SWT_REPO_DIR;
			
			// for swt project ONLY
			// There is a bug that opened at April of 2002
			//   <bug id="14654" opendate="2002-04-25 13:35:00" fixdate="2007-04-09 11:23:00">
			since = new GregorianCalendar(2002, Calendar.APRIL, 1);
			until = new GregorianCalendar(2010, Calendar.MAY, 1);
			evaluationProperty = new EvaluationProperty(productName, alpha, beta, pastDays, repoDir, since, until);
			break;
		case Property.ASPECTJ:
			alpha = 0.41;	// a = 0.41 & b = 0.13 is best maybe // 2015/04/09   
			beta = 0.16;
			pastDays = 60;
			repoDir = Property.ASPECTJ_REPO_DIR;
			
			// for aspectj project ONLY
			// Following bug is the oldest one.
			//   <bug fixdate="2003-1-14 15:06:00" id="28919" opendate="2002-12-30 16:40:00">
			// Following bug is the newest one.
			//   <bug fixdate="2010-5-12 7:04:00" id="150271" opendate="2006-7-11 11:31:00">
			since = new GregorianCalendar(2002, Calendar.JULY, 1);
			until = new GregorianCalendar(2010, Calendar.MAY, 15);
			evaluationProperty = new EvaluationProperty(productName, alpha, beta, pastDays, repoDir, since, until);
			break;
		case Property.ZXING:
			alpha = 0.15;  
			beta = 0.5;
			pastDays = 15;
			repoDir = Property.ZXING_REPO_DIR;

			// for zxing project ONLY
			since = new GregorianCalendar(2010, Calendar.MARCH, 1);
			until = new GregorianCalendar(2010, Calendar.SEPTEMBER, 30);
			evaluationProperty = new EvaluationProperty(productName, alpha, beta, pastDays, repoDir, since, until);
			break;
		case Property.ECLIPSE:
			// TODO: find optimized alpha, beta value
			alpha = 0.3;  
			beta = 0.2;
			pastDays = 15;
			repoDir = Property.ECLIPSE_REPO_DIR;

			// for ecplise project ONLY
			//   <bug id="76098" opendate="2004-10-12 12:29:00" fixdate="2004-10-12 17:56:00">
			since = new GregorianCalendar(2004, Calendar.AUGUST, 1);
			until = new GregorianCalendar(2011, Calendar.MARCH, 31);
			evaluationProperty = new EvaluationProperty(productName, alpha, beta, pastDays, repoDir, since, until);
			break;
		default :
			break;
		}
		
		return evaluationProperty;
	}
}
