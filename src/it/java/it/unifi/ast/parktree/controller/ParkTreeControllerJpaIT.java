package it.unifi.ast.parktree.controller;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MySQLContainer;

import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.ParkTreeAssociationId;
import it.unifi.ast.parktree.model.Tree;
import it.unifi.ast.parktree.repository.ParkRepository;
import it.unifi.ast.parktree.repository.ParkTreeAssociationRepository;
import it.unifi.ast.parktree.repository.TreeRepository;
import it.unifi.ast.parktree.repository.jpa.ParkJPARepository;
import it.unifi.ast.parktree.repository.jpa.ParkTreeAssociationJPARepository;
import it.unifi.ast.parktree.repository.jpa.TreeJPARepository;
import it.unifi.ast.parktree.transaction.DefaultParkTreeRepositories;
import it.unifi.ast.parktree.transaction.JpaTransactionManager;
import it.unifi.ast.parktree.view.ParkTreeView;

/**
 * Mirrors {@link ParkTreeControllerMongoIT} scenario by scenario. Unlike the
 * unit tests, here the Controller is wired to a real JpaTransactionManager
 * (see {@code ParkTreeJpaModule}), backed by a real database, so it manages
 * its own transactions exactly as it does in production/E2E.
 */
public class ParkTreeControllerJpaIT {

	@ClassRule
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0");

	private static EntityManagerFactory entityManagerFactory;

	@Mock
	private ParkTreeView parkTreeView;

	private EntityManager entityManager;
	private ParkRepository parkRepository;
	private TreeRepository treeRepository;
	private ParkTreeAssociationRepository associationRepository;
	private ParkTreeController parkTreeController;

	private AutoCloseable closeable;

	@BeforeClass
	public static void setupFactory() {
		Map<String, String> overrides = new HashMap<>();
		overrides.put("javax.persistence.jdbc.url", mysqlContainer.getJdbcUrl());
		overrides.put("javax.persistence.jdbc.user", mysqlContainer.getUsername());
		overrides.put("javax.persistence.jdbc.password", mysqlContainer.getPassword());
		entityManagerFactory = Persistence.createEntityManagerFactory("mysql", overrides);
	}

	@AfterClass
	public static void shutdownFactory() {
		entityManagerFactory.close();
	}

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		entityManager = entityManagerFactory.createEntityManager();

		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM ParkTreeAssociation").executeUpdate();
		entityManager.createQuery("DELETE FROM Park").executeUpdate();
		entityManager.createQuery("DELETE FROM Tree").executeUpdate();
		entityManager.getTransaction().commit();

		parkRepository = new ParkJPARepository(entityManager);
		treeRepository = new TreeJPARepository(entityManager);
		associationRepository = new ParkTreeAssociationJPARepository(entityManager);
		JpaTransactionManager transactionManager = new JpaTransactionManager(() -> entityManager,
				new DefaultParkTreeRepositories(parkRepository, treeRepository, associationRepository));
		parkTreeController = new ParkTreeController(parkTreeView, transactionManager);
	}

	@After
	public void tearDown() throws Exception {
		entityManager.close();
		closeable.close();
	}

	@Test
	public void testAddTreeThenAddParkWithAssociationShouldPersistAcrossAllThreeRepositories() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		parkTreeController.addTree(tree);

		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		List<ParkTreeAssociation> associations = asList(
				new ParkTreeAssociation(new ParkTreeAssociationId("1", "1"), 100));
		parkTreeController.addPark(park, associations);

		verify(parkTreeView).showParkInfo(park, associations);
	}

	@Test
	public void testDeleteParkShouldCascadeDeleteItsAssociations() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		parkTreeController.addTree(tree);
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		List<ParkTreeAssociation> associations = asList(
				new ParkTreeAssociation(new ParkTreeAssociationId("1", "1"), 100));
		parkTreeController.addPark(park, associations);

		parkTreeController.deletePark(park);

		assertThat(associationRepository.findByParkId("1")).isEmpty();
	}

	@Test
	public void testAddParkWhenAlreadyExistsShouldShowErrorAndNotDuplicate() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		parkTreeController.addTree(tree);
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		List<ParkTreeAssociation> associations = asList(
				new ParkTreeAssociation(new ParkTreeAssociationId("1", "1"), 100));
		parkTreeController.addPark(park, associations);
		Park duplicate = new Park("1", "Altro nome", "Altra regione", 10, false);

		parkTreeController.addPark(duplicate, Collections.emptyList());

		verify(parkTreeView).showError("Already existing park with id 1");
		assertThat(parkRepository.findAll()).containsExactly(park);
	}

	@Test
	public void testDeleteTreeAssociatedWithParkShouldBeRejected() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		parkTreeController.addTree(tree);
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		List<ParkTreeAssociation> associations = asList(
				new ParkTreeAssociation(new ParkTreeAssociationId("1", "1"), 100));
		parkTreeController.addPark(park, associations);

		parkTreeController.deleteTree(tree);

		verify(parkTreeView).showError("Cannot delete tree with id 1: associated with one or more parks");
		assertThat(treeRepository.findById("1")).isEqualTo(tree);
	}

}
