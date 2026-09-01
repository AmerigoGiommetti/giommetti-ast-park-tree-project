package it.unifi.ast.parktree.repository;

import java.util.List;
import it.unifi.ast.parktree.model.Park;

public interface ParkRepository {
	public List<Park> findAll();

	public Park findById(String id);

	public void save(Park park);

	public void delete(String id);

}
