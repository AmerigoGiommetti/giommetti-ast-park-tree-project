package it.unifi.ast.parktree.repository.jpa;

import java.util.List;

import javax.persistence.EntityManager;

import com.google.inject.Inject;

import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.repository.ParkTreeAssociationRepository;

public class ParkTreeAssociationJPARepository implements ParkTreeAssociationRepository {

	private EntityManager entityManager;

	@Inject
	public ParkTreeAssociationJPARepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<ParkTreeAssociation> findByParkId(String parkId) {
		return entityManager.createQuery("SELECT a FROM ParkTreeAssociation a WHERE a.id.parkId = :parkId",
				ParkTreeAssociation.class).setParameter("parkId", parkId).getResultList();
	}

	@Override
	public List<ParkTreeAssociation> findByTreeId(String treeId) {
		return entityManager.createQuery("SELECT a FROM ParkTreeAssociation a WHERE a.id.treeId = :treeId",
				ParkTreeAssociation.class).setParameter("treeId", treeId).getResultList();
	}

	@Override
	public void save(ParkTreeAssociation association) {
		entityManager.persist(association);
	}

	@Override
	public void deleteByParkId(String parkId) {
		entityManager.createQuery("DELETE FROM ParkTreeAssociation a WHERE a.id.parkId = :parkId")
				.setParameter("parkId", parkId).executeUpdate();
	}
}
