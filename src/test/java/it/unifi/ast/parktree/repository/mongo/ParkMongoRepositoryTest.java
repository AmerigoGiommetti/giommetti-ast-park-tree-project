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
import it.unifi.ast.parktree.model.Park;

public class ParkMongoRepositoryTest {
	private static MongoServer server;
	private static MongoClient client;
	
	private static final String DB_NAME = "parktree";
	private static final String COLLECTION_NAME = "park";
	
	private MongoCollection<Document> parkCollection;
	private ParkMongoRepository parkMongoRepository;	//SUT
	
	//Start the in-memory mongoDB only one time for all the tests
	@BeforeClass
	public static void setupServer() {
		server = new MongoServer(new MemoryBackend());
		InetSocketAddress serverAddress = server.bind();
		client = new MongoClient(new ServerAddress(serverAddress));
	}
	
	//Shuts down the server once all tests are completed
	@AfterClass
	public static void shutdownServer() {
		client.close();
		server.shutdown();
	}

	//Drop the database before each test is run to ensure a clean consistent state
	@Before
	public void setUp() {
		MongoDatabase database = client.getDatabase(DB_NAME);
		database.drop();
		
		parkCollection = database.getCollection(COLLECTION_NAME);
		
		parkMongoRepository = new ParkMongoRepository(client, DB_NAME, COLLECTION_NAME);
	}
	
	@Test
	public void testFindAllWhenDatabaseIsEmptyShouldReturnEmptyList() {
		assertThatIterable(parkMongoRepository.findAll()).isEmpty();
	}
	
	@Test
	public void testFindAllWhenThereIsASingleParkInTheDatabase() {
		//setup
		addTestPark("0", "Maremma", "Toscana", 50, true);
		
		//verify
		assertThat(parkMongoRepository.findAll()).containsExactlyInAnyOrder(new Park("0", "Maremma", "Toscana", 50, true));
	}
	
	@Test
	public void testFindAllWhenTheDatabaseIsNotEmpty() {
		//setup
		addTestPark("0", "Maremma", "Toscana", 50, true);
		addTestPark("1", "Casentino", "Toscana", 50.5, false);
		
		//verify
		assertThat(parkMongoRepository.findAll())
				.containsExactlyInAnyOrder(new Park("0", "Maremma", "Toscana", 50, true), new Park("1", "Casentino", "Toscana", 50.5, false));
	}
	
	@Test
	public void testFindByIdNotFound() {
		assertThat(parkMongoRepository.findById("1")).isNull();
	}
	
	@Test
	public void testFindByIdFound() {
		//setup
		addTestPark("0", "Maremma", "Toscana", 50, true);
		addTestPark("1", "Casentino", "Toscana", 50.5, false);

		//verify
		assertThat(parkMongoRepository.findById("1"))
			.isEqualTo(new Park("1", "Casentino", "Toscana", 50.5, false));
	}
	
	@Test
	public void testSave() {
		//setup
		Park park = new Park("0", "Maremma", "Toscana", 50, true);

		//exercise
		parkMongoRepository.save(park);

		//verify
		assertThat(readAllParksFromDatabase())
			.containsExactly(new Park("0", "Maremma", "Toscana", 50, true));
	}
	
	@Test
	public void testDeleteForOneRecordInDatabase() {
		//setup
		addTestPark("0", "Maremma", "Toscana", 50, true);

		//exercise
		parkMongoRepository.delete("0");

		//verify
		assertThat(readAllParksFromDatabase()).isEmpty();
	}
	
	@Test
	public void testDeleteForMoreThanOneRecordInDatabase() {
		//setup
		addTestPark("0", "Maremma", "Toscana", 50, true);
		addTestPark("1", "Casentino", "Toscana", 50.5, false);

		//exercise
		parkMongoRepository.delete("0");

		//verify
		assertThat(readAllParksFromDatabase()).containsExactly(new Park("1", "Casentino", "Toscana", 50.5, false));
	}
	
	private void addTestPark(String id, String name, String region, double area, boolean freeAccess) {
		parkCollection.insertOne(new Document()
				.append("id", id)
				.append("name", name)
				.append("region", region)
				.append("area", area)
				.append("freeAccess", freeAccess));
	}
	
	private List<Park> readAllParksFromDatabase() {
		return StreamSupport
			.stream(parkCollection.find().spliterator(), false)
			.map(d -> new Park(
					d.getString("id"),
					d.getString("name"),
					d.getString("region"),
					d.getDouble("area"),
					d.getBoolean("freeAccess")))
			.collect(Collectors.toList());
	}
}
