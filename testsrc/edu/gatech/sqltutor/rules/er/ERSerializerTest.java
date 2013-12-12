package edu.gatech.sqltutor.rules.er;

import org.junit.Test;

public class ERSerializerTest {

	@Test
	public void test() {
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
		
		ERSerializer serializer = new ERSerializer();
		String xml = serializer.serialize(diagram);
		
		System.out.println(xml);

		@SuppressWarnings("unused")
		ERDiagram reloaded = serializer.deserialize(xml);
		
		System.out.println("Reloaded successfully.");
	}

}
