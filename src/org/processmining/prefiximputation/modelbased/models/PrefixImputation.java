package org.processmining.prefiximputation.modelbased.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.streamconformance.local.model.DirectFollowingRelation;

public class PrefixImputation {
	protected LocalModelStructure lms;
	public ArrayList<String> imputedTrace= new ArrayList<String>();
	public Integer imputationSize;
	//public ArrayList<String> traceAfterImputation;
	public boolean imputationRevised=false;
	public String revisedOrphan;
	//public static XFactory xesFactory = new XFactoryBufferedImpl();
	//public static XExtensionManager xesExtensionManager = XExtensionManager.instance();
	public PrefixImputation(LocalModelStructure lms) {
		this.lms = lms;
	}
	
	/*public Pair<XTrace, Integer>  void imputePrefix(String newEventName, XTrace tr, String choice){
		//new Pair<XTrace, Integer>;
		Pair<XTrace, Integer> result;
		
	     switch (choice) {
	         case "Model":{
	             imputePrefixFromModel(newEventName,tr);	             
	             //return result;	            
	         }
			case "EventStore":
	         case "Hybrid":	         
	         default:
	             throw new IllegalArgumentException("Invalid imputation choice: " + choice);
	     }
	     
	}*/
	
	public /*Pair<XTrace, Integer>*/ void imputePrefix(String newEventName/*, XTrace tr*/){  //sets prefix for imputation and its size
		if(!imputedTrace.isEmpty()) { //incase of revisiting imputation we need to first remove the old trace
			imputedTrace.clear();
		}
		ArrayList<String> shortestPrefix = lms.getShortestPrefix(newEventName);
		if(shortestPrefix != null) {
			imputedTrace.addAll(shortestPrefix);
		}else {
			System.out.println("No shortest prefix found for the event: " + newEventName);
		}
		
		//imputedTrace = lms.getShortestPrefix(newEventName);
		imputationSize = imputedTrace.size();
		//>>System.out.println("The imputed prefix for Orphan event: " + newEventName + " is: " + imputedTrace + " with length of: " + imputationSize);
		/*for (int i = 0; i<imputationSize;i++) {
			//System.out.println
            tr.add(i, getXEvent(imputedPrefix.get(i)));		            
        }*/
		//System.out.println("AND the events in the imputed trace are now: ");
		/*for (int i = 0; i<tr.size();i++) {					
			System.out.println(XConceptExtension.instance().extractName(tr.get(i)));					
        }*/
		//traceAfterImputation.addAll(imputedPrefix);
		imputedTrace.add(newEventName);
		//>>System.out.println("AND the events in the imputed trace are now: " + imputedTrace);
		//return new Pair<XTrace, Integer>(tr, imputationSize);
		
	}
	
/*	public XEvent getXEvent(String eventName) {
		XAttributeMap atts = xesFactory.createAttributeMap();
		atts.put("concept:name",
			xesFactory.createAttributeLiteral(
					"concept:name",
					eventName,
					xesExtensionManager.getByName("Concept")));
		return xesFactory.createEvent(atts);
	}*/
	
	public void revisitPrefix(ArrayList<String> traceHistory) {
		//extract non-imputed part
		ArrayList<String> nonImputedPartOfTrace = new ArrayList<String>();
		ArrayList<ArrayList<String>> candidates = new ArrayList<ArrayList<String>>();
		Map<String, ArrayList<DirectFollowingRelation>> Sequences = new HashMap<String, ArrayList<DirectFollowingRelation>>();
		
		//for(int i=this.imputationSize; i<traceHistory.size(); i++ ) {
		nonImputedPartOfTrace.addAll(traceHistory.subList(this.imputationSize, traceHistory.size()));
		//}
		
		
		String orphanEvent = nonImputedPartOfTrace.get(0);
		for(Transition transition : lms.labelsToModelElementsMap.get(orphanEvent)) {
			ArrayList<String> candidate = new ArrayList<String>();
			candidate.add(transition.getLabel());
			candidate.addAll(nonImputedPartOfTrace.subList(1, nonImputedPartOfTrace.size()));		
			candidates.add(candidate);
		}
		int scores[] = new int[candidates.size()];
		for(int l=0; l<candidates.size(); l++) {
			for(int m=0; m<candidates.get(l).size()-1; m++) {
				DirectFollowingRelation relation = new DirectFollowingRelation(candidates.get(l).get(m), candidates.get(l).get(m+1));
				if(lms.isAllowed(relation)) {
					scores[l] = scores[l]+1;
				}
			}
		}
		int maxIndex = 0;
		int maxValue = Integer.MIN_VALUE ;
	    for(int i = 0;i < scores.length;i++){
	     if(scores[i] > maxValue){
	    	 maxValue = scores[i] ;
	    	 maxIndex = i;
	       }
	    }
	    if(candidates.get(maxIndex).get(0).equals(orphanEvent)) {
	    	imputationRevised=false;
	    }else {
	    	/*for (int i = 0; i<imputationSize;i++) {				
				traceHistory.remove(i);		            
	        }*/
	    	traceHistory.clear();
			imputePrefix(candidates.get(maxIndex).get(0)/*, traceHistory*/);  //SHALL we just impute on the basis of maxSizeEvent OR consider the whole pattern of it?
			imputationRevised=true;
			this.revisedOrphan = candidates.get(maxIndex).get(0);
	    }
		//uncover the dominant behavior
		/*ArrayList<DirectFollowingRelation> validRelationsSpace = new ArrayList<DirectFollowingRelation>();
		
		for(int j=0;j<nonImputedPartOfTrace.size();j++) {
			for(int k=j; k<nonImputedPartOfTrace.size();k++) {
				DirectFollowingRelation relation = new DirectFollowingRelation(nonImputedPartOfTrace.get(j), nonImputedPartOfTrace.get(k));
				if(lms.isAllowed(relation)) {					
					validRelationsSpace.add(relation);
					}				
				}
		}
		ArrayList<DirectFollowingRelation> validRelationsSpaceCopy = new ArrayList<DirectFollowingRelation>();		
		validRelationsSpaceCopy.addAll(validRelationsSpace);
		//validRelationsSpaceCopy.remove(0);
		for(int l=0; l<validRelationsSpace.size(); l++) {			
			for(int m=0; m<validRelationsSpaceCopy.size(); m++) {
				ArrayList<String> inFocus = new ArrayList<String>();
				inFocus.add(validRelationsSpace.get(l).getFirst());
				inFocus.add(validRelationsSpace.get(l).getSecond());
				if(inFocus.contains(validRelationsSpaceCopy.get(m).getFirst())) {
					if(!Sequences.containsKey(validRelationsSpace.get(l).getFirst())) {
						ArrayList<DirectFollowingRelation> behavioralPatterns = new ArrayList<DirectFollowingRelation>();
						behavioralPatterns.add(validRelationsSpaceCopy.get(m));
						Sequences.put(validRelationsSpace.get(l).getFirst(), behavioralPatterns);
					}else {
						Sequences.get(validRelationsSpace.get(l).getFirst()).add(validRelationsSpaceCopy.get(m));
					}
					inFocus.add(validRelationsSpaceCopy.get(m).getSecond());
				}
			}
			//remove the relations related to infocus items
			validRelationsSpaceCopy.removeAll(Sequences.get(validRelationsSpace.get(l).getFirst()));
		}
		//now we have the Sequences having all the paths
		int maxsize = 0; 
		String maxSizeEvent = null;
		for(Map.Entry<String, ArrayList<DirectFollowingRelation>> entry : Sequences.entrySet()) {
			if(entry.getValue().size()>maxsize) {
				maxsize=entry.getValue().size();
				maxSizeEvent = entry.getKey();
			}
		}
		if (nonImputedPartOfTrace.get(0).equals(maxSizeEvent)) { //if the maxSizeEvent is the same as orphan event
			imputationRevised=false;
		}else {   //if the maxSizeEvent is NOT the same as orphan event
			for (int i = 0; i<imputationSize;i++) {				
				traceHistory.remove(i);		            
	        }
			imputePrefix(maxSizeEvent, traceHistory);  //SHALL we just impute on the basis of maxSizeEvent OR consider the whole pattern of it?
			imputationRevised=true;
			this.revisedOrphan = maxSizeEvent;
		}	*/
	    
	    
	    
	    
	    
	    
	    
		/*ArrayList<String> nonImputedPartOfTrace = new ArrayList<String>();
		ArrayList<ArrayList<String>> candidates = new ArrayList<ArrayList<String>>();
		Map<String, ArrayList<DirectFollowingRelation>> Sequences = new HashMap<String, ArrayList<DirectFollowingRelation>>();
		
		//for(int i=this.imputationSize; i<traceHistory.size(); i++ ) {
		nonImputedPartOfTrace.addAll(traceHistory.subList(this.imputationSize, traceHistory.size()-1));
		//}
		
		String orphanEvent = nonImputedPartOfTrace.get(0);
		for(Transition transition : lms.labelsToModelElementsMap.get(orphanEvent)) {
			ArrayList<String> candidate = new ArrayList<String>();
			candidate.add(transition.getLabel());
			candidate.addAll(nonImputedPartOfTrace.subList(1, nonImputedPartOfTrace.size()-1));			
		}
		//uncover the dominant behavior
		ArrayList<DirectFollowingRelation> validRelationsSpace = new ArrayList<DirectFollowingRelation>();
		
		for(int j=0;j<nonImputedPartOfTrace.size();j++) {
			for(int k=j; k<nonImputedPartOfTrace.size();k++) {
				DirectFollowingRelation relation = new DirectFollowingRelation(nonImputedPartOfTrace.get(j), nonImputedPartOfTrace.get(k));
				if(lms.isAllowed(relation)) {					
					validRelationsSpace.add(relation);
					}				
				}
		}
		ArrayList<DirectFollowingRelation> validRelationsSpaceCopy = new ArrayList<DirectFollowingRelation>();		
		validRelationsSpaceCopy.addAll(validRelationsSpace);
		//validRelationsSpaceCopy.remove(0);
		for(int l=0; l<validRelationsSpace.size(); l++) {			
			for(int m=0; m<validRelationsSpaceCopy.size(); m++) {
				ArrayList<String> inFocus = new ArrayList<String>();
				inFocus.add(validRelationsSpace.get(l).getFirst());
				inFocus.add(validRelationsSpace.get(l).getSecond());
				if(inFocus.contains(validRelationsSpaceCopy.get(m).getFirst())) {
					if(!Sequences.containsKey(validRelationsSpace.get(l).getFirst())) {
						ArrayList<DirectFollowingRelation> behavioralPatterns = new ArrayList<DirectFollowingRelation>();
						behavioralPatterns.add(validRelationsSpaceCopy.get(m));
						Sequences.put(validRelationsSpace.get(l).getFirst(), behavioralPatterns);
					}else {
						Sequences.get(validRelationsSpace.get(l).getFirst()).add(validRelationsSpaceCopy.get(m));
					}
					inFocus.add(validRelationsSpaceCopy.get(m).getSecond());
				}
			}
			//remove the relations related to infocus items
			validRelationsSpaceCopy.removeAll(Sequences.get(validRelationsSpace.get(l).getFirst()));
		}
		//now we have the Sequences having all the paths
		int maxsize = 0; 
		String maxSizeEvent = null;
		for(Map.Entry<String, ArrayList<DirectFollowingRelation>> entry : Sequences.entrySet()) {
			if(entry.getValue().size()>maxsize) {
				maxsize=entry.getValue().size();
				maxSizeEvent = entry.getKey();
			}
		}
		if (nonImputedPartOfTrace.get(0).equals(maxSizeEvent)) { //if the maxSizeEvent is the same as orphan event
			imputationRevised=false;
		}else {   //if the maxSizeEvent is NOT the same as orphan event
			for (int i = 0; i<imputationSize;i++) {				
				traceHistory.remove(i);		            
	        }
			imputePrefix(maxSizeEvent, traceHistory);  //SHALL we just impute on the basis of maxSizeEvent OR consider the whole pattern of it?
			imputationRevised=true;
			this.revisedOrphan = maxSizeEvent;
		}*/
	}
	
	/*public void revisitPrefix(XTrace traceHistory) {
		//extract non-imputed part
		ArrayList<String> nonImputed = new ArrayList<String>();
		Map<String, ArrayList<DirectFollowingRelation>> Sequences = new HashMap<String, ArrayList<DirectFollowingRelation>>();
		
		for(int i=this.imputationSize; i<traceHistory.size(); i++ ) {
			nonImputed.add(XConceptExtension.instance().extractName(traceHistory.get(i)));
		}				
		//uncover the dominant behavior
		ArrayList<DirectFollowingRelation> validRelationsSpace = new ArrayList<DirectFollowingRelation>();
		
		for(int j=0;j<nonImputed.size();j++) {
			for(int k=j; k<nonImputed.size();k++) {
				DirectFollowingRelation relation = new DirectFollowingRelation(nonImputed.get(j), nonImputed.get(k));
				if(lms.isAllowed(relation)) {					
					validRelationsSpace.add(relation);
					}				
				}
		}
		ArrayList<DirectFollowingRelation> validRelationsSpaceCopy = new ArrayList<DirectFollowingRelation>();		
		validRelationsSpaceCopy.addAll(validRelationsSpace);
		//validRelationsSpaceCopy.remove(0);
		for(int l=0; l<validRelationsSpace.size(); l++) {			
			for(int m=0; m<validRelationsSpaceCopy.size(); m++) {
				ArrayList<String> inFocus = new ArrayList<String>();
				inFocus.add(validRelationsSpace.get(l).getFirst());
				inFocus.add(validRelationsSpace.get(l).getSecond());
				if(inFocus.contains(validRelationsSpaceCopy.get(m).getFirst())) {
					if(!Sequences.containsKey(validRelationsSpace.get(l).getFirst())) {
						ArrayList<DirectFollowingRelation> behavioralPatterns = new ArrayList<DirectFollowingRelation>();
						behavioralPatterns.add(validRelationsSpaceCopy.get(m));
						Sequences.put(validRelationsSpace.get(l).getFirst(), behavioralPatterns);
					}else {
						Sequences.get(validRelationsSpace.get(l).getFirst()).add(validRelationsSpaceCopy.get(m));
					}
					inFocus.add(validRelationsSpaceCopy.get(m).getSecond());
				}
			}
			//remove the relations related to infocus items
			validRelationsSpaceCopy.removeAll(Sequences.get(validRelationsSpace.get(l).getFirst()));
		}
		//now we have the Sequences having all the paths
		int maxsize = 0; 
		String maxSizeEvent = null;
		for(Map.Entry<String, ArrayList<DirectFollowingRelation>> entry : Sequences.entrySet()) {
			if(entry.getValue().size()>maxsize) {
				maxsize=entry.getValue().size();
				maxSizeEvent = entry.getKey();
			}
		}
		if (nonImputed.get(0).equals(maxSizeEvent)) { //if the maxSizeEvent is the same as orphan event
			imputationRevised=false;
		}else {   //if the maxSizeEvent is NOT the same as orphan event
			for (int i = 0; i<imputationSize;i++) {				
				traceHistory.remove(i);		            
	        }
			imputePrefix(maxSizeEvent, traceHistory);  //SHALL we just impute on the basis of maxSizeEvent OR consider the whole pattern of it?
			imputationRevised=true;
		}		
	}	*/

}
