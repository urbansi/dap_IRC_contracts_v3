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

package com.dap.score;

import java.math.BigInteger;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.TestClient;
import com.iconloop.score.token.irc2.IRC2;
import score.Address;

class EURD_Token_V2Client extends TestClient<EURD_Token_V2Client> implements IRC2, IEURD_Token_V2 {
  public EURD_Token_V2Client (Account caller, Score target) {
    super(caller, target);
  }

  public String name() {
    return (String) this.call("name");
  }

  public String symbol() {
    return (String) this.call("symbol");
  }

  public BigInteger decimals() {
    return (BigInteger) this.call("decimals");
  }

  public BigInteger totalSupply() {
    return (BigInteger) this.call("totalSupply");
  }

  public BigInteger balanceOf(Address _owner) {
    return (BigInteger) this.call("balanceOf", _owner);
  }

  public void transfer(Address _to, BigInteger _value, byte[] _data) {
    this.invoke("transfer", _to, _value, _data);
  }

  public void Transfer(Address _from, Address _to, BigInteger _value, byte[] _data) {}
  public void Mint(Address _account, BigInteger _amount) {}
  public void Burn(Address _account, BigInteger _amount) {}

  public void transferFrom(Address _from, Address _to, BigInteger _value, byte[] _data) {
    this.invoke("transferFrom", _from, _to, _value, _data);
  }

  public void mint(Address _account, BigInteger _amount) {
    this.invoke("mint", _account, _amount);
  }

  public void burn(Address _account, BigInteger _amount) {
    this.invoke("burn", _account, _amount);
  }

  public void fallback() {
    this.invoke("fallback");
  }
}