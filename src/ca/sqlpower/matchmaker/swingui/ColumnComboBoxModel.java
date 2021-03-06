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

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * A combo box data model that treats the columns of a SQLTable as
 * combo box list entries.
 * <P>
 * TODO this class does not currently listen to its SQLTable for changes
 * to the columns, but it should!
 */
public class ColumnComboBoxModel implements ComboBoxModel {
    
	private final SQLTable table;

	private List<SQLColumn> columns = new ArrayList<SQLColumn>();

	private SQLColumn selected;

    /**
     * Creates a combo box model for the given table.  This combo box
     * model will only work with this one table for its whole life.
     * 
     * @param table The table to use
     * @throws SQLObjectRuntimeException If the table column populate fails
     */
	public ColumnComboBoxModel(SQLTable table) {
		super();
        if (table == null) throw new NullPointerException("Null table not allowed");
        this.table = table;
        try {
            for (SQLColumn c : table.getColumns()) {
                columns.add(c);
            }
        } catch (SQLObjectException ex) {
            throw new SQLObjectRuntimeException(ex);
        }
	}

	public String getTableName() {
		return table.getName();
	}

	public SQLColumn getElementAt(int index) {
		return columns.get(index);
	}

	public int getSize() {
		return columns.size();
	}
	
    public SQLColumn getSelectedItem() {
		return selected;
	}

	public void setSelectedItem(Object anItem) {
		if (anItem != null) {
			selected = (SQLColumn) anItem;
		}
	}

	public void addListDataListener(ListDataListener l) {
		// nothing for now

	}

	public void removeListDataListener(ListDataListener l) {
		// nothing for now

	}

	public SQLTable getTable() {
		return table;
	}

}
