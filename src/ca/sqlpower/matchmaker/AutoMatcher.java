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

package ca.sqlpower.matchmaker;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.graph.BreadthFirstSearch;
import ca.sqlpower.graph.GraphModel;
import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.graph.NonDirectedUserValidatedMatchPoolGraphModel;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.util.MonitorableImpl;

/**
 * A worker that can perform the "auto match" operation on any munge process's
 * potential match records of a particular match pool. The goal of auto-match is
 * to ensure every source table record incident on a potential match record of a
 * certain munge process is a master or a duplicate. Auto-match only affects
 * potential match records of one specific munge process, but it does affect
 * most of them in one shot. There is no way to control which source table
 * record becomes the master and which is the duplicate.
 */
public class AutoMatcher extends MonitorableImpl {

	private static final Logger logger = Logger.getLogger(AutoMatcher.class);
	
	private MatchPool pool;

	public AutoMatcher(MatchPool pool) {
		this.pool = pool;
	}
	
	/**
	 * Acquires the project's engine lock, blocking until it is available, and then
	 * processes the match pool to ensure every source table record that is incident
	 * on a potential match record of the given munge process is either a master or
	 * a duplicate. When this operation is over, the engine lock will be released.
	 */
	public void doAutoMatch(MungeProcess mungeProcess) throws SQLException, SQLObjectException, InterruptedException {
	    final Project project = mungeProcess.getParent();
	    project.acquireEngineLock(this);
	    try {
	        actuallyDoAutoMatch(mungeProcess);
	    } finally {
	        project.releaseEngineLock(this);
	        setFinished(true);
            checkCancelled();
	    }
	}

    /**
     * Actually performs the auto matching process. This method must only be
     * called from {@link #doAutoMatch(MungeProcess, Aborter)}, which will have
     * acquired the project's engine lock.
     * 
     * @param mungeProcess
     *            The munge process of this project to auto-match
     */
	private void actuallyDoAutoMatch(MungeProcess mungeProcess) throws SQLException, SQLObjectException {
	    Set<SourceTableRecord> visited = new HashSet<SourceTableRecord>();
	    try {
	        setStarted(true);
	        setJobSize(pool.getAllSourceTableRecords().size());
	        setFinished(false);
	        if (mungeProcess == null) {
	            throw new IllegalArgumentException("Auto-Match invoked with an " +
	            "invalid munge process");
	        }

	        Collection<SourceTableRecord> records = pool.getAllSourceTableRecords();

	        logger.debug("Auto-Matching with " + records.size() + " records.");

	        if (records.isEmpty()) {
	            return;
	        }

	        SourceTableRecord selected = null;
	        for (SourceTableRecord record : records) {
	            boolean addToVisited = true;
	            for (PotentialMatchRecord pmr : record.getOriginalMatchEdges()) {
	                if (pmr.getMatchStatus() != MatchType.NOMATCH
	                        && pmr.getMungeProcess() == mungeProcess) {
	                    addToVisited = false;
	                }
	            }
	            if (addToVisited) {
	                visited.add(record);
	            } else {
	                // rather than iterating through all the records again, looking
	                // for one that isn't in visited...
	                selected = record;
	            }
	            setProgress(visited.size());
	            checkCancelled();
	        }

	        logger.debug("The size of visited is " + visited.size());

	        Set<SourceTableRecord> neighbours = findAutoMatchNeighbours(mungeProcess, selected, visited);
	        makeAutoMatches(mungeProcess, selected, neighbours, visited);
	        //If we haven't visited all the nodes, we are not done!
	        while (visited.size() != records.size()) {
	            checkCancelled();
	            SourceTableRecord temp = null;
	            for (SourceTableRecord record : records) {
	                if (!visited.contains(record)) {
	                    temp = record;
	                    break;
	                }
	            }
	            neighbours = findAutoMatchNeighbours(mungeProcess, temp, visited);
	            makeAutoMatches(mungeProcess, temp, neighbours, visited);
	        }
	    } finally {
	        setFinished(true);
	        setProgress(visited.size());
	        checkCancelled();
	    }
	}

	/**
	 * Creates the matches necessary in an auto-match while maintaining the
	 * 'visited' set and propagating the algorithm to neighbours of selected
	 * nodes.
	 */
	private void makeAutoMatches(MungeProcess mungeProcess,
			SourceTableRecord selected,
			Set<SourceTableRecord> neighbours,
			Set<SourceTableRecord> visited) throws SQLException, SQLObjectException {
		logger.debug("makeAutoMatches called, selected's key values = " + selected.getKeyValues());
		visited.add(selected);
		GraphModel<SourceTableRecord, PotentialMatchRecord> nonDirectedGraph =
			new NonDirectedUserValidatedMatchPoolGraphModel(pool, new HashSet<PotentialMatchRecord>());
		BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord> bfs =
			new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
		Set<SourceTableRecord> reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, selected));
		Set<SourceTableRecord> noMatchNodes = pool.findNoMatchNodes(reachable);
		for (SourceTableRecord record : neighbours) {
			if (!noMatchNodes.contains(record)) {
				pool.defineMaster(selected, record, true);
				nonDirectedGraph = new NonDirectedUserValidatedMatchPoolGraphModel(pool, new HashSet<PotentialMatchRecord>());
				bfs = new BreadthFirstSearch<SourceTableRecord, PotentialMatchRecord>();
				reachable = new HashSet<SourceTableRecord>(bfs.performSearch(nonDirectedGraph, selected));
				noMatchNodes = pool.findNoMatchNodes(reachable);
			}
			setProgress(visited.size());
			checkCancelled();
		}
		for (SourceTableRecord record : neighbours) {
			if (!visited.contains(record)) {
				makeAutoMatches(mungeProcess, record, findAutoMatchNeighbours(mungeProcess, record, visited), visited);
			}
            setProgress(visited.size());
            checkCancelled();
		}
	}

	/**
	 * Finds all the neighbours that auto-match worries about as explained in
	 * the comment for doAutoMatch in the context that 'record' is selected in
	 * step 3
	 */
	private Set<SourceTableRecord> findAutoMatchNeighbours(MungeProcess mungeProcess,
			SourceTableRecord record,
			Set<SourceTableRecord> visited) {
		logger.debug("The size of visited is " + visited.size());
		Set<SourceTableRecord> ret = new HashSet<SourceTableRecord>();
		for (PotentialMatchRecord pmr : record.getOriginalMatchEdges()) {
			if (pmr.getMungeProcess() == mungeProcess 
					&& pmr.getMatchStatus() != MatchType.NOMATCH) {
				if (record == pmr.getOrigLHS() && !visited.contains(pmr.getOrigRHS())) {
					ret.add(pmr.getOrigRHS());
				} else if (record == pmr.getOrigRHS() && !visited.contains(pmr.getOrigLHS())) {
					ret.add(pmr.getOrigLHS());
				}
			}
			setProgress(visited.size());
			checkCancelled();
		}
		logger.debug("findAutoMatchNeighbours: The neighbours to automatch for " + record + " are " + ret);
		return ret;
	}
	
	public String getMessage() {
		return null;
	}

}
