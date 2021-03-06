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

package ca.sqlpower.matchmaker.munge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Constructor;



/**
 * A step which outputs <code>null</code> when the input value is
 * <code>null</code> or the empty string, and passes all other input values
 * through intact. The main use for this step is to prevent large numbers of
 * useless matches: during match evaluation, empty strings are considered equal
 * with each other, but nulls are not.
 * <p>
 * This step has one input and one output, each of type String.
 */
public class EmptyStringToNullMungeStep extends AbstractMungeStep {

	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
	
	@Constructor
    public EmptyStringToNullMungeStep() {
        super("Empty String to Null", false);
    }

	public void init() {
		MungeStepOutput<String> out = new MungeStepOutput<String>(
				"emptyStringToNullOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("inputString",
				String.class);
		super.addInput(desc);
	}

    @Override
    public Boolean doCall() throws Exception {
        MungeStepOutput<String> in = getMSOInputs().get(0);
        MungeStepOutput<String> out = getOut();
        
        String str = in.getData();
        if (str == null || str.length() == 0) {
            out.setData(null);
        } else {
            out.setData(str);
        }
        return Boolean.TRUE;
    }
}
