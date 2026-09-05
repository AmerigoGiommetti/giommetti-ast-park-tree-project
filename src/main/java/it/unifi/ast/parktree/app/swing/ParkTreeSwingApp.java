package it.unifi.ast.parktree.app.swing;

import java.awt.EventQueue;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

import it.unifi.ast.parktree.guice.ParkTreeJpaModule;
import it.unifi.ast.parktree.guice.ParkTreeMongoModule;
import it.unifi.ast.parktree.view.swing.ParkTreeSwingView;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "parktree-app", mixinStandardHelpOptions = true)
public class ParkTreeSwingApp implements Callable<Void> {

	@Option(names = { "--mongo" }, description = "Use MongoDB instead of MySQL/JPA")
	private boolean useMongo;

	@Option(names = { "--mongo-host" }, description = "MongoDB host address")
	private String mongoHost = "localhost";

	@Option(names = { "--mongo-port" }, description = "MongoDB host port")
	private int mongoPort = 27017;

	@Option(names = { "--mongo-db-name" }, description = "MongoDB database name")
	private String mongoDbName = "parktree";

	@Option(names = { "--mysql-host" }, description = "MySQL host address")
	private String mysqlHost = "localhost";

	@Option(names = { "--mysql-port" }, description = "MySQL host port")
	private int mysqlPort = 3306;

	@Option(names = { "--mysql-db-name" }, description = "MySQL database name")
	private String mysqlDbName = "parktree";

	@Option(names = { "--mysql-user" }, description = "MySQL user")
	private String mysqlUser = "root";

	@Option(names = { "--mysql-password" }, description = "MySQL password")
	private String mysqlPassword = "";

	public static void main(String[] args) {
		new CommandLine(new ParkTreeSwingApp()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(() -> {
			try {
				Guice.createInjector(selectModule()).getInstance(ParkTreeSwingView.class).setVisible(true);
			} catch (Exception e) {
				Logger.getLogger(getClass().getName())
						.log(Level.SEVERE, "Exception", e);
			}
		});
		return null;
	}

	private AbstractModule selectModule() {
		if (useMongo) {
			return new ParkTreeMongoModule().mongoHost(mongoHost).mongoPort(mongoPort).databaseName(mongoDbName);
		}
		return new ParkTreeJpaModule().jpaHost(mysqlHost).jpaPort(mysqlPort).databaseName(mysqlDbName).user(mysqlUser)
				.password(mysqlPassword);
	}

}
