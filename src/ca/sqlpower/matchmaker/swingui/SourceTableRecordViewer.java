package ca.sqlpower.matchmaker.swingui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.SourceTableRecord;

public class SourceTableRecordViewer {
    
    private final JPanel panel;
    private static final Logger logger = Logger.getLogger(SourceTableRecordViewer.class);
    
    public SourceTableRecordViewer(SourceTableRecord view, SourceTableRecord master, JButton topButton) throws ArchitectException, SQLException {
        this.panel = createPanel(view, master, topButton);
    }
 
    // FIXME: get rid of throws clause when source table records are pre-populated by a join
    private final JPanel createPanel(SourceTableRecord view, SourceTableRecord master, JButton topButton) throws ArchitectException, SQLException {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setBackground(Color.WHITE);
        panel.add(topButton);
        
        Iterator<Object> viewIt = view.fetchValues().iterator();
        Iterator<Object> masterIt = master.fetchValues().iterator();
        while (viewIt.hasNext()) {
            Object viewVal = viewIt.next();
            Object masterVal = masterIt.next();
            
            boolean same;
            
            if (viewVal == null) {
                same = masterVal == null;
            } else {
                same = viewVal.equals(masterVal);
            }
            
            JLabel colValueLabel = new JLabel(String.valueOf(viewVal));
            
            if (!same) {
                Font font = colValueLabel.getFont();
                colValueLabel.setFont(font.deriveFont(Font.BOLD).deriveFont((float) (font.getSize2D() +0.5)));
            }
            
            panel.add(colValueLabel);
        }
        
        return panel;
    }
    
    public JPanel getPanel() {
        return panel;
    }
        
    public static JPanel headerPanel(Match match) throws ArchitectException{
        JPanel panel = new JPanel(new GridLayout(0,1));
        //Add a empty label because the first row is the master button
        //and the header should allign with the proper field
        panel.add(new JLabel());
        for (SQLColumn col : match.getSourceTable().getColumns()){
            JLabel label = new JLabel(col.getName()); 
            panel.add(label);
        }
        return panel;
    }
}