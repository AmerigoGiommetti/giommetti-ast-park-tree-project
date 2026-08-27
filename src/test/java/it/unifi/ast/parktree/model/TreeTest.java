//NB this test won't be in the final version of the project since it is only testing
//POJO (Plain old java objects) and was only an exercise to train TDD

package it.unifi.ast.parktree.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class TreeTest {
	@Test
	public void treeCreationTest(){
		//Setup
		Tree tree = new Tree("1", "pine", true, 100);
		
		//Verify
		assertThat(tree.getId()).isEqualTo("1");
		assertThat(tree.getName()).isEqualTo("pine");
		assertThat(tree.isEvergreen()).isEqualTo(true);
		assertThat(tree.getMediumLifespan()).isEqualTo(50);
	}

}
