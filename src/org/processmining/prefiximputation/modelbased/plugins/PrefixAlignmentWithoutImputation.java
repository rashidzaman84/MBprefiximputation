package org.processmining.prefiximputation.modelbased.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.javatuples.Triplet;
import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.io.PNMLSerializer;
import org.joda.time.DateTimeComparator;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.EfficientPetrinetSemantics;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.impl.EfficientPetrinetSemanticsImpl;
import org.processmining.onlineconformance.algorithms.IncrementalReplayer;
import org.processmining.onlineconformance.models.IncrementalReplayResult;
import org.processmining.onlineconformance.models.ModelSemanticsPetrinet;
import org.processmining.onlineconformance.models.Move;
import org.processmining.onlineconformance.models.PartialAlignment;
import org.processmining.onlineconformance.parameters.IncrementalReplayerParametersImpl;
import org.processmining.onlineconformance.parameters.IncrementalRevBasedReplayerParametersImpl;
import org.processmining.prefiximputation.inventory.NullConfiguration;
import org.processmining.prefiximputation.tests.TimeStampsBasedLogToStreamConverter;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;


@Plugin(name = "Compute Prefix Alignments Incrementally - Without Imputation", parameterLabels = {"Model", "Event Data" }, returnLabels = { "Replay Result" }, returnTypes = { IncrementalReplayResult.class })
public class PrefixAlignmentWithoutImputation {
	
	@UITopiaVariant(author = "S.J. van Zelst", email = "s.j.v.zelst@tue.nl", affiliation = "Eindhoven University of Technology")
	@PluginVariant(variantLabel = "Compute Prefix Alignments Incrementally", requiredParameterLabels = { 0, 1})
	
	public IncrementalReplayResult<String, String, Transition, Marking, ? extends PartialAlignment<String, Transition, Marking>> apply(
			final UIPluginContext context, final Petrinet net, XLog log) {
	//public static void main(String[] args) throws Exception {
		
		Map<Transition, String> modelElementsToLabelMap = new HashMap<>();
		Map<String, Collection<Transition>> labelsToModelElementsMap = new HashMap<>();
		TObjectDoubleMap<Transition> modelMoveCosts = new TObjectDoubleHashMap<>();
		TObjectDoubleMap<String> labelMoveCosts = new TObjectDoubleHashMap<>();
		XFactory xesFactory = new XFactoryBufferedImpl();
		//PrefixAlignmentConstrained pac= new PrefixAlignmentConstrained();
		
		//String petrinetFile = /*"D:\\TEST\\simplest.pnml";*/ "D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Scenario 1\\CCC19 - Model PN_modified.pnml";
		//String petrinetFile = "D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Process Models BPI 2012 from Boudewijn\\Model_O.pnml";
		//String logFile = /*"D:\\TEST\\cpnToolsSimulationLog.mxml";*/ "D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Scenario 1\\CPN Model\\consolidated\\cpnToolsSimulationLog.mxml";
		//String logFile ="D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Process Models BPI 2012 from Boudewijn\\Only_O_Events.xes";
		/*
		String petrinetFile = NullConfiguration.petriNetFilePath;
		String logFile = NullConfiguration.eventLogFilePath;
		*/
		//Petrinet net = constructNet(petrinetFile);
		Marking initialMarking = getInitialMarking(net);
		Marking finalMarking = getFinalMarking(net);
		//finalMarking.clear();
		/*XLog log = null;
		XEventClassifier eventClassifier;
		try {
			log = new XUniversalParser().parse(new File(logFile)).iterator().next();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		setupLabelMap(net, modelElementsToLabelMap, labelsToModelElementsMap );
		setupModelMoveCosts(net, modelMoveCosts, labelMoveCosts);
		IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition> parameters = new IncrementalRevBasedReplayerParametersImpl<>();
		parameters.setUseMultiThreading(false);
		parameters.setLabelMoveCosts(labelMoveCosts);
		parameters.setLabelToModelElementsMap(labelsToModelElementsMap);
		parameters.setModelMoveCosts(modelMoveCosts);
		parameters.setModelElementsToLabelMap(modelElementsToLabelMap);
		parameters.setSearchAlgorithm(IncrementalReplayer.SearchAlgorithm.A_STAR);
		parameters.setUseSolutionUpperBound(false);
		parameters.setLookBackWindow(Integer.MAX_VALUE);
		parameters.setExperiment(false);
		if (parameters.isExperiment()) {
			;//return applyMeasurementAware(context, net, initialMarking, finalMarking, log, parameters);
		} else {
			applyGeneric(net, initialMarking, finalMarking, log, parameters);
		}
		return null;
	}

	/*@UITopiaVariant(author = "S.J. van Zelst", email = "s.j.v.zelst@tue.nl", affiliation = "Eindhoven University of Technology")
	@PluginVariant(variantLabel = "Compute Prefix Alignments Incrementally", requiredParameterLabels = { 0, 3 })
	public IncrementalReplayResult<String, String, Transition, Marking, ? extends PartialAlignment<String, Transition, Marking>> apply(
			final UIPluginContext context, final AcceptingPetriNet accNet, final XLog log) {
		Petrinet net = accNet.getNet();
		Marking initialMarking = accNet.getInitialMarking();
		Marking finalMarking = new Marking(); // empty marking is default
		for (Marking m : accNet.getFinalMarkings()) { //currently picks the first final marking
			finalMarking = m;
			break;
		}
		return apply(context, net, initialMarking, finalMarking, log);
	}*/

	/*public IncrementalReplayResult<String, String, Transition, Marking, PartialAlignment<String, Transition, Marking>> apply(
			final PluginContext context, final Petrinet net, final Marking initialMarking, final Marking finalMarking,
			final XLog log, final IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition> parameters) {
		return applyGeneric(net, initialMarking, finalMarking, log, parameters);
	}

	public IncrementalReplayResult<String, String, Transition, Marking, MeasurementAwarePartialAlignment<String, Transition, Marking>> applyMeasurementAware(
			final PluginContext context, final Petrinet net, final Marking initialMarking, final Marking finalMarking,
			final XLog log, final IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition> parameters) {
		return applyGeneric(context, net, initialMarking, finalMarking, log, parameters);
	}*/

	@SuppressWarnings("unchecked")
	public static <A extends PartialAlignment<String, Transition, Marking>> void applyGeneric(
			final Petrinet net, final Marking initialMarking, final Marking finalMarking,
			/*final*/ XLog log, final IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition> parameters) {
		ModelSemanticsPetrinet<Marking> modelSemantics = ModelSemanticsPetrinet.Factory.construct(net);
		Map<Transition, String> labelsInPN = new HashMap<Transition, String>();
		for (Transition t : net.getTransitions()) {
			if (!t.isInvisible()) {
				labelsInPN.put(t, t.getLabel());
			}
		}
		if (parameters.isExperiment()) {
			/*Map<String, MeasurementAwarePartialAlignment<String, Transition, Marking>> store = new HashMap<>();
			IncrementalReplayer<Petrinet, String, Marking, Transition, String, MeasurementAwarePartialAlignment<String, Transition, Marking>, IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition>> replayer = IncrementalReplayer.Factory
					.construct(initialMarking, finalMarking, store, modelSemantics, parameters, labelsInPN,
							IncrementalReplayer.Strategy.REVERT_BASED);
			return (IncrementalReplayResult<String, String, Transition, Marking, A>) processXLog(log, net,
					initialMarking, replayer);*/;
		} else {
			Map<String, PartialAlignment<String, Transition, Marking>> store = new HashMap<>();
			IncrementalReplayer<Petrinet, String, Marking, Transition, String, PartialAlignment<String, Transition, Marking>, IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition>> replayer = IncrementalReplayer.Factory
					.construct(initialMarking, finalMarking, store, modelSemantics, parameters, labelsInPN,
							IncrementalReplayer.Strategy.REVERT_BASED);
			processXLog(log, net, initialMarking, replayer);
		}
	}

	private boolean isFeasible(String caseId, List<Move<String, Transition>> moves, List<String> trace, Petrinet net,
			Marking iMarking) {
		boolean res = true;
		EfficientPetrinetSemantics semantics = new EfficientPetrinetSemanticsImpl(net);
		semantics.setState(semantics.convert(iMarking));
		int i = 0;
		for (Move<String, Transition> move : moves) {
			if (move.getTransition() != null) {
				res &= semantics.isEnabled(move.getTransition());
				if (!res) {
					System.out.println("Violation for case " + caseId + ", " + "move " + move.toString() + ", at: "
							+ semantics.getStateAsMarking().toString());
				}
				semantics.directExecuteExecutableTransition(move.getTransition());
			}
			if (move.getEventLabel() != null) {
				//				res &= move.getEventLabel().equals(trace.get(i).toString() + "+complete");
				res &= move.getEventLabel().equals(trace.get(i).toString());
				if (!res) {
					System.out.println("Violation for case " + caseId + " on label part. original: " + trace.toString()
							+ ", moves: " + moves.toString());
				}
				i++;
			}
			if (!res)
				break;
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	private static <A extends PartialAlignment<String, Transition, Marking>> IncrementalReplayResult<String, String, Transition, Marking, A> processXLog(
			XLog log, Petrinet net, Marking iMarking,
			IncrementalReplayer<Petrinet, String, Marking, Transition, String, A, ? extends IncrementalReplayerParametersImpl<Petrinet, String, Transition>> replayer){
		XEventClassifier eventClassifier= new XEventNameClassifier();
		//XEventClasses classes = XEventClasses.deriveEventClasses(/*new XEventNameClassifier()*/eventClassifier, log);
		//final TObjectDoubleMap<List<String>> costPerTrace = new TObjectDoubleHashMap<>();
		/*System.out.println(DateTimeComparator.getDateOnlyInstance().compare(startDateCurrentWindow, endDateOfTheLastWindow));
        if(DateTimeComparator.getDateOnlyInstance().compare(startDateCurrentWindow, endDateOfTheLastWindow)<0) {
        	System.out.println("the date has changed");
        }*/
		//final TObjectIntMap<List<String>> count = new TObjectIntHashMap<>();
		IncrementalReplayResult<String, String, Transition, Marking, A> pluginResult = IncrementalReplayResult.Factory
				.construct(IncrementalReplayResult.Impl.HASH_MAP);
		
		//-------------------------------LOG/STREAM INFO-----------------------------------		
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		int totalCases = summary.getNumberOfTraces();
		int totalEvents = summary.getNumberOfEvents();
		System.out.println("The total no. of cases and events are: " + totalCases + " ," + totalEvents );
		//--------------------------------------------------------------
		//int maxCasesToStore = 100; // Integer.MAX_VALUE;	
		int maxCasesInMemoryCurrentWindow = 0;
		int eventsObservedCurrentWindow = 0;
		int eventsObservedTotal = 0;
		String caseId;
	    String event;
	    Date eventTimeStamp;
	    Boolean tideStarted = false;
        Boolean tidePassed = null;
        int noOfCasesDeleted = 0;
        double completeLogCost = 0.0;
		//ArrayList<Pair<Pair<String,String>, Date>> eventLogSortedByDate1 = sortEventLogByDate2(log);
        ArrayList<Triplet<String,String,Date>>	eventLogSortedByDate = TimeStampsBasedLogToStreamConverter.sortEventLogByDate2(log);
		//LinkedHashMap<Pair<String,String>, Date> eventLogSortedByDate = sortEventLogByDate(log);	//Date-sorted Stream	
        int logSize = eventLogSortedByDate.size();
        Date startDateCurrentWindow = getWindowTimeStamp(eventLogSortedByDate, "start");
        //System.out.println(startDateCurrentWindow);
        Date endDateOfTheLastWindow = getWindowTimeStamp(eventLogSortedByDate, "last");
        //System.out.println(endDateOfTheLastWindow);
        
        //Global statistics
        Queue<String> caseIdHistory = new LinkedList<>();        
        Set<String> beforeTideCases = new HashSet<String>();
        Set<String> duringTideCases = new HashSet<String>();
        Set<String> afterTideCases = new HashSet<String>();          
        Set<String> nonConformantCasesCummulative = new  HashSet<String>();        
        List<Double> costPerTraceCummulative = new ArrayList<Double>();
        
        //Window-specific statistics
        Set<String> casesObservedCurrentWindow = new  HashSet<String>();
        Set<String> nonConformantCasesCurrentWindow = new  HashSet<String>();		
		final TObjectDoubleMap<String> costPerTraceCurrentWindow = new TObjectDoubleHashMap<>();
		//TObjectDoubleMap<String> costPerTraceWindowCummulative = new TObjectDoubleHashMap<>();
		HashMap<String, ArrayList<PartialAlignment<String, Transition, Marking>>> partialAlignmentRecord = 
				new HashMap<String , ArrayList<PartialAlignment<String, Transition, Marking>>>();
		
		Map<String, ArrayList<PartialAlignment>> alignmentsLife = new HashMap<String, ArrayList<PartialAlignment>>();
        Map<String, ArrayList<Double>> alignmentsLifeScore = new HashMap<String, ArrayList<Double>>();
        
        System.out.println("CW No. of max. Traces, CW No. of all cases observed, CW No. of conformant traces, CW No. of non-conformant traces, CW No. of pre-tide traces, CW No. of in-tide traces,"
        		+ " CW No. of post-tide traces, CW No. of observed events, CW No. of conformant events, CW No. of non-conformant events");
        		
        //for (Map.Entry<Pair<String,String>, Date> entry : eventLogSortedByDate.entrySet()) {
        for (Triplet<String,String, Date> entry : eventLogSortedByDate) {
			/*caseId = entry.getKey().getFirst();
			event = entry.getKey().getSecond();
			eventTimeStamp = entry.getValue();*/
        	caseId = entry.getValue0();
			event = entry.getValue1();
			eventTimeStamp = entry.getValue2();			
			
			
			//----------------Check if the window is changing. If yes, then report the statistics of the current window and purge the current window-specific data structures
			if(DateTimeComparator.getDateOnlyInstance().compare(startDateCurrentWindow, eventTimeStamp)<0) {
				
				System.out.println(startDateCurrentWindow + ", "						
						+ maxCasesInMemoryCurrentWindow + ", "               //maximum cases in memory in a window
						+ casesObservedCurrentWindow.size() + ", "                  //No. of cases observed current window
						+ (casesObservedCurrentWindow.size() - nonConformantCasesCurrentWindow.size()) + ", "         //No. of conformant cases in a window
						+ nonConformantCasesCurrentWindow.size() + ", "             ////no. of NON-conformant cases in a window
						+ calculateDiffWindowRelatedCases(beforeTideCases, casesObservedCurrentWindow) + ", "        // No. of pre-surge cases in current window
						+ calculateDiffWindowRelatedCases(duringTideCases, casesObservedCurrentWindow) + ", "        // No. of in-surge cases in current window
						+ calculateDiffWindowRelatedCases(afterTideCases, casesObservedCurrentWindow) + ", "         // No. of after-surge cases in current window
						+ eventsObservedCurrentWindow + ", "                                                 //No. of observed events in current window
						+ (eventsObservedCurrentWindow-(int)calculateCurrentCosts(costPerTraceCurrentWindow)) + ", "   //No. of conformant observed events in current window
						+ calculateCurrentCosts(costPerTraceCurrentWindow) /*+ ", "         //No. of NON-conformant observed events in current window
						+ calculateCurrentCosts(costPerTraceWindowCummulative)*/);		//Cumulative costs of non-conformant events in current window				
				
				startDateCurrentWindow= new Date(eventTimeStamp.getTime());
				eventsObservedCurrentWindow = 0;
				completeLogCost += calculateCurrentCosts(costPerTraceCurrentWindow);
				nonConformantCasesCurrentWindow.clear();					
				costPerTraceCurrentWindow.clear(); //??
				//costPerTraceWindowCummulative.clear();
				maxCasesInMemoryCurrentWindow = 0;
				casesObservedCurrentWindow.clear();
			}
						
			if(caseIdHistory.size() > maxCasesInMemoryCurrentWindow) {
				maxCasesInMemoryCurrentWindow = caseIdHistory.size();
			}
			
			//----------Constrain the no. of traces to be retained in memory to maxCasesToStore-------------
			if(replayer.getDataStore().containsKey(caseId)) {
				caseIdHistory.remove(caseId);														
			}else if (replayer.getDataStore().size() >= /*maxCasesToStore*/ NullConfiguration.maxCasesToStore) {
				tideStarted = true;                                         //reporting tide
				noOfCasesDeleted++;
				String toRemove = caseIdHistory.poll();
				if(NullConfiguration.displayFineStats) {
					System.out.println("Tide started at: " + caseId + ", " + event );
					System.out.println("And the case removed was: " + toRemove + " ------ " + replayer.getDataStore().get(toRemove));
				}
				
				PartialAlignment partialAlignment_ = replayer.getDataStore().get(toRemove);
				if(partialAlignmentRecord.containsKey(toRemove)) {
					partialAlignmentRecord.get(toRemove).add(partialAlignment_);
				}else {				
					partialAlignmentRecord.put(toRemove, new ArrayList<PartialAlignment<String, Transition, Marking>>());
					partialAlignmentRecord.get(toRemove).add(partialAlignment_);
				}
				
				replayer.getDataStore().remove(toRemove);
				costPerTraceCummulative.add(costPerTraceCurrentWindow.get(toRemove));
				//costPerTraceCurrentWindow.remove(toRemove);            ////////////??????????????
				//nonConformantCasesCurrentWindow.remove(toRemove);      ///////////??????????????
			}
			
			//-----classifying traces as pre/in/post surge
			/*if(tideHasStarted==true && tidePassed == null && replayer.getDataStore().size()<maxCasesToStore) {
			tidePassed=true;
			}*/
			
			/*if(!tideStarted) {
				beforeTideCases.add(caseId);
			}else if(tideStarted && tidePassed==null ) {
				if(!beforeTideCases.contains(caseId)) {
					duringTideCases.add(caseId);
				}				
			}else if (tidePassed!=null) {
				if(!beforeTideCases.contains(caseId) && !duringTideCases.contains(caseId)) {
				afterTideCases.add(caseId);
				}
			}*/	
			/*if(!tideStarted) {
				beforeTideCases.add(caseId);
			}else if(tideStarted && tidePassed==null Integer.parseInt(caseId)<232 ) {
				if(!beforeTideCases.contains(caseId)) {
					duringTideCases.add(caseId);
				}				
			}else if (tidePassed!=nulltideStarted && tidePassed==null Integer.parseInt(caseId)>=232) {
				if(!beforeTideCases.contains(caseId) && !duringTideCases.contains(caseId)) {
				afterTideCases.add(caseId);
				}
			}*/	
			//--------------------------------------------------------------------------------------------------
			/*if(!replayer.getParameters().getModelElementsToLabelMap().containsValue(event) && NullConfiguration.allowedDuplicateLabelApproximation){
				Boolean NullConfigurationisExperimentValue = NullConfiguration.isExperiment;
				NullConfiguration.isExperiment = false;
				
				HashMap<String, Double> approxSimilarLabels = labelsApproximation(event, replayer.getParameters().getModelElementsToLabelMap().values());
				HashMap<String, Double> approxSimilarLabelsCosts = new HashMap<String, Double>();
				List<String> traceTillNow = new ArrayList<String>();
				if(replayer.getDataStore().containsKey(caseId)) {
					traceTillNow.addAll(replayer.getDataStore().get(caseId).projectOnLabels()) ;
				}
				//traceTillNow.addAll(replayer.getDataStore().get(caseId).projectOnLabels()) ;
				
				for(String key : approxSimilarLabels.keySet()) {
					ArrayList<String> tempTrace = new ArrayList<String>();
					tempTrace.addAll(traceTillNow);
					tempTrace.add(key);
					for (int i = 0; i < tempTrace.size(); i++) {
						//last.setTraceCost(OCC2.processXLog(caseId, tempTrace.get(i)));
						PartialAlignment<String, Transition, Marking> partialAlignment = replayer.processEvent(key, tempTrace.get(i));					
						approxSimilarLabelsCosts.put(key, partialAlignment.getCost());
						if(NullConfiguration.displayFineStats) {
							System.out.println(key + ", " + event + ", " + partialAlignment);
						}
					}
					replayer.getDataStore().remove(key);  //purge the history of the spare replayer				
				}
				if(NullConfiguration.displayFineStats) {
					System.out.println("Event Data Store:  " + replayer.getDataStore());
				}
				
				event = Collections.min(approxSimilarLabelsCosts.entrySet(), Map.Entry.comparingByValue()).getKey();
						
				NullConfiguration.isExperiment = NullConfigurationisExperimentValue;  //restore the value of the isExperiment
				
			}*/
			//---------------------Prefix Alignment of the current observed event--------------------------------
			PartialAlignment<String, Transition, Marking> partialAlignment = replayer.processEvent(caseId, event);
			//->System.out.println(caseId + ", " + partialAlignment);
			if(alignmentsLife.containsKey(caseId)) {
				alignmentsLife.get(caseId).add(partialAlignment);
				alignmentsLifeScore.get(caseId).add(partialAlignment.getCost());
			}else {
				ArrayList<PartialAlignment> tempRecord = new ArrayList<PartialAlignment>();
				tempRecord.add(partialAlignment);
				alignmentsLife.put(caseId, tempRecord);
				ArrayList<Double> tempScore = new ArrayList<Double>();
				tempScore.add(partialAlignment.getCost());
				alignmentsLifeScore.put(caseId, tempScore);
			}
			//System.out.println(partialAlignment);
			//System.out.println(partialAlignment.size());
			//System.out.println(partialAlignment.get((partialAlignment.size()-1)));
			//System.out.println(partialAlignment.get((partialAlignment.size()-1)).getCost());
			if(partialAlignment.get((partialAlignment.size()-1)).getCost() > 0.0) {
				nonConformantCasesCummulative.add(caseId);
				nonConformantCasesCurrentWindow.add(caseId);				
			}
			
			costPerTraceCurrentWindow.put(caseId, (costPerTraceCurrentWindow.get(caseId) + partialAlignment.get((partialAlignment.size()-1)).getCost()));
			/*costPerTraceWindowCummulative.put(caseId, partialAlignment.getCost());*/
			if(NullConfiguration.displayFineStats) {
				System.out.println(caseId + ", " + event + ", " + partialAlignment);
				System.out.println(costPerTraceCurrentWindow);
			}
			
			casesObservedCurrentWindow.add(caseId);
			eventsObservedCurrentWindow++;
			//->System.out.println(eventsObservedCurrentWindow);
			eventsObservedTotal++;
			caseIdHistory.add(caseId);
			
			if(eventTimeStamp.compareTo(endDateOfTheLastWindow)==0 && eventsObservedTotal== logSize) {
				//this is the last event of the stream, report the final statistics of this last window
				System.out.println(startDateCurrentWindow + ", "						
						+ maxCasesInMemoryCurrentWindow + ", "               //maximum cases in memory in a window
						+ casesObservedCurrentWindow.size() + ", "                  //No. of cases observed current window
						+ (casesObservedCurrentWindow.size() - nonConformantCasesCurrentWindow.size()) + ", "         //No. of conformant cases in a window
						+ nonConformantCasesCurrentWindow.size() + ", "             ////no. of NON-conformant cases in a window
						+ calculateDiffWindowRelatedCases(beforeTideCases, casesObservedCurrentWindow) + ", "        // No. of pre-surge cases in current window
						+ calculateDiffWindowRelatedCases(duringTideCases, casesObservedCurrentWindow) + ", "        // No. of in-surge cases in current window
						+ calculateDiffWindowRelatedCases(afterTideCases, casesObservedCurrentWindow) + ", "         // No. of after-surge cases in current window
						+ eventsObservedCurrentWindow + ", "                                                 //No. of observed events in current window
						+ (eventsObservedCurrentWindow-(int)calculateCurrentCosts(costPerTraceCurrentWindow)) + ", "   //No. of conformant observed events in current window
						+ calculateCurrentCosts(costPerTraceCurrentWindow) /*+ ", "         //No. of NON-conformant observed events in current window
						+ calculateCurrentCosts(costPerTraceWindowCummulative)*/);		//Cumulative costs of non-conformant events in current window								
			//System.out.println("The no. of deletions happened are: " + noOfCasesDeleted);		
				completeLogCost += calculateCurrentCosts(costPerTraceCurrentWindow);
				System.out.println("The costs for the whole log is: " + completeLogCost);
				double costMMs = 0.0;
				double costLMs = 0.0;
				/*for(String casedID: replayer.getDataStore().keySet()) {
					if(replayer.getDataStore().get(casedID).getCost()>0.0) {
						
						for(int j=0; j<replayer.getDataStore().get(casedID).size(); j++) {
							if(replayer.getDataStore().get(casedID).get(j).getType().toString().equals("MOVE_LABEL")) {
								costLMs += replayer.getDataStore().get(casedID).get(j).getCost();
							}else if (replayer.getDataStore().get(casedID).get(j).getType().toString().equals("MOVE_MODEL")) {
								costMMs += replayer.getDataStore().get(casedID).get(j).getCost();
							}
							
						}
						if(tracecostt>0.0) {
							System.out.println(casedID + ", " + replayer.getDataStore().get(casedID));
						}
						
						//System.out.println(casedID + "," + replayer.getDataStore().get(casedID));
					}
					
				}*/
				//System.out.println("Total MMs: " + costMMs + ", Total LMs: " + costLMs);
				
				//CODE FOR DISPLAYING THE DATASTORE ON CASE OF UNLIMITED CASE MGMT.
				/*double totalCost = 0.0;
				for(String casedID: replayer.getDataStore().keySet()) {
					totalCost += replayer.getDataStore().get(casedID).getCost();
					if(replayer.getDataStore().get(casedID).getCost()>0.0) {
						System.out.println( casedID + "," + replayer.getDataStore().get(casedID));
					}
					//System.out.println( casedID + "," + replayer.getDataStore().get(casedID).getCost());
				}
				System.out.println("Total Cost: " + totalCost);*/
				
				//CODE FOR DISPLAYING THE WHOLE PARTIAL ALIGNMENT HISTORY OF EACH CASE WHEN CONSTRAINED CASE MANAGEMENT
				for(Entry<String, A> record : replayer.getDataStore().entrySet()) {
					String caseID = record.getKey();
					PartialAlignment partialAlignment_ = record.getValue();
					if(partialAlignmentRecord.containsKey(caseID)) {
						partialAlignmentRecord.get(caseID).add(partialAlignment_);
					}else {				
						partialAlignmentRecord.put(caseID, new ArrayList<PartialAlignment<String, Transition, Marking>>());
						partialAlignmentRecord.get(caseID).add(partialAlignment_);
					}
				}
				System.out.println("----------------------------------------------------the final stats.-------");
				for(Entry<String, ArrayList<PartialAlignment<String, Transition, Marking>>> entry_ : partialAlignmentRecord.entrySet()) {
					System.out.print(entry_.getKey() + "\t");
					double cost = 0.0;
					for(PartialAlignment<String, Transition, Marking> partialAlignments : entry_.getValue()) {
						System.out.print(partialAlignments);
						cost += partialAlignments.getCost();
					}
					System.out.print("\t" + entry_.getValue().get(entry_.getValue().size()-1).getCost());
					System.out.println("\t" + cost );
				}
					
			}		
			
		}
        System.out.println("********************************************************************");
		double sum = 0.0;	
		for(Entry<String, ArrayList<PartialAlignment>> entry : alignmentsLife.entrySet()) {
			System.out.println(entry.getKey() + "\t" + entry.getValue() + "\t" + alignmentsLifeScore.get(entry.getKey()) + "\t" + sumArrayList(alignmentsLifeScore.get(entry.getKey())));
			//System.out.println(alignmentsScore.get(entry.getKey()));
			sum += sumArrayList(alignmentsLifeScore.get(entry.getKey()));
		}
		System.out.println("And the grand sum is " + sum);
		
		for(Entry<String, ArrayList<Double>> entry : alignmentsLifeScore.entrySet()) {
			System.out.println();
			System.out.print(entry.getKey() + ",");
			for(Double value: entry.getValue()) {
				System.out.print(value + ",");
			}
		}
        
		System.out.println("And the grand sum is " + sum);
		
		return pluginResult;
	}
	
	public static double sumArrayList(ArrayList<Double> arrayList) {
		Double sum = 0.0;
		for(int i = 0; i < arrayList.size(); i++)
	    {
	        sum += arrayList.get(i);
	    }
	    return sum;		
	}
	private static int calculateDiffWindowRelatedCases(Set<String> parentCasesSet, Set<String> childCasesSet){
		int score = 0;
		for (String item: childCasesSet) {
			if(parentCasesSet.contains(item)) {
				score++;
			}
        }
		return score;
	}
	/*private static ArrayList<Triplet<String,String,Date>> sortEventLogByDate2(XLog log){
		int index = 0;
		Map<Integer, Triplet<String,String,Date>> eventsStream = new HashMap<Integer, Triplet<String,String,Date>>();
		for (XTrace t : log) {
			for(XEvent e: t) {
				String caseId = XConceptExtension.instance().extractName(t);
				String newEventName = XConceptExtension.instance().extractName(e);
				//Pair<String,String> eventPacket = new Pair<String, String>(caseId, newEventName);
				Date date = XTimeExtension.instance().extractTimestamp(e);
				Triplet<String,String,Date> eventPacket = new Triplet<String,String,Date>(caseId, newEventName, date);				
				eventsStream.put(index, eventPacket);
				index++;
			}
		}
		//need to sort the hashmap on date
		Comparator<Entry<Integer, Triplet<String,String,Date>>> valueComparator = new Comparator<Entry<Integer, Triplet<String,String,Date>>>() { 
			@Override public int compare(Entry<Integer, Triplet<String,String,Date>> e1, Entry<Integer, Triplet<String,String,Date>> e2) { 
				Date v1 = e1.getValue().getValue2(); 
				Date v2 = e2.getValue().getValue2(); 
				return v1.compareTo(v2);
				}
			};
		ArrayList<Entry<Integer, Triplet<String,String,Date>>> entries = new ArrayList<Entry<Integer, Triplet<String,String,Date>>>();
		entries.addAll(eventsStream.entrySet());	
		List<Entry<Integer, Triplet<String,String,Date>>> listOfEntries = new ArrayList<Entry<Integer, Triplet<String,String,Date>>>(entries);
		Collections.sort(listOfEntries, valueComparator);
	    ArrayList<Triplet<String,String,Date>> sortedByValue = new ArrayList<Triplet<String,String,Date>>(listOfEntries.size());
	    //System.out.println(sortedByValue.size());
	    for(Entry<Integer, Triplet<String,String,Date>> entry : listOfEntries){
	    	sortedByValue.add(entry.getValue());
	    	}
	    if(NullConfiguration.displayFineStats) {
	    	printTripletList(sortedByValue);
	    }	    
		return sortedByValue;
	}*/
	
	/*private static LinkedHashMap<Pair<String,String>, Date> sortEventLogByDate(XLog log){
		Map<Pair<String,String>, Date> eventsStream = new HashMap<Pair<String,String>, Date>();
		for (XTrace t : log) {
			for(XEvent e: t) {
				String caseId = XConceptExtension.instance().extractName(t);
				String newEventName = XConceptExtension.instance().extractName(e);
				Pair<String,String> eventPacket = new Pair<String, String>(caseId, newEventName);
				Date date = XTimeExtension.instance().extractTimestamp(e);
				eventsStream.put(eventPacket, date);
			}
		}
	//need to sort the hashmap on date
	Comparator<Entry<Pair<String,String>, Date>> valueComparator = new Comparator<Entry<Pair<String,String>, Date>>() { 
		@Override public int compare(Entry<Pair<String,String>, Date> e1, Entry<Pair<String,String>, Date> e2) { 
			Date v1 = e1.getValue(); 
			Date v2 = e2.getValue(); 
			return v1.compareTo(v2);
			}
		};
	Set<Entry<Pair<String,String>, Date>> entries = eventsStream.entrySet();	
	List<Entry<Pair<String,String>, Date>> listOfEntries = new ArrayList<Entry<Pair<String,String>, Date>>(entries);
	Collections.sort(listOfEntries, valueComparator);
    LinkedHashMap<Pair<String,String>, Date> sortedByValue = new LinkedHashMap<Pair<String,String>,Date>(listOfEntries.size());
    //System.out.println(sortedByValue.size());
    for(Entry<Pair<String,String>, Date> entry : listOfEntries){
    	sortedByValue.put(entry.getKey(), entry.getValue());
    	}
    printLinkedHashMap(sortedByValue);
	return sortedByValue;
	}*/
	private static Date getWindowTimeStamp(ArrayList<Triplet<String,String,Date>> sortedByValue, String choice) {
		//LinkedList<Pair<String,String>> listKeys = new LinkedList<Pair<String,String>>(sortedByValue.keySet());
		if(choice.equals("start")) {
			//return sortedByValue.get( listKeys.getFirst());
			return sortedByValue.get(0).getValue2();
		}else {
			//return sortedByValue.get( listKeys.getLast());
			return sortedByValue.get(sortedByValue.size()-1).getValue2();
		}		
	}
	/*private static void printTripletList(ArrayList<Triplet<String,String,Date>> tripletList ) {
		for(Triplet entry: tripletList) {
			 System.out.println(entry.getValue0() + ", " +  entry.getValue1() + ", " +  entry.getValue2());
			 //System.out.println(entry.getValue(0) + ", " +  entry.getValue(1) + ", " +  entry.getValue(2));
		}
	}
	private static void printLinkedHashMap(LinkedHashMap<Pair<String,String>,Date> sortedByValue  ){
		for (Map.Entry<Pair<String,String>,Date> entry : sortedByValue.entrySet()) {		    
		   System.out.println(entry.getKey() + ", " +  entry.getValue());
		}
	}*/
	public int alternateFactor(int factor) {	
		if(factor<24) {
			factor += 6;
		}else {
			factor=6;
		}
		return factor;
	}

	public static <A extends PartialAlignment<String, Transition, Marking>> A processEventUsingReplayer(String caseId,
			String event,
			IncrementalReplayer<Petrinet, String, Marking, Transition, String, A, ? extends IncrementalReplayerParametersImpl<Petrinet, String, Transition>> replayer) {
		return replayer.processEvent(caseId, event);
	}

	private List<String> toStringList(XTrace trace, XEventClasses classes) {
		List<String> l = new ArrayList<>(trace.size());
		for (int i = 0; i < trace.size(); i++) {
			l.add(i, classes.getByIdentity(XConceptExtension.instance().extractName(trace.get(i))).toString());
		}
		return l;
	}

	private static void setupLabelMap(final Petrinet net, Map<Transition, String> modelElementsToLabelMap, Map<String, Collection<Transition>> labelsToModelElementsMap) {
		if(NullConfiguration.isMappingAutomatic) {
			for (Transition t : net.getTransitions()) {
				if (!t.isInvisible()) {
					String label = t.getLabel();
					modelElementsToLabelMap.put(t, label);
					if (!labelsToModelElementsMap.containsKey(label)) {
						labelsToModelElementsMap.put(label, Collections.singleton(t));
					} else {
						labelsToModelElementsMap.get(label).add(t);
					}
				}
			}
		}else if (!NullConfiguration.isMappingAutomatic && NullConfiguration.eventlog.equals("BPI12AplusO")) {
			for (Transition t : net.getTransitions()) {
				switch(t.getLabel()) {
					case "A_SUBMITTED":
						modelElementsToLabelMap.put(t, "A_SUBMITTED");
						break;
					/*case "A_Sub":
						modelElementsToLabelMap.put(t, "A_Sub");
						break;*/					
					case "A_PARTLYSUBMITTED":
						modelElementsToLabelMap.put(t, "A_PARTLYSUBMITTED");
						break;
					/*case "A_Par_Sub":
						modelElementsToLabelMap.put(t, "A_Par_Sub");
						break;*/
					case "A_PREACCEPTED":
						modelElementsToLabelMap.put(t, "A_PREACCEPTED");
						break;
					/*case "A_Pre_Accp":
						modelElementsToLabelMap.put(t, "A_Pre_Accp");
						break;*/
					case "A_ACCEPTED":
						modelElementsToLabelMap.put(t, "A_ACCEPTED");
						break;
					/*case "A_Accp":
						modelElementsToLabelMap.put(t, "A_Accp");
						break;	*/				
					case "A_DECLINED_1":
					case "A_DECLINED_2":
					case "A_DECLINED_3":
					case "A_DECLINED_4":
						modelElementsToLabelMap.put(t, "A_DECLINED");
						break;
					/*case "A_Dec_1":
					case "A_Dec_2":
					case "A_Dec_3":
					case "A_Dec_4":
						modelElementsToLabelMap.put(t, "A_Dec");
						break;*/
					case "A_FINALIZED":
						modelElementsToLabelMap.put(t, "A_FINALIZED");
						break;
					/*case "A_Fin":
						modelElementsToLabelMap.put(t, "A_Fin");
						break;*/
					case "A_APPROVED":
						modelElementsToLabelMap.put(t, "A_APPROVED");
						break;
					/*case "A_Appr":
						modelElementsToLabelMap.put(t, "A_Appr");
						break;*/
					case "A_REGISTERED":
						modelElementsToLabelMap.put(t, "A_REGISTERED");
						break;
					/*case "A_Reg":
						modelElementsToLabelMap.put(t, "A_Reg");
						break;*/
					case "A_ACTIVATED":
						modelElementsToLabelMap.put(t, "A_ACTIVATED");
						break;
					/*case "A_Actv":
						modelElementsToLabelMap.put(t, "A_Actv");
						break;*/
					case "A_CANCELLED_1":
					case "A_CANCELLED_2":
					case "A_CANCELLED_3":
						modelElementsToLabelMap.put(t, "A_CANCELLED");
						break;	
					/*case "A_Canc_1":
					case "A_Canc_2":
					case "A_Canc_3":
						modelElementsToLabelMap.put(t, "A_Canc");
						break;	*/
						
					case "O_SELECTED_1":
					case "O_SELECTED_2":
						modelElementsToLabelMap.put(t, "O_SELECTED");
						break;
					/*case "O_Sel_1":
					case "O_Sel_2":
						modelElementsToLabelMap.put(t, "O_Sel");
						break;*/
					case "O_CREATED":
						modelElementsToLabelMap.put(t, "O_CREATED");
						break;
					/*case "O_Cre":
						modelElementsToLabelMap.put(t, "O_Cre");
						break;*/
					case "O_SENT":
						modelElementsToLabelMap.put(t, "O_SENT");
						break;
					/*case "O_Sent":
						modelElementsToLabelMap.put(t, "O_Sent");
						break;*/
					/*case "O_SENT_BACK_INCOMPLETE":
						modelElementsToLabelMap.put(t, "O_SENT_BACK_INCOMPLETE");
						break;*/
					/*case "O_S_BACK_INCM":
						modelElementsToLabelMap.put(t, "O_S_BACK_INCM");
						break;*/
					case "O_CANCELLED_1":
					case "O_CANCELLED_2":
						modelElementsToLabelMap.put(t, "O_CANCELLED");
						break;
					/*case "O_Canc_1":
					case "O_Canc_2":
						modelElementsToLabelMap.put(t, "O_Canc");
						break;*/
					case "O_SENT_BACK_1":
					case "O_SENT_BACK_2":
						modelElementsToLabelMap.put(t, "O_SENT_BACK");
						break;
					/*case "O_S_BACK":
						modelElementsToLabelMap.put(t, "O_S_BACK");
						break;*/
					case "O_ACCEPTED":
						modelElementsToLabelMap.put(t, "O_ACCEPTED");
						break;
					/*case "O_Accp":
						modelElementsToLabelMap.put(t, "O_Accp");
						break;*/
					case "O_DECLINED":
						modelElementsToLabelMap.put(t, "O_DECLINED");
						break;
					/*case "O_Dec":
						modelElementsToLabelMap.put(t, "O_Dec");
						break;*/
					default:
						if(t.getLabel().contains("tau")) {
							modelElementsToLabelMap.put(t, "tau");
						}
							
				}
			}
			Collection<Transition> A_DECLINED = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("A_DECLINED_1") || t.getLabel().equals("A_DECLINED_2") || t.getLabel().equals("A_DECLINED_3") || t.getLabel().equals("A_DECLINED_4")) {
					A_DECLINED.add(t);
				}
			}
			labelsToModelElementsMap.put("A_DECLINED", A_DECLINED);
			
			Collection<Transition> O_SENT_BACK = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_SENT_BACK_1") || t.getLabel().equals("O_SENT_BACK_2")) {
					O_SENT_BACK.add(t);
				}
			}
			labelsToModelElementsMap.put("O_SENT_BACK", O_SENT_BACK);
			
			/*Collection<Transition> A_Dec = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("A_Dec_1") || t.getLabel().equals("A_Dec_2") || t.getLabel().equals("A_Dec_3") || t.getLabel().equals("A_Dec_4")) {
					A_Dec.add(t);
				}
			}
			labelsToModelElementsMap.put("A_Dec", A_Dec);*/
			
			Collection<Transition> A_CANCELLED = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("A_CANCELLED_1") || t.getLabel().equals("A_CANCELLED_2") || t.getLabel().equals("A_CANCELLED_3")) {
					A_CANCELLED.add(t);
				}
			}
			labelsToModelElementsMap.put("A_CANCELLED", A_CANCELLED);
			
			/*Collection<Transition> A_Canc = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("A_Canc_1") || t.getLabel().equals("A_Canc_2") || t.getLabel().equals("A_Canc_3")) {
					A_Canc.add(t);
				}
			}
			labelsToModelElementsMap.put("A_Canc", A_Canc);*/
			
			
			Collection<Transition> O_CANCELLED = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_CANCELLED_1") || t.getLabel().equals("O_CANCELLED_2")) {
					O_CANCELLED.add(t);
				}
			}
			labelsToModelElementsMap.put("O_CANCELLED", O_CANCELLED);
			
			/*Collection<Transition> O_Canc = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_Canc_1") || t.getLabel().equals("O_Canc_2")) {
					O_Canc.add(t);
				}
			}
			labelsToModelElementsMap.put("O_Canc", O_Canc);
			*/
			Collection<Transition> O_SELECTED = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_SELECTED_1") || t.getLabel().equals("O_SELECTED_2")) {
					O_SELECTED.add(t);
				}
			}
			labelsToModelElementsMap.put("O_SELECTED", O_SELECTED);
			
			/*Collection<Transition> O_Sel = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_Sel_1") || t.getLabel().equals("O_Sel_2")) {
					O_Sel.add(t);
				}
			}
			labelsToModelElementsMap.put("O_Sel", O_Sel);*/
			
			Collection<Transition> Taus = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().contains("tau")) {
					Taus.add(t);
				}
			}
			labelsToModelElementsMap.put("tau", Taus);
			
			for (Transition t : net.getTransitions()) {
				if(t.getLabel().equals("O_CANCELLED_1") || t.getLabel().equals("O_CANCELLED_2") || 
						t.getLabel().equals("O_SENT_BACK_1") || t.getLabel().equals("O_SENT_BACK_2") ||
						t.getLabel().equals("O_SELECTED_1") || t.getLabel().equals("O_SELECTED_2")||
						t.getLabel().equals("A_DECLINED_1") || t.getLabel().equals("A_DECLINED_2") || t.getLabel().equals("A_DECLINED_3") || t.getLabel().equals("A_DECLINED_4")||
						t.getLabel().equals("A_CANCELLED_1") ||t.getLabel().equals("A_CANCELLED_2") || t.getLabel().equals("A_CANCELLED_3") || t.getLabel().contains("tau")) {
					;
				}else {
					labelsToModelElementsMap.put(t.getLabel(), Collections.singleton(t));
				}
			}
			
			/*for (Transition t : net.getTransitions()) {
				if(t.getLabel().equals("O_Canc_1") || t.getLabel().equals("O_Canc_2") ||
						t.getLabel().equals("O_Sel_1") || t.getLabel().equals("O_Sel_2")||
						t.getLabel().equals("A_Dec_1") || t.getLabel().equals("A_Dec_2") || t.getLabel().equals("A_Dec_3") || t.getLabel().equals("A_Dec_4")||
						t.getLabel().equals("A_Canc_1") ||t.getLabel().equals("A_Canc_2") || t.getLabel().equals("A_Canc_3") || t.getLabel().contains("tau")) {
					;
				}else {
					labelsToModelElementsMap.put(t.getLabel(), Collections.singleton(t));
				}
			}*/
		}else if (!NullConfiguration.isMappingAutomatic && NullConfiguration.eventlog.equals("RoadTraffic")) {
			for (Transition t : net.getTransitions()) {
				if(!t.isInvisible()) {
					switch(t.getLabel()) {
						case "Create Fine":
							modelElementsToLabelMap.put(t, "Create Fine");
							break;
							
						case "Send Fine":
							modelElementsToLabelMap.put(t, "Send Fine");
							break;
							
						case "Add penalty":
							modelElementsToLabelMap.put(t, "Add penalty");
							break;
							
						case "Insert Fine Notification":
							modelElementsToLabelMap.put(t, "Insert Fine Notification");
							break;
											
						case "Payment1":
						case "Payment2":
						case "Payment3":
							modelElementsToLabelMap.put(t, "Payment");
							break;
						
						case "Send for Credit Collection":
							modelElementsToLabelMap.put(t, "Send for Credit Collection");
							break;
							
						case "Insert Date Appeal to Prefecture":
							modelElementsToLabelMap.put(t, "Insert Date Appeal to Prefecture");
							break;
							
						case "Send Appeal to Prefecture":
							modelElementsToLabelMap.put(t, "Send Appeal to Prefecture");
							break;
							
						case "Receive Result Appeal from Prefecture":
							modelElementsToLabelMap.put(t, "Receive Result Appeal from Prefecture");
							break;					
					
						case "Notify Result Appeal to Offender":
							modelElementsToLabelMap.put(t, "Notify Result Appeal to Offender");
							break;
							
						case "Appeal to Judge":
							modelElementsToLabelMap.put(t, "Appeal to Judge");
							break;
							
						default:
							System.out.println("Some problem");
							System.out.println(t.getLabel());
					}
				}
				/*switch(t.getLabel()) {
					case "Create Fine":
						modelElementsToLabelMap.put(t, "Create Fine");
						break;
						
					case "Send Fine":
						modelElementsToLabelMap.put(t, "Send Fine");
						break;
						
					case "Add Penalty":
						modelElementsToLabelMap.put(t, "Add Penalty");
						break;
						
					case "Insert Fine Notification":
						modelElementsToLabelMap.put(t, "Insert Fine Notification");
						break;
										
					case "Payment1":
					case "Payment2":
					case "Payment3":
						modelElementsToLabelMap.put(t, "Payment");
						break;
					
					case "Send for Credit Collection":
						modelElementsToLabelMap.put(t, "Send for Credit Collection");
						break;
						
					case "Insert Date Appeal to Prefecture":
						modelElementsToLabelMap.put(t, "Insert Date Appeal to Prefecture");
						break;
						
					case "Send Appeal to Prefecture":
						modelElementsToLabelMap.put(t, "Send Appeal to Prefecture");
						break;
						
					case "Receive Result Appeal from Prefecture":
						modelElementsToLabelMap.put(t, "Receive Result Appeal from Prefecture");
						break;					
				
					case "Notify Result Appeal to Offender":
						modelElementsToLabelMap.put(t, "Notify Result Appeal to Offender");
						break;
						
					case "Appeal to Judge":
						modelElementsToLabelMap.put(t, "Appeal to Judge");
						break;
						
					default:
						if(t.getLabel().contains("tau")) {
							modelElementsToLabelMap.put(t, "tau");
						}

				}*/
			}
			
			Collection<Transition> Payment = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("Payment1") || t.getLabel().equals("Payment2") || t.getLabel().equals("Payment3")) {
					Payment.add(t);
				}
			}
			labelsToModelElementsMap.put("Payment", Payment);
			
			for (Transition t : net.getTransitions()) {
				if(t.getLabel().equals("Payment1") || t.getLabel().equals("Payment2") || t.getLabel().equals("Payment3")) {
					;
				}else {
					labelsToModelElementsMap.put(t.getLabel(), Collections.singleton(t));
				}
			}
			
		}
		/*else if (!NullConfiguration.isMappingAutomatic) {
			for (Transition t : net.getTransitions()) {
				switch(t.getLabel()) {
					case "A_SUBMITTED":
						modelElementsToLabelMap.put(t, "A_SUBMITTED");
						break;
					case "A_Sub":
						modelElementsToLabelMap.put(t, "A_Sub");
						break;					
					case "A_PARTLYSUBMITTED":
						modelElementsToLabelMap.put(t, "A_PARTLYSUBMITTED");
						break;
					case "A_Par_Sub":
						modelElementsToLabelMap.put(t, "A_Par_Sub");
						break;
					case "A_PREACCEPTED":
						modelElementsToLabelMap.put(t, "A_PREACCEPTED");
						break;
					case "A_Pre_Accp":
						modelElementsToLabelMap.put(t, "A_Pre_Accp");
						break;
					case "A_ACCEPTED":
						modelElementsToLabelMap.put(t, "A_ACCEPTED");
						break;
					case "A_Accp":
						modelElementsToLabelMap.put(t, "A_Accp");
						break;					
					case "A_DECLINED_1":
					case "A_DECLINED_2":
					case "A_DECLINED_3":
					case "A_DECLINED_4":
						modelElementsToLabelMap.put(t, "A_DECLINED");
						break;
					case "A_Dec_1":
					case "A_Dec_2":
					case "A_Dec_3":
					case "A_Dec_4":
						modelElementsToLabelMap.put(t, "A_Dec");
						break;
					case "A_FINALIZED":
						modelElementsToLabelMap.put(t, "A_FINALIZED");
						break;
					case "A_Fin":
						modelElementsToLabelMap.put(t, "A_Fin");
						break;
					case "A_APPROVED":
						modelElementsToLabelMap.put(t, "A_APPROVED");
						break;
					case "A_Appr":
						modelElementsToLabelMap.put(t, "A_Appr");
						break;
					case "A_REGISTERED":
						modelElementsToLabelMap.put(t, "A_REGISTERED");
						break;
					case "A_Reg":
						modelElementsToLabelMap.put(t, "A_Reg");
						break;
					case "A_ACTIVATED":
						modelElementsToLabelMap.put(t, "A_ACTIVATED");
						break;
					case "A_Actv":
						modelElementsToLabelMap.put(t, "A_Actv");
						break;
					case "A_CANCELLED_1":
					case "A_CANCELLED_2":
					case "A_CANCELLED_3":
						modelElementsToLabelMap.put(t, "A_CANCELLED");
						break;	
					case "A_Canc_1":
					case "A_Canc_2":
					case "A_Canc_3":
						modelElementsToLabelMap.put(t, "A_Canc");
						break;	
						
					case "O_SELECTED_1":
					case "O_SELECTED_2":
						modelElementsToLabelMap.put(t, "O_SELECTED");
						break;
					case "O_Sel_1":
					case "O_Sel_2":
						modelElementsToLabelMap.put(t, "O_Sel");
						break;
					case "O_CREATED":
						modelElementsToLabelMap.put(t, "O_CREATED");
						break;
					case "O_Cre":
						modelElementsToLabelMap.put(t, "O_Cre");
						break;
					case "O_SENT":
						modelElementsToLabelMap.put(t, "O_SENT");
						break;
					case "O_Sent":
						modelElementsToLabelMap.put(t, "O_Sent");
						break;
					case "O_SENT_BACK_INCOMPLETE":
						modelElementsToLabelMap.put(t, "O_SENT_BACK_INCOMPLETE");
						break;
					case "O_S_BACK_INCM":
						modelElementsToLabelMap.put(t, "O_S_BACK_INCM");
						break;
					case "O_CANCELLED_1":
					case "O_CANCELLED_2":
						modelElementsToLabelMap.put(t, "O_CANCELLED");
						break;
					case "O_Canc_1":
					case "O_Canc_2":
						modelElementsToLabelMap.put(t, "O_Canc");
						break;
					case "O_SENT_BACK":
						modelElementsToLabelMap.put(t, "O_SENT_BACK");
						break;
					case "O_S_Back":
						modelElementsToLabelMap.put(t, "O_S_Back");
						break;
					case "O_ACCEPTED":
						modelElementsToLabelMap.put(t, "O_ACCEPTED");
						break;
					case "O_Accp":
						modelElementsToLabelMap.put(t, "O_Accp");
						break;
					case "O_DECLINED":
						modelElementsToLabelMap.put(t, "O_DECLINED");
						break;
					case "O_Dec":
						modelElementsToLabelMap.put(t, "O_Dec");
						break;
					default:
						if(t.getLabel().contains("tau")) {
							modelElementsToLabelMap.put(t, "tau");
						}
							
				}
			}
			Collection<Transition> A_DECLINED = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("A_DECLINED_1") || t.getLabel().equals("A_DECLINED_2") || t.getLabel().equals("A_DECLINED_3") || t.getLabel().equals("A_DECLINED_4")) {
					A_DECLINED.add(t);
				}
			}
			labelsToModelElementsMap.put("A_DECLINED", A_DECLINED);
			
			Collection<Transition> A_Dec = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("A_Dec_1") || t.getLabel().equals("A_Dec_2") || t.getLabel().equals("A_Dec_3") || t.getLabel().equals("A_Dec_4")) {
					A_Dec.add(t);
				}
			}
			labelsToModelElementsMap.put("A_Dec", A_Dec);
			
			Collection<Transition> A_CANCELLED = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("A_CANCELLED_1") || t.getLabel().equals("A_CANCELLED_2") || t.getLabel().equals("A_CANCELLED_3")) {
					A_CANCELLED.add(t);
				}
			}
			labelsToModelElementsMap.put("A_CANCELLED", A_CANCELLED);
			
			Collection<Transition> A_Canc = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("A_Canc_1") || t.getLabel().equals("A_Canc_2") || t.getLabel().equals("A_Canc_3")) {
					A_Canc.add(t);
				}
			}
			labelsToModelElementsMap.put("A_Canc", A_Canc);
			
			
			Collection<Transition> O_CANCELLED = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_CANCELLED_1") || t.getLabel().equals("O_CANCELLED_2")) {
					O_CANCELLED.add(t);
				}
			}
			labelsToModelElementsMap.put("O_CANCELLED", O_CANCELLED);
			
			Collection<Transition> O_Canc = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_Canc_1") || t.getLabel().equals("O_Canc_2")) {
					O_Canc.add(t);
				}
			}
			labelsToModelElementsMap.put("O_Canc", O_Canc);
			
			Collection<Transition> O_SELECTED = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_SELECTED_1") || t.getLabel().equals("O_SELECTED_2")) {
					O_SELECTED.add(t);
				}
			}
			labelsToModelElementsMap.put("O_SELECTED", O_SELECTED);
			
			Collection<Transition> O_Sel = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_Sel_1") || t.getLabel().equals("O_Sel_2")) {
					O_Sel.add(t);
				}
			}
			labelsToModelElementsMap.put("O_Sel", O_Sel);
			
			Collection<Transition> Taus = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().contains("tau")) {
					Taus.add(t);
				}
			}
			labelsToModelElementsMap.put("tau", Taus);
			
			for (Transition t : net.getTransitions()) {
				if(t.getLabel().equals("O_CANCELLED_1") || t.getLabel().equals("O_CANCELLED_2") || 
						t.getLabel().equals("O_SELECTED_1") || t.getLabel().equals("O_SELECTED_2")||
						t.getLabel().equals("A_DECLINED_1") || t.getLabel().equals("A_DECLINED_2") || t.getLabel().equals("A_DECLINED_3") || t.getLabel().equals("A_DECLINED_4")||
						t.getLabel().equals("A_CANCELLED_1") ||t.getLabel().equals("A_CANCELLED_2") || t.getLabel().equals("A_CANCELLED_3") || t.getLabel().contains("tau")) {
					;
				}else {
					labelsToModelElementsMap.put(t.getLabel(), Collections.singleton(t));
				}
			}
			
			for (Transition t : net.getTransitions()) {
				if(t.getLabel().equals("O_Canc_1") || t.getLabel().equals("O_Canc_2") || 
						t.getLabel().equals("O_Sel_1") || t.getLabel().equals("O_Sel_2")||
						t.getLabel().equals("A_Dec_1") || t.getLabel().equals("A_Dec_2") || t.getLabel().equals("A_Dec_3") || t.getLabel().equals("A_Dec_4")||
						t.getLabel().equals("A_Can_1") ||t.getLabel().equals("A_Can_2") || t.getLabel().equals("A_Can_3") || t.getLabel().contains("tau")) {
					;
				}else {
					labelsToModelElementsMap.put(t.getLabel(), Collections.singleton(t));
				}
			}
		}*/				
	}

	//TODO: needs a parameter object
	private static void setupModelMoveCosts(final Petrinet net, TObjectDoubleMap<Transition> modelMoveCosts, TObjectDoubleMap<String> labelMoveCosts) {
		for (Transition t : net.getTransitions()) {
			if (t.isInvisible()) {
				modelMoveCosts.put(t, (short) 0);
			} else {
				modelMoveCosts.put(t, (short) 1);
				labelMoveCosts.put(t.getLabel(), (short) 1);
			}
		}
	}
	
	private double calculateMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		//>>double memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
		//>>System.out.println(memoryUsed + ", ");
		return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
	}
	private static double calculateCurrentCosts(TObjectDoubleMap<String> costPerTrace) {
		double totalCost = 0;
		for (String t : costPerTrace.keySet()) {
			//totalCost += count.get(t) * costPerTrace.get(t);
			totalCost += costPerTrace.get(t);
		}
		return totalCost;
	}
	private double calculateCummulativeCosts(List<Double> costPerTraceCummulative) {
		double totalCost = 0;
		for (Double t : costPerTraceCummulative) {
			//totalCost += count.get(t) * costPerTrace.get(t);
			totalCost += t;
		}
		return totalCost;
	}
	
	public static Petrinet constructNet(String netFile) {
		PNMLSerializer PNML = new PNMLSerializer();
		NetSystem sys = PNML.parse(netFile);

		//System.err.println(sys.getMarkedPlaces());

		//		int pi, ti;
		//		pi = ti = 1;
		//		for (org.jbpt.petri.Place p : sys.getPlaces())
		//			p.setName("p" + pi++);
		//		for (org.jbpt.petri.Transition t : sys.getTransitions())
		//				t.setName("t" + ti++);

		Petrinet net = PetrinetFactory.newPetrinet(netFile);

		// places
		Map<org.jbpt.petri.Place, Place> p2p = new HashMap<org.jbpt.petri.Place, Place>();
		for (org.jbpt.petri.Place p : sys.getPlaces()) {
			Place pp = net.addPlace(p.toString());
			p2p.put(p, pp);
		}

		// transitions
		Map<org.jbpt.petri.Transition, Transition> t2t = new HashMap<org.jbpt.petri.Transition, Transition>();
		for (org.jbpt.petri.Transition t : sys.getTransitions()) {
			Transition tt = net.addTransition(t.getLabel()); 
			if (t.isSilent() || t.getLabel().startsWith("tau") || t.getLabel().equals("t2") || t.getLabel().equals("t8")
					|| t.getLabel().equals("complete")) {
				tt.setInvisible(true);
			}
			t2t.put(t, tt);
		}

		// flow
		for (Flow f : sys.getFlow()) {
			if (f.getSource() instanceof org.jbpt.petri.Place) {
				net.addArc(p2p.get(f.getSource()), t2t.get(f.getTarget()));
			} else {
				net.addArc(t2t.get(f.getSource()), p2p.get(f.getTarget()));
			}
		}

		// add unique start node
		if (sys.getSourceNodes().isEmpty()) {
			Place i = net.addPlace("START_P");
			Transition t = net.addTransition("");
			t.setInvisible(true);
			net.addArc(i, t);

			for (org.jbpt.petri.Place p : sys.getMarkedPlaces()) {
				net.addArc(t, p2p.get(p));
			}

		}

		return net;
	}

	public static Marking getFinalMarking(PetrinetGraph net) {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		return finalMarking;
	}

	public static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}
	private static HashMap<String, Double> labelsApproximation(String event, Collection<String> labelsMap) {		

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
}



