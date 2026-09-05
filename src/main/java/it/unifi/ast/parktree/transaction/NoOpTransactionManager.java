package it.unifi.ast.parktree.transaction;

import com.google.inject.Inject;

public class NoOpTransactionManager implements TransactionManager {

	private final ParkTreeRepositories repositories;

	@Inject
	public NoOpTransactionManager(ParkTreeRepositories repositories) {
		this.repositories = repositories;
	}

	@Override
	public <T> T doInTransaction(TransactionCode<T> code) {
		return code.apply(repositories);
	}

}
