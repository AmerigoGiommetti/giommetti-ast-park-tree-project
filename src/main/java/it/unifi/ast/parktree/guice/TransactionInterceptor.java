package it.unifi.ast.parktree.guice;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class TransactionInterceptor implements MethodInterceptor {

	private final Provider<EntityManager> entityManagerProvider;

	@Inject
	public TransactionInterceptor(Provider<EntityManager> entityManagerProvider) {
		this.entityManagerProvider = entityManagerProvider;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		EntityTransaction transaction = entityManagerProvider.get().getTransaction();
		boolean isTransactionOwner = !transaction.isActive();
		if (isTransactionOwner) {
			transaction.begin();
		}
		try {
			Object result = invocation.proceed();
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
