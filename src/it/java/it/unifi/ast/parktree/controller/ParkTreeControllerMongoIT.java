package it.unifi.ast.parktree.controller;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MongoDBContainer;

import com.mongodb.MongoClient;

import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.ParkTreeAssociationId;
import it.unifi.ast.parktree.model.Tree;
import it.unifi.ast.parktree.repository.ParkRepository;
import it.unifi.ast.parktree.repository.ParkTreeAssociationRepository;
import it.unifi.ast.parktree.repository.TreeRepository;
import it.unifi.ast.parktree.repository.mongo.ParkMongoRepository;
import it.unifi.ast.parktree.repository.mongo.ParkTreeAssociationMongoRepository;
import it.unifi.ast.parktree.repository.mongo.TreeMongoRepository;
import it.unifi.ast.parktree.transaction.DefaultParkTreeRepositories;
import it.unifi.ast.parktree.transaction.NoOpTransactionManager;
import it.unifi.ast.parktree.view.ParkTreeView;

public class ParkTreeControllerMongoIT {

	@ClassRule
	public static final MongoDBContainer mongoContainer = new MongoDBContainer("mongo:5");

	private static final String DB_NAME = "parktree";

	@Mock
	private ParkTreeView parkTreeView;

	private MongoClient mongoClient;
	private ParkRepository parkRepository;
	private TreeRepository treeRepository;
	private ParkTreeAssociationRepository associationRepository;
	private ParkTreeController parkTreeController;

	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		mongoClient = new MongoClient(mongoContainer.getHost(), mongoContainer.getFirstMappedPort());
		mongoClient.getDatabase(DB_NAME).drop();
		parkRepository = new ParkMongoRepository(mongoClient, DB_NAME, "park");
		treeRepository = new TreeMongoRepository(mongoClient, DB_NAME, "tree");
		associationRepository = new ParkTreeAssociationMongoRepository(mongoClient, DB_NAME, "parkTreeAssociation");
		NoOpTransactionManager transactionManager = new NoOpTransactionManager(
				new DefaultParkTreeRepositories(parkRepository, treeRepository, associationRepository));
		parkTreeController = new ParkTreeController(parkTreeView, transactionManager);
	}

	@After
	public void tearDown() throws Exception {
		mongoClient.close();
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
