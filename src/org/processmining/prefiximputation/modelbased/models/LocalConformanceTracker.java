package org.processmining.prefiximputation.modelbased.models;

import java.util.HashMap;
import java.util.Set;

/**
 * This class keeps track of the conformance status of a whole stream, by dispatching the events based on their
 * process instance. Also it keeps track of the different cases and is in charge of removing old ones.
 * 
 * @author Andrea Burattin
 */
public class LocalConformanceTracker extends HashMap<String, LocalConformanceStatus> {

	private static final long serialVersionUID = -7453522111588238137L;

	//protected Map<String, Integer> caseIdsWithImputationInfo;   
	//protected Map<String, XTrace> casesHistory; 
	//protected Map<String, Integer> casesImputationHistory; 
	protected LocalModelStructure lms;
	protected int noOfCasesInMemory;
	protected int maxCasesToStore;
	protected String ccAlgoChoice;
	/*protected int statesToStore;
	protected int costNoActivity;
	protected int errorsToStore;	*/
	//protected boolean imputationRevisitSelected;
	protected int imputationRevisitWindowSize;
	//private String activityName;
	//public static XFactory xesFactory = new XFactoryBufferedImpl();
	//public static XExtensionManager xesExtensionManager = XExtensionManager.instance();

	public LocalConformanceTracker(LocalModelStructure lms, int maxCasesToStore) {
		//this.caseIdHistory = new HashMap<String, Integer>();
		this.lms = lms;
		this.maxCasesToStore = maxCasesToStore;
		//this.caseIdsWithImputationInfo = new HashMap<String, Integer>();
		//this.casesHistory = new HashMap<String, XTrace>();
		this.noOfCasesInMemory=0;
	}
	
	public LocalConformanceTracker(LocalModelStructure lms, int maxCasesToStore, String ccAlgoChoice) {
		//this.caseIdHistory = new HashMap<String, Integer>();
		this.lms = lms;
		this.maxCasesToStore = maxCasesToStore;
		//this.caseIdsWithImputationInfo = new HashMap<String, Integer>();
		//this.casesHistory = new HashMap<String, XTrace>();
		this.noOfCasesInMemory=0;
		this.ccAlgoChoice = ccAlgoChoice;
	}
	
	public LocalConformanceTracker(LocalModelStructure lms, int maxCasesToStore, String ccAlgoChoice, Integer imputationRevisitWindowSize) {
		//this.caseIdHistory = new HashMap<String, Integer>();
		this.lms = lms;
		this.maxCasesToStore = maxCasesToStore;
		//this.caseIdsWithImputationInfo = new HashMap<String, Integer>();
		//this.casesHistory = new HashMap<String, XTrace>();
		this.noOfCasesInMemory=0;
		this.ccAlgoChoice = ccAlgoChoice;
		//this.imputationRevisitSelected = imputationRevisitSelected;
		this.imputationRevisitWindowSize = imputationRevisitWindowSize;
	}
	
	/**
	 * This method performs the replay of an event and keeps track of corresponding process instance.
	 * 
	 * @param caseId
	 * @param newEventName
	 * @return
	 */
	
	
	
	
	public OnlineConformanceScore replayEvent(String caseId, String newEventName) {
		OnlineConformanceScore currentScore;
		
		if (containsKey(caseId)) {
			currentScore = get(caseId).replayTrace(newEventName, false);
			//currentScore = get(caseId).replayTrace(newEventName, tr, false); //Conformance Checking of an existing case(object)
																  //i.e.,LocalConformanceStatus
			
		} else {
			// check if we can store the new case
			if (size() >= maxCasesToStore) {
				// The event store is full to the allowed extent, we need to devise smart strategies to remove cases:
				// (i)with compliance score of 1, (ii)the cases out of non-deterministic region
				//(iii) with the least imputation score i.e., least no. of imputed transitions OR a hybrid of these.
				CasesRemoval casesRemoval = new CasesRemoval(lms, this );               //We can calculate a deletion candidacy score for relevant cases on arrival of every new event but that will
				String toRemove = casesRemoval.selectCaseToBeRemoved();              //introduce unnecessary processing overhead as we may need to delete cases once in a while.				
				//>>System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!The memory is full as there are: " + size() + " cases in memory. Therefore, this case is removed, Id: " + toRemove + " and trace is: " + get(toRemove).trace  );
				this.remove(toRemove); //Removed the LocalConformanceTracker HashMap entry corresponding to the toRemove case ID
			}			
			
			// now we can perform the replay
			LocalConformanceStatus lcs = new LocalConformanceStatus(lms, this.ccAlgoChoice, this.imputationRevisitWindowSize, caseId);
			//currentScore = lcs.replayTrace(newEventName, tr, true );
			currentScore = lcs.replayTrace(newEventName,true );
			put(caseId, lcs);
			
		}
		return currentScore;
	}
	
	public Set<String> getHandledCases() {
		return keySet();
	}
	
	/*public OnlineConformanceScore replayEvent(String caseId, String newEventName, XTrace tr) {
		OnlineConformanceScore currentScore;
		
		if (containsKey(caseId)) {
			
			currentScore = get(caseId).replayTrace(newEventName, tr, false); //Conformance Checking of an existing case(object)
																  //i.e.,LocalConformanceStatus
			
		} else {
			// check if we can store the new case
			if (size() >= maxCasesToStore) {
				// The event store is full to the allowed extent, we need to devise smart strategies to remove cases:
				// (i)with compliance score of 1, (ii)the cases out of non-deterministic region
				//(iii) with the least imputation score i.e., least no. of imputed transitions OR a hybrid of these.
				CasesRemoval casesRemoval = new CasesRemoval(lms, this );               //We can calculate a deletion candidacy score for relevant cases on arrival of every new event but that will
				String toRemove = casesRemoval.selectCaseToBeRemoved();              //introduce unnecessary processing overhead as we may need to delete cases once in a while.				
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!The memory is full as there are: " + size() + " cases in memory. Therefore, this case is removed, Id: " + toRemove + " and trace: " + get(toRemove).trace  );
				remove(toRemove); //Removed the LocalConformanceTracker HashMap entry corresponding to the toRemove case ID
			}			
			
			// now we can perform the replay
			LocalConformanceStatus lcs = new LocalConformanceStatus(lms, this.ccAlgoChoice, this.imputationRevisitWindowSize, caseId);
			currentScore = lcs.replayTrace(newEventName, tr, true );
			put(caseId, lcs);
			
		}
		return currentScore;
	}*/
	
	/*public OnlineConformanceScore replayEvent(String caseId, String newEventName) {   //to be removed
	OnlineConformanceScore currentScore;
	
	if (containsKey(caseId)) {
		// now we can perform the replay
		currentScore = get(caseId).replayEvent(newEventName);    //Conformance Checking of an existing case(object)
																//i.e.,LocalConformanceStatus
		// need to refresh the cache
		//caseIdsWithImputationInfo.remove(caseId);          //redundant?? as Hashmap can replace old values 
	} else {
		// check if we can store the new case
		if (caseIdsWithImputationInfo.size() >= maxCasesToStore) {
			// we have no room for the case, we need to remove the case id
			// with most far update time
			
			String toRemove = selectToRemove(); 
			remove(toRemove);
		}
		if (!lms.isFirstEvent(newEventName)) {
			ArrayList<String> imputedPrefix = lms.getShortestPrefix(newEventName);
			int imputedPrefixLength = imputedPrefix.size();
			System.out.println("The imputed prefix for Orphan event: " + newEventName + " is: " + imputedPrefix + " with length of: " + imputedPrefixLength);
		}
		
		//casesImputationHistory.put(caseId, len);
		//affix the imputed prefix to the current event to generate the incomplete trace and put it to
		//the concerned/new process instance....
		
		// now we can perform the replay
		LocalConformanceStatus lcs = new LocalConformanceStatus(lms);
		currentScore = lcs.replayEvent(newEventName);
		put(caseId, lcs);
		
	}
	// put the replayed case as first one
	caseIdsWithImputationInfo.put(caseId,0);

	return currentScore;
}*/
}
