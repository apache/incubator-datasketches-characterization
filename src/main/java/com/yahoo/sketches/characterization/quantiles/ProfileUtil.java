/*
 * Copyright 2019, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.characterization.quantiles;

import static com.yahoo.sketches.Util.ceilingPowerOfBdouble;
import static com.yahoo.sketches.Util.logB;
import static com.yahoo.sketches.Util.pwrLawNextDouble;
import static java.lang.Math.ceil;
import static java.lang.Math.max;

import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class ProfileUtil {

  /**
   * Compute the split-points for the PMF function.
   * This assumes all points are positive and may include zero.
   * @param min The data minimum value
   * @param max The data maximum value
   * @param pplb desired number of Points Per Log Base.
   * @param logBase the chosen Log Base
   * @param eps the epsilon added to each split-point
   * @return the split-points array, which may include zero
   */
  public static double[] buildSplitPointsArr(final double min, final double max,
      final int pplb, final double logBase, final double eps) {
    final double ceilPwrBmin = ceilingPowerOfBdouble(logBase, min);
    final double ceilPwrBmax = ceilingPowerOfBdouble(logBase, max);
    final int bot = (int) ceil(logB(logBase, max(1.0, ceilPwrBmin)));
    final int top = (int) ceil(logB(logBase, ceilPwrBmax));
    final int numSP;
    double next;
    final double[] spArr;
    if (min < 1.0) {
      numSP = ((top - bot) * pplb) + 2;
      spArr = new double[numSP];
      spArr[0] = 0;
      spArr[1] = 1;
      next = 1.0;
      for (int i = 2; i < numSP; i++) {
        next = pwrLawNextDouble(pplb, next, false, logBase);
        spArr[i] = next;
      }
    } else {
      numSP = ((top - bot) * pplb) + 1;
      spArr = new double[numSP];
      spArr[0] = ceilPwrBmin;
      next = ceilPwrBmin;
      for (int i = 1; i < numSP; i++) {
        next = pwrLawNextDouble(pplb, next, false, logBase);
        spArr[i] = next;
      }
    }
    if (eps != 0.0) {
      for (int i = 0; i < numSP; i++) {
        if (spArr[i] == 0.0) { spArr[i] = eps; }
        else { spArr[i] *= 1 + eps; }
      }
    }
    return spArr;
  }

  /**
   * Monotonicity check.
   * @param arr Array to check
   */
  public static void checkMonotonic(final double[] arr) {
    final int len = arr.length;
    for (int i = 1; i < len; i++) {
      assert arr[i] > arr[i - 1];
    }
  }

  @Test
  public void checkBuildSPArr() {
    final double[] arr = buildSplitPointsArr(0, 999, 2, 10.0, 1E-6);
    for (int i = 0; i < arr.length; i++) { System.out.println("" + arr[i]); }
  }

}
