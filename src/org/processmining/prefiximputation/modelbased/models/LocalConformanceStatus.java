package org.processmining.prefiximputation.modelbased.models;

import java.util.ArrayList;
import java.util.Date;

/**
 * This class keeps track of the conformance status for a single process instance
 * 
 * @author Andrea Burattin
 */
public class LocalConformanceStatus {

	protected int currentImputationSize=0;
	//protected XTrace trace;
	protected ArrayList<String> trace;
	//protected Map<Integer,ArrayList<String>> imputationHistory;
	protected boolean isInNonDeterministicRegion=false;
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
	protected OnlineConformanceChecker2 OCC2;
	protected PrefixImputation prefixImputation;
	//protected OnlineConformanceCalculator2 OCC2 = new OnlineConformanceCalculator2(lms);
	//protected OnlineConformanceCalculator2 OCC2 = new OnlineConformanceCalculator2();
	protected Boolean imputationRevisitSelected=false;
	protected int imputationRevisitWindowSize;
	protected String caseId;
	
	public LocalConformanceStatus (LocalModelStructure lms) {
		this.lms = lms;
		//this.OCC2 = new OnlineConformanceChecker2(this.lms);
		//this.lastUpdate = new Date();		
	}
	
	public LocalConformanceStatus (LocalModelStructure lms, String ccAlgoChoice, Integer imputationRevisitWindowSize, String caseId ) {
		this.lms = lms;
		this.ccAlgoChoice = ccAlgoChoice;
		//this.OCC2 = new OnlineConformanceChecker2(this.lms);		
		this.imputationRevisitWindowSize = imputationRevisitWindowSize;
		this.imputationRevisitSelected = imputationRevisitWindowSize==0?false:true;
		this.caseId = caseId;
		this.trace = new ArrayList<String>();
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
	
	public OnlineConformanceScore replayEventBehProf(String newEventName, /*XTrace tr,*/ Boolean isNew) {
		
		if(isNew){                                   //first observed event for a new case
			if (!lms.isFirstEvent(newEventName)) {   //first observed event for a case is not a case-starter
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
				isInNonDeterministicRegion = lms.isInNonDeterministicRegion(newEventName);
				if(isInNonDeterministicRegion) {
					//>>System.out.println("The event: " + newEventName + " is found to be located in NDR");
					//locate the orphan event in the process tree and find the relevant ND regions and activities to this case;
					//Accordingly, the MM cost for these found activites will be set to 0
					//How will this effect the imputation window approach
				}
				
				
				//>>System.out.println("The arrived event is Orphan and needs Prefix imputation");
				prefixImputation =  new PrefixImputation(this.lms);
				prefixImputation.imputePrefix(newEventName);
				trace = prefixImputation.imputedTrace;
				currentImputationSize = prefixImputation.imputationSize;
				//imputationHistory = new HashMap<Integer,ArrayList<String>>();
				//imputationHistory.put(0, prefixImputation.imputedPrefix);
				
				batchCCBehProf(trace);
								
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
				trace.add(newEventName);
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
			trace.add(newEventName);
			//>>System.out.println("The arrived belongs to an already existing case and added to its case history");
			if(currentImputationSize>0 && imputationRevisitSelected==true && (trace.size()-currentImputationSize)==imputationRevisitWindowSize){
				//revisit the imputed prefix by extracting the part other than the imputed prefix and check the most probable
				//shortest path for it now
				//IF the Prefix is changed THEN reset the prefixImputation object OTHERWISE DO NOTHING
					//reset the OCC1 object
					//recmompute the conformance w.r.t to the new prefix and trace
				//>>System.out.println("The imputation revision option is enabled so we have to revisit the imputation");
				prefixImputation.revisitPrefix(trace);
				
				if(prefixImputation.imputationRevised) {
					trace = prefixImputation.imputedTrace;
					currentImputationSize = prefixImputation.imputationSize;
					//imputationHistory.put(1, prefixImputation.imputedPrefix);
					batchCCBehProf(trace);
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
	
	
	
	public OnlineConformanceScore replayEventPrefAlign(String newEventName, /*XTrace tr,*/ Boolean isNew) {
		
		if(isNew) {			
			if (!lms.isFirstEvent(newEventName)) {             //if observed event is orphan
								
				//If the orphan event is located in one of the Non-Deterministic Region (NDR) then set the flag
				//isInNonDeterministicRegion to "true" and copy the transitions of that NDR to the related LCS i.e.,
				//transitionsInNonDeterministicRegion
				
				isInNonDeterministicRegion = lms.isInNonDeterministicRegion(newEventName);
				if(isInNonDeterministicRegion) {
					//>>System.out.println("The event: " + newEventName + " is found to be located in NDR");
					//locate the orphan event in the process tree and find the relevant ND regions and activities to this case;
					//Accordingly, the MM cost for these found activites will be set to 0
					//How will this effect the imputation window approach
				}
				/*for (Map.Entry<Integer, ArrayList<String>> entry : (lms.getNonDeterministicRegions()).entrySet()) {				   
				    isInNonDeterministicRegion = (entry.getValue()).contains(newEventName);
				    if(isInNonDeterministicRegion) {
				    	System.out.println("The event: " + newEventName + " is found to be located in NDR: " +  entry.getKey());
						transitionsInNonDeterministicRegion = new ArrayList<String>();
						transitionsInNonDeterministicRegion.addAll(entry.getValue());
						break;
					}
				}*/
				
				prefixImputation =  new PrefixImputation(lms);
				prefixImputation.imputePrefix(newEventName);
				trace = prefixImputation.imputedTrace;
				currentImputationSize = prefixImputation.imputationSize;
				//imputationHistory = new HashMap<Integer,ArrayList<String>>();
				//imputationHistory.put(0, prefixImputation.imputedPrefix);
				OCC2 = new OnlineConformanceChecker2(this.lms, false);
				batchCCPrefAlign(trace);
				//>>System.out.println(last.toString());
				refreshUpdateTime();
				return last;
			}else {
				trace.add(newEventName);
				OCC2 = new OnlineConformanceChecker2(this.lms, false);
				last.setConformance(OCC2.processXLog(caseId, newEventName)>0.0?0.0:1.0);
				//>>System.out.println("The newly arrived event is the case-starting event and conformance has been calculated as:");
				//last.setConformance(1.0);
				//last.setCompleteness(0.0);
				//last.setConfidence(0.0);
				//last.isLastObservedViolation(false);
				//>>System.out.println(last.toString());
				refreshUpdateTime();
				return last;
			}
			
		}else {
			trace.add(newEventName); //the newly arrived event is added to the existing trace history
			if(currentImputationSize>0 && imputationRevisitSelected==true && (trace.size()-currentImputationSize)==imputationRevisitWindowSize){
				//revisit the imputed prefix by extracting the part other than the imputed prefix and check the most probable
				//shortest path for it now
				//>>System.out.println("The imputation revision option is enabled so we have to revisit the imputation");
				prefixImputation.revisitPrefix(trace);
				
				if(prefixImputation.imputationRevised) {
					trace = prefixImputation.imputedTrace;
					currentImputationSize = prefixImputation.imputationSize;
					//imputationHistory.put(1, prefixImputation.imputedPrefix);
					this.OCC2.applyGeneric();
					batchCCPrefAlign(trace);
				}				
				
				imputationRevisitSelected=false; //to avoid multiple revisits
				//>>System.out.println(last.toString());
				refreshUpdateTime();
				return last;
			}
			//>>System.out.println("The newly arrived event is NOT a case-starting event and added to the its existing history. Now the case is: " + trace);
			last.setConformance(OCC2.processXLog(caseId, newEventName)>0.0?0.0:1.0);                         //The trace cost needs to be transformed into conformance score.
			//>>System.out.println(last.toString());
			refreshUpdateTime();
			return last;
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
			last.setConformance(OCC2.processXLog(caseId, xtrace.get(i))>0.0?0.0:1.0);
		}
	}
}
