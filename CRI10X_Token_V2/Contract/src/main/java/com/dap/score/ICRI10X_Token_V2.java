package com.dap.score;

import java.math.BigInteger;
import score.Address;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

public interface ICRI10X_Token_V2 {
  
  @EventLog(indexed=2)
  public void Mint(Address _account, BigInteger _amount);

  @EventLog(indexed=2)
  public void Burn(Address _account, BigInteger _amount);

  @External
  public void transferFrom (Address _from, Address _to, BigInteger _value, @Optional byte[] _data);

  @External
  public void mint(Address _account, BigInteger _amount);

  @External
  public void burn(Address _account, BigInteger _amount);

  @Payable
  public void fallback();
}
