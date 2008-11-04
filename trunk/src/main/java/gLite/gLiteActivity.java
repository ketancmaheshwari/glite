package gLite;

import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jlite.GridSession;
import jlite.GridSessionConfig;
import jlite.GridSessionFactory;
import jlite.util.Util;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.glite.jdl.JobAd;
import org.globus.gsi.GlobusCredential;

/**
 * An Activity providing glite functionality. This is the place where the logic
 * of submitting a job and getting its status back resides...
 * 
 */
public class gLiteActivity extends AbstractAsynchronousActivity<gLiteActivityConfigurationBean> {
	private static Object input;
	private gLiteActivityConfigurationBean configurationBean;
	private String outputDir;
	private static ArrayList<String> wfinput;
	private static ArrayList<String> wfoutput;
	private static HashMap<String, String> datanamemap = new HashMap<String, String>();

	@Override
	public void configure(gLiteActivityConfigurationBean configurationBean) throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		configurePorts(configurationBean);
		List<Class<? extends ExternalReferenceSPI>> handledReferenceSchemes = new ArrayList<Class<? extends ExternalReferenceSPI>>();
		addInput("datain", 0, true, handledReferenceSchemes, String.class);
		addOutput("jobstatus", 0, 0);
		addOutput("dataout", 0, 0);
	}

	@Override
	public gLiteActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	public ActivityInputPort getInputPort(String name) {
		for (ActivityInputPort port : getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data, final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				ReferenceService referenceService = callback.getContext().getReferenceService();

				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();
				try {
					// set inputs
					int i = 0;
					wfinput = new ArrayList<String>();

					for (String inputName : data.keySet()) {
						ActivityInputPort inputPort = getInputPort(inputName);
						input = referenceService.renderIdentifier(data.get(inputName), inputPort.getTranslatedElementClass(), callback.getContext());
						inputName = sanatisePortName(inputName);
						wfinput.add(input.toString());
						// If inputName starts with 'file' call commands to
						// transfer it to ui
						if (/* first part is local and second part is data */getPart(wfinput.get(i), 1).equals("local") && getPart(wfinput.get(i), 2).equals("data")) {
							datanamemap.put(wfinput.get(i), getRandomString());
							Runtime.getRuntime().exec("scp /home/ketan/ManchesterWork/gliteworkflows/inputs/" + getPart(wfinput.get(i),3) + " glite.unice.fr:");
							System.out.println("scp /home/ketan/ManchesterWork/gliteworkflows/inputs/" + getPart(wfinput.get(i),3) + " glite.unice.fr:");
							// Transfer this to grid
							Runtime.getRuntime().exec("ssh glite.unice.fr lcg-del -a lfn:" + datanamemap.get(wfinput.get(i)));
							System.out.println("ssh glite.unice.fr lcg-del -a lfn:" + datanamemap.get(wfinput.get(i)));
							// upload the data on the grid with a random name
							// using getRandomString
							Runtime.getRuntime().exec("ssh glite.unice.fr lcg-cr --vo biomed -l lfn:" + datanamemap.get(wfinput.get(i)) + " -d prod-se-01.pd.infn.it file://`pwd`/" + getPart(wfinput.get(i),3));
							System.out.println("ssh glite.unice.fr lcg-cr --vo biomed -l lfn:" + datanamemap.get(wfinput.get(i)) + " -d prod-se-01.pd.infn.it file://`pwd`/" + getPart(wfinput.get(i),3));
						}
						if(/*first part lfn and second part data*/getPart(wfinput.get(i), 1).equals("lfn") && getPart(wfinput.get(i), 2).equals("data")){
							datanamemap.put(wfinput.get(i), getRandomString());
						}
						i++;
					}
					
					int j=0;
					wfoutput=new ArrayList<String>();
					int indexofstatus = 0;
					for (OutputPort outputPort : getOutputPorts()) {
					//	Object value = null;
						String name = outputPort.getName();
						wfoutput.add(j, name);
						if(name.equals("jobstatus")){
							indexofstatus=j;
						}else{
							datanamemap.put(wfoutput.get(j), "lfn:data:"+getRandomString());	
						}
						
						j++;
					}
					
					displayPortDetails();
					
					configurationBean.getJdlconfigbean().setWrapper(createWrapper(configurationBean));
					configurationBean.setJDLPath(createJDL(configurationBean));

					// run the activity
					/*
					 * ----------------------------------------- Configure and
					 * create Grid session
					 * -----------------------------------------
					 */
					GridSessionConfig config = new GridSessionConfig();
					// path to CA certificates
					config.setCADir(configurationBean.getCaDir());
					// paths to VOMS configuration files and certificates
					config.setVOMSDir(configurationBean.getVOMSDir());
					config.setVOMSCertDir(configurationBean.getVOMSCertDir());
					// path to WMProxy configuration files
					config.setWMSDir(configurationBean.getWMSDir());
					config.addWMProxy(configurationBean.getVO(), configurationBean.getWMProxy());
					config.setProxyPath(configurationBean.getProxyPath());
					// create Grid session
					GridSession session = GridSessionFactory.create(config);

					/*
					 * ---------------------------------------------- Create
					 * user proxy (12 hours)
					 * ----------------------------------------------
					 */
					GlobusCredential proxy = new GlobusCredential(configurationBean.getProxyPath());
					System.out.println("Created user proxy: " + proxy.getSubject());

					/*
					 * --------------------------------------------- Delegate
					 * user proxy to WMProxy server
					 * ---------------------------------------------
					 */
					session.delegateProxy(configurationBean.getDelegationID());

					/*
					 * ---------------------------- Load job description
					 * ----------------------------
					 */
					JobAd jad = new JobAd();
					jad.fromFile(configurationBean.getJDLPath());
					String jdl = jad.toString();

					/*
					 * -------------------------- Submit job to grid
					 * --------------------------
					 */
					String jobId = session.submitJob(jdl, configurationBean.getJdlconfigbean().getInputsPath());
					System.out.println("Started job: " + jobId);

					/*
					 * -------------------------- Monitor job status
					 * --------------------------
					 */
					String jobState = "";
					do {
						Thread.sleep(Long.parseLong(configurationBean.getPollFrequency()));
						jobState = session.getJobState(jobId);
						System.out.println("Job status: " + jobState);
					} while (!jobState.equals("DONE") && !jobState.equals("ABORTED"));

					/*
					 * --------------------------- Download job output
					 * ---------------------------
					 */
					if (jobState.equals("DONE")) {
						System.out.println("output path=" + configurationBean.getOutputPath());
						outputDir = configurationBean.getOutputPath() + Util.getShortJobId(jobId);
						System.out.println("outputdir= " + outputDir);
						session.getJobOutput(jobId, outputDir);
						System.out.println("Job output is downloaded to: " + outputDir);
					}
					
					// register outputs
					for (OutputPort outputPort : getOutputPorts()) {
						Object value = null;
						String name = outputPort.getName();
						if (name.equals("jobstatus")){
							value = jobState;
							datanamemap.put(wfoutput.get(indexofstatus),value.toString());
						}
						
						
						j++;
						if (value != null) {
							outputData.put(name, referenceService.register(value, outputPort.getDepth(), true, callback.getContext()));
						}
						System.out.println("Output port's name is " + name + " and value is " + value.toString());
						// clear outputs
						// TODO
					}
					// send result to the callback
					callback.receiveResult(outputData, new int[0]);
				} catch (Exception e) {
					System.err.println("**Exception Occured**");
					System.err.println(e.getLocalizedMessage());
					System.err.println(e.getMessage());
					callback.fail("Exception", e);
				}
			}

			/**
			 * Removes any invalid characters from the port name. For example,
			 * xml-text would become xmltext.
			 * 
			 * @param name
			 * @return
			 */
			private String sanatisePortName(String name) {
				String result = name;
				if (Pattern.matches("\\w++", name) == false) {
					result = "";
					for (char c : name.toCharArray()) {
						if (Character.isLetterOrDigit(c) || c == '_') {
							result += c;
						}
					}
				}
				return result;
			}
		});
	}

	public static String createJDL(gLiteActivityConfigurationBean glb) throws IOException {

		File jdlfile = new File("/tmp/", System.currentTimeMillis() + ".jdl");
		PrintWriter f = new PrintWriter(new FileWriter(jdlfile));
		// add other properties line by line to the file
		f.println("Type=\"" + glb.getJdlconfigbean().getType() + "\";");
		f.println("JobType=\"" + glb.getJdlconfigbean().getJobType() + "\";");
		// add nodenumber for MPI jobs
		if (glb.getJdlconfigbean().getJobType().equals("MPICH"))
			f.println("NodeNumber=" + glb.getJdlconfigbean().getNodeNumber() + ";");
		f.println("Executable=\"" + glb.getJdlconfigbean().getWrapper() + "\";");
		f.println("Arguments=\"" + glb.getJdlconfigbean().getArguments() + "\";");
		f.println("Stdoutput=\"" + glb.getJdlconfigbean().getStdOut() + "\";");
		f.println("StdError=\"" + glb.getJdlconfigbean().getStdErr() + "\";");
		f.println("InputSandbox={" + glb.getJdlconfigbean().getInputSandbox() + ",\"" + glb.getJdlconfigbean().getWrapper() + "\"};");
		f.println("OutputSandbox={" + glb.getJdlconfigbean().getOutputSandbox() + ",\"outdata.txt\"};");
		f.println("RetryCount=" + glb.getJdlconfigbean().getRetryCount() + ";");
		f.println("Requirements=" + glb.getJdlconfigbean().getRequirements() + ";");
		f.println("Rank=(-other.GlueCEStateEstimatedResponseTime);");
		f.println();
		// close file
		f.close();
		// return its path
		return jdlfile.getAbsolutePath();
	}
	
	public static String createWrapper(gLiteActivityConfigurationBean glb) throws IOException {

		File wrapperfile = new File(glb.getJdlconfigbean().getInputsPath(), "wrapper_" + System.currentTimeMillis() + ".sh");
		PrintWriter f = new PrintWriter(new FileWriter(wrapperfile));
		f.println("#!/bin/sh");
		f.println("export LFC_HOME=lfc-biomed.in2p3.fr:/grid/biomed/testKetan");
		f.println();
		f.println("#Read the starting time");
		f.println("DATA_TRANSFER_FROM_GRID_START=`date +%s`");
		f.println();
		f.println("#copy data to Workernode");

		for (int i = 0; i < wfinput.size(); i++) {
			if(getPart(wfinput.get(i), 1).equals("lfn")){
				f.println("rm -f " + getPart(wfinput.get(i),3));
				f.println("lcg-cp --vo biomed lfn:" + getPart(wfinput.get(i),3) + " file://$(pwd)/" + getPart(wfinput.get(i),3));
			}
		}

		f.println("DATA_TRANSFER_FROM_GRID_END=`date +%s`");
		f.println("TIME_TAKEN_FOR_DATA_TRANSFER_FROM_GRID=`expr $DATA_TRANSFER_FROM_GRID_END - $DATA_TRANSFER_FROM_GRID_START`");

		f.println();
		f.println("START=`date +%s`");
		f.println("#export current path and run the executable");
		f.println("export PATH=.:$PATH");
		//f.println("/bin/chmod 755 " + glb.getJdlconfigbean().getExecutable());
		//create the files corresponding to the outputports
		try{
			for (int i = 0; i < wfoutput.size(); i++) {
				if(datanamemap.get(wfoutput.get(i))!=null)
					f.println("touch "+getPart(datanamemap.get(wfoutput.get(i)),3));
			}	
		}catch (Exception e) {
			System.err.println("exception in touch");
			System.err.println(e.getMessage());
			System.err.println(e.getLocalizedMessage());
			// TODO: handle exception
		}
		
		//create a string with all input and output ports separated by space
		try{
			String arg="";
			for (int i = 0; i < wfinput.size(); i++) {
				if(datanamemap.get(wfinput.get(i))!=null)
					arg=arg+" "+datanamemap.get(wfinput.get(i));
				else
					arg=arg+" "+wfinput.get(i);
			}
			for (int i = 0; i < wfoutput.size(); i++) {
				if(datanamemap.get(wfoutput.get(i))!=null)
					arg=arg+" "+getPart(datanamemap.get(wfoutput.get(i)),3);
			}
			System.out.println("Arguments ===> "+arg);
			
		}catch (IllegalArgumentException iae) {
			System.err.println("exception in creating args");
			System.err.println(iae.getMessage());
			
			// TODO: handle exception
		}catch(Exception e){
			System.err.println("exception in creating args");
			System.err.println(e.getLocalizedMessage());
		}
		
		
		//put executable with all ports marked with data as arguments 
		f.println(glb.getJdlconfigbean().getExecutable() + " " + "$*");
		f.println();
		f.println("STOP=`date +%s`");
		f.println("TOTAL=`expr $STOP - $START`");
		f.println("echo \"Total running time: $TOTAL seconds\"");
		f.println("DATA_TRANSFER_TO_GRID_START=`date +%s`");
		
		f.println("Treating input ports ..");
		for (int i = 0; i < wfinput.size(); i++) {
			if(getPart(wfinput.get(i), 1).equals("lfn")){
				f.println("lcg-del --vo biomed -a lfn:" + getPart(wfinput.get(i),3));
				// introduce a delay for updation of the lcg catalogue
				f.println("/bin/sleep 3");
				f.println("lcg-cr --vo biomed -l lfn:" + getPart(wfinput.get(i),3) + " -d prod-se-01.pd.infn.it file://$(pwd)/" + getPart(wfinput.get(i),3));				
			}
		}
		
		f.println("Treating output ports ...");
		for (int i = 0; i < wfoutput.size(); i++) {
			f.println(wfoutput.get(i));
		}

		f.println("DATA_TRANSFER_TO_GRID_END=`date +%s`");

		f.println("TIME_TAKEN_FOR_DATA_TRANSFER_TO_GRID=`expr $DATA_TRANSFER_TO_GRID_END - $DATA_TRANSFER_TO_GRID_START`");

		f.println("TOTAL_DATA_TRANSFER_TIME=`expr $TIME_TAKEN_FOR_DATA_TRANSFER_FROM_GRID + $TIME_TAKEN_FOR_DATA_TRANSFER_TO_GRID`");

		f.println("echo \"Total data transfer time: $TOTAL_DATA_TRANSFER_TIME seconds. \"");
		f.println("#Create the file outdata.txt");
		f.println("touch outdata.txt");
		// copy the outputdata link to this file
		f.println();
		f.close();
		return wrapperfile.getName();
	}


	private static String getPart(String s, int n) {

		java.util.StringTokenizer st = new java.util.StringTokenizer(s, ":");
		int i = 1;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (i == n)
				return token;
			i++;
		}
		return null;
	}

	private static String getRandomString() {
		Random r = new Random();
		return Long.toString(Math.abs(r.nextLong()), 36).substring(0, 5);
	}
	
	private static void displayPortDetails(){
		for (int i = 0; i < wfinput.size(); i++) {
			System.out.println(wfinput.get(i)+" ==> "+datanamemap.get(wfinput.get(i)));
		}
		
		for (int i = 0; i < wfoutput.size(); i++) {
			System.out.println(wfoutput.get(i)+" ==> "+datanamemap.get(wfoutput.get(i)));
		}
	}
	
}

