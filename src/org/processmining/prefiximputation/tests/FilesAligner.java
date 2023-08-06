package org.processmining.prefiximputation.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;

public class FilesAligner {

	public static void main(String[] args) throws IOException {
		List<Pair<String, String>> firstFile = new ArrayList<Pair<String, String>>();
		List<Pair<String, String>> unorderedSecondFile = new ArrayList<Pair<String, String>>();
		List<Pair<String, String>> orderedSecondFile = new ArrayList<Pair<String, String>>();
		//BufferedReader br = new BufferedReader(new FileReader("D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Process Models BPI 2012 from Boudewijn\\Conventional Alignment Output.txt"));
		BufferedReader br = new BufferedReader(new FileReader("D:\\\\Research Work\\\\latest\\\\Streams\\\\Rashid Prefix Alignment\\\\Scenario 3\\\\PROM A-Star prefix Alignment all traces_ASCENDING COSTS.txt"));

		String line = null;

		while ((line = br.readLine()) != null) {
		  String[] values = line.split("\\t");
//		  for (String str : values) {
//		    System.out.println(str);
//		  }
		  Pair<String, String> packet = new Pair<String, String>(values[0], values[1]);
		  firstFile.add(packet);
		}
		br.close();
		
		//br = new BufferedReader(new FileReader("D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Process Models BPI 2012 from Boudewijn\\Unconstrained Prefix Alignment.txt"));
		br = new BufferedReader(new FileReader("D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Scenario 3\\Imputation based Prefix Alignment All Traces.txt"));

		line = null;
		while ((line = br.readLine()) != null) {
			  String[] values = line.split("\\t");
//			  for (String str : values) {
//			    System.out.println(str);
//			  }
			  Pair<String, String> packet = new Pair<String, String>(values[0], values[1]);
			  unorderedSecondFile.add(packet);
			}
		br.close();
		//System.out.println(unorderedSecondFile);
		
		for(Pair<String, String> pair: firstFile ){
			for(Pair<String, String> pair2: unorderedSecondFile) {
				if(pair2.getKey().equals(pair.getKey())) {
					orderedSecondFile.add(pair2);
					break;
				}
			}
				
		}
		for(Pair<String, String> pair: orderedSecondFile ){
		System.out.println(pair);
		}
		
	}
}

