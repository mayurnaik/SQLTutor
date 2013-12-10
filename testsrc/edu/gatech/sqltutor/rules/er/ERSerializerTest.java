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
		entity.addAttribute(attr = new ERAttribute("Name"));
		attr.setKey(true);
		EREntity employee;
		diagram.addEntity(entity = employee = new EREntity("employee"));
		entity.addAttribute(attr = new ERAttribute("Ssn"));
		attr.setKey(true);
		
		// composite attribute
		entity.addAttribute(attr = new ERAttribute("Name"));
		attr.addAttribute(new ERAttribute("Fname"));
		attr.addAttribute(new ERAttribute("Minit"));
		attr.addAttribute(new ERAttribute("Lname"));
		
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
