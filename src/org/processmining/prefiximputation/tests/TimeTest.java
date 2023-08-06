package org.processmining.prefiximputation.tests;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TimeTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
	      Date d1 = null;
		try {
			d1 = sdformat.parse("1970-01-21T01:00:00.000+01:00");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      Date d2 = null;
		try {
			d2 = sdformat.parse("1970-01-21T01:00:00.000+01:00");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      System.out.println("The date 1 is: " + sdformat.format(d1));
	      System.out.println("The date 2 is: " + sdformat.format(d2));
	      if(d1.compareTo(d2) > 0) {
	         System.out.println("Date 1 occurs after Date 2");
	      } else if(d1.compareTo(d2) < 0) {
	         System.out.println("Date 1 occurs before Date 2");
	      } else if(d1.compareTo(d2) == 0) {
	         System.out.println("Both dates are equal");
	      }
	      
	      ArrayList<Integer> test = new ArrayList<Integer>();
	      test.add(1);
	      test.add(2);
	      test.add(3);
	      ArrayList<Integer> test2 = new ArrayList<Integer>();
	      //test2=test;	      
	      test2.addAll(test);
	      test2.remove(2);
	      
	      System.out.println("+++++++++++++++++++++++++++++++++++++");
	      System.out.println("test" + test);
	      System.out.println("test2" + test2);

	}

}
