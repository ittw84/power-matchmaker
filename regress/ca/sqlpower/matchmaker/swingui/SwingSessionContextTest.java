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


package ca.sqlpower.matchmaker.swingui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javax.swing.event.UndoableEditListener;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import prefs.PreferencesFactory;
import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.MatchMakerConfigurationException;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.security.PLSecurityException;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.DatabaseListChangeListener;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.swingui.event.SessionLifecycleListener;

public class SwingSessionContextTest extends TestCase {

    private static final Logger logger = Logger.getLogger(SwingSessionContextTest.class);
	
	SwingSessionContextImpl context;
    
    protected void setUp() throws Exception {
        super.setUp();
        final DataSourceCollection<JDBCDataSource> dsCollection = new DataSourceCollection<JDBCDataSource>() {

            public void addDataSource(JDBCDataSource spds) {
                logger.debug("Stub DSCollection.addDataSource("+spds+")");
            }

			public void addDataSourceType(JDBCDataSourceType spdst) {
                logger.debug("Stub DSCollection.addDataSourceType("+spdst+")");
			}

            public void addDatabaseListChangeListener(DatabaseListChangeListener l) {
                logger.debug("Stub DSCollection.addDatabaseListChangeListener("+l+")");
            }

            public List<JDBCDataSource> getConnections() {
                logger.debug("Stub DSCollection.getConnections()");
                List<JDBCDataSource> connections = new ArrayList<JDBCDataSource>();
                connections.add(DBTestUtil.getOracleDS());
                return connections;
            }

			public List<JDBCDataSourceType> getDataSourceTypes() {
                logger.debug("Stub DSCollection.getConnections()");
                List<JDBCDataSourceType> connectionTypes = new ArrayList<JDBCDataSourceType>();
                connectionTypes.add(DBTestUtil.getOracleDS().getParentType());
                return connectionTypes;
			}

            public JDBCDataSource getDataSource(String name) {
                logger.debug("Stub DSCollection.getDataSource("+name+")");
                return null;
            }

            public void mergeDataSource(JDBCDataSource spds) {
                logger.debug("Stub DSCollection.mergeDataSource("+spds+")");
            }

            public void mergeDataSourceType(JDBCDataSourceType spdst) {
                logger.debug("Stub DSCollection.mergeDataSourceType("+spdst+")");
            }

            public void read(File location) throws IOException {
                logger.debug("Stub DSCollection.read("+location+")");
            }

            public void read(InputStream in) throws IOException {
                logger.debug("Stub DSCollection.read("+in+")");
            }
            
            public void removeDataSource(JDBCDataSource spds) {
                logger.debug("Stub DSCollection.removeDataSource("+spds+")");
            }
            
            public boolean removeDataSourceType(JDBCDataSourceType spdst) {
                logger.debug("Stub DSCollection.removeDataSourceType("+spdst+")");
                return false;
            }

            public void removeDatabaseListChangeListener(DatabaseListChangeListener l) {
                logger.debug("Stub DSCollection.removeDatabaseListChangeListener("+l+")");
            }

            public void write(File location) throws IOException {
                logger.debug("Stub DSCollection.write("+location+")");
            }

            public void write(OutputStream out) throws IOException {
                logger.debug("Stub call: DataSourceCollection.write()");
            }

            public void write() throws IOException {
                logger.debug("Stub DSCollection.write()");
            }

			public void addUndoableEditListener(UndoableEditListener l) {
				logger.debug("Stub call: DataSourceCollection.addUndoableEditListener()");
			}

			public void removeUndoableEditListener(UndoableEditListener l) {
				logger.debug("Stub call: DataSourceCollection.removeUndoableEditListener()");
			}

			public URI getServerBaseURI() {
				// TODO Auto-generated method stub
				logger.debug("Stub call: DataSourceCollection.getServerBaseURI()");
				return null;
			}
			
			public URI getMondrianServerBaseURI() {
				// TODO Auto-generated method stub
				logger.debug("Stub call: DataSourceCollection.getMondrianServerBaseURI()");
				return null;
			}

            public <C extends JDBCDataSource> List<C> getConnections(
                    Class<C> classType) {
                // TODO Auto-generated method stub
                logger.debug("Stub call: DataSourceCollection<JDBCDataSource>.getConnections()");
                return null;
            }

            public <C extends JDBCDataSource> C getDataSource(String name,
                    Class<C> classType) {
                // TODO Auto-generated method stub
                logger.debug("Stub call: DataSourceCollection<JDBCDataSource>.getDataSource()");
                return null;
            }

			public UserDefinedSQLType getSQLType(String name) {
				// TODO Auto-generated method stub
				logger.debug("Stub call: DataSourceCollection<JDBCDataSource>.getSQLType()");
				return null;
			}

			public List<UserDefinedSQLType> getSQLTypes() {
				// TODO Auto-generated method stub
				logger.debug("Stub call: DataSourceCollection<JDBCDataSource>.getSQLTypes()");
				return null;
			}

			public UserDefinedSQLType getNewSQLType(String name, int jdbcCode) {
				// TODO Auto-generated method stub
				logger.debug("Stub call: DataSourceCollection<JDBCDataSource>.getNewSQLType()");
				return null;
			}

        };
        MatchMakerSessionContext stubContext = new MatchMakerSessionContext() {

            public MatchMakerSession createDefaultSession() {
                logger.debug("Stub MMSContext.createDefaultSession()");
                return null;
            }

            public List<JDBCDataSource> getDataSources() {
                logger.debug("Stub MMSContext.getDataSources()");
                return dsCollection.getConnections();
            }

            public DataSourceCollection<JDBCDataSource> getPlDotIni() {
                logger.debug("Stub MMSContext.getPlDotIni()");
                return dsCollection;
            }

			public String getEmailSmtpHost() {
				logger.debug("Stub call: MMSContext.getEmailHost()");
				return null;
			}

			public void setEmailSmtpHost(String host) {
				logger.debug("Stub call: MMSContext.setEmailHost()");
			}

			public Collection<MatchMakerSession> getSessions() {
				logger.debug("Stub call: .getSessions()");
				return null;
			}

			public SessionLifecycleListener<MatchMakerSession> getSessionLifecycleListener() {
				logger.debug("Stub call: .getSessionLifecycleListener()");
				return null;
			}

			public void closeAll() {
				logger.debug("Stub call: .closeAll()");
			}
			
			public void ensureDefaultRepositoryDefined() {
				logger.debug("Stub call: .ensureDefaultRepositoryDefined()");
			}

			public String getAddressCorrectionDataPath() {
				// TODO Auto-generated method stub
				logger.debug("Stub call: MatchMakerSessionContext.getAddressCorrectionDataPath()");
				return null;
			}

			public void setAddressCorrectionDataPath(String path) {
				// TODO Auto-generated method stub
				logger.debug("Stub call: MatchMakerSessionContext.setAddressCorrectionDataPath()");
				
			}

			public void addPreferenceChangeListener(PreferenceChangeListener l) {
				logger.debug("Stub call: MatchMakerSessionContext.addPreferenceChangeListener()");
				
			}

			public void removePreferenceChangeListener(
					PreferenceChangeListener l) {
				logger.debug("Stub call: MatchMakerSessionContext.removePreferenceChangeListener()");
				
			}

			@Override
			public MatchMakerSession createSession()
					throws PLSecurityException, SQLException,
					SQLObjectException, MatchMakerConfigurationException {
				// TODO Auto-generated method stub
				logger.debug("Stub call: MatchMakerSessionContext.createSession()");
				return null;
			}
        };
        System.getProperties().setProperty("java.util.prefs.PreferencesFactory", "prefs.PreferencesFactory");
        PreferencesFactory stubPrefsFactory = new PreferencesFactory();

        Preferences memoryPrefs = stubPrefsFactory.userRoot();
        context = new SwingSessionContextImpl(memoryPrefs, stubContext);
    }
    
    public void testGetLastLogin(){                
        SPDataSource ds = DBTestUtil.getOracleDS();
        context.setLastLoginDataSource(ds);        
        SPDataSource actualDS = context.getLastLoginDataSource();
        assertEquals("Did not remember the last login", ds, actualDS);
    }
    
    public void testGetLastImportExportAccessPath(){
        context.setLastImportExportAccessPath("/thisisatestpath");
        assertEquals ("Last import/export access path is incorrect", "/thisisatestpath",
                            context.getLastImportExportAccessPath());
    }
    
}
