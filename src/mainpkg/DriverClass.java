package mainpkg;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helperClasses.ExecutionPlan;
import helperClasses.FileAnalysis;

public class DriverClass implements iSyntheticEventGen{
//	static String pathToFile = "500-500-5000-1.csv";
	static String pathToFile = null;
//	static String pathToFile = "/Users/shashankshekhar/PycharmProjects/AdvancedDataGen/10000-30.csvclear"; 
	EventGen eventGen;
	String dataSetType = "PLUG-11";
	InputHandler inputHandler = null;
//	Calendar calendar = null;
//	BufferedWriter inputFileBuff = null;
//	BufferedWriter outFileBuff = null;
	int sigma = 1; // sigma should be configurable from outside
//	File inputFile = null;
	File outFile  = null;
	HashMap<Long, Integer> inputHashMap = null;
	HashMap<Long, Integer> outputHashMap = null;
	private static int numThreads= 0;
	public static Logger Log = LoggerFactory.getLogger(DriverClass.class);
	public static void main(String[] args) {
		// initialise the file path here 
//		System.out.println("array length = "+Integer.toString(args.length));
//		LOG.debug("debugstring ");
		Log.info("experiment started");
//		LOG.error("error string");
		
		if (args.length != 1) {
			System.out.println("Please enter the filname as only parameter.. Returning");
			return;
		}
		pathToFile = args[0];
		File testFile = new File(pathToFile);
		if(testFile.exists() == false || testFile.isDirectory() == true) {
			System.out.println("file does not exist.. Returning");
			return;
		}
		
		DriverClass dc = new DriverClass();
		dc.initiateExecutionPlan();
		dc.initiateEventGen();
	}
	private void initiateExecutionPlan () {
		SiddhiManager siddhiManager = new SiddhiManager();
        String executionPlan = ExecutionPlan.returnExecutionPlan("fil");
        if (executionPlan == null) {
        	Log.info("pls enter a valid execution plan(fil/agg/seq) as 2nd cmdline parameter");
        	return;
        }
        	
        //Generating runtime
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        //Adding callback to retrieve output events from query
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
//                EventPrinter.print(inEvents);
            	// write the data in outfile 
//                long ts = inEvents[0].getTimestamp();
//                int data = inEvents[0].getData();
                
                for (Event eve:inEvents) {
//                	long time = System.currentTimeMillis()/1000;
                	Long key = Long.valueOf(System.currentTimeMillis()/1000);
        			if(outputHashMap.containsKey(key)) {
        				Integer tempInt = outputHashMap.get(key);
        				outputHashMap.put(key, tempInt+1);
        				
        			}
        			else {
        				outputHashMap.put(Long.valueOf(System.currentTimeMillis()/1000), Integer.valueOf(1));
        			}
//                	Object[] data = eve.getData();
//                	int height = (int)data[0];
//                	try {
//                		outFileBuff.write(time+ "," + height);
//            			outFileBuff.write("\n");
//                	} catch (IOException ex) {
//            			System.out.println("could not write filter output to file");
//            			ex.printStackTrace();
//            		}
                }
            }
        });
        inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
       

	}
	private void initiateEventGen () {
		inputHashMap=  new HashMap<Long,Integer>();
		outputHashMap=  new HashMap<Long,Integer>();
//		calendar = Calendar.getInstance();
//		try {
////			inputFile = new File("inputDataFile.csv");
//			outFile = new File("outputDataFile.csv");
////			inputFileBuff = new BufferedWriter(new FileWriter(inputFile));
////			outFileBuff = new BufferedWriter(new FileWriter(outFile));
//		} catch (IOException ex) {
//			System.out.println("could not open file");
//			ex.printStackTrace();
//		}
		 
		
		eventGen=new EventGen(this,1); // scaling factor is 1
		try {
            eventGen.launch(pathToFile,dataSetType);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException(e);
        }
	}
	@Override
    public void receive(List<String> event)  {	 
		try {
			inputHandler.send(new Object[]{Integer.parseInt(event.get(2))});
//			System.out.println("data received" + event.get(2));
//			inputFileBuff.write(System.currentTimeMillis()/1000 + "," + event.get(2));
//			inputFileBuff.write("\n");
			hashMapupdate();
			
		} catch (InterruptedException ie) {
			System.out.println("could not send to Siddhi");
			ie.printStackTrace();
		} 
//		catch (IOException ex) {
//			System.out.println("could not write to input file");
//			ex.printStackTrace();
//		}
		
	}
	private synchronized void hashMapupdate () {
		Long key = Long.valueOf(System.currentTimeMillis()/1000);
		if(inputHashMap.containsKey(key)) {
			Integer tempInt = inputHashMap.get(key);
			inputHashMap.put(key, tempInt+1);
		}
		else {
			inputHashMap.put(Long.valueOf(System.currentTimeMillis()/1000), Integer.valueOf(1));
		}
	}
	@Override
	public void dataSetOver() {
		Log.info("datasetOver called");
//		for (Map.Entry<Long, Integer> entry: inputHashMap.entrySet()) {
//			Long key  = entry.getKey();
//			Integer val = entry.getValue();
//			System.out.println("key val pair\n");
//			System.out.println(key);
//			System.out.println(val);
//		}
//			inputFileBuff.close();
		try {
			Thread.sleep(10000);
			fileWrite();
		} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private synchronized void fileWrite () {
		numThreads++;
		if (numThreads !=GlobalConstants.numThreads) {
			System.out.println("sskipping file write call");
			return;
		}
		Log.info("fileWrite called");
		FileAnalysis obj = new FileAnalysis();
//		obj.calculateFrequency(inputFile.getAbsolutePath(), "result-"+pathToFile);
		obj.calculateFrequencyFromBuffer(inputHashMap, "inputRes-" + pathToFile);
		obj.calculateFrequencyFromBuffer(outputHashMap, "outputRes-" + pathToFile);
//		obj.calculateFrequency(outFile.getAbsolutePath(), "result-"+pathToFile);
		Log .info("experiment over");
	}

}
