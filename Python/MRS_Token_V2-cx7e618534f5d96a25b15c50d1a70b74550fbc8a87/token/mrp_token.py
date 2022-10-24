from iconservice import *
from .interfaces.irc_2_interface import *
from .interfaces.token_fallback_interface import *

TAG = 'MRS_Token_V2_V2'


class MrpToken(IconScoreBase, IRC2TokenStandard):

    @eventlog(indexed=3)
    def Transfer(self, _from: Address, _to: Address, _value: int, _data: bytes):
        pass

    @eventlog(indexed=2)
    def Mint(self, _account: Address, _amount: int):
        pass

    @eventlog(indexed=2)
    def Burn(self, _account: Address, _amount: int):
        pass

    def __init__(self, db: IconScoreDatabase) -> None:
        super().__init__(db)
        self._total_supply = VarDB('total_supply', db, value_type=int)
        self._decimals = VarDB('decimals', db, value_type=int)
        self._balances = DictDB('balances', db, value_type=int)

    def on_install(self, _decimals: int = 18) -> None:
        super().on_install()

        if _decimals < 0:
            revert("Decimals cannot be less than zero")

        self._decimals.set(_decimals)

    def on_update(self) -> None:
        super().on_update()

    @external(readonly=True)
    def name(self) -> str:
        return "MRS_Token_V2"

    @external(readonly=True)
    def symbol(self) -> str:
        return "MRS"

    @external(readonly=True)
    def decimals(self) -> int:
        return self._decimals.get()

    @external(readonly=True)
    def totalSupply(self) -> int:
        return self._total_supply.get()

    @external(readonly=True)
    def balanceOf(self, _owner: Address) -> int:
        return self._balances[_owner]

    @external
    def transfer(self, _to: Address, _value: int, _data: bytes = None):
        if _data is None:
            _data = b'None'
        self._transfer(self.msg.sender, _to, _value, _data)

    @external(readonly=False)
    def transferFrom(self, _from: Address, _to: Address, _value: int, _data: bytes = None):
        if self.msg.sender != self.owner:
            revert("MRS_Token_V2: Only owner function")
        if _data is None:
            _data = b'None'
        self._transfer(_from, _to, _value, _data)

    def _transfer(self, _from: Address, _to: Address, _value: int, _data: bytes):
        # Checks the sending value and balance.
        if _value < 0:
            revert("Transferring value cannot be less than zero")
        if self._balances[_from] < _value:
            revert("Out of balance")

        self._balances[_from] = self._balances[_from] - _value
        self._balances[_to] = self._balances[_to] + _value

        if _to.is_contract:
            # If the recipient is SCORE,q
            #   then calls `tokenFallback` to hand over control.
            recipient_score = self.create_interface_score(_to, TokenFallbackInterface)
            recipient_score.tokenFallback(_from, _value, _data)

        # Emits an event log `Transfer`
        self.Transfer(_from, _to, _value, _data)
        Logger.debug(f'Transfer({_from}, {_to}, {_value}, {_data})', TAG)

    @external(readonly=False)
    def mint(self, _account: Address, _amount: int):
        if self.msg.sender != self.owner:
            revert("MRS_Token_V2: Only owner function.")

        self._balances[_account] = self._balances[_account] + _amount
        self._total_supply.set(self._total_supply.get() + _amount)

        self.Mint(_account, _amount)

    @external(readonly=False)
    def burn(self, _account: Address, _amount: int):
        if self.msg.sender != self.owner:
            revert("MRS_Token_V2: Only owner function.")
        if self._balances[_account] < _amount:
            revert("MRS_Token_V2: Account has not sufficient funds.")

        self._balances[_account] = self._balances[_account] - _amount
        self._total_supply.set(self._total_supply.get() - _amount)

        self.Burn(_account, _amount)

    @payable
    def fallback(self):
        revert('MRS_Token_V2 does not accept ICX.')
