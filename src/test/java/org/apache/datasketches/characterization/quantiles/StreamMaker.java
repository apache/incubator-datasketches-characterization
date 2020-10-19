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

package org.apache.datasketches.characterization.quantiles;

import static org.apache.datasketches.characterization.quantiles.StreamMaker.Pattern.FlipFlop;
import static org.apache.datasketches.characterization.quantiles.StreamMaker.Pattern.Random;
import static org.apache.datasketches.characterization.quantiles.StreamMaker.Pattern.Reversed;
import static org.apache.datasketches.characterization.quantiles.StreamMaker.Pattern.Sorted;
import static org.apache.datasketches.characterization.quantiles.StreamMaker.Pattern.Sqrt;
import static org.apache.datasketches.characterization.quantiles.StreamMaker.Pattern.Zoomin;
import static org.apache.datasketches.characterization.quantiles.StreamMaker.Pattern.Zoomout;

import org.apache.datasketches.characterization.Shuffle;
import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class StreamMaker {
  static final String LS = System.getProperty("line.separator");
  static String TAB = "\t";
  public enum Pattern { Sorted, Reversed, Zoomin, Zoomout, Random, Sqrt, FlipFlop,
    Clustered, ClusteredZoomin }
  public float min = 0;
  public float max = 0;

  public float[] makeStream(int n, Pattern pattern, int offset) {
    float[] arr = new float[n];
    min = offset;
    max = n - 1 + offset;
    switch(pattern) {
      case Sorted: {
        for (int i = 0; i < n; i++) { arr[i] = i + offset; }
        break;
      }
      case Reversed: {
        for (int i = 0; i < n; i++) { arr[n - 1 - i] = i + offset; }
        break;
      }
      case Zoomin: {
        for (int i = 0, j = 0; i < n; i++) {
          if ((i & 1) > 0) { arr[i] = n - j - 1 + offset; j++; } //odd
          else { arr[i] = j + offset; }
        }
        break;
      }
      case Zoomout: {
        for (int i = 0, j = 0; i < n; i++) {
          if ((i & 1) > 0) { arr[n - 1 - i] = n - j - 1 + offset; j++; } //odd
          else { arr[n - 1 - i] = j + offset; }
        }
        break;
      }
      case Random: {
        for (int i = 0; i < n; i++) { arr[i] = i + offset; }
        Shuffle.shuffle(arr);
        break;
      }
      case Sqrt: {
        int idx = 0;
        int t = (int)Math.sqrt(2 * n);
        int item = 0;
        int initialItem = 0;
        int initialSkip = 1;
        for (int i = 0; i < t; i++) {
          item = initialItem;
          int skip = initialSkip;
          for (int j = 0; j < t - i; j++) {
            if (idx > n - 1) { break; }
            arr[idx++] = item + offset;
            item += skip;
            skip += 1;
          }
          if (idx > n - 1) { break; }
          initialSkip += 1;
          initialItem += initialSkip;
        }
        break;
      }
      case FlipFlop: {
        FlipFlopStream ffs = new FlipFlopStream(n, offset);
        ffs.flipFlop(1, 1, n * 2 / 5);
        int m = n / 5;
        ffs.flipFlop(m, 1, m);
        ffs.flipFlop(1, m, m);
        ffs.flipFlop(1, 1, n);
        arr = ffs.getArray();
        break;
      }
      case Clustered: {
        break;
      }
      case ClusteredZoomin: {
        break;
      }
    }
    return arr;
  }

  public void printStream(int n, Pattern order, int offset) {
    float[] stream = makeStream(n, order, offset);
    println(order + " n:" + n + " offset: " + offset);
    for (int i = 0; i < stream.length; i++) {
      //if (i != 0 && i % 21 == 0) { println(""); }
      println(i + TAB + (int)stream[i]);
    }
    println("");
  }

  @Test
  public void checkStreamMaker() {
    printStream(100, Sorted, 1);
    printStream(100, Reversed, 1);
    //printStream(100, Zoomin, 0);
    printStream(100, Zoomin, 1);
    //printStream(100, Zoomout, 0);
    printStream(100, Zoomout, 1);
    //printStream(100, Random, 0);
    printStream(100, Random, 1);
    //printStream(100, Sqrt, 0);
    printStream(100, Sqrt, 1);
    //printStream(100, FlipFlop, 0);
    printStream(100, FlipFlop, 1);
  }

  static void print(Object o) { System.out.print(o.toString()); }

  static void println(Object o) { System.out.println(o.toString()); }
}
