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

#include <cpc_union.hpp>

#include "cpc_union_accuracy_profile.hpp"

namespace datasketches {

void cpc_union_accuracy_profile::run_trial() {
  const size_t lg_k = 12;
  const size_t num_sketches = 32;

  std::unique_ptr<cpc_sketch> sketches[num_sketches];
  for (size_t i = 0; i < num_sketches; i++) {
    sketches[i] = std::unique_ptr<cpc_sketch>(new cpc_sketch(lg_k));
  }
  cpc_union u(lg_k);

  size_t count = 0;
  size_t i = 0;
  for (auto& stat: stats) {
    const size_t delta = stat.get_true_value() - count;
    for (size_t j = 0; j < delta; j++) {
      sketches[i++]->update(key++);
      if (i == num_sketches) i = 0;
    }
    count += delta;
    for (auto& sketch: sketches) {
      u.update(*sketch);
    }
    cpc_sketch result = u.get_result();
    stat.update(
      result.get_estimate(),
      result.get_lower_bound(1), result.get_lower_bound(2), result.get_lower_bound(3),
      result.get_upper_bound(1), result.get_upper_bound(2), result.get_upper_bound(3)
    );
  }
}

}
