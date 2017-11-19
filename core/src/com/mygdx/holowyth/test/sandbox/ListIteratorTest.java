package com.mygdx.holowyth.test.sandbox;

import java.util.ArrayList;
import java.util.ListIterator;

public class ListIteratorTest {

	public static void main (String[] args){
		ArrayList<Integer> a = new ArrayList<Integer>();
		
		a.add(1);
		a.add(2);
		a.add(3);
		a.add(4);
		a.add(5);
		
		ListIterator<Integer> iter = a.listIterator();
		iter.next();
		iter.next();
		
		
		iter.previous();
		iter.remove();
		System.out.println(iter.next());
		
		for(Integer i: a){
			System.out.print(i  + ", ");
		}
		
		
		
	}
}
