package edu.gatech.sqltutor.rules.er;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class ERSerializerTest {
	
	private ERDiagram makeDiagram() {
		ERDiagram diagram = new ERDiagram();
		
		EREntity entity = null;
		ERAttribute attr = null;
		ERRelationship relationship = null;
		
		EREntity department;
		diagram.addEntity(entity = department = new EREntity("Department"));
		attr = entity.addAttribute("Name");
		attr.setKey(true);
		EREntity employee;
		diagram.addEntity(entity = employee = new EREntity("Employee"));
		attr = entity.addAttribute("Ssn");
		attr.setKey(true);
		
		// composite attribute
		attr = entity.addCompositeAttribute("Name");
		
		for( String childName: new String[]{"Fname", "Minit", "Lname"} )
			entity.addChildAttribute("Name", childName);
//		attr.addAttribute("Fname");
//		attr.addAttribute("Minit");
//		attr.addAttribute("Lname");
		
		// relationship
		relationship = new ERRelationship("works_for", 
			employee, new EREdgeConstraint(1, "Employee"), 
			department, new EREdgeConstraint(EREdgeConstraint.ANY_CARDINALITY, "Department"));
		diagram.addRelationship(relationship);
		
		return diagram;
	}

	@Test
	public void testDiagram() {
		ERDiagram diagram = makeDiagram();
		
		ERAttribute nameAttr = diagram.getAttribute("Employee.Name");
		Assert.assertNotNull("Could not find Employee.Name attribute.", nameAttr);
		ERAttribute fnameAttr = diagram.getAttribute("Employee.Fname");
		Assert.assertNotNull("Could not find Employee.Fname attribute.", fnameAttr);
		
		ERSerializer serializer = new ERSerializer();
		String xml = serializer.serialize(diagram);
		
		System.out.println(xml);

		@SuppressWarnings("unused")
		ERDiagram reloaded = (ERDiagram)serializer.deserialize(xml);
		
		System.out.println("Reloaded successfully.");
		
		String reloadedXML = serializer.serialize(reloaded);
		
		System.out.println(reloadedXML);
		
		Assert.assertEquals(xml, reloadedXML);
	}

	@Test
	public void testMapping() {
		ERDiagram diagram = makeDiagram();
		ERMapping mapping = new ERMapping(diagram);
		mapping.mapAttribute("Employee.Fname", "employee.first_name");
		
		ERSerializer serializer = new ERSerializer();
		String xml = serializer.serialize(mapping);
		
		xml = xml.replaceAll(" class=\"com\\.google\\.common\\.collect\\.HashBiMap\"", "");
		
		System.out.println(xml);
		
		ERMapping reloaded = (ERMapping)serializer.deserialize(xml);
		reloaded.setDiagram(diagram);
		System.out.println("Reloaded successfully.");
		
		Assert.assertNotNull(reloaded.getAttribute("employee.first_name"));
	}
	
	@Test
	public void testRegex() {
		Pattern p = Pattern.compile("([\"\\s]|&quot;)(http://.*?)(?=\\1)", Pattern.CASE_INSENSITIVE);
		final String URL = "http://test.url/here.php?var1=val&var2=val2";
		final String INPUT = "some text " + URL + " more text + \"" + URL + 
				"\" more then &quot;" + URL + "&quot; testing greed &quot;";
		
		Matcher m = p.matcher(INPUT);
		while( m.find() ) {
			System.out.println("Found: " + m.group(2));
		}
	}
}
