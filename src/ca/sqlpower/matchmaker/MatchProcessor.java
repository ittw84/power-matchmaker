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

package ca.sqlpower.matchmaker;

import java.util.Collections;
import java.util.List;

import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeResult;

/**
 * A Processor which takes a List of arrays of MungeStepOutputs, which it would
 * typically get from a MungeProcess, and performs matching on the data, and stores
 * the match results into the match repository.
 */
public class MatchProcessor extends AbstractProcessor {

	/**
	 * A list of Munged data that the MatchProcessor would take from a MungeProcessor
	 */
	private List<MungeResult> matchData;
	
	/**
	 * The MungeProces that this MatchProcess is processing
	 */
	private MungeProcess mungeProcess;
	
	/**
	 * The MatchPool that will be used to store the PotentialMatchRecords
	 * and SourceTableRecords that will be marked as the potential matches
	 */
	private MatchPool pool;
	
	public MatchProcessor(MungeProcess process, List<MungeResult> matchData) {
		this.matchData = matchData;
		pool = new MatchPool(process.getParentMatch());
		this.mungeProcess = process; 
	}
	
	public Boolean call() throws Exception {
		
		Collections.sort(matchData);
		
		for (MungeResult data: matchData) {
			int dataIndex = matchData.indexOf(data);
			
			for (int i=dataIndex + 1; i<matchData.size(); i++){
				if (data.compareTo(matchData.get(i)) == 0) {
					// Potential Match! so store in Match Result Table
					PotentialMatchRecord pmr = new PotentialMatchRecord(mungeProcess, 
																		MatchType.UNMATCH, 
																		data.getSourceTableRecord(), 
																		matchData.get(i).getSourceTableRecord(),
																		false);
					pool.addSourceTableRecord(data.getSourceTableRecord());
					pool.addSourceTableRecord(matchData.get(i).getSourceTableRecord());
					pool.addPotentialMatch(pmr);
				}
			}
		}
		
		pool.store();
		
		return Boolean.TRUE;
	}
}