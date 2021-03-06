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

import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;

/**
 * Test for the Munge Processor itself.  Here's the setup:
 *
 * <pre>
 *         +---A---+          Notes: A has one output, which is
 *         |   |   |                 connected to B, C, and F
 *         v   |   v
 *         B   |   C---+   D         C has two outputs.
 *         |   |   |   |             Output 0 is connected to F
 *         v   v   |   v             Output 1 is connected to G
 *         E   F&lt;--+   G
 *         |   |
 *         v   |					 R is the Result Step
 *         R&lt;--+					 with Inputs from E and F
 * </pre>
 * <p>
 * Note, this setup was originally identical to the setup in the
 * MungeProcessGraphModelTest, but in order to allow modification
 * of this setup for one test case or the other, we elected to copy
 * the setup rather than factor it out, where a modification could
 * disturb all the test cases sharing it.
 */
public class MungeProcessorTest extends TestCase {

    /**
     * The munge processor under test.  It will be set up to have
     * all MungeStep objects a-g as its children, and those
     * children will be connected according to the diagram in the
     * class-level comment of this test case.
     */
    MungeProcessor mp;
    
    /**
     * Step A as shown in the diagram in the class comment.
     */
    private TestingMungeStep a;
    
    /**
     * Step B as shown in the diagram in the class comment.
     */
    private TestingMungeStep b;
    
    /**
     * Step C as shown in the diagram in the class comment.
     */
    private TestingMungeStep c;
    
    /**
     * Step D as shown in the diagram in the class comment.
     */
    private TestingMungeStep d;
    
    /**
     * Step E as shown in the diagram in the class comment.
     */
    private TestingMungeStep e;
    
    /**
     * Step F as shown in the diagram in the class comment.
     */
    private TestingMungeStep f;
    
    /**
     * Step G as shown in the diagram in the class comment.
     */
    private TestingMungeStep g;
    
    /**
	 * Step R as shown in the diagram in the class comment.
	 */
    private TestingResultMungeStep r;
    
    private final Logger logger = Logger.getLogger("testLogger");
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MungeProcess mungeProcess = new MungeProcess();
        mungeProcess.setParent(new Project());

        a = new TestingMungeStep("A", 0, 1);

        b = new TestingMungeStep("B", 1, 1);
        b.connectInput(0, a.getMungeStepOutputs().get(0));

        c = new TestingMungeStep("C", 1, 2);
        c.connectInput(0, a.getMungeStepOutputs().get(0));

        d = new TestingMungeStep("D", 1, 1);

        e = new TestingMungeStep("E", 1, 1);
        e.connectInput(0, b.getMungeStepOutputs().get(0));

        f = new TestingMungeStep("F", 3, 1);
        f.connectInput(0, a.getMungeStepOutputs().get(0));
        // purposely leaving input 1 not connected (for testing)
        f.connectInput(2, c.getMungeStepOutputs().get(0));

        g = new TestingMungeStep("G", 1, 1);
        g.connectInput(0, c.getMungeStepOutputs().get(1));
        
        r = new TestingResultMungeStep("R", 2);
        r.connectInput(0, e.getMungeStepOutputs().get(0));
        r.connectInput(1, f.getMungeStepOutputs().get(0));
        
        // The order in which MungeSteps are added should be
        // randomized to ensure the processor isn't just processing
        // the steps in the order in which it was given.
        mungeProcess.addChild(a);
        mungeProcess.addChild(g);
        mungeProcess.addChild(b);
        mungeProcess.addChild(f);
        mungeProcess.addChild(c);
        mungeProcess.addChild(e);
        mungeProcess.addChild(d);
        mungeProcess.addChild(r);
        
        mp = new MungeProcessor(mungeProcess, logger);
    }
    
    /**
     * Tests to ensure that the MungeSteps are being processed in a valid order.
     * The idea is that no MungeStep should be called before its parents, if any.
     */
    public void testCorrectProcessingOrder() throws Exception {
    	mp.determineProcessOrder();
        List<MungeStep> processOrder = mp.getProcessOrder();

        for (MungeStep step: processOrder) {
        	List<MungeStepOutput> stepInputs = step.getMSOInputs();
        	
        	for (MungeStepOutput output: stepInputs) {
        		if (output != null) {
        			// Check if each parent step would be called before the current
        			Boolean called = output.getParent().isExpanded();
        			assertTrue(called);
        		}
        	}
        	// mark the current step as 'called'
        	step.setExpanded(true);
        }
    }
    
    public void testRollbackOnError() throws Exception {
        d.setExceptionOnCall(true);
        try {
            mp.call();
            fail("Call should have thrown an exception");
        } catch (Exception ex) {
            // expected because d will fail
        }
        assertTrue(a.isRolledBack());
        assertTrue(b.isRolledBack());
        assertTrue(c.isRolledBack());
        assertTrue(d.isRolledBack());
        assertTrue(e.isRolledBack());
        assertTrue(f.isRolledBack());
        assertTrue(g.isRolledBack());
        assertTrue(r.isRolledBack());
    }

    public void testNoCommitOnError() throws Exception {
        d.setExceptionOnCall(true);
        try {
            mp.call();
            fail("Call should have thrown an exception");
        } catch (Exception ex) {
            // expected because d will fail
        }
        assertFalse(a.isCommitted());
        assertFalse(b.isCommitted());
        assertFalse(c.isCommitted());
        assertFalse(d.isCommitted());
        assertFalse(e.isCommitted());
        assertFalse(f.isCommitted());
        assertFalse(g.isCommitted());
        assertFalse(r.isCommitted());
    }

    public void testCommitOnSuccess() throws Exception {
        mp.call();
        assertTrue(a.isCommitted());
        assertTrue(b.isCommitted());
        assertTrue(c.isCommitted());
        assertTrue(d.isCommitted());
        assertTrue(e.isCommitted());
        assertTrue(f.isCommitted());
        assertTrue(g.isCommitted());
        assertTrue(r.isCommitted());
    }

    public void testNoRollbackOnSuccess() throws Exception {
        mp.call();
        assertFalse(a.isRolledBack());
        assertFalse(b.isRolledBack());
        assertFalse(c.isRolledBack());
        assertFalse(d.isRolledBack());
        assertFalse(e.isRolledBack());
        assertFalse(f.isRolledBack());
        assertFalse(g.isRolledBack());
        assertFalse(r.isRolledBack());
    }

    public void testCloseOnSuccess() throws Exception {
        mp.call();
        assertFalse(a.isOpen());
        assertFalse(b.isOpen());
        assertFalse(c.isOpen());
        assertFalse(d.isOpen());
        assertFalse(e.isOpen());
        assertFalse(f.isOpen());
        assertFalse(g.isOpen());
        assertFalse(r.isOpen());
    }
    
    public void testCloseOnFailure() throws Exception {
        d.setExceptionOnCall(true);
        try {
            mp.call();
            fail("Call should have thrown an exception");
        } catch (RuntimeException ex) {
            // expected because step d will fail
        }
        assertFalse(a.isOpen());
        assertFalse(b.isOpen());
        assertFalse(c.isOpen());
        assertFalse(d.isOpen());
        assertFalse(e.isOpen());
        assertFalse(f.isOpen());
        assertFalse(g.isOpen());
        assertFalse(r.isOpen());
    }
}
