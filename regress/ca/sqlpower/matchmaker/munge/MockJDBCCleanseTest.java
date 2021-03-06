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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;

public class MockJDBCCleanseTest extends SQLInputStepTest {
	
	public MockJDBCCleanseTest(String name) {
		super(name);
	}

	private final Logger logger = Logger.getLogger("testLogger");

    @Override
    protected ProjectMode getProjectType() {
        return ProjectMode.CLEANSE;
    }
	
	public void testDoNothing() throws Exception {
        
        assertSame(project, step.getParent().getParent());
        assertSame(ProjectMode.CLEANSE, ((Project) (step.getParent().getParent())).getType());

	    MungeProcessor mp = new MungeProcessor(process, logger);
	    try {
	    	mp.call();
	    } catch (IllegalStateException ex) {
	    	// Expecting ex to be thrown since result step is not connected
	    }

	    Connection con = db.getConnection();
	    Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM table1");

	    if (!rs.next()) {
	        fail("NOTHING IN THE TABLE! :(");
	    }


	    assertEquals("row1,1", rs.getString(1));
	}
	
	public void testOneUpperCaseConnection() throws Exception {	
		step.refresh(logger);
	    UpperCaseMungeStep ucms = new UpperCaseMungeStep();
	    resultStep.refresh(logger);
	    process.addChild(ucms);
	    ucms.connectInput(0, step.getMungeStepOutputs().get(0));
	    resultStep.connectInput(0, ucms.getMungeStepOutputs().get(0));

	    MungeProcessor mp = new MungeProcessor(process, logger);
        mp.call();

	    Connection con = db.getConnection();
	    Statement stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM table1");

	    if (!rs.next()) {
	        fail("NOTHING IN THE TABLE! :(");
	    }

	    assertEquals("row1,1".toUpperCase(), rs.getString(1));

	}
	
}
