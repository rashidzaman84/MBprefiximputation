package org.processmining.prefiximputation.modelbased.completeforgetting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.log.csv.CSVFile;
import org.processmining.log.csv.ICSVReader;
import org.processmining.log.csv.config.CSVConfig;
import org.processmining.log.csvimport.exception.CSVConversionException;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystemImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.onlineconformance.models.ModelSemanticsPetrinet;
import org.processmining.prefiximputation.inventory.NullConfiguration;
import org.processmining.prefiximputation.modelbased.plugins.PrefixAlignmentWithoutImputation;

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
	//private ArrayList<String> NDStarterTransitions = new ArrayList<>();
	public ArrayList<Transition> NDEndingTransitions = new ArrayList<Transition>();
	public HashMap<Transition, Marking> NDEndingTransitionsEnteringMarkings= new HashMap<Transition, Marking>();
	public Transition mainSplitter;
	public Marking mainSplitterOutMarking;
	public CSVFile modelStreamMapping=null;
	
	private TransitionSystemImpl transitionSystem = null;
	private UIPluginContext context;
	private Petrinet net;
	private XEventClasses classes = new XEventClasses(XLogInfoImpl.NAME_CLASSIFIER);
	
	/*public ArrayList<String> getNDStarterTransitions() {
		return NDStarterTransitions;
	}*/

	public ArrayList<Transition> getNDEndingTransitions() {
		return NDEndingTransitions;
	}

	//private ArrayList<String> NDEndingTransitions = new ArrayList<>();
	

	
	public GetEventClassesOutOfModel(UIPluginContext context, Petrinet net, CSVFile modelStreamMapping) {
		this.net = net;
		this.context = context;
		this.modelStreamMapping = modelStreamMapping;
	}
	
	public void manipulateModel() throws IOException, CSVConversionException {
		//XEventClasses classes = new XEventClasses(XLogInfoImpl.NAME_CLASSIFIER);
		//Set<XEventClass> eventClasses = new HashSet<XEventClass>();
		//XLog atts = xesFactory.createLog();
		extractClasses();
		//mapping();
		mapping2();
		setMovesCosts();		
		extractModelAplhabet();		
		extractNDRegions();
		/*for(Transition t: net.getTransitions()) {
			String eventLabel = t.getLabel();
			if(!t.isInvisible() || !eventLabel.equals("Tau")) {
				XEvent event = getXEvent(eventLabel);
				classes.register(event);
			}											
		}*/
		
		/*if(NullConfiguration.isMappingAutomatic) {
			for (Transition t : net.getTransitions()) {
				if (!t.isInvisible()) {
					String label = t.getLabel();
					modelElementsToLabelMap.put(t, label);
					if (!labelsToModelElementsMap.containsKey(label)) {
						Collection collection = new ArrayList<Transition>();
						collection.add(t);
						labelsToModelElementsMap.put(label, collection);
						//labelsToModelElementsMap.put(label, Collections.singleton(t));
					} else {
						labelsToModelElementsMap.get(label).add(t);
					}
				}
			}
		}else if (!NullConfiguration.isMappingAutomatic) {
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
					case "O_SENT_BACK_1":
					case "O_SENT_BACK_2":
						modelElementsToLabelMap.put(t, "O_SENT_BACK");
						break;
					case "O_S_BACK":
						modelElementsToLabelMap.put(t, "O_S_BACK");
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
			
			Collection<Transition> O_SENT_BACK = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().equals("O_SENT_BACK_1") || t.getLabel().equals("O_SENT_BACK_2")) {
					O_SENT_BACK.add(t);
				}
			}
			labelsToModelElementsMap.put("O_SENT_BACK", O_SENT_BACK);
			
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
						t.getLabel().equals("O_SENT_BACK_1") || t.getLabel().equals("O_SENT_BACK_2") ||
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
						t.getLabel().equals("A_Canc_1") ||t.getLabel().equals("A_Canc_2") || t.getLabel().equals("A_Canc_3") || t.getLabel().contains("tau")) {
					;
				}else {
					labelsToModelElementsMap.put(t.getLabel(), Collections.singleton(t));
				}
			}
		}	*/
		
		//return classes;
		//setMovesCosts();
		/*for (Transition t : net.getTransitions()) {
			if (t.isInvisible() || (t.getLabel().equals("A_FINALIZED"))) {
				modelMoveCosts.put(t, (short) 0);
				//labelMoveCosts.put(t.getLabel(), (short) 0);
			} else {
				modelMoveCosts.put(t, (short) 1);
				labelMoveCosts.put(t.getLabel(), (short) 1);
				//labelMoveCosts.put("A_FINALIZED", (short) 1);
			}
		}*/	
		//extractModelAplhabet();
		/*for (Transition t : net.getTransitions()) {
			processModelAlphabet.add(t.getLabel());
		}*/
		
		//extractNDRegions();
		
		/*for(Transition t: net.getTransitions()) {
			int noofchildren = 0;
			for(PetrinetEdge p : net.getEdges()) {
				if(p.getSource().equals(t)) {
					noofchildren++;
					//System.out.println(t.getLabel());
					//System.out.println(p.getTarget());
				}
			}
			if(noofchildren>1) {
				NDStarterTransitions.add(t.getLabel());
			}
			
			//System.out.println(t + ", " + t.getVisibleSuccessors());
			//System.out.println(t + "  has parent: " + t.getParent());
		}
		//System.out.println(NDStarterTransitions);
		//System.out.println("----------------------------------");
		for(Transition t: net.getTransitions()) {
			int noofchildren = 0;
			for(PetrinetEdge p : net.getEdges()) {
				if(p.getTarget().equals(t)) {
					noofchildren++;
					//System.out.println(t.getLabel());
					//System.out.println(p.getTarget());
				}
			}
			if(noofchildren>1) {
				NDEndingTransitions.add(t.getLabel());
			}
		}*/
		//System.out.println(NDEndingTransitions);
		
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
	private void extractClasses() {
		for(Transition t: net.getTransitions()) {
			String eventLabel = t.getLabel();
			if(!t.isInvisible() || !eventLabel.equals("Tau")) {
				XEvent event = getXEvent(eventLabel);
				classes.register(event);
			}											
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
	
	private void mapping2() throws IOException, CSVConversionException {
		if(Objects.isNull(modelStreamMapping)) {
			for (Transition t : net.getTransitions()) {
				if (!t.isInvisible()) {
					String label = t.getLabel();
					modelElementsToLabelMap.put(t, label);
					if (!labelsToModelElementsMap.containsKey(label)) {
						Collection collection = new ArrayList<Transition>();
						collection.add(t);
						labelsToModelElementsMap.put(label, collection);
						//labelsToModelElementsMap.put(label, Collections.singleton(t));
					} else {
						labelsToModelElementsMap.get(label).add(t);
					}
				}
			}
		}else {			
			String[] nextLine= new String[2];
			CSVConfig importConfig = new CSVConfig(modelStreamMapping);
			try (ICSVReader reader = modelStreamMapping.createReader(importConfig)) {
				while ((nextLine = reader.readNext()) != null) {
					for(Transition t: net.getTransitions()) {
						if(t.getLabel().equals(nextLine[0])) {
							modelElementsToLabelMap.put(t, nextLine[1]);
						}
					}
				}
		}
			
			HashSet<String> distinctValues = new HashSet<>();
			for(Entry<Transition, String> entry: modelElementsToLabelMap.entrySet()) {
				distinctValues.add(entry.getValue());
			}
			
			for(String value : distinctValues) {
				Collection<Transition> temp = new ArrayList<Transition>();
				for(Entry<Transition, String> entry: modelElementsToLabelMap.entrySet()) {
					if(entry.getValue().equals(value)) {
						temp.add(entry.getKey());
					}
				}
				labelsToModelElementsMap.put(value, temp);
			}			
		}
			
	}
	
	private void mapping() {	
		
		if(NullConfiguration.isMappingAutomatic) {
			for (Transition t : net.getTransitions()) {
				if (!t.isInvisible()) {
					String label = t.getLabel();
					modelElementsToLabelMap.put(t, label);
					if (!labelsToModelElementsMap.containsKey(label)) {
						Collection collection = new ArrayList<Transition>();
						collection.add(t);
						labelsToModelElementsMap.put(label, collection);
						//labelsToModelElementsMap.put(label, Collections.singleton(t));
					} else {
						labelsToModelElementsMap.get(label).add(t);
					}
				}
			}
		}else if (!NullConfiguration.isMappingAutomatic && NullConfiguration.eventlog.equals("BPI12AplusO")) {
			for (Transition t : net.getTransitions()) {
				if(!t.isInvisible()) {
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
							System.out.println("something wrong");
					}
				}
				/*switch(t.getLabel()) {
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
					case "O_SENT_BACK_1":
					case "O_SENT_BACK_2":
						modelElementsToLabelMap.put(t, "O_SENT_BACK");
						break;
						case "O_S_BACK":
					modelElementsToLabelMap.put(t, "O_S_BACK");
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

				}*/
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

			/*Collection<Transition> Taus = new ArrayList<Transition>();
			for (Transition t : net.getTransitions()) {
				if (t.getLabel().contains("tau")) {
					Taus.add(t);
				}
			}
			labelsToModelElementsMap.put("tau", Taus);*/

			for (Transition t : net.getTransitions()) {
				
				if(!t.isInvisible()) {
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
				
			}
			
			
			/*Collection<Transition> A_DECLINED = new ArrayList<Transition>();
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
						t.getLabel().equals("O_SENT_BACK_1") || t.getLabel().equals("O_SENT_BACK_2") ||
						t.getLabel().equals("O_SELECTED_1") || t.getLabel().equals("O_SELECTED_2")||
						t.getLabel().equals("A_DECLINED_1") || t.getLabel().equals("A_DECLINED_2") || t.getLabel().equals("A_DECLINED_3") || t.getLabel().equals("A_DECLINED_4")||
						t.getLabel().equals("A_CANCELLED_1") ||t.getLabel().equals("A_CANCELLED_2") || t.getLabel().equals("A_CANCELLED_3") || t.getLabel().contains("tau")) {
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
	}	
	
	private void setMovesCosts() {
		for (Transition t : net.getTransitions()) {
			if (t.isInvisible() /*|| (t.getLabel().equals("A_FINALIZED"))*/) {
				modelMoveCosts.put(t, (short) 0);
				//labelMoveCosts.put(t.getLabel(), (short) 0);
			} else {
				modelMoveCosts.put(t, (short) 1);
				labelMoveCosts.put(t.getLabel(), (short) 1);
				//labelMoveCosts.put("A_FINALIZED", (short) 1);
			}
		}
		//labelMoveCosts.put("dummy", (short) 1);
	}
	
	private void extractModelAplhabet() {
		for (Transition t : net.getTransitions()) {
			processModelAlphabet.add(t.getLabel());
		}
	}
	
	public void extractNDRegions() {		
		
		Marking initialMarking = PrefixAlignmentWithoutImputation.getInitialMarking(net);
		System.out.println("Initial Marking " + initialMarking);
		Marking finalMarking = PrefixAlignmentWithoutImputation.getFinalMarking(net);
		System.out.println("Final Marking " + finalMarking);
		//for (Transition t : net.getTransitions()) 
		//(Flow f : sys.getFlow()) {
		//sys.getplaces f.getSource() f.getTarget()
		ModelSemanticsPetrinet<Marking> modelSemantics = ModelSemanticsPetrinet.Factory.construct(net);
		System.out.println("Transition enabled in the initial Marking of the model " + modelSemantics.getEnabledTransitions(initialMarking));
		ArrayList<Transition> NDStarterTransitions = new ArrayList<Transition>();
		/*ArrayList<Transition> NDEndingTransitions = new ArrayList<Transition>();
		HashMap<Transition, Marking> NDEndingTransitionsEnteringMarkings= new HashMap<Transition, Marking>();
		Transition mainSplitter;*/
		mainSplitterOutMarking = new Marking();
		
		for(Transition t: net.getTransitions()) {
			int noOfChildren = 0;
			for(PetrinetEdge p : net.getEdges()) {
				if(p.getSource().equals(t)) {
					noOfChildren++;
					//System.out.println(t.getLabel());
					//System.out.println(p.getTarget());
				}
			}
			if(noOfChildren>1) {
				NDStarterTransitions.add(t);
			}
			
			//System.out.println(t + ", " + t.getVisibleSuccessors());
			//System.out.println(t + "  has parent: " + t.getParent());
		}
		//System.out.println(NDStarterTransitions);
		if(NDStarterTransitions.isEmpty()) {
			return;
		}else if(NDStarterTransitions.size()==1) {
			mainSplitter = NDStarterTransitions.get(0);
		}else {
			mainSplitter = getFirstSplitter(modelSemantics,initialMarking, net);			
		}
		
		for(PetrinetEdge p : net.getEdges()) {
			if((p.getSource() instanceof Transition) && p.getSource().equals(mainSplitter)) {
				
				//System.out.println(p.getTarget() );
				for(Place pl: net.getPlaces()) {
					//System.out.println((pl.getLabel() + "," + p.getTarget().toString()));
					if(pl.getLabel().equals(p.getTarget().toString())) {
						System.out.println(pl);
						mainSplitterOutMarking.add(pl);
					}
				}
				//mainSplitterOutMarking.add( p.getLabel());
			}
		}
		System.out.println("The marking of the main splitter " + mainSplitterOutMarking);
		System.out.println("----------------------------------");
		for(Transition t: net.getTransitions()) {
			int noofchildren = 0;
			for(PetrinetEdge p : net.getEdges()) {
				if(p.getTarget().equals(t)) {
					noofchildren++;
					//System.out.println(t.getLabel());
					//System.out.println(p.getTarget());
				}
			}
			if(noofchildren>1) {
				NDEndingTransitions.add(t);
			}
		}
		System.out.println(NDEndingTransitions);
		System.out.println("----------------------------------");
		for(Transition t: NDEndingTransitions) {
			Marking temp = new Marking();
			//System.out.println(t.getVisiblePredecessors());
			for(PetrinetEdge p : net.getEdges()) {
				if(p.getTarget().equals(t)) {
					System.out.println(p.getTarget() );
					for(Place pl: net.getPlaces()) {
						if(pl.getLabel().equals(p.getSource().toString())) {
							System.out.println(pl);
							temp.add(pl);
						}
					}
					//mainSplitterOutMarking.add( p.getLabel());
				}
			}
			NDEndingTransitionsEnteringMarkings.put(t,temp);
			
		}
		
		
		
		System.out.println(NDEndingTransitionsEnteringMarkings);
		
	
	
		
	
	}
	public static Transition getFirstSplitter(ModelSemanticsPetrinet<Marking> modelSemantics, Marking initialMarking, Petrinet net) {
		boolean found = false;
		Transition tt=null;
		for(Transition t: modelSemantics.getEnabledTransitions(initialMarking)) {
			int noofchildren = 0;
			for(PetrinetEdge p : net.getEdges()) {
				if(p.getSource().equals(t)) {
					noofchildren++;
					//System.out.println(t.getLabel());
					//System.out.println(p.getTarget());
				}
			}
			if(noofchildren>1) {
				System.out.println(t.getLabel());
				found = true;
				tt=t;
				break;
				
			}
		}
		if(!found) {
			for(Transition t: modelSemantics.getEnabledTransitions(initialMarking)) {
				Marking m = modelSemantics.execute(initialMarking, t);
				tt = getFirstSplitter(modelSemantics, m, net);
				if(tt!=null) {
					break;
				}else {
					continue;
				}
			}
			
		}
		
		if(tt!=null) {
			//System.out.println("here");
			return tt;
			
		}else {
			return null;
		}
	
		
	}
	
	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}
	//System.out.println(NDEndingTransitions);
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
	
	

}
