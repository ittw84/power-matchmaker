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


package ca.sqlpower.matchmaker.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchRuleSet;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.hibernate.MatchDAOHibernate;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSession;
import ca.sqlpower.matchmaker.dao.hibernate.PlFolderDAOHibernate;

public abstract class AbstractMatchRuleSetDAOTestCase extends AbstractDAOTestCase<MatchRuleSet,MatchRuleSetDAO>  {

	Long count=0L;
    Match match;
    PlFolder folder;
    public AbstractMatchRuleSetDAOTestCase() {
        match= new Match();
        match.setName("Criteria Group Test Match");
        match.setType(Match.MatchMode.BUILD_XREF);
        folder = new PlFolder("main test folder");
        match.setParent(folder);
        try {
            match.setSession(getSession());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public abstract MatchMakerHibernateSession getSession() throws Exception;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
		plFolderDAO.save(folder);
        MatchDAO matchDAO = new MatchDAOHibernate(getSession());
        matchDAO.save(match);
    }

	@Override
	public MatchRuleSet createNewObjectUnderTest() throws Exception {
		count++;
		MatchRuleSet criteriaGroup = new MatchRuleSet();
        criteriaGroup.setSession(getSession());
		try {
			setAllSetters(criteriaGroup, getNonPersitingProperties());
            criteriaGroup.setName("Group "+count);
            match.addMatchCriteriaGroup(criteriaGroup);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return criteriaGroup;
	}

	@Override
	public List<String> getNonPersitingProperties() {
		List<String> nonPersistingProperties = super.getNonPersitingProperties();
		nonPersistingProperties.add("lastUpdateOSUser");
		nonPersistingProperties.add("lastUpdateDate");
		nonPersistingProperties.add("lastUpdateAppUser");
		nonPersistingProperties.add("session");
        nonPersistingProperties.add("parent");
        nonPersistingProperties.add("parentMatch");
        nonPersistingProperties.add("oid");

		return nonPersistingProperties;
	}

    public void testDelete() throws Exception {
        Connection con = getSession().getConnection();
        MatchDAO matchDAO = new MatchDAOHibernate(getSession());
        Statement stmt = null;
        try {

            MatchRuleSet group = match.getMatchCriteriaGroups().get(0);
            String groupId = group.getName();

            MatchRuleSetDAO dao = getDataAccessObject();
            dao.save(group);
            
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM pl_match_group WHERE group_id = '"+groupId+"'");
            assertTrue("match group didn't save?!", rs.next());
            rs.close();

            dao.delete(group);

            rs = stmt.executeQuery("SELECT * FROM pl_match_group WHERE group_id = '"+groupId+"'");
            assertFalse("match group didn't delete", rs.next());
            rs.close();
        } finally {
            stmt.close();
        }
    }
}