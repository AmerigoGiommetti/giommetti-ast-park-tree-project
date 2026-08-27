package it.unifi.ast.parktree.model;

public class Tree {
	
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
	
	

}
