package it.unifi.ast.parktree.repository.jpa;

import java.util.List;

import javax.persistence.EntityManager;

import com.google.inject.Inject;

import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.Tree;
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
		// links park_id/tree_id as real foreign keys (see @MapsId on
		// ParkTreeAssociation): getReference is a lightweight proxy, no
		// extra query
		association.setPark(entityManager.getReference(Park.class, association.getId().getParkId()));
		association.setTree(entityManager.getReference(Tree.class, association.getId().getTreeId()));
		entityManager.persist(association);
	}

	@Override
	public void deleteByParkId(String parkId) {
		entityManager.createQuery("DELETE FROM ParkTreeAssociation a WHERE a.id.parkId = :parkId")
				.setParameter("parkId", parkId).executeUpdate();
	}
}
