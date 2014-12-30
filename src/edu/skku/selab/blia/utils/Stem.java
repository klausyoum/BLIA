// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Stem.java

package edu.skku.selab.blia.utils;


// Referenced classes of package utils:
//            PorterStemmer

public class Stem
{

    public Stem()
    {
    }

    public static String stem(String word)
    {
        stemmer.reset();
        stemmer.stem(word);
        return stemmer.toString();
    }

    private static PorterStemmer stemmer = new PorterStemmer();

}
