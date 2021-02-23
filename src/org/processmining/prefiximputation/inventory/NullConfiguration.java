package org.processmining.prefiximputation.inventory;

public class NullConfiguration {
	public static boolean isExperiment = true;
	//public static boolean isExperiment = false;
	public static boolean displayFineStats = false;
	public static boolean allowedDuplicateLabelApproximation = true;
	public static double similarityThreshold = 0.66;
	public static boolean isMappingAutomatic = false;
	
	//public static int maxCasesToStore = Integer.MAX_VALUE;	
	public static int maxCasesToStore = 100;
	
	String petriNetFileName[] = {"CCC19 - Model PN_modified.pnml", "Model_A.pnml", "Model_O.pnml", "Model_AO.pnml"};
	
	public static String petriNetFilePath = "D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\"
			+ "Process Models BPI 2012 from Boudewijn\\Model_AO.pnml";
	//CCC19 - Model PN_modified.pnml //Model_A (2).pnml
	
	String eventLogFileName[] = {"A only Events.xes", "O only Events.xes", "A plus O Events.xes", "CCC19  single trace.xes", "problematic cases sample_.xes"  };	
	
	public static String eventLogFilePath = "D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\"
			+ "Process Models BPI 2012 from Boudewijn\\A plus O Events.xes";
	//cpnToolsSimulationLog.mxml //Only_A_Events.xes
	String processTreeFileName[] = {"processtree.ptml"};
	
	public static String processTreeFilePath = "D:\\Research Work\\latest\\Streams\\Rashid Prefix Alignment\\processtree.ptml";
}

