package edu.gatech.sqltutor.rules.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class GraphUtils {
	private GraphUtils() { }
	
	public static <T> List<List<T>> mergeLists(List<List<T>>... lists) {
		return mergeLists(Arrays.asList(lists));
	}
	
	public static <T> List<List<T>> mergeLists(Collection<List<List<T>>> lists) {
		if( lists == null || lists.size() < 1 )
			throw new IllegalArgumentException("Must provide at least one list-of-lists.");
		

		Iterator<List<List<T>>> iter = lists.iterator();
		List<List<T>> head = iter.next();
		if( lists.size() == 1 )
			return head;
		
		while( iter.hasNext() ) {
			List<List<T>> tail = iter.next();
			head = mergeLists(head, tail);
		}
		
		return head;
	}
	
	public static <T> List<List<T>> mergeLists(List<List<T>> head, List<List<T>> tail) {
		if( head == null ) throw new NullPointerException("head is null");
		if( tail == null ) throw new NullPointerException("tail is null");
		
		if( head.isEmpty() )
			return tail;
		if( tail.isEmpty() )
			return head;
		
		List<List<T>> newList = new ArrayList<List<T>>(head.size() * tail.size());
		for( List<T> headItem: head ) {
			for( List<T> tailItem: tail ) {
				List<T> concat = new ArrayList<T>(headItem.size() + tailItem.size());
				concat.addAll(headItem);
				concat.addAll(tailItem);
				newList.add(concat);
			}
		}
		return newList;
	}
}
