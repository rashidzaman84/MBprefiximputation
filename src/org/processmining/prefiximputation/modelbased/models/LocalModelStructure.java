package org.processmining.prefiximputation.modelbased.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.SerializationUtils;
import org.deckfour.xes.classification.XEventClasses;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.Pair;
import org.processmining.log.csv.CSVFile;
import org.processmining.log.csvimport.exception.CSVConversionException;
import org.processmining.models.graphbased.directed.analysis.ShortestPathFactory;
import org.processmining.models.graphbased.directed.analysis.ShortestPathInfo;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.transitionsystem.CoverabilityGraph;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.onlineconformance.models.ModelSemanticsPetrinet;
import org.processmining.plugins.petrinet.behavioralanalysis.CGGenerator;
import org.processmining.prefiximputation.inventory.NullConfiguration;
import org.processmining.processtree.Block;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.streamconformance.local.model.DirectFollowingRelation;
import org.processmining.streamconformance.utils.PetrinetHelper;
import org.processmining.streamconformance.utils.TSUtils;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;



/**
 * This class is a container of all structures needed for local online conformance checking.
 * 
 * @author Andrea Burattin
 */
public class LocalModelStructure {

	public ArrayList<String> caseStarterActivities = new ArrayList<String>();
	//private Map<Integer, ArrayList<String>> nonDeterministicRegions = new HashMap<Integer, ArrayList<String>>();
	public Set<String> nonDeterministicActivities = new HashSet<String>();  //should be Map<String, ArrayList<String>> as there
																// can be multiple non-deterministic regions 
	public ArrayList</*Place*/String> nonDeterministicPlaces = new ArrayList</*Place*/String>();
	//private String[] deterministicTransitions = {"E"};      //We shall also take transitions after E into account
														   //as E may get lost or G/H/F may arrive out-of-order	
	//HashSet<Block> NDBlocks = new HashSet<Block>();
	private Map<String, ArrayList<String>> shortestPrefixes = new HashMap<String, ArrayList<String>>();
	//we want to store all the shortest prefixes and also may me min. and max. non-conformant for MULTIPLE IMPUTATION. So the internal
	//HashMap<String, ArrayList<String>> the string will say if the prefix is second or third prefix of the external String i.e., orphan event.
	//private Map<String, HashMap<String, ArrayList<String>>> otherShortestPrefixes = new HashMap<String, HashMap<String, ArrayList<String>>>();
	private Set<DirectFollowingRelation> allowedDirectFollowingRelations = new HashSet<DirectFollowingRelation>();
	private Map<DirectFollowingRelation, Pair<Integer, Integer>> minMaxRelationsBefore = new HashMap<DirectFollowingRelation, Pair<Integer, Integer>>();
	private Map<DirectFollowingRelation, Integer> minRelationsAfter = new HashMap<DirectFollowingRelation, Integer>();
	public Petrinet net;	
	public ProcessTree tree;
	public Marking initialMarking;
	public Marking finalMarking;
	//public TransEvClassMapping mapping = null;
	public int imputationRevisitWindowSize;
	public String ccAlgoChoice;
	public XEventClasses eventClasses;
	public Map<org.processmining.models.graphbased.directed.petrinet.elements.Transition, String> modelElementsToLabelMap = new HashMap<>();
	public Map<String, Collection<org.processmining.models.graphbased.directed.petrinet.elements.Transition>> labelsToModelElementsMap = new HashMap<>();
	public TObjectDoubleMap<org.processmining.models.graphbased.directed.petrinet.elements.Transition> modelMoveCosts = new TObjectDoubleHashMap<>();
	public TObjectDoubleMap<String> labelMoveCosts = new TObjectDoubleHashMap<>();
	public OnlineConformanceChecker2 spareReplayer;
	public List<String> processModelAlphabet = new ArrayList<String>();
	public ShortestPathInfo<State, Transition> shortestPathCalculatorUnfoldedCopy;
	public CoverabilityGraph coverabilityGraphUnfoldedCopy;
	public ArrayList<org.processmining.models.graphbased.directed.petrinet.elements.Transition> NDStarterTransitions = new ArrayList<>();
	public ArrayList<org.processmining.models.graphbased.directed.petrinet.elements.Transition> NDEndingTransitions = new ArrayList<>();
	public org.processmining.models.graphbased.directed.petrinet.elements.Transition mainSplitter;
	public Marking mainSplitterOutMarking;
	public HashMap<org.processmining.models.graphbased.directed.petrinet.elements.Transition, Marking> NDEndingTransitionsEnteringMarkings= new HashMap<>();
	public HashMap<String, ArrayList<org.processmining.models.graphbased.directed.petrinet.elements.Transition>> executionSequences = new HashMap<>();
	public HashMap<String, NonDeterministicRegion> NDRegions = new HashMap<>();
	public CSVFile modelStreamMapping=null;
	
	
	public HashMap<String, NonDeterministicRegion> getNDRegionsCopy() {		
		HashMap<String, NonDeterministicRegion> copy = SerializationUtils.clone(NDRegions);	   
	    return copy;		
	}
	// cached values
	private Double maxOfMinRelationsAfter = null;
	
	/**
	 * Initializes the local model structure
	 * 
	 * @param coverabilityGraph
	 * @param coverabilityGraphUnfolded
	 * @param coverabilityGraphDualUnfolded
	 */
	
	public LocalModelStructure(CoverabilityGraph coverabilityGraph, CoverabilityGraph coverabilityGraphUnfolded,
			CoverabilityGraph coverabilityGraphDualUnfolded) {
		populateStructure(coverabilityGraph, coverabilityGraphUnfolded, coverabilityGraphDualUnfolded);
	}
	public LocalModelStructure(CoverabilityGraph coverabilityGraph, CoverabilityGraph coverabilityGraphUnfolded, CoverabilityGraph coverabilityGraphDualUnfolded,
			Petrinet net) {
		populateStructure(coverabilityGraph, coverabilityGraphUnfolded, coverabilityGraphDualUnfolded);
	}

	/**
	 * Initializes the local model structure
	 * 
	 * @param context
	 * @param net
	 * @param initMarking
	 * @throws Exception
	 */
	public LocalModelStructure(UIPluginContext context, Petrinet net, Marking initMarking) throws Exception {
		this.net = net;
		this.initialMarking = getInitialMarking(net);
		this.finalMarking = getFinalMarking(net);
		//this.eventClasses = getEventClasses(net);
		//this.mapping = getEventTransitionMapping(net,this.eventClasses);
		//populateStructure(context, net, initMarking);
		populateStructureForBehavioralProfiles(context, net, initMarking);
	}
	
	public LocalModelStructure(UIPluginContext context, Petrinet net, Marking initMarking, /*ProcessTree tree,*/ String ccAlgoChoice, int imputationRevisitWindowSize) throws Exception {
		this.net = net;
		//this.tree = tree;
		this.initialMarking = getInitialMarking(net);
		this.finalMarking = getFinalMarking(net);
		//this.eventClasses = getEventClasses(net);
		this.imputationRevisitWindowSize = imputationRevisitWindowSize;
		this.ccAlgoChoice = ccAlgoChoice;
		//this.mapping = getEventTransitionMapping(net,this.eventClasses);
		populateAppropriateStructure(context, net, initMarking, ccAlgoChoice);
		if(NullConfiguration.allowedDuplicateLabelApproximation) {
			this.spareReplayer = new OnlineConformanceChecker2(this, false, null);
		}
	}
	
	public LocalModelStructure(UIPluginContext context, Petrinet net, Marking initMarking, /*ProcessTree tree,*/ String ccAlgoChoice, int imputationRevisitWindowSize, CSVFile modelStreamMapping) throws Exception {
		this.net = net;
		//this.tree = tree;
		this.initialMarking = getInitialMarking(net);
		this.finalMarking = getFinalMarking(net);
		//this.eventClasses = getEventClasses(net);
		this.imputationRevisitWindowSize = imputationRevisitWindowSize;
		this.ccAlgoChoice = ccAlgoChoice;
		this.modelStreamMapping = modelStreamMapping;
		//this.mapping = getEventTransitionMapping(net,this.eventClasses);
		populateAppropriateStructure(context, net, initMarking, ccAlgoChoice);
		if(NullConfiguration.allowedDuplicateLabelApproximation) {
			this.spareReplayer = new OnlineConformanceChecker2(this, false, null);
		}
		
	}
	
	protected void populateAppropriateStructure(UIPluginContext context, Petrinet net, Marking initMarking, String ccAlgoChoice) throws Exception {
		switch(ccAlgoChoice) {
			case "Behavioral Profiles":
				populateStructureForBehavioralProfiles(context, net, initMarking);
				break;
			case "Prefix Alignment":
				populateStructureForPrefixAlignment(context, net, initMarking);
				//bypasspopulateStructureForPrefixAlignment(context, net, initMarking);
				break;
			default:
			    System.out.println("A wrong CC Algo Choice has been made during configuration");			   
			}
	}
	protected void bypasspopulateStructureForPrefixAlignment(UIPluginContext context, Petrinet net, Marking initMarking) throws Exception {
		populateCaseStarters(net);
		getCCEssentials(context, net);
		//calculateNDRegions();
		for(org.processmining.models.graphbased.directed.petrinet.elements.Transition tr: net.getTransitions()) {
			System.out.println(tr.getLabel());
		}
	}
	
	protected void populateStructureForPrefixAlignment(UIPluginContext context, Petrinet net, Marking initMarking) throws Exception {
		// build coverability graph of unfolded net
		Pair<Petrinet, Marking> unfoldedTotal = PetrinetHelper.unfold(context, net);
				CoverabilityGraph coverabilityGraphUnfolded = context.tryToFindOrConstructFirstNamedObject(
						CoverabilityGraph.class,
						CGGenerator.class.getAnnotation(Plugin.class).name(),
						null,
						null,
						unfoldedTotal.getFirst(),
						unfoldedTotal.getSecond());	
				
		/*test cg = new test();
				CGGenerator cgg = new CGGenerator();
				PetrinetSemantics semantics = PetrinetSemanticsFactory.regularPetrinetSemantics(Petrinet.class);				
				CoverabilityGraph coverabilityGraphUnfolded = cg.petriNetToCoverabilityGraph(context, net, initialMarking);*/
				
				//System.out.println("I am here");
				/*coverabilityGraphUnfoldedCopy = context.tryToFindOrConstructFirstNamedObject(
						CoverabilityGraph.class,
						CGGenerator.class.getAnnotation(Plugin.class).name(),
						null,
						null,
						unfoldedTotal.getFirst(),
						unfoldedTotal.getSecond());*/
				
				coverabilityGraphUnfoldedCopy = coverabilityGraphUnfolded;
				
				populateCaseStarters(net);
				//populateNonDeterministicActivities(tree);
				getCCEssentials(context, net);
				populateStructure(coverabilityGraphUnfolded);
				calculateNDRegions();
				//Now we need to define ND class.
				
				/*if(imputationRevisitWindowSize>0) {
					// build coverability graphs
					CoverabilityGraph coverabilityGraph = context.tryToFindOrConstructFirstNamedObject(
							CoverabilityGraph.class,
							CGGenerator.class.getAnnotation(Plugin.class).name(),
							null,
							null,
							net,
							initMarking);
					
					populateDirectFollowingRelations(coverabilityGraph);
				}*/
	}
	
	protected void populateStructureForBehavioralProfiles(PluginContext context, Petrinet net, Marking initMarking) throws Exception {
		
		// build coverability graphs
		CoverabilityGraph coverabilityGraph = context.tryToFindOrConstructFirstNamedObject(
				CoverabilityGraph.class,
				CGGenerator.class.getAnnotation(Plugin.class).name(),
				null,
				null,
				net,
				initMarking);
		
		// build coverability graph of unfolded net
		Pair<Petrinet, Marking> unfoldedTotal = PetrinetHelper.unfold(context, net);
		CoverabilityGraph coverabilityGraphUnfolded = context.tryToFindOrConstructFirstNamedObject(
				CoverabilityGraph.class,
				CGGenerator.class.getAnnotation(Plugin.class).name(),
				null,
				null,
				unfoldedTotal.getFirst(),
				unfoldedTotal.getSecond());
		
		// build coverability graph of dual net
		Pair<Petrinet, Marking> dualNet = PetrinetHelper.computeDual(context, net);
		Pair<Petrinet, Marking> unfoldedDualNet = PetrinetHelper.unfold(context, dualNet.getFirst());
		CoverabilityGraph coverabilityGraphDualUnfolded = context.tryToFindOrConstructFirstNamedObject(
				CoverabilityGraph.class,
				CGGenerator.class.getAnnotation(Plugin.class).name(),
				null,
				null,
				unfoldedDualNet.getFirst(),
				unfoldedDualNet.getSecond());
		
		populateCaseStarters(net);
		//populateNonDeterministicActivities(tree);
		populateStructure(coverabilityGraph, coverabilityGraphUnfolded, coverabilityGraphDualUnfolded);
	}	
	
	/**
	 * This method checks if the given relation is allowed by the model or not
	 * 
	 * @param relation
	 * @return
	 */
	public Boolean isInNonDeterministicRegion(String orphanEvent) {				
		if(nonDeterministicActivities.contains(orphanEvent)) {
			return true;
		}
		/*if(orphanEvent.InArray(nonDeterministicTransitions)) {
			return true;
		}*/
		return false;
	}
	
	public Petrinet getNet() {
		return this.net;
	}
	
	/*public Map<Integer, ArrayList<String>> getNonDeterministicRegions(){
		return nonDeterministicRegions;
	}*/
	
	/*public String[] getNonDeterministicRegions() {
		return nonDeterministicTransitions;
	}*/
	
	public ArrayList<String> getEquivalentModelLabels(String eventName){
		ArrayList<String> potentialModelLabels = new ArrayList<String>();
		if(labelsToModelElementsMap.containsKey(eventName)){
			for(org.processmining.models.graphbased.directed.petrinet.elements.Transition transition: labelsToModelElementsMap.get(eventName)) {
				potentialModelLabels.add(transition.getLabel());
			}			
		}
		return potentialModelLabels;
	}
	
	public ArrayList<String> getShortestPrefix(String orphanEvent) {		
		return shortestPrefixes.get(orphanEvent);
	}	
	public boolean isCaseStartingEvent(String newEventName) {
		//if the event is directly contained in the process model alphabet OR is label-equivalently available
		//System.out.println(newEventName);
		//System.out.println(labelsToModelElementsMap.get(newEventName));
		for(org.processmining.models.graphbased.directed.petrinet.elements.Transition transition: labelsToModelElementsMap.get(newEventName)){
			if(caseStarterActivities.contains(transition.getLabel())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isInProcessModelAlphabet(String newEventName) {
		if(labelsToModelElementsMap.containsKey(newEventName)) {
			return true;
		}else {
			return false;
		}
	}
	public boolean isAllowed(DirectFollowingRelation relation) {
		return allowedDirectFollowingRelations.contains(relation);
	}
	
	public Pair<Integer, Integer> getMinMaxRelationsBefore(DirectFollowingRelation relation) {
		return minMaxRelationsBefore.get(relation);
	}
	
	public Integer getMinRelationsAfter(DirectFollowingRelation relation) {
		return minRelationsAfter.get(relation);
	}
	
	public Double getMaxOfMinRelationsAfter() {
		if (maxOfMinRelationsAfter == null) {
			maxOfMinRelationsAfter = Double.MIN_VALUE;
			for (Integer v : minRelationsAfter.values()) {
				maxOfMinRelationsAfter = Math.max(maxOfMinRelationsAfter, v);
			}
		}
		return maxOfMinRelationsAfter;
	}
	
	private static Marking getFinalMarking(PetrinetGraph net) {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		return finalMarking;
	}

	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}
	/*private static XEventClasses getEventClasses(Petrinet net) {
		GetEventClassesOutOfModel getEventClassesOOM = new GetEventClassesOutOfModel(net);
		return getEventClassesOOM.manipulateModel();		
	}*/
	private void getCCEssentials(UIPluginContext context, Petrinet net) throws IOException, CSVConversionException {
		GetEventClassesOutOfModel getEventClassesOOM = new GetEventClassesOutOfModel(context, net, modelStreamMapping);
		getEventClassesOOM.manipulateModel();
		this.eventClasses = getEventClassesOOM.getXEventClasses();
		this.labelsToModelElementsMap = getEventClassesOOM.getLabelsToModelElementsMap();
		this.modelElementsToLabelMap = getEventClassesOOM.getModelElementsToLabelMap();
		this.modelMoveCosts = getEventClassesOOM.getModelMoveCosts();
		this.labelMoveCosts = getEventClassesOOM.getLabelMoveCosts();	
		this.processModelAlphabet = getEventClassesOOM.getProcessModelAlphabet();
		//this.NDStarterTransitions = getEventClassesOOM.getNDStarterTransitions();
		this.NDEndingTransitions = getEventClassesOOM.getNDEndingTransitions();
		this.mainSplitter = getEventClassesOOM.mainSplitter;
		this.mainSplitterOutMarking = getEventClassesOOM.mainSplitterOutMarking;
		this.NDEndingTransitionsEnteringMarkings = getEventClassesOOM.NDEndingTransitionsEnteringMarkings;
	}
	/*private static TransEvClassMapping getEventTransitionMapping(Petrinet net, XEventClasses eventClasses) {
		GetEventClassesOutOfModel getEventClassesOOM = new GetEventClassesOutOfModel(net);
		return getEventClassesOOM.mapEventsToTransitions(net, eventClasses);
	}*/
	
	
	
	
	
	protected void populateStructure(
			CoverabilityGraph coverabilityGraph,
			CoverabilityGraph coverabilityGraphUnfolded,
			CoverabilityGraph coverabilityGraphDualUnfolded) {
		
		populateDirectFollowingRelations(coverabilityGraph);
		//populateMinMaxBefore(coverabilityGraph);
		populateMinMaxBefore(coverabilityGraphUnfolded);
		populateMinAfter(coverabilityGraphDualUnfolded);
		//populateCaseStarters(net);
	}
	
	protected void populateDirectFollowingRelations(CoverabilityGraph coverabilityGraph) {
		// populate allowed direct following relations
		for (State s : coverabilityGraph.getNodes()) {
			System.out.println("s is : " + s.toString());
			for (Transition first : TSUtils.getIncomingNonTau(s, new HashSet<Transition>())) {
				System.out.println("first is : " + first.toString());
				for (Transition second : TSUtils.getOutgoingNonTau(s, new HashSet<Transition>())) {
					System.out.println("second is : " + second.toString());
					allowedDirectFollowingRelations.add(new DirectFollowingRelation(first.getLabel(), second.getLabel()));
					//System.out.println(allowedDirectFollowingRelations.toString());
				}
			}
		}
		System.out.println(allowedDirectFollowingRelations.toString());
	}
	
	protected void populateMinMaxBefore(CoverabilityGraph coverabilityGraphUnfolded) {
		ShortestPathInfo<State, Transition> shortestPathCalculatorUnfolded = ShortestPathFactory.calculateAllShortestDistanceDijkstra(coverabilityGraphUnfolded);
		
		// populate min/max relations BEFORE from unfolded model
		State startState = null;
		for (Object s : coverabilityGraphUnfolded.getStates()) {
			if (coverabilityGraphUnfolded.getInEdges(coverabilityGraphUnfolded.getNode(s)).isEmpty()) {
				startState = coverabilityGraphUnfolded.getNode(s);
				break;
			}
		}
		
		for (State s : coverabilityGraphUnfolded.getNodes()) {
			System.out.println("s is : " + s.toString());
			for (Transition first : TSUtils.getIncomingNonTau(s, new HashSet<Transition>())) {
				System.out.println("first is : " + first.toString());
				for (Transition second : TSUtils.getOutgoingNonTau(s, new HashSet<Transition>())) {
					System.out.println("second is : " + second.toString());
					String firstLabel = TSUtils.getTransitionLabel(first);
					String secondLabel = TSUtils.getTransitionLabel(second);
					
					//DirectFollowingRelation relation = new DirectFollowingRelation(firstLabel, secondLabel);
					if (imputationRevisitWindowSize>0) {
						allowedDirectFollowingRelations.add(new DirectFollowingRelation(first.getLabel(), second.getLabel()));
					}
					State targetState = first.getTarget();
					System.out.println("targetState is : " + targetState.toString());
					List<DirectFollowingRelation> path = TSUtils.getShortestPath(shortestPathCalculatorUnfolded, startState, targetState);
					System.out.println("path: " + path.toString());
					ArrayList<String> temp = new ArrayList<String>();
					temp = pathToPrefix(path,firstLabel, secondLabel);
					if (shortestPrefixes.containsKey(second.toString())) {
						if ((shortestPrefixes.get(second.toString())).size()>(temp.size())) {     //??all the shortest equal-length prefixes for an event shall be stored as there might be multiple prefixes possible
							shortestPrefixes.put(second.toString(), temp);
						}
						
					}else {
						shortestPrefixes.put(second.toString(), temp);
					}
					
					//System.out.println(shortestPrefixes);
					//Integer min = path.size();
					//Integer max = path.size();
					/*if (minMaxRelationsBefore.containsKey(relation)) {
						Pair<Integer, Integer> minMax = minMaxRelationsBefore.get(relation);
						min = Math.min(min, minMax.getFirst());
						max = Math.max(max, minMax.getSecond());
					}*/
					//minMaxRelationsBefore.put(relation, new Pair<Integer, Integer>(min, max));
				}
			}
			for(ArrayList<String> entry: shortestPrefixes.values()) {
				Iterator<String> iterator = entry.iterator();
				 while (iterator.hasNext()) {
					 String label = iterator.next();
					 if (label.contains("tau")) {
					        entry.remove(label);
					      }					 
				 }
				
			}
			System.out.println(shortestPrefixes);
		}
		/*for (Map.Entry<DirectFollowingRelation, Pair<Integer, Integer>> entry : minMaxRelationsBefore.entrySet()) {
		     System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}*/
	}
	
	public void calculateNDRegions() {
		if(Objects.isNull(mainSplitter)) {
			System.out.println("No splitter found");
			return;
		}
		String start = mainSplitter.getLabel();
		State startState = null;
		
		for (State s : coverabilityGraphUnfoldedCopy.getNodes()) {
			for (Transition first : TSUtils.getOutgoingNonTau(s, new HashSet<Transition>())) {
				if(TSUtils.getTransitionLabel(first).equals(start)) {
					startState = s;  //add break below
				}
			}			
		}		
		Iterator iterator = NDEndingTransitions.iterator();
		while(iterator.hasNext()) {
			org.processmining.models.graphbased.directed.petrinet.elements.Transition target = (org.processmining.models.graphbased.directed.petrinet.elements.Transition) iterator.next();
			State targetState = null;
			String end = target.getLabel();
			
			ArrayList<String> temp = new ArrayList<String>();
		/*}
		for(org.processmining.models.graphbased.directed.petrinet.elements.Transition target : NDEndingTransitions) {
			String end = target.getLabel();
			State targetState = null;*/
			for (State s : coverabilityGraphUnfoldedCopy.getNodes()) {				
				for(Transition second : TSUtils.getIncomingNonTau(s, new HashSet<Transition>())) {
					if(TSUtils.getTransitionLabel(second).equals(end)) {
						targetState = s;
						ArrayList<String> temp2 =  getShortestPath(shortestPathCalculatorUnfoldedCopy, startState, targetState);
						if(temp2.size()>temp.size()) {
							temp.clear();
							temp.addAll(temp2);
						}
					}
				}
			}
			//ArrayList<String> temp = new ArrayList<String>();
			temp =/*TSUtils.*/getShortestPath(shortestPathCalculatorUnfoldedCopy, startState, targetState);
			
			//System.out.println(start + "-->" + end + " :: " + path);
			//ArrayList<String> temp = new ArrayList<String>();
			//temp = pathToPrefix(path,start, end);
			System.out.println(temp);
			if(!temp.isEmpty()) {
				if(!isTrueTerminal(temp,end)) {
					iterator.remove();
					executionSequences.remove(end);
				}else {
					nonDeterministicActivities.addAll(temp.subList(1, temp.size()));
				}
			}
			
		}	
		
		populateNDObjects();
	}
	
	public boolean isTrueTerminal(ArrayList<String> temp, String terminalActivityCandidate) {
		ArrayList<org.processmining.models.graphbased.directed.petrinet.elements.Transition> tempTrace = new ArrayList<>();
		org.processmining.models.graphbased.directed.petrinet.elements.Transition terminalActivityCandidatetransition = null;
		for(String str: temp) {
			for(org.processmining.models.graphbased.directed.petrinet.elements.Transition tran : net.getTransitions()) {
				if(tran.getLabel().equals(str)) {
					tempTrace.add(tran);
				}
				if(tran.getLabel().equals(terminalActivityCandidate)) {
					terminalActivityCandidatetransition = tran;
				}
			}
		}
		System.out.println(tempTrace.toString());
		executionSequences.put(terminalActivityCandidate, tempTrace);
		tempTrace.remove(0);
		//executionSequences.put(terminalActivityCandidate, tempTrace);
		ModelSemanticsPetrinet<Marking> modelSemantics = ModelSemanticsPetrinet.Factory.construct(net);
		Marking mark = this.mainSplitterOutMarking;
		for(int i=0; i<tempTrace.size();i++) {
			Marking inputMarking = constructInputMarking(tempTrace.get(i));
			if(inputMarking.compareTo(mark)==0 && i<tempTrace.size()-1) {
				return false;
			}
			mark = modelSemantics.execute(mark, tempTrace.get(i) );			
		}
		System.out.println(this.NDEndingTransitionsEnteringMarkings.get(terminalActivityCandidatetransition));
		if(mark.compareTo(this.NDEndingTransitionsEnteringMarkings.get(terminalActivityCandidatetransition))==0) {
			System.out.println("true terminal");
			return true;
		}else {
			return false;
		}
	}
	
	public Marking constructInputMarking(org.processmining.models.graphbased.directed.petrinet.elements.Transition transit) {
		Marking constructedMarking = new Marking();
			
		for(PetrinetEdge p : net.getEdges()) {
			if(p.getTarget().equals(transit)) {
				//System.out.println(p.getTarget() );
				for(Place pl: net.getPlaces()) {
					if(pl.getLabel().equals(p.getSource().toString())) {
						//System.out.println(pl);
						constructedMarking.add(pl);
					}
				}
				//mainSplitterOutMarking.add( p.getLabel());
			}
		}
		
		return constructedMarking;
	}
	
	public void populateNDObjects() {
		for(Entry<String, ArrayList<org.processmining.models.graphbased.directed.petrinet.elements.Transition>> entry: executionSequences.entrySet()) {
			NDRegions.put(entry.getKey(), new NonDeterministicRegion());
			int index=0;
			for(Place p : mainSplitterOutMarking.baseSet()) {
				index++;
				ArrayList<String> temporary = extract(p, entry.getValue());
				NonDeterministicRegion.branch b1= NDRegions.get(entry.getKey()).new branch(entry.getKey() + "-branch " + p.getLabel()) ;
				b1.setBranchExecution(temporary);
				NDRegions.get(entry.getKey()).addToSymmetry(/*p, */b1);
			}
		}
		 
		
	}
	
	public ArrayList<String> extract(Place p,  ArrayList<org.processmining.models.graphbased.directed.petrinet.elements.Transition> array) {
		ArrayList<String> temp = new ArrayList<>();
		ModelSemanticsPetrinet<Marking> modelSemantics = ModelSemanticsPetrinet.Factory.construct(net);
		Marking mr = new Marking();
		mr.add(p);
		org.processmining.models.graphbased.directed.petrinet.elements.Transition match = null;
		do {
			match = null;
			outerloop:
			for(org.processmining.models.graphbased.directed.petrinet.elements.Transition trOut : modelSemantics.getEnabledTransitions(mr)) {
				Iterator iter = array.iterator();
				while(iter.hasNext()) {
					org.processmining.models.graphbased.directed.petrinet.elements.Transition trIn = (org.processmining.models.graphbased.directed.petrinet.elements.Transition) iter.next();
					if (trIn.getLabel().equals(trOut.getLabel())){
						match = trIn;
						//mr = modelSemantics.execute(mr, match);
						iter.remove();
						 break outerloop;
					}
				}
				/*for(org.processmining.models.graphbased.directed.petrinet.elements.Transition trIn : array) {
					if (trIn.getLabel().equals(trOut.getLabel())){
						match = trIn;
						//mr = modelSemantics.execute(mr, match);
						 break outerloop;
					}
				}*/
				//mr = modelSemantics.execute(mr, matlooba);
			}
			if(match == null) {
				;
			}else {
				System.out.println(match.getLabel());
				temp.add(match.getLabel());
				mr = modelSemantics.execute(mr, match);
				//matlooba=null;
			}
			
		}while(modelSemantics.getEnabledTransitions(mr)!=null && match!=null);
		return temp;
	}
	
	protected void populateShortestPrefixes(CoverabilityGraph coverabilityGraphUnfolded) {
		ShortestPathInfo<State, Transition> shortestPathCalculatorUnfolded = ShortestPathFactory.calculateAllShortestDistanceDijkstra(coverabilityGraphUnfolded);
		shortestPathCalculatorUnfoldedCopy = ShortestPathFactory.calculateAllShortestDistanceDijkstra(coverabilityGraphUnfolded);
		// populate min/max relations BEFORE from unfolded model
		State startState = null;
		
		for (Object s : coverabilityGraphUnfolded.getStates()) {
			if (coverabilityGraphUnfolded.getInEdges(coverabilityGraphUnfolded.getNode(s)).isEmpty()) {
				startState = coverabilityGraphUnfolded.getNode(s);
				break;
			}
		}
		
		for (State s : coverabilityGraphUnfolded.getNodes()) {
			System.out.println("s is : " + s.toString());
			for (Transition first : TSUtils.getIncomingNonTau(s, new HashSet<Transition>())) {
				System.out.println("first is : " + first.toString());
				for (Transition second : TSUtils.getOutgoingNonTau(s, new HashSet<Transition>())) {
					System.out.println("second is : " + second.toString());
					String firstLabel = TSUtils.getTransitionLabel(first);
					String secondLabel = TSUtils.getTransitionLabel(second);
					
					//DirectFollowingRelation relation = new DirectFollowingRelation(firstLabel, secondLabel);
					if (imputationRevisitWindowSize>0) {
						allowedDirectFollowingRelations.add(new DirectFollowingRelation(first.getLabel(), second.getLabel()));
					}
					State targetState = first.getTarget();
					System.out.println("targetState is : " + targetState.toString());
					List<DirectFollowingRelation> path = TSUtils.getShortestPath(shortestPathCalculatorUnfolded, startState, targetState);
					System.out.println("path: " + path.toString());
					//System.out.println(shortestPathCalculatorUnfolded.getShortestPath(startState, targetState));
					
					ArrayList<String> temp = new ArrayList<String>();
					temp = pathToPrefix(path,firstLabel, secondLabel);
					
					System.out.print(second.toString() + ": " + getShortestPath(shortestPathCalculatorUnfoldedCopy, startState, targetState));
					if (shortestPrefixes.containsKey(second.toString())) {
						if ((shortestPrefixes.get(second.toString())).size()>(temp.size())) {     //??all the shortest equal-length prefixes for an event shall be stored as there might be multiple prefixes possible
							shortestPrefixes.put(second.toString(), temp);
						}
						
					}else {
						shortestPrefixes.put(second.toString(), temp);
					}
					
					//System.out.println(shortestPrefixes);
					//Integer min = path.size();
					//Integer max = path.size();
					/*if (minMaxRelationsBefore.containsKey(relation)) {
						Pair<Integer, Integer> minMax = minMaxRelationsBefore.get(relation);
						min = Math.min(min, minMax.getFirst());
						max = Math.max(max, minMax.getSecond());
					}*/
					//minMaxRelationsBefore.put(relation, new Pair<Integer, Integer>(min, max));
				}
			}			
		}
		Iterator<ArrayList<String>> iteratorOuter = shortestPrefixes.values().iterator();
		while (iteratorOuter.hasNext()) {
			ArrayList<String> currentPrefix = iteratorOuter.next();
			Iterator<String> iteratorInner = currentPrefix.iterator();
			 while (iteratorInner.hasNext()) {
				 String label = iteratorInner.next();
				 if (label.contains("tau")) {
					 iteratorInner.remove();
				 }
			 }
			
		}
		System.out.println(shortestPrefixes);
		/*for (Map.Entry<DirectFollowingRelation, Pair<Integer, Integer>> entry : minMaxRelationsBefore.entrySet()) {
		     System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		}*/
	}
	
	protected void populateMinAfter(CoverabilityGraph coverabilityGraphDualUnfolded) {
		ShortestPathInfo<State, Transition> shortestPathCalculatorDualUnfolded = ShortestPathFactory.calculateAllShortestDistanceDijkstra(coverabilityGraphDualUnfolded);
		
		// populate min/max relations AFTER the current one
		State startStateDual = null; // start state on dual is end state on original
		for (Object s : coverabilityGraphDualUnfolded.getStates()) {
			if (coverabilityGraphDualUnfolded.getInEdges(coverabilityGraphDualUnfolded.getNode(s)).isEmpty()) {
				startStateDual = coverabilityGraphDualUnfolded.getNode(s);
				break;
			}
		}
		
		for (State s : coverabilityGraphDualUnfolded.getNodes()) {
			if (!coverabilityGraphDualUnfolded.getInEdges(s).isEmpty() && !coverabilityGraphDualUnfolded.getOutEdges(s).isEmpty()) {
				for (Transition first : TSUtils.getIncomingNonTau(s, new HashSet<Transition>())) {
					for (Transition second : TSUtils.getOutgoingNonTau(s, new HashSet<Transition>())) {
						String firstLabel = TSUtils.getTransitionLabel(first);
						String secondLabel = TSUtils.getTransitionLabel(second);
						
						State targetState = second.getSource();
						List<DirectFollowingRelation> path = TSUtils.getShortestPath(shortestPathCalculatorDualUnfolded, startStateDual, targetState);
						DirectFollowingRelation relation = new DirectFollowingRelation(secondLabel, firstLabel);
						Integer min = path.size();
						if (minRelationsAfter.containsKey(relation)) {
							min = Math.min(min, minRelationsAfter.get(relation));
						}
						minRelationsAfter.put(relation, min);
					}
				}
			}
		}
	}
	
	public void populateCaseStarters(PetrinetGraph net) {
				
		for (org.processmining.models.graphbased.directed.petrinet.elements.Transition tran: net.getTransitions()) {
			if ((tran.getVisiblePredecessors()).isEmpty()){
				System.out.println("found: " + tran.toString());
				caseStarterActivities.add(tran.toString());
			}
		}
	}
			
	public ArrayList<String> pathToPrefix(List<DirectFollowingRelation> path,String first, String second) {
		ArrayList<String> result = new ArrayList<>();
		//String[] result;
		boolean flag = true;
		if(!path.isEmpty()) {
			for (DirectFollowingRelation r : path) {
				if (flag==true) {
					result.add(r.getFirst());					
					flag=false;
				}
				result.add(r.getSecond());
				
			}
			//result.add(second);  //this should be commented out to not append the observed event to the prefix
			return result;
			
		}else {
			result.add(first);
			//result.add(second);  //this should be commented out to not append the observed event to the prefix
			return result;
		}
		
		
	}
	
	
	
	protected void populateStructure(CoverabilityGraph coverabilityGraphUnfolded) {
		
		//populateDirectFollowingRelations(coverabilityGraph);
		//populateMinMaxBefore(coverabilityGraph);
		populateShortestPrefixes(coverabilityGraphUnfolded);
		//populateMinAfter(coverabilityGraphDualUnfolded);
		//populateCaseStarters(net);
	}
	
	protected void populateNonDeterministicActivities(ProcessTree pTree) {
		//HashSet<Node> NDNodes = new HashSet<Node>();
		//HashSet<Block> NDBlocks = new HashSet<Block>();
		for(Node n: pTree.getNodes()) {
			//System.out.println("Target found: " + tree2.getType(n).toString());
			if((pTree.getType(n).toString()== "Manual task" || pTree.getType(n).toString()== "Automatic task")) {
				for(Block m: n.getParents()) {
					if(pTree.getType(m).toString()=="And") {
						//NDNodes.add(n);
						nonDeterministicActivities.add(n.getName());
						//NDBlocks.add(m);
					}
				}
			}
		}
		/*nonDeterministicPlaces.add("n5");
		nonDeterministicPlaces.add("n26");
		nonDeterministicPlaces.add("n13");
		nonDeterministicPlaces.add("n14");
		nonDeterministicPlaces.add("n15");
		nonDeterministicPlaces.add("n16");
		nonDeterministicPlaces.add("n17");
		nonDeterministicPlaces.add("n19");
		nonDeterministicPlaces.add("n22");
		nonDeterministicPlaces.add("n23");
		nonDeterministicPlaces.add("n24");
		nonDeterministicPlaces.add("n25");
		nonDeterministicPlaces.add("n21");
		nonDeterministicPlaces.add("n20");
		
		nonDeterministicActivities.add("A_FINALIZED");
		nonDeterministicActivities.add("O_SELECTED_1");
		nonDeterministicActivities.add("O_SELECTED_2");
		nonDeterministicActivities.add("O_CREATED");
		nonDeterministicActivities.add("O_SENT");
		nonDeterministicActivities.add("O_SENT_BACK_1");
		nonDeterministicActivities.add("O_SENT_BACK_2");
		nonDeterministicActivities.add("O_CANCELLED_2");
		nonDeterministicActivities.add("A_CANCELLED_1");
		nonDeterministicActivities.add("A_DECLINED_4");
		nonDeterministicActivities.add("O_ACCEPTED");
		nonDeterministicActivities.add("O_DECLINED");
		nonDeterministicActivities.add("O_CANCELLED_1");
		nonDeterministicActivities.add("A_APPROVED");*/
		
		
		System.out.println(nonDeterministicActivities.toString());
		/*for(Node node : NDNodes) {
			nonDeterministicActivities.add(node.getName());
			System.out.println(node.getName());
		}*/
	}
	public static ArrayList<String> getShortestPath(ShortestPathInfo<State, Transition> calculator, State from, State to) {
		List<DirectFollowingRelation> result = new LinkedList<DirectFollowingRelation>();
		ArrayList<String> trace = new ArrayList<>();
		State prev = null;
		Transition prevTransition = null;
		System.out.println("///////////////////////////////////////////////////////////////////////////////////////////");
		System.out.println(calculator.getShortestPath(from, to));
		for (State s : calculator.getShortestPath(from, to)) {
			if (prev != null) {
				Transition t = TSUtils.getConnection(prev, s);
				if (t != null && !TSUtils.isTransitionTau(t)) {
					if (prevTransition != null) {
						DirectFollowingRelation newRel = new DirectFollowingRelation(prevTransition.getLabel(), t.getLabel());
						//System.out.print(prevTransition.getLabel() + ",");
						trace.add(prevTransition.getLabel());
						if (!result.contains(newRel)) {
							result.add(newRel);
						}
					}
					prevTransition = t;
				}
			}
			prev = s;
		}
		return trace;
	}
	
	/*public void printNicely(PrintStream out) {
		List<String> alphabet = new LinkedList<String>();
		for (DirectFollowingRelation rel : allowedDirectFollowingRelations) {
			if (!alphabet.contains(rel.getFirst())) {
				alphabet.add(rel.getFirst());
			}
			if (!alphabet.contains(rel.getSecond())) {
				alphabet.add(rel.getSecond());
			}
		}
		Collections.sort(alphabet);
		
		// print direct following relations
		out.println("DIRECT FOLLOWING RELATIONS");
		out.println("==========================");
		out.print("\t");
		for (String a : alphabet) {
			out.print(a + "\t");
		}
		out.println("");
		for (String a : alphabet) {
			out.print(a + "\t");
			for (String b : alphabet) {
				if (allowedDirectFollowingRelations.contains(new DirectFollowingRelation(a, b))) {
					out.print(">");
				} else {
					out.print(" ");
				}
				out.print("\t");
			}
			out.println("");
		}
		
		// print min/max from start

		out.println("MIN/MAX RELATIONS FROM START");
		out.println("============================");
		out.print("\t");
		for (String a : alphabet) {
			out.print(a + "\t");
		}
		out.println("");
		for (String a : alphabet) {
			out.print(a + "\t");
			for (String b : alphabet) {
				DirectFollowingRelation r = new DirectFollowingRelation(a, b);
				if (minMaxRelationsBefore.containsKey(r)) {
					out.print(minMaxRelationsBefore.get(r).getFirst() + "/" + minMaxRelationsBefore.get(r).getSecond());
				} else {
					out.print(" ");
				}
				out.print("\t");
			}
			out.println("");
		}
		
		// print min to end
		out.println("MIN RELATIONS TO END");
		out.println("====================");
		out.print("\t");
		for (String a : alphabet) {
			out.print(a + "\t");
		}
		out.println("");
		for (String a : alphabet) {
			out.print(a + "\t");
			for (String b : alphabet) {
				DirectFollowingRelation r = new DirectFollowingRelation(a, b);
				if (minRelationsAfter.containsKey(r)) {
					out.print(minRelationsAfter.get(r));
				} else {
					out.print(" ");
				}
				out.print("\t");
			}
			out.println("");
		}
	}*/
}
