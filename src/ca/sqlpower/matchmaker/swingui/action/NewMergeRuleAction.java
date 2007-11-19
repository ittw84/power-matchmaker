/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui.action;

import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.MergeColumnRuleEditor;
import ca.sqlpower.matchmaker.swingui.SQLObjectChooser;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This action opens a dialog for the user to choose the SQLTable for
 * a new merge rule which is then added to the swing session and the editor 
 * for the new merge rule is opened.
 */
public class NewMergeRuleAction extends AbstractAction {
    
    private final MatchMakerSwingSession swingSession;
	private final Project parent;
	private TableMergeRules mergeRule;
	private JDialog dialog;

	public NewMergeRuleAction(MatchMakerSwingSession swingSession, Project parent) {
	    super("New Merge Rule");
        this.swingSession = swingSession;
        this.parent = parent;
        dialog = new JDialog(swingSession.getFrame(),"New merge rule");
        
        if (parent == null) throw new IllegalArgumentException("Parent must be non null");
	}
	
	public void actionPerformed(ActionEvent e) {
		final SQLObjectChooser chooser = new SQLObjectChooser(swingSession);
		
		ItemListener il = new ItemListener(){
			public void itemStateChanged(ItemEvent e) {			
				SwingUtilities.invokeLater(new Runnable(){

					public void run() {	
						dialog.setSize(dialog.getPreferredSize());
					}
				});
			}
		};
		
		chooser.getCatalogComboBox().addItemListener(il);
		chooser.getSchemaComboBox().addItemListener(il);
		chooser.getTableComboBox().addItemListener(il);
		//this is hidden from the user but important
		chooser.getDataSourceComboBox().getModel().setSelectedItem(parent.getSourceTable().getParentDatabase().getDataSource());
		
		Action okAction = new AbstractAction("OK") {
			public void actionPerformed(ActionEvent e) {
				mergeRule = new TableMergeRules();
				mergeRule.setTable((SQLTable) chooser.getTableComboBox().getSelectedItem());
				mergeRule.setTableIndex((SQLIndex) chooser.getUniqueKeyComboBox().getSelectedItem());
		        mergeRule.deriveColumnMergeRules();
				
				swingSession.setCurrentEditorComponent(new MergeColumnRuleEditor(swingSession,
						parent,mergeRule));
				dialog.dispose();
			}
		};
		
		List<Action> actions = new ArrayList<Action>();
		actions.add(okAction);
		StatusComponent status = new StatusComponent();
		
		FormValidationHandler handler = new FormValidationHandler(status,
			actions);
		handler.resetHasValidated(); 
		
		JButton okButton = new JButton(okAction);
		JButton cancelButton = new JButton(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		
		FormLayout layout = new FormLayout(
			//"10dlu,pref,4dlu,fill:min(pref;"+3*(new JComboBox().getMinimumSize().width)+"px):grow,10dlu",
				"10dlu,pref,4dlu,fill:max(pref;" + 5*new JComboBox().getMinimumSize().getWidth() + "px):grow,10dlu", 
        	"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,10dlu");
        	//1    2    3    4    5     6    7   8    9 
		CellConstraints cc = new CellConstraints();
		Panel p = new Panel(layout);
	
		int row = 2;
		p.add(status, cc.xyw(2, row, 3, "f,f"));
		
		row += 2;
		p.add(new JLabel("Catalog:"), cc.xy(2, row));
        p.add(chooser.getCatalogComboBox(), cc.xy(4, row));
        
        row += 2;
        p.add(new JLabel("Schema:"), cc.xy(2, row));
        p.add(chooser.getSchemaComboBox(), cc.xy(4, row));
        
        row += 2;
        p.add(new JLabel("Table:"), cc.xy(2, row));
        p.add(chooser.getTableComboBox(), cc.xy(4, row));
        
        row += 2;
        p.add(new JLabel("Index:"), cc.xy(2, row));
        p.add(chooser.getUniqueKeyComboBox(), cc.xy(4, row));
        
        row += 2;
        Panel bot = new Panel(new FlowLayout());
        bot.add(okButton);
        bot.add(cancelButton);
        
		MergeColumnRuleComboBoxValidator v1 = new MergeColumnRuleComboBoxValidator(chooser);
		handler.addValidateObject(chooser.getCatalogComboBox(), v1);
		handler.addValidateObject(chooser.getSchemaComboBox(), v1);
		handler.addValidateObject(chooser.getTableComboBox(), v1);
		handler.addValidateObject(chooser.getUniqueKeyComboBox(), v1);
        
        p.add(bot, cc.xyw(1, row, 5, "c,f"));
        dialog.add(p);
        dialog.pack();
		dialog.setLocationRelativeTo(swingSession.getFrame());
        dialog.setVisible(true);
	}

	/**
	 * Validates the SQLChooser ComboBoxes
	 */
	private class MergeColumnRuleComboBoxValidator implements Validator {

		private SQLObjectChooser chooser;
		public MergeColumnRuleComboBoxValidator(SQLObjectChooser chooser) {
			this.chooser = chooser;
		}
		
		public ValidateResult validate(Object contents) {
			if (chooser.getTableComboBox().getSelectedItem() == null) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Merge table is required");
				
			// Checks if an existing merge rule is already set to operate on 
			// the chosen schema and table.
			} else if (chooser.getTableComboBox().isEnabled()){
				List<TableMergeRules> mergeRules = parent.getTableMergeRules();
				String schemaName = ((SQLSchema)chooser.getSchemaComboBox().getSelectedItem()).getName();
				String tableName = ((SQLTable)chooser.getTableComboBox().getSelectedItem()).getName();
				
				for (TableMergeRules rule: mergeRules) {
					if (rule != mergeRule &&
							rule.getSchemaName().equals(schemaName) &&
							rule.getTableName().equals(tableName)) {
						return ValidateResult.createValidateResult(Status.FAIL,
								"Only one merge rule can operate on an individual table");
					}
				}
			}
			if (chooser.getUniqueKeyComboBox().getSelectedItem() == null) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Merge table index is required");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
		
	}
	
}
