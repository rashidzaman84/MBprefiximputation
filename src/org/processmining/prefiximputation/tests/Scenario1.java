package org.processmining.prefiximputation.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.prefiximputation.modelbased.completeforgetting.LocalConformanceTracker;
import org.processmining.prefiximputation.modelbased.completeforgetting.LocalModelStructure;
import org.processmining.prefiximputation.modelbased.plugins.LocalOnlineConformanceConfiguration;
import org.processmining.processtree.ProcessTree;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

@Plugin(
	name = "Online Conformance - With Model-Based Prefix Imputation - S1-S2",
	returnLabels = { /*"Online conformance checking - with Model-based Prefix Imputation"*/ },
	returnTypes = { /*LocalOnlineConformanceConfiguration.class*/ },
	parameterLabels = {
			"Model", "Marking", "Process Tree", "Event Data"
	},
	categories = PluginCategory.Analytics,
	help = "This plugin computes the conformance of a given model with respect to an event streams.",
	userAccessible = true)
public class Scenario1 {
	public static XFactory xesFactory = new XFactoryBufferedImpl();
	protected Runtime runtime = Runtime.getRuntime();
	@PluginVariant(requiredParameterLabels = { 0, 1, 2, 3 })
	@UITopiaVariant(
		author = "R. Zaman",
		email = "r.zaman@tue.nl",
		affiliation = "TUe")
	
	public LocalOnlineConformanceConfiguration plugin(UIPluginContext context, Petrinet net, Marking initMarking, ProcessTree tree, XLog log) throws Exception {
		
		LocalModelStructure lms = new LocalModelStructure(context, net, initMarking, /*tree,*/ "Prefix Alignment", 0); 				
		LocalConformanceTracker tracker = new LocalConformanceTracker(lms, 100);
		
		XEventClasses eventClasses = XEventClasses.deriveEventClasses(new XEventNameClassifier(), log);
		
		int factor = 1;
		List<Double> memoryList = new ArrayList<Double>();
		final TObjectDoubleMap<String> costPerTrace = new TObjectDoubleHashMap<>();
		System.out.println("Before Start: " + calculateMemoryUsage()); //the memory usage before the start of the CC checking
		while (!log.isEmpty()) {
			for (XTrace t : log) {
				int numberOfCases = 25;
				int numberOfEvents = 6;
				int innerLoop = (t.size()>=numberOfEvents?numberOfEvents:t.size());
				for(int x=0; x<numberOfCases; x++) {					
					String caseId = XConceptExtension.instance().extractName(t);
					List<String> traceStrLst = toStringList(t, eventClasses);
					double traceCost=0.0;
					for(int y=0; y<innerLoop; y++) {
						traceCost += tracker.replayEvent(caseId, traceStrLst.get(y)).getConformance();
						t.remove(0);
					}
					costPerTrace.adjustOrPutValue(caseId, traceCost, traceCost);
					
				}
				factor = alternateFactor(factor);
				String caseId = XConceptExtension.instance().extractName(t);
				List<String> traceStrLst = toStringList(t, eventClasses);
				double traceCost=0.0;
				int loopsize = (t.size()>=factor?factor:t.size());
				for(int j=0; j<loopsize; j++) {
					traceCost += tracker.replayEvent(caseId, traceStrLst.get(j)).getConformance();
					t.remove(0);
				}
				
				costPerTrace.adjustOrPutValue(caseId, traceCost, traceCost);
				factor = alternateFactor(factor);
				//>>System.out.println(t.size());
				
				//System.out.println((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
				memoryList.add(calculateMemoryUsage());
				if (memoryList.size()%1000==0) {
					System.out.println(memoryList.stream().mapToDouble(Double::doubleValue).average().getAsDouble() + " ," + calculateCosts(costPerTrace));
					memoryList.clear();
				}
			}		
			//for removing vanished cases
			Iterator<XTrace> itr = log.iterator();
			while(itr.hasNext()){
				if(itr.next().size()==0) {
					itr.remove();
				}
			}
			
			//>>System.out.println(log.size());
			/*for(int i = 0; i<log.size()(log.size()>=25?25:log.size());i++ ) {
				String caseId = XConceptExtension.instance().extractName(t);
				List<String> traceStrLst = toStringList(t, lms.eventClasses);
				
			}*/
		}
		//double totalCost = calculateCosts(costPerTrace);
		/*for (String t : costPerTrace.keySet()) {
			//totalCost += count.get(t) * costPerTrace.get(t);
			totalCost += costPerTrace.get(t);
		}*/
		System.out.println("After Finishing: " + calculateCosts(costPerTrace));
		/*for (XTrace t : log) {
			String caseId = XConceptExtension.instance().extractName(t);
			List<String> traceStrLst = toStringList(t, lms.eventClasses);
			//String traceStr = StringUtils.join(traceStrLst, ",");
			
			for (String e : traceStrLst) {
				tracker.replayEvent(caseId, e); 
			}
			
			
			//trimTrace(e,t);
			System.out.println((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
			//memoryUsage(e,t);
		}*/
		//System.out.println((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
		LocalOnlineConformanceConfiguration locc = new LocalOnlineConformanceConfiguration();
		return locc;
	}
	private double calculateMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		//>>double memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
		//>>System.out.println(memoryUsed + ", ");
		return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
	}
	private double calculateCosts(TObjectDoubleMap<String> costPerTrace) {
		int totalCost = 0;
		for (String t : costPerTrace.keySet()) {
			//totalCost += count.get(t) * costPerTrace.get(t);
			totalCost += costPerTrace.get(t);
		}
		return totalCost;
	}
	public int alternateFactor(int factor) {
		/*if(factor<5) {
			factor += 2;
		}else {
			factor=1;
		}
		return factor;*/
		if(factor<24) {
			factor += 6;
		}else {
			factor=6;
		}
		return factor;
	}
	private List<String> toStringList(XTrace trace, XEventClasses classes) {
		List<String> l = new ArrayList<>(trace.size());
		for (int i = 0; i < trace.size(); i++) {
			l.add(i, classes.getByIdentity(XConceptExtension.instance().extractName(trace.get(i))).toString());
		}
		return l;
	}
	/*public XTrace createTrace(XTrace te, String ev ) {
		XTrace t = xesFactory.createTrace();
		XAttributeMap atts = xesFactory.createAttributeMap();
		
		atts.put("concept:name",xesFactory.createAttributeLiteral("concept:name", XConceptExtension.instance().extractName(te) , null));
		t.setAttributes(atts);
		t.add(getBasicXEvent(ev));
		
		
	}
	public XEvent getBasicXEvent() {
		XAttributeMap atts = PlgProcess.xesFactory.createAttributeMap();
		atts.put("concept:name",
			PlgProcess.xesFactory.createAttributeLiteral(
					"concept:name",
					getName(),
					PlgProcess.xesExtensionManager.getByName("Concept")));
		return PlgProcess.xesFactory.createEvent(atts);
	}*/
}
