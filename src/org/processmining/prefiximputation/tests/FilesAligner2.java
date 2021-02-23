package org.processmining.prefiximputation.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Triplet;

public class FilesAligner2 {
	public static void main(String[] args) throws IOException {
		List<Triplet<String, String, Double>> firstFile = new ArrayList<Triplet<String, String, Double>>();
		List<Triplet<String, String, Double>> unorderedSecondFile = new ArrayList<Triplet<String, String, Double>>();
		List<Triplet<String, String, Double>> filteredFirstFile = new ArrayList<Triplet<String, String, Double>>();
		List<Triplet<String, String, Double>> filteredSecondFile = new ArrayList<Triplet<String, String, Double>>();
		BufferedReader br = new BufferedReader(new FileReader("D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Process Models BPI 2012 from Boudewijn\\Conventional Alignment Output.txt"));
		String line = null;

		while ((line = br.readLine()) != null) {
		  String[] values = line.split("\\t");
//		  for (String str : values) {
//		    System.out.println(str);
//		  }
		  Triplet<String, String, Double> packet = new Triplet<String, String, Double>(values[0], values[1], Double.parseDouble(values[2].substring(1, 4)));
		  firstFile.add(packet);
		}
		br.close();
		
		br = new BufferedReader(new FileReader("D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Process Models BPI 2012 from Boudewijn\\Unconstrained Prefix Alignment.txt"));
		line = null;
		while ((line = br.readLine()) != null) {
			  String[] values = line.split("\\t");
//			  for (String str : values) {
//			    System.out.println(str);
//			  }
			  Triplet<String, String, Double> packet = new Triplet<String, String, Double>(values[0], values[1], Double.parseDouble(values[2].substring(1, 4)));
			  unorderedSecondFile.add(packet);
			}
		br.close();
		//System.out.println(unorderedSecondFile);
		
		for(Triplet<String, String, Double> triplet1: firstFile ){
			for(Triplet<String, String, Double> triplet2: unorderedSecondFile) {
				if(triplet2.getValue0().equals(triplet1.getValue0())) {
					if(Double.compare(triplet2.getValue2(), triplet1.getValue2()) != 0) {
						System.out.println(triplet1.getValue2() +"," + triplet2.getValue2());
						filteredFirstFile.add(triplet1);
						filteredSecondFile.add(triplet2);
						break;
					}
					
				}
			}
				
		}
		System.out.println("------------------------------Conventional Alignment Filtered");
		for(Triplet<String, String, Double> triplet1: filteredFirstFile ){
		System.out.println(triplet1.getValue0() + ", " + triplet1.getValue1() + "   " + triplet1.getValue2());
			}
		System.out.println(" ");
		System.out.println(" ");
		System.out.println("------------------------------Unconstrained Prefix Alignment");
		for(Triplet<String, String, Double> triplet2: filteredSecondFile ){
			System.out.println(triplet2.getValue0() + ", " + triplet2.getValue1() + "   " + triplet2.getValue2());
			}
	}

}
