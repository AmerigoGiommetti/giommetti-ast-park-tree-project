package it.unifi.ast.parktree.guice;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;

import it.unifi.ast.parktree.controller.ParkTreeController;
import it.unifi.ast.parktree.repository.ParkRepository;
import it.unifi.ast.parktree.repository.ParkTreeAssociationRepository;
import it.unifi.ast.parktree.repository.TreeRepository;
import it.unifi.ast.parktree.repository.jpa.ParkJPARepository;
import it.unifi.ast.parktree.repository.jpa.ParkTreeAssociationJPARepository;
import it.unifi.ast.parktree.repository.jpa.TreeJPARepository;
import it.unifi.ast.parktree.view.swing.ParkTreeSwingView;

public class ParkTreeJpaModule extends AbstractModule {

	private static final String PERSISTENCE_UNIT_NAME = "mysql";

	private String jpaHost = "localhost";
	private int jpaPort = 3306;
	private String databaseName = "parktree";
	private String user = "root";
	private String password = "";

	public ParkTreeJpaModule jpaHost(String jpaHost) {
		this.jpaHost = jpaHost;
		return this;
	}

	public ParkTreeJpaModule jpaPort(int jpaPort) {
		this.jpaPort = jpaPort;
		return this;
	}

	public ParkTreeJpaModule databaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}

	public ParkTreeJpaModule user(String user) {
		this.user = user;
		return this;
	}

	public ParkTreeJpaModule password(String password) {
		this.password = password;
		return this;
	}

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(JpaHost.class).toInstance(jpaHost);
		bind(Integer.class).annotatedWith(JpaPort.class).toInstance(jpaPort);
		bind(String.class).annotatedWith(JpaDbName.class).toInstance(databaseName);
		bind(String.class).annotatedWith(JpaUser.class).toInstance(user);
		bind(String.class).annotatedWith(JpaPassword.class).toInstance(password);

		bind(ParkRepository.class).to(ParkJPARepository.class);
		bind(TreeRepository.class).to(TreeJPARepository.class);
		bind(ParkTreeAssociationRepository.class).to(ParkTreeAssociationJPARepository.class);

		// the repositories persist/remove entities without managing a
		// transaction themselves ("the level above" handles it, per their own
		// comments); for the Guice-wired production/E2E path, this interceptor
		// is that level above: it wraps every ParkTreeController call in a
		// transaction (joining an already-active one for nested calls, e.g.
		// addPark() calling parkInfo() internally)
		bindInterceptor(Matchers.subclassesOf(ParkTreeController.class), Matchers.any(),
				new TransactionInterceptor(getProvider(EntityManager.class)));

		install(new FactoryModuleBuilder().implement(ParkTreeController.class, ParkTreeController.class)
				.build(ParkTreeControllerFactory.class));
	}

	@Provides
	@Singleton
	EntityManagerFactory entityManagerFactory(@JpaHost String host, @JpaPort int port, @JpaDbName String dbName,
			@JpaUser String user, @JpaPassword String password) {
		String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
				+ "?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC";
		Map<String, String> overrides = new HashMap<>();
		overrides.put("javax.persistence.jdbc.url", jdbcUrl);
		overrides.put("javax.persistence.jdbc.user", user);
		overrides.put("javax.persistence.jdbc.password", password);
		return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, overrides);
	}

	@Provides
	@Singleton
	EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
		return entityManagerFactory.createEntityManager();
	}

	@Provides
	ParkTreeSwingView view(ParkTreeControllerFactory controllerFactory) {
		ParkTreeSwingView view = new ParkTreeSwingView();
		view.setParkTreeController(controllerFactory.create(view));
		return view;
	}

}
