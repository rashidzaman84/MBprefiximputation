package org.processmining.prefiximputation.modelbased.models;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.processmining.framework.util.Pair;
import org.processmining.streamconformance.local.model.DirectFollowingRelation;

public class OnlineConformanceChecker1 {
	public static XFactory xesFactory = new XFactoryBufferedImpl();
	public static XExtensionManager xesExtensionManager = XExtensionManager.instance();
	
	
	
	//private LocalModelStructure lms;
	
	//private XEventClasses eventClasses;
	//public XTrace trace;
	
	
	protected OnlineConformanceScore last = new OnlineConformanceScore();
	protected int correctObservedDirectFollowingRelations = 0;
	protected int incorrectObservedDirectFollowingRelations = 0;
	protected String lastActivityForCase = null;
	protected LocalModelStructure lms;
	protected Set<DirectFollowingRelation> observedRelations = new HashSet<DirectFollowingRelation>();
	
	public OnlineConformanceChecker1() {		
	}
	public OnlineConformanceChecker1(LocalModelStructure lms) {
		this.lms = lms;		
	}
	/*public OnlineConformanceChecker1(Petrinet net, Marking initialMarking, Marking finalMarking) {
		this.net = net;
		this.initialMarking = initialMarking;
		this.finalMarking = finalMarking;		
	}*/
	
	public void setLastActivityForCase(String newEventName) {
		this.lastActivityForCase = newEventName;
	}
	
	public OnlineConformanceScore doReplay(String newEventName) {
		DirectFollowingRelation relation = new DirectFollowingRelation(lastActivityForCase, newEventName);
		
		// count relations based on whether it is allowed or not
		if (lms.isAllowed(relation)) {
			if (!observedRelations.contains(relation)) {
				correctObservedDirectFollowingRelations++;
				observedRelations.add(relation);
			}
			last.isLastObservedViolation(true);
		} else {
			incorrectObservedDirectFollowingRelations++;
			last.isLastObservedViolation(false);
		}
		
		// compute the conformance
		last.setConformance((double) correctObservedDirectFollowingRelations /
				(correctObservedDirectFollowingRelations + incorrectObservedDirectFollowingRelations));
		
		// compute the completeness
		if (lms.isAllowed(relation)) {
			Pair<Integer, Integer> minMax = lms.getMinMaxRelationsBefore(relation);
			int observed = observedRelations.size();
			if (observed >= minMax.getFirst() &&  observed <= minMax.getSecond()) {
				last.setCompleteness(1d);
			} else {
				double comp = observed / (minMax.getFirst() + 1d);
				if (observed > (minMax.getFirst() + 1d)) {
					comp = observed / (minMax.getSecond() + 1d);
				}
				if (comp > 1) {
					comp = 1;
				}
				last.setCompleteness(comp);
			}
		}
		
		// compute the confidence
		if (lms.isAllowed(relation)) {
			last.setConfidence(1d - (lms.getMinRelationsAfter(relation) / lms.getMaxOfMinRelationsAfter()));
		}
		
		lastActivityForCase = newEventName;
		//refreshUpdateTime();
		return last;
	}
	
	
	
	
		
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

}
