package it.unifi.ast.parktree.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NoOpTransactionManagerTest {

	@Mock
	private ParkTreeRepositories repositories;

	private NoOpTransactionManager transactionManager; // SUT

	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		transactionManager = new NoOpTransactionManager(repositories);
	}

	@After
	public void tearDown() throws Exception {
		closeable.close();
	}

	@Test
	public void testDoInTransactionShouldRunCodeWithTheRepositoriesAndReturnItsResult() {
		Object result = transactionManager.doInTransaction(repos -> {
			assertThat(repos).isSameAs(repositories);
			return "result";
		});

		assertThat(result).isEqualTo("result");
		verifyNoInteractions(repositories);
	}

	@Test
	public void testDoInTransactionShouldPropagateExceptionsFromCodeWithoutWrapping() {
		RuntimeException failure = new RuntimeException("boom");

		assertThatThrownBy(() -> transactionManager.doInTransaction(repos -> {
			throw failure;
		})).isSameAs(failure);
	}

}
