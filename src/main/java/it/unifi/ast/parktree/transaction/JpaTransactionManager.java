package it.unifi.ast.parktree.transaction;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class JpaTransactionManager implements TransactionManager {

	private final Provider<EntityManager> entityManagerProvider;
	private final ParkTreeRepositories repositories;

	@Inject
	public JpaTransactionManager(Provider<EntityManager> entityManagerProvider, ParkTreeRepositories repositories) {
		this.entityManagerProvider = entityManagerProvider;
		this.repositories = repositories;
	}

	@Override
	public <T> T doInTransaction(TransactionCode<T> code) {
		EntityTransaction transaction = entityManagerProvider.get().getTransaction();
		// join an already-active transaction instead of nesting (e.g. addPark()
		// calling parkInfo() internally): only the outermost call owns it
		boolean isTransactionOwner = !transaction.isActive();
		if (isTransactionOwner) {
			transaction.begin();
		}
		try {
			T result = code.apply(repositories);
			if (isTransactionOwner) {
				transaction.commit();
			}
			return result;
		} catch (RuntimeException e) {
			if (isTransactionOwner && transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		}
	}

}
