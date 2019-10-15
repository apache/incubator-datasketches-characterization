/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.yahoo.sketches.characterization.quantiles.momentsketch;

//CHECKSTYLE.OFF: FinalLocalVariable
//CHECKSTYLE.OFF: JavadocMethod
//CHECKSTYLE.OFF: LineLength
//CHECKSTYLE.OFF: OperatorWrap
//CHECKSTYLE.OFF: NonEmptyAtclauseDescription
//CHECKSTYLE.OFF: JavadocParagraph
//CHECKSTYLE.OFF: WhitespaceAround
//CHECKSTYLE.OFF: EmptyLineSeparator

import java.util.Arrays;

/**
 * Structure for storing the statistics in a Moments Sketch.
 *
 * Unlike in the paper we only store power sums here and omit log moments.
 * When the data can be skewed or have outliers one should preprocess the data using either
 * a log transform or an arcsinh transform beforehand.
 */
public class MomentStruct {
  public double[] power_sums;
  public double min, max;

  /**
   * Initialize a sketch with pre-computed statistics
   * 
   * @param pSums sums of powers of data values sum x^i starting with the count i=0
   * @param min the minimum observed value
   * @param max the maximum observed value
   */
  public MomentStruct(
      double[] pSums, double min, double max) {
    power_sums = pSums;
    this.min = min;
    this.max = max;
  }

  /**
   * @param k number of moments to track. [2,20] is the useful range.
   */
  public MomentStruct(
      int k) {
    power_sums = new double[k];
    min = Double.MAX_VALUE;
    max = -Double.MAX_VALUE;
  }

  /**
   * @param x value to add to the sketch
   */
  public void add(double x) {
    if (x < min) {
      min = x;
    }
    if (x > max) {
      max = x;
    }
    double curPow = 1.0;
    int k = power_sums.length;
    for (int i = 0; i < k; i++) {
      power_sums[i] += curPow;
      curPow *= x;
    }
  }

  public void add(double[] xVals) {
    for (double x : xVals) {
      add(x);
    }
  }

  /**
   * @param other existing moment sketch structure to aggregate into current structure
   */
  public void merge(MomentStruct other) {
    if (other.min < min) {
      min = other.min;
    }
    if (other.max > max) {
      max = other.max;
    }
    int k = power_sums.length;
    for (int i = 0; i < k; i++) {
      power_sums[i] += other.power_sums[i];
    }
  }

  @Override
  public String toString() {
    return String.format("%g:%g:%s", min, max, Arrays.toString(power_sums));
  }

}
