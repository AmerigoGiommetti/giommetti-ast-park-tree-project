package it.unifi.ast.parktree.repository.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.repository.ParkRepository;

public class ParkJPARepository implements ParkRepository {
	private EntityManager entityManager;

	public ParkJPARepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<Park> findAll() {
		TypedQuery<Park> query = entityManager.createQuery(
				"SELECT p FROM Park p", Park.class);
			return query.getResultList();
	}

	@Override
	public Park findById(String id) {
		return entityManager.find(Park.class, id);
	}

	@Override
	public void save(Park park) {
		//The transaction will be delegated to the above layer
		entityManager.persist(park);
	}

	@Override
	public void delete(String id) {
		//The transaction will be delegated to the above layer
		Park park = entityManager.find(Park.class, id);
		if (park != null) {
			entityManager.remove(park);
		}
	}
}
