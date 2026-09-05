package it.unifi.ast.parktree.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JpaTransactionManagerTest {

	@Mock
	private EntityManager entityManager;

	@Mock
	private EntityTransaction entityTransaction;

	@Mock
	private ParkTreeRepositories repositories;

	private JpaTransactionManager transactionManager; // SUT

	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		when(entityManager.getTransaction()).thenReturn(entityTransaction);
		transactionManager = new JpaTransactionManager(() -> entityManager, repositories);
	}

	@After
	public void tearDown() throws Exception {
		closeable.close();
	}

	@Test
	public void testDoInTransactionShouldBeginExecuteAndCommitWhenNotAlreadyActive() {
		when(entityTransaction.isActive()).thenReturn(false);

		Object result = transactionManager.doInTransaction(repos -> "result");

		assertThat(result).isEqualTo("result");
		verify(entityTransaction).begin();
		verify(entityTransaction).commit();
		verify(entityTransaction, never()).rollback();
	}

	@Test
	public void testDoInTransactionShouldJoinAlreadyActiveTransactionWithoutBeginningOrCommittingAgain() {
		when(entityTransaction.isActive()).thenReturn(true);

		transactionManager.doInTransaction(repos -> "result");

		verify(entityTransaction, never()).begin();
		verify(entityTransaction, never()).commit();
	}

	@Test
	public void testDoInTransactionShouldRollbackAndRethrowWhenCodeThrowsAndOwnsTheTransaction() {
		// not active yet when checked to decide ownership, still active when
		// checked again in the catch block (not committed, not yet rolled back)
		when(entityTransaction.isActive()).thenReturn(false, true);
		RuntimeException failure = new RuntimeException("boom");

		assertThatThrownBy(() -> transactionManager.doInTransaction(repos -> {
			throw failure;
		})).isSameAs(failure);

		verify(entityTransaction).begin();
		verify(entityTransaction).rollback();
		verify(entityTransaction, never()).commit();
	}

	@Test
	public void testDoInTransactionShouldNotRollbackWhenJoiningAnAlreadyActiveTransactionAndCodeThrows() {
		when(entityTransaction.isActive()).thenReturn(true);
		RuntimeException failure = new RuntimeException("boom");

		assertThatThrownBy(() -> transactionManager.doInTransaction(repos -> {
			throw failure;
		})).isSameAs(failure);

		verify(entityTransaction, never()).begin();
		verify(entityTransaction, never()).rollback();
	}

	@Test
	public void testDoInTransactionShouldNotRollbackWhenOwnedTransactionIsAlreadyInactiveOnFailure() {
		// not active yet when checked to decide ownership (so this call owns
		// it), but no longer active by the time the catch block checks again
		when(entityTransaction.isActive()).thenReturn(false, false);
		RuntimeException failure = new RuntimeException("boom");

		assertThatThrownBy(() -> transactionManager.doInTransaction(repos -> {
			throw failure;
		})).isSameAs(failure);

		verify(entityTransaction).begin();
		verify(entityTransaction, never()).rollback();
	}

}
