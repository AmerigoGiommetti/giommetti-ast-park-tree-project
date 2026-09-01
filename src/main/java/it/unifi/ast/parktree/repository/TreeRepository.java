package it.unifi.ast.parktree.repository;

import java.util.List;
import it.unifi.ast.parktree.model.Tree;

public interface TreeRepository {
	public List<Tree> findAll();

	public Tree findById(String id);

	public void save(Tree tree);

	public void delete(String id);

}
