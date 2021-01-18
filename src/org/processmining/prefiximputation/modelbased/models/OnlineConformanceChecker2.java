package org.processmining.prefiximputation.modelbased.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.onlineconformance.algorithms.IncrementalReplayer;
import org.processmining.onlineconformance.models.ModelSemanticsPetrinet;
import org.processmining.onlineconformance.models.PartialAlignment;
import org.processmining.onlineconformance.parameters.IncrementalRevBasedReplayerParametersImpl;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class OnlineConformanceChecker2 {
	//public static XFactory xesFactory = new XFactoryBufferedImpl();
	//public static XExtensionManager xesExtensionManager = XExtensionManager.instance();
	private final Petrinet net;
	private final Marking initialMarking;
	private final Marking finalMarking;
	private LocalModelStructure lms;
	private Double conformanceValue;
	private XEventClasses classes;
	//public XTrace trace;
	/*private Map<Transition, String> modelElementsToLabelMap = new HashMap<>();
	private Map<String, Collection<Transition>> labelsToModelElementsMap = new HashMap<>();*/
	private Map<Transition, String> modelElementsToLabelMap;
	private Map<String, Collection<Transition>> labelsToModelElementsMap;
	//private TObjectDoubleMap<Transition> modelMoveCosts = new TObjectDoubleHashMap<>();
	//private TObjectDoubleMap<String> labelMoveCosts = new TObjectDoubleHashMap<>();
	private TObjectDoubleMap<Transition> modelMoveCosts = new TObjectDoubleHashMap<>();
	private TObjectDoubleMap<String> labelMoveCosts = new TObjectDoubleHashMap<>();
	private final IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition> parameters;
	private IncrementalReplayer<Petrinet, String, Marking, Transition, String, PartialAlignment<String, Transition, Marking>, IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition>> replayer;
	//private IncrementalReplayResult<String, String, Transition, Marking, PartialAlignment<String, Transition, Marking>> pluginResult;
	
	/*public OnlineConformanceChecker2() {		
	}*/
	public OnlineConformanceChecker2(LocalModelStructure lms, Boolean statusND) {
		this.lms = lms;
		this.net= this.lms.net;
		this.initialMarking = this.lms.initialMarking;
		this.finalMarking = this.lms.finalMarking;
		this.classes = lms.eventClasses;
		//this.modelElementsToLabelMap = lms.modelElementsToLabelMap;
		//this.labelsToModelElementsMap = lms.labelsToModelElementsMap;
		parameters = new IncrementalRevBasedReplayerParametersImpl<>();
		initialiseComponents(statusND);
	}
	/*public OnlineConformanceChecker2(Petrinet net, Marking initialMarking, Marking finalMarking) {
		this.net = net;
		this.initialMarking = initialMarking;
		this.finalMarking = finalMarking;		
	}*/
	
	public void initialiseComponents(Boolean statusND) {
		if(statusND) {
			;//use cutomised Model and Label move costs w.r.t. ND philosophy
		}else {
			parameters.setLabelMoveCosts(lms.labelMoveCosts);
			parameters.setModelMoveCosts(lms.modelMoveCosts);
		}
		//setupLabelMap(net);
		//setupModelMoveCosts(net);
		//parameters = new IncrementalRevBasedReplayerParametersImpl<>();
		parameters.setUseMultiThreading(false);
		//parameters.setLabelMoveCosts(labelMoveCosts);
		parameters.setLabelToModelElementsMap(lms.labelsToModelElementsMap);
		//parameters.setModelMoveCosts(modelMoveCosts);
		parameters.setModelElementsToLabelMap(lms.modelElementsToLabelMap);
		parameters.setSearchAlgorithm(IncrementalReplayer.SearchAlgorithm.A_STAR);
		parameters.setUseSolutionUpperBound(true);
		parameters.setLookBackWindow(2);
		parameters.setExperiment(false);
		applyGeneric();
	}
	
	/*public Double doReplay(XTrace trace) throws Exception {
		setupLabelMap(net);
		setupModelMoveCosts(net);
		IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition> parameters = new IncrementalRevBasedReplayerParametersImpl<>();
		parameters.setUseMultiThreading(false);
		parameters.setLabelMoveCosts(labelMoveCosts);
		parameters.setLabelToModelElementsMap(labelsToModelElementsMap);
		parameters.setModelMoveCosts(modelMoveCosts);
		parameters.setModelElementsToLabelMap(modelElementsToLabelMap);
		parameters.setSearchAlgorithm(IncrementalReplayer.SearchAlgorithm.A_STAR);
		parameters.setUseSolutionUpperBound(true);
		parameters.setLookBackWindow(2);
		parameters.setExperiment(false);
		if (parameters.isExperiment()) {
			;
		} else {
			//Double traceCost =  applyGeneric(net, initialMarking, finalMarking, parameters, trace);
			applyGeneric(net, initialMarking, finalMarking, parameters, trace);			
		}
		return this.conformanceValue;
	}*/
	@SuppressWarnings("unchecked")
	public void /*<A extends PartialAlignment<String, Transition, Marking>> IncrementalReplayResult<String, String, Transition, Marking, A>*/ applyGeneric(
			/*final Petrinet net, final Marking initialMarking, final Marking finalMarking,
			final IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition> parameters, final XTrace trace*/) {
		ModelSemanticsPetrinet<Marking> modelSemantics = ModelSemanticsPetrinet.Factory.construct(net);
		Map<Transition, String> labelsInPN = new HashMap<Transition, String>();
		for (Transition t : net.getTransitions()) {
			if (!t.isInvisible()) {
				labelsInPN.put(t, t.getLabel());
			}
		}
		//if (parameters.isExperiment()) {
			/*Map<String, MeasurementAwarePartialAlignment<String, Transition, Marking>> store = new HashMap<>();
			IncrementalReplayer<Petrinet, String, Marking, Transition, String, MeasurementAwarePartialAlignment<String, Transition, Marking>, IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition>> replayer = IncrementalReplayer.Factory
					.construct(initialMarking, finalMarking, store, modelSemantics, parameters, labelsInPN,
							IncrementalReplayer.Strategy.REVERT_BASED);
			return (IncrementalReplayResult<String, String, Transition, Marking, A>) processXLog(log, net,
					initialMarking, replayer);*/
			;
		//} else {
			Map<String, PartialAlignment<String, Transition, Marking>> store = new HashMap<>();
			/*IncrementalReplayer<Petrinet, String, Marking, Transition, String, PartialAlignment<String, Transition, Marking>, IncrementalRevBasedReplayerParametersImpl<Petrinet, String, Transition>> replayer = IncrementalReplayer.Factory
					.construct(initialMarking, finalMarking, store, modelSemantics, parameters, labelsInPN,
							IncrementalReplayer.Strategy.REVERT_BASED);*/
			
			replayer = IncrementalReplayer.Factory
					.construct(initialMarking, finalMarking, store, modelSemantics, parameters, labelsInPN,
							IncrementalReplayer.Strategy.REVERT_BASED);
			//return (IncrementalReplayResult<String, String, Transition, Marking, A>) processXLog(/*net, initialMarking,*/ replayer, trace);
		//}
	}
	@SuppressWarnings("unchecked")
	public /*<A extends PartialAlignment<String, Transition, Marking>> IncrementalReplayResult<String, String, Transition, Marking, A>*/ Double processXLog(String caseId, String e
			/*Petrinet net, Marking iMarking,
			IncrementalReplayer<Petrinet, String, Marking, Transition, String, A, ? extends IncrementalReplayerParametersImpl<Petrinet, String, Transition>> replayer, XTrace trace*/) {
		//XEventClasses classes = lms.eventClasses;
		
		//XEventClasses classes = XEventClasses.deriveEventClasses(final XEventClassifier classifier;, trace.);
		//final TObjectDoubleMap<List<String>> costPerTrace = new TObjectDoubleHashMap<>();
		//final TObjectIntMap<List<String>> count = new TObjectIntHashMap<>();
		/*IncrementalReplayResult<String, String, Transition, Marking, A> pluginResult = IncrementalReplayResult.Factory
				.construct(IncrementalReplayResult.Impl.HASH_MAP);*/
		/*pluginResult = IncrementalReplayResult.Factory
				.construct(IncrementalReplayResult.Impl.HASH_MAP);*/
		
		//List<String> traceStrLst = toStringList(trace, classes);
		//String traceStr = StringUtils.join(traceStrLst, ",");
		//String caseId = XConceptExtension.instance().extractName(trace);
		//pluginResult.put(traceStr, new ArrayList<A>());
		//PartialAlignment<String, Transition, Marking> partialAlignment = null;
		PartialAlignment<String, Transition, Marking> partialAlignment = replayer.processEvent(caseId, e.toString());
		this.conformanceValue = partialAlignment.getCost();
		return this.conformanceValue;
		/*for (String e : traceStrLst) {
			partialAlignment = replayer.processEvent(caseId, e.toString());
			pluginResult.get(traceStr).add((A) partialAlignment);
		}
		System.out.println("For Trace size: " + trace.size() + " the trace cost is: " + partialAlignment.getCost());
		this.conformanceValue = partialAlignment.getCost();
		return pluginResult;*/
	}
	
	/*public void reset() {
		//reset the necessary field to flush the previous values, if any...
	}*/
		
		/*for (XTrace t : log) {
			List<String> traceStrLst = toStringList(t, classes);
			String traceStr = StringUtils.join(traceStrLst, ",");
			String caseId = XConceptExtension.instance().extractName(t);
			if (!costPerTrace.containsKey(traceStrLst)) {
				pluginResult.put(traceStr, new ArrayList<A>());
				PartialAlignment<String, Transition, Marking> partialAlignment = null;
				for (String e : traceStrLst) {
					partialAlignment = replayer.processEvent(caseId, e.toString());
					pluginResult.get(traceStr).add((A) partialAlignment);
				}
				if (partialAlignment != null) {
					assert (isFeasible(caseId, partialAlignment, traceStrLst, net, iMarking));
				} else {
					assert (false);
				}
				costPerTrace.put(traceStrLst, partialAlignment.getCost());
				System.out.println("For Trace: " + t.size() + " the trace cost is: " + partialAlignment.getCost() );
				count.put(traceStrLst, 1);
			} else {
				count.adjustOrPutValue(traceStrLst, 1, 1);
			}
		}*/
		/*int totalCost = 0;
		for (List<String> t : costPerTrace.keySet()) {
			totalCost += count.get(t) * costPerTrace.get(t);
		}
		System.out.println("total costs: " + totalCost);
		return pluginResult;
	}*/
	/*private void setupLabelMap(final Petrinet net) {
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
	}*/
	/*private void setupModelMoveCosts(final Petrinet net) {
		for (Transition t : net.getTransitions()) {
			if (t.isInvisible()) {
				modelMoveCosts.put(t, (short) 0);
			} else {
				modelMoveCosts.put(t, (short) 1);
				labelMoveCosts.put(t.getLabel(), (short) 1);
			}
		}
	}*/
	private List<String> toStringList(XTrace trace, XEventClasses classes) {
		List<String> l = new ArrayList<>(trace.size());
		for (int i = 0; i < trace.size(); i++) {
			l.add(i, classes.getByIdentity(XConceptExtension.instance().extractName(trace.get(i))).toString());
		}
		return l;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*public void doReplayy(XTrace trace) throws Exception {
		LpSolve.lpSolveVersion();
		
		//calculate the alignment for the XTrace with the (petri)net of the lms;
		//pm4py.algo.conformance.alignments.versions.state_equation_a_star import apply as state_equation_a_star_apply
		//Lets assume that the activity name in log and the (corresponding) transition label in the model are the same.
				
		
		//XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		XEventClasses classes=lms.eventClasses;
				
		Map<Transition, Integer> costModelMove = new HashMap<>();
		Map<Transition, Integer> costSyncMove = new HashMap<>();
		Map<XEventClass, Integer> costLogMove = new HashMap<>();
		for (Transition t : net.getTransitions()) {
			costSyncMove.put(t, 0);
			costModelMove.put(t, t.isInvisible() ? 0 : 1);       //removed 2
		}
		for (XEventClass c : classes.getClasses()) {
			costLogMove.put(c, 1);								//removed 5
		}
		TransEvClassMapping mapping = lms.mapping;
		int i = net.getTransitions().size();
		int i=0;
		for (Transition t: net.getTransitions()) {
			mapping.put(t, classes.getByIndex(i));           //????classes.getBy
			mapping.put(t, classes.getClassOf(event));
			i++;
		}
		int nThreads = 1;
		int costUpperBound = Integer.MAX_VALUE;
		int timeoutMilliseconds = 10 * 1000;
		int maximumNumberOfStates = Integer.MAX_VALUE;
		ReplayerParameters parameters;
		parameters = new ReplayerParameters.Dijkstra(false, false, nThreads, Debug.DOT, timeoutMilliseconds,
				maximumNumberOfStates, costUpperBound, false);
		Replayer replayer = new Replayer(parameters, net, initialMarking, finalMarking, classes, costModelMove,
				costLogMove, costSyncMove, mapping, false);
		long preProcessTimeNanoseconds = 0;

		int success = 0;
		int failed = 0;
		ExecutorService service = Executors.newFixedThreadPool(parameters.nThreads);
		Future<TraceReplayTask>[] futures = new Future[1];
		TraceReplayTask task = new TraceReplayTask(replayer, parameters, trace, i, timeoutMilliseconds,
				parameters.maximumNumberOfStates, preProcessTimeNanoseconds);
		futures[0] = service.submit(task);
		service.shutdown();
		TraceReplayTask result;
		try {
			result = futures[0].get();
		} catch (Exception e) {
			// execution os the service has terminated.
			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
		}
		switch (result.getResult()) {
			case DUPLICATE :
				assert false; // cannot happen in this setting
				throw new RuntimeException("Result cannot be a duplicate in per-trace computations.");
			case FAILED :
				// internal error in the construction of synchronous product or other error.
				throw new RuntimeException("Error in alignment computations");
			case SUCCESS :
				// process succcesful execution of the replayer
				SyncReplayResult replayResult = result.getSuccesfulResult();
				int exitCode = replayResult.getInfo().get(Replayer.TRACEEXITCODE).intValue();
				if ((exitCode & Utils.OPTIMALALIGNMENT) == Utils.OPTIMALALIGNMENT) {
					// Optimal alignment found.
					success++;

					System.out.println(String.format("Time (ms): %f",
							result.getSuccesfulResult().getInfo().get(PNRepResult.TIME)));
					//			System.out.println(result.getSuccesfulResult().getStepTypes());

					int logMove = 0, syncMove = 0, modelMove = 0, tauMove = 0;
					for (StepTypes step : result.getSuccesfulResult().getStepTypes()) {
						if (step == StepTypes.L) {
							logMove++;
						} else if (step == StepTypes.LMGOOD) {
							syncMove++;
						} else if (step == StepTypes.MREAL) {
							modelMove++;
						} else if (step == StepTypes.MINVI) {
							tauMove++;
						}
					}
					System.out.println(String.format("Log %d, Model %d, Sync %d, tau %d", logMove, modelMove,
							syncMove, tauMove));

				} else if ((exitCode & Utils.FAILEDALIGNMENT) == Utils.FAILEDALIGNMENT) {
					// failure in the alignment. Error code shows more details.
					failed++;
				}
				if ((exitCode & Utils.ENABLINGBLOCKEDBYOUTPUT) == Utils.ENABLINGBLOCKEDBYOUTPUT) {
					// in some marking, there were too many tokens in a place, blocking the addition of more tokens. Current upper limit is 128
				}
				if ((exitCode & Utils.COSTFUNCTIONOVERFLOW) == Utils.COSTFUNCTIONOVERFLOW) {
					// in some marking, the cost function went through the upper limit of 2^24
				}
				if ((exitCode & Utils.HEURISTICFUNCTIONOVERFLOW) == Utils.HEURISTICFUNCTIONOVERFLOW) {
					// in some marking, the heuristic function went through the upper limit of 2^24
				}
				if ((exitCode & Utils.TIMEOUTREACHED) == Utils.TIMEOUTREACHED
						|| (exitCode & Utils.TIMEOUTREACHED) == Utils.TIMEOUTREACHED) {
					// alignment failed with a timeout (caused in the solver if SOLVERTIMEOUTREACHED is set)
				}
				if ((exitCode & Utils.STATELIMITREACHED) == Utils.STATELIMITREACHED) {
					// alignment failed due to reacing too many states.
				}
				if ((exitCode & Utils.COSTLIMITREACHED) == Utils.COSTLIMITREACHED) {
					// no optimal alignment found with cost less or equal to the given limit.
				}
				if ((exitCode & Utils.CANCELLED) == Utils.CANCELLED) {
					// user-cancelled.
				}
				if ((exitCode & Utils.FINALMARKINGUNREACHABLE) == Utils.FINALMARKINGUNREACHABLE) {
					// user-cancelled.
					System.err.println("final marking unreachable.");
				}

				break;
		}
		
	}
	
	public XEvent getXEvent(String eventName) {
		XAttributeMap atts = xesFactory.createAttributeMap();
		atts.put("concept:name",
			xesFactory.createAttributeLiteral(
					"concept:name",
					eventName,
					xesExtensionManager.getByName("Concept")));
		return xesFactory.createEvent(atts);
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
	}*/
	
	/*private static Marking getFinalMarking(PetrinetGraph net) {
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
	}*/
	
	

}
