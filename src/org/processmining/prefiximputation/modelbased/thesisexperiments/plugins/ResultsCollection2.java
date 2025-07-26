package org.processmining.prefiximputation.modelbased.thesisexperiments.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.javatuples.Triplet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.onlineconformance.models.PartialAlignment;

public class ResultsCollection2 {
	
	public int featureSize;
	public int caseLimitSize;
	public LinkedHashMap<String, Double> costRecords;
	public int sumOfForgottenPrematureCases;
	public int sumOfEternalPrematureCases;
	public int conformantAsConformant;
	public int conformantAsNonConformant;
	public int nonConformantAsConformant;
	public int nonConformantAsNonConformantExactlyEstimated;
	public int nonConformantAsNonConformantUnderEstimated;
	public int nonConformantAsNonConformantOverEstimated;
	public double ATPE;
	public int maxStates=0;
	public ArrayList<Integer> allFoldsMaxStates;
	public int fold;
	public ArrayList<Triplet<Integer, String, Double>> foldsResults;
	public ArrayList<Long> ATPErecord;
	public long maxATPE;
	public ArrayList<Integer> foldStates;
	HashMap<String, PartialAlignment<String, Transition, Marking>> eventualAlignments;
	public ResultsCollection2() {		
		this.costRecords = new LinkedHashMap<>();
		this.allFoldsMaxStates = new ArrayList<>();
		this.ATPErecord = new ArrayList<>();
		this.foldStates = new ArrayList<>();
		this.eventualAlignments = new HashMap<String , PartialAlignment<String, Transition, Marking>>();
		
	}
	
	

}