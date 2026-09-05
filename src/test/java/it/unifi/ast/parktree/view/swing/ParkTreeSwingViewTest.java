package it.unifi.ast.parktree.view.swing;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTabbedPaneFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unifi.ast.parktree.controller.ParkTreeController;
import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.ParkTreeAssociationId;
import it.unifi.ast.parktree.model.Tree;

@RunWith(GUITestRunner.class)
public class ParkTreeSwingViewTest extends AssertJSwingJUnitTestCase {

	private FrameFixture window;

	private ParkTreeSwingView parkTreeSwingView;

	@Mock
	private ParkTreeController parkTreeController;

	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			parkTreeSwingView = new ParkTreeSwingView();
			parkTreeSwingView.setParkTreeController(parkTreeController);
			return parkTreeSwingView;
		});
		window = new FrameFixture(robot(), parkTreeSwingView);
		window.show();
	}

	@Override
	protected void onTearDown() throws Exception {
		if (closeable != null)
			closeable.close();
	}

	@Test
	@GUITest
	public void testTabTitlesArePresent() {
		JTabbedPaneFixture tabs = window.tabbedPane("tabbedPane");
		assertThat(tabs.target().getTitleAt(0)).isEqualTo("Add Tree");
		assertThat(tabs.target().getTitleAt(1)).isEqualTo("Add Park");
		assertThat(tabs.target().getTitleAt(2)).isEqualTo("All Trees");
		assertThat(tabs.target().getTitleAt(3)).isEqualTo("All Parks");
	}

	@Test
	@GUITest
	public void testAddTreeTabControlsInitialState() {
		window.label(JLabelMatcher.withText("id").andShowing());
		window.textBox("treeIdTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("name").andShowing());
		window.textBox("treeNameTextBox").requireEnabled();
		window.checkBox("evergreenCheckBox").requireEnabled().requireNotSelected();
		window.label(JLabelMatcher.withText("lifespan (years)").andShowing());
		window.textBox("treeLifespanTextBox").requireEnabled();
		window.button("addTreeButton").requireDisabled();
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	@GUITest
	public void testAddParkTabControlsInitialState() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.label(JLabelMatcher.withText("id").andShowing());
		window.textBox("parkIdTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("name").andShowing());
		window.textBox("parkNameTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("region").andShowing());
		window.textBox("parkRegionTextBox").requireEnabled();
		window.label(JLabelMatcher.withText("area (square meters)").andShowing());
		window.textBox("parkAreaTextBox").requireEnabled();
		window.checkBox("freelyAccessibleCheckBox").requireEnabled().requireNotSelected();
		window.button("addAssociationButton").requireEnabled();
		window.button("addParkButton").requireDisabled();
		assertThat(parkTreeSwingView.getAssociationsPanel().getComponentCount()).isZero();
	}

	@Test
	@GUITest
	public void testAllTreesTabInitiallyEmpty() {
		window.tabbedPane("tabbedPane").selectTab("All Trees");
		assertThat(parkTreeSwingView.getTreesListPanel().getComponentCount()).isZero();
	}

	@Test
	@GUITest
	public void testAllParksTabInitiallyEmpty() {
		window.tabbedPane("tabbedPane").selectTab("All Parks");
		assertThat(parkTreeSwingView.getParksListPanel().getComponentCount()).isZero();
	}

	@Test
	@GUITest
	public void testAddTreeButtonShouldBeEnabledWhenAllFieldsAreValid() {
		window.textBox("treeIdTextBox").enterText("1");
		window.textBox("treeNameTextBox").enterText("Faggio");
		window.textBox("treeLifespanTextBox").enterText("50");
		window.button("addTreeButton").requireEnabled();
	}

	@Test
	@GUITest
	public void testAddTreeButtonShouldBeDisabledWhenIdIsBlank() {
		window.textBox("treeIdTextBox").enterText(" ");
		window.textBox("treeNameTextBox").enterText("Faggio");
		window.textBox("treeLifespanTextBox").enterText("50");
		window.button("addTreeButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testAddTreeButtonShouldBeDisabledWhenNameIsBlank() {
		window.textBox("treeIdTextBox").enterText("1");
		window.textBox("treeNameTextBox").enterText(" ");
		window.textBox("treeLifespanTextBox").enterText("50");
		window.button("addTreeButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testAddTreeButtonShouldBeDisabledWhenLifespanIsNotAValidInteger() {
		window.textBox("treeIdTextBox").enterText("1");
		window.textBox("treeNameTextBox").enterText("Faggio");
		window.textBox("treeLifespanTextBox").enterText("abc");
		window.button("addTreeButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testAddTreeButtonShouldDelegateToControllerAddTree() {
		window.textBox("treeIdTextBox").enterText("1");
		window.textBox("treeNameTextBox").enterText("Faggio");
		window.checkBox("evergreenCheckBox").check();
		window.textBox("treeLifespanTextBox").enterText("50");

		window.button("addTreeButton").click();

		verify(parkTreeController).addTree(new Tree("1", "Faggio", true, 50));
	}

	@Test
	@GUITest
	public void testTreeAddedShouldResetFormAndAddRowToAllTreesListAndResetErrorLabel() {
		window.textBox("treeIdTextBox").enterText("1");
		window.textBox("treeNameTextBox").enterText("Faggio");
		window.checkBox("evergreenCheckBox").check();
		window.textBox("treeLifespanTextBox").enterText("50");

		GuiActionRunner.execute(() -> parkTreeSwingView.showError("some previous error"));
		robot().waitForIdle();

		Tree tree = new Tree("1", "Faggio", true, 50);
		GuiActionRunner.execute(() -> parkTreeSwingView.treeAdded(tree));
		robot().waitForIdle();

		window.textBox("treeIdTextBox").requireText("");
		window.textBox("treeNameTextBox").requireText("");
		window.checkBox("evergreenCheckBox").requireNotSelected();
		window.textBox("treeLifespanTextBox").requireText("");
		window.button("addTreeButton").requireDisabled();
		window.label("errorMessageLabel").requireText(" ");

		window.tabbedPane("tabbedPane").selectTab("All Trees");
		window.label("treeRowLabel_1");
		window.button("deleteTreeButton_1");
	}

	@Test
	@GUITest
	public void testShowErrorShouldShowMessageAndNotResetAddTreeFields() {
		window.textBox("treeIdTextBox").enterText("1");
		window.textBox("treeNameTextBox").enterText("Faggio");
		window.textBox("treeLifespanTextBox").enterText("50");

		GuiActionRunner.execute(() -> parkTreeSwingView.showError("Already existing tree with id 1"));
		robot().waitForIdle();

		window.textBox("treeIdTextBox").requireText("1");
		window.textBox("treeNameTextBox").requireText("Faggio");
		window.textBox("treeLifespanTextBox").requireText("50");
		window.label("errorMessageLabel").requireText("Already existing tree with id 1");
	}

	@Test
	@GUITest
	public void testShowAllTreesShouldPopulateTreeRowsWithDeleteButtons() {
		Tree tree1 = new Tree("1", "Faggio", false, 50);
		Tree tree2 = new Tree("2", "Abete Bianco", true, 80);

		window.tabbedPane("tabbedPane").selectTab("All Trees");
		GuiActionRunner.execute(() -> parkTreeSwingView.showAllTrees(asList(tree1, tree2)));
		robot().waitForIdle();

		window.label("treeRowLabel_1");
		window.button("deleteTreeButton_1");
		window.label("treeRowLabel_2");
		window.button("deleteTreeButton_2");
	}

	@Test
	@GUITest
	public void testDeleteButtonOnTreeRowShouldDelegateToControllerDeleteTree() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		window.tabbedPane("tabbedPane").selectTab("All Trees");
		GuiActionRunner.execute(() -> parkTreeSwingView.showAllTrees(asList(tree)));
		robot().waitForIdle();

		window.button("deleteTreeButton_1").click();

		verify(parkTreeController).deleteTree(tree);
	}

	@Test
	@GUITest
	public void testTreeDeletedShouldRemoveRowFromAllTreesList() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		window.tabbedPane("tabbedPane").selectTab("All Trees");
		GuiActionRunner.execute(() -> parkTreeSwingView.showAllTrees(asList(tree)));
		robot().waitForIdle();

		GuiActionRunner.execute(() -> parkTreeSwingView.treeDeleted("1"));
		robot().waitForIdle();

		assertThat(parkTreeSwingView.getTreesListPanel().getComponentCount()).isZero();
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	@GUITest
	public void testSelectingAllTreesTabShouldDelegateToControllerAllTrees() {
		window.tabbedPane("tabbedPane").selectTab("All Trees");
		verify(parkTreeController).allTrees();
	}

	@Test
	@GUITest
	public void testAddParkButtonShouldBeEnabledWhenRequiredFieldsAreValid() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("50.5");
		window.button("addParkButton").requireEnabled();
	}

	@Test
	@GUITest
	public void testAddParkButtonShouldBeDisabledWhenIdIsBlank() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.textBox("parkIdTextBox").enterText(" ");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("50.5");
		window.button("addParkButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testAddParkButtonShouldBeDisabledWhenNameIsBlank() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText(" ");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("50.5");
		window.button("addParkButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testAddParkButtonShouldBeDisabledWhenRegionIsBlank() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText(" ");
		window.textBox("parkAreaTextBox").enterText("50.5");
		window.button("addParkButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testAddParkButtonShouldBeDisabledWhenAreaIsNotAValidDouble() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("abc");
		window.button("addParkButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testAddParkButtonShouldBeDisabledWhenAreaIsNegative() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("-50.5");
		window.button("addParkButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testAddParkButtonShouldBeDisabledWhenAreaIsZero() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("0");
		window.button("addParkButton").requireDisabled();
	}

	@Test
	@GUITest
	public void testAddTreeAssociationButtonShouldAddANewRow() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.button("addAssociationButton").click();
		robot().waitForIdle();

		window.comboBox("treeComboBox_0");
		window.textBox("percentageTextBox_0");
		window.button("removeAssociationButton_0");
		assertThat(parkTreeSwingView.getAssociationsPanel().getComponentCount()).isEqualTo(1);
	}

	@Test
	@GUITest
	public void testAddTreeAssociationButtonClickedTwiceShouldAddTwoRows() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.button("addAssociationButton").click();
		robot().waitForIdle();
		window.button("addAssociationButton").click();
		robot().waitForIdle();

		window.comboBox("treeComboBox_0");
		window.comboBox("treeComboBox_1");
		assertThat(parkTreeSwingView.getAssociationsPanel().getComponentCount()).isEqualTo(2);
	}

	@Test
	@GUITest
	public void testRemoveButtonShouldRemoveOnlyThatRow() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.button("addAssociationButton").click();
		robot().waitForIdle();
		window.button("addAssociationButton").click();
		robot().waitForIdle();

		window.button("removeAssociationButton_0").click();
		robot().waitForIdle();

		assertThat(parkTreeSwingView.getAssociationsPanel().getComponentCount()).isEqualTo(1);
		window.comboBox("treeComboBox_1");
	}

	@Test
	@GUITest
	public void testAddParkButtonShouldDelegateToControllerAddParkWithAssociations() {
		Tree tree1 = new Tree("1", "Faggio", false, 50);
		Tree tree2 = new Tree("2", "Abete Bianco", true, 80);
		GuiActionRunner.execute(() -> parkTreeSwingView.showAllTrees(asList(tree1, tree2)));
		robot().waitForIdle();

		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("50.5");
		window.checkBox("freelyAccessibleCheckBox").check();

		window.button("addAssociationButton").click();
		window.comboBox("treeComboBox_0").selectItem(0);
		window.textBox("percentageTextBox_0").enterText("40");

		window.button("addAssociationButton").click();
		window.comboBox("treeComboBox_1").selectItem(1);
		window.textBox("percentageTextBox_1").enterText("60");

		window.button("addParkButton").click();

		Park expectedPark = new Park("1", "Maremma", "Toscana", 50.5, true);
		ParkTreeAssociation expectedAssociation1 = new ParkTreeAssociation(new ParkTreeAssociationId("1", "1"), 40);
		ParkTreeAssociation expectedAssociation2 = new ParkTreeAssociation(new ParkTreeAssociationId("1", "2"), 60);
		verify(parkTreeController).addPark(expectedPark, asList(expectedAssociation1, expectedAssociation2));
	}

	@Test
	@GUITest
	public void testAddParkButtonShouldSkipRowsWithNoTreeSelectedOrInvalidPercentage() {
		Tree tree1 = new Tree("1", "Faggio", false, 50);
		GuiActionRunner.execute(() -> parkTreeSwingView.showAllTrees(asList(tree1)));
		robot().waitForIdle();

		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("50.5");

		window.button("addAssociationButton").click();
		window.textBox("percentageTextBox_0").enterText("50");

		window.button("addAssociationButton").click();
		window.comboBox("treeComboBox_1").selectItem(0);

		window.button("addParkButton").click();

		Park expectedPark = new Park("1", "Maremma", "Toscana", 50.5, false);
		verify(parkTreeController).addPark(expectedPark, Collections.emptyList());
	}

	@Test
	@GUITest
	public void testParkAddedShouldResetFormRemoveAssociationRowsAndRefreshParkListAndResetErrorLabel() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("50.5");
		window.checkBox("freelyAccessibleCheckBox").check();
		window.button("addAssociationButton").click();

		GuiActionRunner.execute(() -> parkTreeSwingView.showError("some previous error"));
		robot().waitForIdle();

		Park park = new Park("1", "Maremma", "Toscana", 50.5, true);
		GuiActionRunner.execute(() -> parkTreeSwingView.parkAdded(park));
		robot().waitForIdle();

		window.textBox("parkIdTextBox").requireText("");
		window.textBox("parkNameTextBox").requireText("");
		window.textBox("parkRegionTextBox").requireText("");
		window.textBox("parkAreaTextBox").requireText("");
		window.checkBox("freelyAccessibleCheckBox").requireNotSelected();
		window.button("addParkButton").requireDisabled();
		assertThat(parkTreeSwingView.getAssociationsPanel().getComponentCount()).isZero();
		window.label("errorMessageLabel").requireText(" ");
		verify(parkTreeController, never()).parkInfo(any());

		window.tabbedPane("tabbedPane").selectTab("All Parks");
		window.label("parkRowLabel_1");
		window.button("deleteParkButton_1");
	}

	@Test
	@GUITest
	public void testShowErrorShouldNotResetAddParkFieldsOrAssociationRows() {
		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.textBox("parkIdTextBox").enterText("1");
		window.textBox("parkNameTextBox").enterText("Maremma");
		window.textBox("parkRegionTextBox").enterText("Toscana");
		window.textBox("parkAreaTextBox").enterText("50.5");
		window.button("addAssociationButton").click();
		window.textBox("percentageTextBox_0").enterText("40");

		GuiActionRunner
				.execute(() -> parkTreeSwingView.showError("Total tree percentage must be exactly 100% (was 40%)"));
		robot().waitForIdle();

		window.textBox("parkIdTextBox").requireText("1");
		window.textBox("parkNameTextBox").requireText("Maremma");
		window.textBox("parkRegionTextBox").requireText("Toscana");
		window.textBox("parkAreaTextBox").requireText("50.5");
		window.textBox("percentageTextBox_0").requireText("40");
		assertThat(parkTreeSwingView.getAssociationsPanel().getComponentCount()).isEqualTo(1);
		window.label("errorMessageLabel").requireText("Total tree percentage must be exactly 100% (was 40%)");
	}

	@Test
	@GUITest
	public void testShowAllParksShouldPopulateParkRows() {
		Park park1 = new Park("1", "Maremma", "Toscana", 50, true);
		Park park2 = new Park("2", "Cinque Terre", "Liguria", 30, false);

		window.tabbedPane("tabbedPane").selectTab("All Parks");
		GuiActionRunner.execute(() -> parkTreeSwingView.showAllParks(asList(park1, park2)));
		robot().waitForIdle();

		window.label("parkRowLabel_1");
		window.button("deleteParkButton_1");
		window.label("parkRowLabel_2");
		window.button("deleteParkButton_2");
		verify(parkTreeController, never()).parkInfo(any());
	}

	@Test
	@GUITest
	public void testShowParkInfoShouldPopulateAssociationsLabelForThatPark() {
		Tree tree1 = new Tree("1", "Faggio", false, 50);
		GuiActionRunner.execute(() -> parkTreeSwingView.showAllTrees(asList(tree1)));
		robot().waitForIdle();

		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		window.tabbedPane("tabbedPane").selectTab("All Parks");
		GuiActionRunner.execute(() -> parkTreeSwingView.showAllParks(asList(park)));
		robot().waitForIdle();

		ParkTreeAssociation association = new ParkTreeAssociation(new ParkTreeAssociationId("1", "1"), 40);
		GuiActionRunner.execute(() -> parkTreeSwingView.showParkInfo(park, asList(association)));
		robot().waitForIdle();

		String labelText = window.label("parkAssociationsLabel_1").text();
		assertThat(labelText).contains("Faggio").contains("40%");
	}

	@Test
	@GUITest
	public void testDeleteButtonOnParkRowShouldDelegateToControllerDeletePark() {
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		window.tabbedPane("tabbedPane").selectTab("All Parks");
		GuiActionRunner.execute(() -> parkTreeSwingView.showAllParks(asList(park)));
		robot().waitForIdle();

		window.button("deleteParkButton_1").click();

		verify(parkTreeController).deletePark(park);
	}

	@Test
	@GUITest
	public void testParkDeletedShouldRemoveRowFromAllParksList() {
		Park park = new Park("1", "Maremma", "Toscana", 50, true);
		window.tabbedPane("tabbedPane").selectTab("All Parks");
		GuiActionRunner.execute(() -> parkTreeSwingView.showAllParks(asList(park)));
		robot().waitForIdle();

		GuiActionRunner.execute(() -> parkTreeSwingView.parkDeleted("1"));
		robot().waitForIdle();

		assertThat(parkTreeSwingView.getParksListPanel().getComponentCount()).isZero();
		window.label("errorMessageLabel").requireText(" ");
	}

	@Test
	@GUITest
	public void testSelectingAllParksTabShouldDelegateToControllerAllParks() {
		window.tabbedPane("tabbedPane").selectTab("All Parks");
		verify(parkTreeController).allParks();
	}

	@Test
	@GUITest
	public void testShowTreeInfoShouldUpdateInternalTreeCache() {
		Tree tree = new Tree("1", "Faggio", false, 50);
		GuiActionRunner.execute(() -> parkTreeSwingView.showAllTrees(asList(tree)));
		robot().waitForIdle();

		Tree updatedTree = new Tree("1", "Faggio Rosso", false, 55);
		List<ParkTreeAssociation> associations = asList(
				new ParkTreeAssociation(new ParkTreeAssociationId("2", "1"), 40));
		GuiActionRunner.execute(() -> parkTreeSwingView.showTreeInfo(updatedTree, associations));
		robot().waitForIdle();

		window.tabbedPane("tabbedPane").selectTab("Add Park");
		window.button("addAssociationButton").click();

		window.comboBox("treeComboBox_0").selectItem("Faggio Rosso");
	}

}
