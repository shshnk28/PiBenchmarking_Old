package mainpkg;


import helperClasses.*;


import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


public class EventGen {
	iSyntheticEventGen iseg;
	ExecutorService executorService;
	double scalingFactor;
//////
	private void writeObject(java.io.ObjectOutputStream stream)
			throws IOException {
		stream.writeObject(iseg);
//		stream.writeInt(id);
		stream.writeObject(scalingFactor);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		iseg = (iSyntheticEventGen) stream.readObject();
//		id = stream.readInt();
		scalingFactor = (Double) stream.readObject();
	}

	////
	
	public EventGen(iSyntheticEventGen iseg){
		this(iseg, GlobalConstants.accFactor);
	}

	public EventGen(iSyntheticEventGen iseg, double scalingFactor){
		this.iseg = iseg;
		this.scalingFactor = scalingFactor;
	}
	
	public static List<String> getHeadersFromCSV(String csvFileName){
		return CsvSplitter.extractHeadersFromCSV(csvFileName);
	}
	
	//Launches all the threads
	public void launch(String csvFileName, String outCSVFileName){
		//1. Load CSV to in-memory data structure
		//2. Assign a thread with (new in.dream_lab.genevents.SubEventGen(myISEG, eventList))
		//3. Attach this thread to ThreadPool
		try {
			int numThreads = GlobalConstants.numThreads;
			//double scalingFactor = GlobalConstants.accFactor;
			String datasetType = "";
			if(outCSVFileName.indexOf("TAXI") != -1){
				datasetType = "TAXI";
			}
			else if(outCSVFileName.indexOf("SYS") != -1){
				datasetType = "SYS";
			}
			else if(outCSVFileName.indexOf("PLUG") != -1){
				datasetType = "PLUG";
			}
			List<TableClass> nestedList = CsvSplitter.roundRobinSplitCsvToMemory(csvFileName, numThreads, scalingFactor, datasetType);
			
			this.executorService = Executors.newFixedThreadPool(numThreads);
			Semaphore sem1 = new Semaphore(0);
			Semaphore sem2 = new Semaphore(0);
			
			SubEventGen[] subEventGenArr = new SubEventGen[numThreads];
			for(int i=0; i<numThreads; i++){
				//this.executorService.execute(new in.dream_lab.genevents.SubEventGen(this.iseg, nestedList.get(i)));
				subEventGenArr[i] = new SubEventGen(this.iseg, nestedList.get(i), sem1, sem2);
				this.executorService.execute(subEventGenArr[i]);
			}
			
			sem1.acquire(numThreads);
			//set the start time to all the thread objects
			long experiStartTs = System.currentTimeMillis();
			for(int i=0; i<numThreads; i++){
				//this.executorService.execute(new in.dream_lab.genevents.SubEventGen(this.iseg, nestedList.get(i)));
				subEventGenArr[i].experiStartTime = experiStartTs;
				this.executorService.execute(subEventGenArr[i]);
			}
			sem2.release(numThreads);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException ex) {
			// TODO Auto-generated catch block
			System.out.println("exception in event gen\n");
			ex.printStackTrace();
		}	
	}
	
	public static void testRun () {
		System.out.println("in eventgetn");
	}
}


class SubEventGen implements Runnable{
	iSyntheticEventGen iseg;
	TableClass eventList;
	Long experiStartTime;  //in millis since epoch
	Semaphore sem1, sem2;
	
	public SubEventGen(iSyntheticEventGen iseg, TableClass eventList, Semaphore sem1, Semaphore sem2){
		this.iseg = iseg;
		this.eventList = eventList;
		this.sem1 = sem1;
		this.sem2 = sem2;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		sem1.release();
		try {
			sem2.acquire();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//System.out.println("THREAD: " + Thread.currentThread().getName() + " size of eventList = " + this.eventList.getRows().size() + " relativeTs= " + this.eventList.getTs().get(0) + " TRY to call receive ###" + " this.experiStartTs = " + this.experiStartTime);
		
		for(int i=0; i<this.eventList.getRows().size(); i++){
			Long deltaTs = this.eventList.getTs().get(i);
			List<String> event = this.eventList.getRows().get(i);
			Long t1 = System.currentTimeMillis();
			if( (t1 - (this.experiStartTime+deltaTs)) < 0){
				try {
					Thread.sleep((this.experiStartTime+deltaTs) - System.currentTimeMillis());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			this.iseg.receive(event);
		}
		this.iseg.dataSetOver();
	}	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
