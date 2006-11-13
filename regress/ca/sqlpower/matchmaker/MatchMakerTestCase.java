package ca.sqlpower.matchmaker;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;
import ca.sqlpower.matchmaker.util.SourceTable;
import ca.sqlpower.matchmaker.util.ViewSpec;
import ca.sqlpower.matchmaker.util.log.Level;
import ca.sqlpower.matchmaker.util.log.Log;
import ca.sqlpower.matchmaker.util.log.LogFactory;

/**
 * A base test that all test cases of MatchMakerObject implementations should extend.
 *
 * @param <C> The class under test
 * @version $Id$
 */
public abstract class MatchMakerTestCase<C extends MatchMakerObject> extends TestCase {

	/**
	 * The object under test.
	 */
	C target;

    Set<String>propertiesToIgnoreForEventGeneration = new HashSet<String>();
    public MatchMakerSession session = new TestingMatchMakerSession();

	protected void setUp() throws Exception {
		super.setUp();

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected abstract C getTarget();

	public void testAllSettersGenerateEvents()
	throws IllegalArgumentException, IllegalAccessException,
	InvocationTargetException, NoSuchMethodException, ArchitectException {

		MatchMakerObject mmo = getTarget();

		MatchMakerEventCounter listener = new MatchMakerEventCounter();
		mmo.addMatchMakerListener(listener);

		List<PropertyDescriptor> settableProperties;

		settableProperties = Arrays.asList(PropertyUtils.getPropertyDescriptors(mmo.getClass()));

		for (PropertyDescriptor property : settableProperties) {
			Object oldVal;
			if (propertiesToIgnoreForEventGeneration.contains(property.getName())) continue;

			try {
				oldVal = PropertyUtils.getSimpleProperty(mmo, property.getName());
				// check for a setter
				if (property.getWriteMethod() == null)
				{
					continue;
				}

			} catch (NoSuchMethodException e) {
				System.out.println("Skipping non-settable property "+property.getName()+" on "+mmo.getClass().getName());
				continue;
			}
			Object newVal; // don't init here so compiler can warn if the
							// following code doesn't always give it a value
			if (property.getPropertyType() == Integer.TYPE
					|| property.getPropertyType() == Integer.class) {
				newVal = ((Integer) oldVal) + 1;
			} else if (property.getPropertyType() == String.class) {
				// make sure it's unique
				newVal = "new " + oldVal;

			} else if (property.getPropertyType() == Boolean.TYPE) {
				newVal = new Boolean(!((Boolean) oldVal).booleanValue());
			} else if (property.getPropertyType() == Long.class) {
				if (oldVal == null) {
					newVal = new Long(0L);
				} else {
					newVal = new Long(((Long) oldVal).longValue() + 1L);
				}
			} else if (property.getPropertyType() == BigDecimal.class) {
				if (oldVal == null) {
					newVal = new BigDecimal(0);
				} else {
					newVal = new BigDecimal(((BigDecimal) oldVal).longValue() + 1L);
				}
			} else if (property.getPropertyType() == SourceTable.class) {
				newVal = new SourceTable();
			} else if (property.getPropertyType() == MatchSettings.class) {
				newVal = new MatchSettings();
			} else if (property.getPropertyType() == MergeSettings.class) {
				newVal = new MergeSettings();
			} else if (property.getPropertyType() == SQLTable.class) {
				newVal = new SQLTable();
			} else if (property.getPropertyType() == ViewSpec.class) {
				newVal = new ViewSpec();
			} else if (property.getPropertyType() == Log.class) {
				newVal = LogFactory
						.getLogger(Level.DEBUG, "TestMatchMaker.log");
			} else if (property.getPropertyType() == PlFolder.class) {
				newVal = new PlFolder<Match>("Test Folder");
			} else if (property.getPropertyType() == Match.MatchType.class) {
				if (oldVal == Match.MatchType.BUILD_XREF) {
					newVal = Match.MatchType.FIND_DUPES;
				} else {
					newVal = Match.MatchType.BUILD_XREF;
				}
			} else if (property.getPropertyType() == MatchMakerTranslateGroup.class) {
				newVal = new MatchMakerTranslateGroup();
			} else if (property.getPropertyType() == MatchMakerObject.class) {
				newVal = new TestingAbstractMatchMakerObject();
			}else if (property.getPropertyType() == SQLColumn.class) {
				newVal = new SQLColumn();
			} else {
				throw new RuntimeException("This test case lacks a value for "
						+ property.getName() + " (type "
						+ property.getPropertyType().getName() + ") from "
						+ mmo.getClass());
			}

			if (newVal instanceof MatchMakerObject){
				((MatchMakerObject)newVal).setSession(session);
			}

			int oldChangeCount = listener.getAllEventCounts();

			try {
				BeanUtils.copyProperty(mmo, property.getName(), newVal);

				// some setters fire multiple events (they change more than one property)
				assertTrue("Event for set "+property.getName()+" on "+mmo.getClass().getName()+" didn't fire!",
						listener.getAllEventCounts() > oldChangeCount);
				if (listener.getAllEventCounts() == oldChangeCount + 1) {
					assertEquals("Property name mismatch for "+property.getName()+ " in "+mmo.getClass(),
							property.getName(),
							listener.getLastEvt().getPropertyName());
					assertEquals("New value for "+property.getName()+" was wrong",
							newVal,
							listener.getLastEvt().getNewValue());
				}
			} catch (InvocationTargetException e) {
				System.out.println("(non-fatal) Failed to write property '"+property.getName()+" to type "+mmo.getClass().getName());
			}
		}
	}

    /**
     * The child list should never be null for any Match Maker Object, even if
     * that object's type is childless.
     */
    public void testChildrenNotNull() {
        assertNotNull(getTarget().getChildren());
    }

}
