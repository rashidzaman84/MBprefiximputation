package org.processmining.prefiximputation.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.javatuples.Triplet;

public class FilesAligner2 {
	public static void main(String[] args) throws IOException {
		//List<Triplet<String, String, Double>> firstFile = new ArrayList<Triplet<String, String, Double>>();
		List<String> firstFile = new ArrayList<String>();
		//List<Triplet<String, String, Double>> unorderedSecondFile = new ArrayList<Triplet<String, String, Double>>();
		//Map<String, Triplet<String, Double, Double>> unorderedSecondFile = new HashMap<String, Triplet<String, Double, Double>>();
		Map<String, Triplet<String, String, Double>> unorderedSecondFile = new HashMap<String, Triplet<String, String, Double>>();
		List<Triplet<String, String, Double>> filteredFirstFile = new ArrayList<Triplet<String, String, Double>>();
		List<Triplet<String, String, Double>> filteredSecondFile = new ArrayList<Triplet<String, String, Double>>();
		//BufferedReader br = new BufferedReader(new FileReader("D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Process Models BPI 2012 from Boudewijn\\Conventional Alignment Output.txt"));
		BufferedReader br = new BufferedReader(new FileReader("D:\\\\Research Work\\\\latest\\\\Streams\\\\Rashid Prefix Alignment\\\\Scenario 3\\PROM A-Star Whole Alignment Life Ascending.txt"));
		
		String line = null;

		while ((line = br.readLine()) != null) {
		  String[] values = line.split("\\t");
//		  for (String str : values) {
//		    System.out.println(str);
//		  }
		  //Triplet<String, String, Double> packet = new Triplet<String, String, Double>(values[0], values[1], Double.parseDouble(values[2]/*.substring(1, 4)*/));
		  //firstFile.add(packet);
		  firstFile.add(values[0]);
		  //System.out.println(values[0]);
		}
		br.close();
		
		//br = new BufferedReader(new FileReader("D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Process Models BPI 2012 from Boudewijn\\Unconstrained Prefix Alignment.txt"));
		br = new BufferedReader(new FileReader("D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Scenario 3\\IPA Whole Alignment Life.txt"));

		line = null;
		while ((line = br.readLine()) != null) {
			  String[] values = line.split("\\t");
//			  for (String str : values) {
//			    System.out.println(str);
//			  }
			  //Triplet<String, Double, Double> packet = new Triplet<>(values[1], Double.parseDouble(values[2]), Double.parseDouble(values[3]/*.substring(1, 4)*/));
			  Triplet<String, String, Double> packet = new Triplet<>(values[1], values[2], Double.parseDouble(values[3]/*.substring(1, 4)*/));

			  unorderedSecondFile.put(values[0], packet);
			}
		br.close();
		//System.out.println(unorderedSecondFile);
		//FILTERING FOR ONLY NON-ZERO COST TRACES AND ALIGNING BOTH FILES
		/*for(Triplet<String, String, Double> triplet1: firstFile ){
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
				
		}*/
		
		//FOR ALIGNING TWO FILES
		for(/*Triplet<String, String, Double>*/ String triplet1: firstFile ){
			//for(Entry<String, Triplet<String, Double, Double>> triplet2: unorderedSecondFile.entrySet()) {
			for(Entry<String, Triplet<String, String, Double>> triplet2: unorderedSecondFile.entrySet()) {
				if(triplet2.getKey().equals(triplet1/*.getValue0()*/)) {
					/*if(Double.compare(triplet2.getValue2(), triplet1.getValue2()) != 0) {
						System.out.println(triplet1.getValue2() +"," + triplet2.getValue2());
						filteredFirstFile.add(triplet1);
						filteredSecondFile.add(triplet2);
						break;
					}*/
					System.out.println(triplet2.getKey() + "\t" + triplet2.getValue().getValue0() + "\t" + triplet2.getValue().getValue1() 
							+ "\t" + triplet2.getValue().getValue2());
				}
			}
				
		}
		
		
		
		/*System.out.println("------------------------------Conventional Alignment Filtered");
		for(Triplet<String, String, Double> triplet1: filteredFirstFile ){
		System.out.println(triplet1.getValue0() + ", " + triplet1.getValue1() + "   " + triplet1.getValue2());
			}
		System.out.println(" ");
		System.out.println(" ");
		System.out.println("------------------------------Unconstrained Prefix Alignment");
		for(Triplet<String, String, Double> triplet2: filteredSecondFile ){
			System.out.println(triplet2.getValue0() + ", " + triplet2.getValue1() + "   " + triplet2.getValue2());
			}*/
	}

}
