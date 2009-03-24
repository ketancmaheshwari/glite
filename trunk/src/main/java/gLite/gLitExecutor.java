package gLite;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import net.sf.taverna.t2.workflowmodel.OutputPort;

import org.glite.jdl.JobAd;

import jlite.GridAPIException;
import jlite.GridSession;
import jlite.GridSessionConfig;
import jlite.GridSessionFactory;
import jlite.util.Util;

public class gLitExecutor {
	private TreeMap<String, Object> wfinput;
	private ArrayList<String> wfoutput;
	private Collection<Object> inputportvalues;
	private String nextinput;
	private Map<String, String> datanamemap;
	private String wrapperarg;
	private static Random rand=new Random();
	
	
	public gLitExecutor() {
		super();
		datanamemap=new HashMap<String, String>();
		wfinput=new TreeMap<String, Object>();
		wfoutput=new ArrayList<String>();
		wrapperarg=new String();
	}

	/*public String postprocess(ArrayList<String> wfout, gLiteActivityConfigurationBean glb) {
		// sort outputport names
		Collections.sort(wfout);
		String wrapperarg = null;
		for (Iterator<String> iterator = wfout.iterator(); iterator.hasNext();) {
			nextinput = (String) iterator.next();
			if (datanamemap.get(nextinput) != null)
				wrapperarg += " " + getPart(datanamemap.get(nextinput), 2);
		}
		System.out.println("Execution String is  " + glb.getJdlconfigbean().getExecutable() + " " + wrapperarg);

		return "blah";
	}*/

	public void setOutParams(OutputPort outputPort, Object value) {
		wfoutput.add(outputPort.getName());
		datanamemap.put(outputPort.getName(), (String) value);
		
	}

	public void setInParams(String paramname, Object paramvalue){
		// Create DatanameMap
		wfinput.put(paramname, paramvalue);
	}
	
	public void dataTransfer(gLiteActivityConfigurationBean configurationBean) throws IOException, InterruptedException{
		inputportvalues = wfinput.values();
		for (Iterator<Object> iterator = inputportvalues.iterator(); iterator.hasNext();) {
			// If inputName starts with 'file' transfer it to ui
			nextinput = (String) iterator.next();
			if (getPart(nextinput, 1).equals("file")) {
				datanamemap.put(nextinput, getRandomString());
				ProcessBuilder pb1 = new ProcessBuilder("bash", "-c", "scp " + configurationBean.getJdlconfigbean().getInputsPath() + "" + getPart(nextinput, 2) + " "
						+ configurationBean.getUI() + ":");
				Process p1 = pb1.start();
				int exitval1 = p1.waitFor();
				System.out.println("Exit value for scp is " + exitval1);

				System.out.println("ssh " + configurationBean.getUI() + " lcg-cr --vo biomed -l lfn:" + datanamemap.get(nextinput) + " -d " + configurationBean.getSE()
						+ " file:///home/ketan/" + getPart(nextinput, 2));
				ProcessBuilder pb2 = new ProcessBuilder("bash", "-c", "ssh " + configurationBean.getUI() + " lcg-cr --vo biomed -l lfn:" + datanamemap.get(nextinput) + " -d "
						+ configurationBean.getSE() + " file:///home/ketan/" + getPart(nextinput, 2));
				Process p2 = pb2.start();
				int exitval2 = p2.waitFor();
				System.out.println("Exit value for lcg-cr is " + exitval2);
			}
		}

	}

	private String createWrapperArg() {
		// create a string with all input and output ports separated by space
		
		for (Iterator<Object> iterator = inputportvalues.iterator(); iterator.hasNext();) {
			nextinput = (String) iterator.next();
			if (getPart(nextinput, 1).equals("lfn")) {
				wrapperarg+=" " + getPart(nextinput, 2);
			} else if (datanamemap.get(nextinput) != null) {
				wrapperarg+=" " + datanamemap.get(nextinput);
			}
		}
		Collections.sort(wfoutput);
		for (Iterator<String> iterator = wfoutput.iterator(); iterator.hasNext();) {
			nextinput = (String) iterator.next();
			if (datanamemap.get(nextinput) != null)
				wrapperarg+=" " + getPart(datanamemap.get(nextinput), 2);
		}
		return wrapperarg;
	}

	public String execute(gLiteActivityConfigurationBean configurationBean) throws Exception {
		
		GridSessionConfig config = null;
		GridSession session = null;
		int retrycount = 0;
		long start_time = System.currentTimeMillis();

		config = new GridSessionConfig();
		// path to CA certificates
		config.setCertDir(configurationBean.getCaDir());
		// paths to VOMS configuration files and certificates
		config.setVOMSDir(configurationBean.getVOMSDir());
		config.setVOMSCertDir(configurationBean.getVOMSCertDir());
		// path to WMProxy configuration files
		config.setWMSDir(configurationBean.getWMSDir());
		String vo = configurationBean.getVO();
		String[] wmproxy = { "https://lcgwms02.gridpp.rl.ac.uk:7443/glite_wms_wmproxy_server",
				"https://wmslb101.grid.ucy.ac.cy:7443/glite_wms_wmproxy_server", "https://grid07.lal.in2p3.fr:7443/glite_wms_wmproxy_server",
				"https://wms01.grid.sinica.edu.tw:7443/glite_wms_wmproxy_server", "https://wms01.egee-see.org:7443/glite_wms_wmproxy_server",
				"https://svr023.gla.scotgrid.ac.uk:7443/glite_wms_wmproxy_server", "https://glite-rb.scai.fraunhofer.de:7443/glite_wms_wmproxy_server",
				"https://grid-wms.ii.edu.mk:7443/glite_wms_wmproxy_server", "https://rb1.cyf-kr.edu.pl:7443/glite_wms_wmproxy_server","https://grid25.lal.in2p3.fr:7443/glite_wms_wmproxy_server" };
		boolean proxyassigned = true;
		config.setProxyPath(configurationBean.getProxyPath());

		int wmproxyroundrobincounter = 0;
		String currproxy=wmproxy[rand.nextInt(wmproxy.length)];
		System.out.println("Currently used proxy is "+ currproxy);
		config.addWMProxy(vo, currproxy);

		jobsubmitloop: while (true) {
			if (retrycount > 4) {
				System.out.println("Too many retries done!! Quitting!!!");
				System.exit(1);
			}

			wmproxyroundrobincounter++;
			if (wmproxyroundrobincounter >= wmproxy.length)
				wmproxyroundrobincounter = 0;
			if (!proxyassigned) {
				config.addWMProxy(vo, wmproxy[wmproxyroundrobincounter]);
				proxyassigned = true;
			}

			// create Grid session
			session = GridSessionFactory.create(config);
				try {
					// Delegate user proxy to WMProxy server
					session.delegateProxy(getRandomString());
					//session.delegateProxy("501");
				} catch (GridAPIException e) {
					e.printStackTrace();
				}
			// Load job description
			JobAd jad = new JobAd();
			String jobId = new String("none");

			String innerarg = configurationBean.getJdlconfigbean().getJDLArguments();
			String wrappername = createWrapper(configurationBean, wfinput, wfoutput, datanamemap, innerarg);
			wrapperarg="";
			wrapperarg=createWrapperArg();
			System.out.println("Wrapper Arg is: "+wrapperarg);
			String JDLPath = createJDL(configurationBean, wrappername, wrapperarg);
			jad.fromFile(JDLPath);
			String jdl = new String("");
			jdl = jad.toString();
			// Submit job to grid
			jobId = session.submitJob(jdl, configurationBean.getJdlconfigbean().getInputsPath());
			// resubmit if jobid is null
			if (jobId.equals("none")) {
				System.out.println("Resubmitting: jobId is returned as null " + config.getWMProxies().get("biomed"));
				System.out.println("jdl is " + jdl);
				System.out.println("inputspath is " + configurationBean.getJdlconfigbean().getInputsPath());
				System.out.println("jobid is " + jobId);
				proxyassigned = false;
				continue;
			}
			System.out.println("Started job with Id : " + jobId + " (" + configurationBean.getJdlconfigbean().getExecutable() + ") ");

			String jobState="";
			boolean flaginwaiting = false;
            boolean flaginscheduled = false;
            boolean flaginready = false;
            long start_time_in_waiting_state = System.currentTimeMillis();
            long start_time_in_scheduled_state = System.currentTimeMillis();
            long start_time_in_ready_state = System.currentTimeMillis();

			do {
				Thread.sleep(Long.parseLong(configurationBean.getPollFrequency()));
				
				try {
					jobState = session.getJobState(jobId);
				} catch (Exception e) {
					// TODO: handle exception
					System.err.println("Number Format Exception");
					continue jobsubmitloop;
				}

				if (jobState.equals("WAITING") && !flaginwaiting) {
					start_time_in_waiting_state = System.currentTimeMillis();
					flaginwaiting = true;
				}

				if (jobState.equals("READY") && !flaginready) {
					start_time_in_ready_state = System.currentTimeMillis();
					flaginready = true;
				}

				if (jobState.equals("SCHEDULED") && !flaginscheduled) {
					start_time_in_waiting_state = System.currentTimeMillis();
					flaginscheduled = true;
				}

				if (((System.currentTimeMillis() - start_time_in_waiting_state >= 900000) && jobState.equals("WAITING"))) {
					System.out.println("Resubmitting because taking too much time in WAITING state");
					retrycount++;
					proxyassigned = false;
					continue jobsubmitloop;
				}
				if (((System.currentTimeMillis() - start_time_in_ready_state >= 900000) && jobState.equals("SCHEDULED"))) {
					System.out.println("Resubmitting because taking too much time in SCHEDULED state");
					retrycount++;
					proxyassigned = false;
					continue jobsubmitloop;
				}
				if (((System.currentTimeMillis() - start_time_in_scheduled_state >= 900000) && jobState.equals("SCHEDULED"))) {
					System.out.println("Resubmitting because taking too much time in SCHEDULED state");
					retrycount++;
					proxyassigned = false;
					continue jobsubmitloop;
				}
				System.out.println("Job status: " + configurationBean.getJdlconfigbean().getExecutable() + " ( " + jobId + " ) : " + jobState);
			} while (!jobState.equals("DONE") && !jobState.equals("ABORTED"));

			if (jobState.equals("ABORTED")) {
				System.out.println("Resubmitting because aborted");
				retrycount++;
				proxyassigned = false;
				continue;
			}

			String outputDir = new String();
			// Download job output
			if (jobState.equals("DONE")) {
				DateFormat df = DateFormat.getInstance();
				outputDir = configurationBean.getOutputPath() + Util.getShortJobId(jobId);
				session.getJobOutput(jobId, outputDir, true);
				System.out.println("Job output is downloaded to: " + outputDir + " at " + df.format(new Date()));
			}
			break;
		}
		long end_time = System.currentTimeMillis();
		long elapsed_time = (end_time - start_time) / 1000;
		System.out.println("The Job took " + elapsed_time + " seconds to complete.");
		return "returned";
	}
	
	
	private static String createWrapper(gLiteActivityConfigurationBean glb, TreeMap<String, Object> wfinput, ArrayList<String> wfoutput, Map<String, String> datanamemap,
			String innerarg) throws IOException {

		File wrapperfile = new File(glb.getJdlconfigbean().getInputsPath(), "wrapper_" + System.currentTimeMillis() + ".sh");
		PrintWriter f = new PrintWriter(new FileWriter(wrapperfile));
		f.println("#!/bin/bash");
		// f.println("/bin/sleep 10");
		f.println("echo $*");
		f.println("/bin/hostname");
		f.println("export LFC_HOME=lfc-biomed.in2p3.fr:/grid/biomed/testKetan");
		f.println();
		f.println("#Read the starting time");
		f.println("DATA_TRANSFER_FROM_GRID_START=`date +%s`");
		f.println();
		f.println("#copy data to Workernode");

		Collection<Object> inputvalues = wfinput.values();
		String nextinput;
		// for (int i = 0; i < wfinput.size(); i++) {
			for (Iterator<Object> iterator = inputvalues.iterator(); iterator.hasNext();) {
				nextinput = (String) iterator.next();
				if (getPart(nextinput, 1).equals("lfn")) {
					f.println("#rm -f " + getPart(nextinput, 2));
					f.println("lcg-cp --vo biomed lfn:" + getPart(nextinput, 2) + " file://$(pwd)/" + getPart(nextinput, 2));
					f.println("if [ $? -ne 0 ]; then lcg-cp --vo biomed lfn:" + getPart(nextinput, 2) + " file://$(pwd)/" + getPart(nextinput, 2));
					f.println("fi;");
					f.println("/bin/sleep 5");
				}
				if (getPart(nextinput, 1).equals("file")) {
					f.println("lcg-cp --vo biomed lfn:" + datanamemap.get(nextinput) + " file://$(pwd)/" + datanamemap.get(nextinput));
					f.println("if [ $? -ne 0 ]; then lcg-cp --vo biomed lfn:" + datanamemap.get(nextinput) + " file://$(pwd)/" + datanamemap.get(nextinput));
					f.println("fi;");
					f.println("/bin/sleep 5");
				}
			}

		f.println("DATA_TRANSFER_FROM_GRID_END=`date +%s`");
		f.println("TIME_TAKEN_FOR_DATA_TRANSFER_FROM_GRID=`expr $DATA_TRANSFER_FROM_GRID_END - $DATA_TRANSFER_FROM_GRID_START`");

		f.println();
		f.println("START=`date +%s`");
		f.println("#export current path and run the executable");
		f.println("export PATH=.:$PATH");
		f.println("/bin/chmod 755 " + glb.getJdlconfigbean().getExecutable());

		// put executable with all ports marked with data as arguments
		f.println(glb.getJdlconfigbean().getExecutable() + " " + innerarg);
		f.println();
		f.println("STOP=`date +%s`");
		f.println("TOTAL=`expr $STOP - $START`");
		f.println("echo \"Total running time: $TOTAL seconds\"");
		f.println("DATA_TRANSFER_TO_GRID_START=`date +%s`");

		f.println("#Treating output ports ...");
		// for (int i = 0; i < wfoutput.size(); i++) {
		for (Iterator<String> iterator = wfoutput.iterator(); iterator.hasNext();) {
			nextinput = (String) iterator.next();
			f.println("#" + getPart(datanamemap.get(nextinput), 1) + " " + getPart(datanamemap.get(nextinput), 2));
			f.println("lcg-cr --vo biomed -l lfn:" + getPart(datanamemap.get(nextinput), 2) + " -d " + glb.getSE() + " file://$(pwd)/" + getPart(datanamemap.get(nextinput), 2));
			f.println("if [ $? -ne 0 ]; then lcg-cr --vo biomed -l lfn:" + getPart(datanamemap.get(nextinput), 2) + " -d " + glb.getSE() + " file://$(pwd)/"
					+ getPart(datanamemap.get(nextinput), 2));
			f.println("fi;");
			f.println("/bin/sleep 5");
		}

		f.println("DATA_TRANSFER_TO_GRID_END=`date +%s`");

		f.println("TIME_TAKEN_FOR_DATA_TRANSFER_TO_GRID=`expr $DATA_TRANSFER_TO_GRID_END - $DATA_TRANSFER_TO_GRID_START`");

		f.println("TOTAL_DATA_TRANSFER_TIME=`expr $TIME_TAKEN_FOR_DATA_TRANSFER_FROM_GRID + $TIME_TAKEN_FOR_DATA_TRANSFER_TO_GRID`");

		f.println("echo \"Total data transfer time: $TOTAL_DATA_TRANSFER_TIME seconds.\"");
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

	private static String createJDL(gLiteActivityConfigurationBean glb, String wrappername, String wrapperarg) throws IOException {

		File jdlfile = new File("/tmp/", System.currentTimeMillis() + ".jdl");
		PrintWriter f = new PrintWriter(new FileWriter(jdlfile));
		// add other properties line by line to the file
		f.println("Type=\"" + glb.getJdlconfigbean().getType() + "\";");
		f.println("JobType=\"" + glb.getJdlconfigbean().getJobType() + "\";");
		// add nodenumber for MPI jobs
		if (glb.getJdlconfigbean().getJobType().equals("MPICH"))
			f.println("NodeNumber=" + glb.getJdlconfigbean().getNodeNumber() + ";");
		// f.println("Executable=\"" + glb.getJdlconfigbean().getWrapper() +
		// "\";");
		f.println("Executable=\"" + wrappername + "\";");
		// f.println("Arguments=\"" +
		// glb.getJdlconfigbean().getWrapperArguments() + "\";");
		f.println("Arguments=\"" + wrapperarg + "\";");
		f.println("Stdoutput=\"" + glb.getJdlconfigbean().getStdOut() + "\";");
		f.println("StdError=\"" + glb.getJdlconfigbean().getStdErr() + "\";");
		// f.println("InputSandbox={" + glb.getJdlconfigbean().getInputSandbox()
		// + ",\"" + glb.getJdlconfigbean().getWrapper() + "\"};");
		f.println("InputSandbox={" + glb.getJdlconfigbean().getInputSandbox() + ",\"" + wrappername + "\"};");
		f.println("OutputSandbox={" + glb.getJdlconfigbean().getOutputSandbox() + "};");
		f.println("RetryCount=" + glb.getJdlconfigbean().getRetryCount() + ";");
		f.println("ShallowRetryCount=3;");
		f.println("Requirements=" + glb.getJdlconfigbean().getRequirements() + ";");
		f.println("Rank=(-other.GlueCEStateEstimatedResponseTime);");
		f.println();
		// close file
		f.close();
		// return its path
		return jdlfile.getAbsolutePath();
	}

	private static String getRandomString() {
		return java.util.UUID.randomUUID().toString().substring(0, 5);
	}

	public Object get(OutputPort outputPort) {
		return datanamemap.get(outputPort.getName());
	}

	public void showStuff() {
		// TODO Auto-generated method stub
		System.out.println(this.datanamemap.size());
	}

}
