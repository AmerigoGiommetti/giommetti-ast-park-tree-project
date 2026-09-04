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

import it.unifi.ast.parktree.guice.AssociationCollectionName;
import it.unifi.ast.parktree.guice.MongoDbName;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.ParkTreeAssociationId;
import it.unifi.ast.parktree.repository.ParkTreeAssociationRepository;

public class ParkTreeAssociationMongoRepository implements ParkTreeAssociationRepository {

	private MongoCollection<Document> associationCollection;

	@Inject
	public ParkTreeAssociationMongoRepository(MongoClient client, @MongoDbName String dbName,
			@AssociationCollectionName String collectionName) {
		MongoDatabase database = client.getDatabase(dbName);
		this.associationCollection = database.getCollection(collectionName);
	}

	@Override
	public void save(ParkTreeAssociation association) {
		associationCollection.insertOne(new Document().append("parkId", association.getId().getParkId())
				.append("treeId", association.getId().getTreeId()).append("percentage", association.getPercentage()));
	}

	@Override
	public void deleteByParkId(String parkId) {
		// deleteMany because each park can have more than one association to delete
		associationCollection.deleteMany(Filters.eq("parkId", parkId));
	}

	@Override
	public List<ParkTreeAssociation> findByParkId(String parkId) {
		return StreamSupport.stream(associationCollection.find(Filters.eq("parkId", parkId)).spliterator(), false)
				.map(this::fromDocumentToAssociation).collect(Collectors.toList());
	}

	@Override
	public List<ParkTreeAssociation> findByTreeId(String treeId) {
		return StreamSupport.stream(associationCollection.find(Filters.eq("treeId", treeId)).spliterator(), false)
				.map(this::fromDocumentToAssociation).collect(Collectors.toList());
	}

	private ParkTreeAssociation fromDocumentToAssociation(Document d) {
		ParkTreeAssociationId id = new ParkTreeAssociationId(d.getString("parkId"), d.getString("treeId"));
		return new ParkTreeAssociation(id, d.getInteger("percentage"));
	}
}