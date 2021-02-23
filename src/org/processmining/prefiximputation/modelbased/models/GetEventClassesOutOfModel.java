package org.processmining.prefiximputation.modelbased.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.prefiximputation.inventory.NullConfiguration;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class GetEventClassesOutOfModel {
	public static XFactory xesFactory = new XFactoryBufferedImpl();
	public static XExtensionManager xesExtensionManager = XExtensionManager.instance();
	public static final XEventClassifier NAME_CLASSIFIER = new XEventNameClassifier();
	private Map<Transition, String> modelElementsToLabelMap = new HashMap<>();
	private Map<String, Collection<Transition>> labelsToModelElementsMap = new HashMap<>();
	private TObjectDoubleMap<Transition> modelMoveCosts = new TObjectDoubleHashMap<>();
	private TObjectDoubleMap<String> labelMoveCosts = new TObjectDoubleHashMap<>();
	private List<String> processModelAlphabet = new ArrayList<String>();
	

	private Petrinet net;
	private XEventClasses classes = new XEventClasses(XLogInfoImpl.NAME_CLASSIFIER);
	public GetEventClassesOutOfModel(Petrinet net) {
		this.net = net;
	}
	
	public void manipulateModel() {
		//XEventClasses classes = new XEventClasses(XLogInfoImpl.NAME_CLASSIFIER);
		//Set<XEventClass> eventClasses = new HashSet<XEventClass>();
		//XLog atts = xesFactory.createLog();
		for(Transition t: net.getTransitions()) {
			String eventLabel = t.getLabel();
			if(!t.isInvisible() || !eventLabel.equals("Tau")) {
				XEvent event = getXEvent(eventLabel);
				classes.register(event);
			}											
		}
		
		if(NullConfiguration.isMappingAutomatic) {
			for (Transition t : net.getTransitions()) {
				//if (!t.isInvisible()) {
					String label = t.getLabel();
					modelElementsToLabelMap.put(t, label);
					if (!labelsToModelElementsMap.containsKey(label)) {
						labelsToModelElementsMap.put(label, Collections.singleton(t));
					} else {
						labelsToModelElementsMap.get(label).add(t);
					}
				//}
			}
		}else if (!NullConfiguration.isMappingAutomatic) {
			for (Transition t : net.getTransitions()) {
				switch(t.getLabel()) {
					case "A_SUBMITTED":
						modelElementsToLabelMap.put(t, "A_SUBMITTED");
						break;
					case "A_PARTLYSUBMITTED":
						modelElementsToLabelMap.put(t, "A_PARTLYSUBMITTED");
						break;
					case "A_PREACCEPTED":
						modelElementsToLabelMap.put(t, "A_PREACCEPTED");
						break;
					case "A_ACCEPTED":
						modelElementsToLabelMap.put(t, "A_ACCEPTED");
						break;
					case "A_DECLINED_1":
					case "A_DECLINED_2":
					case "A_DECLINED_3":
					case "A_DECLINED_4":
						modelElementsToLabelMap.put(t, "A_DECLINED");
						break;
					case "A_FINALIZED":
						modelElementsToLabelMap.put(t, "A_FINALIZED");
						break;
					case "A_APPROVED":
						modelElementsToLabelMap.put(t, "A_APPROVED");
						break;
					case "A_REGISTERED":
						modelElementsToLabelMap.put(t, "A_REGISTERED");
						break;
					case "A_ACTIVATED":
						modelElementsToLabelMap.put(t, "A_ACTIVATED");
						break;
					case "A_CANCELLED_1":
					case "A_CANCELLED_2":
					case "A_CANCELLED_3":
						modelElementsToLabelMap.put(t, "A_CANCELLED");
						break;						
						
					case "O_SELECTED_1":
					case "O_SELECTED_2":
						modelElementsToLabelMap.put(t, "O_SELECTED");
						break;
					case "O_CREATED":
						modelElementsToLabelMap.put(t, "O_CREATED");
						break;
					case "O_SENT":
						modelElementsToLabelMap.put(t, "O_SENT");
						break;
					case "O_SENT_BACK_INCOMPLETE":
						modelElementsToLabelMap.put(t, "O_SENT_BACK_INCOMPLETE");
						break;
					case "O_CANCELLED_1":
					case "O_CANCELLED_2":
						modelElementsToLabelMap.put(t, "O_CANCELLED");
						break;
					case "O_SENT_BACK":
						modelElementsToLabelMap.put(t, "O_SENT_BACK");
						break;
					case "O_ACCEPTED":
						modelElementsToLabelMap.put(t, "O_ACCEPTED");
						break;
					case "O_DECLINED":
						modelElementsToLabelMap.put(t, "O_DECLINED");
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
			
			Collection<Transition> A_CANCELLED = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("A_CANCELLED_1") || t.getLabel().equals("A_CANCELLED_2") || t.getLabel().equals("A_CANCELLED_3")) {
					A_CANCELLED.add(t);
				}
			}
			labelsToModelElementsMap.put("A_CANCELLED", A_CANCELLED);
			
			
			Collection<Transition> O_CANCELLED = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_CANCELLED_1") || t.getLabel().equals("O_CANCELLED_2")) {
					O_CANCELLED.add(t);
				}
			}
			labelsToModelElementsMap.put("O_CANCELLED", O_CANCELLED);
			
			Collection<Transition> O_SELECTED = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_SELECTED_1") || t.getLabel().equals("O_SELECTED_2")) {
					O_SELECTED.add(t);
				}
			}
			labelsToModelElementsMap.put("O_SELECTED", O_SELECTED);
			
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
		}	
		
		//return classes;
		
		for (Transition t : net.getTransitions()) {
			if (t.isInvisible()) {
				modelMoveCosts.put(t, (short) 0);
				//labelMoveCosts.put(t.getLabel(), (short) 0);
			} else {
				modelMoveCosts.put(t, (short) 1);
				labelMoveCosts.put(t.getLabel(), (short) 1);
			}
		}	
		for (Transition t : net.getTransitions()) {
			processModelAlphabet.add(t.getLabel());
		}
		
	}
	
	public XEventClasses getXEventClasses() {
		return this.classes;
	}
	public Map<Transition, String> getModelElementsToLabelMap(){
		return this.modelElementsToLabelMap;
	}
	public Map<String, Collection<Transition>> getLabelsToModelElementsMap(){
		return this.labelsToModelElementsMap;
	}
	public TObjectDoubleMap<Transition> getModelMoveCosts(){
		return this.modelMoveCosts;
	}
	public TObjectDoubleMap<String> getLabelMoveCosts(){
		return this.labelMoveCosts;
	}
	
	public List<String> getProcessModelAlphabet() {
		return processModelAlphabet;
	}
	/*private void setupLabelMap() {
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
	
	/*public TransEvClassMapping mapEventsToTransitions(Petrinet net, XEventClasses eventClasses) {
		TransEvClassMapping mapping = null;
		for (Transition t: net.getTransitions()) {
			//mapping.put(t, eventClasses.getByIndex(i));           //????classes.getBy
			mapping.put(t, eventClasses.getClassOf(getXEvent(t.getLabel())));
			//i++;
		}
		return mapping;
	}*/
	
	public XEvent getXEvent(String eventName) {
		XAttributeMap atts = xesFactory.createAttributeMap();
		atts.put("concept:name",
			xesFactory.createAttributeLiteral(
					"concept:name",
					eventName,
					xesExtensionManager.getByName("Concept")));
		return xesFactory.createEvent(atts);
	}

}
