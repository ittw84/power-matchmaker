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

import java.util.concurrent.Callable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.sqlpower.sql.DatabaseObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.util.Monitorable;

/**
 * The matchmaker engine interface is a generic way of controlling a
 * process which does some time-consuming operation on a Match.  Currently,
 * there are two major implementations: The Match Engine and Merge Engine.
 */
public interface MatchMakerEngine extends Monitorable,
	Callable<EngineInvocationResult>, DatabaseObject {

	public interface EngineMode{};
	
	/**
	 * Starts the engine.  This method returns once the engine run has
	 * completed, so if you're running the engine within a Swing GUI, it
	 * is almost always necessary to call this method on a separate worker
	 * thread.
	 *   
	 * @throws EngineSettingException When the preconditions for running the
	 * engine are not met.
     * @throws SourceTableException If there was a change in the source table that 
     * could cause problems for running the engine.
	 */
	public EngineInvocationResult call() throws EngineSettingException, SourceTableException;
	
	/**
	 * Makes an effort to verify all the assumptions that the engine makes
     * about the local environment and the remote database(s) are valid.
     * Throws an exception if the engine's preconditions are not currently
     * fulfilled.
     * 
	 * @throws EngineSettingException If there is a precondition to running
     * the engine which is not currently met.
	 * @throws SQLObjectException If there are errors encountered while attempting
     * to check the preconditions (this is a more severe case than a precondition
     * failure, because it means there's something wrong with the MatchMaker too).
     * @throws SourceTableException If there was a change in the source table that 
     * could cause problems for running the engine.
	 */
	public void checkPreconditions() throws EngineSettingException, SQLObjectException, SourceTableException;
	
	/**
	 * Creates the command line to run the match engine, based on the
	 * current engine settings for the appropriate engine. 
	 * 
	 * @return The command line for running the engine.
	 */
	public String createCommandLine();

	/**
	 * Returns the logger instance that all engine messages are logged to.  Engine messages
	 * are logged at any combination of the standard Log4J logging levels.
	 */
	public Logger getLogger();
	
	/**
	 * Return the level at which to log the engine progress
	 */
	public Level getMessageLevel();
	
	/**
	 * Sets the level at which to log the engine progress
	 */
	public void setMessageLevel(Level lev);
	
	/**
	 * Adds an {@link EngineListener} to listen to this {@link MatchMakerEngine}
	 * 
	 * @param listener
	 *            The {@link EngineListener} that will listen to this
	 *            {@link MatchMakerEngine}.
	 */
	public void addEngineListener(EngineListener listener);
	
	/**
	 * Removes an {@link EngineListener} to listen to this
	 * {@link MatchMakerEngine}
	 * 
	 * @param listener
	 *            The {@link EngineListener} to remove from this
	 *            {@link MatchMakerEngine}.
	 */
	public void removeEngineListener(EngineListener listener);
	
	/**
	 * Fires an event to all {@link EngineListener}s notifying that this
	 * {@link MatchMakerEngine} has started.
	 */
	public void fireEngineStarted();
	
	/**
	 * Fires an event to all {@link EngineListener}s notifying that this
	 * {@link MatchMakerEngine} has stopped.
	 */
	public void fireEngineStopped();
}