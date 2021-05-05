package org.processmining.prefiximputation.modelbased.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.onlineconformance.models.Move;
import org.processmining.onlineconformance.models.PartialAlignment;
import org.processmining.prefiximputation.inventory.NullConfiguration;
import org.processmining.prefiximputation.modelbased.models.NonDeterministicRegion.branch;

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
	protected boolean isTraceInNonDeterministicRegion=false;
	protected ArrayList<String> transitionsInNonDeterministicRegion;
	//public static XFactory xesFactory = new XFactoryBufferedImpl();
	//public static XExtensionManager xesExtensionManager = XExtensionManager.instance();
	//protected int correctObservedDirectFollowingRelations = 0;
	//protected int incorrectObservedDirectFollowingRelations = 0;
	//protected String lastActivityForCase = null;
	protected LocalModelStructure lms;
	protected String ccAlgoChoice;
	protected Date lastUpdate;
	//protected Set<DirectFollowingRelation> observedRelations = new HashSet<DirectFollowingRelation>();
	protected OnlineConformanceScore last;	
	protected OnlineConformanceChecker1 OCC1;
	public /*protected*/ OnlineConformanceChecker2 OCC2;
	protected PrefixImputation prefixImputation;
	//protected OnlineConformanceCalculator2 OCC2 = new OnlineConformanceCalculator2(lms);
	//protected OnlineConformanceCalculator2 OCC2 = new OnlineConformanceCalculator2();
	protected Boolean imputationRevisitSelected=false;
	protected int imputationRevisitWindowSize;
	protected String caseId;
	public HashMap<String, NonDeterministicRegion> NDRegionsLocalPersonalisedCopy = new HashMap<>();
	
	public LocalConformanceStatus (LocalModelStructure lms) {
		this.lms = lms;
		//this.OCC2 = new OnlineConformanceChecker2(this.lms);
		//this.lastUpdate = new Date();		
	}
	
	public LocalConformanceStatus (LocalModelStructure lms, String caseId ) {
		this.lms = lms;
		this.ccAlgoChoice = lms.ccAlgoChoice;
		//this.OCC2 = new OnlineConformanceChecker2(this.lms);		
		this.imputationRevisitWindowSize = lms.imputationRevisitWindowSize;
		this.imputationRevisitSelected = imputationRevisitWindowSize==0?false:true;
		this.caseId = caseId;
		this.traceModelAlphabet = new ArrayList<String>();
		this.traceStreamAlphabet = new ArrayList<String>();
		this.lastUpdate = new Date();
		this.last = new OnlineConformanceScore();
	}
	
	public OnlineConformanceScore replayTrace(String newEventName, /*XTrace tr,*/ Boolean isNew) {
		switch(this.ccAlgoChoice) {
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
		
		/*if(!lms.processModelAlphabet.contains(newEventName) && NullConfiguration.allowedDuplicateLabelApproximation){
			Boolean NullConfigurationisExperimentValue = NullConfiguration.isExperiment;
			NullConfiguration.isExperiment = false;
			
			//HashMap<String, Double> approxSimilarLabels = labelsApproximation(newEventName, lms.processModelAlphabet);
			ArrayList<String> approxSimilarLabels = lms.getEquivalentModelLabels(newEventName);
			HashMap<String, Double> approxSimilarLabelsCosts = new HashMap<String, Double>();
			
			for(String key : approxSimilarLabels) {
				ArrayList<String> tempTrace = new ArrayList<String>();
				tempTrace.addAll(trace);
				tempTrace.add(key);
				for (int i = 0; i < tempTrace.size(); i++) {
					//last.setTraceCost(OCC2.processXLog(caseId, tempTrace.get(i)));
					double score = lms.spareReplayer.processXLog(key, tempTrace.get(i));					
					approxSimilarLabelsCosts.put(key, score);
				}
				lms.spareReplayer.replayer.getDataStore().clear();  //purge the history of the spare replayer				
			}
			
			newEventName = Collections.min(approxSimilarLabelsCosts.entrySet(), Map.Entry.comparingByValue()).getKey();
			//Do we need to alter the actual event with the equivalent label in model or we can just forward the equivalent label
			//to the replayer but keep the original event unaltered, so that it may be revisited in future IF REQUIRED.
			NullConfiguration.isExperiment = NullConfigurationisExperimentValue;  //restore the value of the isExperiment
			
		}*/
		
		if(isNew) {			
			//if observed event is orphan and is in Alphabet, either exactly aor equivalently in case of duplication
			if(lms.isInProcessModelAlphabet(newEventName) && !lms.isCaseStartingEvent(newEventName)  /*processModelAlphabet.contains(newEventName)*/){
			//if (!isDistinctlyOrEquivalentlyExist(lms.caseStarterActivities,newEventName) && isDistinctlyOrEquivalentlyExist(lms.processModelAlphabet,newEventName)) { 
				prefixImputation =  new PrefixImputation(lms);
				if(lms.labelsToModelElementsMap.get(newEventName).size() == 1) {
					prefixImputation.imputePrefix(newEventName);                   //shall I use the label lms.labelsToModelElementsMap.get(newEventName).get(0).getlabel??
				}else {
					int minLength = Integer.MAX_VALUE;
					String label = null;
					for(Transition transition: lms.labelsToModelElementsMap.get(newEventName)){
						if(lms.getShortestPrefix(transition.getLabel()).size()<minLength) {
							minLength = lms.getShortestPrefix(transition.getLabel()).size();
							label = transition.getLabel();
						}
					}
					prefixImputation.imputePrefix(label);
				}
				//prefixImputation =  new PrefixImputation(lms);
				/*if(lms.processModelAlphabet.contains(newEventName)) {
					prefixImputation.imputePrefix(newEventName);
					trace = prefixImputation.imputedTrace;
					currentImputationSize = prefixImputation.imputationSize;
				}else {					
					ArrayList<String> approxSimilarLabels = lms.getEquivalentModelLabels(newEventName);
					HashMap<String, ArrayList<String>> approxSimilarLabelsPrefixes = new HashMap<String, ArrayList<String>>();
					
					for(String key : approxSimilarLabels) {
						PrefixImputation localPrefixImputation =  new PrefixImputation(lms);
						localPrefixImputation.imputePrefix(key);
						ArrayList<String> prefix = new ArrayList<String>();
						prefix = localPrefixImputation.imputedTrace;
						approxSimilarLabelsPrefixes.put(key, prefix);									
					}
					String key=null;
					int size = Integer.MAX_VALUE;
					for (Entry<String, ArrayList<String>> entry : approxSimilarLabelsPrefixes.entrySet()){
						if(entry.getValue().size() < size) {
							size = entry.getValue().size();
							key = entry.getKey();
						}
					}
					trace = approxSimilarLabelsPrefixes.get(key);
					//trace.set(trace.size()-1, newEventName);
					currentImputationSize = size;
				}*/
				//prefixImputation =  new PrefixImputation(lms);
				//prefixImputation.imputePrefix(newEventName);
				traceModelAlphabet = prefixImputation.imputedTrace;
				traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
				//traceModelAlphabet.set(traceModelAlphabet.size()-1, newEventName);
				currentImputationSize = prefixImputation.imputationSize;
				
				//imputationHistory = new HashMap<Integer,ArrayList<String>>();
				//imputationHistory.put(0, prefixImputation.imputedPrefix);
				//If the orphan event is located in one of the Non-Deterministic Region (NDR) then set the flag
				//isInNonDeterministicRegion to "true" and copy the transitions of that NDR to the related LCS i.e.,
				//transitionsInNonDeterministicRegion
				
				//isTraceInNonDeterministicRegion = lms.isInNonDeterministicRegion(newEventName);
				if(lms.isInNonDeterministicRegion(traceModelAlphabet.get(traceModelAlphabet.size()-1))) {
					isTraceInNonDeterministicRegion = true;
					NDRegionsLocalPersonalisedCopy = lms.getNDRegionsCopy();
					for(Entry<String, NonDeterministicRegion> entry : NDRegionsLocalPersonalisedCopy.entrySet()) {
						for(branch eentry: entry.getValue().getSymmetry()) {
							if(eentry.getBranchExecution().contains(traceModelAlphabet.get(traceModelAlphabet.size()-1))) {
								eentry.activated = true;
							}
						}
					}
					
				}
				/*if(isInNonDeterministicRegion) {
					//>>System.out.println("The event: " + newEventName + " is found to be located in NDR");
					//locate the orphan event in the process tree and find the relevant ND regions and activities to this case;
					//Accordingly, the MM cost for these found activites will be set to 0
					//How will this effect the imputation window approach
					OCC2 = new OnlineConformanceChecker2(this.lms, true, newEventName);
				}else {
					OCC2 = new OnlineConformanceChecker2(this.lms, false, newEventName);
				}*/
				OCC2 = new OnlineConformanceChecker2(this.lms, false, newEventName);
				/*for (Map.Entry<Integer, ArrayList<String>> entry : (lms.getNonDeterministicRegions()).entrySet()) {				   
				    isInNonDeterministicRegion = (entry.getValue()).contains(newEventName);
				    if(isInNonDeterministicRegion) {
				    	System.out.println("The event: " + newEventName + " is found to be located in NDR: " +  entry.getKey());
						transitionsInNonDeterministicRegion = new ArrayList<String>();
						transitionsInNonDeterministicRegion.addAll(entry.getValue());
						break;
					}
				}*/
				batchCCPrefAlign(traceStreamAlphabet);
				//batchCCPrefAlign(traceModelAlphabet);
				
				/*trace.clear();
				trace.addAll(generateTraceFromAlignment(OCC2.replayer.getDataStore().get(caseId)));*/
				
				//The projectOnLabels() or projectOnModel() methods on the above alignment retireved from datastore is helpful but not 
				//completely as we require the sequence of the events observed but in terms of the alignmed model activities. For example,
				//we observed O_Select but for our trace we need its aligned equivalnet label i.e., either O_Select_1 or O_Select_2 as 
				//our Case Removal module requires this information for isTraceSameAsShortestPath() method.
				//>>System.out.println(last.toString());
				refreshUpdateTime();
				return last;
			}else {                                       //when first event observed for a case is is either a case-starter or not-in model aplhabet
				traceModelAlphabet.add(newEventName);
				traceStreamAlphabet.add(newEventName);
				OCC2 = new OnlineConformanceChecker2(this.lms, false, newEventName);
				//last.setConformance(OCC2.processXLog(caseId, newEventName)>0.0?0.0:1.0);
				last.setTraceCost(OCC2.processXLog(caseId, newEventName));
				//>>System.out.println("The newly arrived event is the case-starting event and conformance has been calculated as:");
				//last.setConformance(1.0);
				//last.setCompleteness(0.0);
				//last.setConfidence(0.0);
				//last.isLastObservedViolation(false);
				//>>System.out.println(last.toString());
				
				//trace.clear();
				//trace.addAll(generateTraceFromAlignment(OCC2.replayer.getDataStore().get(caseId)));
				
				refreshUpdateTime();
				return last;
			}
			
		}else { //when the observed event has a history
			
			if(isTraceInNonDeterministicRegion) {
				ArrayList<Transition> mappedLabels = new ArrayList<Transition>(); 
				mappedLabels.addAll(lms.labelsToModelElementsMap.get(newEventName));  ///??????
				
				
				
				if(mappedLabels.size() > 1) {    //if there is label duplication
					Iterator iter = mappedLabels.iterator();
					while(iter.hasNext()) {
						Transition tran = (Transition) iter.next();
						if(!lms.nonDeterministicActivities.contains(tran.getLabel())) {
							iter.remove();
						}
						
					}
					if(mappedLabels.size()==1) {  //if a single activity in the ND activities list exists with this label
						newEventName = mappedLabels.get(0).getLabel();
					}else {  //if multiple corresponding activities exists with this label
						boolean found =false;
						out:
						for(Transition tr : mappedLabels) {
							for(Entry<String, NonDeterministicRegion> entry : NDRegionsLocalPersonalisedCopy.entrySet()) {
								for(branch eentry: entry.getValue().getSymmetry()) {
									if(eentry.getBranchExecution().contains(tr.getLabel())) {
										newEventName = tr.getLabel();  //select the one in the activited branch 
																		//as it will do less damage in case of wrong decision
																		//i.e. it will not impute the prefix and maximally
																		//marked as log-move
										found = true;
										break out;
									}
								}
							}
						}
						if(found==false) {
							newEventName = mappedLabels.get(0).getLabel();  //is it a wise decision???????
						}
					}
				}
				//if(mappedLabels.size() == 1) {  //the label is pure and need not to be checked for equivalence
					//First we check if the punctuation has been observed and now we need to select the relevant ND object
					//and do the gliding
				for(Entry<String, NonDeterministicRegion> entry : NDRegionsLocalPersonalisedCopy.entrySet()) {
					if(entry.getKey().equals(newEventName)) {
						isTraceInNonDeterministicRegion = false;

						for(branch eentry: entry.getValue().getSymmetry()) {
							if(!(eentry.activated)) {
								traceModelAlphabet.addAll(eentry.getBranchExecution());
								OCC2 = new OnlineConformanceChecker2(this.lms, false, newEventName);
								traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
								batchCCPrefAlign(traceStreamAlphabet);
								refreshUpdateTime();
								return last;
							}
						}
					}
				}
				//If the observed event is not a punctuation then if it belongs to one of the activated branch
				//then nothing needs to be done 
				for(Entry<String, NonDeterministicRegion> entry : NDRegionsLocalPersonalisedCopy.entrySet()) {
					//Boolean foundRelevantNDRegion = false;
					for(branch eentry: entry.getValue().getSymmetry()) {
						if(eentry.activated) {
							//foundRelevantNDRegion = true;
							if(eentry.getBranchExecution().contains(newEventName)) {
								traceModelAlphabet.add(newEventName);
								traceStreamAlphabet.clear();
								//traceStreamAlphabet.add(newEventName);
								traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
								last.setTraceCost(OCC2.processXLog(caseId, traceStreamAlphabet.get(traceStreamAlphabet.size()-1)));
								refreshUpdateTime();
								return last;									
							}
						}
					}						
				}

				//If the observed event is not a punctuation and it does not belongs to one of the activated branch
				// but rather un-activated branch(es) then we need to impute the prefix of the longest branch till this event and 
				//mark the branch(es) as activated
				int maxIndex = Integer.MIN_VALUE; 
				ArrayList<String> toBeImputed = new ArrayList<>();
				for(Entry<String, NonDeterministicRegion> entry : NDRegionsLocalPersonalisedCopy.entrySet()) {
					for(branch eentry: entry.getValue().getSymmetry()) {
						if(eentry.getBranchExecution().contains(newEventName)) {
							eentry.activated = true;
							int index = eentry.getBranchExecution().indexOf(newEventName);
							if (index> maxIndex) {
								maxIndex = index;
								toBeImputed.clear();
								toBeImputed.addAll(eentry.getBranchExecution().subList(0, index+1));
							}								
						}
					}
				}
				traceModelAlphabet.addAll(toBeImputed);
				traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
				OCC2 = new OnlineConformanceChecker2(this.lms, false, traceStreamAlphabet.get(traceStreamAlphabet.size()-1));
				batchCCPrefAlign(traceStreamAlphabet);
				refreshUpdateTime();
				return last;
					/*if(foundRelevantNDRegion) {
						for(Entry<Place, branch> eentry: entry.getValue().getSymmetry().entrySet()) {
							if(eentry.getValue().getBranchExecution().contains(newEventName)) {
								eentry.getValue().activated = true;
								int index = eentry.getValue().getBranchExecution().indexOf(newEventName);
								traceModelAlphabet.addAll(eentry.getValue().getBranchExecution().subList(0, index+1));
								traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
								OCC2 = new OnlineConformanceChecker2(this.lms, false, newEventName);
								traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
								batchCCPrefAlign(traceStreamAlphabet);
								refreshUpdateTime();
								return last;
							}
						}
					}*/
					
					
				/*}else {		//the label has duplication and needs to be checked for equivalence
					        //we get a set of the equivalent labels and then check if any of the 
					//getModelVersionOfTheEvent(newEventName);
					return null;
				}*/
				
				//case 1: the event is in already activated branch(es)
						//do nothing
				//case 2: the event is in non-activated branch(es)
						//impute the behavior before this event
						//mark the flag activated of the relevant branches to activated
						//batch alignment done after re-newing OCC2
				//case 3: the event is a punctuation
						//in this case we impute the non-activated branches (if any) and set isTraceInNonDeterministicRegion to false
						//batch alignment done after re-newing OCC2
				
			}else {
				traceModelAlphabet.add(newEventName); //the newly arrived event is added to the existing trace history
				traceStreamAlphabet.add(newEventName);
				last.setTraceCost(OCC2.processXLog(caseId, newEventName));
				refreshUpdateTime();
				return last;
			}
			/*traceModelAlphabet.add(newEventName); //the newly arrived event is added to the existing trace history
			traceStreamAlphabet.add(newEventName);*/
			/*if(isTraceInNonDeterministicRegion && lms.isInNonDeterministicRegion(newEventName)) {
				boolean enabled =false;
				PartialAlignment.State Marking state = OCC2.replayer.getDataStore().get(caseId).getState().getStateInModel();
				for(Transition tt: lms.labelsToModelElementsMap) {
					
				}
				if(OCC2.modelSemantics.getEnabledTransitions(state).contains(o))
				for (Transition t : OCC2.modelSemantics.getEnabledTransitions(state)) {
					if(t.getLabel().equals(newEventName)) {
						enabled = true;
						break;
					}
				}
				
				if(!enabled) {
					
				}
			}*/
			
			/*if(currentImputationSize>0 && imputationRevisitSelected==true && (traceModelAlphabet.size()-currentImputationSize)>=imputationRevisitWindowSize){
				//revisit the imputed prefix by extracting the part other than the imputed prefix and check the most probable
				//shortest path for it now
				//>>System.out.println("The imputation revision option is enabled so we have to revisit the imputation");
				prefixImputation.revisitPrefix(traceModelAlphabet);
				
				if(prefixImputation.imputationRevised) {
					traceModelAlphabet = prefixImputation.imputedTrace;
					traceStreamAlphabet = transformAlphabet(traceModelAlphabet);
					currentImputationSize = prefixImputation.imputationSize;
					//imputationHistory.put(1, prefixImputation.imputedPrefix);
					if(lms.isInNonDeterministicRegion(prefixImputation.revisedOrphan)) {
						OCC2 = new OnlineConformanceChecker2(this.lms, true, prefixImputation.revisedOrphan);
					}else {
						this.OCC2.applyGeneric();
					}
					batchCCPrefAlign(traceStreamAlphabet);
					//batchCCPrefAlign(traceModelAlphabet);
				}				
				
				imputationRevisitSelected=false; //to avoid multiple revisits
				//>>System.out.println(last.toString());
				
				//trace.clear();
				//trace.addAll(generateTraceFromAlignment(OCC2.replayer.getDataStore().get(caseId)));
				
				refreshUpdateTime();
				return last;
			}*/
			//>>System.out.println("The newly arrived event is NOT a case-starting event and added to the its existing history. Now the case is: " + trace);
			//last.setConformance(OCC2.processXLog(caseId, newEventName)>0.0?0.0:1.0);                         //The trace cost needs to be transformed into conformance score.
			/*last.setTraceCost(OCC2.processXLog(caseId, newEventName));*/
			//>>System.out.println(last.toString());
			
			//trace.clear();
			//trace.addAll(generateTraceFromAlignment(OCC2.replayer.getDataStore().get(caseId)));
			
			/*refreshUpdateTime();
			return last;*/
		}
		//compute last i.e., conformance etc. result to be sent back to environment ;
		//OnlineConformanceCalculator OCC = new OnlineConformanceCalculator();
		/*try {
			OnlineConformanceChecker2 OCC2 = new OnlineConformanceChecker2(this.lms);
			last.setConformance(OCC2.processXLog(XConceptExtension.instance().extractName(tr), newEventName)>0.0?0.0:1.0);                         //The trace cost needs to be transformed into conformance score.
			//OCC2.doReplay(trace);
			//receive completeness, confidence etc. results as well????
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
					
	}
	
	public ArrayList<String> transformAlphabet(ArrayList<String> inModelAlphabet){
		
		ArrayList<String> temp = new ArrayList<>();
		for(String event: inModelAlphabet) {
			for(Entry<String, Collection<Transition>> entry: lms.labelsToModelElementsMap.entrySet()) {
				for(Transition transition: entry.getValue()) {
					if(transition.getLabel().equals(event)) {
						temp.add(entry.getKey());
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
	
	public boolean isDistinctlyOrEquivalentlyExist(List<String> transitionsList, String newEventName) {
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
		
			
	}
	
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
		
		if(isNew){                                   //first observed event for a new case
			if (!lms.isCaseStartingEvent(newEventName)/*.isFirstEvent(newEventName)*/) {   //first observed event for a case is not a case-starter
				///// how to consider non-deterministic aspect here????????????
				/*//If the orphan event is located in one of the Non-Deterministic Region (NDR) then set the flag
				//isInNonDeterministicRegion to "true" and copy the transitions of that NDR to a local copy i.e.,
				//transitionsInNonDeterministicRegion
				for (Map.Entry<Integer, ArrayList<String>> entry : (lms.getNonDeterministicRegions()).entrySet()) {				   
				    isInNonDeterministicRegion = (entry.getValue()).contains(newEventName);
				    if(isInNonDeterministicRegion) {
				    	System.out.println("The event: " + newEventName + " is found to be located in NDR: " +  entry.getKey());
						transitionsInNonDeterministicRegion = new ArrayList<String>();
						transitionsInNonDeterministicRegion.addAll(entry.getValue());
						break;
					}
				}*/
				isTraceInNonDeterministicRegion = lms.isInNonDeterministicRegion(newEventName);
				if(isTraceInNonDeterministicRegion) {
					//>>System.out.println("The event: " + newEventName + " is found to be located in NDR");
					//locate the orphan event in the process tree and find the relevant ND regions and activities to this case;
					//Accordingly, the MM cost for these found activites will be set to 0
					//How will this effect the imputation window approach
				}
				
				
				//>>System.out.println("The arrived event is Orphan and needs Prefix imputation");
				prefixImputation =  new PrefixImputation(this.lms);
				prefixImputation.imputePrefix(newEventName);
				traceModelAlphabet = prefixImputation.imputedTrace;
				currentImputationSize = prefixImputation.imputationSize;
				//imputationHistory = new HashMap<Integer,ArrayList<String>>();
				//imputationHistory.put(0, prefixImputation.imputedPrefix);
				
				batchCCBehProf(traceModelAlphabet);
								
				/*for (int i = 0; i < trace.size(); i++) {
					if(i==0) {
						this.OCC1 = new OnlineConformanceChecker1(this.lms);
						OCC1.setLastActivityForCase(XConceptExtension.instance().extractName(trace.get(i)));
						//XConceptExtension.instance().extractName(t.get(0))
						continue;
					}
					last = OCC1.doReplay(XConceptExtension.instance().extractName(trace.get(i)));
					//l.add(i, classes.getByIdentity(XConceptExtension.instance().extractName(trace.get(i))).toString());
				}*/
				//>>System.out.println(last.toString());
				refreshUpdateTime();
				return last;
			}else {    //first observed event for a case is a case-starter
				traceModelAlphabet.add(newEventName);
				this.OCC1 = new OnlineConformanceChecker1(this.lms);
				OCC1.setLastActivityForCase(newEventName);	
				
				last.setConformance(1.0);
				//>>System.out.println("The arrived event is the case-starting event for a new case and conformance has been by-default set as 1.0");
				//last.setCompleteness(1.0);                  //??????
				//last.setConfidence(1.0);                    //??????????
				last.isLastObservedViolation(false);        //??????
				//>>System.out.println(last.toString());
				
				refreshUpdateTime();
				return last;
			}
		}else {                             //observed event belongs to an on-going case
			//trace.add(getXEvent(newEventName)); //the newly arrived event is added to the existing trace history
			traceModelAlphabet.add(newEventName);
			//>>System.out.println("The arrived belongs to an already existing case and added to its case history");
			if(currentImputationSize>0 && imputationRevisitSelected==true && (traceModelAlphabet.size()-currentImputationSize)==imputationRevisitWindowSize){
				//revisit the imputed prefix by extracting the part other than the imputed prefix and check the most probable
				//shortest path for it now
				//IF the Prefix is changed THEN reset the prefixImputation object OTHERWISE DO NOTHING
					//reset the OCC1 object
					//recmompute the conformance w.r.t to the new prefix and trace
				//>>System.out.println("The imputation revision option is enabled so we have to revisit the imputation");
				prefixImputation.revisitPrefix(traceModelAlphabet);
				
				if(prefixImputation.imputationRevised) {
					traceModelAlphabet = prefixImputation.imputedTrace;
					currentImputationSize = prefixImputation.imputationSize;
					//imputationHistory.put(1, prefixImputation.imputedPrefix);
					batchCCBehProf(traceModelAlphabet);
				}				
				
				imputationRevisitSelected=false; //to avoid multiple revisits
				//>>System.out.println(last.toString());
				refreshUpdateTime();
				return last;
				
			}
			last = OCC1.doReplay(newEventName);
			//>>System.out.println(last.toString());
			refreshUpdateTime();
			return last;
		}		
	}
}
