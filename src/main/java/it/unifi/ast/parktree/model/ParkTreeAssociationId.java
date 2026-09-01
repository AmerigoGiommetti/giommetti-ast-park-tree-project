package it.unifi.ast.parktree.model;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;

/*
 * This class is needed for the JPA primary key of the association
 */
@Embeddable
public class ParkTreeAssociationId implements Serializable {

	private static final long serialVersionUID = 1L;

	private String parkId;
	private String treeId;

	// empty constructor need for JPA
	public ParkTreeAssociationId() {
	}

	public ParkTreeAssociationId(String parkId, String treeId) {
		this.parkId = parkId;
		this.treeId = treeId;
	}

	public String getParkId() {
		return parkId;
	}

	public void setParkId(String parkId) {
		this.parkId = parkId;
	}

	public String getTreeId() {
		return treeId;
	}

	public void setTreeId(String treeId) {
		this.treeId = treeId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(parkId, treeId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParkTreeAssociationId other = (ParkTreeAssociationId) obj;
		return Objects.equals(parkId, other.parkId) && Objects.equals(treeId, other.treeId);
	}
}
