package it.unifi.ast.parktree.repository.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import it.unifi.ast.parktree.model.Tree;
import it.unifi.ast.parktree.repository.TreeRepository;

public class TreeJPARepository implements TreeRepository{
	
	private EntityManager entityManager;
	
	public TreeJPARepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<Tree> findAll() {
		TypedQuery<Tree> query = entityManager.createQuery(
			"SELECT t FROM Tree t", Tree.class);
		return query.getResultList();
	}

	@Override
	public Tree findById(String id) {
		return entityManager.find(Tree.class, id);
	}

	@Override
	public void save(Tree tree) {
		//The transaction is NOT competence of the repository
		entityManager.persist(tree);
	}

	@Override
	public void delete(String id) {
		//The transaction will be implemented in the level above
		Tree tree = entityManager.find(Tree.class, id);
		if (tree != null) {
			entityManager.remove(tree);
		}
	}
}
