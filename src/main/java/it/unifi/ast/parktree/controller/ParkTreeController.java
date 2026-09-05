package it.unifi.ast.parktree.controller;

import java.util.List;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.Tree;
import it.unifi.ast.parktree.transaction.TransactionManager;
import it.unifi.ast.parktree.view.ParkTreeView;

public class ParkTreeController {

	private TransactionManager transactionManager;
	private ParkTreeView parkTreeView;

	@Inject
	public ParkTreeController(@Assisted ParkTreeView parkTreeView, TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
		this.parkTreeView = parkTreeView;
	}

	public void allParks() {
		transactionManager.doInTransaction(repositories -> {
			List<Park> parks = repositories.parkRepository().findAll();
			parkTreeView.showAllParks(parks);
			for (Park park : parks) {
				parkInfo(park);
			}
			return null;
		});
	}

	public void allTrees() {
		transactionManager.doInTransaction(repositories -> {
			parkTreeView.showAllTrees(repositories.treeRepository().findAll());
			return null;
		});
	}

	public void addPark(Park park, List<ParkTreeAssociation> associations) {
		transactionManager.doInTransaction(repositories -> {
			if (park == null || park.getId() == null) {
				parkTreeView.showError("Invalid park data");
				return null;
			}

			if (repositories.parkRepository().findById(park.getId()) != null) {
				parkTreeView.showError("Already existing park with id " + park.getId());
				return null;
			}

			int totalPercentage = 0;
			if (associations != null) {
				totalPercentage = associations.stream().mapToInt(ParkTreeAssociation::getPercentage).sum();
			}

			if (totalPercentage > 100) {
				parkTreeView.showError("Total tree percentage must be exactly 100% (was " + totalPercentage + "%)");
				return null;
			}

			if (totalPercentage < 100) {
				parkTreeView.showError("Total tree percentage must be exactly 100% (was " + totalPercentage + "%)");
				return null;
			}

			repositories.parkRepository().save(park);
			// null associations means totalPercentage stayed 0, which always
			// returns above (0 != 100): reaching this point guarantees non-null.
			associations.forEach(repositories.associationRepository()::save);
			parkTreeView.parkAdded(park);
			parkInfo(park);
			return null;
		});
	}

	public void deletePark(Park park) {
		transactionManager.doInTransaction(repositories -> {
			if (park == null || park.getId() == null) {
				parkTreeView.showError("Invalid park data");
				return null;
			}

			if (repositories.parkRepository().findById(park.getId()) == null) {
				parkTreeView.showError("No such park with id " + park.getId());
				return null;
			}

			// associations reference this park via a real foreign key
			// (park_id): they must go first, or the database would reject
			// deleting a park that still has associations pointing to it
			repositories.associationRepository().deleteByParkId(park.getId());
			repositories.parkRepository().delete(park.getId());
			parkTreeView.parkDeleted(park.getId());
			return null;
		});
	}

	public void addTree(Tree tree) {
		transactionManager.doInTransaction(repositories -> {
			if (tree == null || tree.getId() == null) {
				parkTreeView.showError("Invalid tree data");
				return null;
			}

			if (repositories.treeRepository().findById(tree.getId()) != null) {
				parkTreeView.showError("Already existing tree with id " + tree.getId());
				return null;
			}

			repositories.treeRepository().save(tree);
			parkTreeView.treeAdded(tree);
			return null;
		});
	}

	public void deleteTree(Tree tree) {
		transactionManager.doInTransaction(repositories -> {
			if (tree == null || tree.getId() == null) {
				parkTreeView.showError("Invalid tree data");
				return null;
			}

			if (repositories.treeRepository().findById(tree.getId()) == null) {
				parkTreeView.showError("No such tree with id " + tree.getId());
				return null;
			}

			List<ParkTreeAssociation> activeAssociations = repositories.associationRepository()
					.findByTreeId(tree.getId());
			// both repository implementations always return a (possibly empty)
			// list, never null
			if (!activeAssociations.isEmpty()) {
				parkTreeView
						.showError("Cannot delete tree with id " + tree.getId() + ": associated with one or more parks");
				return null;
			}

			repositories.treeRepository().delete(tree.getId());
			parkTreeView.treeDeleted(tree.getId());
			return null;
		});
	}

	public void parkInfo(Park park) {
		transactionManager.doInTransaction(repositories -> {
			if (park == null || park.getId() == null) {
				return null;
			}
			if (repositories.parkRepository().findById(park.getId()) == null) {
				parkTreeView.showError("No such park with id " + park.getId());
				return null;
			}
			List<ParkTreeAssociation> associations = repositories.associationRepository().findByParkId(park.getId());
			parkTreeView.showParkInfo(park, associations);
			return null;
		});
	}

	public void treeInfo(Tree tree) {
		transactionManager.doInTransaction(repositories -> {
			if (tree == null || tree.getId() == null) {
				return null;
			}
			if (repositories.treeRepository().findById(tree.getId()) == null) {
				parkTreeView.showError("No such tree with id " + tree.getId());
				return null;
			}
			List<ParkTreeAssociation> associations = repositories.associationRepository().findByTreeId(tree.getId());
			parkTreeView.showTreeInfo(tree, associations);
			return null;
		});
	}
}
