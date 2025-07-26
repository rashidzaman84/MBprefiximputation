package org.processmining.prefiximputation.modelbased.completeforgetting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.onlineconformance.models.Move;
import org.processmining.onlineconformance.models.Move.Type;
import org.processmining.onlineconformance.models.PartialAlignment;
import org.processmining.prefiximputation.inventory.NullConfiguration;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

/**
 * This class keeps track of the conformance status for a single process instance
 * 
 * @author Andrea Burattin
 */
public class LocalConformanceStatus {

	protected int currentImputationSize=0;
	//protected XTrace trace;
	protected ArrayList<String> traceModelAlphabet;
	protected ArrayList<String> traceStreamAlphabet;
	//protected Map<Integer,ArrayList<String>> imputationHistory;
	protected boolean isTraceInNonDeterministicRegion = false;
	//protected ArrayList<String> transitionsInNonDeterministicRegion;
	//public static XFactory xesFactory = new XFactoryBufferedImpl();
	//public static XExtensionManager xesExtensionManager = XExtensionManager.instance();
	//protected int correctObservedDirectFollowingRelations = 0;
	//protected int incorrectObservedDirectFollowingRelations = 0;
	//protected String lastActivityForCase = null;
	protected LocalModelStructure lms;
	//protected String ccAlgoChoice;
	protected Date lastUpdate;
	//protected Set<DirectFollowingRelation> observedRelations = new HashSet<DirectFollowingRelation>();
	protected OnlineConformanceScore last;	
	protected OnlineConformanceChecker1 OCC1;
	public /*protected*/ PrefixAlignmentBasedOCC OCC2;
	//protected PrefixImputation prefixImputation;
	//protected PrefixImputationStatic prefixImputationStatic;
	//protected OnlineConformanceCalculator2 OCC2 = new OnlineConformanceCalculator2(lms);
	//protected OnlineConformanceCalculator2 OCC2 = new OnlineConformanceCalculator2();
	protected Boolean imputationRevisitSelected = false;
	//protected int imputationRevisitWindowSize;
	protected String caseId;
	public HashMap<String, NonDeterministicRegion> NDRegionsLocalPersonalisedCopy = new HashMap<>();
	public ArrayList<Place> deterministicPlaces = new ArrayList<>();
	public ArrayList<Place> nonDeterministicPlaces = new ArrayList<>();

	public LocalConformanceStatus (LocalModelStructure lms) {
		this.lms = lms;
		//this.OCC2 = new OnlineConformanceChecker2(this.lms);
		//this.lastUpdate = new Date();		
	}

	public LocalConformanceStatus (LocalModelStructure lms, String caseId ) {
		this.lms = lms;
		//this.ccAlgoChoice = lms.ccAlgoChoice;
		//this.OCC2 = new OnlineConformanceChecker2(this.lms);		
		//this.imputationRevisitWindowSize = lms.imputationRevisitWindowSize;
		this.imputationRevisitSelected = lms.imputationRevisitWindowSize==0?false:true;
		this.caseId = caseId;
		this.traceModelAlphabet = new ArrayList<String>();
		this.traceStreamAlphabet = new ArrayList<String>();
		this.lastUpdate = new Date();
		this.last = new OnlineConformanceScore();
	}

	public OnlineConformanceScore replayTrace(String newEventName, /*XTrace tr,*/ Boolean isNew) {
		switch(this.lms.ccAlgoChoice) {
			case "Behavioral Profiles":
				return replayEventBehProf(newEventName,/* tr, */isNew);
			case "Prefix Alignment":
				return replayEventPrefAlign(newEventName, /*tr,*/ isNew);
			default:
				System.out.println("You made a wrong CC Algo Choice");
				return null;
		}	

	}


	public OnlineConformanceScore replayEventPrefAlign(String newEventName, /*XTrace tr,*/ Boolean isNew) {
		

		if(isNew) {			
			//if observed event is orphan and is in Alphabet, either exactly or equivalently in case of duplication
			if(lms.isInProcessModelAlphabet(newEventName) && !lms.isCaseStartingEvent(newEventName)){							
				if(lms.labelsToModelElementsMap.get(newEventName).size() == 1) {
					//prefixImputation.imputePrefix(newEventName);                   //shall I use the label lms.labelsToModelElementsMap.get(newEventName).get(0).getlabel??
					traceModelAlphabet = PrefixImputationStatic.imputePrefix(lms, newEventName);
					currentImputationSize = traceModelAlphabet.size()-1;
				}else {
					int minLength = Integer.MAX_VALUE;					
					for(Transition transition: lms.labelsToModelElementsMap.get(newEventName)){
						ArrayList<String> temp = new ArrayList<>();
						temp.addAll(PrefixImputationStatic.imputePrefix(lms, transition.getLabel()));
						if(temp.size() < minLength) {
							traceModelAlphabet = temp;
							currentImputationSize = traceModelAlphabet.size()-1;
							minLength = temp.size();
						}						
					}
				}
				traceStreamAlphabet = transformAlphabet(traceModelAlphabet);								
//				//If orphan event is in ND region
//				if(lms.isInNonDeterministicRegion(traceModelAlphabet.get(traceModelAlphabet.size()-1))) {
//					isTraceInNonDeterministicRegion = true;
//					NDRegionsLocalPersonalisedCopy = lms.getNDRegionsCopy();  //we need copies of only relevant ND objects not all
//					for(Entry<String, NonDeterministicRegion> entry : NDRegionsLocalPersonalisedCopy.entrySet()) {
//						for(branch eentry: entry.getValue().getSymmetry()) {
//							if(eentry.getBranchExecution().contains(traceModelAlphabet.get(traceModelAlphabet.size()-1))) {
//								eentry.activated = true;
//							}
//						}
//					}
//
//				}				
				OCC2 = new PrefixAlignmentBasedOCC(this.lms);				
				batchCCPrefAlign(traceStreamAlphabet);
				//batchCCPrefAlign(traceModelAlphabet);
				//The projectOnLabels() or projectOnModel() methods on the above alignment retireved from datastore is helpful but not 
				//completely as we require the sequence of the events observed but in terms of the alignmed model activities. For example,
				//we observed O_Select but for our trace we need its aligned equivalnet label i.e., either O_Select_1 or O_Select_2 as 
				//our Case Removal module requires this information for isTraceSameAsShortestPath() method.
				//>>System.out.println(last.toString());
				
				
				//If orphan event is in ND region
				
				
				
				PartialAlignment partialAlignment= OCC2.replayer.getDataStore().get(caseId);
				List<Place> placesInCurrentMarking = ((Marking) partialAlignment.getState().getStateInModel()).toList();
				Transition currentTransition = (Transition) partialAlignment.getState().getParentMove().getTransition();
				
				for(PetrinetNode tran: lms.net.getNodes()) {
					if(tran == currentTransition && tran instanceof Transition) {
						//
						for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : lms.net.getOutEdges(tran)) {
							if(edge.getTarget() instanceof Place) {
								deterministicPlaces.add((Place) edge.getTarget());
							}
						}
						break;
					}
				}
				
				if(placesInCurrentMarking.size() > deterministicPlaces.size() && placesInCurrentMarking.containsAll(deterministicPlaces)) {
					isTraceInNonDeterministicRegion = true;
					placesInCurrentMarking.removeAll(deterministicPlaces);
					nonDeterministicPlaces.addAll(placesInCurrentMarking);
				}
				
				refreshUpdateTime();
				return last;
				
				
				
				
				
//				if(lms.isInNonDeterministicRegion(traceModelAlphabet.get(traceModelAlphabet.size()-1))) {
//					isTraceInNonDeterministicRegion = true;
//					PartialAlignment partialAlignment= OCC2.replayer.getDataStore().get(caseId);
//					Marking currentMarking = (Marking) partialAlignment.getState().getStateInModel();
//					
//					Transition currentTransition = (Transition) partialAlignment.getState().getParentMove().getTransition();
//					for(PetrinetNode tran: lms.net.getNodes()) {
//						if(tran.equals(currentTransition) && tran instanceof Transition) {
//							//
//							for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : lms.net.getOutEdges(tran)) {
//								if(edge.getTarget() instanceof Place) {
//									deterministicPlaces.add((Place) edge.getTarget());
//								}
//							}
//						}
//					}
//					
//					for(Place place : currentMarking.baseSet()) {
//						if(!deterministicPlaces.contains(place)) {
//							nonDeterministicPlaces.add(place);
//						}
//					}
//
//				}	
//				
//				refreshUpdateTime();
//				return last;
			}else {              //when first event observed for a case is either a case-starter or not-in model alphabet
				traceModelAlphabet.add(newEventName);
				traceStreamAlphabet.add(newEventName);
				OCC2 = new PrefixAlignmentBasedOCC(this.lms);
				last.setTraceCost(OCC2.processXLog(caseId, newEventName));
				refreshUpdateTime();
				return last;
			}

		}else { //when the observed event has a history
						
			
			//PartialAlignment partialAlignment= OCC2.replayer.getDataStore().get(caseId);
			//Transition currentTransition = (Transition) partialAlignment.getState().getParentMove().getTransition();
			
			if(lms.isInProcessModelAlphabet(newEventName)){
				
				if(isTraceInNonDeterministicRegion) {			
				
				ArrayList<Transition> mappedTransitions = new ArrayList<Transition>(); 
				mappedTransitions.addAll(lms.labelsToModelElementsMap.get(newEventName));  ///?????? Sometimes 
				
				/////1. we check if a transition corresponding to the new event is DIRECTLY enabled in the current marking (consisting of deterministic and non-deterministic places)
				for(Transition transition : mappedTransitions) {
					
					ArrayList<Place> inputPlaces = new ArrayList<>();
					ArrayList<Place> outputPlaces = new ArrayList<>();
					
					for(PetrinetEdge edge :lms.net.getInEdges(transition)) {
						inputPlaces.add((Place) edge.getSource());
					}
					
					for(PetrinetEdge edge :lms.net.getOutEdges(transition)) {
						outputPlaces.add((Place) edge.getTarget());
					}
					
					if(deterministicPlaces.containsAll(inputPlaces)){             //we check if the transition corresponding to the new event is DIRECTLY enabled in the deterministic places of the current marking
						
						traceModelAlphabet.add(newEventName);
						traceStreamAlphabet.add(newEventName);
						last.setTraceCost(OCC2.processXLog(caseId, newEventName));
					
						deterministicPlaces.removeAll(inputPlaces);
						deterministicPlaces.addAll(outputPlaces);
						
					}else if(nonDeterministicPlaces.containsAll(inputPlaces)) {     //we check if the transition corresponding to the new event is DIRECTLY enabled in the non-deterministic places of the current marking
						
						traceModelAlphabet.add(newEventName);
						traceStreamAlphabet.add(newEventName);
						last.setTraceCost(OCC2.processXLog(caseId, newEventName));
						
						nonDeterministicPlaces.removeAll(inputPlaces);
						deterministicPlaces.addAll(outputPlaces);
						
					}else {   //2. we check if the new event is enabled by token firing a sequence from the NON-deterministic places
						
						ArrayList<String> temp = new ArrayList<>();
						temp.addAll(lms.getShortestPath(OCC2.replayer.getDataStore().get(caseId).getState().getStateInModel(),transition, deterministicPlaces, nonDeterministicPlaces));
						
						double traceCost = OCC2.replayer.getDataStore().get(caseId).getCost();
						
						if(!temp.isEmpty()) {
							traceModelAlphabet.addAll(temp);
							traceModelAlphabet.add(newEventName);
							traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
							OCC2 = new PrefixAlignmentBasedOCC(this.lms);
							batchCCPrefAlign(traceStreamAlphabet);
							
						}else {
							System.out.println("empty firing sequence");
							System.out.println("Current Trace: " + traceStreamAlphabet);
							System.out.println("And event is: " + newEventName);
//							if(caseId.equals("419")) {
//								System.out.println("stop");
//							}
							
							traceModelAlphabet.add(newEventName); //the newly arrived event is added to the existing trace history
							traceStreamAlphabet.add(newEventName);
							last.setTraceCost(OCC2.processXLog(caseId, newEventName));
							System.out.println(OCC2.replayer.getDataStore().get(caseId));
						}
					
					
//					traceModelAlphabet.add(newEventName);
//					double traceCost = OCC2.replayer.getDataStore().get(caseId).getCost();
//					OCC2 = new PrefixAlignmentBasedOCC(this.lms);
//					traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
//					batchCCPrefAlign(traceStreamAlphabet);
					refreshUpdateTime();
					
					double updatedTraceCost = OCC2.replayer.getDataStore().get(caseId).getCost();
					
					//update places: the non-deterministic to deterministic has already taken place in getShortestPath
					
					if(traceCost == updatedTraceCost && OCC2.replayer.getDataStore().get(caseId).getState().getParentMove().getType()==Type.MOVE_SYNC) {
						deterministicPlaces.removeAll(inputPlaces);
						deterministicPlaces.addAll(outputPlaces);
					}					
					}		
					
					if(/*!lms.nonDeterministicActivities.contains(transition.getLabel()) ||*/ nonDeterministicPlaces.isEmpty()) {  //check if trace is still in ND region
						isTraceInNonDeterministicRegion = false;
					}
					
					return last;
				}
				
			/////2. we check if the new event is enabled by token firing a sequence from the NON-deterministic places
				
//				ArrayList<String> effectiveTrace = new ArrayList<>();
//				effectiveTrace.addAll(OCC2.replayer.getDataStore().get(caseId).projectOnLabels());
				
//				for(Transition transition : mappedTransitions) {  //here we are not checking all the transitions as for a transition with multiple imput places it is hard to say what is shortest
//					
//
//					
//					ArrayList<String> temp = new ArrayList<>();
//					temp.addAll(lms.getShortestPath(OCC2.replayer.getDataStore().get(caseId).getState().getStateInModel(),transition, deterministicPlaces));
//					
//					if(!temp.isEmpty()) {
//						traceModelAlphabet.addAll(temp);
//						
//						//update nd places 
//						//nonDeterministicPlaces.remove(effectivePlace);
//						
//						//for(PetrinetEdge edge :lms.net.getOutEdges(transition)) {
//							//outputPlaces.add((Place) edge.getTarget());
//						//}
//						
//						//deterministicPlaces.addAll(outputPlaces);
//					}
//				
//				
//				traceModelAlphabet.add(newEventName);
//				OCC2 = new PrefixAlignmentBasedOCC(this.lms);
//				traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
//				//traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
//				batchCCPrefAlign(traceStreamAlphabet);
//				refreshUpdateTime();
//				
//				//updatePlaces()
//					
//				return last;
//				}
				
								
			/////3. event is appended to the trace and subjected to CC
				
				//traceModelAlphabet.add(newEventName); //the newly arrived event is added to the existing trace history
				//traceStreamAlphabet.add(newEventName);
				//last.setTraceCost(OCC2.processXLog(caseId, newEventName));
				//refreshUpdateTime();
				//return last;
				
			}
//				else {
//				traceModelAlphabet.add(newEventName); //the newly arrived event is added to the existing trace history
//				traceStreamAlphabet.add(newEventName);
//				last.setTraceCost(OCC2.processXLog(caseId, newEventName));
//				refreshUpdateTime();
//				return last;
//			}
				
			}  
			
			
			
			//event is either an outlier and does not belong to the process, or the trace is not in ND region, 
			//or the transition corresponding to the event is not firable by deterministic and non-deterministic places
				//add event to trace and go for conformance checking
				traceModelAlphabet.add(newEventName); //the newly arrived event is added to the existing trace history
				traceStreamAlphabet.add(newEventName);
				last.setTraceCost(OCC2.processXLog(caseId, newEventName));
				refreshUpdateTime();
				return last;
			
		}	
	}

			
			
				
//			for(Place place : lms.net.getPlaces()) {
//			if(place.getLabel().equals("place_5")) {
//				for(Place place_ : lms.net.getPlaces()) {
//					if(place_.getLabel().equals("place_3")) {
//						System.out.println(lms.getShortestPath(place, place_)); //getShortestPath(Place source, Place target) {
//					}
//					
//				}
//			}
//				
//		}		
					
					
//					for(PetrinetNode node: lms.net.getNodes()) { 
//						
//						if(node instanceof Transition && node.equals(transition)) {
//							
//							
//							
//							for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : lms.net.getInEdges(node)) {
//								if(edge.getSource() instanceof Place) {
//									inputPlaces.add((Place) edge.getSource());
//								}
//							}
//							
//							if(deterministicPlaces.containsAll(inputPlaces)){
//								last.setTraceCost(OCC2.processXLog(caseId, newEventName));
//								
//							}
//							
//							for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : lms.net.getOutEdges(node)) {
//								if(edge.getTarget() instanceof Place) {
//									outputPlaces.add((Place) edge.getTarget());
//								}
//							}
//							
//							
//							deterministicPlaces.removeAll(inputPlaces);
//							deterministicPlaces.addAll(outputPlaces);
//							
//							if(!lms.nonDeterministicActivities.contains(transition.getLabel()) || nonDeterministicPlaces.isEmpty()) {  //check if trace is still in ND region
//								isTraceInNonDeterministicRegion = false;
//							}
//							
//							
//							return last;
//						}
//					}
					

			//}
			
	
			
//			for(Transition transition : currentTransition.getVisibleSuccessors()) {
//				if(transition.getLabel().equals(newEventName)) {
//					last.setTraceCost(OCC2.processXLog(caseId, newEventName));
//					return last;
//				}
//			}
//			
			////2, If not, then we check if the transition(s) corresponding to the event observed can be enabled through firing sequence(s) from one or multiple places in non-deterministic places
			//dus, first we need to calculate the number of input places to the this transition and then calculate the above
			
			//for(place p_source : transition.inputplaces){
					//for(place p_target in non-deterministic places){
							//search a firing sequence from p_source to p_target
					//}
			//}
			
			////////////////////////////////////////////////////////////////////////////
			
			
			
//			String tempLabel = newEventName;
//
//			if(isTraceInNonDeterministicRegion) {  //trace is in ND region
//				ArrayList<Transition> mappedLabels = new ArrayList<Transition>(); 
//				//System.out.println("The event before the error may be!!!!!!!!!!!!!!!!" + newEventName);
//				mappedLabels.addAll(lms.labelsToModelElementsMap.get(newEventName));  ///?????? Sometimes 
//
//				//-------------------------------------------------
//				if(mappedLabels.size() > 1) {    //if there is label duplication
//					Iterator iter = mappedLabels.iterator();
//					while(iter.hasNext()) {
//						Transition tran = (Transition) iter.next();
//						if(!lms.nonDeterministicActivities.contains(tran.getLabel())) {
//							iter.remove();
//						}
//
//					}
//					if(mappedLabels.size()==1) {  //if a single activity in the ND activities list exists with this label
//						newEventName = mappedLabels.get(0).getLabel();
//					}else {  //if multiple corresponding activities exists with this label
//						boolean found = false;
//						out:
//							for(Transition tr : mappedLabels) {
//								for(Entry<String, NonDeterministicRegion> entry : NDRegionsLocalPersonalisedCopy.entrySet()) {
//									for(branch eentry: entry.getValue().getSymmetry()) {
//										if(eentry.getBranchExecution().contains(tr.getLabel())) {
//											newEventName = tr.getLabel();  //select the one in the activited branch 
//											//as it will do less damage in case of wrong decision
//											//i.e. it will not impute the prefix and maximally
//											//marked as log-move
//											found = true;
//											break out;
//										}
//									}
//								}
//							}
//						if(found==false) {
//							newEventName = mappedLabels.get(0).getLabel();  //is it a wise decision???????
//						}
//					}
//				}
//				//-----------------------------------------------------------------------------------
//				
//				//First we check if the punctuation has been observed and now we need to select the relevant ND object and do gliding
//				for(Entry<String, NonDeterministicRegion> entry : NDRegionsLocalPersonalisedCopy.entrySet()) {
//					if(entry.getKey().equals(newEventName)) {
//						isTraceInNonDeterministicRegion = false;
//						
//						for(branch eentry: entry.getValue().getSymmetry()) {
//							if(!(eentry.activated)) {
//								traceModelAlphabet.addAll(eentry.getBranchExecution());
////								OCC2 = new PrefixAlignmentBasedOCC(this.lms);
////								traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
////								batchCCPrefAlign(traceStreamAlphabet);
////								refreshUpdateTime();
////								return last;
//							}
//						}
//						traceModelAlphabet.add(newEventName);
//						OCC2 = new PrefixAlignmentBasedOCC(this.lms);
//						traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
//						batchCCPrefAlign(traceStreamAlphabet);
//						refreshUpdateTime();
//						return last;
//					}
//				}
//				//If the observed event is not a punctuation then if it belongs to one of the activated branch AND nothing needs to be done
//				
//				for(Entry<String, NonDeterministicRegion> entry : NDRegionsLocalPersonalisedCopy.entrySet()) {
//					//Boolean foundRelevantNDRegion = false;
//					for(branch eentry: entry.getValue().getSymmetry()) {
//						if(eentry.activated) {
//							//foundRelevantNDRegion = true;
//							if(eentry.getBranchExecution().contains(newEventName)) {
//								traceModelAlphabet.add(newEventName);
//								traceStreamAlphabet.clear();
//								//traceStreamAlphabet.add(newEventName);
//								traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
//								last.setTraceCost(OCC2.processXLog(caseId, traceStreamAlphabet.get(traceStreamAlphabet.size()-1)));
//								refreshUpdateTime();
//								return last;									
//							}
//						}
//					}						
//				}
//
//				//If the observed event is not a punctuation and it does not belongs to one of the activated branch but rather un-activated branch(es)
//				//then we need to impute the prefix of the longest branch till this event and mark the branch(es) as activated
//				
//				boolean found = false;
//				int maxIndex = Integer.MIN_VALUE; 
//				ArrayList<String> toBeImputed = new ArrayList<>();
//				for(Entry<String, NonDeterministicRegion> entry : NDRegionsLocalPersonalisedCopy.entrySet()) {
//					for(branch eentry: entry.getValue().getSymmetry()) {
//						if(eentry.getBranchExecution().contains(newEventName)) {
//							eentry.activated = true;
//							found = true;
//							int index = eentry.getBranchExecution().indexOf(newEventName);
//							if (index> maxIndex) {
//								maxIndex = index;
//								toBeImputed.clear();
//								toBeImputed.addAll(eentry.getBranchExecution().subList(0, index+1));
//							}								
//						}
//					}
//				}
//				if(found) {
//					traceModelAlphabet.addAll(toBeImputed);
//					traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
//					OCC2 = new PrefixAlignmentBasedOCC(this.lms);
//					batchCCPrefAlign(traceStreamAlphabet);
//					refreshUpdateTime();
//					return last;
//				}else {
//					traceModelAlphabet.add(tempLabel); //the newly arrived event is added to the existing trace history
//					//System.out.println(newEventName);
//					traceStreamAlphabet.add(tempLabel);
//					last.setTraceCost(OCC2.processXLog(caseId, tempLabel));
//					refreshUpdateTime();
//					return last;
//				}
//				//-----------------------------------------------
//				
//			}else {  //trace not in ND region
//				traceModelAlphabet.add(newEventName); //the newly arrived event is added to the existing trace history
//				traceStreamAlphabet.add(newEventName);
//				last.setTraceCost(OCC2.processXLog(caseId, newEventName));
//				refreshUpdateTime();
//				return last;
//			}		

	public ArrayList<String> transformAlphabet(ArrayList<String> inModelAlphabet){

		ArrayList<String> temp = new ArrayList<>();
		for(String event: inModelAlphabet) {
			boolean mapped = false;
			for(Entry<String, Collection<Transition>> entry: lms.labelsToModelElementsMap.entrySet()) {
				if(mapped) {
					break;
				}
				for(Transition transition: entry.getValue()) {
					if(transition.getLabel().equals(event)) {
						temp.add(entry.getKey());
						mapped = true;
						break;
					}
				}
			}
		}
		//System.out.println("The input arraylist is: " + inModelAlphabet);
		//System.out.println("The output arraylist is: " + temp);
		return temp;
	}

	public ArrayList<String> generateTraceFromAlignment(PartialAlignment<String, 
			Transition, Marking> partialAlignment){
		//OCC2.replayer.getDataStore().get(caseId).size();
		ArrayList<String> generatedTrace = new ArrayList<String>();
		int index = 0;
		//System.out.println(partialAlignment);
		while(index<partialAlignment.size()/*!(partialAlignment.get(index)!= java.lang.IndexOutOfBoudsException)*/) {
			Move move = partialAlignment.get(index);
			if(move.getType().name().equals("MOVE_SYNC")) {
				//System.out.print(move);
				//System.out.println("\t is sync");
				if(!partialAlignment.get(index).getTransition().getLabel().equals("tau")) {
					generatedTrace.add(partialAlignment.get(index).getTransition().getLabel());
				}				
			}else if (move.getType().name().equals("MOVE_LABEL")) {
				//System.out.print(move);
				//System.out.println("\t is label move");
				generatedTrace.add(partialAlignment.get(index).getEventLabel());
			}else {
				//System.out.print(move);
				//System.out.println("\t is model move");
			}

			/*String eventLabel = partialAlignment.get(index).getEventLabel().toString();
			String transitionLabel = partialAlignment.get(index).getTransition().getLabel();
			if(!eventLabel.equals(">>") && !transitionLabel.equals(">>")) {
				generatedTrace.add(transitionLabel);
			}*/
			index++;
		}
		//System.out.println(generatedTrace);
		return generatedTrace;		
	}

	public void batchCCPrefAlign(/*String*/ ArrayList<String> xtrace) {
		//String caseId = XConceptExtension.instance().extractName(xtrace);
		for (int i = 0; i < xtrace.size(); i++) {
			/*if(i==0) {
				this.OCC2.applyGeneric();
			}*/
			//last.setConformance(OCC2.processXLog(caseId, xtrace.get(i))>0.0?0.0:1.0);
			last.setTraceCost(OCC2.processXLog(caseId, xtrace.get(i)));
		}
	}

	/*public XEvent getXEvent(String eventName) {
		XAttributeMap atts = xesFactory.createAttributeMap();
		atts.put("concept:name",
			xesFactory.createAttributeLiteral(
					"concept:name",
					eventName,
					xesExtensionManager.getByName("Concept")));
		return xesFactory.createEvent(atts);
	}*/

	/*public Integer fitnessForDeletionScore() {
		//compute fitnessForDeletionScore by a criteria taking into account imputation size, if the case is in non-deterministic
		//region, conformance score, if there is only a single shortest-path to the current marking or more etc...
		return 2;
	}*/

	/*public boolean isDistinctlyOrEquivalentlyExist(List<String> transitionsList, String newEventName) {
		if(transitionsList.contains(newEventName)) {
			return true;
		}else {
			ArrayList<String> approxSimilarLabels = lms.getEquivalentModelLabels(newEventName);
			for(String key : approxSimilarLabels) {
				if (transitionsList.contains(key)) {
					return true;
				}
			}
			return false;
		}			
	}*/

	public OnlineConformanceScore getCurrentScore() {
		return last;
	}

	public void refreshUpdateTime() {
		lastUpdate.setTime(System.currentTimeMillis());
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void batchCCBehProf(/*XTrace*/ ArrayList<String> xtrace) {
		for (int i = 0; i < xtrace.size(); i++) {
			if(i==0) {
				this.OCC1 = new OnlineConformanceChecker1(this.lms);
				//OCC1.setLastActivityForCase(XConceptExtension.instance().extractName(xtrace.get(i)));
				OCC1.setLastActivityForCase(xtrace.get(i));
				//XConceptExtension.instance().extractName(t.get(0))
				continue;
			}
			//last = OCC1.doReplay(XConceptExtension.instance().extractName(xtrace.get(i)));
			last = OCC1.doReplay(xtrace.get(i));
			//l.add(i, classes.getByIdentity(XConceptExtension.instance().extractName(xtrace.get(i))).toString());
		}
	}



	private HashMap<String, Double> labelsApproximation(String event, Collection<String> labelsMap) {		

		String tempEvent = event.toLowerCase();
		HashMap<String, Double> simScores = new HashMap<String, Double>();
		AbstractStringMetric metric = new Levenshtein();

		/*int index = 0;
			float simOld = Float.MIN_VALUE;*/
		for (String label : labelsMap) {
			String transitionLabel = label.toLowerCase();

			/*if (tempEvent.startsWith(transitionLabel)) {          we are not considering this as in our example process both the labels
					simScores.put(transitionLabel, (float) 0.75);       start with the transitionlabel
					continue;
				}*/

			double sim = metric.getSimilarity(tempEvent, transitionLabel);
			if (sim >= NullConfiguration.similarityThreshold) {
				simScores.put(label, sim);
			}
		}

		//mapTrans2ComboBox.get(transition).setForeground(Color.YELLOW);

		return simScores;
		/*} else {
			return 0;
		}*/
	}

	public OnlineConformanceScore replayEventBehProf(String newEventName, /*XTrace tr,*/ Boolean isNew) {

		//		if(isNew){                                   //first observed event for a new case
		//			if (!lms.isCaseStartingEvent(newEventName)/*.isFirstEvent(newEventName)*/) {   //first observed event for a case is not a case-starter
		//				///// how to consider non-deterministic aspect here????????????
		//				/*//If the orphan event is located in one of the Non-Deterministic Region (NDR) then set the flag
		//				//isInNonDeterministicRegion to "true" and copy the transitions of that NDR to a local copy i.e.,
		//				//transitionsInNonDeterministicRegion
		//				for (Map.Entry<Integer, ArrayList<String>> entry : (lms.getNonDeterministicRegions()).entrySet()) {				   
		//				    isInNonDeterministicRegion = (entry.getValue()).contains(newEventName);
		//				    if(isInNonDeterministicRegion) {
		//				    	System.out.println("The event: " + newEventName + " is found to be located in NDR: " +  entry.getKey());
		//						transitionsInNonDeterministicRegion = new ArrayList<String>();
		//						transitionsInNonDeterministicRegion.addAll(entry.getValue());
		//						break;
		//					}
		//				}*/
		//				isTraceInNonDeterministicRegion = lms.isInNonDeterministicRegion(newEventName);
		//				if(isTraceInNonDeterministicRegion) {
		//					//>>System.out.println("The event: " + newEventName + " is found to be located in NDR");
		//					//locate the orphan event in the process tree and find the relevant ND regions and activities to this case;
		//					//Accordingly, the MM cost for these found activites will be set to 0
		//					//How will this effect the imputation window approach
		//				}
		//
		//
		//				//>>System.out.println("The arrived event is Orphan and needs Prefix imputation");
		//				prefixImputation =  new PrefixImputation(this.lms);
		//				prefixImputation.imputePrefix(newEventName);
		//				traceModelAlphabet = prefixImputation.imputePrefix(newEventName);
		//				//currentImputationSize = prefixImputation.imputationSize;
		//				//imputationHistory = new HashMap<Integer,ArrayList<String>>();
		//				//imputationHistory.put(0, prefixImputation.imputedPrefix);
		//
		//				batchCCBehProf(traceModelAlphabet);
		//
		//				/*for (int i = 0; i < trace.size(); i++) {
		//					if(i==0) {
		//						this.OCC1 = new OnlineConformanceChecker1(this.lms);
		//						OCC1.setLastActivityForCase(XConceptExtension.instance().extractName(trace.get(i)));
		//						//XConceptExtension.instance().extractName(t.get(0))
		//						continue;
		//					}
		//					last = OCC1.doReplay(XConceptExtension.instance().extractName(trace.get(i)));
		//					//l.add(i, classes.getByIdentity(XConceptExtension.instance().extractName(trace.get(i))).toString());
		//				}*/
		//				//>>System.out.println(last.toString());
		//				refreshUpdateTime();
		//				return last;
		//			}else {    //first observed event for a case is a case-starter
		//				traceModelAlphabet.add(newEventName);
		//				this.OCC1 = new OnlineConformanceChecker1(this.lms);
		//				OCC1.setLastActivityForCase(newEventName);	
		//
		//				last.setConformance(1.0);
		//				//>>System.out.println("The arrived event is the case-starting event for a new case and conformance has been by-default set as 1.0");
		//				//last.setCompleteness(1.0);                  //??????
		//				//last.setConfidence(1.0);                    //??????????
		//				last.isLastObservedViolation(false);        //??????
		//				//>>System.out.println(last.toString());
		//
		//				refreshUpdateTime();
		//				return last;
		//			}
		//		}else {                             //observed event belongs to an on-going case
		//			//trace.add(getXEvent(newEventName)); //the newly arrived event is added to the existing trace history
		//			traceModelAlphabet.add(newEventName);
		//			//>>System.out.println("The arrived belongs to an already existing case and added to its case history");
		//			if(prefixImputation.imputationSize > 0 && imputationRevisitSelected == true && (traceModelAlphabet.size()-prefixImputation.imputationSize) == lms.imputationRevisitWindowSize){
		//				//revisit the imputed prefix by extracting the part other than the imputed prefix and check the most probable
		//				//shortest path for it now
		//				//IF the Prefix is changed THEN reset the prefixImputation object OTHERWISE DO NOTHING
		//				//reset the OCC1 object
		//				//recmompute the conformance w.r.t to the new prefix and trace
		//				//>>System.out.println("The imputation revision option is enabled so we have to revisit the imputation");
		//				prefixImputation.revisitPrefix(traceModelAlphabet);
		//
		//				if(prefixImputation.imputationRevised) {
		//					traceModelAlphabet = prefixImputation.imputePrefix(newEventName);
		//					//currentImputationSize = prefixImputation.imputationSize;
		//					//imputationHistory.put(1, prefixImputation.imputedPrefix);
		//					batchCCBehProf(traceModelAlphabet);
		//				}				
		//
		//				imputationRevisitSelected=false; //to avoid multiple revisits
		//				//>>System.out.println(last.toString());
		//				refreshUpdateTime();
		//				return last;
		//
		//			}
		//			last = OCC1.doReplay(newEventName);
		//			//>>System.out.println(last.toString());
		//			refreshUpdateTime();
		return last;
		//		}		
	}
}

//ArrayList<Place> inputPlaces = new ArrayList<>();
//ArrayList<Place> outputPlaces = new ArrayList<>();
//
//for(PetrinetEdge edge :lms.net.getInEdges(transition)) {
//	inputPlaces.add((Place) edge.getSource());
//}
////HashMap<Place, ArrayList<String>> prefixes = new HashMap<>();
//
//for(Place inputPlace : inputPlaces) {
//	ArrayList<String> shortestPrefix = new ArrayList<>();
//	Place effectivePlace = null;
//	for(Place nonDetPlace : nonDeterministicPlaces) {
//		ArrayList<String> currentPrefix = lms.getShortestPath(nonDetPlace, inputPlace);
//		if(!currentPrefix.isEmpty() && (shortestPrefix.isEmpty() || (shortestPrefix.size()> currentPrefix.size()))) {
//			shortestPrefix.clear();
//			shortestPrefix.addAll(currentPrefix);
//			effectivePlace = nonDetPlace;
//		}
//	}
//	
//	if(!shortestPrefix.isEmpty()) {
//		traceModelAlphabet.addAll(shortestPrefix);
//		traceModelAlphabet.add(newEventName);
//		//update nd places 
//		nonDeterministicPlaces.remove(effectivePlace);
//		
//		for(PetrinetEdge edge :lms.net.getOutEdges(transition)) {
//			outputPlaces.add((Place) edge.getTarget());
//		}
//		
//		deterministicPlaces.addAll(outputPlaces);
//	}
//}
//
//
//OCC2 = new PrefixAlignmentBasedOCC(this.lms);
//traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
//traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
//batchCCPrefAlign(traceStreamAlphabet);
//refreshUpdateTime();

/////
