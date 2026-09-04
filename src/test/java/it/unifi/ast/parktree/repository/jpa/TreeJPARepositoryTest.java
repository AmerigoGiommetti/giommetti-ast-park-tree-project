package it.unifi.ast.parktree.repository.jpa;

import static org.assertj.core.api.Assertions.assertThatIterable;
import static org.assertj.core.api.Assertions.assertThat;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.unifi.ast.parktree.model.Tree;

public class TreeJPARepositoryTest {

	private static EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private TreeJPARepository treeJPARepository; // SUT

	// The factory gets initialized once for everything
	@BeforeClass
	public static void setupFactory() {
		entityManagerFactory = Persistence.createEntityManagerFactory("h2-test");
	}

	// Closing the factory after all tests
	@AfterClass
	public static void shutdownFactory() {
		entityManagerFactory.close();
	}

	// Setting up consistent fixture before each test
	@Before
	public void setUp() {
		entityManager = entityManagerFactory.createEntityManager();

		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM Tree").executeUpdate();
		entityManager.getTransaction().commit();
		// Injecting the entity manager dependency for the tests
		treeJPARepository = new TreeJPARepository(entityManager);
	}

	// Closing the entity manager after each test
	@After
	public void tearDown() {
		entityManager.close();
	}

	@Test
	public void testFindAllWhenDatabaseIsEmptyShouldReturnEmptyList() {
		assertThatIterable(treeJPARepository.findAll()).isEmpty();
	}

	@Test
	public void testFindAllWhenDatabaseIsNotEmptyShouldReturnAllTrees() {
		Tree tree1 = new Tree("0", "pine", true, 100);
		Tree tree2 = new Tree("1", "maple", false, 200);

		// We populate the test database directly in a transaction
		entityManager.getTransaction().begin();
		entityManager.persist(tree1);
		entityManager.persist(tree2);
		entityManager.getTransaction().commit();

		// SUT execution and assertion
		assertThatIterable(treeJPARepository.findAll()).containsExactly(tree1, tree2);
	}

	@Test
	public void testFindByIdWhenDatabaseIsEmpty() {
		assertThat(treeJPARepository.findById("1")).isNull();
	}

	@Test
	public void testFindByIdWhenDatabaseNotEmpty() {
		// setup
		Tree tree1 = new Tree("0", "pine", true, 100);
		Tree tree2 = new Tree("1", "maple", false, 200);

		entityManager.getTransaction().begin();
		entityManager.persist(tree1);
		entityManager.persist(tree2);
		entityManager.getTransaction().commit();

		// verify
		assertThat(treeJPARepository.findById("1")).isEqualTo(tree2);
	}

	@Test
	public void testSave() {
		// setup
		Tree tree = new Tree("0", "pine", true, 100);

		// exercise
		entityManager.getTransaction().begin();
		treeJPARepository.save(tree);
		entityManager.getTransaction().commit();

		// verify
		Tree savedTree = entityManager.find(Tree.class, "0");

		assertThat(savedTree).isEqualTo(tree);
	}

	@Test
	public void testDeleteWhenOneRecordInTheDatabase() {
		// setup
		Tree tree = new Tree("0", "pine", true, 100);

		entityManager.getTransaction().begin();
		entityManager.persist(tree);
		entityManager.getTransaction().commit();

		// exercise
		treeJPARepository.delete("0");

		// verify
		assertThat(entityManager.find(Tree.class, "0")).isNull();
	}

	@Test
	public void testDeleteWhenMoreThanOneRecordInTheDatabase() {
		// setup
		Tree tree1 = new Tree("0", "pine", true, 100);
		Tree tree2 = new Tree("1", "maple", false, 200);

		entityManager.getTransaction().begin();
		entityManager.persist(tree1);
		entityManager.persist(tree2);
		entityManager.getTransaction().commit();

		// exercise
		treeJPARepository.delete("0");

		// verify
		assertThat(entityManager.find(Tree.class, "0")).isNull();
		assertThat(entityManager.find(Tree.class, "1")).isEqualTo(tree2);
	}
}
