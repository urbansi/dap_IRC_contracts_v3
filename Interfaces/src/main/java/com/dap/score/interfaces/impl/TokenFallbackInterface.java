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

package com.dap.score.interfaces.impl;

import java.math.BigInteger;
import score.Address;
import score.annotation.Optional;

/**
 * An interface of tokenFallback.
 * Receiving SCORE that has implemented this interface can handle
 * the receiving or further routine.
 */
public interface TokenFallbackInterface {
  public void tokenFallback(Address _from, BigInteger _value, @Optional byte[] _data);
}
