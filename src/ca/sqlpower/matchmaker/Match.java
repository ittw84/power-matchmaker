package ca.sqlpower.matchmaker;

import java.util.List;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.util.SourceTable;
import ca.sqlpower.matchmaker.util.ViewSpec;

/**
 * folder is the parent of match. should be not null.
 */
public class Match extends AbstractMatchMakerObject<Match, MatchMakerFolder> {

	public enum MatchType {
		FIND_DUPES("Find Duplicates"), BUILD_XREF("Build Cross-Reference");

		String displayName;

		private MatchType(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}

		/**
		 * Get the MatchType that corrisponds to the type string.
		 *
		 * @param type a string detailing the type you want to get
		 * @return the match type that has type as its toString
		 */
		public static MatchType getTypeByString(String type){
			MatchType[] types = MatchType.values();

			for (MatchType matchType: types) {
				if (matchType.toString().toLowerCase().equals(type.toLowerCase())) {
					return matchType;
				}
			}
			throw new IllegalArgumentException("There is no match type with a string "+type);
		}
	}

	/** the oid for the match */
	Long oid;

	/** The type of match */
	MatchType type;

	/**
	 * The table where we get the match data.
	 */
	SourceTable sourceTable;

	/**
	 * The table where the engine stores the results of a match
	 */
	SQLTable resultTable;

	/**
	 * Table used for cross references
	 */
	SQLTable xrefTable;

	/** The settings for the match engine */
	MatchSettings matchSettings;

	/** the settings for the merge engine */
	MergeSettings mergeSettings;

	/** a filter for the tables that are matched */
	String filter;

	/** FIXME can't remember what the view does */
	ViewSpec view;


	public Match( ) {
		matchSettings = new MatchSettings();
		mergeSettings = new MergeSettings();
	}
	/**
	 * FIXME Implement me
	 *
	 */
	public void execute() {
		throw new NotImplementedException();
	}

	/**
	 * FIXME Implement me
	 *
	 */
	public boolean checkValid() {
		throw new NotImplementedException();
	}

	/**
	 * FIXME Implement me
	 *
	 */
	public void createResultTable() {
		throw new NotImplementedException();
	}

	/**
	 * FIXME Implement me
	 *
	 */
	public void createViewTable() {
		throw new NotImplementedException();
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		String oldValue = this.filter;
		this.filter = filter;
		getEventSupport().firePropertyChange("filter", oldValue, this.filter);
	}

	public MatchSettings getMatchSettings() {
		return matchSettings;
	}

	public void setMatchSettings(MatchSettings matchSettings) {
		MatchSettings oldValue = this.matchSettings;
		this.matchSettings = matchSettings;
		getEventSupport().firePropertyChange("matchSettings", oldValue,
				this.matchSettings);
	}

	public MergeSettings getMergeSettings() {
		return mergeSettings;
	}

	public void setMergeSettings(MergeSettings mergeSettings) {
		MergeSettings oldValue = this.mergeSettings;
		this.mergeSettings = mergeSettings;
		getEventSupport().firePropertyChange("mergeSettings", oldValue,
				this.mergeSettings);
	}



	public SQLTable getResultTable() {
		return resultTable;
	}

	public void setResultTable(SQLTable resultTable) {
		SQLTable oldValue = this.resultTable;
		this.resultTable = resultTable;
		getEventSupport().firePropertyChange("resultTable", oldValue,
				this.resultTable);
	}

	public SourceTable getSourceTable() {
		return sourceTable;
	}

	public void setSourceTable(SourceTable sourceTable) {
		SourceTable oldValue = this.sourceTable;
		this.sourceTable = sourceTable;
		getEventSupport().firePropertyChange("sourceTable", oldValue,
				this.sourceTable);
	}

	public MatchType getType() {
		return type;
	}

	public void setType(MatchType type) {
		MatchType oldValue = this.type;
		this.type = type;
		getEventSupport().firePropertyChange("type", oldValue, this.type);
	}

	public ViewSpec getView() {
		return view;
	}

	public void setView(ViewSpec view) {
		ViewSpec oldValue = this.view;
		this.view = view;
		getEventSupport().firePropertyChange("view", oldValue, this.view);
	}

	/**
	 * Preconditions
	 *
	 * FIXME write this method
	 * @return
	 */
	public List<PlMatchGroup> getMatchGroups() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 0;
        result = PRIME * result + ((getName() == null) ? 0 : getName().hashCode());
        return result;
    }
	@Override
    public boolean equals(Object obj) {
		if ( !(obj instanceof Match) ) {
			return false;
		}
        if (this == obj) {
            return true;
        }

        final Match other = (Match) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        return true;
    }
	public Long getOid() {
		return oid;
	}
	public void setOid(Long oid) {
		this.oid = oid;
	}
	public SQLTable getXrefTable() {
		return xrefTable;
	}
	public void setXrefTable(SQLTable xrefTable) {
		SQLTable oldValue = this.xrefTable;
		this.xrefTable = xrefTable;
		getEventSupport().firePropertyChange("xrefTable", oldValue,
				this.xrefTable);
	}

}