package it.unifi.ast.parktree.controller;

import static org.mockito.Mockito.*;
import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.model.Tree;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.ParkTreeAssociationId;
import it.unifi.ast.parktree.repository.ParkRepository;
import it.unifi.ast.parktree.repository.TreeRepository;
import it.unifi.ast.parktree.repository.ParkTreeAssociationRepository;
import it.unifi.ast.parktree.view.ParkTreeView;

public class ParkTreeControllerTest {

	@Mock
	private ParkRepository parkRepository;

	@Mock
	private TreeRepository treeRepository;

	@Mock
	private ParkTreeAssociationRepository associationRepository;

	@Mock
	private ParkTreeView parkTreeView;

	@InjectMocks
	private ParkTreeController parkTreeController; // SUT

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testAllParksDelegatesToRepositoryAndUpdatesView() {
		Park park = new Park("0", "Maremma", "Toscana", 50, true);
		List<Park> parks = asList(park);
		List<ParkTreeAssociation> associations = asList(
				new ParkTreeAssociation(new ParkTreeAssociationId("0", "1"), 100));
		when(parkRepository.findAll()).thenReturn(parks);
		when(parkRepository.findById("0")).thenReturn(park);
		when(associationRepository.findByParkId("0")).thenReturn(associations);

		parkTreeController.allParks();

		verify(parkTreeView).showAllParks(parks);
	}

	@Test
	public void testAllParksShouldLoadParkInfoForEachParkItselfWithoutDelegatingToTheView() {
		Park park1 = new Park("1", "Maremma", "Toscana", 50, true);
		Park park2 = new Park("2", "Cinque Terre", "Liguria", 30, false);
		List<Park> parks = asList(park1, park2);
		List<ParkTreeAssociation> associations1 = asList(
				new ParkTreeAssociation(new ParkTreeAssociationId("1", "1"), 100));
		List<ParkTreeAssociation> associations2 = Collections.emptyList();
		when(parkRepository.findAll()).thenReturn(parks);
		when(parkRepository.findById("1")).thenReturn(park1);
		when(parkRepository.findById("2")).thenReturn(park2);
		when(associationRepository.findByParkId("1")).thenReturn(associations1);
		when(associationRepository.findByParkId("2")).thenReturn(associations2);

		parkTreeController.allParks();

		verify(parkTreeView).showParkInfo(park1, associations1);
		verify(parkTreeView).showParkInfo(park2, associations2);
	}

	@Test
	public void testAllTreesDelegatesToRepositoryAndUpdatesView() {
		List<Tree> trees = asList(new Tree("1", "Faggio", false, 50));
		when(treeRepository.findAll()).thenReturn(trees);

		parkTreeController.allTrees();

		verify(parkTreeView).showAllTrees(trees);
	}

	@Test
	public void testAddParkWhenParkAlreadyExistsShouldShowErrorAndNotSave() {
		Park existingPark = new Park("1", "Maremma", "Toscana", 50, true);
		Park newPark = new Park("1", "Nuova Maremma", "Toscana", 60, false);
		List<ParkTreeAssociation> associations = Collections.emptyList();
		when(parkRepository.findById("1")).thenReturn(existingPark);

		parkTreeController.addPark(newPark, associations);

		verify(parkTreeView).showError("Already existing park with id 1");
		verify(parkTreeView, never()).parkAdded(any());
		verify(parkRepository, never()).save(any());
		verifyNoInteractions(associationRepository);
	}

	@Test
	public void testAddParkWhenTotalPercentageExceeds100ShouldShowErrorAndNotSave() {
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		ParkTreeAssociationId id1 = new ParkTreeAssociationId("1", "1");
		ParkTreeAssociationId id2 = new ParkTreeAssociationId("1", "2");
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(id1, 60);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(id2, 50);
		List<ParkTreeAssociation> associations = asList(assoc1, assoc2);
		when(parkRepository.findById("1")).thenReturn(null);

		parkTreeController.addPark(park, associations);

		verify(parkTreeView).showError("Total tree percentage must be exactly 100% (was 110%)");
		verify(parkTreeView, never()).parkAdded(any());
		verify(parkRepository, never()).save(any());
		verify(associationRepository, never()).save(any());
	}

	@Test
	public void testAddParkWhenTotalPercentageIsLowerThan100ShouldShowErrorAndNotSave() {
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		ParkTreeAssociationId id1 = new ParkTreeAssociationId("1", "1");
		ParkTreeAssociationId id2 = new ParkTreeAssociationId("1", "2");
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(id1, 40);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(id2, 50);
		List<ParkTreeAssociation> associations = asList(assoc1, assoc2);
		when(parkRepository.findById("1")).thenReturn(null);

		parkTreeController.addPark(park, associations);

		verify(parkTreeView).showError("Total tree percentage must be exactly 100% (was 90%)");
		verify(parkTreeView, never()).parkAdded(any());
		verify(parkRepository, never()).save(any());
		verify(associationRepository, never()).save(any());
	}

	@Test
	public void testAddParkSuccessShouldSaveParkAndAssociationsAndUpdateView() {
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		ParkTreeAssociationId id1 = new ParkTreeAssociationId("1", "1");
		ParkTreeAssociationId id2 = new ParkTreeAssociationId("1", "2");
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(id1, 40);
		ParkTreeAssociation assoc2 = new ParkTreeAssociation(id2, 60);
		List<ParkTreeAssociation> associations = asList(assoc1, assoc2);
		when(parkRepository.findById("1")).thenReturn(null);

		parkTreeController.addPark(park, associations);

		verify(parkRepository).save(park);
		verify(associationRepository).save(assoc1);
		verify(associationRepository).save(assoc2);
		verify(parkTreeView).parkAdded(park);
	}

	@Test
	public void testAddParkSuccessShouldAlsoLoadParkInfoItselfWithoutDelegatingToTheView() {
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		ParkTreeAssociationId id1 = new ParkTreeAssociationId("1", "1");
		ParkTreeAssociation assoc1 = new ParkTreeAssociation(id1, 100);
		List<ParkTreeAssociation> associations = asList(assoc1);
		// first call (existence check) returns null, second call (parkInfo, after save) returns the saved park
		when(parkRepository.findById("1")).thenReturn(null, park);
		when(associationRepository.findByParkId("1")).thenReturn(associations);

		parkTreeController.addPark(park, associations);

		verify(parkTreeView).parkAdded(park);
		verify(parkTreeView).showParkInfo(park, associations);
	}

	@Test
	public void testDeleteParkWhenParkDoesNotExistShouldShowErrorAndNotDelete() {
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		when(parkRepository.findById("1")).thenReturn(null);

		parkTreeController.deletePark(park);

		verify(parkTreeView).showError("No such park with id 1");
		verify(parkTreeView, never()).parkDeleted(anyString());
		verify(parkRepository, never()).delete(anyString());
		verify(associationRepository, never()).deleteByParkId(anyString());
	}

	@Test
	public void testDeleteParkSuccessShouldDeleteParkAndAssociationsAndUpdateView() {
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		when(parkRepository.findById("1")).thenReturn(park);

		parkTreeController.deletePark(park);

		verify(parkRepository).delete("1");
		verify(associationRepository).deleteByParkId("1");
		verify(parkTreeView).parkDeleted("1");
	}

	@Test
	public void testAddTreeWhenTreeAlreadyExistsShouldShowErrorAndNotSave() {
		Tree existingTree = new Tree("1", "Faggio", false, 50);
		Tree newTree = new Tree("1", "Abete Bianco", true, 80);
		when(treeRepository.findById("1")).thenReturn(existingTree);

		parkTreeController.addTree(newTree);

		verify(parkTreeView).showError("Already existing tree with id 1");
		verify(treeRepository, never()).save(any());
		verify(parkTreeView, never()).treeAdded(any());
	}

	@Test
	public void testAddTreeSuccessShouldSaveTreeAndUpdateView() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		when(treeRepository.findById("1")).thenReturn(null);

		parkTreeController.addTree(tree);

		verify(treeRepository).save(tree);
		verify(parkTreeView).treeAdded(tree);
	}

	@Test
	public void testDeleteTreeWhenTreeDoesNotExistShouldShowErrorAndNotDelete() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		when(treeRepository.findById("1")).thenReturn(null);

		parkTreeController.deleteTree(tree);

		verify(parkTreeView).showError("No such tree with id 1");
		verify(treeRepository, never()).delete(anyString());
		verify(parkTreeView, never()).treeDeleted(anyString());
	}

	@Test
	public void testDeleteTreeWhenTreeIsAssociatedWithParksShouldShowErrorAndNotDelete() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		ParkTreeAssociationId id = new ParkTreeAssociationId("1", "1");
		ParkTreeAssociation association = new ParkTreeAssociation(id, 30);
		when(treeRepository.findById("1")).thenReturn(tree);
		when(associationRepository.findByTreeId("1")).thenReturn(asList(association));

		parkTreeController.deleteTree(tree);

		verify(parkTreeView).showError("Cannot delete tree with id 1: associated with one or more parks");
		verify(treeRepository, never()).delete(anyString());
		verify(parkTreeView, never()).treeDeleted(anyString());
	}

	@Test
	public void testDeleteTreeSuccessShouldDeleteTreeAndUpdateView() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		when(treeRepository.findById("1")).thenReturn(tree);
		when(associationRepository.findByTreeId("1")).thenReturn(Collections.emptyList());

		parkTreeController.deleteTree(tree);

		verify(treeRepository).delete("1");
		verify(parkTreeView).treeDeleted("1");
	}

	@Test
	public void testParkInfoShouldRetrieveAssociationsAndUpdatesView() {
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		ParkTreeAssociationId id = new ParkTreeAssociationId("1", "1");
		List<ParkTreeAssociation> associations = asList(new ParkTreeAssociation(id, 40));
		when(associationRepository.findByParkId("1")).thenReturn(associations);
		when(parkRepository.findById("1")).thenReturn(park);

		parkTreeController.parkInfo(park);

		verify(parkTreeView).showParkInfo(park, associations);
	}

	@Test
	public void testParkInfoWhenParkDoesNotExistShouldShowErrorAndNotRetrieveAssociations() {
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		when(parkRepository.findById("1")).thenReturn(null);

		parkTreeController.parkInfo(park);

		verify(parkTreeView).showError("No such park with id 1");
		verifyNoInteractions(associationRepository);
		verify(parkTreeView, never()).showParkInfo(any(), any());
	}

	@Test
	public void testTreeInfoShouldRetrieveAssociationsAndUpdatesView() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		ParkTreeAssociationId id = new ParkTreeAssociationId("2", "1");
		List<ParkTreeAssociation> associations = asList(new ParkTreeAssociation(id, 40));
		when(associationRepository.findByTreeId("1")).thenReturn(associations);
		when(treeRepository.findById("1")).thenReturn(tree);

		parkTreeController.treeInfo(tree);

		verify(parkTreeView).showTreeInfo(tree, associations);
	}

	@Test
	public void testTreeInfoWhenTreeDoesNotExistShouldShowErrorAndNotRetrieveAssociations() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		when(treeRepository.findById("1")).thenReturn(null);

		parkTreeController.treeInfo(tree);

		verify(parkTreeView).showError("No such tree with id 1");
		verifyNoInteractions(associationRepository);
		verify(parkTreeView, never()).showTreeInfo(any(), any());
	}
}