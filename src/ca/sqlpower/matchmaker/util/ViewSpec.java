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

package ca.sqlpower.matchmaker.util;

/**
 * The ViewSpec class specifies a SQL view object as a fully-qualified view name
 * with a set of three strings that can be concatenated together to form a
 * SELECT statement. It also provides methods for creating, replacing, and
 * dropping the view.
 */
public class ViewSpec extends SQLQuery {
    
	/** the view's name */
	private String name;
    
	/** The jdbc catalog containing the view */
	private String catalog;
    
	/** the jdbc schema containing the view */
	private String schema;
	
	public ViewSpec() {
		super();
	}
	
	public ViewSpec(String select, String from, String where) {
		super(select,from,where);
	}

	private void create(){
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private void drop(){
        throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private void replace(){
        throw new UnsupportedOperationException("Not yet implemented");
	}
	
	private void verifyQuery(){
        throw new UnsupportedOperationException("Not yet implemented");
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;		
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	/**
	 * duplicate all the properties of the ViewSpec
	 * @return new ViewSpec instance with the same properties
	 */
	public ViewSpec duplicate() {
		ViewSpec spec = new ViewSpec();
		spec.setCatalog(getCatalog()==null?null:new String(getCatalog()));
		spec.setFrom(getFrom()==null?null:new String(getFrom()));
		spec.setName(getName()==null?null:new String(getName()));
		spec.setSchema(getSchema()==null?null:new String(getSchema()));
		spec.setSelect(getSelect()==null?null:new String(getSelect()));
		spec.setWhere(getWhere()==null?null:new String(getWhere()));
		return spec;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ViewSpec other = (ViewSpec) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (schema == null) {
			if (other.schema != null) {
				return false;
			}
		} else if (!schema.equals(other.schema)) {
			return false;
		}
		if (catalog == null) {
			if (other.catalog != null) {
				return false;
			}
		} else if (!catalog.equals(other.catalog)) {
			return false;
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + ((schema == null) ? 0 : schema.hashCode());
		result = PRIME * result + ((catalog == null) ? 0 : catalog.hashCode());
		return result;
	}
}
