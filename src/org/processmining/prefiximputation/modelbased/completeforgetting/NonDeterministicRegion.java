package org.processmining.prefiximputation.modelbased.completeforgetting;


import java.io.Serializable;
import java.util.ArrayList;

public class NonDeterministicRegion implements Serializable{
	private String starterTransition;
	private String terminatorTransition;
	private int noOfBranches=0;
	/*private ArrayList<Place> starterPlaces;
	private ArrayList<Place> terminatorPlaces;*/
	//private Map<Place, branch> symmetry;
	private ArrayList<branch> symmetry;
	
	public NonDeterministicRegion() {
		this.symmetry = new ArrayList<>();
	}
	
	public ArrayList<branch> getSymmetry() {
		return symmetry;
	}

	public void addToSymmetry(/*Place p, */branch br) {
		//this.symmetry.put(p, br);
		this.symmetry.add(br);
	}

	public class branch implements Serializable{
		private String a1;
		boolean activated = false;
		private ArrayList<String> branchExecution= new ArrayList<>(); 
		
		public branch(String name) {
			this.a1 = name;
		}

		public ArrayList<String> getBranchExecution() {
			return branchExecution;
		}

		public void setBranchExecution(ArrayList<String> branchExecution) {
			this.branchExecution = branchExecution;
		}
		
		//starting place
		//ending place
		//min execution
		//max execution
	}	

}
