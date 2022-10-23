package com.iconloop.score.test;

import static java.math.BigInteger.ZERO;
import java.math.BigInteger;
import score.Address;

public class TestClient<T> {
  public Account caller;
  public final Score target;
  protected BigInteger icx;

  public TestClient (Account caller, Score target) {
    this.caller = caller;
    this.target = target;
    this.icx = ZERO;
  }

  public void invoke (String method, Object ...params) {
    this.target.invoke(this.caller, method, params);
  }

  public void invokePayable (String method, Object ...params) {
    this.target.invoke(this.caller, this.icx, method, params);
    this.icx = ZERO;
  }

  public Object call (String method, Object ...params) {
    return this.target.call(method, params);
  }

  public void setCaller (Account newCaller) {
    this.caller = newCaller;
  }

  @SuppressWarnings("unchecked")
  public T icx (BigInteger icx) {
    this.icx = icx;
    return (T) this;
  }
  
  public Address getAddress() {
    return this.target.getAddress();
  }

  public Account getAccount() {
    return this.target.getAccount();
  }
}