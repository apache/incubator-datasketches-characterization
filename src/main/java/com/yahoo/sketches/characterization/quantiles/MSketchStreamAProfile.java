/*
 * Copyright 2019, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.characterization.quantiles;

import static java.lang.Math.rint;

import java.io.File;

import com.yahoo.sketches.LineReader;
import com.yahoo.sketches.ProcessLine;
import com.yahoo.sketches.UnzipFiles;
import com.yahoo.sketches.characterization.Job;
import com.yahoo.sketches.characterization.JobProfile;
import com.yahoo.sketches.characterization.Properties;
import com.yahoo.sketches.characterization.quantiles.momentsketch.SimpleMomentSketch;

/**
 * @author Lee Rhodes
 */
public class MSketchStreamAProfile implements JobProfile {

  private Job job;

  //Properties
  private String srcFileName;
  private int reportInterval; //prints number of lines read to console every reportInterval lines.
  private int numRanks; //number of linearly spaced ranks between zero and one.
  //private int ppOoM; //number of split-points per Order-Of-Magnitude (OOM).
  private int moments; //number of moments
  private String cdfHdr;
  private String cdfFmt;
  //private String pmfHdr;
  //private String pmfFmt;

  SimpleMomentSketch sketch;

  private boolean dataWasZipped = false;
  //private double eps = 1e-6;
  private Process proc = new Process();

  //outputs for plotting
  private double minV;
  private double maxV;
  private long numItems = 0;

  //JobProfile
  @Override
  public void start(final Job job) {
    this.job = job;
    final Properties prop = job.getProperties();
    //Get Properties
    srcFileName = prop.mustGet("FileName");
    reportInterval = Integer.parseInt(prop.mustGet("ReportInterval"));
    numRanks = Integer.parseInt(prop.mustGet("NumRanks"));
    //logBase = Double.parseDouble(prop.mustGet("LogBase"));
    //pplb = Integer.parseInt(prop.mustGet("PPLB"));

    moments = Integer.parseInt(prop.mustGet("Moments"));

    cdfHdr = prop.mustGet("CdfHdr").replace("\\t", "\t");
    cdfFmt = prop.mustGet("CdfFmt").replace("\\t", "\t");
    //pmfHdr = prop.mustGet("PdfHdr").replace("\\t", "\t");
    //pmfFmt = prop.mustGet("PdfFmt").replace("\\t", "\t");

    sketch = new SimpleMomentSketch(moments);

    processInputStream();

    if (dataWasZipped) {
      final File file = new File(srcFileName);
      if (file.exists()) { file.delete(); }
    }
  }

  /**
   * Read file, Print CDF, Print PMF.
   */
  private void processInputStream() {
    checkIfZipped(srcFileName);

    //Read
    println("");
    println("Input Lines Processed: ");
    final LineReader lineReader = new LineReader(srcFileName);

    final long startReadTime_nS = System.nanoTime();
    lineReader.read(0, proc);
    final long readTime_nS = System.nanoTime() - startReadTime_nS;
    numItems = proc.n;
    println("");

    //print sketch stats
    println("Sketch.toString()");
    println(sketch.toString().replace(", ", "\n"));
    minV = sketch.getMin();
    maxV = sketch.getMax();
    println("Min: " + minV);
    println("Max: " + maxV);
    println("NumItems: " + numItems);
    println("");

    //CDF
    final double[] fracRanks = buildRanksArr(numRanks);
    final long startCdfTime_nS = System.nanoTime();
    final double[] quantiles = sketch.getQuantiles(fracRanks);
    final long cdfTime_nS = System.nanoTime() - startCdfTime_nS;

    println("CDF");
    println(String.format(cdfHdr, "Index", "Rank", "Quantile"));
    for (int i = 0; i < numRanks; i++) {
      final String s = String.format(cdfFmt, i, fracRanks[i], quantiles[i]);
      println(s);
    }
    println("");


    final double readTime_S = readTime_nS / 1E9;

    println(String.format("ReadTime_Sec  :\t%10.3f", readTime_S));
    println(String.format("ReadRate/Sec  :\t%,10.0f", numItems / readTime_S));
    println(String.format("CdfTime_mSec  :\t%10.3f", cdfTime_nS / 1E6));
    println(String.format("Cdf/Point_nSec:\t%10.3f", (double)cdfTime_nS / numRanks));
    //println(String.format("PmfTime_mSec  :\t%10.3f", pmfTime_nS / 1E6));
    //println(String.format("Pmf/Point_nSec:\t%10.3f", (double)pmfTime_nS / lenPMF));
  }


  /**
   * Compute the ranks array.
   * @param numRanks the number of evenly-spaced rank values including 0 and 1.0.
   * @return the ranks array
   */
  private static double[] buildRanksArr(final int numRanks) {
    final int numRM1 = numRanks - 1;
    final double[] fractions = new double[numRanks];
    final double delta = 1.0 / (numRM1);
    double d = 0.0;
    for (int i = 0; i < numRanks; i++) {
      fractions[i] = d;
      d += delta;
      d = rint(d * numRM1) / numRM1;
    }
    return fractions;
  }

  private void checkIfZipped(final String srcFileName) {
    final File file = new File(srcFileName);
    if (!file.exists()) {
      final String srcZipFile = srcFileName + ".zip";
      final File zipFile = new File(srcZipFile);
      if (!zipFile.exists()) {
        throw new IllegalArgumentException("Neither file nor zipFile exists.");
      }
      final String parent = zipFile.getParent();
      println("Unzipping data file: " + srcZipFile + "...");
      UnzipFiles.unzip(srcZipFile, parent);
      if (!zipFile.exists()) {
        throw new IllegalArgumentException("Unsuccessful Unzip.");
      }
      println(srcZipFile + " unzipped!");
      dataWasZipped = true;
    }
  }

  @Override
  public void shutdown() {}

  @Override
  public void cleanup() {}

  @Override
  public void println(final String s) {
    job.println(s);
  }

  // Callback
  class Process implements ProcessLine {
    int n;

    @Override
    public void process(final String strArr0, final int lineNo) {
      if ((lineNo % reportInterval) == 0) {
        println("" + lineNo);
      }
      final long v = Long.parseLong(strArr0);
      sketch.add(v);
      n++;
    }
  }

}