package RTi.Util.String;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.HashMap;

import org.junit.Test;

/**
 * Unit tests for EmbeddedPropertiesString class.
 */
public class EmbeddedPropertiesStringTest {

	/**
	 * Test parsing a string that only contains 1 Property=Value.
	 */
	@Test
	public void test1Property () {
		// Define string with only a property.
		String s = "Property1=Value1";
		// Parse the string to extract the property map.
		EmbeddedPropertiesString eps = new EmbeddedPropertiesString(s);
		HashMap<String,String> propertyMap = eps.getPropertyMap();
		
		// Confirm that the property was correctly parsed.
		assertThat(propertyMap.size(), equalTo(1));
		assertThat(propertyMap.get("Property1"), equalTo("Value1"));
	}

	/**
	 * Test parsing a string that contains 1 Property=Value, with whitespace around line.
	 */
	@Test
	public void test1PropertyLineWhitespace () {
		// Define string with only a property.
		String s = " Property1=Value1 ";
		// Parse the string to extract the property map.
		EmbeddedPropertiesString eps = new EmbeddedPropertiesString(s);
		HashMap<String,String> propertyMap = eps.getPropertyMap();
		
		// Confirm that the property was correctly parsed.
		assertThat(propertyMap.size(), equalTo(1));
		assertThat(propertyMap.get("Property1"), equalTo("Value1"));
	}

	/**
	 * Test parsing a string that contains 1 Property = Value, with whitespace around =.
	 */
	@Test
	public void test1PropertyEqualWhitespace () {
		// Define string with only a property.
		String s = "Property1 = Value1";
		// Parse the string to extract the property map.
		EmbeddedPropertiesString eps = new EmbeddedPropertiesString(s);
		HashMap<String,String> propertyMap = eps.getPropertyMap();
		
		// Confirm that the property was correctly parsed.
		assertThat(propertyMap.size(), equalTo(1));
		assertThat(propertyMap.get("Property1"), equalTo("Value1"));
	}

	/**
	 * Test parsing a string that only contains 2 Property=Value.
	 */
	@Test
	public void test2Property () {
		// Define string with only a property.
		String s = "Property1=Value1 Property2=Value2";
		// Parse the string to extract the property map.
		EmbeddedPropertiesString eps = new EmbeddedPropertiesString(s);
		HashMap<String,String> propertyMap = eps.getPropertyMap();
		
		// Confirm that the properties were correctly parsed.
		assertThat(propertyMap.size(), equalTo(2));
		assertThat(propertyMap.get("Property1"), equalTo("Value1"));
		assertThat(propertyMap.get("Property2"), equalTo("Value2"));
	}

	/**
	 * Test parsing a string that only contains 2 Property=Value on separate lines.
	 */
	@Test
	public void test2Property2Lines () {
		// Define string with only a property.
		String s = "Property1=Value1\nProperty2=Value2";
		// Parse the string to extract the property map.
		EmbeddedPropertiesString eps = new EmbeddedPropertiesString(s);
		HashMap<String,String> propertyMap = eps.getPropertyMap();
		
		// Confirm that the properties were correctly parsed.
		assertThat(propertyMap.size(), equalTo(2));
		assertThat(propertyMap.get("Property1"), equalTo("Value1"));
		assertThat(propertyMap.get("Property2"), equalTo("Value2"));
	}

	/**
	 * Test parsing a string ignoring the property indicator, so 2 properties should be found:
	 * <pre>
	 * Property1=Value1 // Property2=Value2
	 * </pre>
	 */
	@Test
	public void test2PropertyIgnoredIndicator () {
		// Define string with only a property.
		String s = "Property1=Value1 // Property2=Value2";
		// Parse the string to extract the property map.
		EmbeddedPropertiesString eps = new EmbeddedPropertiesString(s);
		HashMap<String,String> propertyMap = eps.getPropertyMap();
		
		// Confirm that the properties were correctly parsed.
		assertThat(propertyMap.size(), equalTo(2));
		assertThat(propertyMap.get("Property1"), equalTo("Value1"));
		assertThat(propertyMap.get("Property2"), equalTo("Value2"));
	}

	/**
	 * Test parsing a string with property indicator, so 1 properties should be found:
	 * <pre>
	 * Property1=Value1 // Property2=Value2
	 * </pre>
	 */
	@Test
	public void test2PropertyWithIndicator () {
		// Define string with only a property.
		String s = "Property1=Value1 // Property2=Value2";
		// Parse the string to extract the property map.
		EmbeddedPropertiesString eps = new EmbeddedPropertiesString(s, "//");
		HashMap<String,String> propertyMap = eps.getPropertyMap();
		
		// Confirm that the properties were correctly parsed.
		assertThat(propertyMap.size(), equalTo(1));
		assertThat(propertyMap.get("Property1"), nullValue());
		assertThat(propertyMap.get("Property2"), equalTo("Value2"));
	}
}