package it.unifi.ast.parktree.repository.mongo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import it.unifi.ast.parktree.guice.MongoDbName;
import it.unifi.ast.parktree.guice.TreeCollectionName;
import it.unifi.ast.parktree.model.Tree;
import it.unifi.ast.parktree.repository.TreeRepository;

import org.bson.Document;

import com.google.inject.Inject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class TreeMongoRepository implements TreeRepository {

	private MongoCollection<Document> treeCollection;

	@Inject
	public TreeMongoRepository(MongoClient client, @MongoDbName String dbName,
			@TreeCollectionName String collectionName) {
		MongoDatabase database = client.getDatabase(dbName);
		this.treeCollection = database.getCollection(collectionName);
	}

	public List<Tree> findAll() {
		return StreamSupport.stream(treeCollection.find().spliterator(), false).map(this::fromDocumentToTree)
				.collect(Collectors.toList());
	}

	@Override
	public Tree findById(String id) {
		// Find the document with the actual tree with that id
		Document d = treeCollection.find(Filters.eq("id", id)).first();

		// If it exists returns it but shaped into a java object
		if (d != null) {
			return fromDocumentToTree(d);
		}
		// otherwise return null
		return null;
	}

	@Override
	public void save(Tree tree) {
		treeCollection.insertOne(new Document().append("id", tree.getId()).append("name", tree.getName())
				.append("evergreen", tree.isEvergreen()).append("mediumLifespan", tree.getMediumLifespan()));
	}

	@Override
	public void delete(String id) {
		// removing firrst document that corresponds to the id value
		treeCollection.deleteOne(Filters.eq("id", id));
	}

	private Tree fromDocumentToTree(Document d) {
		return new Tree("" + d.get("id"), "" + d.get("name"), d.getBoolean("evergreen"),
				d.getInteger("mediumLifespan"));
	}

}
