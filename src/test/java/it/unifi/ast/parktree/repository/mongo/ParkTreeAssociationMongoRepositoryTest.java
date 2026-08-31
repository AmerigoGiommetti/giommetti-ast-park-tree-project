package it.unifi.ast.parktree.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.ParkTreeAssociationId;

public class ParkTreeAssociationMongoRepositoryTest {

	private static MongoServer server;
	private static MongoClient mongoClient;
	private MongoDatabase database;
	private MongoCollection<Document> associationCollection;
	private ParkTreeAssociationMongoRepository parkTreeAssociationMongoRepository; // SUT

	private static final String DB_NAME = "parktree";
	private static final String COLLECTION_NAME = "associations";

	@BeforeClass
	public static void setupServer() {
		//starting the mongo client once and for all at the start of all the test suite
		server = new MongoServer(new MemoryBackend());
		InetSocketAddress serverAddress = server.bind();
		mongoClient = new MongoClient(new ServerAddress(serverAddress));
	}

	@AfterClass
	public static void shutdownServer() {
		//close the mongo client after all the tests are done
		mongoClient.close();
		server.shutdown();
	}

	@Before
	public void setUp() {
		//setting a standard isolated environment before each test
		database = mongoClient.getDatabase(DB_NAME);
		database.drop();
		associationCollection = database.getCollection(COLLECTION_NAME);
		parkTreeAssociationMongoRepository = new ParkTreeAssociationMongoRepository(mongoClient, DB_NAME, COLLECTION_NAME);
	}

	@Test
	public void testSave() {
		//setup
		ParkTreeAssociationId id = new ParkTreeAssociationId("1","1");
		ParkTreeAssociation association = new ParkTreeAssociation(id, 45);

		//exercise
		parkTreeAssociationMongoRepository.save(association);

		//verify
		assertThat(readAllAssociationsFromDatabase()).containsExactly(association);
	}

	@Test
	public void testDeleteByParkIdWhenDatabaseIsEmpty() {
		//exercise
		parkTreeAssociationMongoRepository.deleteByParkId("park1");

		//verify
		assertThat(readAllAssociationsFromDatabase()).isEmpty();
	}

	@Test
	public void testDeleteByParkIdWhenNotEmptyShouldDeleteOnlyTargetParkAssociations() {
		//setup
		ParkTreeAssociationId id1 = new ParkTreeAssociationId("1","1");
		ParkTreeAssociationId id2 = new ParkTreeAssociationId("1","2");
		ParkTreeAssociationId id = new ParkTreeAssociationId("2","1");
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(id1, 40);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(id2, 60);
		ParkTreeAssociation assocOther = new ParkTreeAssociation(id, 30);

		addTestAssociation(assoc1);
		addTestAssociation(assoc2);
		addTestAssociation(assocOther);

		//exercise
		parkTreeAssociationMongoRepository.deleteByParkId("1");

		//verify
		assertThat(readAllAssociationsFromDatabase()).containsExactly(assocOther);
	}

	@Test
	public void testFindByParkIdWhenDatabaseIsEmptyShouldReturnEmptyList() {
		assertThat(parkTreeAssociationMongoRepository.findByParkId("1")).isEmpty();
	}

	@Test
	public void testFindByParkIdWhenNotEmpty() {
		//setup
		ParkTreeAssociationId id1 = new ParkTreeAssociationId("1","1");
		ParkTreeAssociationId id2 = new ParkTreeAssociationId("1","2");
		ParkTreeAssociationId id = new ParkTreeAssociationId("2","1");
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(id1, 40);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(id2, 60);
		ParkTreeAssociation assocOther = new ParkTreeAssociation(id, 30);

		addTestAssociation(assoc1);
		addTestAssociation(assoc2);
		addTestAssociation(assocOther);

		//verify
		assertThat(parkTreeAssociationMongoRepository.findByParkId("1")).containsExactlyInAnyOrder(assoc1, assoc2);
	}

	@Test
	public void testFindByTreeIdWhenDatabaseIsEmptyShouldReturnEmptyList() {
		assertThat(parkTreeAssociationMongoRepository.findByTreeId("1")).isEmpty();
	}

	@Test
	public void testFindByTreeIdWhenNotEmpty() {
		//setup
		ParkTreeAssociationId id1 = new ParkTreeAssociationId("1","1");
		ParkTreeAssociationId id2 = new ParkTreeAssociationId("2","1");
		ParkTreeAssociationId id = new ParkTreeAssociationId("1","2");
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(id1, 40);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(id2, 60);
		ParkTreeAssociation assocOther = new ParkTreeAssociation(id, 30);

		addTestAssociation(assoc1);
		addTestAssociation(assoc2);
		addTestAssociation(assocOther);

		//verify
		assertThat(parkTreeAssociationMongoRepository.findByTreeId("1")).containsExactlyInAnyOrder(assoc1, assoc2);
	}

	// Helper that writes in the DB bypassing the SUT for tests in isolation
	private void addTestAssociation(ParkTreeAssociation assoc) {
		associationCollection.insertOne(
				new Document().append("parkId", assoc.getId().getParkId()).append("treeId", assoc.getId().getTreeId()).append("percentage", assoc.getPercentage()));
	}

	// Helper that reads the DB bypassing the SUT for tests in isolation
	private List<ParkTreeAssociation> readAllAssociationsFromDatabase() {
		return StreamSupport.stream(associationCollection.find().spliterator(), false).map(
				d -> new ParkTreeAssociation(new ParkTreeAssociationId(d.getString("parkId"), d.getString("treeId")), d.getInteger("percentage")))
				.collect(Collectors.toList());
	}
}