package org.processmining.prefiximputation.tests;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClasses;
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
import org.processmining.prefiximputation.modelbased.models.LocalConformanceTracker;
//import org.processmining.streamconformance.local.model.LocalModelStructure;
import org.processmining.prefiximputation.modelbased.models.LocalModelStructure;
import org.processmining.prefiximputation.modelbased.plugins.LocalOnlineConformanceConfiguration;
import org.processmining.processtree.ProcessTree;

@Plugin(
	name = "Online Conformance - With Model-Based Prefix Imputation1",
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
		
		LocalModelStructure lms = new LocalModelStructure(context, net, initMarking, tree, "Prefix Alignment", 0); 				
		LocalConformanceTracker tracker = new LocalConformanceTracker(lms, 100, "Prefix Alignment", 0 );
				
		for (XTrace t : log) {
			String caseId = XConceptExtension.instance().extractName(t);
			List<String> traceStrLst = toStringList(t, lms.eventClasses);
			//String traceStr = StringUtils.join(traceStrLst, ",");
			for (String e : traceStrLst) {
				tracker.replayEvent(caseId, e); 
			}
			
			
			//trimTrace(e,t);
			System.out.println((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
			//memoryUsage(e,t);
		}
		//System.out.println((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
		LocalOnlineConformanceConfiguration locc = new LocalOnlineConformanceConfiguration();
		return locc;
	}
	public XTrace trimTrace(XTrace te, String ev ) {
		XTrace t = xesFactory.createTrace();
		
		for(int i = 0; i<te.size(); i++ ) {
			
		}
		return te;
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
