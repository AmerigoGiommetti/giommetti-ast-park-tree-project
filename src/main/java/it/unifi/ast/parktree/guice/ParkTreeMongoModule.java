package it.unifi.ast.parktree.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.mongodb.MongoClient;

import it.unifi.ast.parktree.controller.ParkTreeController;
import it.unifi.ast.parktree.repository.ParkRepository;
import it.unifi.ast.parktree.repository.ParkTreeAssociationRepository;
import it.unifi.ast.parktree.repository.TreeRepository;
import it.unifi.ast.parktree.repository.mongo.ParkMongoRepository;
import it.unifi.ast.parktree.repository.mongo.ParkTreeAssociationMongoRepository;
import it.unifi.ast.parktree.repository.mongo.TreeMongoRepository;
import it.unifi.ast.parktree.transaction.DefaultParkTreeRepositories;
import it.unifi.ast.parktree.transaction.NoOpTransactionManager;
import it.unifi.ast.parktree.transaction.ParkTreeRepositories;
import it.unifi.ast.parktree.transaction.TransactionManager;
import it.unifi.ast.parktree.view.swing.ParkTreeSwingView;

public class ParkTreeMongoModule extends AbstractModule {

	private static final String PARK_COLLECTION = "park";
	private static final String TREE_COLLECTION = "tree";
	private static final String ASSOCIATION_COLLECTION = "parkTreeAssociation";

	private String mongoHost = "localhost";
	private int mongoPort = 27017;
	private String databaseName = "parktree";

	public ParkTreeMongoModule mongoHost(String mongoHost) {
		this.mongoHost = mongoHost;
		return this;
	}

	public ParkTreeMongoModule mongoPort(int mongoPort) {
		this.mongoPort = mongoPort;
		return this;
	}

	public ParkTreeMongoModule databaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(MongoHost.class).toInstance(mongoHost);
		bind(Integer.class).annotatedWith(MongoPort.class).toInstance(mongoPort);
		bind(String.class).annotatedWith(MongoDbName.class).toInstance(databaseName);
		bind(String.class).annotatedWith(ParkCollectionName.class).toInstance(PARK_COLLECTION);
		bind(String.class).annotatedWith(TreeCollectionName.class).toInstance(TREE_COLLECTION);
		bind(String.class).annotatedWith(AssociationCollectionName.class).toInstance(ASSOCIATION_COLLECTION);

		bind(ParkRepository.class).to(ParkMongoRepository.class);
		bind(TreeRepository.class).to(TreeMongoRepository.class);
		bind(ParkTreeAssociationRepository.class).to(ParkTreeAssociationMongoRepository.class);

		bind(ParkTreeRepositories.class).to(DefaultParkTreeRepositories.class);
		// MongoDB transactions are out of scope (per the book): the controller
		// still only accesses repositories through the TransactionManager, but
		// this implementation does not wrap them in an actual transaction
		bind(TransactionManager.class).to(NoOpTransactionManager.class);

		install(new FactoryModuleBuilder().implement(ParkTreeController.class, ParkTreeController.class)
				.build(ParkTreeControllerFactory.class));
	}

	@Provides
	@Singleton
	MongoClient mongoClient(@MongoHost String host, @MongoPort int port) {
		return new MongoClient(host, port);
	}

	@Provides
	ParkTreeSwingView view(ParkTreeControllerFactory controllerFactory) {
		ParkTreeSwingView view = new ParkTreeSwingView();
		view.setParkTreeController(controllerFactory.create(view));
		return view;
	}

}
