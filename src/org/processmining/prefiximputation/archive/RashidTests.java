package org.processmining.prefiximputation.archive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.io.PNMLSerializer;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.operationalsupport.xml.OSXMLConverter;

public class RashidTests {
	private ArrayList<String> allowedActivities = new ArrayList<String>();
	protected static OSXMLConverter converter = new OSXMLConverter();
	public static XFactory xesFactory = new XFactoryBufferedImpl();
	public static XExtensionManager xesExtensionManager = XExtensionManager.instance();
	public static final XEventClassifier NAME_CLASSIFIER = new XEventNameClassifier();
	
	public static void main(String[] args) throws Exception {
		
		//LpSolve.lpSolveVersion();
		
		String petrinetFile = "D:\\TEST\\simplest2.pnml";
		Petrinet net = constructNet(petrinetFile);
		Marking initialMarking = getInitialMarking(net);
		Marking finalMarking = getFinalMarking(net);
		
		//Marking finalMarking = "P3";
		//XEventClasses classes;
		//classes.getClassOf(event)
		
		
		
		
		RashidTests rs= new RashidTests();
		
		/*for (Transition t : net.getTransitions()) {
			System.out.println(t.toString());
			System.out.println(net.getTransitions().toString());
			rs.allowedActivities=(ArrayList<String>)net.getTransitions();
			
		}*/
		
		String str = "<org.deckfour.xes.model.impl.XTraceImpl><log openxes.version=\"1.0RC7\" xes.features=\"nested-attributes\" xes.version=\"1.0\" xmlns=\"http://www.xes-standard.org/\"><trace><string key=\"concept:name\" value=\"case_134\"/><event><string key=\"concept:name\" value=\"A\"/><date key=\"time:timestamp\" value=\"2020-10-28T00:00:13.608+01:00\"/></event></trace></log></org.deckfour.xes.model.impl.XTraceImpl>";
		XTrace t = (XTrace) converter.fromXML(str);
		System.out.println(t.size());
		System.out.println(XConceptExtension.instance().extractName(t));
		String b= "B";
		t.add(getXEvent(b));
		t.add(getXEvent("E"));
		System.out.println(t.size());
		System.out.println(isTraceSameAsShortestPath(t));
		System.out.println("----------------");
		System.out.println(XConceptExtension.instance().extractName(t.get(0)));
		System.out.println(XConceptExtension.instance().extractName(t.get(1)));
		System.out.println(XConceptExtension.instance().extractName(t.get(2)));
		/*for (int j=0; j<rs.allowedActivities.size();j++) {
			System.out.println(rs.allowedActivities.get(j));
		}*/
		System.out.println("NEW TEST----------------");
		XEventClasses classes = new XEventClasses(XLogInfoImpl.NAME_CLASSIFIER);
		//Set<XEventClass> eventClasses = new HashSet<XEventClass>();
		//XLog atts = xesFactory.createLog();
		for(Transition tt: net.getTransitions()) {
			XEvent event = getXEvent(tt.getLabel());
			//if(!label invisible)
			classes.register(event);					
		}
		System.out.println(classes.getClasses());
		
		//OnlineConformanceChecker2 OCC = new OnlineConformanceChecker2(net, initialMarking, finalMarking);
		//OCC.doReplay(t);
	}
	
	public static XEvent getXEvent(String eventName) {
		XAttributeMap atts = xesFactory.createAttributeMap();
		atts.put("concept:name",
			xesFactory.createAttributeLiteral(
					"concept:name",
					eventName,
					xesExtensionManager.getByName("Concept")));
		return xesFactory.createEvent(atts);
	}
	public static Boolean isTraceSameAsShortestPath(XTrace trace) {
		boolean same =true;
		String lastEventInTrace = XConceptExtension.instance().extractName(trace.get((trace.size()-1)));
		System.out.println(lastEventInTrace);
		ArrayList<String> test = new ArrayList<String>();
		test.add("A");
		test.add("B");
		test.add("E");
		if(trace.size()!=test.size()) {
			System.out.println("Not same");
			return false;
		}
		for(int i=0; i<trace.size(); i++) {
			String eventName = XConceptExtension.instance().extractName(trace.get(i));
			System.out.println("The event at position: " + i + " is " + eventName);
			if(!eventName.equals(test.get(i))) {
				same = false;
				break;
			}
		}
		
		return same;
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
	
	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}
	
	private static Marking getFinalMarking(PetrinetGraph net) {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		return finalMarking;
	}
}
