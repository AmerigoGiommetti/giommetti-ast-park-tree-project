package it.unifi.ast.parktree.repository.mongo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.google.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import it.unifi.ast.parktree.guice.MongoDbName;
import it.unifi.ast.parktree.guice.ParkCollectionName;
import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.repository.ParkRepository;

public class ParkMongoRepository implements ParkRepository {

	private MongoCollection<Document> parkCollection;

	@Inject
	public ParkMongoRepository(MongoClient client, @MongoDbName String dbName,
			@ParkCollectionName String collectionName) {
		MongoDatabase database = client.getDatabase(dbName);
		this.parkCollection = database.getCollection(collectionName);
	}

	@Override
	public List<Park> findAll() {
		return StreamSupport.stream(parkCollection.find().spliterator(), false).map(this::fromDocumentToPark)
				.collect(Collectors.toList());
	}

	@Override
	public Park findById(String id) {
		// Find the document with the actual park with that id
		Document d = parkCollection.find(Filters.eq("id", id)).first();

		// If it exists returns it but shaped into a java object
		if (d != null) {
			return fromDocumentToPark(d);
		}
		// otherwise return null
		return null;
	}

	@Override
	public void save(Park park) {
		parkCollection.insertOne(new Document().append("id", park.getId()).append("name", park.getName())
				.append("region", park.getRegion()).append("area", park.getArea())
				.append("freeAccess", park.isFreeAccess()));
	}

	@Override
	public void delete(String id) {
		// removing first document that corresponds to the id value
		parkCollection.deleteOne(Filters.eq("id", id));
	}

	private Park fromDocumentToPark(Document d) {
		return new Park(d.getString("id"), d.getString("name"), d.getString("region"), d.getDouble("area"),
				d.getBoolean("freeAccess"));
	}

}
