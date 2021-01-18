package org.processmining.prefiximputation.modelbased.models;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
		//return classes;
		
		for (Transition t : net.getTransitions()) {
			if (t.isInvisible()) {
				modelMoveCosts.put(t, (short) 0);
			} else {
				modelMoveCosts.put(t, (short) 1);
				labelMoveCosts.put(t.getLabel(), (short) 1);
			}
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
