package gLite;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

//Ketan Was Here!!
public class blah {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		FileReader input = new FileReader("/home/ketan/data/config1.init");
		BufferedReader bufRead = new BufferedReader(input);
		              
		              String line;    // String that holds current file line
		              int count = 0;  // Line number of count 
		              String[] dir = {"",""};
		              int i=0;
		              // Read first line
		              line = bufRead.readLine();
		              count++;
		             // Read through file one line at time. Print line # and line
		              while (line != null){
		            	  if(line.contains("data")) dir[i++]=line;
		            	  line = bufRead.readLine();
		                  count++;
		              }
		              bufRead.close();
		              System.out.println(dir[0]);
		              System.out.println(dir[1]);
		             
	}

}
