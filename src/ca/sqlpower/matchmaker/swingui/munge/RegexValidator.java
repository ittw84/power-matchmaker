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

package ca.sqlpower.matchmaker.swingui.munge;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Action;

import org.apache.log4j.Logger;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * This validator is used to check the regex in the MungeComponent.
 * It gets the translations from the model and adds it to the list.  The list
 * is then put into a Set.  If the set and list size are not equal, this indicates
 * that there is a duplicate translations in which case the status becomes fail.
 */
public class RegexValidator implements Validator {

    private static final Logger logger = Logger.getLogger(RegexValidator.class);

    private List<Action> actions;
    
    public RegexValidator(List<Action> actions){
        this.actions = actions;
    }
    
    public ValidateResult validate(Object contents) {
    	try {
    		String regex = (String) contents;
    		Pattern p = Pattern.compile(regex);
    	} catch (PatternSyntaxException e) {
    		setComponentsEnabled(false);
    		return ValidateResult.createValidateResult(Status.FAIL, 
    				"The given regular expression is invalid.");
    	}

        setComponentsEnabled(true);
        return ValidateResult.createValidateResult(Status.OK, "");
    }
    
    private void setComponentsEnabled(boolean enable){
        if (actions != null){
            for (Action c : actions){
            	logger.debug(c + ".enable(" + enable);
                c.setEnabled(enable);
            }
        }
    }
}