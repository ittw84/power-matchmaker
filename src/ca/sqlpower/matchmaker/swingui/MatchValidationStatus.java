package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.RowSet;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.RowSetModel;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.rowset.CachedRowSetImpl;

/**
 * An EditorPane that shows a table of status information about one Match object.
 * If you want to see a status table for a different match, create a new one of these.
 */
public class MatchValidationStatus implements EditorPane {

	private static final Logger logger = Logger.getLogger(MatchValidationStatus.class);
	
	/**
	 * The Match object whose match validation status is this class' concern.
	 */
	private final Match match;
	
	/**
	 * A table to display the validation status.
	 */
	private final JTable status = new JTable();
	
	/**
	 * The session to which this EditorPane belongs.
	 */
    private final MatchMakerSwingSession swingSession;
    
    /**
     * The top-level panel that displays the validation status.
     */
    private JPanel panel;

	public MatchValidationStatus(MatchMakerSwingSession swingSession, Match match) {
		this.swingSession = swingSession;
		this.match = match;
		this.panel = createUI();
	}

	/**
	 * Queries the database to learn the validation status of <code>match</code>
	 * and returns a RowSet that represents the information.
	 * 
	 * @throws SQLException If there was a problem with the database, most
	 * likely connection refused or malformed SQL. 
	 */
	private RowSet getMatchStats() throws SQLException {
    	Connection con = null;
    	Statement stmt = null;
    	ResultSet rs =  null;

    	try {
    		con = swingSession.getConnection();
    		StringBuffer sql = new StringBuffer();
    		sql.append("SELECT GROUP_ID,MATCH_PERCENT,MATCH_STATUS");
    		sql.append(",COUNT(*)/2");
    		sql.append(" FROM  ");
    		SQLTable resultTable = match.getResultTable();
			sql.append(DDLUtils.toQualifiedName(resultTable.getCatalogName(),
					resultTable.getSchemaName(),
					resultTable.getName()));
    		sql.append(" GROUP BY GROUP_ID,MATCH_PERCENT,MATCH_STATUS");
    		sql.append(" ORDER BY MATCH_PERCENT DESC,MATCH_STATUS");

    		PreparedStatement pstmt = con.prepareStatement(sql.toString());
    		rs = pstmt.executeQuery();
    		CachedRowSetImpl crset = new CachedRowSetImpl();
    		crset.setReadOnly(true);
    		crset.populate(rs);
    		return crset;
    	} finally {
    		if ( rs != null )
    			rs.close();
    		if ( stmt != null )
    			stmt.close();
    		if (con != null)
    			con.close();
    	}
    }
	
	/**
	 * A table model wrapper that represents a status table; just
	 * names the first 4 columns and delegates to the table model
	 * supplied in the constructor.
	 */
	private class MatchStatsTableModel extends AbstractTableModel {

		private AbstractTableModel model;

		public MatchStatsTableModel(AbstractTableModel model) {
			this.model = model;
		}
		public int getRowCount() {
			return model.getRowCount();
		}

		public int getColumnCount() {
			return model.getColumnCount();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return model.getValueAt(rowIndex,columnIndex);
		}

		@Override
		public String getColumnName(int column) {
			if ( column == 0 ) {
				return "Group";
			} else if ( column == 1 ) {
				return "Match Percent";
			} else if ( column == 2 ) {
				return "Match Status";
			} else if ( column == 3 ) {
				return "Row Count";
			} else {
				return "unknown column";
			}
		}
	}

	/**
	 * Returns a panel that displays all the status information 
	 */
	private JPanel createUI() {
		RowSetModel rsm = null;
		try {
			rsm = new RowSetModel(getMatchStats());
			MatchStatsTableModel tableModel = new MatchStatsTableModel(rsm);
			status.setModel(tableModel);
			SQLTable resultTable = match.getResultTable();
			status.setName(DDLUtils.toQualifiedName(
					resultTable.getCatalogName(),
					resultTable.getSchemaName(),
					resultTable.getName()));
		} catch (SQLException e1) {
			MMSUtils.showExceptionDialog(getPanel(),
					"Unknown SQL Error", e1);
		}

		FormLayout layout = new FormLayout(
                "10dlu,fill:pref:grow, 10dlu",
         //		 1     2               3
                "10dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,10dlu,fill:pref:grow,4dlu,pref,4dlu");
        //		 1     2    3    4    5    6    7    8    9     10             11   12   13
        PanelBuilder pb;
        JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
        pb = new PanelBuilder(layout, p);

        CellConstraints cc = new CellConstraints();

        pb.add(new JLabel("Match: " + match.getName()), cc.xy(2,6,"l,c"));

		pb.add(new JScrollPane(status), cc.xy(2,10));

        JButton save = new JButton(new AbstractAction("Save"){
			public void actionPerformed(ActionEvent e) {
				new JTableExporter(getPanel(), status);
			}
		});

        ButtonBarBuilder bb1 = new ButtonBarBuilder();
        bb1.addRelatedGap();
        bb1.addGridded(save);
        bb1.addRelatedGap();
        pb.add(bb1.getPanel(), cc.xy(2,12));

        return pb.getPanel();
	}

	/*=============== Editor Pane Interface ============*/
	
	/**
	 * Blindly returns true despite the interface's warning. This
	 * is because we are not actually editing anything so we never
	 * have to save changes.
	 */
	public boolean doSave() {
		return true;
	}

	/**
	 * Returns the top-level panel that holds all the components that this
	 * EditorPane is responsible for.
	 */
	public JComponent getPanel() {
		return panel;
	}

	/**
	 * Returns false because, since we do not allow editing any information
	 * on this EditorPane, there are never changes that could be unsaved.
	 */
	public boolean hasUnsavedChanges() {
		return false;
	}

}
