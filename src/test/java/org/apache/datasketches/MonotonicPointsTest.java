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

package org.apache.datasketches;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * @author Lee Rhodes
 */
public class MonotonicPointsTest {


  @Test
  public void checkMonotonicPoints() {
    double[] arr = MonotonicPoints.evenlySpaced(0.0, 100.0, 21, false);
    for (int i = 0; i < arr.length; i++) { println(arr[i] + ""); }
  }

  @Test
  public void checkMonotonicPoints2() {
    double[] arr = MonotonicPoints.evenlySpaced(0, 1, 3, false);
    assertEquals(arr[0], 0.0);
    assertEquals(arr[1], 0.5);
    assertEquals(arr[2], 1.0);
    arr = MonotonicPoints.evenlySpaced(3, 7, 3, false);
    assertEquals(arr[0], 3.0);
    assertEquals(arr[1], 5.0);
    assertEquals(arr[2], 7.0);
  }

  @Test void checkEvenlySpacedPoints() {
    double[] arr = Util.evenlySpaced(0.0, 100.0, 21);
    for (int i = 0; i < arr.length; i++) { println(arr[i] + ""); }
  }

  static void println(Object o) { System.out.println(o.toString()); }
}
