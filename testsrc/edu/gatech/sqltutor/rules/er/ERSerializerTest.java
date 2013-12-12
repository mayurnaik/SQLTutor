package edu.gatech.sqltutor.rules.er;

import org.junit.Before;
import org.junit.Test;

public class ERSerializerTest {
	
	private ERDiagram makeDiagram() {
		ERDiagram diagram = new ERDiagram();
		
		EREntity entity = null;
		ERAttribute attr = null;
		ERRelationship relationship = null;
		
		EREntity department;
		diagram.addEntity(entity = department = new EREntity("department"));
		attr = entity.addAttribute("Name");
		attr.setKey(true);
		EREntity employee;
		diagram.addEntity(entity = employee = new EREntity("employee"));
		attr = entity.addAttribute("Ssn");
		attr.setKey(true);
		
		// composite attribute
		attr = entity.addAttribute("Name");
		attr.addAttribute("Fname");
		attr.addAttribute("Minit");
		attr.addAttribute("Lname");
		
		// relationship
		relationship = new ERRelationship("works_for", 
			employee, new EREdgeConstraint(1, 1, "Employee"), 
			department, new EREdgeConstraint(4, -1, "Department"));
		diagram.addRelationship(relationship);
		
		return diagram;
	}

	@Test
	public void testDiagram() {
		ERDiagram diagram = makeDiagram();
		
		ERSerializer serializer = new ERSerializer();
		String xml = serializer.serialize(diagram);
		
		System.out.println(xml);

		@SuppressWarnings("unused")
		ERDiagram reloaded = (ERDiagram)serializer.deserialize(xml);
		
		System.out.println("Reloaded successfully.");
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
		System.out.println("Reloaded successfully.");
	}
}
