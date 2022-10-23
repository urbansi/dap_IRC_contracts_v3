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
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;
import com.iconloop.score.test.AssertUtils;

public class MRPRO_Token_V2_1Test extends TestBase {

  protected ServiceManager sm = TestBase.getServiceManager();
  protected final Account owner = sm.createAccount(1_000);
  protected final Account admin = sm.createAccount(1_000);
  protected MRPRO_Token_V2_1Client client;
  
  @BeforeEach
  public void setup() throws Exception {
    this.client = new MRPRO_Token_V2_1Client(owner, sm.deploy(owner, MRPRO_Token_V2_1.class, 18));
  }

  @Test
  void transfer () {
    // Setup
    final Account alice = sm.createAccount(1000);
    final BigInteger amount = BigInteger.valueOf(1000);
    this.client.mint(owner.getAddress(), amount);

    // Test
    BigInteger before = client.balanceOf(owner.getAddress());
    client.transfer(alice.getAddress(), amount, "".getBytes());
    BigInteger after = client.balanceOf(owner.getAddress());
    assertEquals(amount, before.subtract(after));
    assertEquals(amount, client.balanceOf(alice.getAddress()));
  }

  @Test
  void transferFrom () {
    // Setup
    final Account alice = sm.createAccount(1000);
    final BigInteger amount = BigInteger.valueOf(1000);
    this.client.mint(owner.getAddress(), amount);

    // Test
    BigInteger before = client.balanceOf(owner.getAddress());
    this.client.transferFrom(owner.getAddress(), alice.getAddress(), amount, "".getBytes());
    BigInteger after = client.balanceOf(owner.getAddress());

    assertEquals(amount, before.subtract(after));
    assertEquals(amount, client.balanceOf(alice.getAddress()));
  }
  
  @Test
  void transferFromOnlyOwner () {
    // Setup
    final Account alice = sm.createAccount(1000);
    final BigInteger amount = BigInteger.valueOf(1000);
    this.client.mint(owner.getAddress(), amount);

    // Only owner
    final Account eve = sm.createAccount(1000);
    this.client.setCaller(eve);
    AssertUtils.assertThrowsMessage(AssertionError.class, 
      () -> this.client.transferFrom(owner.getAddress(), alice.getAddress(), amount, "".getBytes()), 
      "Reverted(0): MRPRO_Token_V2_1: Only owner function");
  }

  @Test
  void mint () {
    // Setup
    final Account alice = sm.createAccount(1000);
    final BigInteger amount = BigInteger.valueOf(1000);

    // Test
    BigInteger before = client.balanceOf(alice.getAddress());
    this.client.mint(alice.getAddress(), amount);
    BigInteger after = client.balanceOf(alice.getAddress());
    
    assertEquals(amount, after.subtract(before));
    assertEquals(amount, client.balanceOf(alice.getAddress()));
  }
  
  @Test
  void mintOnlyOwner () {
    // Setup
    final Account alice = sm.createAccount(1000);
    final Account eve = sm.createAccount(1000);
    final BigInteger amount = BigInteger.valueOf(1000);

    // Test
    this.client.setCaller(eve);

    AssertUtils.assertThrowsMessage(AssertionError.class, 
      () -> this.client.mint(alice.getAddress(), amount), 
      "Reverted(0): MRPRO_Token_V2_1: Only owner function");
  }

  @Test
  void burn () {
    // Setup
    final Account alice = sm.createAccount(1000);
    final BigInteger amount = BigInteger.valueOf(1000);
    this.client.mint(alice.getAddress(), amount);

    // Test
    BigInteger before = client.balanceOf(alice.getAddress());
    this.client.burn(alice.getAddress(), amount);
    BigInteger after = client.balanceOf(alice.getAddress());
    
    assertEquals(amount, before.subtract(after));
    assertEquals(ZERO, client.balanceOf(alice.getAddress()));
  }
  
  @Test
  void burnOnlyOwner () {
    // Setup
    final Account alice = sm.createAccount(1000);
    final Account eve = sm.createAccount(1000);
    final BigInteger amount = BigInteger.valueOf(1000);

    // Test
    this.client.setCaller(eve);

    AssertUtils.assertThrowsMessage(AssertionError.class, 
      () -> this.client.burn(alice.getAddress(), amount), 
      "Reverted(0): MRPRO_Token_V2_1: Only owner function");
  }

  @Test
  void fallback () {
    AssertUtils.assertThrowsMessage(AssertionError.class, 
      () -> this.client.fallback(), 
      "Reverted(0): MRPRO_Token_V2_1 does not accept ICX.");
  }
}