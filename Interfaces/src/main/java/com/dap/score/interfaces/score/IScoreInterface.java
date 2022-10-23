/*
 * Copyright 2022 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dap.score.interfaces.score;

import static java.math.BigInteger.ZERO;
import java.math.BigInteger;
import score.Address;
import score.Context;

public class IScoreInterface<T> {
  public final Address target;
  public BigInteger icx;

  public IScoreInterface (Address target) {
    Context.require(target != null, 
      "IScoreInterface::IScoreInterface: Invalid target SCORE address");
    this.target = target;
    this.icx = ZERO;
  }

  public Object call(String method, Object... params) {
    Object result;
    
    if (this.icx.equals(ZERO)) {
      result = Context.call(this.target, method, params);
    } else {
      result = Context.call(this.icx, this.target, method, params);
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  public T icx (BigInteger amount) {
    this.icx = amount;
    return (T) this;
  }
}
