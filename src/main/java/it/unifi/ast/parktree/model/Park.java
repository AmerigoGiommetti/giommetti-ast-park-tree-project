package it.unifi.ast.parktree.model;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//JPA annotations
@Entity
@Table(name = "park")
public class Park {

	@Id // primary key for JPA
	private String id;
	private String name;
	private String region;
	private double area; // in square meters
	private boolean freeAccess; // does the park have a ticket that cost to enter?

	public Park(String id, String name, String region, double area, boolean freeAccess) {
		super();
		this.id = id;
		this.name = name;
		this.region = region;
		this.area = area;
		this.freeAccess = freeAccess;
	}

	// needed for JPA
	protected Park() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public boolean isFreeAccess() {
		return freeAccess;
	}

	public void setFreeAccess(boolean freeAccess) {
		this.freeAccess = freeAccess;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Double.valueOf(area), Boolean.valueOf(freeAccess), id, name, region);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Park other = (Park) obj;
		return Double.doubleToLongBits(area) == Double.doubleToLongBits(other.area) && freeAccess == other.freeAccess
				&& Objects.equals(id, other.id) && Objects.equals(name, other.name)
				&& Objects.equals(region, other.region);
	}
}
