package it.unifi.ast.parktree.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.unifi.ast.parktree.model.Park;

public class ParkJPARepositoryTest {
	
	private static EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	private ParkJPARepository parkJPARepository; // SUT
	
	//The factory gets initialized once for everything
	@BeforeClass
	public static void setupFactory() {
		entityManagerFactory = Persistence.createEntityManagerFactory("h2-test");
	}
	
	//Closing the factory after all tests
	@AfterClass
	public static void shutdownFactory() {
		entityManagerFactory.close();
	}
	
	//Setting up consistent fixture before each test
	@Before
	public void setUp() {
		entityManager = entityManagerFactory.createEntityManager();
		
		entityManager.getTransaction().begin();
		entityManager.createQuery("DELETE FROM Park").executeUpdate();
		entityManager.getTransaction().commit();
		//Injecting the entity manager dependency for the tests
		parkJPARepository = new ParkJPARepository(entityManager);
	}
	
	//Closing the entity manager after each test
	@After
	public void tearDown() {
		entityManager.close();
	}
	
	@Test
	public void testFindAllWhenDatabaseIsEmptyShouldReturnEmptyList() {
		assertThat(parkJPARepository.findAll()).isEmpty();
	}
	
	@Test
	public void testFindAllWhenDatabaseIsNotEmptyShouldReturnAllParks() {
		Park park1 = new Park("0", "Maremma", "Toscana", 50, true);
		Park park2 = new Park("1", "Casentino", "Toscana", 50.5, false);

		// We populate the test database directly in a transaction
		entityManager.getTransaction().begin();
		entityManager.persist(park1);
		entityManager.persist(park2);
		entityManager.getTransaction().commit();

		// SUT execution and assertion
		assertThat(parkJPARepository.findAll())
			.containsExactly(park1, park2);
	}
	
	@Test
	public void testFindByIdWhenDatabaseIsEmpty() {
		assertThat(parkJPARepository.findById("1")).isNull();
	}
	
	@Test
	public void testFindByIdWhenDatabaseNotEmpty() {
		//setup
		Park park1 = new Park("0", "Maremma", "Toscana", 50, true);
		Park park2 = new Park("1", "Casentino", "Toscana", 50.5, false);

		entityManager.getTransaction().begin();
		entityManager.persist(park1);
		entityManager.persist(park2);
		entityManager.getTransaction().commit();

		//verify
		assertThat(parkJPARepository.findById("1")).isEqualTo(park2);
	}
	
	@Test
	public void testSave() {
		//setup
		Park park = new Park("0", "Maremma", "Toscana", 50, true);

		//exercise
		entityManager.getTransaction().begin();
		parkJPARepository.save(park);
		entityManager.getTransaction().commit();


		//verify
		Park savedPark = entityManager.find(Park.class, "0");
		
		assertThat(savedPark).isEqualTo(park);
	}
	
	@Test
	public void testDeleteWhenOneRecordInTheDatabase() {
		//setup
		Park park = new Park("0", "Maremma", "Toscana", 50, true);

		entityManager.getTransaction().begin();
		entityManager.persist(park);
		entityManager.getTransaction().commit();

		//exercise
		parkJPARepository.delete("0");

		//verify
		assertThat(entityManager.find(Park.class, "0")).isNull();
	}
	
	@Test
	public void testDeleteWhenMoreThanOneRecordInTheDatabase() {
		//setup
		Park park1 = new Park("0", "Maremma", "Toscana", 50, true);
		Park park2 = new Park("1", "Casentino", "Toscana", 50.5, false);

		entityManager.getTransaction().begin();
		entityManager.persist(park1);
		entityManager.persist(park2);
		entityManager.getTransaction().commit();

		//exercise
		parkJPARepository.delete("0");

		//verify
		assertThat(entityManager.find(Park.class, "0")).isNull();
		assertThat(entityManager.find(Park.class, "1")).isEqualTo(park2);
	}
}
