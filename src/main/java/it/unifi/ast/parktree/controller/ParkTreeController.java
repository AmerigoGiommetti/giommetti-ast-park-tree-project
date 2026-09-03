package it.unifi.ast.parktree.controller;

import java.util.List;

import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.Tree;
import it.unifi.ast.parktree.repository.ParkRepository;
import it.unifi.ast.parktree.repository.ParkTreeAssociationRepository;
import it.unifi.ast.parktree.repository.TreeRepository;
import it.unifi.ast.parktree.view.ParkTreeView;

public class ParkTreeController {

	private ParkRepository parkRepository;
	private TreeRepository treeRepository;
	private ParkTreeAssociationRepository associationRepository;
	private ParkTreeView parkTreeView;

	public ParkTreeController(ParkRepository parkRepository, TreeRepository treeRepository,
			ParkTreeAssociationRepository associationRepository, ParkTreeView parkTreeView) {
		this.parkRepository = parkRepository;
		this.treeRepository = treeRepository;
		this.associationRepository = associationRepository;
		this.parkTreeView = parkTreeView;
	}

	public void allParks() {
		List<Park> parks = parkRepository.findAll();
		parkTreeView.showAllParks(parks);
		for (Park park : parks) {
			parkInfo(park);
		}
	}

	public void allTrees() {
		parkTreeView.showAllTrees(treeRepository.findAll());
	}

	public void addPark(Park park, List<ParkTreeAssociation> associations) {
		if (park == null || park.getId() == null) {
			parkTreeView.showError("Invalid park data");
			return;
		}

		if (parkRepository.findById(park.getId()) != null) {
			parkTreeView.showError("Already existing park with id " + park.getId());
			return;
		}

		int totalPercentage = 0;
		if (associations != null) {
			totalPercentage = associations.stream().mapToInt(ParkTreeAssociation::getPercentage).sum();
		}

		if (totalPercentage > 100) {
			parkTreeView.showError("Total tree percentage must be exactly 100% (was " + totalPercentage + "%)");
			return;
		}

		if (totalPercentage < 100) {
			parkTreeView.showError("Total tree percentage must be exactly 100% (was " + totalPercentage + "%)");
			return;
		}

		parkRepository.save(park);
		if (associations != null) {
			associations.forEach(associationRepository::save);
		}
		parkTreeView.parkAdded(park);
		parkInfo(park);
	}

	public void deletePark(Park park) {
		if (park == null || park.getId() == null) {
			parkTreeView.showError("Invalid park data");
			return;
		}

		if (parkRepository.findById(park.getId()) == null) {
			parkTreeView.showError("No such park with id " + park.getId());
			return;
		}

		parkRepository.delete(park.getId());
		associationRepository.deleteByParkId(park.getId());
		parkTreeView.parkDeleted(park.getId());
	}

	public void addTree(Tree tree) {
		if (tree == null || tree.getId() == null) {
			parkTreeView.showError("Invalid tree data");
			return;
		}

		if (treeRepository.findById(tree.getId()) != null) {
			parkTreeView.showError("Already existing tree with id " + tree.getId());
			return;
		}

		treeRepository.save(tree);
		parkTreeView.treeAdded(tree);
	}

	public void deleteTree(Tree tree) {
		if (tree == null || tree.getId() == null) {
			parkTreeView.showError("Invalid tree data");
			return;
		}

		if (treeRepository.findById(tree.getId()) == null) {
			parkTreeView.showError("No such tree with id " + tree.getId());
			return;
		}

		List<ParkTreeAssociation> activeAssociations = associationRepository.findByTreeId(tree.getId());
		if (activeAssociations != null && !activeAssociations.isEmpty()) {
			parkTreeView
					.showError("Cannot delete tree with id " + tree.getId() + ": associated with one or more parks");
			return;
		}

		treeRepository.delete(tree.getId());
		parkTreeView.treeDeleted(tree.getId());
	}

	public void parkInfo(Park park) {
		if (park == null || park.getId() == null) {
			return;
		}
		if (parkRepository.findById(park.getId()) == null) {
			parkTreeView.showError("No such park with id " + park.getId());
			return;
		}
		List<ParkTreeAssociation> associations = associationRepository.findByParkId(park.getId());
		parkTreeView.showParkInfo(park, associations);
	}

	public void treeInfo(Tree tree) {
		if (tree == null || tree.getId() == null) {
			return;
		}
		if (treeRepository.findById(tree.getId()) == null) {
			parkTreeView.showError("No such tree with id " + tree.getId());
			return;
		}
		List<ParkTreeAssociation> associations = associationRepository.findByTreeId(tree.getId());
		parkTreeView.showTreeInfo(tree, associations);
	}
}