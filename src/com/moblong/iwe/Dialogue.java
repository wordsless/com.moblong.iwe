package com.moblong.iwe;

import java.util.List;

import com.moblong.flipped.model.Account;
import com.moblong.flipped.model.Whistle;

public final class Dialogue implements IReciveListener {

	private Account account;
	
	private List<Whistle<?>> history;

	public final Account getAccount() {
		return account;
	}

	public final void setAccount(Account account) {
		this.account = account;
	}

	public final List<Whistle<?>> getHistory() {
		return history;
	}

	public final void setHistory(List<Whistle<?>> history) {
		this.history = history;
	}

	@Override
	public boolean recived(Whistle<String> whistle) {
		history.add(whistle);
		return true;
	}
}
