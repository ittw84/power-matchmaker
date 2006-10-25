package ca.sqlpower.matchmaker.hibernate;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diasparsoftware.util.junit.ValueObjectEqualsTest;

/**
 * A framework for building ValueObjectEqualsTest objects
 * semi-automatically; these still extend JUnit 3.8 TestCase
 * so any other tests can be added. Assumes you haven't done
 * anything fancy like have field names different from their
 * getter/setter names, since it uses reflection on the
 * fields directly.
 * <p>
 * To use this framework you must do the following:
 * <ul>
 * <li>Subclass this class;
 * <li>In a static initializer, map.put(fieldName, fieldClass)
 * for each field in the target
 * <li>Keep the map up to date as your target class evolves!
 * <li>Provide a createInstance() that returns a
 * default instance;
 * <li>Provide a createControlInstance that returns a
 * non-default instance
 * </ul>
 * @author ian
 */
public abstract class AutoDifferentValueObjectTestCase
	extends ValueObjectEqualsTest {

	/** Map from field names to class types; you MUST
	 * have a static initializer to fill in these values.
	 */
	static Map<String, Class> map  = new HashMap<String, Class>();

	@Override
	protected List<String> keyPropertyNames() {
		List<String> n = new ArrayList<String>();
		for (String s : map.keySet())
			n.add(s);
		return n;
	}

	@Override
	protected Object createInstanceDiffersIn(String fieldName) throws Exception {
		Class c = map.get(fieldName);
		Field f = getField(fieldName);
		Object target = createInstance();
		f.setAccessible(true);  // bye-bye "private"
		if (c.equals(String.class)) {
			f.set(target, "testme");
		} else if (c.equals(BigDecimal.class)) {
			f.set(target, new BigDecimal(123));
		} else if (c.equals(boolean.class)) {
			f.set(target, Boolean.TRUE);
		} else if (c.equals(java.util.Date.class)) {
			f.set(target, new Date());
		} else if (c.equals(Integer.class)) {
			f.set(target, new Integer(111));
		} else if (c.equals(Long.class)) {
			f.set(target, new Long(111));
		} else throw new IllegalArgumentException(
			"Unhandled type " + c);
		return target;
	}

	Field[] fields;

	private Field getField(String name) {
		if (fields == null) {
			fields = createInstance().getClass().getDeclaredFields();
		}
		for (Field f : fields) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		throw new IllegalArgumentException(name + " is not a field");
	}

	/** Template factory method: user must create a
	 * (possibly null) instance.
	 * @return
	 */
	abstract Object createInstance();
}
