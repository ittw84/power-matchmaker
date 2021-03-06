/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This class attaches listeners to a model object and a UI field to keep the
 * two in sync and notify the user if they become out of sync.
 */
public abstract class AbstractUIUpdateManager {
	
	private static Logger logger = Logger.getLogger(AbstractUIUpdateManager.class);

	/**
	 * A listener on spo for property propertyName. If the property changes,
	 * update the component or notify the user that there is a conflict.
	 */
	protected final SPListener propertyChangeListener = new AbstractSPListener() {
		public void propertyChanged(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(propertyName) &&
					!updateUI(evt)) {
				modelAndUiInSync = false;
				handler.performFormValidation();
				refreshButton.setVisible(true);
			}
		};
	};
	
	private boolean modelAndUiInSync = true;

	/**
	 * This listener will be attached to the text component. When the text
	 * component takes focus a timer listener will be added to apply changes
	 * after the user has stopped making modifications for several seconds. When
	 * focus is lost the property will also be persisted.
	 */
	private final FocusListener focusListener = new FocusListener() {
		
		@Override
		public void focusLost(FocusEvent e) {
			if (!handler.getWorstValidationStatus().getStatus().equals(Status.FAIL)) {
				dep.applyChanges();
			}
		}
		
		@Override
		public void focusGained(FocusEvent e) {
			//TODO attach a timer listener that will apply the changes after a set amount of seconds of inactivity.
		}
	};

	/**
	 * If this property changes on the SPObject passed to the constructor the
	 * data entry panel may become invalid because its state is inconsistent
	 * with its model.
	 */
	private final String propertyName;
	
	/**
	 * The object we are monitoring for changes to update the UI with.
	 */
	private final SPObject spo;

	/**
	 * This button is used to refresh the UI based on the model. If set this
	 * refresh button will be made visible when the UI and model are out of sync.
	 * If null then no button's visibility will change when the UI and model
	 * come out of sync.
	 */
	private final JButton refreshButton;

	private final FormValidationHandler handler;

	private final DataEntryPanel dep;

	public AbstractUIUpdateManager(JComponent ui,
			SPObject spo, 
			final String propertyName,
			FormValidationHandler handler, 
			DataEntryPanel dep, 
			JButton refreshButton) {
		this.spo = spo;
		this.propertyName = propertyName;
		this.handler = handler;
		this.refreshButton = refreshButton;
		this.dep = dep;
		spo.addSPListener(propertyChangeListener);
		ui.addFocusListener(focusListener);
		setValidator(ui);
	}
	
	protected void setValidator(JComponent comp) {

		handler.addValidateObject(comp, new Validator() {
			
			@Override
			public ValidateResult validate(Object contents) {
				if (!modelAndUiInSync) {
					return ValidateResult.createValidateResult(Status.FAIL, 
							"The " + propertyName + " has been updated and conflicts with local changes.");
				}
				return ValidateResult.createValidateResult(Status.OK, "");
			}
		});
	}
	
	/**
	 * Call this method to clear the warnings that the UI and model are out of sync.
	 */
	public void clearWarnings() {
		modelAndUiInSync = true;
	}
	
	/**
	 * Call this method when the UI is being disposed to correctly remove listeners.
	 */
	public void cleanup() {
		spo.removeSPListener(propertyChangeListener);
	}

	/**
	 * This method will update the UI according to the new value in the event.
	 * If the UI is updated correctly then this method will return true. If the
	 * UI cannot be updated due to changes made by the user that are not saved
	 * this method will return false and an error message will be displayed to
	 * the user.
	 * 
	 * @param evt
	 *            The event that changed the model object that the UI component
	 *            must now update to.
	 * @return True if the UI was successfully updated. False if the UI could
	 *         not be updated.
	 */
	protected abstract boolean updateUI(PropertyChangeEvent evt);
}
