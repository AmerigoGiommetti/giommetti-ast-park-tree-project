package it.unifi.ast.parktree.repository.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.Persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.ParkTreeAssociationId;
import it.unifi.ast.parktree.model.Tree;

public class ParkTreeAssociationJPARepositoryTest {

	private static EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private ParkTreeAssociationJPARepository parkTreeAssociationJPARepository; // SUT

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
		entityManager.createQuery("DELETE FROM ParkTreeAssociation").executeUpdate();
		entityManager.createQuery("DELETE FROM Park").executeUpdate();
		entityManager.createQuery("DELETE FROM Tree").executeUpdate();
		entityManager.getTransaction().commit();
		// Injecting the entity manager dependency for the tests
		parkTreeAssociationJPARepository = new ParkTreeAssociationJPARepository(entityManager);
	}

	// Closing the entity manager after each test
	@After
	public void tearDown() {
		entityManager.close();
	}

	// park_id/tree_id are now real foreign keys (see ParkTreeAssociation's
	// @MapsId mappings): every association fixture needs a matching Park/Tree
	// row already in the database, or the insert is rejected
	private void persistPark(String id) {
		entityManager.getTransaction().begin();
		entityManager.persist(new Park(id, "Park " + id, "Toscana", 50, true));
		entityManager.getTransaction().commit();
	}

	private void persistTree(String id) {
		entityManager.getTransaction().begin();
		entityManager.persist(new Tree(id, "Tree " + id, false, 50));
		entityManager.getTransaction().commit();
	}

	private void saveAssociation(ParkTreeAssociation association) {
		entityManager.getTransaction().begin();
		parkTreeAssociationJPARepository.save(association);
		entityManager.getTransaction().commit();
	}

	@Test
	public void testSave() {
		// setup
		persistPark("1");
		persistTree("1");
		ParkTreeAssociationId id = new ParkTreeAssociationId("1", "1");
		ParkTreeAssociation association = new ParkTreeAssociation(id, 45);

		// exercise
		saveAssociation(association);

		// verify
		ParkTreeAssociation saved = entityManager.find(ParkTreeAssociation.class, association.getId());
		assertThat(saved).isEqualTo(association);
	}

	@Test
	public void testSaveWithNonExistentParkShouldFailDueToForeignKeyConstraint() {
		persistTree("1");
		ParkTreeAssociation association = new ParkTreeAssociation(new ParkTreeAssociationId("missing-park", "1"), 50);

		assertThatThrownBy(() -> saveAssociation(association)).isInstanceOf(PersistenceException.class);
	}

	@Test
	public void testSaveWithNonExistentTreeShouldFailDueToForeignKeyConstraint() {
		persistPark("1");
		ParkTreeAssociation association = new ParkTreeAssociation(new ParkTreeAssociationId("1", "missing-tree"), 50);

		assertThatThrownBy(() -> saveAssociation(association)).isInstanceOf(PersistenceException.class);
	}

	@Test
	public void testFindByParkIdWhenDatabaseIsEmptyShouldReturnEmptyList() {
		assertThat(parkTreeAssociationJPARepository.findByParkId("1")).isEmpty();
	}

	@Test
	public void testFindByParkIdWhenNotEmpty() {
		// setup
		persistPark("0");
		persistPark("1");
		persistTree("0");
		persistTree("1");
		persistTree("2");
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(new ParkTreeAssociationId("1", "1"), 40);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(new ParkTreeAssociationId("1", "2"), 60);
		ParkTreeAssociation assocOther = new ParkTreeAssociation(new ParkTreeAssociationId("0", "0"), 30);
		saveAssociation(assoc1);
		saveAssociation(assoc2);
		saveAssociation(assocOther);

		// verify
		assertThat(parkTreeAssociationJPARepository.findByParkId("1")).containsExactlyInAnyOrder(assoc1, assoc2);
	}

	@Test
	public void testFindByTreeIdWhenDatabaseIsEmptyShouldReturnEmptyList() {
		assertThat(parkTreeAssociationJPARepository.findByTreeId("1")).isEmpty();
	}

	@Test
	public void testFindByTreeIdWhenNotEmpty() {
		// setup
		persistPark("0");
		persistPark("1");
		persistPark("2");
		persistTree("0");
		persistTree("1");
		ParkTreeAssociation assocOther = new ParkTreeAssociation(new ParkTreeAssociationId("0", "0"), 30);
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(new ParkTreeAssociationId("1", "1"), 40);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(new ParkTreeAssociationId("2", "1"), 60);
		saveAssociation(assoc1);
		saveAssociation(assoc2);
		saveAssociation(assocOther);

		// verify
		assertThat(parkTreeAssociationJPARepository.findByTreeId("1")).containsExactlyInAnyOrder(assoc1, assoc2);
	}

	@Test
	public void testDeleteByParkIdWhenDatabaseIsEmpty() {
		// exercise
		entityManager.getTransaction().begin();
		parkTreeAssociationJPARepository.deleteByParkId("1");
		entityManager.getTransaction().commit();

		// verify
		assertThat(entityManager.createQuery("SELECT a FROM ParkTreeAssociation a", ParkTreeAssociation.class)
				.getResultList()).isEmpty();
	}

	@Test
	public void testDeleteByParkIdWhenNotEmptyShouldDeleteOnlyTargetParkAssociations() {
		// setup
		persistPark("0");
		persistPark("1");
		persistTree("0");
		persistTree("1");
		persistTree("2");
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(new ParkTreeAssociationId("1", "1"), 40);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(new ParkTreeAssociationId("1", "2"), 60);
		ParkTreeAssociation assocOther = new ParkTreeAssociation(new ParkTreeAssociationId("0", "0"), 30);
		saveAssociation(assoc1);
		saveAssociation(assoc2);
		saveAssociation(assocOther);

		// exercise
		entityManager.getTransaction().begin();
		parkTreeAssociationJPARepository.deleteByParkId("1");
		entityManager.getTransaction().commit();

		// verify
		assertThat(entityManager.createQuery("SELECT a FROM ParkTreeAssociation a", ParkTreeAssociation.class)
				.getResultList()).containsExactly(assocOther);
	}
}
