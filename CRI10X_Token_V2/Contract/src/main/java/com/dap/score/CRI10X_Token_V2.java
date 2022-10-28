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

import static java.math.BigInteger.ZERO;
import static score.Context.revert;
import java.math.BigInteger;
import com.dap.score.interfaces.score.ITokenFallback;
import com.iconloop.score.token.irc2.IRC2;
import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

public class CRI10X_Token_V2 implements IRC2, ICRI10X_Token_V2 {

  // ================================================
  // Consts
  // ================================================
  public final String TAG = "CRI10X_Token_V2";

  // ================================================
  // Eventlog
  // ================================================
  @EventLog(indexed=3)
  public void Transfer(Address _from,  Address _to, BigInteger _value, @Optional byte[] _data) {}

  @EventLog(indexed=2)
  public void Mint(Address _account, BigInteger _amount) {}

  @EventLog(indexed=2)
  public void Burn(Address _account, BigInteger _amount) {}

  // ================================================
  // DB Variables
  // ================================================
  final VarDB<BigInteger> _total_supply = Context.newVarDB("total_supply", BigInteger.class);
  final VarDB<BigInteger> _decimals = Context.newVarDB("decimals", BigInteger.class);
  final DictDB<Address, BigInteger> _balances = Context.newDictDB("balances", BigInteger.class);
  
  // ================================================
  // Methods
  // ================================================
  public CRI10X_Token_V2 (int _decimals) {
    if (_decimals < 0) {
      revert("Decimals cannot be less than zero");
    }

    if (this._decimals.get() == null) {
      this._decimals.set(BigInteger.valueOf(_decimals));
    }
  }

  @External(readonly = true)
  public String name() {
    return "CRI10X_Token_V2";
  }

  @External(readonly = true)
  public String symbol() {
    return "CRI10X";
  }

  @External(readonly = true)
  public BigInteger decimals() {
    return this._decimals.get();
  }

  @External(readonly = true)
  public BigInteger totalSupply() {
    return this._total_supply.getOrDefault(ZERO);
  }

  @External(readonly = true)
  public BigInteger balanceOf(Address _owner) {
    return this._balances.getOrDefault(_owner, ZERO);
  }

  @External
  public void transfer (Address _to, BigInteger _value, @Optional byte[] _data) {
    if (_data == null) {
      _data = "None".getBytes();
    }

    this._transfer(Context.getCaller(), _to, _value, _data);
  }

  @External
  public void transferFrom (Address _from, Address _to, BigInteger _value, @Optional byte[] _data) {
    if (!Context.getCaller().equals(Context.getOwner())) {
      revert("CRI10X_Token_V2: Only owner function");
    }
          
    if (_data == null) {
      _data = "None".getBytes();
    }
    
    this._transfer(_from, _to, _value, _data);
  }

  private void _transfer(Address _from, Address _to, BigInteger _value, byte[] _data) {
    
    // Checks the sending value and balance.
    if (_value.compareTo(ZERO) < 0) {
      revert("Transferring value cannot be less than zero");
    }

    if (this._balances.getOrDefault(_from, ZERO).compareTo(_value) < 0) {
      revert("Out of balance");
    }

    this._balances.set(_from, this._balances.getOrDefault(_from, ZERO).subtract(_value));
    this._balances.set(_to, this._balances.getOrDefault(_to, ZERO).add(_value));

    if (_to.isContract()) {
      // If the recipient is SCORE,q
      //   then calls `tokenFallback` to hand over control.
      ITokenFallback recipient_score = new ITokenFallback(_to);
      recipient_score.tokenFallback(_from, _value, _data);
    }

    // Emits an event log `Transfer`
    this.Transfer(_from, _to, _value, _data);
    Context.println(TAG + ": Transfer(" + _from + ", " + _to + ", " + _value + ", " + new String(_data) + ")");
  }

  @External
  public void mint(Address _account, BigInteger _amount) {
    if (!Context.getCaller().equals(Context.getOwner())) {
      revert("CRI10X_Token_V2: Only owner function");
    }

    this._balances.set(_account, this._balances.getOrDefault(_account, ZERO).add(_amount));
    this._total_supply.set(this._total_supply.getOrDefault(ZERO).add(_amount));

    this.Mint(_account, _amount);
  }

  @External
  public void burn(Address _account, BigInteger _amount) {
    if (!Context.getCaller().equals(Context.getOwner())) {
      revert("CRI10X_Token_V2: Only owner function");
    }
  
    if (this._balances.getOrDefault(_account, ZERO).compareTo(_amount) < 0) {
      revert("CRI10X_Token_V2: Account has not sufficient funds.");
    }

    this._balances.set(_account, this._balances.getOrDefault(_account, ZERO).subtract(_amount));
    this._total_supply.set(this._total_supply.getOrDefault(ZERO).subtract(_amount));

    this.Burn(_account, _amount);
  }

  @Payable
  public void fallback() {
    revert("CRI10X_Token_V2 does not accept ICX.");
  }
}
