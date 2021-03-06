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

package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.TranslateWordsEditor;

/**
 * A simple action to adds a new translate group to the swing session and
 * opens up the editor for the new translate group.
 */
public class NewTranslateGroupAction extends AbstractAction {
    
    private final MatchMakerSwingSession swingSession;

	public NewTranslateGroupAction(MatchMakerSwingSession swingSession) {
	    super("New Translate Group");
        this.swingSession = swingSession;
	}
	
	public void actionPerformed(ActionEvent e) {
		MatchMakerTranslateGroup tg = new MatchMakerTranslateGroup();
		swingSession.setCurrentEditorComponent(new TranslateWordsEditor(swingSession, tg));
	}

}
