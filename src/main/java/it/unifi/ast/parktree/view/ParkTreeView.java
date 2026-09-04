package it.unifi.ast.parktree.view;

import java.util.List;

import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.Tree;

public interface ParkTreeView {

	public void showAllParks(List<Park> parks);

	public void showAllTrees(List<Tree> trees);

	public void showError(String string);

	public void parkAdded(Park park);

	public void parkDeleted(String string);

	public void treeAdded(Tree tree);

	public void treeDeleted(String string);

	public void showParkInfo(Park park, List<ParkTreeAssociation> associations);

	public void showTreeInfo(Tree tree, List<ParkTreeAssociation> associations);
}
