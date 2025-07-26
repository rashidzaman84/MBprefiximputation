package org.processmining.prefiximputation.modelbased.thesisexperiments.plugins;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.javatuples.Triplet;
import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.io.PNMLSerializer;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.csv.CSVFile;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.onlineconformance.models.PartialAlignment;
import org.processmining.onlineconformance.models.PartialAlignment.State;
import org.processmining.prefiximputation.inventory.NullConfiguration;
import org.processmining.prefiximputation.modelbased.completeforgetting.LocalConformanceTracker_APTE;
import org.processmining.prefiximputation.modelbased.completeforgetting.LocalModelStructure_APTE;
import org.processmining.prefiximputation.tests.ParallelCasesBasedLogToStreamConverter;
import org.processmining.prefiximputation.tests.TimeStampsBasedLogToStreamConverter;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ptml.Ptml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import gnu.trove.map.TObjectDoubleMap;

@Plugin(
	name = "Online Conformance - With Model-Based Prefix Imputation - THESIS APTE",
	returnLabels = { "Online conformance checking - with Model-based Prefix Imputation" },
	returnTypes = { /*LocalOnlineConformanceConfiguration.class*/ NullConfiguration.class },
	parameterLabels = {
			"Model", "Event Data", "Model Stream Mapping"/*, "Marking", "Process Tree", "Event Data"*/
	},
	categories = PluginCategory.Analytics,
	help = "This plugin computes the conformance of a given model with respect to an event streams.",
	userAccessible = true)
public class PrefixImputationBasedPrefixAlignment_Reallife_APTE {
	//public static XFactory xesFactory = new XFactoryBufferedImpl();
	protected Runtime runtime = Runtime.getRuntime();
	
	
	@PluginVariant(variantLabel = "Online Conformance - With Model-Based Prefix Imputation - S3 APTE",requiredParameterLabels = { 0, 1/*, 1, 2, 3 */})
	@UITopiaVariant(
		author = "R. Zaman",
		email = "r.zaman@tue.nl",
		affiliation = "TUe")
	
	public /*LocalOnlineConformanceConfiguration*/ NullConfiguration plugin(UIPluginContext context, Petrinet net, XLog log/*, Marking initMarking, ProcessTree tree, XLog log*/) throws Exception {
		Marking initialMarking = getInitialMarking(net);
		LocalModelStructure_APTE lms = new LocalModelStructure_APTE(context, net, /*initMarking*/initialMarking, /*tree2,*/ "Prefix Alignment", 0); 				
		
		String logName = "BPIC12";
		int[] maxCasesToStoreChoices = {50,100,200,300,400,500,1000};
		String outputFolderPath = "D:/Research Work/latest/Streams/Rashid Prefix Alignment/Information Systems/Results/PI/With End marker/ATPE/";
		LinkedHashMap<String, ResultsCollection2> globalResults = new LinkedHashMap<>(); //contains the results for all n values
		
		for(int j=0; j<maxCasesToStoreChoices.length; j++) {      //n size

						
			System.out.println("\t APTE State Size: infinite, Max. Cases: " + maxCasesToStoreChoices[j]);

			ResultsCollection2 resultsCollection2 = process(lms, log, maxCasesToStoreChoices[j]);

			//here we record the results for each "n" value
			globalResults.put("(" + Integer.MAX_VALUE + "/" + maxCasesToStoreChoices[j] + ")", resultsCollection2);
		}			
		//PublishResults.writeToFilesCC(globalResults, logName, "", outputFolderPath);
		//PublishResults.writeToFilesCC(globalResults, logName, "", outputFolderPath);
		PublishResults.writeToFilesATPE(globalResults, logName, "", outputFolderPath);	
		
		//process(lms, log);
		return new NullConfiguration();
	}
	
	@PluginVariant(variantLabel = "Online Conformance - With Model-Based Prefix Imputation - S3 APTE",requiredParameterLabels = { 0, 1, 2})
	@UITopiaVariant(
		author = "R. Zaman",
		email = "r.zaman@tue.nl",
		affiliation = "TUe")
	
	public /*LocalOnlineConformanceConfiguration*/ NullConfiguration plugin(UIPluginContext context, Petrinet net, XLog log, CSVFile modelStreamMapping/*, Marking initMarking, ProcessTree tree, XLog log*/) throws Exception {
		Marking initialMarking = getInitialMarking(net);
		LocalModelStructure_APTE lms = new LocalModelStructure_APTE(context, net, /*initMarking*/initialMarking, /*tree2,*/ "Prefix Alignment", 0, modelStreamMapping); 	
		
		String logName = "BPIC12";
		int[] maxCasesToStoreChoices = {50,100,200,300,400,500,1000};
		String outputFolderPath = "D:/Research Work/latest/Streams/Rashid Prefix Alignment/Information Systems/Results/PI/With End marker/ATPE/";
		LinkedHashMap<String, ResultsCollection2> globalResults = new LinkedHashMap<>(); //contains the results for all n values
		
		for(int j=0; j<maxCasesToStoreChoices.length; j++) {      //n size

						
			System.out.println("\t State Size: infinite, Max. Cases: " + maxCasesToStoreChoices[j]);

			ResultsCollection2 resultsCollection2 = process(lms, log, maxCasesToStoreChoices[j]);

			//here we record the results for each "n" value
			globalResults.put("(" + Integer.MAX_VALUE + "/" + maxCasesToStoreChoices[j] + ")", resultsCollection2);
		}			
		//PublishResults.writeToFilesCC(globalResults, logName, "", outputFolderPath);
		//PublishResults.writeToFilesCC(globalResults, logName, "", outputFolderPath);
		PublishResults.writeToFilesATPE(globalResults, logName, "", outputFolderPath);	
		
		return new NullConfiguration();
	}
	
	@PluginVariant(variantLabel = "Online Conformance - With Model-Based Prefix Imputation - S3 APTE",requiredParameterLabels = {0})
	@UITopiaVariant(
		author = "R. Zaman",
		email = "r.zaman@tue.nl",
		affiliation = "TUe")
	
	public /*LocalOnlineConformanceConfiguration*/ NullConfiguration plugin(UIPluginContext context, Petrinet net) throws Exception {
		Marking initialMarking = getInitialMarking(net);
			
		
		
		String[] logTypes = {"a12", "a22", "a32"};
		int[] maxCasesToStoreChoices = {5,10,15,20,25,50};
		String logType = logTypes[2];
		
		String eventLogInputFolderPath = "D:/Research Work/latest/Streams/Rashid Prefix Alignment/Process Models from Eric/Event Logs Repository/" + logType + "/timed logs/";
		
		String shortestPrefixesInputFolderPath = "D:/Research Work/latest/Streams/Rashid Prefix Alignment/Process Models from Eric/Process Models Repository/" + logType + " shortest prefixes_.txt";
		
		//String shortestPrefixesInputFolderPath = "D:/Research Work/latest/Streams/Rashid Prefix Alignment/Process Models from Eric/Process Models Repository/test/exPM prefixes.txt";
		
		//String outputFolderPath = "D:/Research Work/latest/Streams/Rashid Prefix Alignment/Thesis/Prefix Imputation/Results/N/With End marker/";
		
		String outputFolderPath = "D:/Research Work/latest/Streams/Rashid Prefix Alignment/Information Systems/Results/PI/With End marker/";
		
		LocalModelStructure_APTE lms = new LocalModelStructure_APTE(context, net, /*initMarking*/initialMarking, /*tree2,*/ "Prefix Alignment", 0, shortestPrefixesInputFolderPath); 
		
		
		File inputFolder = new File(eventLogInputFolderPath);
		
		for (File file : inputFolder.listFiles()) { 
			System.out.println(file.getName());

			String fileName = FilenameUtils.getBaseName(file.getName());
			String fileExtension = FilenameUtils.getExtension(file.getName());
			
			XLog log = null; 

			if(!fileExtension.equals("xes") || fileName.endsWith("25_50") || fileName.endsWith("50_2") || fileName.endsWith("50_5") || fileName.contains("f1")) {
				System.out.println("error!! not an xes file or a wanted file");
				continue;
			}
			
			

			try {
				log = new XUniversalParser().parse(file).iterator().next();
			} catch (Exception e) {
				e.printStackTrace();
			}	
			
			LinkedHashMap<String, ResultsCollection2> globalResults = new LinkedHashMap<>(); //contains the results for all n values
			
			for(int j=0; j<maxCasesToStoreChoices.length; j++) {      //n size

				System.out.println("\t State Size: infinite, Max. Cases: " + maxCasesToStoreChoices[j]);

				ResultsCollection2 resultsCollection2 = process(lms, log, maxCasesToStoreChoices[j]);

				//here we record the results for each "n" value
				globalResults.put("(" + Integer.MAX_VALUE + "/" + maxCasesToStoreChoices[j] + ")", resultsCollection2);
			}
			
			//System.out.println("test");
			PublishResults.writeToFilesCC(globalResults, fileName, "", outputFolderPath );				
		}
		
		
		return new NullConfiguration();
	}
		public ResultsCollection2 process(LocalModelStructure_APTE lms, XLog log, int maxCasesToStore) throws Exception {
		/*LocalModelStructure lms = new LocalModelStructure(context, net, initMarking, tree, "Prefix Alignment", 0); 				
		LocalConformanceTracker tracker = new LocalConformanceTracker(lms, 100);*/
		///--------------
		//String petrinetFile = /*"D:\\TEST\\simplest.pnml";*/ "D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Scenario 1\\CCC19 - Model PN_modified.pnml";
		//String petrinetFile = "D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Process Models BPI 2012 from Boudewijn\\Model_O.pnml";		
		//String logFile = /*"D:\\TEST\\cpnToolsSimulationLog.mxml";*/ "D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Scenario 1\\CPN Model\\consolidated\\cpnToolsSimulationLog.mxml";
		//String logFile ="D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\Process Models BPI 2012 from Boudewijn\\Only_O_Events.xes";
		
		//String petrinetFile = NullConfiguration.petriNetFilePath;
		//String logFile = NullConfiguration.eventLogFilePath;
		
		//Petrinet net = constructNet(petrinetFile);
		//Marking initialMarking = getInitialMarking(net);
		//Marking finalMarking = getFinalMarking(net);
		//XLog log;
		//XEventClassifier eventClassifier;
		//log = new XUniversalParser().parse(new File(logFile)).iterator().next();
		/////////---------------------------------
		/*File file = new File(NullConfiguration.processTreeFilePath);
		//FileInputStream(file)
		//PluginContext context = null;
		Ptml ptml = importPtmlFromStream(context, inputnew FileInputStream(file), 
				NullConfiguration.processTreeFilePath, file.length());*/
		/*if (ptml == null) {
			
			 * No PTML found in file. Fail.
			 
			JOptionPane.showMessageDialog(null, "No PTML-formatted process tree was found in file \"" + "D:\\Research Work\\latest\\Streams\\Rashid\\processtree.ptml" + "\".");
			return null;
		}*/
		/*
		 * PTML file has been imported. Now we need to convert the contents to a
		 * regular Process tree.
		 */
		/*ProcessTree tree2 = new ProcessTreeImpl(ptml.getId(), ptml.getName());
		System.out.println(tree2);

		tree2 = connectTree(context, ptml, tree2);*/
		//-----------------
		//int maxCasesToStore = 100;

		
		//LocalModelStructure lms = new LocalModelStructure(context, net, /*initMarking*/initialMarking, /*tree2,*/ "Prefix Alignment", 0); 				
		LocalConformanceTracker_APTE tracker = new LocalConformanceTracker_APTE(lms, /*maxCasesToStore*/maxCasesToStore);
		////////////////////-------------
		ArrayList<Triplet<String,String,Date>>	eventLogSortedByDate = new ArrayList<Triplet<String,String,Date>>();
		
		if(NullConfiguration.logToStream.equals("timestamps")) {
			eventLogSortedByDate = TimeStampsBasedLogToStreamConverter.sortEventLogByDate(log);
		}else if (NullConfiguration.logToStream.equals("parallelRunningCases")) {
			eventLogSortedByDate = ParallelCasesBasedLogToStreamConverter.logToStream(log, NullConfiguration.maxParallelRunningCases);
		}		
		//System.out.println(eventLogSortedByDate);
		
		//LinkedHashMap<Pair<String,String>, Date> eventLogSortedByDate = sortEventLogByDate(log);	//Date-sorted Stream		
//		int logSize = eventLogSortedByDate.size();
//		Date startDateCurrentWindow = getWindowTimeStamp(eventLogSortedByDate, "start");
//		Date endDateOfTheLastWindow = getWindowTimeStamp(eventLogSortedByDate, "last");
		
		//Global statistics
//		Set<String> beforeTideCases = new HashSet<String>();
//        Set<String> duringTideCases = new HashSet<String>();
//        Set<String> afterTideCases = new HashSet<String>();        
////        Set<String> nonConformantCasesCummulative = new  HashSet<String>();        
////        List<Double> costPerTraceCummulative = new ArrayList<Double>();
//        HashMap<String, ArrayList<PartialAlignment<String, Transition, Marking>>> partialAlignmentRecord =new HashMap<String , ArrayList<PartialAlignment<String, Transition, Marking>>>();
//        
//        Map<String, ArrayList<PartialAlignment>> alignmentsLife = new HashMap<String, ArrayList<PartialAlignment>>();
//        Map<String, ArrayList<Double>> alignmentsLifeScore = new HashMap<String, ArrayList<Double>>();
//        
//        Map<String, Double> eventualCosts = new HashMap<>();
//        ArrayList<Integer> stateRecords = new ArrayList<>();
//        HashMap<String, PartialAlignment<String, Transition, Marking>> eventualAlignments =new HashMap<String , PartialAlignment<String, Transition, Marking>>();
//        
//      //Window-specific statistics
//        Set<String> casesObservedCurrentWindow = new  HashSet<String>();
//        Set<String> nonConformantCasesCurrentWindow = new  HashSet<String>();		
//		final TObjectDoubleMap<String> costPerTraceCurrentWindow = new TObjectDoubleHashMap<>();
//		//TObjectDoubleMap<String> costPerTraceWindowCummulative = new TObjectDoubleHashMap<>();
//		
//		int maxCasesInMemoryCurrentWindow =0;
//		int eventsObservedCurrentWindow = 0;	
		
		final int runs = 50;
		Map<Integer, Double> elapsedTime = new HashMap<>();
		
		for(int i=0; i<=runs; i++) {      //i<runs+1 because we need to discard the first run.
			

			//System.out.println("\nRun No. " + (i+1));
			//System.out.println("Window, Time Elapsed in Millis, Observed Events,Avg. Time per Event");
			
//			int eventsObservedTotal = 0;
//			String caseId;
//		    String event;
		    //Date eventTimeStamp;
//		    Boolean tideStarted = false;
//	        Boolean tidePassed = null;
	        //double traceCost = 0.0;
//	        double completeLogCost = 0.0;
	        int observedEvents = 0;
	        
	        ArrayList<Triplet<String,String,Date>>	eventLogSortedByDateCopy = new ArrayList<>();	
	        
	        for (Triplet<String,String, Date> entry : eventLogSortedByDate) {  //creates a clone of the event log with distinct case ids to stress memo
				eventLogSortedByDateCopy.add(new Triplet<String, String, Date>(entry.getValue0()+i, entry.getValue1(), entry.getValue2()));
			}
			
			System.gc();
			Instant start = Instant.now();
			
			for (Triplet<String,String, Date> entry : eventLogSortedByDateCopy) {
				/*caseId = entry.getKey().getFirst();
				event = entry.getKey().getSecond();
				eventTimeStamp = entry.getValue();*/
//	        	caseId = entry.getValue0();
//				event = entry.getValue1();
				//eventTimeStamp = entry.getValue2();
				//System.out.println(entry.getValue0() + "," + entry.getValue1() );

				tracker.replayEvent(entry.getValue0(), entry.getValue1());
				//PartialAlignment partialAlignmentForCurrentEvent = tracker.get(caseId).OCC2.replayer.getDataStore().get(caseId);
				observedEvents++;
				
			}
			
			Instant end = Instant.now(); 
			Duration timeElapsed = Duration.between(start, end);
			elapsedTime.put(i, ((double)timeElapsed.toMillis()/(double)observedEvents));
			//System.out.println(elapsedTime.get(i));

			
		}
		
		double sumATPE = 0;
		for(int j=1; j<elapsedTime.size(); j++) {  //we discard the first run.
			sumATPE += elapsedTime.get(j);
		}
		
		
	
		
//		System.out.println("CW No. of max. Traces, CW No. of all cases observed, CW No. of conformant traces, CW No. of non-conformant traces, CW No. of pre-tide traces, CW No. of in-tide traces,"
//        		+ " CW No. of post-tide traces, CW No. of observed events, CW No. of conformant events, CW No. of non-conformant events");
//		
		
		
		ResultsCollection2 resultsCollection2 = new ResultsCollection2();
			
		resultsCollection2.ATPE = sumATPE/(elapsedTime.size()-1);
		resultsCollection2.caseLimitSize = maxCasesToStore;

		return resultsCollection2;
		

	}
	

	
	/*private static ArrayList<Triplet<String,String,Date>> sortEventLogByDate2(XLog log){
		int index = 0;
		Map<Integer, Triplet<String,String,Date>> eventsStream = new HashMap<Integer, Triplet<String,String,Date>>();
		for (XTrace t : log) {
			for(XEvent e: t) {
				String caseId = XConceptExtension.instance().extractName(t);
				String newEventName = XConceptExtension.instance().extractName(e);
				//Pair<String,String> eventPacket = new Pair<String, String>(caseId, newEventName);
				Date date = XTimeExtension.instance().extractTimestamp(e);
				Triplet<String,String,Date> eventPacket = new Triplet<String,String,Date>(caseId, newEventName, date);				
				eventsStream.put(index, eventPacket);
				index++;
			}
		}
		//need to sort the hashmap on date
		Comparator<Entry<Integer, Triplet<String,String,Date>>> valueComparator = new Comparator<Entry<Integer, Triplet<String,String,Date>>>() { 
			@Override public int compare(Entry<Integer, Triplet<String,String,Date>> e1, Entry<Integer, Triplet<String,String,Date>> e2) { 
				Date v1 = e1.getValue().getValue2(); 
				Date v2 = e2.getValue().getValue2(); 
				return v1.compareTo(v2);
				}
			};
		ArrayList<Entry<Integer, Triplet<String,String,Date>>> entries = new ArrayList<Entry<Integer, Triplet<String,String,Date>>>();
		entries.addAll(eventsStream.entrySet());	
		List<Entry<Integer, Triplet<String,String,Date>>> listOfEntries = new ArrayList<Entry<Integer, Triplet<String,String,Date>>>(entries);
		Collections.sort(listOfEntries, valueComparator);
	    ArrayList<Triplet<String,String,Date>> sortedByValue = new ArrayList<Triplet<String,String,Date>>(listOfEntries.size());
	    //System.out.println(sortedByValue.size());
	    for(Entry<Integer, Triplet<String,String,Date>> entry : listOfEntries){
	    	sortedByValue.add(entry.getValue());
	    	}
	    if(NullConfiguration.displayFineStats) {
	    	printTripletList(sortedByValue);
	    }
		return sortedByValue;
	}*/
			
		//------------------------------------------
		/*int factor = 1;
		List<Double> memoryList = new ArrayList<Double>();
		final TObjectDoubleMap<String> costPerTrace = new TObjectDoubleHashMap<>();
		System.out.println("Before Start: " + calculateMemoryUsage()); //the memory usage before the start of the CC checking
		while (!log.isEmpty()) {
			int x=0;
			int numberOfCases = 25;
			int numberOfEvents = 6;
			//int innerLoop = 0;
			for (XTrace t : log) {
				
				//innerLoop = (t.size()>=numberOfEvents?numberOfEvents:t.size());
				
				String caseId = XConceptExtension.instance().extractName(t);
				List<String> traceStrLst = toStringList(t, lms.eventClasses);
				double traceCost=0.0;
				if(x>=numberOfCases) {			
					numberOfEvents = alternateFactor(numberOfEvents);
					x=0;					
				}
					
				for(int y=0; y<(traceStrLst.size()>numberOfEvents? numberOfEvents:traceStrLst.size()); y++) {
					traceCost += tracker.replayEvent(caseId, traceStrLst.get(y)).getConformance();
					t.remove(0);
				}				
					
				costPerTrace.adjustOrPutValue(caseId, traceCost, traceCost);
				
				memoryList.add(calculateMemoryUsage());
				if (memoryList.size()%1000==0) {
					System.out.println(memoryList.stream().mapToDouble(Double::doubleValue).average().getAsDouble() + " ," + calculateCosts(costPerTrace));
					memoryList.clear();
				}
					x++;
				}
				//factor = alternateFactor(factor);
				/////////////////////////////////////////////////////////////
					
			//for removing vanished cases
			Iterator<XTrace> itr = log.iterator();
			while(itr.hasNext()){
				if(itr.next().size()==0) {
					itr.remove();
				}
			}*/
			
			//>>System.out.println(log.size());
			/*for(int i = 0; i<log.size()(log.size()>=25?25:log.size());i++ ) {
				String caseId = XConceptExtension.instance().extractName(t);
				List<String> traceStrLst = toStringList(t, lms.eventClasses);
				
			}*/
		//}
		/////////////////////////////////////////////////////////////////////////////////////////
		//double totalCost = calculateCosts(costPerTrace);
		/*for (String t : costPerTrace.keySet()) {
			//totalCost += count.get(t) * costPerTrace.get(t);
			totalCost += costPerTrace.get(t);
		}*/
		//System.out.println("After Finishing: " + calculateCosts(costPerTrace));
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
		/*LocalOnlineConformanceConfiguration locc = new LocalOnlineConformanceConfiguration();
		//return locc;
		return locc;
	}*/
	
	/*private static void printTripletList(ArrayList<Triplet<String,String,Date>> tripletList ) {
		for(Triplet entry: tripletList) {
			 System.out.println(entry.getValue0() + ", " +  entry.getValue1() + ", " +  entry.getValue2());
			 //System.out.println(entry.getValue(0) + ", " +  entry.getValue(1) + ", " +  entry.getValue(2));
		}
	}*/
	private static ProcessTree connectTree(PluginContext context, Ptml ptml, ProcessTree tree) {
		ptml.unmarshall(tree);
//		System.out.println("Tree contains " + tree.getNodes().size() + " nodes and " + tree.getEdges().size() + "  edges.");
		return tree;
	}
	public static Ptml importPtmlFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		/*
		 * Get an XML pull parser.
		 */
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		/*
		 * Initialize the parser on the provided input.
		 */
		xpp.setInput(input, null);
		/*
		 * Get the first event type.
		 */
		int eventType = xpp.getEventType();
		/*
		 * Create a fresh PNML object.
		 */
		Ptml ptml = new Ptml();

		/*
		 * Skip whatever we find until we've found a start tag.
		 */
		while (eventType != XmlPullParser.START_TAG) {
			eventType = xpp.next();
		}
		/*
		 * Check whether start tag corresponds to PNML start tag.
		 */
		if (xpp.getName().equals(Ptml.TAG)) {
			/*
			 * Yes it does. Import the PNML element.
			 */
			ptml.importElement(context, xpp, ptml);
		} else {
			/*
			 * No it does not. Return null to signal failure.
			 */
			ptml.log(Ptml.TAG, xpp.getLineNumber(), "Expected ptml");
		}
		
		ptml.log("Acyclicity check");
		ptml.checkAcyclicity();
		
		if (ptml.hasErrors()) {
			context.getProvidedObjectManager().createProvidedObject("Log of PTML import", ptml.getLog(), XLog.class,
					context);
			return null;
		}
		return ptml;
	}
	
	private static double calculateMemoryUsage() {
		Runtime runtime = Runtime.getRuntime();
		//>>double memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
		//>>System.out.println(memoryUsed + ", ");
		return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
	}
	
	private static double calculateCosts(TObjectDoubleMap<String> costPerTrace) {
		int totalCost = 0;
		for (String t : costPerTrace.keySet()) {
			//totalCost += count.get(t) * costPerTrace.get(t);
			totalCost += costPerTrace.get(t);
		}
		return totalCost;
	}
	
	public static int alternateFactor(int factor) {
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
	
	private static List<String> toStringList(XTrace trace, XEventClasses classes) {
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
			}else {
				t2t.put(t, tt);
			}
			
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

	private static Marking getFinalMarking(PetrinetGraph net) {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		return finalMarking;
	}

	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}
	
	/*private static LinkedHashMap<Pair<String,String>, Date> sortEventLogByDate(XLog log){
		Map<Pair<String,String>, Date> eventsStream = new HashMap<Pair<String,String>, Date>();
		for (XTrace t : log) {
			for(XEvent e: t) {
				String caseId = XConceptExtension.instance().extractName(t);
				String newEventName = XConceptExtension.instance().extractName(e);
				Pair<String,String> eventPacket = new Pair<String, String>(caseId, newEventName);
				Date date = XTimeExtension.instance().extractTimestamp(e);
				eventsStream.put(eventPacket, date);
			}
		}
	//need to sort the hashmap on date
	Comparator<Entry<Pair<String,String>, Date>> valueComparator = new Comparator<Entry<Pair<String,String>, Date>>() { 
		@Override public int compare(Entry<Pair<String,String>, Date> e1, Entry<Pair<String,String>, Date> e2) { 
			Date v1 = e1.getValue(); 
			Date v2 = e2.getValue(); 
			return v1.compareTo(v2);
			}
		};
	Set<Entry<Pair<String,String>, Date>> entries = eventsStream.entrySet();	
	List<Entry<Pair<String,String>, Date>> listOfEntries = new ArrayList<Entry<Pair<String,String>, Date>>(entries);
	Collections.sort(listOfEntries, valueComparator);
    LinkedHashMap<Pair<String,String>, Date> sortedByValue = new LinkedHashMap<Pair<String,String>,Date>(listOfEntries.size());
    //System.out.println(sortedByValue.size());
    for(Entry<Pair<String,String>, Date> entry : listOfEntries){
    	sortedByValue.put(entry.getKey(), entry.getValue());
    	}
    printLinkedHashMap(sortedByValue);
	return sortedByValue;
	}*/
	
	private static Date getWindowTimeStamp(ArrayList<Triplet<String,String,Date>> sortedByValue, String choice) {
		//LinkedList<Pair<String,String>> listKeys = new LinkedList<Pair<String,String>>(sortedByValue.keySet());
				if(choice.equals("start")) {
					//return sortedByValue.get( listKeys.getFirst());
					return sortedByValue.get(0).getValue2();
				}else {
					//return sortedByValue.get( listKeys.getLast());
					return sortedByValue.get(sortedByValue.size()-1).getValue2();
				}		
	}
	
	private static int calculateDiffWindowRelatedCases(Set<String> parentCasesSet, Set<String> childCasesSet){
		int score = 0;
		for (String item: childCasesSet) {
			if(parentCasesSet.contains(item)) {
				score++;
			}
        }
		return score;
	}
	
	private static double calculateCurrentCosts(TObjectDoubleMap<String> costPerTrace) {
		double totalCost = 0;
		for (String t : costPerTrace.keySet()) {
			//totalCost += count.get(t) * costPerTrace.get(t);
			totalCost += costPerTrace.get(t);
		}
		return totalCost;
	}
	public double sumArrayList(ArrayList<Double> arrayList) {
		Double sum = 0.0;
		for(int i = 0; i < arrayList.size(); i++)
	    {
	        sum += arrayList.get(i);
	    }
	    return sum;		
	}
	
	/*private static void printLinkedHashMap(LinkedHashMap<Pair<String,String>,Date> sortedByValue  ){
		for (Map.Entry<Pair<String,String>,Date> entry : sortedByValue.entrySet()) {		    
		   System.out.println(entry.getKey() + ", " +  entry.getValue());
		}
	}*/
	
	public static <A extends PartialAlignment, S, L, T> int getNumberOfStates(final A previousAlignment) {
		/*if (previousAlignment == null || previousAlignment.size() < getParameters().getLookBackWindow()) {
			return getInitialState();
		} else {*/
		//System.out.println(previousAlignment.size());
		if (previousAlignment != null) {
			State<S, L, T> state = previousAlignment.getState();
			int i = 1;
			while (state.getParentState() != null /*&& state.getParentState().getParentMove() !=null*/ ) {
				/*if(state.getParentState() instanceof LightWeight) {
					break;
				}*/
				state = state.getParentState();
				i++;
			}
			return i;
		}
		//System.out.println("The prefix-alignment is NULL");
		return 0;
		//}
	}
	
}
