package org.processmining.prefiximputation.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.javatuples.Triplet;
import org.processmining.framework.util.Pair;
import org.processmining.prefiximputation.inventory.NullConfiguration;

import com.google.common.collect.Ordering;

public class TimeStampsBasedLogToStreamConverter {
	
	public static ArrayList<Triplet<String,String,Date>> sortEventLogByDate(XLog log){
		int index = 0;
		List<Triplet<String,String,Date>> eventsStream = new ArrayList<Triplet<String,String,Date>>();
		for (XTrace t : log) {
			String caseId = XConceptExtension.instance().extractName(t);
			for(XEvent e: t) {				
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
	    	printTripletList(sortedByValue);
	    }
	    //printTripletList(sortedByValue);
		return sortedByValue;
	}
	
	private static void printTripletList(ArrayList<Triplet<String,String,Date>> tripletList ) {
		for(Triplet entry: tripletList) {
			 System.out.println(entry.getValue0() + ", " +  entry.getValue1() + ", " +  entry.getValue2());
			 //System.out.println(entry.getValue(0) + ", " +  entry.getValue(1) + ", " +  entry.getValue(2));
		}
	}
	
	private static void printLinkedHashMap(LinkedHashMap<Pair<String,String>,Date> sortedByValue  ){
		for (Map.Entry<Pair<String,String>,Date> entry : sortedByValue.entrySet()) {		    
		   System.out.println(entry.getKey() + ", " +  entry.getValue());
		}
	}
	
	public static void main(String args[]) {
		String logFile = NullConfiguration.eventLogFilePath;
		XLog log = null;
		//XEventClassifier eventClassifier;
		try {
			log = new XUniversalParser().parse(new File(logFile)).iterator().next();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Triplet<String,String,Date>>	eventLogSortedByDate = TimeStampsBasedLogToStreamConverter.sortEventLogByDate(log);
		checkCorrectness(log, eventLogSortedByDate);
		
	}
	
	private static void checkCorrectness(XLog log, ArrayList<Triplet<String,String,Date>> eventLogSortedByDate) {
		for(int i=0; i<log.size();i++) {
			String caseId = XConceptExtension.instance().extractName(log.get(i));
			ArrayList<Integer> temp = new ArrayList<>();
			for(int j=0; j<log.get(i).size();j++) {
				String newEventName = XConceptExtension.instance().extractName(log.get(i).get(j));
				Date date = XTimeExtension.instance().extractTimestamp(log.get(i).get(j));
				for(int k=0; k<eventLogSortedByDate.size(); k++) {
					Triplet<String,String,Date> trip = eventLogSortedByDate.get(k);
					if(trip.getValue0().equals(caseId) && trip.getValue1().equals(newEventName) && trip.getValue2().compareTo(date)==0) {
						temp.add(k);
						break;
						
					}
				}
			}
			//check if temp is in order
			boolean sorted = Ordering.natural().isOrdered(temp);
			boolean sorted2 = isCollectionSorted(temp);
			if(sorted==true && sorted2==true) {
				System.out.println(temp);
				continue;
			}else {
				System.out.println("The case with Id: " + caseId + " is problematic");
			}
			//System.out.println(temp);
		}
		System.out.println("The checking is finished");
	}
	
	public static boolean isCollectionSorted(ArrayList list) {
	    List copy = new ArrayList(list);
	    Collections.sort(copy);
	    return copy.equals(list);
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

}
