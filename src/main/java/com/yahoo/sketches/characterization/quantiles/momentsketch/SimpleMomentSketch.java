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

/**
 * Example wrapper class for exposing an approximate quantiles sketch API
 * Uses the MomentSolver and MomentStruct internally and optionally compressed
 * the input range using arcsinh.
 */
public class SimpleMomentSketch {
  public MomentStruct data;
  // Whether we use arcsinh to compress the range
  public boolean useArcSinh = true;

  public SimpleMomentSketch(
      int k) {
    data = new MomentStruct(k);
  }

  public SimpleMomentSketch(
      MomentStruct data) {
    this.data = data;
  }

  public void setCompressed(boolean flag) {
    useArcSinh = flag;
  }

  public boolean getCompressed() {
    return useArcSinh;
  }

  public int getK() {
    return data.power_sums.length;
  }

  public double[] getPowerSums() {
    return data.power_sums;
  }

  public double getMin() {
    return data.min;
  }

  public double getMax() {
    return data.max;
  }

  public void add(double rawX) {
    double x = rawX;
    if (useArcSinh) {
      x = Math.log(rawX + Math.sqrt(1 + (rawX * rawX)));
    }
    data.add(x);
  }

  public void merge(SimpleMomentSketch other) {
    data.merge(other.data);
  }

  public MomentSolver getSolver() {
    MomentSolver ms = new MomentSolver(data);
    return ms;
  }

  public double[] getQuantiles(double[] fractions) {
    MomentSolver ms = new MomentSolver(data);
    ms.setGridSize(1024);
    ms.setMaxIter(15);
    ms.solve();

    double[] quantiles = new double[fractions.length];
    for (int i = 0; i < fractions.length; i++) {
      double rawQuantile = ms.getQuantile(fractions[i]);
      quantiles[i] = Math.sinh(rawQuantile);
    }

    return quantiles;
  }

  @Override
  public String toString() {
    return data.toString();
  }
}
