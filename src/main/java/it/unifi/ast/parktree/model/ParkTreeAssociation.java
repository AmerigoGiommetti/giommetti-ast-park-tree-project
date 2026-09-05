package it.unifi.ast.parktree.model;

import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;

@Entity
public class ParkTreeAssociation {

	@EmbeddedId
	private ParkTreeAssociationId id;

	// these two associations exist so that park_id/tree_id are real foreign
	// keys (enforced by the database), not just plain columns: the JPA
	// repository sets them from the embedded id's values before persisting
	// (see ParkTreeAssociationJPARepository#save), everyone else keeps
	// constructing this class from a ParkTreeAssociationId as before
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("parkId")
	@JoinColumn(name = "park_id")
	private Park park;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("treeId")
	@JoinColumn(name = "tree_id")
	private Tree tree;

	private int percentage;

	public ParkTreeAssociation() {
	}

	public ParkTreeAssociation(ParkTreeAssociationId id, int percentage) {
		this.id = id;
		this.percentage = percentage;
	}

	public ParkTreeAssociationId getId() {
		return id;
	}

	public void setId(ParkTreeAssociationId id) {
		this.id = id;
	}

	public Park getPark() {
		return park;
	}

	public void setPark(Park park) {
		this.park = park;
	}

	public Tree getTree() {
		return tree;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}

	public int getPercentage() {
		return percentage;
	}

	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Integer.valueOf(percentage), id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParkTreeAssociation other = (ParkTreeAssociation) obj;
		return percentage == other.percentage && Objects.equals(id, other.id);
	}
}
