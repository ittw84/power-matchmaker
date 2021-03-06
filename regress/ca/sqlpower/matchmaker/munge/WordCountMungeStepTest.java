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

import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTestCase;
import ca.sqlpower.object.SPObject;

public class WordCountMungeStepTest extends MatchMakerTestCase<WordCountMungeStep> {

	WordCountMungeStep step;
	
	private MungeStepOutput testInput;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		step = new WordCountMungeStep();
		super.setUp();
        MungeProcess process = (MungeProcess) createNewValueMaker(
        		getRootObject(), null).makeNewValue(
        				MungeProcess.class, null, "parent process");
        process.addTransformationMungeStep(step);
	}
	
	public WordCountMungeStepTest(String name) {
		super(name);
	}
	
	@Override
	protected WordCountMungeStep getTarget() {
		return step;
	}

	public void testCallonNoOccurrence() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcdefg");
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		MungeStepOutput output = results.get(0);
		BigDecimal result = (BigDecimal)output.getData();
		assertEquals(1, result.intValue());
	}

	public void testCallonMultipleOccurrences() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("a b c d e f g");
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		MungeStepOutput output = results.get(0);
		BigDecimal result = (BigDecimal)output.getData();
		assertEquals(7, result.intValue());
	}

	public void testCallonMixedDelimiters() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("a   b \nc d\n e f\n\ng");
		step.setDelimiter(" \n");
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		MungeStepOutput output = results.get(0);
		BigDecimal result = (BigDecimal)output.getData();
		assertEquals(7, result.intValue());
	}
	
	/**
	 * Tests for a previous bug where special regex characters would
	 * not be taken in as literals even if regex was turned off.
	 */
	public void testCallonRegexInput() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("a()[]{}|$^<=b()\\-+*?c");
		step.setDelimiter("\\-+*?()[]{}|$^<=");
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		MungeStepOutput output = results.get(0);
		BigDecimal result = (BigDecimal)output.getData();
		assertEquals(3, result.intValue());
	}
	
	/**
	 * This tests the case where the regular expression option is enabled.
	 */
	public void testCallonUsingRegex() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("aaaaab");
		step.setRegex(true);
		step.setDelimiter("a+");
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		MungeStepOutput output = results.get(0);
		BigDecimal result = (BigDecimal)output.getData();
		assertEquals(2, result.intValue());
	}
	
	public void testCallonNull() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData(null);
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		MungeStepOutput output = results.get(0);
		BigDecimal result = (BigDecimal)output.getData();
		assertEquals(0, result.intValue());
	}
	
	public void testCallonInteger() throws Exception {
		testInput = new MungeStepOutput<Integer>("test", Integer.class);
		testInput.setData(new Integer(1));
		try {
			step.connectInput(0, testInput);
			fail("UnexpectedDataTypeException was not thrown as expected");
		} catch (UnexpectedDataTypeException ex) {
			// UnexpectedDataTypeException was thrown as expected
		}
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return MungeStepOutput.class;
	}
	
	@Override
	public void testAllowedChildTypesField() throws Exception {
		// no-op
	}
}
