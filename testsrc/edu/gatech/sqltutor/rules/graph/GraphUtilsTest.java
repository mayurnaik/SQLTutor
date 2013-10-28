package edu.gatech.sqltutor.rules.graph;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class GraphUtilsTest {

	@Test
	public void test1() {
		List<String> fnameList = Arrays.asList("first name");
		List<String> lnameList = Arrays.asList("last name");
		List<String> nameLocalList = Arrays.asList("name");
		List<String> addrList = Arrays.asList("address");
		
		List<List<String>> flMergeList = GraphUtils.mergeLists(
			singletonList(fnameList), singletonList(lnameList)
		);
		
		System.out.println("flMergeList:");
		System.out.println(flMergeList);
		
		List<List<String>> nameList = new ArrayList<List<String>>();
		nameList.add(nameLocalList);
		nameList.addAll(flMergeList);
		
		List<List<String>> nameMergeList = GraphUtils.mergeLists(
			nameList, singletonList(addrList));
		
		System.out.println("nameMergeList");
		System.out.println(nameMergeList);
		
		System.out.println("Chained merge:");
		System.out.println(GraphUtils.mergeLists(
			singletonList(fnameList), singletonList(lnameList), singletonList(addrList)));
	}
}
