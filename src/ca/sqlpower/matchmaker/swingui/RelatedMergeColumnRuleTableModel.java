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

package ca.sqlpower.matchmaker.swingui;

import javax.swing.event.TableModelEvent;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.swingui.SPSUtils;

/**
 * Implementation of {@link AbstractMergeColumnRuleTableModel}, table model 
 * for column merge rules of related merge rules. This has 4 columns: column
 * name, checkbox for is in primary key, combo boxes for imported key column
 * and action.
 */
public class RelatedMergeColumnRuleTableModel extends 
	AbstractMergeColumnRuleTableModel {
	
	public RelatedMergeColumnRuleTableModel(TableMergeRules mergeRule) {
		super(mergeRule);
		updatePrimaryKeys(null);
	}
	
	public void updatePrimaryKeys(SQLTable table){
		super.updatePrimaryKeys();
		if (table != null) {
			try {
				SQLIndex tableIndex = table.getPrimaryKeyIndex();
				if (tableIndex != null) {
					System.out.println(tableIndex.getChildren());
					for (int i = 0; i < getRowCount(); i++) {
						SQLColumn column = (SQLColumn) getValueAt(i, 0);
						if (tableIndex.getChildByName(column.getName()) != null) {
							primaryKeys.add(i);
						}
					}
				}
			} catch (ArchitectException e) {
				MatchMakerSwingSession session = (MatchMakerSwingSession) mergeRule.getSession();
				SPSUtils.showExceptionDialogNoReport(session.getFrame(),
						"Failed to retrieve primary keys of parent table", e);
			}
		}
	}

	public int getColumnCount() {
		return 4;
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return "Column";
		} else if (column == 1) {
			return "Primary Key";
		} else if (column == 2) {
			return "Imported Key Column";
		} else if (column == 3) {
			return "Update SQL Statement";
		} else {
			throw new RuntimeException("getColumnName: Unexcepted column index:"+column);
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return mergeRule.getChildren().get(rowIndex).getColumn();
		} else if (columnIndex == 1) {
			return mergeRule.getChildren().get(rowIndex).isInPrimaryKey();
		} else if(columnIndex == 2) {
			return mergeRule.getChildren().get(rowIndex).getImportedKeyColumn();
		} else if (columnIndex == 3) {
			if (primaryKeys.contains(rowIndex)) {
				return "NOT_APPLICABLE";
			} else {
				return mergeRule.getChildren().get(rowIndex).getUpdateStatement();
			}
		} else {
			throw new RuntimeException("getValueAt: Unexcepted column index:"+columnIndex);
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 3 && primaryKeys.contains(rowIndex)) {
			return false;
		} else {
			return columnIndex != 0;
		}
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return SQLColumn.class;
		} else if (columnIndex == 1) {
			return Boolean.class;
		} else if (columnIndex == 2) {
			return SQLColumn.class;
		} else if (columnIndex == 3) {
			return String.class;	
		} else {
			throw new IllegalArgumentException("unknown columnIndex: "+ columnIndex);
		}
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		ColumnMergeRules rule = mergeRule.getChildren().get(rowIndex);
		if (columnIndex == 0) {
			rule.setColumn((SQLColumn) aValue);
		} else if (columnIndex == 1) {
			rule.setInPrimaryKey((Boolean) aValue);
		} else if (columnIndex == 2) {
			rule.setImportedKeyColumn((SQLColumn) aValue);
		} else if (columnIndex == 3) {
			if (!primaryKeys.contains(rowIndex)) {
				rule.setUpdateStatement((String) aValue);
			} else {
				// Do not set the value if the cell is a primary key
				return;
			}
		} else {
			throw new RuntimeException("setValueAt: Unexcepted column index:"+columnIndex);
		}
		fireTableChanged(new TableModelEvent(this,rowIndex));
	}
}