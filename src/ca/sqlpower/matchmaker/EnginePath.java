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

/**
 * A program to be looked up by ExternalProgramUtils
 */
public enum EnginePath {

	POWERLOADER("ploader_NOT_RIGHT_YET"),
	MATCHMAKER("Match_ODBC"),
	MERGEENGINE("Merge_ODBC"),
	EMAILNOTIFICATION("Mailsender_ODBC");

	private String progName;

	private EnginePath(String path) {
		this.progName = path;
	}

	public String getProgName() {
		boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("windows")>=0;
		return isWindows ? progName + ".exe" : progName;
	}
}