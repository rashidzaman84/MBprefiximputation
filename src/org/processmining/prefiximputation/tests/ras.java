package org.processmining.prefiximputation.tests;

import java.util.ArrayList;

public class ras {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ArrayList<String> str1 = new ArrayList<String>();
		ArrayList<String> str2 = new ArrayList<String>();
		str1.add("A");
		str1.add("B");
		str2.add("A");
		str2.add("B");
		str2.add("C");
		//str2.add("D");
		if(str1.equals(str2.subList(0, str2.size()-1))) {
			System.out.println("True");
		}
		
		Double test = 0.0;
		if(test == 0.0) {
			System.out.println("We are equal");
		}
	}

}
