package it.unifi.ast.parktree.model;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.EmbeddedId;

@Entity
public class ParkTreeAssociation {

	@EmbeddedId
	private ParkTreeAssociationId id;
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
