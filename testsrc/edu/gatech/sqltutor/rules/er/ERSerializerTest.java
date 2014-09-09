package edu.gatech.sqltutor.rules.er;

import static edu.gatech.sqltutor.rules.er.EREdgeConstraint.ANY_CARDINALITY;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import edu.gatech.sqltutor.TestConst;
import edu.gatech.sqltutor.rules.er.mapping.ERForeignKeyJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERLookupTableJoin;
import edu.gatech.sqltutor.rules.er.mapping.ERMapping;

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
		

		
		entity = diagram.newEntity("Project");
		attr = entity.addAttribute("Pnumber");
		attr.setKey(true);
		entity.addAttribute("Pname");
		
		relationship = new ERRelationship("works_on", 
			employee, new EREdgeConstraint(ANY_CARDINALITY, "Employee"),
			entity, new EREdgeConstraint(ANY_CARDINALITY, "Project"));
		diagram.addRelationship(relationship);
		relationship.addAttribute("Hours");
		
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
		mapping.mapRelationship("works_for", 
			new ERForeignKeyJoin("department.name", "employee.dno"));
		mapping.mapRelationship("works_on", 
			new ERLookupTableJoin("employee.ssn", "works_on.essn", 
				"project.number", "works_on.pno"));

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
	public void testReadEmployeeDiagram() throws Exception {
		ERSerializer serializer = new ERSerializer();
		InputStream inStream = ERSerializerTest.class.getResourceAsStream(TestConst.Resources.COMPANY_DIAGRAM);
		ERDiagram diagram = null;
		try {
			diagram = (ERDiagram)serializer.deserialize(inStream);
		} finally {
			if( inStream != null ) inStream.close();
		}
		
		System.out.println("Loaded successfully.");
		
		String xml = serializer.serialize(diagram);
		System.out.println("Reserialization result:");
		System.out.println(xml);
	}
	
	@Test
	public void testReadEmployeeMapping() throws Exception {
		ERSerializer serializer = new ERSerializer();
		InputStream inStream = ERSerializerTest.class.getResourceAsStream(TestConst.Resources.COMPANY_MAPPING);
		ERMapping mapping = null;
		try {
			mapping = (ERMapping)serializer.deserialize(inStream);
		} finally {
			if( inStream != null ) inStream.close();
		}
		
		System.out.println("Loaded successfully.");
		
		String xml = serializer.serialize(mapping);
		System.out.println("Reserialization result:");
		System.out.println(xml);
	}
	
	@Test
	public void testReadBusinessTripDiagram() throws Exception {
		ERSerializer serializer = new ERSerializer();
		InputStream inStream = ERSerializerTest.class.getResourceAsStream(TestConst.Resources.BUSINESS_TRIP_DIAGRAM);
		ERDiagram diagram = null;
		try {
			diagram = (ERDiagram)serializer.deserialize(inStream);
		} finally {
			if( inStream != null ) inStream.close();
		}
		
		System.out.println("Loaded successfully.");
		
		String xml = serializer.serialize(diagram);
		System.out.println("Reserialization result:");
		System.out.println(xml);
	}
	
	@Test
	public void testReadBusinessTripMapping() throws Exception {
		ERSerializer serializer = new ERSerializer();
		InputStream inStream = ERSerializerTest.class.getResourceAsStream(TestConst.Resources.BUSINESS_TRIP_MAPPING);
		ERMapping mapping = null;
		try {
			mapping = (ERMapping)serializer.deserialize(inStream);
		} finally {
			if( inStream != null ) inStream.close();
		}
		
		System.out.println("Loaded successfully.");
		
		String xml = serializer.serialize(mapping);
		System.out.println("Reserialization result:");
		System.out.println(xml);
	}
}
