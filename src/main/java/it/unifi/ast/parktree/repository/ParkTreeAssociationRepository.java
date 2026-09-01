package it.unifi.ast.parktree.repository;

import java.util.List;

import it.unifi.ast.parktree.model.ParkTreeAssociation;

public interface ParkTreeAssociationRepository {
	public List<ParkTreeAssociation> findByParkId(String parkId);

	public List<ParkTreeAssociation> findByTreeId(String treeId);

	public void save(ParkTreeAssociation association);

	public void deleteByParkId(String parkId);

}
