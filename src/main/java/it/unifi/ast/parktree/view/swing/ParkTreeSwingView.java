package it.unifi.ast.parktree.view.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import it.unifi.ast.parktree.controller.ParkTreeController;
import it.unifi.ast.parktree.model.Park;
import it.unifi.ast.parktree.model.ParkTreeAssociation;
import it.unifi.ast.parktree.model.ParkTreeAssociationId;
import it.unifi.ast.parktree.model.Tree;
import it.unifi.ast.parktree.view.ParkTreeView;

public class ParkTreeSwingView extends JFrame implements ParkTreeView {

	private static final long serialVersionUID = 1L;

	private static final int ALL_TREES_TAB_INDEX = 2;
	private static final int ALL_PARKS_TAB_INDEX = 3;

	private transient ParkTreeController parkTreeController;

	private final Map<String, Tree> treesById = new LinkedHashMap<>();
	private final Map<String, Park> parksById = new LinkedHashMap<>();

	private final Map<String, JPanel> treeRowById = new LinkedHashMap<>();
	private final Map<String, JPanel> parkRowById = new LinkedHashMap<>();
	private final Map<String, JLabel> parkAssociationsLabelById = new LinkedHashMap<>();

	private final List<AssociationRow> associationRows = new ArrayList<>();
	private int associationRowCounter = 0;

	private JTabbedPane tabbedPane;
	private JLabel errorMessageLabel;

	private JTextField treeIdTextBox;
	private JTextField treeNameTextBox;
	private JCheckBox evergreenCheckBox;
	private JTextField treeLifespanTextBox;
	private JButton btnAddTree;

	private JTextField parkIdTextBox;
	private JTextField parkNameTextBox;
	private JTextField parkRegionTextBox;
	private JTextField parkAreaTextBox;
	private JCheckBox freelyAccessibleCheckBox;
	private JPanel associationsPanel;
	private JButton btnAddPark;

	private JPanel treesListPanel;
	private JPanel parksListPanel;

	public ParkTreeSwingView() {
		setTitle("Park Tree View");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 600);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		tabbedPane = new JTabbedPane();
		tabbedPane.setName("tabbedPane");
		tabbedPane.addTab("Add Tree", buildAddTreePanel());
		tabbedPane.addTab("Add Park", buildAddParkPanel());
		tabbedPane.addTab("All Trees", buildAllTreesPanel());
		tabbedPane.addTab("All Parks", buildAllParksPanel());
		tabbedPane.addChangeListener(e -> onTabChanged());
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		errorMessageLabel = new JLabel(" ");
		errorMessageLabel.setName("errorMessageLabel");
		errorMessageLabel.setForeground(Color.RED);
		contentPane.add(errorMessageLabel, BorderLayout.SOUTH);
	}

	public void setParkTreeController(ParkTreeController parkTreeController) {
		this.parkTreeController = parkTreeController;
	}

	JPanel getAssociationsPanel() {
		return associationsPanel;
	}

	JPanel getTreesListPanel() {
		return treesListPanel;
	}

	JPanel getParksListPanel() {
		return parksListPanel;
	}

	private void onTabChanged() {
		if (parkTreeController == null) {
			return;
		}
		int selectedIndex = tabbedPane.getSelectedIndex();
		if (selectedIndex == ALL_TREES_TAB_INDEX) {
			parkTreeController.allTrees();
		} else if (selectedIndex == ALL_PARKS_TAB_INDEX) {
			parkTreeController.allParks();
		}
	}

	private JPanel buildAddTreePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;

		treeIdTextBox = new JTextField(15);
		treeIdTextBox.setName("treeIdTextBox");
		addFormRow(panel, gbc, 0, "id", treeIdTextBox);

		treeNameTextBox = new JTextField(15);
		treeNameTextBox.setName("treeNameTextBox");
		addFormRow(panel, gbc, 1, "name", treeNameTextBox);

		evergreenCheckBox = new JCheckBox("Evergreen");
		evergreenCheckBox.setName("evergreenCheckBox");
		gbc.gridx = 1;
		gbc.gridy = 2;
		panel.add(evergreenCheckBox, gbc);

		treeLifespanTextBox = new JTextField(15);
		treeLifespanTextBox.setName("treeLifespanTextBox");
		addFormRow(panel, gbc, 3, "lifespan (years)", treeLifespanTextBox);

		btnAddTree = new JButton("Add Tree");
		btnAddTree.setName("addTreeButton");
		btnAddTree.setEnabled(false);
		gbc.gridx = 1;
		gbc.gridy = 4;
		panel.add(btnAddTree, gbc);

		KeyAdapter addTreeEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAddTree.setEnabled(isValidTreeForm());
			}
		};
		treeIdTextBox.addKeyListener(addTreeEnabler);
		treeNameTextBox.addKeyListener(addTreeEnabler);
		treeLifespanTextBox.addKeyListener(addTreeEnabler);

		btnAddTree.addActionListener(e -> onAddTreeClicked());

		return panel;
	}

	private boolean isValidTreeForm() {
		return !treeIdTextBox.getText().trim().isEmpty() && !treeNameTextBox.getText().trim().isEmpty()
				&& isValidNonNegativeInt(treeLifespanTextBox.getText());
	}

	private void onAddTreeClicked() {
		Tree tree = new Tree(treeIdTextBox.getText().trim(), treeNameTextBox.getText().trim(),
				evergreenCheckBox.isSelected(), Integer.parseInt(treeLifespanTextBox.getText().trim()));
		parkTreeController.addTree(tree);
	}

	private void resetAddTreeForm() {
		treeIdTextBox.setText("");
		treeNameTextBox.setText("");
		evergreenCheckBox.setSelected(false);
		treeLifespanTextBox.setText("");
		btnAddTree.setEnabled(false);
	}

	private JPanel buildAddParkPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel formPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.anchor = GridBagConstraints.WEST;

		parkIdTextBox = new JTextField(15);
		parkIdTextBox.setName("parkIdTextBox");
		addFormRow(formPanel, gbc, 0, "id", parkIdTextBox);

		parkNameTextBox = new JTextField(15);
		parkNameTextBox.setName("parkNameTextBox");
		addFormRow(formPanel, gbc, 1, "name", parkNameTextBox);

		parkRegionTextBox = new JTextField(15);
		parkRegionTextBox.setName("parkRegionTextBox");
		addFormRow(formPanel, gbc, 2, "region", parkRegionTextBox);

		parkAreaTextBox = new JTextField(15);
		parkAreaTextBox.setName("parkAreaTextBox");
		addFormRow(formPanel, gbc, 3, "area (square meters)", parkAreaTextBox);

		freelyAccessibleCheckBox = new JCheckBox("Freely accessible");
		freelyAccessibleCheckBox.setName("freelyAccessibleCheckBox");
		gbc.gridx = 1;
		gbc.gridy = 4;
		formPanel.add(freelyAccessibleCheckBox, gbc);

		JButton addAssociationButton = new JButton("Add Tree Association");
		addAssociationButton.setName("addAssociationButton");
		gbc.gridx = 1;
		gbc.gridy = 5;
		formPanel.add(addAssociationButton, gbc);

		associationsPanel = new JPanel();
		associationsPanel.setName("associationsPanel");
		associationsPanel.setLayout(new BoxLayout(associationsPanel, BoxLayout.Y_AXIS));

		btnAddPark = new JButton("Add Park");
		btnAddPark.setName("addParkButton");
		btnAddPark.setEnabled(false);

		KeyAdapter addParkEnabler = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				btnAddPark.setEnabled(isValidParkForm());
			}
		};
		parkIdTextBox.addKeyListener(addParkEnabler);
		parkNameTextBox.addKeyListener(addParkEnabler);
		parkRegionTextBox.addKeyListener(addParkEnabler);
		parkAreaTextBox.addKeyListener(addParkEnabler);

		addAssociationButton.addActionListener(e -> addAssociationRow());
		btnAddPark.addActionListener(e -> onAddParkClicked());

		JPanel southPanel = new JPanel();
		southPanel.add(btnAddPark);

		panel.add(formPanel, BorderLayout.NORTH);
		panel.add(new JScrollPane(associationsPanel), BorderLayout.CENTER);
		panel.add(southPanel, BorderLayout.SOUTH);

		return panel;
	}

	private boolean isValidParkForm() {
		return !parkIdTextBox.getText().trim().isEmpty() && !parkNameTextBox.getText().trim().isEmpty()
				&& !parkRegionTextBox.getText().trim().isEmpty() && isValidPositiveDouble(parkAreaTextBox.getText());
	}

	private void addAssociationRow() {
		int rowIndex = associationRowCounter++;

		JComboBox<Tree> treeComboBox = new JComboBox<>(treesById.values().toArray(new Tree[0]));
		treeComboBox.setName("treeComboBox_" + rowIndex);
		treeComboBox.setSelectedItem(null);
		treeComboBox.setRenderer(new TreeListCellRenderer());

		JTextField percentageTextBox = new JTextField(5);
		percentageTextBox.setName("percentageTextBox_" + rowIndex);

		JButton removeButton = new JButton("Remove");
		removeButton.setName("removeAssociationButton_" + rowIndex);

		JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		rowPanel.add(new JLabel("tree"));
		rowPanel.add(treeComboBox);
		rowPanel.add(new JLabel("percentage (%)"));
		rowPanel.add(percentageTextBox);
		rowPanel.add(removeButton);

		AssociationRow row = new AssociationRow(rowPanel, treeComboBox, percentageTextBox);
		removeButton.addActionListener(e -> removeAssociationRow(row));

		associationRows.add(row);
		associationsPanel.add(rowPanel);
		associationsPanel.revalidate();
		associationsPanel.repaint();
	}

	private void removeAssociationRow(AssociationRow row) {
		associationRows.remove(row);
		associationsPanel.remove(row.rowPanel);
		associationsPanel.revalidate();
		associationsPanel.repaint();
	}

	private void clearAssociationRows() {
		associationRows.clear();
		associationsPanel.removeAll();
		associationsPanel.revalidate();
		associationsPanel.repaint();
	}

	private void onAddParkClicked() {
		Park park = new Park(parkIdTextBox.getText().trim(), parkNameTextBox.getText().trim(),
				parkRegionTextBox.getText().trim(), Double.parseDouble(parkAreaTextBox.getText().trim()),
				freelyAccessibleCheckBox.isSelected());

		List<ParkTreeAssociation> associations = new ArrayList<>();
		for (AssociationRow row : associationRows) {
			Tree selectedTree = (Tree) row.treeComboBox.getSelectedItem();
			if (selectedTree == null || !isValidNonNegativeInt(row.percentageTextBox.getText())) {
				continue;
			}
			int percentage = Integer.parseInt(row.percentageTextBox.getText().trim());
			ParkTreeAssociationId associationId = new ParkTreeAssociationId(park.getId(), selectedTree.getId());
			associations.add(new ParkTreeAssociation(associationId, percentage));
		}

		parkTreeController.addPark(park, associations);
	}

	private void resetAddParkForm() {
		parkIdTextBox.setText("");
		parkNameTextBox.setText("");
		parkRegionTextBox.setText("");
		parkAreaTextBox.setText("");
		freelyAccessibleCheckBox.setSelected(false);
		clearAssociationRows();
		btnAddPark.setEnabled(false);
	}

	private JPanel buildAllTreesPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		treesListPanel = new JPanel();
		treesListPanel.setName("treesListPanel");
		treesListPanel.setLayout(new BoxLayout(treesListPanel, BoxLayout.Y_AXIS));
		panel.add(new JScrollPane(treesListPanel), BorderLayout.CENTER);
		return panel;
	}

	private JPanel createTreeRow(Tree tree) {
		JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JLabel label = new JLabel(treeRowText(tree));
		label.setName("treeRowLabel_" + tree.getId());

		JButton deleteButton = new JButton("Delete");
		deleteButton.setName("deleteTreeButton_" + tree.getId());
		deleteButton.addActionListener(e -> parkTreeController.deleteTree(treesById.get(tree.getId())));

		rowPanel.add(label);
		rowPanel.add(deleteButton);
		return rowPanel;
	}

	private String treeRowText(Tree tree) {
		return tree.getId() + " - " + tree.getName() + " - " + (tree.isEvergreen() ? "Evergreen" : "Deciduous") + " - "
				+ tree.getMediumLifespan() + " years";
	}

	private void addOrUpdateTreeRow(Tree tree) {
		treesById.put(tree.getId(), tree);
		JPanel existingRow = treeRowById.remove(tree.getId());
		if (existingRow != null) {
			treesListPanel.remove(existingRow);
		}
		JPanel row = createTreeRow(tree);
		treeRowById.put(tree.getId(), row);
		treesListPanel.add(row);
		treesListPanel.revalidate();
		treesListPanel.repaint();
	}

	private JPanel buildAllParksPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		parksListPanel = new JPanel();
		parksListPanel.setName("parksListPanel");
		parksListPanel.setLayout(new BoxLayout(parksListPanel, BoxLayout.Y_AXIS));
		panel.add(new JScrollPane(parksListPanel), BorderLayout.CENTER);
		return panel;
	}

	private JPanel createParkRow(Park park) {
		JPanel rowPanel = new JPanel();
		rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.Y_AXIS));

		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel(parkRowText(park));
		label.setName("parkRowLabel_" + park.getId());

		JButton deleteButton = new JButton("Delete");
		deleteButton.setName("deleteParkButton_" + park.getId());
		deleteButton.addActionListener(e -> parkTreeController.deletePark(parksById.get(park.getId())));

		headerPanel.add(label);
		headerPanel.add(deleteButton);

		JLabel associationsLabel = new JLabel(" ");
		associationsLabel.setName("parkAssociationsLabel_" + park.getId());
		parkAssociationsLabelById.put(park.getId(), associationsLabel);

		rowPanel.add(headerPanel);
		rowPanel.add(associationsLabel);

		return rowPanel;
	}

	private String parkRowText(Park park) {
		return park.getId() + " - " + park.getName() + " - " + park.getRegion() + " - " + park.getArea() + " sqm - "
				+ (park.isFreeAccess() ? "Freely accessible" : "Not freely accessible");
	}

	private void addOrUpdateParkRow(Park park) {
		parksById.put(park.getId(), park);
		JPanel existingRow = parkRowById.remove(park.getId());
		if (existingRow != null) {
			parksListPanel.remove(existingRow);
			parkAssociationsLabelById.remove(park.getId());
		}
		JPanel row = createParkRow(park);
		parkRowById.put(park.getId(), row);
		parksListPanel.add(row);
		parksListPanel.revalidate();
		parksListPanel.repaint();
	}

	@Override
	public void showAllTrees(List<Tree> trees) {
		SwingUtilities.invokeLater(() -> {
			treesById.clear();
			treeRowById.clear();
			treesListPanel.removeAll();
			for (Tree tree : trees) {
				treesById.put(tree.getId(), tree);
				JPanel row = createTreeRow(tree);
				treeRowById.put(tree.getId(), row);
				treesListPanel.add(row);
			}
			treesListPanel.revalidate();
			treesListPanel.repaint();
		});
	}

	@Override
	public void showAllParks(List<Park> parks) {
		SwingUtilities.invokeLater(() -> {
			parksById.clear();
			parkRowById.clear();
			parkAssociationsLabelById.clear();
			parksListPanel.removeAll();
			for (Park park : parks) {
				parksById.put(park.getId(), park);
				JPanel row = createParkRow(park);
				parkRowById.put(park.getId(), row);
				parksListPanel.add(row);
			}
			parksListPanel.revalidate();
			parksListPanel.repaint();
		});
	}

	@Override
	public void showError(String string) {
		SwingUtilities.invokeLater(() -> errorMessageLabel.setText(string));
	}

	@Override
	public void parkAdded(Park park) {
		SwingUtilities.invokeLater(() -> {
			resetAddParkForm();
			addOrUpdateParkRow(park);
			resetErrorLabel();
		});
	}

	@Override
	public void parkDeleted(String string) {
		SwingUtilities.invokeLater(() -> {
			JPanel row = parkRowById.remove(string);
			if (row != null) {
				parksListPanel.remove(row);
				parksListPanel.revalidate();
				parksListPanel.repaint();
			}
			parksById.remove(string);
			parkAssociationsLabelById.remove(string);
			resetErrorLabel();
		});
	}

	@Override
	public void treeAdded(Tree tree) {
		SwingUtilities.invokeLater(() -> {
			resetAddTreeForm();
			addOrUpdateTreeRow(tree);
			resetErrorLabel();
		});
	}

	@Override
	public void treeDeleted(String string) {
		SwingUtilities.invokeLater(() -> {
			JPanel row = treeRowById.remove(string);
			if (row != null) {
				treesListPanel.remove(row);
				treesListPanel.revalidate();
				treesListPanel.repaint();
			}
			treesById.remove(string);
			resetErrorLabel();
		});
	}

	@Override
	public void showParkInfo(Park park, List<ParkTreeAssociation> associations) {
		SwingUtilities.invokeLater(() -> {
			JLabel label = parkAssociationsLabelById.get(park.getId());
			if (label == null) {
				return;
			}
			if (associations == null || associations.isEmpty()) {
				label.setText(" ");
				return;
			}
			StringBuilder text = new StringBuilder("<html>");
			for (ParkTreeAssociation association : associations) {
				Tree tree = treesById.get(association.getId().getTreeId());
				String treeName = tree != null ? tree.getName() : association.getId().getTreeId();
				text.append(treeName).append(" - ").append(association.getPercentage()).append("%<br>");
			}
			text.append("</html>");
			label.setText(text.toString());
		});
	}

	@Override
	public void showTreeInfo(Tree tree, List<ParkTreeAssociation> associations) {
		SwingUtilities.invokeLater(() -> treesById.put(tree.getId(), tree));
	}

	private void resetErrorLabel() {
		errorMessageLabel.setText(" ");
	}

	private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
		gbc.gridx = 0;
		gbc.gridy = row;
		panel.add(new JLabel(labelText), gbc);
		gbc.gridx = 1;
		gbc.gridy = row;
		panel.add(field, gbc);
	}

	private boolean isValidNonNegativeInt(String text) {
		if (text == null) {
			return false;
		}
		String trimmed = text.trim();
		if (trimmed.isEmpty()) {
			return false;
		}
		try {
			return Integer.parseInt(trimmed) >= 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private boolean isValidPositiveDouble(String text) {
		if (text == null) {
			return false;
		}
		String trimmed = text.trim();
		if (trimmed.isEmpty()) {
			return false;
		}
		try {
			return Double.parseDouble(trimmed) > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static class AssociationRow {
		private final JPanel rowPanel;
		private final JComboBox<Tree> treeComboBox;
		private final JTextField percentageTextBox;

		AssociationRow(JPanel rowPanel, JComboBox<Tree> treeComboBox, JTextField percentageTextBox) {
			this.rowPanel = rowPanel;
			this.treeComboBox = treeComboBox;
			this.percentageTextBox = percentageTextBox;
		}
	}

	private static class TreeListCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			String text = value instanceof Tree ? ((Tree) value).getName() : "";
			return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
		}
	}

}
