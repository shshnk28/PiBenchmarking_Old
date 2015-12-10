package helperClasses;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileAnalysis {
	
	public FileAnalysis() {
		
	}
	public void calculateFrequencyFromBuffer(HashMap<Long,Integer>buffer, String fileTag) {
		
		File file = new File(fileTag);
		if(file.exists() == true) {
			file.delete();
		}
		try {
			BufferedWriter fileBuffer = new BufferedWriter(new FileWriter(file));
			List<Long> keys = new ArrayList<Long>(buffer.keySet());
			Collections.sort(keys);
			for (Long key:keys) {
				Integer value = buffer.get(key);
				fileBuffer.write(key+ ": "+value);
				fileBuffer.write("\n");
			}
			fileBuffer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void calculateFrequency(String filePath, String fileTag) {
		//open the file write the file tag then do your processing 
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line;
			String referenceStr = null;
			int count =0;
			Map<String, String> dictionary = new HashMap<String,String>(); 
			while((line = reader.readLine())!=null) {
				String[] parts = line.split(",");
				if(referenceStr  == null) {
					referenceStr = parts[0]; 
				}
				if(referenceStr.equals(parts[0])) {
					count++;
				} else {
					// write the count to a dict
					if (referenceStr!= null) {
						dictionary.put(referenceStr, Integer.toString(count));
					}
					referenceStr = parts[0];
					count = 0;
				}
				
			}
			dictionary.put(referenceStr, Integer.toString(count));
			reader.close();
			writeDatatoFile(dictionary, fileTag, filePath);
			// call the method to write it in a file
		} catch (FileNotFoundException e) {
			System.out.println("file not found");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("some io exception");
			e.printStackTrace();
		}
	    
	}
	
	public void writeDatatoFile(Map<String, String> dictionary,String fileTag, String filePath) {
		String filePath1 = filePath;
		if (filePath.indexOf(".") > 0) { // remove the extension
			filePath1 = filePath.substring(0, filePath.lastIndexOf("."));
		}
		String fileToBeWritten = filePath1 + fileTag;
		File file = new File(fileToBeWritten);
		if(file.exists() == true) {
			file.delete();
		}
		try {
			BufferedWriter fileBuffer = new BufferedWriter(new FileWriter(file));
			List<String> keys = new ArrayList<String>(dictionary.keySet());
			Collections.sort(keys);
			for (String key:keys) { 
				String value = dictionary.get(key);
				fileBuffer.write(key+ ": "+value);
				fileBuffer.write("\n");
			}
			fileBuffer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
