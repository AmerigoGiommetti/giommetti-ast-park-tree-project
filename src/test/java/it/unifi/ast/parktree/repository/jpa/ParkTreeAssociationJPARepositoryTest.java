package it.unifi.ast.parktree.repository.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.ParkTreeAssociationId;

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
		entityManager.getTransaction().commit();
		// Injecting the entity manager dependency for the tests
		parkTreeAssociationJPARepository = new ParkTreeAssociationJPARepository(entityManager);
	}

	// Closing the entity manager after each test
	@After
	public void tearDown() {
		entityManager.close();
	}

	@Test
	public void testSave() {
		// setup
		ParkTreeAssociationId id = new ParkTreeAssociationId("1", "1");
		ParkTreeAssociation association = new ParkTreeAssociation(id, 45);

		// exercise
		entityManager.getTransaction().begin();
		parkTreeAssociationJPARepository.save(association);
		entityManager.getTransaction().commit();

		// verify
		ParkTreeAssociation saved = entityManager.find(ParkTreeAssociation.class, association.getId());
		assertThat(saved).isEqualTo(association);
	}

	@Test
	public void testFindByParkIdWhenDatabaseIsEmptyShouldReturnEmptyList() {
		assertThat(parkTreeAssociationJPARepository.findByParkId("1")).isEmpty();
	}

	@Test
	public void testFindByParkIdWhenNotEmpty() {
		// setup
		ParkTreeAssociationId id = new ParkTreeAssociationId("0", "0");
		ParkTreeAssociationId id1 = new ParkTreeAssociationId("1", "1");
		ParkTreeAssociationId id2 = new ParkTreeAssociationId("1", "2");
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(id1, 40);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(id2, 60);
		ParkTreeAssociation assocOther = new ParkTreeAssociation(id, 30);

		entityManager.getTransaction().begin();
		entityManager.persist(assoc1);
		entityManager.persist(assoc2);
		entityManager.persist(assocOther);
		entityManager.getTransaction().commit();

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
		ParkTreeAssociationId id = new ParkTreeAssociationId("0", "0");
		ParkTreeAssociationId id1 = new ParkTreeAssociationId("1", "1");
		ParkTreeAssociationId id2 = new ParkTreeAssociationId("2", "1");
		ParkTreeAssociation assocOther = new ParkTreeAssociation(id, 30);
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(id1, 40);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(id2, 60);

		entityManager.getTransaction().begin();
		entityManager.persist(assoc1);
		entityManager.persist(assoc2);
		entityManager.persist(assocOther);
		entityManager.getTransaction().commit();

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
		ParkTreeAssociationId id = new ParkTreeAssociationId("0", "0");
		ParkTreeAssociationId id1 = new ParkTreeAssociationId("1", "1");
		ParkTreeAssociationId id2 = new ParkTreeAssociationId("1", "2");
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(id1, 40);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(id2, 60);
		ParkTreeAssociation assocOther = new ParkTreeAssociation(id, 30);

		entityManager.getTransaction().begin();
		entityManager.persist(assoc1);
		entityManager.persist(assoc2);
		entityManager.persist(assocOther);
		entityManager.getTransaction().commit();

		// exercise
		entityManager.getTransaction().begin();
		parkTreeAssociationJPARepository.deleteByParkId("1");
		entityManager.getTransaction().commit();

		// verify
		assertThat(entityManager.createQuery("SELECT a FROM ParkTreeAssociation a", ParkTreeAssociation.class)
				.getResultList()).containsExactly(assocOther);
	}
}
