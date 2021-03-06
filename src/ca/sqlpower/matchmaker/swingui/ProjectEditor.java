/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.MutableComboBoxModel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.validation.ProjectNameValidator;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.jdbcwrapper.DatabaseMetaDataDecorator;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLIndex;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.validation.AlwaysOKValidator;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The MatchEditor is the GUI for editing all aspects of a {@link Project} instance.
 */
public class ProjectEditor implements MatchMakerEditorPane {

	private static final Logger logger = Logger.getLogger(ProjectEditor.class);

	/**
	 * The collection of combo boxes for choosing the project source table (or view).
	 */
	private SQLObjectChooser sourceChooser;
	
	/**
	 * The collection of combo boxes for choosing the project result table.
	 */
	private SQLObjectChooser resultChooser;

	/**
	 * The panel that holds this editor's GUI.
	 */
	private final JPanel panel;

	private StatusComponent status = new StatusComponent();
    private JTextField projectName = new JTextField();
    private JComboBox folderComboBox = new JComboBox();
    private JComboBox indexComboBox = new JComboBox();
    private JTextArea desc = new JTextArea();
    private JTextField projectType = new JTextField();
    private JTextField resultTableName = new JTextField();
    
    private FilterComponents filterPanel;

    private final MatchMakerSwingSession swingSession;

    /**
     * The project that this editor is editing.  If you want to edit a different match,
     * create a new ProjectEditor.
     */
	private final Project project;
	private final PlFolder folder;
	private FormValidationHandler handler;

	
	/**
	 * Construct a ProjectEditor; for a project that is not new, we create a backup for it,
	 * and give it the name of the old one, when we save it, we will remove
	 * the backup from the folder, and insert the new one.
	 * @param swingSession  -- a MatchMakerSession
	 * @param project the project Object to be edited
	 * @param folder the project's parent folder
	 */
    public ProjectEditor(final MatchMakerSwingSession swingSession, Project project, PlFolder folder, Action cancelAction) throws SQLObjectException {
        if (project == null) throw new IllegalArgumentException("You can't edit a null project");
        if (folder == null) throw new IllegalArgumentException("Project must be in a folder");
        
        this.swingSession = swingSession;
        this.project = project;
        this.folder = folder;
        this.cancelAction = cancelAction;
        handler = new FormValidationHandler(status, true);
        handler.setValidatedAction(saveAction);
        panel = buildUI();
        setDefaultSelections();
        addListeners();
        addValidators();
        
        handler.resetHasValidated(); // avoid false hits when newly created

        if ( project.getParent() != null ) {
        	sourceChooser.getDataSourceComboBox().setEnabled(false);
        	sourceChooser.getCatalogComboBox().setEnabled(false);
        	sourceChooser.getSchemaComboBox().setEnabled(false);
        	sourceChooser.getTableComboBox().setEnabled(false);
        }
    }

    private void addListeners() {
    	//This is only good if the result choosers datasource's combo box is invisible.
    	sourceChooser.getDataSourceComboBox().addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				resultChooser.getDataSourceComboBox().getModel().setSelectedItem(sourceChooser.getDataSourceComboBox().getSelectedItem());
			}
    	});
    	
    	// listen to the table change
        sourceChooser.getTableComboBox().addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent e) {
        		SQLTable sourceTable = (SQLTable)(sourceChooser.getTableComboBox().getSelectedItem());
				refreshIndexComboBoxAndAction(sourceTable);
				if (sourceTable != null) {
					try {
						for(SQLColumn c : sourceTable.getColumns()) {
							logger.debug("old SQL type: " + c.getShortDisplayName());
							c.setType(swingSession.getSQLType(c.getType()));
							logger.debug("new SQL type: " + c.getShortDisplayName());
						}
					} catch (SQLObjectException evt) {
						throw new RuntimeException(evt);
					}
					String trimmedResultTableName;
					if (project.getType() == ProjectMode.FIND_DUPES) {
						trimmedResultTableName = sourceTable.getName() + "_match_pool";
					} else if (project.getType() == ProjectMode.ADDRESS_CORRECTION) {
						trimmedResultTableName = sourceTable.getName() + "_address_pool";
					} else {
						trimmedResultTableName = sourceTable.getName() + "_result";
					}
					resultTableName.setText(trimmedResultTableName);
				}
				filterPanel.setTable(sourceTable);
        	}
        });

    }
    
    private void addValidators() {
    	Validator v = new ProjectNameValidator(swingSession,project);
        handler.addValidateObject(projectName,v);

        Validator v2 = new ProjectSourceTableValidator(Collections.singletonList(saveAction));
        handler.addValidateObject(sourceChooser.getTableComboBox(),v2);

        Validator v2a = new ProjectSourceTableIndexValidator();
        handler.addValidateObject(indexComboBox,v2a);

        if (project.getType() != ProjectMode.CLEANSE) { 
    		Validator v3 = new ProjectResultCatalogSchemaValidator("Result "+
    				resultChooser.getCatalogTerm().getText());
    		handler.addValidateObject(resultChooser.getCatalogComboBox(),v3);
    	
    		Validator v4 = new ProjectResultCatalogSchemaValidator("Result "+
    				resultChooser.getSchemaTerm().getText());
    		handler.addValidateObject(resultChooser.getSchemaComboBox(),v4);
        	
        	Validator v5 = new ProjectResultTableNameValidator();
        	handler.addValidateObject(resultTableName,v5);
        }
        	
        Validator v6 = new AlwaysOKValidator();
        handler.addValidateObject(desc, v6);
        handler.addValidateObject(filterPanel.getFilterTextArea(), v6);
        
        handler.addValidateObject(sourceChooser.getDataSourceComboBox(), v6);
        handler.addValidateObject(resultChooser.getDataSourceComboBox(), v6);
    }
    
    private Action showConnectionManagerAction = new AbstractAction("Manage Connections...") {
        public void actionPerformed(ActionEvent e) {
            swingSession.getContext().showDatabaseConnectionManager(swingSession.getFrame());
        }
    };
    
    /**
     * Saves the current project (which is referenced in the plMatch member variable of this editor instance).
     * If there is no current plMatch, a new one will be created and its properties will be set just like
     * they would if one had existed.  In either case, this action will then use Hibernate to save the
     * project object back to the database (but it should use the MatchHome interface instead).
     */
	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(final ActionEvent e) {
            try {
                boolean ok = applyChanges();
                if (!ok) { 
                	JOptionPane.showMessageDialog(swingSession.getFrame(),
                			"Project Not Saved",
                			"Not Saved",JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                MMSUtils.showExceptionDialog(swingSession.getFrame(),
                		"Project Not Saved", ex);
            }
		}
	};
	
	private Action cancelAction;

	private Window getParentWindow() {
	    return SPSUtils.getWindowInHierarchy(panel);
	}

    /**
     * Returns the parent (owning) frame of this project editor.  If the owner
     * isn't a frame (it might be a dialog or AWT Window) then null is returned.
     * You should always use {@link #getParentWindow()} in preference to
     * this method unless you really really need a JFrame.
     *
     * @return the parent JFrame of this project editor's panel, or null if
     * the owner is not a JFrame.
     */
    private JFrame getParentFrame() {
        Window owner = getParentWindow();
        if (owner instanceof JFrame) return (JFrame) owner;
        else return null;
    }
			
	private Action createIndexAction = new AbstractAction("Pick Columns..."){
		public void actionPerformed(ActionEvent e) {
			SQLTable sourceTable = (SQLTable)sourceChooser.getTableComboBox().getSelectedItem();
			if (sourceTable == null) {
				JOptionPane.showMessageDialog(panel,
						"You have to select a source table and save before picking columns" );
				return;
			}
			try {
				for(SQLColumn c : sourceTable.getColumns()) {
					c.setType(swingSession.getSQLType(c.getType()));
				}
			} catch (SQLObjectException evt) {
				throw new RuntimeException(evt);
			}
			try {
				MatchMakerIndexBuilder indexBuilder = new MatchMakerIndexBuilder(sourceTable, (MutableComboBoxModel)indexComboBox.getModel(),swingSession);
				JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
						indexBuilder,
						getParentWindow(),
						"Choose the index",
						"OK");
				d.pack();
				d.setLocationRelativeTo(swingSession.getFrame());
				d.setVisible(true);
				
			} catch (Exception ex) {
				ex.printStackTrace();
				SPSUtils.showExceptionDialogNoReport(panel, "An exception occured while picking columns", ex);
			}
		}
	};

    private JPanel buildUI() {

    	projectName.setName("Project Name");
		sourceChooser = new SQLObjectChooser(swingSession, swingSession.getFrame());
        resultChooser = new SQLObjectChooser(swingSession, swingSession.getFrame());
        sourceChooser.getTableComboBox().setName("Source Table");
        resultChooser.getCatalogComboBox().setName("Result "+
        		resultChooser.getCatalogTerm().getText());
        resultChooser.getSchemaComboBox().setName("Result "+
        		resultChooser.getSchemaTerm().getText());
        resultTableName.setName("Result Table");

        sourceChooser.getCatalogComboBox().setRenderer(new SQLObjectComboBoxCellRenderer());
        sourceChooser.getSchemaComboBox().setRenderer(new SQLObjectComboBoxCellRenderer());
        sourceChooser.getTableComboBox().setRenderer(new SQLObjectComboBoxCellRenderer());
        resultChooser.getCatalogComboBox().setRenderer(new SQLObjectComboBoxCellRenderer());
        resultChooser.getSchemaComboBox().setRenderer(new SQLObjectComboBoxCellRenderer());

        filterPanel = new FilterComponents(swingSession.getFrame());

    	JButton saveProject = new JButton(saveAction);
    	JButton cancelProject = new JButton(cancelAction);
        JButton createIndexButton = new JButton(createIndexAction );

    	FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:min(pref;"+new JComboBox().getMinimumSize().width+"px):grow, 4dlu,pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,40dlu,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref, 4dlu,32dlu,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,10dlu"); // rows

		PanelBuilder pb;

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();
		int row = 2;
		pb.add(status, cc.xy(4,row));
		row += 2;
		pb.add(new JLabel("Project Name:"), cc.xy(2,row,"r,c"));
		pb.add(projectName, cc.xy(4,row));
		row += 2;
		pb.add(new JLabel("Folder:"), cc.xy(2,row,"r,c"));
		pb.add(folderComboBox, cc.xy(4,row));
		row += 2;
        desc.setWrapStyleWord(true);
        desc.setLineWrap(true);
		pb.add(new JLabel("Description:"), cc.xy(2,row,"r,t"));
		pb.add(new JScrollPane(desc), cc.xy(4,row,"f,f"));
		row += 2;
		pb.add(new JLabel("Type:"), cc.xy(2,row,"r,c"));
		pb.add(projectType, cc.xy(4,row));
        projectType.setEditable(false);
        row+=2;
        pb.add(new JLabel("Data Source:"),cc.xy(2,row,"r,c"));
        pb.add(sourceChooser.getDataSourceComboBox(),cc.xy(4, row));
        pb.add(new JButton(showConnectionManagerAction), cc.xy(6, row));
		row+=2;
		pb.addTitle("Source Table", cc.xy(2, row));
		row+=2;
		pb.add(sourceChooser.getCatalogTerm(), cc.xy(2,row,"r,c"));
		pb.add(sourceChooser.getCatalogComboBox(), cc.xy(4,row));
		row+=2;
		pb.add(sourceChooser.getSchemaTerm(), cc.xy(2,row,"r,c"));
		pb.add(sourceChooser.getSchemaComboBox(), cc.xy(4,row));
		row+=2;
		pb.add(new JLabel("Table Name:"), cc.xy(2,row,"r,c"));
		pb.add(sourceChooser.getTableComboBox(), cc.xy(4,row));
		row+=2;
		pb.add(new JLabel("Unique Index:"), cc.xy(2,row,"r,t"));
		pb.add(indexComboBox, cc.xy(4,row,"f,f"));
		pb.add(createIndexButton, cc.xy(6,row,"f,f"));
		row+=2;
		pb.add(new JLabel("Filter:"), cc.xy(2,row,"r,t"));
		pb.add(new JScrollPane(filterPanel.getFilterTextArea()), cc.xy(4,row,"f,f"));
        pb.add(filterPanel.getEditButton(), cc.xy(6,row));
		row+=2;
		if (project.getType() != ProjectMode.CLEANSE) {
			pb.addTitle("Output Table", cc.xy(2, row));
			row+=2;
			pb.add(resultChooser.getCatalogTerm(), cc.xy(2,row,"r,c"));
			pb.add(resultChooser.getCatalogComboBox(), cc.xy(4,row));
			row+=2;
			pb.add(resultChooser.getSchemaTerm(), cc.xy(2,row,"r,c"));
			pb.add(resultChooser.getSchemaComboBox(), cc.xy(4,row));
			row+=2;
			pb.add(new JLabel("Table Name:"), cc.xy(2,row,"r,c"));
			pb.add(resultTableName, cc.xy(4,row));
			row+=2;
		}
		
        final List<PlFolder> folders = swingSession.getCurrentFolderParent().getChildren(PlFolder.class);
        folderComboBox.setModel(new DefaultComboBoxModel(folders.toArray()));
        folderComboBox.setRenderer(new MatchMakerObjectComboBoxCellRenderer());

		// We don't want the save button to take up the whole column width
		// so we wrap it in a JPanel with a FlowLayout. If there is a better
		// way, please fix this.
		JPanel savePanel = new JPanel(new FlowLayout());
		savePanel.add(saveProject);
		savePanel.add(cancelProject);
		pb.add(savePanel, cc.xy(4, row));

		return pb.getPanel();
    }


    private void setDefaultSelections() throws SQLObjectException {


        folderComboBox.setSelectedItem(folder);
        projectName.setText(project.getName());
        desc.setText(project.getMungeSettings().getDescription());
        projectType.setText(project.getType().toString());
        filterPanel.getFilterTextArea().setText(project.getFilter());

        //sets the sourceChooser defaults
        if ( project.getSourceTable() != null ) {
        	SQLTable sourceTable = project.getSourceTable();
        	filterPanel.setTable(sourceTable);
        	SQLCatalog cat = sourceTable.getCatalog();
        	SQLSchema sch = sourceTable.getSchema();
        	
        	if (project.getSourceTableSPDatasource().length() == 0) {
        		sourceChooser.getDataSourceComboBox().getModel().setSelectedItem(null);
        	} else {
        		for (int x = 0; x < sourceChooser.getDataSourceComboBox().getModel().getSize(); x++) {
        			SPDataSource curr =(SPDataSource) sourceChooser.getDataSourceComboBox().getModel().getElementAt(x);
        			if (curr != null && curr.getName().equals(project.getSourceTableSPDatasource())) {
        				sourceChooser.getDataSourceComboBox().setSelectedItem(curr);
        				break;
        			}
        		}
        	}
        	
        	sourceChooser.getCatalogComboBox().getModel().setSelectedItem(cat);
        	sourceChooser.getSchemaComboBox().getModel().setSelectedItem(sch);
        	sourceChooser.getTableComboBox().getModel().setSelectedItem(sourceTable);
    	}

        refreshIndexComboBoxAndAction(project.getSourceTable());
        
        if (project.getSourceTable() != null) {
        	int sourceTableIndexIndex = 0;
			List<SQLIndex> uniqueIndices = project.getSourceTable()
					.getUniqueIndices();
			SQLIndex sourceTableIndex = project.getSourceTableIndex();
			for (int i = 0; i < uniqueIndices.size(); i++) {
				SQLIndex index = uniqueIndices.get(i);
				if (index.getName().equals(sourceTableIndex.getName())) {
					sourceTableIndexIndex = i;
					break;
				}
			}
			indexComboBox.setSelectedIndex(sourceTableIndexIndex);
		}
        
        //sets the resultChooser defaults
        resultChooser.getDataSourceComboBox().setSelectedItem(null);
    	SQLTable resultTable = project.getResultTable();
    	logger.debug("result table: " + resultTable);
    	if ( resultTable != null ) {
    		SQLCatalog cat = resultTable.getCatalog();
    		resultChooser.getDataSourceComboBox().getModel().setSelectedItem(resultTable.getParentDatabase().getDataSource());
    		
    		if ( cat != null ) {
    			//this sets the selected item in the model because it refused to work 
    			//when just the combobox set told to with new tables with the schema and it may cause 
    			// problems when on platforms with catalogs.
    			resultChooser.getCatalogComboBox().getModel().setSelectedItem(cat);
    		}
    		
    		SQLSchema sch = resultTable.getSchema();
    		
    		if ( sch != null ) {
    			//this sets the selected item in the model because it refused to work 
    			//when just the combobox set told to with new tables. I have no idea why.
				resultChooser.getSchemaComboBox().getModel().setSelectedItem(sch);
    		} 
    		
    		resultTableName.setText(project.getResultTable().getName());
    	}
    }

    /**
     * refresh combo box item
     * @param newTable    the sqlTable contains unique index
     */
	private void refreshIndexComboBoxAndAction(SQLTable newTable) {
		indexComboBox.removeAllItems();
		if ( newTable != null ) {
			try {
				List<SQLIndex> uniqueIndices = newTable.getUniqueIndices();
				SQLIndex sourceTableIndex = project.getSourceTableIndex();
				
				if (sourceTableIndex != null && !sourceTableIndex.isEmpty()) {
					boolean contains = false;
					for (SQLIndex index : uniqueIndices) {
						if (index.getName().equals(sourceTableIndex.getName())) {
							contains = true;
							break;
						}
					}
					if (!contains)
						indexComboBox.addItem(sourceTableIndex);
				}
				
				for ( SQLIndex index : uniqueIndices ) {
					indexComboBox.addItem(index);
				}
			} catch (SQLObjectException e) {
				throw new RuntimeException(e);
			}
			if ( indexComboBox.getItemCount() > 0 ) {
				indexComboBox.setSelectedIndex(0);
			}
			createIndexAction.setEnabled(true);
		} else {
			createIndexAction.setEnabled(false);
		}
	}

	public JPanel getPanel() {
		return panel;
	}

    /**
     * Copies all the values from the GUI components into the PlMatch
     * object this component is editing, then persists it to the database.
     * @return true if save OK
     * @throws SQLObjectRuntimeException if we cannot set the result table on a project
     */
    public boolean applyChanges() {
    	List<String> fail = handler.getFailResults();

    	if ( fail.size() > 0 ) {
    		StringBuffer failMessage = new StringBuffer();
    		for ( String f : fail ) {
    			failMessage.append(f).append("\n");
    		}
    		JOptionPane.showMessageDialog(swingSession.getFrame(),
    				"You have to fix these errors before saving:\n"+failMessage.toString(),
    				"Project error",
    				JOptionPane.ERROR_MESSAGE);
    		return false;
    	}

        //sets the sourceTable
        SQLTable sourceTable = (SQLTable) sourceChooser.getTableComboBox().getSelectedItem();
        SQLIndex sourceTableIndex = (SQLIndex) indexComboBox.getSelectedItem();
        if (sourceTable == null || sourceTableIndex == null) {
        	throw new IllegalStateException("Source table/index not found.");
        }
		try {
			for(SQLColumn c : sourceTable.getColumns()) {
				c.setType(swingSession.getSQLType(c.getType()));
			}
		} catch (SQLObjectException evt) {
			throw new RuntimeException(evt);
		}
        project.setSourceTable(sourceTable);
        project.setSourceTableIndex(sourceTableIndex);
        project.setFilter(filterPanel.getFilterTextArea().getText());

        //sets the project name, id and desc
        final String pName = projectName.getText().trim();
        project.getMungeSettings().setDescription(desc.getText());
        String id = projectName.getText();
        if ( pName == null || pName.length() == 0 ) {
        	StringBuffer s = new StringBuffer();
        	s.append("PROJECT_");
			if (sourceTable.getCatalogName() != null &&
        			sourceTable.getCatalogName().length() > 0 ) {
        		s.append(sourceTable.getCatalogName()).append("_");
        	}
			if (sourceTable.getSchemaName() != null &&
        			sourceTable.getSchemaName().length() > 0 ) {
        		s.append(sourceTable.getSchemaName()).append("_");
        	}
			s.append(sourceTable.getName());
        	id = s.toString();
        	projectName.setText(id);
        }
		if (!id.equals(project.getName())) {
        	if (!swingSession.isThisProjectNameAcceptable(id)) {
        		JOptionPane.showMessageDialog(getPanel(),
        				"<html>Project name \"" + projectName.getText() +
        					"\" does not exist or is invalid.\n" +
        					"The project has not been saved",
        				"Project name invalid",
        				JOptionPane.ERROR_MESSAGE);
        		return false;
        	}
        	project.setName(id);
        }

        if (project.getType() != ProjectMode.CLEANSE) {
        	//sets the result table
	
	        project.setResultTableSPDatasource(((SPDataSource)(resultChooser.getDataSourceComboBox().getSelectedItem())).getName());
	        
	        if(resultChooser.getCatalogComboBox().getSelectedItem() != null) {
	        	project.setResultTableCatalog( ((SQLCatalog) resultChooser.getCatalogComboBox().getSelectedItem()).getName());
	        }
	        if(resultChooser.getSchemaComboBox().getSelectedItem() != null) {
	        	project.setResultTableSchema( ((SQLSchema) resultChooser.getSchemaComboBox().getSelectedItem()).getName());
	        }
	        project.setResultTableName(resultTableName.getText());
	        
	        logger.debug(project.getResultTable());
        
	        try {
	        	if (!project.doesResultTableExist() ||
	        			!project.verifyResultTableStructure()) {
	        		JDBCDataSource resultDataSource = (JDBCDataSource) resultChooser.getDataSourceComboBox().getSelectedItem();
					MMSUtils.createResultTable(swingSession.getFrame(), resultDataSource, project);

	        		// Invalidate the current cache because we just added a new result table and it won't be in the cache.
					DatabaseMetaDataDecorator.putHint(DatabaseMetaDataDecorator.CACHE_STALE_DATE, new Date());
	        		SQLTable resultTable = swingSession.findPhysicalTableByName(project.getResultTableSPDatasource(), project.getResultTableCatalog(),
	        				project.getResultTableSchema(), project.getResultTableName());
					if (resultTable == null) return false;
					project.setResultTable(resultTable);
	        	}
	        	if (!project.doesResultTableExist() || !project.verifyResultTableStructure()) {
	        		return false;
	        	}
			} catch (Exception e) {
				SPSUtils.showExceptionDialogNoReport(swingSession.getFrame(),
					"Error in trying to update result table while saving", e);
			}
        }

        if (project.getParent() == null) {
        	sourceChooser.getDataSourceComboBox().setEnabled(false);
        	sourceChooser.getCatalogComboBox().setEnabled(false);
        	sourceChooser.getSchemaComboBox().setEnabled(false);
        	sourceChooser.getTableComboBox().setEnabled(false);
        	
        	if (project.getType() == ProjectMode.FIND_DUPES) {
	        	// defaults the merge rules
				TableMergeRules mergeRule = new TableMergeRules();
				mergeRule.setTable(sourceTable);
				mergeRule.setTableIndex(sourceTableIndex);
				mergeRule.deriveColumnMergeRules();
				for (ColumnMergeRules cmr : mergeRule.getChildren(ColumnMergeRules.class)) {
					if (mergeRule.getPrimaryKeyFromIndex().contains(cmr.getColumn())) {
						cmr.setActionType(MergeActionType.NA);
					}
				}
				project.addChild(mergeRule);
        	}
        } else {
        	if (project.getType() == ProjectMode.FIND_DUPES) {
        		for (TableMergeRules tmr : project.getTableMergeRules()) {
        			if (tmr.isSourceMergeRule()) {
        				tmr.setTableIndex(sourceTableIndex);
        				for (ColumnMergeRules cmr : tmr.getChildren(ColumnMergeRules.class)) {
        					if (tmr.getPrimaryKeyFromIndex().contains(cmr.getColumn())) {
        						cmr.setActionType(MergeActionType.NA);
        					} else if (cmr.getActionType() == MergeActionType.NA) {
        						cmr.setActionType(MergeActionType.USE_MASTER_VALUE);
        					}
        				}
        				break;
        			}
        		}
        	}
        }
		
        logger.debug(project.getResultTable());
		logger.debug("Saving Project:" + project.getName());
		handler.resetHasValidated();
        
        PlFolder selectedFolder = (PlFolder) folderComboBox.getSelectedItem();
        if (project.getParent() != selectedFolder) {
            swingSession.move(project,selectedFolder);
        } 
        
        logger.debug(project.getResultTable());
        logger.debug("saving");

		return true;
    }
	
    private class ProjectSourceTableValidator implements Validator {

        List<Action> actionsToDisable;

        public ProjectSourceTableValidator(List<Action> actionsToDisable){
            this.actionsToDisable = actionsToDisable;
        }

        public ValidateResult validate(Object contents) {

			SQLTable value = (SQLTable)contents;
			if ( value == null ) {
                enableAction(false);
				return ValidateResult.createValidateResult(Status.FAIL,
						"Project source table is required");
			}
			else {
				try {
					value.populate();
				} catch (SQLObjectException e) {
					throw new SQLObjectRuntimeException(e);
				}
				enableAction(true);

			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}

        public void enableAction(boolean enable){
            for (Action a : actionsToDisable){
                a.setEnabled(enable);
            }
        }
    }


    private class ProjectSourceTableIndexValidator implements Validator {

    	public ValidateResult validate(Object contents) {
			SQLIndex value = (SQLIndex)contents;
			if ( value == null ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Project source table index is required");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }

    private class ProjectResultCatalogSchemaValidator implements Validator {

    	private String componentName;
    	public ProjectResultCatalogSchemaValidator(String componentName) {
    		this.componentName = componentName;
		}
		public ValidateResult validate(Object contents) {
			SQLObject value = (SQLObject)contents;
			if ( value == null ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						componentName + " is required");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }

    private class ProjectResultTableNameValidator implements Validator {
        private static final int MAX_CHAR_RESULT_TABLE = 30;

		public ValidateResult validate(Object contents) {
			final Pattern sqlIdentifierPattern =
				Pattern.compile("[a-z_][a-z0-9_]*", Pattern.CASE_INSENSITIVE);

			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Project result table name is required");
			}
			
			if (value.length() > MAX_CHAR_RESULT_TABLE){
			    return ValidateResult.createValidateResult(Status.FAIL, "The result table name " +
                        "cannot be more than " +  MAX_CHAR_RESULT_TABLE + " characters long");
            }
			
			if (!sqlIdentifierPattern.matcher(value).matches()) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Result table name is not a valid SQL identifier");
			}
			
			return ValidateResult.createValidateResult(Status.OK, "");
		}

        private String getSelectedSchemaName() {
            if ( resultChooser.getSchemaComboBox().getSelectedItem() != null) {
                return ((SQLSchema) resultChooser.getSchemaComboBox().getSelectedItem()).getName();
            } else {
                return null;
            }
        }

        private String getSelectedCatalogName() {
            if ( resultChooser.getCatalogComboBox().getSelectedItem() != null) {
                return ((SQLCatalog) resultChooser.getCatalogComboBox().getSelectedItem()).getName();
            } else {
                return null;
            }
        }
    }

	public boolean hasUnsavedChanges() {
		return handler.hasPerformedValidation();
	}

	public void discardChanges() {
		
	}

	public Project getCurrentEditingMMO() {
		return project;
	}
}