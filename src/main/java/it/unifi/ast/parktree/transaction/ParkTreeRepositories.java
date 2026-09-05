package it.unifi.ast.parktree.transaction;

import it.unifi.ast.parktree.repository.ParkRepository;
import it.unifi.ast.parktree.repository.ParkTreeAssociationRepository;
import it.unifi.ast.parktree.repository.TreeRepository;

public interface ParkTreeRepositories {

	ParkRepository parkRepository();

	TreeRepository treeRepository();

	ParkTreeAssociationRepository associationRepository();

}
