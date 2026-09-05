package it.unifi.ast.parktree.app.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JFrame;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MySQLContainer;

@RunWith(GUITestRunner.class)
public class ParkTreeSwingAppJpaE2E extends AssertJSwingJUnitTestCase {

	@ClassRule
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0");

	private FrameFixture window;

	@Override
	protected void onSetUp() {
		String host = mysqlContainer.getHost();
		int port = mysqlContainer.getMappedPort(3306);
		String dbName = mysqlContainer.getDatabaseName();
		String user = mysqlContainer.getUsername();
		String password = mysqlContainer.getPassword();

		cleanDatabase(host, port, dbName, user, password);

		application("it.unifi.ast.parktree.app.swing.ParkTreeSwingApp")
				.withArgs(
						"--mysql-host=" + host,
						"--mysql-port=" + port,
						"--mysql-db-name=" + dbName,
						"--mysql-user=" + user,
						"--mysql-password=" + password)
				.start();

		window = WindowFinder.findFrame(new GenericTypeMatcher<JFrame>(JFrame.class) {
			@Override
			protected boolean isMatching(JFrame frame) {
				return "Park Tree View".equals(frame.getTitle()) && frame.isShowing();
			}
		}).using(robot());
	}

	@Override
	protected void onTearDown() {
		// dispose the launched application's window so the next test's
		// WindowFinder cannot accidentally bind to a leftover one
		window.cleanUp();
	}

	private void cleanDatabase(String host, int port, String dbName, String user, String password) {
		Map<String, String> overrides = new HashMap<>();
		overrides.put("javax.persistence.jdbc.url", "jdbc:mysql://" + host + ":" + port + "/" + dbName
				+ "?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC");
		overrides.put("javax.persistence.jdbc.user", user);
		overrides.put("javax.persistence.jdbc.password", password);
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("mysql", overrides);
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM ParkTreeAssociation").executeUpdate();
		entityManager.createQuery("DELETE FROM Park").executeUpdate();
		entityManager.createQuery("DELETE FROM Tree").executeUpdate();
		entityManager.getTransaction().commit();
		entityManager.close();
		entityManagerFactory.close();
	}

	@Test
	@GUITest
	public void testAddTreeThenAddParkWithAssociationShouldPersistAcrossAllThreeRepositories() {
		window.textBox("treeIdTextBox").enterText("1");
		window.textBox("treeNameTextBox").enterText("Faggio");
		window.textBox("treeLifespanTextBox").enterText("50");
		window.button("addTreeButton").click();
		robot().waitForIdle();

		window.tabbedPane("tabbedPane").selectTab("Add Park");
		robot().waitForIdle();
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("50");
		window.checkBox("freelyAccessibleCheckBox").check();
		window.button("addAssociationButton").click();
		robot().waitForIdle();
		window.comboBox("treeComboBox_0").selectItem(0);
		window.textBox("percentageTextBox_0").enterText("100");
		window.button("addParkButton").click();
		robot().waitForIdle();

		window.tabbedPane("tabbedPane").selectTab("All Parks");
		robot().waitForIdle();

		assertThat(window.label("parkAssociationsLabel_1").text()).contains("Faggio").contains("100%");
	}

	@Test
	@GUITest
	public void testDeleteTreeAssociatedWithParkShouldBeRejected() {
		window.textBox("treeIdTextBox").enterText("1");
		window.textBox("treeNameTextBox").enterText("Faggio");
		window.textBox("treeLifespanTextBox").enterText("50");
		window.button("addTreeButton").click();
		robot().waitForIdle();

		window.tabbedPane("tabbedPane").selectTab("Add Park");
		robot().waitForIdle();
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("50");
		window.button("addAssociationButton").click();
		robot().waitForIdle();
		window.comboBox("treeComboBox_0").selectItem(0);
		window.textBox("percentageTextBox_0").enterText("100");
		window.button("addParkButton").click();
		robot().waitForIdle();

		window.tabbedPane("tabbedPane").selectTab("All Trees");
		robot().waitForIdle();
		window.button("deleteTreeButton_1").click();
		robot().waitForIdle();

		window.label("errorMessageLabel")
				.requireText("Cannot delete tree with id 1: associated with one or more parks");
		window.label("treeRowLabel_1");
	}

}
