package it.unifi.ast.parktree.model;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//JPA Annotations
@Entity
@Table(name="tree")
public class Tree {
	
	@Id //primary key dor JPA
	private String id;
	private String name;
	private boolean evergreen;
	private int mediumLifespan;

	public Tree(String id, String name, boolean evergreen, int mediumLifespan) {
		this.id = id;
		this.name = name;
		this.evergreen = evergreen;
		this.mediumLifespan = mediumLifespan;
	}
	
	//needed for JPA
	protected Tree() {}

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

	public boolean isEvergreen() {
		return evergreen;
	}

	public void setEvergreen(boolean evergreen) {
		this.evergreen = evergreen;
	}

	public int getMediumLifespan() {
		return mediumLifespan;
	}

	public void setMediumLifespan(int mediumLifespan) {
		this.mediumLifespan = mediumLifespan;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Boolean.valueOf(evergreen), id, Integer.valueOf(mediumLifespan), name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tree other = (Tree) obj;
		return evergreen == other.evergreen && Objects.equals(id, other.id) && mediumLifespan == other.mediumLifespan
				&& Objects.equals(name, other.name);
	}
	
	

}
