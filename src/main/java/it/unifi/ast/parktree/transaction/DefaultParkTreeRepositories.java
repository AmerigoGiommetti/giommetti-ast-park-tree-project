package it.unifi.ast.parktree.transaction;

import com.google.inject.Inject;

import it.unifi.ast.parktree.repository.ParkRepository;
import it.unifi.ast.parktree.repository.ParkTreeAssociationRepository;
import it.unifi.ast.parktree.repository.TreeRepository;

public class DefaultParkTreeRepositories implements ParkTreeRepositories {

	private final ParkRepository parkRepository;
	private final TreeRepository treeRepository;
	private final ParkTreeAssociationRepository associationRepository;

	@Inject
	public DefaultParkTreeRepositories(ParkRepository parkRepository, TreeRepository treeRepository,
			ParkTreeAssociationRepository associationRepository) {
		this.parkRepository = parkRepository;
		this.treeRepository = treeRepository;
		this.associationRepository = associationRepository;
	}

	@Override
	public ParkRepository parkRepository() {
		return parkRepository;
	}

	@Override
	public TreeRepository treeRepository() {
		return treeRepository;
	}

	@Override
	public ParkTreeAssociationRepository associationRepository() {
		return associationRepository;
	}

}
