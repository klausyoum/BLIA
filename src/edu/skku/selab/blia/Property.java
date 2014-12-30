// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Property.java

package edu.skku.selab.blia;


public class Property
{
    public int getBugTermCount()
    {
        return bugTermCount;
    }

    public void setBugTermCount(int bugTermCount)
    {
        this.bugTermCount = bugTermCount;
    }

    public int getBugReportCount()
    {
        return bugReportCount;
    }

    public void setBugReportCount(int bugReportCount)
    {
        this.bugReportCount = bugReportCount;
    }

    public int getFileCount()
    {
        return fileCount;
    }

    public void setFileCount(int fileCount)
    {
        this.fileCount = fileCount;
    }

    public int getWordCount()
    {
        return wordCount;
    }

    public void setWordCount(int wordCount)
    {
        this.wordCount = wordCount;
    }

    public String getLineSeparator()
    {
        return lineSeparator;
    }

    public String getWorkDir()
    {
        return workDir;
    }

    public static void createInstance(String bugFilePath, String sourceCodeDir, String workDir, float alpha, float beta, String outputFile)
    {
        if(null == p) {
            p = new Property(bugFilePath, sourceCodeDir, workDir, alpha, beta, outputFile);
        }
    }

    public static Property getInstance()
    {
        return p;
    }

    private Property(String bugFilePath, String sourceCodeDir, String workDir, float alpha, float beta, String outputFile)
    {
        this.bugFilePath = bugFilePath;
        this.sourceCodeDir = sourceCodeDir;
        this.workDir = workDir;
        this.alpha = alpha;
        this.beta = beta;
        this.outputFile = outputFile;
    }

    public float getAlpha()
    {
        return alpha;
    }
    
    public float getBeta()
    {
        return beta;
    }    

    public String getOutputFile()
    {
        return outputFile;
    }

    public String getBugFilePath()
    {
        return bugFilePath;
    }

    public String getSourceCodeDir()
    {
        return sourceCodeDir;
    }

    public String getSeparator()
    {
        return separator;
    }

    public final String bugFilePath;
    public final String sourceCodeDir;
    private final String workDir;
    private int fileCount;
    private int wordCount;
    private int bugReportCount;
    private int bugTermCount;
    private final float alpha;
    private final float beta;
    private final String outputFile;
    private final String separator = System.getProperty("file.separator");
    private final String lineSeparator = System.getProperty("line.separator");
    private static Property p = null;

}
