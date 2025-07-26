package org.processmining.prefiximputation.modelbased.completeforgetting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.onlineconformance.models.PartialAlignment;

public class ForgettingCases {
	protected LocalModelStructure lms;
	protected LocalConformanceTracker lct;
	/*public CasesRemoval(LocalModelStructure lms, Set<Map.Entry<String, LocalConformanceStatus>> EventStore) {
		
	}*/
	public ForgettingCases(LocalModelStructure lms, LocalConformanceTracker lct) {
		this.lms = lms;
		this.lct = lct;
	}
	
	//THIS IS THE METHOD ONLY IF WE DON"T DISTINGUISH BETWEEN IMPUTED AND ORIGINAL TRACES
			//
			//IDEAL CANDIDATE
			//IF there are cases with the single start event and not in non-deterministic region then this is an IDEAL CANDIDATE 
			//
			//IF there are cases which have imputation history (such that imputed prefix+orphan event) 
			//	if there are traces which are not in non-deterministic region then they have priority. Trace with least imputed
			//      prefix is the IDEAL CANDIDATE
			//  Else the trace in non-deterministic region (with least imputed prefix) is the IDEAL CANDIDATE
			//
			//Then cases with 100 conformance such that the trace length is least and ideally the current marking of the trace
			//  is also reproducible by the imputation i.e, the trace is exactly the shortest possible prefix for the last event
			//  in this trace
			
			//ELSE select the one farthest updated
	
	public String selectCaseToBeForgotten() {
		//lct is HashMap<String, LocalConformanceStatus> where LocalConformanceStatus is individual for each trace
		//now we have to iterate through each entry and from the statistics (imputatioin history, conformance score, etc.)
		//of each trace (accessible through correspondingLocalConformanceStatus object) identify the IDEAL CANDIDATE for removal. 
		
		//REPRODUCIBILITY SCORE
		//Condition1: Check the CC score
		//Condition2: if the trace is the same as shortest path in our list
		//Condition3: if the current marking is in Non-deterministic region?
		//Condition4: if the imputation is true?
		//Condition5: The trace length is the shortest
		LinkedHashMap<String, Integer> casesDeletionPriority = new LinkedHashMap<String, Integer>();
		//String caseIdToBeRemoved=null;
		//Double conformanceScore=Double.MIN_VALUE;
		//Boolean isTraceSameAsShortestPath=false;
		//Boolean isCurrentMarkingNonDeterministic=true;
		//Boolean isTraceImputed = true;
		//Integer imputationSize = 0;		
		String caseIdToBeForgotten=null;
		Integer minTraceLength = Integer.MAX_VALUE;
		//Integer maxTraceLength = Integer.MIN_VALUE;
		//Boolean firstTrace=true;
		
		boolean priority1, priority2, priority3, priority4;
		priority1 = priority2 = priority3 = priority4 = false;
		    
		
		for (Map.Entry<String, LocalConformanceStatus> entry : lct.entrySet()) {
		    String caseId = entry.getKey();
//		    if(caseId.equals(/*"199098""199071""179841""198113"*/"173757") ) {
//				//System.out.println("Found!!!!");
//			}
		    LocalConformanceStatus lcs = entry.getValue();
		    ArrayList<String> trace=lcs.traceModelAlphabet;
		    PartialAlignment partialAlignment = lcs.OCC2.replayer.getDataStore().get(caseId);
		    
		    if(partialAlignment.getState().getStateInModel().equals(lms.finalMarking)){
		    	return caseId;
		    } else if(!priority1 && trace.size()==1 && partialAlignment.getCost() == 0) {		//by-default conformance will be 100% as the single event is a case-starter otherwise would have been imputed
//		    	if(!trace.get(0).equals("S+complete")) {
//		    		System.out.println("WHATTTTTTTT");
//		    	}
		    	priority1 = priority2= priority3 = priority4 = true;
		    	casesDeletionPriority.put(caseId, 1);
		    	//return caseId;
		    }else if (!priority2 && lcs.currentImputationSize == (trace.size()-1) ) {       //by-default conformance will be 100% as we are imputing conformant prefix
		    	if(!lcs.isTraceInNonDeterministicRegion) {
		    		casesDeletionPriority.put(caseId, 2);   //traces with last event in non-ND regions are favored as the orphan event in a ND-region 
		    	}else {										//can provide an escape for all other events in the ND region
		    		casesDeletionPriority.put(caseId, 3);
		    	}
		    	priority2= priority3 = priority4 = true;
		    }else if (!priority3 && lcs.last.getTraceCost()==0.0 && isTraceSameAsShortestPath(/*trace*/lcs.generateTraceFromAlignment(lcs.OCC2.replayer.getDataStore().get(caseId))) && !lcs.isTraceInNonDeterministicRegion) {  //may or may not be imputed
		    	casesDeletionPriority.put(caseId, 4);
		    	priority3 = priority4= true;
		    }else if(!priority4 && lcs.last.getTraceCost()==0.0 && isTraceSameAsShortestPath(/*trace*/lcs.generateTraceFromAlignment(lcs.OCC2.replayer.getDataStore().get(caseId)))){
		    	casesDeletionPriority.put(caseId, 5);
		    	priority4 = true;
		    	
		    }/*else if(another scenario) {
		    	
		    }*/
		    
		   if (trace.size()<minTraceLength) {
		    	caseIdToBeForgotten = caseId;
		    	minTraceLength = trace.size();
		    }
		    
		    /*if(trace.size()>maxTraceLength) {
		    	caseIdToBeRemoved = caseId;
		    	maxTraceLength = trace.size();
		    }*/
		    
		}
		
		 if(!casesDeletionPriority.isEmpty()) {
			 caseIdToBeForgotten = Collections.min(casesDeletionPriority.entrySet(), Map.Entry.comparingByValue()).getKey();
		    }	
			/*System.out.println("SFR:\t" + caseIdToBeRemoved + "\t (" + (casesDeletionPriority.get(caseIdToBeRemoved)==null?null:casesDeletionPriority.get(caseIdToBeRemoved)) + ")\t" + lct.get(caseIdToBeRemoved).traceModelAlphabet +" in marking:\t" 
		    + fetchMarking(caseIdToBeRemoved));*/
		return caseIdToBeForgotten;    
	}
	
	private Marking fetchMarking(String caseId) {
		Marking state = lct.get(caseId).OCC2.replayer.getDataStore().get(caseId).getState().getStateInModel();
				//.getDataStore().get(caseId);
				
		//Marking state = previousAlignment == null ? null : previousAlignment.getState().getStateInModel();
		return state;
	}
	
	public Boolean isTraceSameAsShortestPath(ArrayList<String> localTrace) {		
		//boolean same = true;
		//String lastEventInTrace = trace.get((trace.size()-1));
		//System.out.println(lastEventInTrace);
		//ArrayList<String> relevantShortestPrefix = new ArrayList<String>();
		//relevantShortestPrefix = lms.getShortestPrefix(trace.get((trace.size()-1)));
		
		/*if(trace.size()!=relevantShortestPrefix.size()) {
			System.out.println("Not same");
			return false;
		}*/
		//trace.remove(trace.size() - 1); 
		//ArrayList<String> str1 = lms.getShortestPrefix(localTrace.get((localTrace.size()-1)));
		//ArrayList<String> str2 = new ArrayList<String>();
		//str2.addAll(localTrace);
		//Collections.copy(str2,localTrace);
		//str2.remove(str2.size() - 1);
		/*if(lms.processModelAlphabet.contains(localTrace.get((localTrace.size()-1)))) {
			if(lms.getShortestPrefix(localTrace.get((localTrace.size()-1))).equals(localTrace.subList(0, localTrace.size()-1))) {
				//>>System.out.println("Same");
				return true;
			}else {
				return false;
			}
		}else {
			ArrayList<String> approxSimilarLabels = lms.getEquivalentModelLabels(localTrace.get((localTrace.size()-1)));
			for(String key : approxSimilarLabels) {
				if(lms.getShortestPrefix(key).equals(localTrace.subList(0, localTrace.size()-1))){
					return true;
				}
			}
			return false;
		}*/
		
		/*System.out.println("-----------------------------------------");
		System.out.println("The trace is: " + localTrace);
		System.out.println("The last event in this trace is: " + localTrace.get((localTrace.size()-1)));
		System.out.println("The shortest prefix for this last event is: " + lms.getShortestPrefix(localTrace.get((localTrace.size()-1))));
		System.out.println("The actual prefix of the last event is: " + localTrace.subList(0, localTrace.size()-1));*/
		if(lms.getShortestPrefix(localTrace.get((localTrace.size()-1))).equals(localTrace.subList(0, localTrace.size()-1))) {
			//System.out.println("Same");
			return true;
		}else {
			return false;
		}
		 
		/*if(str1.equals(str2)) {
			System.out.println("Same");
			return true;
		}*/
			
		//>>System.out.println("Not same");		
		//return false;		
	}
	
	
	
	/*public Boolean isTraceSameAsShortestPath(XTrace trace) {		
		boolean same =true;
		String lastEventInTrace = XConceptExtension.instance().extractName(trace.get((trace.size()-1)));
		//System.out.println(lastEventInTrace);
		ArrayList<String> relevantShortestPrefix = new ArrayList<String>();
		relevantShortestPrefix = lms.getShortestPrefix(lastEventInTrace);
		
		if(trace.size()!=relevantShortestPrefix.size()) {
			System.out.println("Not same");
			return false;
		}
		
		for(int i=0; i<trace.size(); i++) {
			String eventName = XConceptExtension.instance().extractName(trace.get(i));
			System.out.println("The event at position: " + i + " is " + eventName);
			if(!eventName.equals(relevantShortestPrefix.get(i))) {
				same = false;
				break;
			}
		}		
		return same;		
	}*/
}
