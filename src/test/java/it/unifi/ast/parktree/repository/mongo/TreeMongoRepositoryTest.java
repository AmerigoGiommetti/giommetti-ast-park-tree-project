package it.unifi.ast.parktree.repository.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIterable;

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
import it.unifi.ast.parktree.model.Tree;

public class TreeMongoRepositoryTest {
	private static MongoServer server;
	private static MongoClient client;

	private static final String DB_NAME = "parktree";
	private static final String COLLECTION_NAME = "tree";

	private MongoCollection<Document> treeCollection;
	private TreeMongoRepository treeMongoRepository; // SUT

	// Start the in-memory mongoDB only one time for all the tests
	@BeforeClass
	public static void setupServer() {
		server = new MongoServer(new MemoryBackend());
		InetSocketAddress serverAddress = server.bind();
		client = new MongoClient(new ServerAddress(serverAddress));
	}

	// Shuts down the server once all tests are completed
	@AfterClass
	public static void shutdownServer() {
		client.close();
		server.shutdown();
	}

	// Drop the database before each test is run to ensure a clean consistent state
	@Before
	public void setUp() {
		MongoDatabase database = client.getDatabase(DB_NAME);
		database.drop();

		treeCollection = database.getCollection(COLLECTION_NAME);

		treeMongoRepository = new TreeMongoRepository(client, DB_NAME, COLLECTION_NAME);
	}

	@Test
	public void testFindAllWhenDatabaseIsEmptyShouldReturnEmptyList() {
		assertThatIterable(treeMongoRepository.findAll()).isEmpty();
	}

	@Test
	public void testFindAllWhenThereIsASingleTreeInTheDatabase() {
		// setup
		addTestTree("0", "pine", true, 100);

		// verify
		assertThat(treeMongoRepository.findAll()).containsExactlyInAnyOrder(new Tree("0", "pine", true, 100));
	}

	@Test
	public void testFindAllWhenTheDatabaseIsNotEmpty() {
		// setup
		addTestTree("0", "pine", true, 100);
		addTestTree("1", "maple", false, 200);

		// verify
		assertThat(treeMongoRepository.findAll()).containsExactlyInAnyOrder(new Tree("0", "pine", true, 100),
				new Tree("1", "maple", false, 200));
	}

	@Test
	public void testFindByIdNotFound() {
		assertThat(treeMongoRepository.findById("1")).isNull();
	}

	@Test
	public void testFindByIdFound() {
		// setup
		addTestTree("1", "pine", true, 100);
		addTestTree("2", "maple", false, 200);

		// verify
		assertThat(treeMongoRepository.findById("2")).isEqualTo(new Tree("2", "maple", false, 200));
	}

	@Test
	public void testSave() {
		// setup
		Tree tree = new Tree("1", "pine", true, 100);

		// exercise
		treeMongoRepository.save(tree);

		// verify
		assertThat(readAllTreesFromDatabase()).containsExactly(new Tree("1", "pine", true, 100));
	}

	@Test
	public void testDeleteForOneRecordInDatabase() {
		// setup
		addTestTree("1", "pine", true, 100);

		// exercise
		treeMongoRepository.delete("1");

		// verify
		assertThat(readAllTreesFromDatabase()).isEmpty();
	}

	@Test
	public void testDeleteForMoreThanOneRecordInDatabase() {
		// setup
		addTestTree("1", "pine", true, 100);
		addTestTree("2", "maple", false, 200);

		// exercise
		treeMongoRepository.delete("1");

		// verify
		assertThat(readAllTreesFromDatabase()).containsExactly(new Tree("2", "maple", false, 200));
	}

	private void addTestTree(String id, String name, boolean evergreen, int mediumLifespan) {
		treeCollection.insertOne(new Document().append("id", id).append("name", name).append("evergreen", evergreen)
				.append("mediumLifespan", mediumLifespan));
	}

	private List<Tree> readAllTreesFromDatabase() {
		return StreamSupport
				.stream(treeCollection.find().spliterator(), false).map(d -> new Tree("" + d.get("id"),
						"" + d.get("name"), d.getBoolean("evergreen"), d.getInteger("mediumLifespan")))
				.collect(Collectors.toList());
	}

}
