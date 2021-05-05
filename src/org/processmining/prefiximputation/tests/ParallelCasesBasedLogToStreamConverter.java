package org.processmining.prefiximputation.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.javatuples.Triplet;
import org.processmining.prefiximputation.inventory.NullConfiguration;

public class ParallelCasesBasedLogToStreamConverter {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String logFile = "D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\"
				+ "Scenario 4\\a22f0n05.xes";
		
		XLog log = null;
		XEventClassifier eventClassifier;
		try {
			log = new XUniversalParser().parse(new File(logFile)).iterator().next();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finePrint(logToStream(log,5));


	}

	public static ArrayList<Triplet<String,String,Date>> logToStream(XLog log, int parallelCases){

		ArrayList<Triplet<String,String,Date>> eventsStream = new ArrayList<Triplet<String,String,Date>>();
		LinkedHashMap<Integer, Integer> eventSize  = new LinkedHashMap();
		LinkedHashMap<Integer, Integer> eventCounter  = new LinkedHashMap();
		for(int i=0; i<log.size(); i++) {
			String caseId = XConceptExtension.instance().extractName(log.get(i));
			eventSize.put(i, log.get(i).size());
			eventCounter.put(i, 0);
		}
		//System.out.println(eventSize);
		//System.out.println(eventCounter);
		while(!eventSize.isEmpty()) {
			int jValue = eventSize.size()>=parallelCases?parallelCases:eventSize.size();
			for(int j=0; j<jValue; j++) { 
				//String newEventName = XConceptExtension.instance().extractName();
				int value = (int) eventSize.values().toArray()[j];
				int indexInLog = (int) eventSize.keySet().toArray()[j];
				int value2 = (int) eventCounter.values().toArray()[j];
				
				//System.out.println(indexInLog + "," +  value + "," + value2);
				//log.get(log.indexOf(eventSize.keySet().toArray()[j])).get((int)eventCounter.values().toArray()[j]);
				//System.out.println(XConceptExtension.instance().extractName(log.get(indexInLog).get(value2)));
				String newEventName = XConceptExtension.instance().extractName(log.get(indexInLog).get(value2)); 
				//caseid is in hashmap
				Date date = XTimeExtension.instance().extractTimestamp(log.get(indexInLog).get(value2));
				if(date == null) {
					//System.out.println("Date is null");
					Calendar calendar = Calendar.getInstance();
					date = calendar.getTime();
				}
				//System.out.println(date);
				String caseId = XConceptExtension.instance().extractName(log.get(indexInLog));
				Triplet<String,String,Date> eventPacket = new Triplet<String,String,Date>(caseId, newEventName, date);
				eventsStream.add(eventPacket);
				
				eventSize.put(indexInLog, --value);
				eventCounter.put(indexInLog, ++value2);
				
				/*if(value==0) {
					eventSize.remove(indexInLog);
					eventCounter.remove(indexInLog);
				}*/
				
				/*Iterator<Map.Entry<Integer, Integer> >
	            iterator = eventSize.entrySet().iterator();
				
				
				while (iterator.hasNext()) {
					Map.Entry<Integer, Integer> entry = iterator.next();
					if (entry.getValue()==0) {
						  
		                // Remove this entry from HashMap
		                iterator.remove();
		            }
				}*/
				
				//now we need to check for 0 size of eventsize and drop entry in both maps if so
			}
			Iterator<Map.Entry<Integer, Integer> >
            iterator = eventSize.entrySet().iterator();
			
			
			while (iterator.hasNext()) {
				Map.Entry<Integer, Integer> entry = iterator.next();
				if (entry.getValue()==0) {
					  
	                // Remove this entry from HashMap
					eventCounter.remove(entry.getKey());
					
	                iterator.remove();
	            }
			}
			
			/*for(int k=0; k<jValue; k++) {
				int value = (int) eventSize.values().toArray()[k];
				if(value==0) {
					int indexInLog = (int) eventSize.keySet().toArray()[k];
					eventSize.remove(indexInLog);
					eventCounter.remove(indexInLog);
				}
			}*/
			
			//System.out.println("--------------------------------");
		}
		
		
		
		/*String caseId = XConceptExtension.instance().extractName(t);
		String newEventName = XConceptExtension.instance().extractName(e);
		//Pair<String,String> eventPacket = new Pair<String, String>(caseId, newEventName);
		Date date = XTimeExtension.instance().extractTimestamp(e);
		Triplet<String,String,Date> eventPacket = new Triplet<String,String,Date>(caseId, newEventName, date);				
		eventsStream.add(eventPacket);*/
		
		
		/*while (!log.isEmpty()) {
			for (XTrace t : log) {
				int numberOfCases = 25;
				int numberOfEvents = 6;
				int innerLoop = (t.size()>=numberOfEvents?numberOfEvents:t.size());
				for(int x=0; x<numberOfCases; x++) {					
					String caseId = XConceptExtension.instance().extractName(t);
					List<String> traceStrLst = toStringList(t, lms.eventClasses);
					double traceCost=0.0;
					for(int y=0; y<innerLoop; y++) {
						traceCost += tracker.replayEvent(caseId, traceStrLst.get(y)).getConformance();
						t.remove(0);
					}
					costPerTrace.adjustOrPutValue(caseId, traceCost, traceCost);

				}
				factor = alternateFactor(factor);
				String caseId = XConceptExtension.instance().extractName(t);
				List<String> traceStrLst = toStringList(t, lms.eventClasses);
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
		}*/
		return eventsStream;
	}
	
	public static ArrayList<Triplet<String,String,Date>> sortEventLogByDate2(XLog log){
		int index = 0;
		List<Triplet<String,String,Date>> eventsStream = new ArrayList<Triplet<String,String,Date>>();
		for (XTrace t : log) {
			for(XEvent e: t) {
				String caseId = XConceptExtension.instance().extractName(t);
				String newEventName = XConceptExtension.instance().extractName(e);
				//Pair<String,String> eventPacket = new Pair<String, String>(caseId, newEventName);
				Date date = XTimeExtension.instance().extractTimestamp(e);
				Triplet<String,String,Date> eventPacket = new Triplet<String,String,Date>(caseId, newEventName, date);				
				eventsStream.add(eventPacket);
				index++;
			}
		}
		//need to sort the hashmap on date
		Comparator<Triplet<String,String,Date>> valueComparator = new Comparator<Triplet<String,String,Date>>() { 
			@Override public int compare(Triplet<String,String,Date> e1, Triplet<String,String,Date> e2) { 
				Date v1 = e1.getValue2(); 
				Date v2 = e2.getValue2(); 
				return v1.compareTo(v2);
				}
			};
		ArrayList<Triplet<String,String,Date>> entries = new ArrayList<Triplet<String,String,Date>>(eventsStream);
		//entries.addAll(eventsStream.);
		Collections.copy(entries,eventsStream);
		List<Triplet<String,String,Date>> listOfEntries = new ArrayList<Triplet<String,String,Date>>(entries);
		Collections.sort(listOfEntries, valueComparator);
	    ArrayList<Triplet<String,String,Date>> sortedByValue = new ArrayList<Triplet<String,String,Date>>(listOfEntries.size());
	    //System.out.println(sortedByValue.size());
	    for(Triplet<String,String,Date> entry : listOfEntries){
	    	sortedByValue.add(entry);
	    	}
	    if(NullConfiguration.displayFineStats) {
	    	//printTripletList(sortedByValue);
	    }
	    //printTripletList(sortedByValue);
		return sortedByValue;
	}
	
	public static void finePrint(ArrayList<Triplet<String,String,Date>> stream) {
		for(int k =0; k<stream.size();k++) {
			if(k>0 && k%5==0) {
				System.out.println();
				}
			System.out.print(stream.get(k));
			
		}
		
	}
}
