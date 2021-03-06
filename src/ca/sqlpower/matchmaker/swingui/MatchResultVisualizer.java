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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import ca.sqlpower.graph.BreadthFirstSearch;
import ca.sqlpower.graph.ConnectedComponentFinder;
import ca.sqlpower.graph.GraphModel;
import ca.sqlpower.matchmaker.AutoMatcher;
import ca.sqlpower.matchmaker.MatchPool;
import ca.sqlpower.matchmaker.PotentialMatchRecord;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.SourceTableRecord;
import ca.sqlpower.matchmaker.SourceTableRecordDisplayComparator;
import ca.sqlpower.matchmaker.graph.MatchPoolDotExport;
import ca.sqlpower.matchmaker.graph.MatchPoolGraphModel;
import ca.sqlpower.matchmaker.graph.NonDirectedUserValidatedMatchPoolGraphModel;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.engine.MungeProcessSelectionList;
import ca.sqlpower.matchmaker.swingui.graphViewer.DefaultGraphLayoutCache;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphNodeRenderer;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphSelectionListener;
import ca.sqlpower.matchmaker.swingui.graphViewer.GraphViewer;
import ca.sqlpower.matchmaker.undo.AbstractUndoableEditorPane;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.SwingWorkerRegistry;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The MatchResultVisualizer produces graphical representations of the matches
 * in a Match Result Table.
 */
public class MatchResultVisualizer extends AbstractUndoableEditorPane<MatchPool> {
    
    private static final Logger logger = Logger.getLogger(MatchResultVisualizer.class);

    /**
     * Icons used for the match validation actions.
     */
	final private Icon masterIcon = new ImageIcon(getClass().getResource("/icons/master.png"));
	final private Icon duplicateIcon = new ImageIcon(getClass().getResource("/icons/duplicate.png"));
	final private Icon nomatchIcon = new ImageIcon(getClass().getResource("/icons/nomatch.png"));
	final private Icon unmatchIcon = new ImageIcon(getClass().getResource("/icons/unmatch.png"));
    
	private static final int RECORD_VIEWER_ROW_HEADER_LAYOUT_PADDING = 4;
	
    /**
     * Pops up a save dialog and saves the match pool to the chosen DOT file.
     */
    private final Action exportDotFileAction = new AbstractAction("Export as Dot file...") {
        public void actionPerformed(ActionEvent e) {
            try {
                File dotFile = new File(
                        System.getProperty("user.home"), "dqguru_graph_"+project.getName()+".dot");
                JFileChooser fc = new JFileChooser(dotFile);
                int choice = fc.showSaveDialog(getPanel());
                if (choice == JFileChooser.APPROVE_OPTION) {
                    MatchPoolDotExport exporter = new MatchPoolDotExport(project);
                    exporter.setDotFile(fc.getSelectedFile());
                    exporter.exportDotFile();
                }
            } catch (Exception ex) {
                SPSUtils.showExceptionDialogNoReport(getPanel(), "Couldn't export dot file!", ex);
            }
        }
    };
    
    /**
	 * This action calls the reset method on the pool. For more information
	 * see {@link MatchPool#resetPool()}.
	 */
    private final Action resetPoolAction = new AbstractAction("Reset All") {
    	public void actionPerformed(ActionEvent e) {
			if (JOptionPane.showConfirmDialog(getPanel(),
							"You are about to reset the entire match pool! Do you really wish to do this?",
							"Reset Entire Match Pool",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) return;
			pool.begin("Reseting the match pool.");
			pool.resetPool();
			pool.commit();
    		graph.repaint();
    	}
    };
    
    /**
	 * An action that calls
	 * {@link MatchPoolGraphModel#resetCluster(SourceTableRecord)} on
	 * {@link #selectedNode}, and then saves the changes into the database.
	 */
    private final Action resetClusterAction = new AbstractAction("Reset Cluster") {
		public void actionPerformed(ActionEvent e) {
			graphModel.resetCluster(selectedNode);
			graph.repaint();
		}
    };

    /**
	 * A button associated with {@link #resetClusterAction}. It's made into a
	 * field so that we can enable/disable it according to whether or not a node
	 * has been selected.
	 */
    private JButton resetClusterButton;

    /**
	 * A button associated with refreshAction so that we can reload the MatchScreen with the new
	 * number of matches to display.
	 */
    private JButton refreshButton;
    
    /**
     * Warning! Will throw a ClassCastException if the node renderer in graph
     * is not a SourceTableRecordRenderer.
     */
    private final Action chooseDisplayedValueAction = new AbstractAction("Choose Displayed Value...") {
    	
    	private JDialog dialog;    	
    	private DisplayedNodeValueChooser chooser;
    	private List<SQLColumn> revertList = new ArrayList<SQLColumn>();
    	
    	private final AbstractAction okAction = new AbstractAction("OK") {
    		public void actionPerformed(ActionEvent e) {
    			
    			List<SQLColumn> chosenColumns = chooser.getChosenColumns();
    			
    			try{
	    			// Refresh the display column preferences with the newly chosen columns
	    			Preferences displayColumnPrefs = matchValidationPrefs.node("displayColumns");
	    			displayColumnPrefs.clear();
	    			for (int i = 0; i < chosenColumns.size(); i++) {
	    				SQLColumn column = chosenColumns.get(i);
	    				displayColumnPrefs.putInt(column.getName(), i);
	    			}
    			} catch (BackingStoreException bsEx) {
    				logger.error("Exception while storing columns in Preferences", bsEx);
    			}
    			
    			try {
    				MatchResultVisualizer.this.displayColumns = chosenColumns;
					MatchResultVisualizer.this.getPool().find(chosenColumns);
    				MatchResultVisualizer.this.getPanel().repaint();
    				MatchResultVisualizer.this.doAutoLayout();
    			} catch (SQLObjectException ex) {
    				MMSUtils.showExceptionDialog((Component) e.getSource(), ex.getMessage(), ex);
    			} catch (SQLException sqlEx) {
    				MMSUtils.showExceptionDialog((Component) e.getSource(), sqlEx.getMessage(), sqlEx);
    			} finally {
    				dialog.dispose();
    			}
    		}
    	};
    	
    	private final AbstractAction cancelAction = new AbstractAction("Cancel") {
    		public void actionPerformed(ActionEvent e) {
    			chooser.setChosenColumns(revertList);
    			dialog.dispose();
    		}
    	};
    	
    	private final AbstractAction selectAction = new AbstractAction("Select All") {
    		public void actionPerformed(ActionEvent e) {
    			chooser.setAllDefaultChosen(true);
    		}
    	};
    	
    	private final AbstractAction deselectAction = new AbstractAction("Deselect All") {
    		public void actionPerformed(ActionEvent e) {
    			chooser.setAllDefaultChosen(false);
    		}
    	};
    	
    	public void actionPerformed(ActionEvent e) {
    		try {
    			dialog = SPSUtils.makeOwnedDialog(getPanel(), "Select Graph Display Values");
				if (chooser == null) {
    				chooser = new DisplayedNodeValueChooser((SourceTableNodeRenderer) graph.getNodeRenderer(), project, displayColumns);
    			}
				
				revertList = chooser.getChosenColumns();
				
				JDefaultButton okButton = new JDefaultButton(okAction);
				JPanel buttonPanelRight = ButtonBarFactory.buildOKCancelBar(okButton, new JButton(cancelAction));
				JPanel buttonPanelLeft = ButtonBarFactory.buildLeftAlignedBar(new JButton(selectAction), 
						new JButton(deselectAction));
                JPanel buttonPanel = new JPanel(new BorderLayout());
                buttonPanel.add(buttonPanelLeft, BorderLayout.WEST);
                buttonPanel.add(buttonPanelRight, BorderLayout.EAST);
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(chooser.makeGUI(), BorderLayout.CENTER);
				panel.add(buttonPanel, BorderLayout.SOUTH);
				dialog.getContentPane().add(panel);
    			dialog.pack();
                dialog.getRootPane().setDefaultButton(okButton);
                SPSUtils.makeJDialogCancellable(dialog, cancelAction, false);
    			dialog.setLocationRelativeTo((Component) e.getSource());
    			dialog.setVisible(true);
    		} catch (SQLObjectException ex) {
    			MMSUtils.showExceptionDialog((Component) e.getSource(), ex.getMessage(), ex);
    		}
    	}
    };  

    /**
     * The action for showing and hiding rows on the validation chart.
     */
    private final Action chooseDisplayedColumnAction = new AbstractAction("Columns...") {
    	
    	private JDialog dialog;    	
    	private DisplayedNodeValueChooser chooser;
    	private List<SQLColumn> revertList = new ArrayList<SQLColumn>();
    	
    	private final AbstractAction okAction = new AbstractAction("OK") {
    		public void actionPerformed(ActionEvent e) {
    			if (chooser.getChosenColumns().isEmpty()) {
    				JOptionPane.showMessageDialog(dialog, "Please select at least one column!", 
    						"No columns selected", JOptionPane.INFORMATION_MESSAGE);
    			} else {
    				List<SQLColumn> chosenColumns = chooser.getChosenColumns();
    				
    				try {
						// Refresh the display column preferences with the newly chosen columns
						Preferences shownColumnPrefs = matchValidationPrefs.node("shownColumns");
						shownColumnPrefs.clear();
						for (int i = 0; i < chosenColumns.size(); i++) {
							SQLColumn column = chosenColumns.get(i);
							shownColumnPrefs.putInt(column.getName(), i);
						}
					} catch (BackingStoreException ex) {
						logger.error("Exception while storing columns in Preferences", ex);
					}
    				
    				shownColumns = chosenColumns;
    				updateMatchTable();
    				dialog.dispose();
    			}
    		}
    	};
    	
    	private final AbstractAction cancelAction = new AbstractAction("Cancel") {
    		public void actionPerformed(ActionEvent e) {
    			chooser.setChosenColumns(revertList);
    			dialog.dispose();
    		}
    	};
    	
    	private final AbstractAction selectAction = new AbstractAction("Select All") {
    		public void actionPerformed(ActionEvent e) {
    			chooser.setAllDefaultChosen(true);
    		}
    	};
    	
    	private final AbstractAction deselectAction = new AbstractAction("Deselect All") {
    		public void actionPerformed(ActionEvent e) {
    			chooser.setAllDefaultChosen(false);
    		}
    	};
    	
    	public void actionPerformed(ActionEvent e) {
    		try {
    			dialog = SPSUtils.makeOwnedDialog(getPanel(), "Select Chart Display Values");
				if (chooser == null) {
					if (shownColumns == null) {
						shownColumns = new ArrayList<SQLColumn>();
						chooser = new DisplayedNodeValueChooser((SourceTableNodeRenderer) graph.getNodeRenderer(), project, shownColumns);
						// Defaults the shown value to true
						chooser.setAllDefaultChosen(true);
						shownColumns = chooser.getChosenColumns();
					} else {
						chooser = new DisplayedNodeValueChooser((SourceTableNodeRenderer) graph.getNodeRenderer(), project, shownColumns);
					}
				}

				revertList = chooser.getChosenColumns();

				JDefaultButton okButton = new JDefaultButton(okAction);
				JPanel buttonPanelRight = ButtonBarFactory.buildOKCancelBar(okButton, new JButton(cancelAction));
				JPanel buttonPanelLeft = ButtonBarFactory.buildLeftAlignedBar(new JButton(selectAction), 
						new JButton(deselectAction));
                JPanel buttonPanel = new JPanel(new BorderLayout());
                buttonPanel.add(buttonPanelLeft, BorderLayout.WEST);
                buttonPanel.add(buttonPanelRight, BorderLayout.EAST);
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(chooser.makeGUI(), BorderLayout.CENTER);
				panel.add(buttonPanel, BorderLayout.SOUTH);
				dialog.getContentPane().add(panel);
    			dialog.pack();
                dialog.getRootPane().setDefaultButton(okButton);
                SPSUtils.makeJDialogCancellable(dialog, cancelAction, false);
    			dialog.setLocationRelativeTo((Component) e.getSource());
    			dialog.setVisible(true);
    		} catch (SQLObjectException ex) {
    			MMSUtils.showExceptionDialog((Component) e.getSource(), ex.getMessage(), ex);
    		}
    	}
    };
    
    /**
	 * When this action is fired the nodes will become unrelated.
	 */
    class SetNoMatchAction extends AbstractAction{
    	private final SourceTableRecord record1;
        private final SourceTableRecord record2;
        
        protected SetNoMatchAction (String name, SourceTableRecord record1, SourceTableRecord record2){
            super("", nomatchIcon);
            super.putValue(AbstractAction.SHORT_DESCRIPTION, name);
            this.record1 = record1;
            this.record2 = record2;
        }
        
        public void actionPerformed(ActionEvent e){
            try {
            	pool.begin("setting a no-match");
				pool.defineNoMatch(record1, record2);
				pool.commit();
            } catch (SQLObjectException ex) {
    			pool.rollback("rolling back no-match setting");
            	MMSUtils.showExceptionDialog(getPanel(), "An exception occurred while trying to " +
            			"define " + record1 + " and " + record2 + " to not be duplicates.", ex);
            } catch (Exception ex) {
    			pool.rollback("rolling back no-match setting");
    			throw new RuntimeException(ex);
    		}
            selectionListener.nodeSelected(record1);
            graph.repaint();
        }
    }
    
    /**
	 * When this action is fired the nodes will have the edge directly relating
	 * them set to undecided if the edge exists.
	 */
    class SetUnmatchAction extends AbstractAction{
    	private final SourceTableRecord record1;
        private final SourceTableRecord record2;
        
        protected SetUnmatchAction (String name, SourceTableRecord record1, SourceTableRecord record2){
            super("", unmatchIcon);
            super.putValue(AbstractAction.SHORT_DESCRIPTION, name);
            this.record1 = record1;
            this.record2 = record2;
        }
        
        public void actionPerformed(ActionEvent e){
            try {
				pool.defineUnmatched(record1, record2);
			} catch (SQLObjectException ex) {
				MMSUtils.showExceptionDialog(getPanel(), "An exception occurred when trying to " +
						"unmatch " + record1 + " and " + record2, ex);
			}
            selectionListener.nodeSelected(record1);
            graph.repaint();
        }
    }
    
    /**
	 * When this action is fired the master given to the constructor will become
	 * the master of the given duplicate.
	 */
    class SetMasterAction extends AbstractAction {
        
        private final SourceTableRecord master;
        private final SourceTableRecord duplicate;
        
        SetMasterAction(String name, SourceTableRecord master, SourceTableRecord duplicate) {
            super("", masterIcon);
            super.putValue(AbstractAction.SHORT_DESCRIPTION, name);
            this.master = master;
            this.duplicate = duplicate;
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
				pool.defineMaster(master, duplicate);
			} catch (SQLObjectException ex) {
				pool.rollback("rolling back master setting");
				MMSUtils.showExceptionDialog(getPanel(), "An exception occurred when trying " +
						"to set " + master + " to be the master of " + duplicate, ex);
			} catch (Exception ex) {
				pool.rollback("rolling back master setting");
				throw new RuntimeException(ex);
			}
            selectionListener.nodeSelected(duplicate);
        }
    }
    
    /**
	 * When this action is fired the master given to the constructor will become
	 * the master of the given duplicate.
	 */
    class SetDuplicateAction extends AbstractAction {
        
        private final SourceTableRecord master;
        private final SourceTableRecord duplicate;
        
        SetDuplicateAction(String name, SourceTableRecord master, SourceTableRecord duplicate) {
            super("", duplicateIcon);
            super.putValue(AbstractAction.SHORT_DESCRIPTION, name);
            this.master = master;
            this.duplicate = duplicate;
        }
        
        public void actionPerformed(ActionEvent e) {
            try {
				pool.defineMaster(master, duplicate);
            } catch (SQLObjectException ex) {
				MMSUtils.showExceptionDialog(getPanel(), "An exception occurred when trying " +
						"to set " + duplicate + " to be a duplicate of " + master, ex);
            }
            selectionListener.nodeSelected(master);
        }
    }
    
    private class MyGraphSelectionListener implements GraphSelectionListener<SourceTableRecord, PotentialMatchRecord> {

        public void nodeDeselected(SourceTableRecord node) {
        	selectedNode = null;
            recordViewerPanel.removeAll();
            recordViewerPanel.add(SourceTableRecordViewer.getNoNodeSelectedLabel());
            recordViewerPanel.revalidate();
            recordViewerColumnHeader.removeAll();
            recordViewerColumnHeader.revalidate();
            recordViewerRowHeader.removeAll();
            recordViewerRowHeader.revalidate();
            recordViewerCornerPanel.removeAll();
            recordViewerCornerPanel.revalidate();
            resetClusterButton.setEnabled(false);
        }

        public void nodeSelected(final SourceTableRecord node) {
        	selectedNode = node;
        	updateMatchTable();
        	resetClusterButton.setEnabled(true);
        }     
    }
    
    /**
     * An action to get at the *shudder* Auto-Match feature.
     */
    private class AutoMatchAction extends AbstractAction {
    	/**
    	 * A message that expresses our concerns about the auto-layout feature
    	 * to the user.
    	 */
    	private final String warningMessage = "WARNING: Performing an the auto-match operation will create\n"
											+ "matches between all records that were matched according to the\n"
											+ "selected rule set. It is imperative that you review these\n"
											+ "matches carefully before merging records because this operation\n"
											+ "does NOT rank records based on their perceived usefulness.";
    	
    	private MatchMakerSwingSession session;
    	
    	public AutoMatchAction(GraphModel<SourceTableRecord, PotentialMatchRecord> model, MatchMakerSwingSession session) {
    		super("Auto-Match");
    		this.session = session;
    	}
    	
		public void actionPerformed(ActionEvent e) {
			int response = JOptionPane.showConfirmDialog(getPanel(), warningMessage, "WARNING", JOptionPane.OK_CANCEL_OPTION);
			if (response == JOptionPane.OK_OPTION) {
				try {
					MungeProcess selectedProcess = (MungeProcess) mungeProcessComboBox.getSelectedItem();
					AutoMatchWorker worker = new AutoMatchWorker(pool, selectedProcess, session);
					ProgressMonitor monitor = new ProgressMonitor(session.getFrame(), "Auto-matching...", null, 0, 100);
					ProgressWatcher watcher = new ProgressWatcher(monitor, worker);
		        	watcher.start();
		            new Thread(worker).start();
				} catch (Exception ex) {
					MMSUtils.showExceptionDialog(getPanel(), "Auto-Match failed, most likely a database connection error", ex);
				}
			}
		}
    }
    
    private class AutoMatchWorker extends SPSwingWorker {

    	private AutoMatcher autoMatcher;
    	private MungeProcess mungeProcess;
    	private MatchPool pool;
    	
		public AutoMatchWorker(MatchPool pool, MungeProcess mungeProcess, SwingWorkerRegistry registry) {
			super(registry);
			autoMatcher = new AutoMatcher(pool);
			this.mungeProcess = mungeProcess;
			this.pool = pool;
		}

		@Override
		public void cleanup() throws Exception {
			if (getDoStuffException() != null) {
				pool.clearRecords();
				pool.find(displayColumns);
				if (!(getDoStuffException() instanceof CancellationException)) {
					MMSUtils.showExceptionDialog(getPanel(), "Error during auto-match", getDoStuffException());
					logger.error("Error during auto-match", getDoStuffException());
				}
			}
			graph.repaint();
		}

		@Override
		public void doStuff() throws Exception {
			pool.begin("Doing automatch");
			autoMatcher.doAutoMatch(mungeProcess);
			pool.setDebug(false);
			pool.setUseBatchUpdates(false);
			pool.commit();
		}

		@Override
		protected Integer getJobSizeImpl() {
			return autoMatcher.getJobSize() + pool.getJobSize();
		}

		@Override
		protected String getMessageImpl() {
			return autoMatcher.getMessage();
		}

		@Override
		protected int getProgressImpl() {
			int amProgress = autoMatcher.getProgress();
            if (!autoMatcher.isFinished()) {
				return amProgress;
			} else {
				return amProgress + pool.getProgress();
			}
		}

		@Override
		protected boolean hasStartedImpl() {
			return autoMatcher.hasStarted();
		}

		@Override
		protected boolean isFinishedImpl() {
			return (autoMatcher.isFinished() && pool.isFinished()) || isCancelled();
		}
    	
		@Override
		public synchronized void setCancelled(boolean cancelled) {
			super.setCancelled(cancelled);
			autoMatcher.setCancelled(cancelled);
			pool.setCancelled(cancelled);
		}
    }
    
    private final Action refreshAction = new AbstractAction("Refresh Matches") {
    	
    	public void actionPerformed(ActionEvent e) {
            try {
            	pool.setLimit(Integer.parseInt(limitField.getText()));
                pool.clearRecords();
				pool.find(displayColumns);
				setViewLabelText();
				refreshGraph();
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			} catch (SQLObjectException ex) {
				throw new RuntimeException(ex);
			}
    	}
    };
    
    /**
     * This is the match whose result table we're visualizing.
     */
    private final Project project;

    /**
     * The visual component that actually displays the match results graph.
     */
    private GraphViewer<SourceTableRecord, PotentialMatchRecord> graph;

    /**
     * The component that is used to layout the RecordViewer objects.
     */
    private final JPanel recordViewerPanel = new JPanel(new RecordViewerLayout(RECORD_VIEWER_ROW_HEADER_LAYOUT_PADDING));

    /**
     * The header component that is fixed above the record viewer panel (it
     * has the match/no match buttons and stuff).
     * <p>
     * The layout strategy here is iffy. It would be better if this header and the
     * recordViewerPanel itself were managed by the same LayoutManager instance,
     * which would ingore all layout requests for this header, and set the sizes
     * for both the record viewer body panel and related header component at the
     * same time (when laying out the body).  The main problem with the current approach
     * is that it's not possible to ensure there will be enough room for the buttons
     * in the header when laying out the body, but we can't have the two layout
     * managers wrestle each other.
     */
    private final JPanel recordViewerColumnHeader = new JPanel(new RecordViewerLayout(0));

    /**
     * The row header component that is placed on the left side of the record viewer panel.
     * If no node is selected, it would contain only one panel with the names of the columns
     * of the source table record to be displayed. If a node is selected, it will also contain
     * in a second column the values of the source table record from the selected node. This is
     * to prevent the selected record from scrolling out of view when scrolling through the records.
     */
    private final JPanel recordViewerRowHeader = new JPanel(new RecordViewerLayout(RECORD_VIEWER_ROW_HEADER_LAYOUT_PADDING));

    /**
     * The component that is placed in the upper left corner of the record viewer.
     * Typically, if no node is selected, then it will be an empty panel. Otherwise, it
     * would contain a toolbar for the source table record column of the selected node. 
     */
    private final JPanel recordViewerCornerPanel = new JPanel(new RecordViewerLayout(0));
    
    private final MyGraphSelectionListener selectionListener = new MyGraphSelectionListener();

    private final MatchPool pool;

    private MatchPoolGraphModel graphModel;
    
    private JComboBox mungeProcessComboBox;
    
    private MungeProcessSelectionList selectionButton;
    
    private JButton autoMatchButton;
    
    private SPListener updaterListener;
    
    private JTextField limitField;
    
    private final JButton nextSet;
    
    private final JButton previousSet;
    
    /**
     * Displays to the user what number of matches we are currently viewing.
     */
    private final JLabel viewLabel;
    
    /**
     * The node on the graph that is currently selected.
     */
    private SourceTableRecord selectedNode;

	/**
	 * A list of the SQLColumns that we want to display for each
	 * SourceTableRecord in the match validation table.
	 */
    private List<SQLColumn> shownColumns;
    
    /**
     * A list of the SQLColumns that we want to display in each SourceTableRecord
     * node in the match validation graph.
     */
    private List<SQLColumn> displayColumns = new ArrayList<SQLColumn>();
    
    /**
	 * A {@link Preferences} node containing user and project-specific
	 * preferences for the match validation screen.
	 * The pathname used to retrieve it from the user root preference node is as
	 * follows:
	 * 
	 * /ca/sqlpower/matchmaker/projectSettings/$<repository connection name>/$<project oid>/matchValidation";
	 */
    private final Preferences matchValidationPrefs;

	private final MatchMakerSwingSession session;
    
    public MatchResultVisualizer(Project project, MatchMakerSwingSession session) throws SQLException, SQLObjectException {
    	super(session, project.getMatchPool());
        this.project = project;
		this.session = session;
        this.pool = project.getMatchPool();
		
        if (!project.doesSourceTableExist() || !project.verifySourceTableStructure()) {
        	String errorText =
        	    "The DQguru has detected changes in the source table structure.\n" +
        	    "Your project will require modifications before the engines can run properly.\n" +
        	    "The DQguru can automatically modify the project but the changes may not be reversible.\n" +
        	    "Would you like the DQguru to modify the project now?";
			int response = JOptionPane.showOptionDialog(session.getFrame(), errorText,
					"Source Table Changed", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
					new String[] {"Fix Now", "Not Now"}, "Fix Now");
			if (response == JOptionPane.YES_OPTION) {
				try {
				    SQLDatabase db = project.getSourceTable().getParentDatabase();
				    db.refresh();
				} catch (Exception ex1) {
					MMSUtils.showExceptionDialog(getPanel(), "Failed to fix project!", ex1);
				}
			}
        }
        
        pool.clearRecords();
        pool.find(displayColumns);
        
        FormLayout topLayout = new FormLayout("pref", "pref, pref");
        JPanel topPanel = new JPanel (topLayout);
        PanelBuilder pb = new PanelBuilder(topLayout, topPanel);
        CellConstraints cc = new CellConstraints();
        JPanel buttonPanel = new JPanel(new GridLayout(8,1));
        buttonPanel.add(new JButton(chooseDisplayedValueAction));
        
        selectionButton = new MungeProcessSelectionList(project) {

			@Override
			public boolean getValue(MungeProcess mp) {
				return mp.isValidate();
			}

			@Override
			public void setValue(MungeProcess mp, boolean value) {
				mp.setValidate(value);
			}

		};
		selectionButton.setCloseAction(new Runnable(){
			public void run() {
				graph.setSelectedNode(null);
				graph.setFocusedNode(null);
				pool.clearRecords();
				pool.clearCache();
		        try {
					pool.find(displayColumns);
				} catch (SQLObjectException ex) {
    				MMSUtils.showExceptionDialog(getPanel(), ex.getMessage(), ex);
    			} catch (SQLException sqlEx) {
    				MMSUtils.showExceptionDialog(getPanel(), sqlEx.getMessage(), sqlEx);
    			}
				((DefaultGraphLayoutCache)graph.getLayoutCache()).clearNodes();
				updateAutoMatchComboBox();
				doAutoLayout();
				graph.repaint();
			}
		});        
        
        buttonPanel.add(selectionButton);
        buttonPanel.add(new JButton(resetPoolAction));
        resetClusterButton = new JButton(resetClusterAction);
        resetClusterButton.setEnabled(selectedNode != null);
        buttonPanel.add(resetClusterButton);
        JPanel limitPanel = new JPanel(new GridLayout(1,2));
        limitPanel.add(new JLabel("# of clusters to view: "));
        limitField = new JTextField(20);
        
        limitPanel.add(limitField);
        buttonPanel.add(limitPanel);
        refreshButton = new JButton(refreshAction);
        buttonPanel.add(refreshButton);
        JPanel setPanel = new JPanel(new GridLayout(1,2));
        previousSet = new JButton(new AbstractAction("Previous Set") {
        	@Override
        	public void actionPerformed(ActionEvent arg0) {
            	pool.setLimit(Integer.parseInt(limitField.getText()));
        		int limit = pool.getLimit();
        		int n = pool.getCurrentMatchNumber();
        		if (n < limit) {
        			n = 0;
        		} else {
        			n = n - limit;
        		}        		
        		try {
            		pool.setCurrentMatchNumber(n);
            		pool.clearRecords();
					pool.find(displayColumns);
					setViewLabelText();
					refreshGraph();
				} catch (SQLException ex) {
					throw new RuntimeException(ex);
				} catch (SQLObjectException ex) {
					throw new RuntimeException(ex);
				}
        	}
        });
        if (pool.getLimit() == 0) {
        	previousSet.setEnabled(false);
        }
        setPanel.add(previousSet);
        nextSet = new JButton(new AbstractAction("Next Set") {
        	@Override
        	public void actionPerformed(ActionEvent arg0) {
            	pool.setLimit(Integer.parseInt(limitField.getText()));
        		int limit = pool.getLimit();
        		int n = pool.getCurrentMatchNumber();
        		n += limit;
        		try {
            		pool.setCurrentMatchNumber(n);
            		pool.clearRecords();
					pool.find(displayColumns);
					setViewLabelText();
					refreshGraph();
				} catch (SQLException ex) {
					throw new RuntimeException(ex);
				} catch (SQLObjectException ex) {
					throw new RuntimeException(ex);
				}
        	}
        });
        setPanel.add(nextSet);
        buttonPanel.add(setPanel);
        viewLabel = new JLabel();
        setViewLabelText();
        buttonPanel.add(viewLabel);
        buttonPanel.setPreferredSize(new Dimension(350,200));
        
        String prefNodePath = "ca/sqlpower/matchmaker/projectSettings/$" +
        						project.getUUID() + "/matchValidation";
        matchValidationPrefs = Preferences.userRoot().node(prefNodePath);

        try {
        	final Preferences displayColumnPrefs = matchValidationPrefs.node("displayColumns");
        	String[] keys = displayColumnPrefs.keys();
			for (String key: keys) {
				SQLColumn column = project.getSourceTable().getColumnByName(key);
				if (column != null) {
					displayColumns.add(column);
				}
			}
			Collections.sort(displayColumns, new Comparator<SQLColumn>() {
				public int compare(SQLColumn o1, SQLColumn o2) {
					int o1Index = displayColumnPrefs.getInt(o1.getName(), -1);
					int o2Index = displayColumnPrefs.getInt(o2.getName(), -1);
					if (o1Index < o2Index) return -1;
					if (o1Index > o2Index) return 1;
					return 0;
				}
			});
		} catch (BackingStoreException e) {
			logger.error("Error loading preferred display values for graph. Defaulting to primary key values", e);
			displayColumns.clear();
		}
		
		try {
			shownColumns = new ArrayList<SQLColumn>();
			final Preferences shownColumnPrefs = matchValidationPrefs.node("shownColumns");
	    	String[] keys = shownColumnPrefs.keys();
	    	
	    	if (keys.length == 0) {
	    		shownColumns = null;
	    	} else {
				for (String key: keys) {
					SQLColumn column = project.getSourceTable().getColumnByName(key);
					if (column != null) {
						shownColumns.add(column);
					}
				}
				Collections.sort(shownColumns, new Comparator<SQLColumn>() {
					public int compare(SQLColumn o1, SQLColumn o2) {
						int o1Index = shownColumnPrefs.getInt(o1.getName(), -1);
						int o2Index = shownColumnPrefs.getInt(o2.getName(), -1);
						if (o1Index < o2Index) return -1;
						if (o1Index > o2Index) return 1;
						return 0;
					}
				});
	    	}
		} catch (BackingStoreException e) {
			logger.error("Error loading preferred display columns for table. Defaulting to all columns", e);
			shownColumns = null;
		}
        
        for(PotentialMatchRecord pmr : pool.getPotentialMatchRecords()) {
        	updaterListener = new AbstractSPListener() {
        		@Override
        		public void propertyChanged(PropertyChangeEvent evt) {
        			if(evt.getPropertyName().equals("master") || evt.getPropertyName().equals("matchStatus")) {
        				graph.repaint();
        			}
        		}
        	};
        	pmr.addSPListener(updaterListener);
        }
        graphModel = new MatchPoolGraphModel(pool);
        graph = new GraphViewer<SourceTableRecord, PotentialMatchRecord>(graphModel);
        graph.setNodeRenderer(new SourceTableNodeRenderer());
        graph.setEdgeRenderer(new PotentialMatchEdgeRenderer(graph));
        graph.addSelectionListener(selectionListener);
        
        JPanel autoMatchPanel = new JPanel(new FlowLayout());
        mungeProcessComboBox = new JComboBox();
        mungeProcessComboBox.setRenderer(new MatchMakerObjectComboBoxCellRenderer());
        autoMatchButton = new JButton(new AutoMatchAction(graph.getModel(), session));
        autoMatchPanel.add(autoMatchButton);
        updateAutoMatchComboBox();
        autoMatchPanel.add(new JLabel(":"));
        autoMatchPanel.add(mungeProcessComboBox);

        pb.add(buttonPanel, cc.xy(1,1));
        limitField.setText(pool.getLimit()+"");
        pb.add(autoMatchPanel, cc.xy(1, 2));
        doAutoLayout();
        
        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.add(pb.getPanel(), BorderLayout.NORTH);
        graphPanel.add(new JScrollPane(graph), BorderLayout.CENTER);
                
        recordViewerPanel.add(SourceTableRecordViewer.getNoNodeSelectedLabel());
        final JScrollPane recordViewerScrollPane =
            new JScrollPane(recordViewerPanel,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        recordViewerScrollPane.getHorizontalScrollBar().setBlockIncrement(100);
        recordViewerScrollPane.getHorizontalScrollBar().setUnitIncrement(15);
        recordViewerScrollPane.getVerticalScrollBar().setBlockIncrement(100);
        recordViewerScrollPane.getVerticalScrollBar().setUnitIncrement(15);
        
        recordViewerScrollPane.setColumnHeaderView(recordViewerColumnHeader);
		recordViewerScrollPane.setRowHeaderView(recordViewerRowHeader);
		recordViewerScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, recordViewerCornerPanel);
		
        // put it all together
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                graphPanel,
                recordViewerScrollPane);
        
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.add(splitPane, BorderLayout.CENTER);
        super.setPanel(panel);
    }
    
    /**
     * Performs auto-layout on the visible graph of matches managed
     * by this component.
     */
    public void doAutoLayout() {
        final GraphModel<SourceTableRecord, PotentialMatchRecord> model = graph.getModel();
        final GraphNodeRenderer<SourceTableRecord> renderer = graph.getNodeRenderer();
        final int hgap = 10;
        final int vgap = 10;
        final int targetHeight = graph.getPreferredGraphLayoutHeight();
        
        ConnectedComponentFinder<SourceTableRecord, PotentialMatchRecord> ccf =
            new ConnectedComponentFinder<SourceTableRecord, PotentialMatchRecord>(new SourceTableRecordDisplayComparator());
        Set<Set<SourceTableRecord>> components = ccf.findConnectedComponents(model);
        
        int y = 0;
        int x = 0;
        int nextx = 0;
        
        for (Set<SourceTableRecord> component : components) {
            
            // lay out the nodes of this component in a circle
            double angleStep = Math.PI * 2.0 / component.size();
            double currentAngle = 0.0;
            double radius = 100.0;
            Rectangle componentBounds = null;
            Map<SourceTableRecord, Rectangle> componentNodeBounds = new HashMap<SourceTableRecord, Rectangle>();
            
            for (SourceTableRecord node : component) {
                double xx = radius * Math.cos(currentAngle);
                double yy = radius * Math.sin(currentAngle);
                
                Dimension prefSize = renderer.getGraphNodeRendererComponent(node, false, false).getPreferredSize();
                
                final Rectangle nodeBounds = new Rectangle(
                        (int) xx, (int) yy,
                        prefSize.width, prefSize.height);
                if (componentBounds == null) {
                    componentBounds = new Rectangle(nodeBounds);
                } else {
                    componentBounds.add(nodeBounds);
                }
                componentNodeBounds.put(node, nodeBounds);
                
                currentAngle += angleStep;
            }
            
            logger.debug("Nodes of a graph and their bounds are " + componentNodeBounds);
            logger.debug("Overall bounds on the above graph are " + componentBounds);
            
            // fit the laid out component under previous component, or in a
            // new column if nodes won't fit in current column
            Point translate = new Point(-componentBounds.x, -componentBounds.y);
            if (y + componentBounds.height > targetHeight) {
                y = 0;
                x = nextx + hgap;
            }
            
            for (SourceTableRecord node : component) {
                final Rectangle bounds = componentNodeBounds.get(node);
                bounds.translate(x + translate.x, y + translate.y);
                graph.setNodeBounds(node, bounds);
                nextx = Math.max(nextx, bounds.x + bounds.width);
            }
            
            y += componentBounds.height + vgap;
        }
        graph.repaint();
        graph.revalidate();
    }
    
    /**
	 * This method will return the actions allowed between the two given nodes
	 * in a graph.
	 * 
	 * @param lhs
	 *            The node that should be considered on the left for the methods
	 *            that the actions will run.
	 * @param rhs
	 *            The node that should be considered on the right for the
	 *            methods that the actions will run.
	 */
    public List<Action> getActions(SourceTableRecord lhs, SourceTableRecord rhs) {
    	List<Action> actionsAllowed = new ArrayList<Action>();
    	if (lhs == rhs) {
    		actionsAllowed.add(new SetMasterAction("Master of All", lhs, rhs));
    		actionsAllowed.add(new SetNoMatchAction("No Match to Any", lhs, rhs));
    		actionsAllowed.add(new SetUnmatchAction("Unmatch All", lhs, rhs));
    		return actionsAllowed;
    	}
    	
    	GraphModel<SourceTableRecord, PotentialMatchRecord> nonDirectedGraph =
    		new NonDirectedUserValidatedMatchPoolGraphModel(pool, new HashSet<PotentialMatchRecord>());
		BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
            new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
        Set<SourceTableRecord> reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, lhs));
        
        logger.debug("lhs record is " + lhs.toString());
        logger.debug("rhs record is " + rhs.toString());
        
        PotentialMatchRecord pmr = pool.getPotentialMatchFromOriginals(lhs, rhs);
        if (reachable.contains(rhs)) {
        	logger.debug("reachable contains rhs");
        	actionsAllowed.add(new SetUnmatchAction("Unmatch", lhs, rhs));
        	actionsAllowed.add(new SetNoMatchAction("No Match", lhs, rhs));
        	return actionsAllowed;
        }
        
        actionsAllowed.add(new SetMasterAction("Master", rhs, lhs));
        actionsAllowed.add(new SetDuplicateAction("Duplicate", lhs, rhs));
        if (pmr != null && pmr.getMatchStatus() == MatchType.NOMATCH) {
        	actionsAllowed.add(new SetUnmatchAction("Unmatch", lhs, rhs));
        } else {
        	actionsAllowed.add(new SetNoMatchAction("No Match", lhs, rhs));
        }
        	
    	return actionsAllowed;
    }

	MatchPool getPool() {
		return pool;
	}
	
	/**
	 * Updates the Auto Match combo box to remove munge processes that are not currently shown
	 */
	private void updateAutoMatchComboBox(){
		mungeProcessComboBox.removeAllItems();
		for (MungeProcess mp : project.getValidatingMungeProcesses()) {
			mungeProcessComboBox.addItem(mp);
		}
		mungeProcessComboBox.setEnabled(mungeProcessComboBox.getItemCount() > 0);
		autoMatchButton.setEnabled(mungeProcessComboBox.getItemCount() > 0);
	}
	
	/**
	 * Whenever we load a new set of matches in the visualizer we need to call this.
	 */
	private void refreshGraph() {
		graph.setLayoutCache(new DefaultGraphLayoutCache<SourceTableRecord, PotentialMatchRecord>());
		doAutoLayout();
        for(PotentialMatchRecord pmr : pool.getPotentialMatchRecords()) {
        	updaterListener = new AbstractSPListener() {
        		@Override
        		public void propertyChanged(PropertyChangeEvent evt) {
        			if(evt.getPropertyName().equals("master") || evt.getPropertyName().equals("matchStatus")) {
        				graph.repaint();
        			}
        		}
        	};
        	pmr.addSPListener(updaterListener);
        }
		graph.repaint();
	}
	
	private void setViewLabelText() {
		int viewTo = (pool.getCurrentMatchNumber() + pool.getLimit());
		int cmn = pool.getCurrentMatchNumber();
		if (pool.getClusterCount() == 0) {
			nextSet.setEnabled(false);
			previousSet.setEnabled(false);
		} else if (viewTo >= pool.getClusterCount()) {
			viewTo = pool.getClusterCount();
			nextSet.setEnabled(false);
			previousSet.setEnabled(true);
		} else if (cmn == 0) {
			nextSet.setEnabled(true);
			previousSet.setEnabled(false);
		} else {
			previousSet.setEnabled(true);
			nextSet.setEnabled(true);
		}
		
		viewLabel.setText("Viewing " + ((cmn == 0) ? 0 : (cmn + 1)) + " to " +
        		 viewTo + " out of " +
        		pool.getClusterCount());
	}
	
	/**
	 * Updates the match table based on shownColumns and the current selectedNode.
	 */
	private void updateMatchTable() {
		try {
            recordViewerPanel.removeAll();
            recordViewerColumnHeader.removeAll();
            recordViewerRowHeader.removeAll();
            recordViewerCornerPanel.removeAll();
            
            JButton displayColumnButton = new JButton(chooseDisplayedColumnAction);
            recordViewerCornerPanel.add(displayColumnButton);
            
            // if there's no node selected, then there will not be any table
            if (selectedNode == null) {
            	recordViewerPanel.add(SourceTableRecordViewer.getNoNodeSelectedLabel());
            }
            // if none of the columns are shown, then there will be no table
            else if (shownColumns != null && shownColumns.isEmpty()) {
            	recordViewerPanel.add(SourceTableRecordViewer.getNoColumnSelectedLabel());
            } else {
            	// if shownColumnes is null, then all the columns will be shown
            	JPanel headerPanel = SourceTableRecordViewer.headerPanel(project, shownColumns);
            	headerPanel.setPreferredSize(new Dimension(Math.max(displayColumnButton.getPreferredSize().width, 
            			headerPanel.getPreferredSize().width) , headerPanel.getPreferredSize().height));
            	recordViewerRowHeader.add(headerPanel);
            	BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
            		new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
            	bfs.setComparator(new SourceTableRecordsComparator(selectedNode));
            	List<SourceTableRecord> reachableNodes = bfs.performSearch(graphModel, selectedNode);
            	for (SourceTableRecord rec : reachableNodes) {
            		if (rec.equals(selectedNode)) {
            			// if shownColumnes is null, then all the columns will be shown
            			
            			SourceTableRecordViewer recordViewer = 
            				new SourceTableRecordViewer(
            						rec, selectedNode, getActions(selectedNode, rec), 
            						shownColumns, RECORD_VIEWER_ROW_HEADER_LAYOUT_PADDING);
            			JToolBar toolBar = recordViewer.getToolBar();
            			EmptyBorder emptyBorder = new EmptyBorder(0, headerPanel.getPreferredSize().width
            					+ RECORD_VIEWER_ROW_HEADER_LAYOUT_PADDING - displayColumnButton.getPreferredSize().width, 
            					0, 0);
            			toolBar.setBorder(emptyBorder);
            			recordViewerCornerPanel.add(toolBar);
            			recordViewerRowHeader.add(recordViewer.getPanel());
            		} else {
            			final SourceTableRecord str = rec;
            			// if shownColumnes is null, then all the columns will be shown
            			SourceTableRecordViewer recordViewer = 
            				new SourceTableRecordViewer(
            						str, selectedNode, getActions(selectedNode, str), 
            						shownColumns, RECORD_VIEWER_ROW_HEADER_LAYOUT_PADDING);
            			recordViewer.getPanel().addMouseListener(new MouseAdapter() {
            				@Override
            				public void mousePressed(MouseEvent e) {
            					if (SwingUtilities.isLeftMouseButton(e)){
            						if (e.getClickCount() == 1) {
            							graph.setFocusedNode(str);
            							graph.scrollNodeToVisible(str);
            						} else if (e.getClickCount() == 2) {
            							graph.setSelectedNode(str);
            							graph.scrollNodeToVisible(str);
            						}
            					}
            				}
            			});
            			recordViewerPanel.add(recordViewer.getPanel());
            			recordViewerColumnHeader.add(recordViewer.getToolBar());
            		}
            	}
            }
		}catch (Exception ex) {
			MMSUtils.showExceptionDialog(getPanel(), "Couldn't show potential matches", ex);
		}
		recordViewerPanel.revalidate();
		recordViewerColumnHeader.revalidate();
		recordViewerRowHeader.revalidate();
		recordViewerCornerPanel.revalidate();
	}

	/**
	 * A comparator that compares SourceTableRecords based on match status and match priority.
	 */
	private class SourceTableRecordsComparator implements Comparator<SourceTableRecord> {
		private SourceTableRecord master;
		private MatchTypeComparator matchTypeComp;
		
		/**
		 *	The master is the basis for comparison, mostly just used
		 *	to get the match priority. 
		 */
		public SourceTableRecordsComparator(SourceTableRecord master) {
			this.master = master;
			matchTypeComp = new MatchTypeComparator();
		}
		
		public int compare(SourceTableRecord o1, SourceTableRecord o2) {
			// Assumes the basis as the smallest
			if (o1 == master) {
				return -1;
			} else if (o2 == master) {
				return 1;
			}
			
			// Finds the "highest" match status related to each SourceTableRecords
			List<PotentialMatchRecord> pmrs1 = new ArrayList<PotentialMatchRecord>(o1.getOriginalMatchEdges());
			MatchType t1 = MatchType.UNMATCH;
			for (PotentialMatchRecord pmr : pmrs1) {
				if (matchTypeComp.compare(pmr.getMatchStatus(), t1) > 0) {
					t1 = pmr.getMatchStatus();
				}
			}
			List<PotentialMatchRecord> pmrs2 = new ArrayList<PotentialMatchRecord>(o2.getOriginalMatchEdges());
			MatchType t2 = MatchType.UNMATCH;
			for (PotentialMatchRecord pmr : pmrs2) {
				if (matchTypeComp.compare(pmr.getMatchStatus(), t2) > 0) {
					t2 = pmr.getMatchStatus();
				}
			}
			
			// Compares the SourceTableRecords based on the higher match status
			if (matchTypeComp.compare(t1, t2) != 0) {
				return matchTypeComp.compare(t1, t2);
			}
			
			// Assumes any SourceTableRecords not directly adjacent to the master to be larger
			PotentialMatchRecord pmr1 = o1.getMatchRecordByOriginalAdjacentSourceTableRecord(master);
			PotentialMatchRecord pmr2 = o2.getMatchRecordByOriginalAdjacentSourceTableRecord(master);
			if (pmr1 == null) {
				return 1;
			} else if (pmr2 == null) {
				return -1;
			}
			
			// Compares based on the match priority of the corresponding munge process 
			int percent1 = 0;
			if (pmr1.getMungeProcess().getMatchPriority() != null) {
				percent1 = pmr1.getMungeProcess().getMatchPriority().shortValue();
			}
			int percent2 = 0;
			if (pmr2.getMungeProcess().getMatchPriority() != null) {
				percent2 = pmr2.getMungeProcess().getMatchPriority().shortValue();
			}
			return percent1 - percent2;
		}
		
		/**
		 * A comparator that compares MatchTypes in the following order:
		 * UNMATCH < AUTOMATCH < MATCH < NOMATCH < MERGED
		 */
		private class MatchTypeComparator implements Comparator<MatchType> {
			
			private List<MatchType> types = new ArrayList<MatchType>();
			
			public MatchTypeComparator() {
				types.add(MatchType.UNMATCH);
				types.add(MatchType.AUTOMATCH);
				types.add(MatchType.MATCH);
				types.add(MatchType.NOMATCH);
				types.add(MatchType.MERGED);
			}
			
			public int compare(MatchType o1, MatchType o2) {
				return types.indexOf(o1) - types.indexOf(o2);
			}			
		}
	}

	@Override
	public void undoEventFired(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: AbstractUndoableEditorPane<MatchPool>.undoEventFired()");
		
	}
}