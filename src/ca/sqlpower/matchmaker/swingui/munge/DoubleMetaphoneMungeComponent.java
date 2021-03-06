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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.DoubleMetaphoneMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This is the component for a double metaphone munge step. It has only one option,
 * a checkbox to decide whether to use regular expressions.
 */
public class DoubleMetaphoneMungeComponent extends AbstractMungeComponent {

	private JCheckBox useAlt;
	
	public DoubleMetaphoneMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler, session);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		useAlt = new JCheckBox("Use Alternate Encoding");
		final DoubleMetaphoneMungeStep temp = (DoubleMetaphoneMungeStep) getStep();
		useAlt.setSelected(temp.isUseAlternate());
		useAlt.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				temp.setUseAlternate(useAlt.isSelected());
			}
			
		});
		content.add(useAlt);
		return content;
	}
}
