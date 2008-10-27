package gLite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

//import org.glite.voms.contact.VOMSServerMap;
/**
 * An Activity providing glite functionality. This is the place where the logic
 * of submitting a job and getting its status back resides...
 * 
 */
public class gLiteActivity extends
		AbstractAsynchronousActivity<gLiteActivityConfigurationBean> {
	private static	Object input;
	private gLiteActivityConfigurationBean configurationBean;
	private String outputDir;
	private static String[]  wfinput;
	@Override
	public void configure(gLiteActivityConfigurationBean configurationBean)
			throws ActivityConfigurationException {
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
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				ReferenceService referenceService = callback.getContext()
						.getReferenceService();

				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();
				try {
					// set inputs
					int i=0;
					for (String inputName : data.keySet()) {
						ActivityInputPort inputPort = getInputPort(inputName);
						input = referenceService.renderIdentifier(data
								.get(inputName), inputPort
								.getTranslatedElementClass(), callback
								.getContext());
						inputName = sanatisePortName(inputName);
						  wfinput[i]=input.toString();
						//If inputName starts with 'file' call commands to transfer it to ui
						if(input.toString().substring(0, 4).equals("file")){
							wfinput[i]=input.toString().substring(4);
							Runtime.getRuntime().exec("scp /home/ketan/ManchesterWork/gliteworkflows/inputs/"+ wfinput+ " glite.unice.fr:");
							//Transfer this to grid
							Runtime.getRuntime().exec("ssh glite.unice.fr lcg-del -a lfn:"+wfinput[i]);
							//Runtime.getRuntime().exec("ssh glite.unice.fr lcg-cr --vo biomed -l lfn:" +wfinput+ " -d tbn15.nikhef.nl file://`pwd`/"+ wfinput);
							Runtime.getRuntime().exec("ssh glite.unice.fr lcg-cr --vo biomed -l lfn:" +wfinput[i]+ " -d prod-se-01.pd.infn.it file://`pwd`/"+ wfinput[i]);
						}
						i++;
					}
					
					configurationBean.getJdlconfigbean().setWrapper(createWrapper(configurationBean));
					configurationBean.setJDLPath(createJDL(configurationBean));
					
					
					// run the activity
					/*
					 * ----------------------------------------- Step 1.
					 * Configure and create Grid session
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
					config.addWMProxy(configurationBean.getVO(),
							configurationBean.getWMProxy());
					config.setProxyPath(configurationBean.getProxyPath());
					// create Grid session
					GridSession session = GridSessionFactory.create(config);

					/*
					 * ---------------------------------------------- Step 2.
					 * Create user proxy (12 hours)
					 * ----------------------------------------------
					 */
					GlobusCredential proxy = new GlobusCredential(
							configurationBean.getProxyPath());
					System.out.println("Created user proxy: "
							+ proxy.getSubject());

					/*
					 * --------------------------------------------- Step 3.
					 * Delegate user proxy to WMProxy server
					 * ---------------------------------------------
					 */
					session.delegateProxy(configurationBean.getDelegationID());

					/*
					 * ---------------------------- Step 4. Load job description
					 * ----------------------------
					 */
					JobAd jad = new JobAd();
					jad.fromFile(configurationBean.getJDLPath());
					String jdl = jad.toString();

					/*
					 * -------------------------- Step 5. Submit job to grid
					 * --------------------------
					 */
					// String jobId =
					// session.submitJob(jdl,"/home/ketan/jlite/test/input");
					String jobId = session.submitJob(jdl,configurationBean.getJdlconfigbean().getInputsPath());
					//String jobId = session.submitJob(jdl,"/home/ketan/ManchesterWork/gliteworkflows/inputs/");
					System.out.println("Started job: " + jobId);

					/*
					 * -------------------------- Step 6. Monitor job status
					 * --------------------------
					 */
					String jobState = "";
					do {
						Thread.sleep(Long.parseLong(configurationBean.getPollFrequency()));
						jobState = session.getJobState(jobId);
						System.out.println("Job status: " + jobState);
					} while (!jobState.equals("DONE")
							&& !jobState.equals("ABORTED"));

					/*
					 * --------------------------- Step 7. Download job output
					 * ---------------------------
					 */
					if (jobState.equals("DONE")) {
						System.out.println("output path="+configurationBean.getOutputPath());
						outputDir = configurationBean.getOutputPath()
								+ Util.getShortJobId(jobId);
						System.out.println("outputdir= "+outputDir);
						session.getJobOutput(jobId, outputDir);
						System.out.println("Job output is downloaded to: "
								+ outputDir);
					}
					// String input = null;
					// register outputs
					for (OutputPort outputPort : getOutputPorts()) {
						Object value = null;
						String name = outputPort.getName();
						if (name.equals("jobstatus"))
							value = jobState;
						if (name.equals("dataout")){
							//value=the first line of outputDir/outdata.txt
							String outdatafilename=outputDir+File.separator+"outdata.txt";
							BufferedReader in = new BufferedReader(new FileReader(outdatafilename));
							value = in.readLine();
							in.close();
						}
						if (value != null) {
							outputData.put(name, referenceService.register(
									value, outputPort.getDepth(), true,
									callback.getContext()));
						}
						System.out.println("Output port's name is " + name + " and value is "+ value.toString());
						// clear outputs
						// TODO
					}
					// send result to the callback
					callback.receiveResult(outputData, new int[0]);
				} catch (Exception e) {
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
	public static String createJDL(gLiteActivityConfigurationBean glb) throws IOException{
		
		// create a file with that name in /tmp
		File jdlfile = new File("/tmp/",System.currentTimeMillis()+".jdl");
		PrintWriter f = new PrintWriter(new FileWriter(jdlfile));
		// add other properties line by line to the file
		f.println("Type=\""+glb.getJdlconfigbean().getType()+"\";");
		f.println("JobType=\""+glb.getJdlconfigbean().getJobType()+"\";");
		//add nodenumber for MPI jobs
		if (glb.getJdlconfigbean().getJobType().equals("MPICH"))
			f.println("NodeNumber="+glb.getJdlconfigbean().getNodeNumber()+";");
		f.println("Executable=\""+glb.getJdlconfigbean().getWrapper()+"\";");
		f.println("Arguments=\""+glb.getJdlconfigbean().getArguments()+"\";");
		f.println("Stdoutput=\""+glb.getJdlconfigbean().getStdOut()+"\";");
		f.println("StdError=\""+glb.getJdlconfigbean().getStdErr()+"\";");
		f.println("InputSandbox={"+glb.getJdlconfigbean().getInputSandbox()+",\""+glb.getJdlconfigbean().getWrapper()+"\"};");
		f.println("OutputSandbox={"+glb.getJdlconfigbean().getOutputSandbox()+",\"outdata.txt\"};");
		f.println("RetryCount="+glb.getJdlconfigbean().getRetryCount()+";");
		f.println("Requirements="+glb.getJdlconfigbean().getRequirements()+";");
		f.println("Rank=(-other.GlueCEStateEstimatedResponseTime);");
		f.println();
		//close file
		f.close();
		// return this path
		return jdlfile.getAbsolutePath();
	}
	
	public static String createWrapper(gLiteActivityConfigurationBean glb) throws IOException{
		File wrapperfile = new File(glb.getJdlconfigbean().getInputsPath(),"wrapper_"+System.currentTimeMillis()+".sh");
		PrintWriter f = new PrintWriter(new FileWriter(wrapperfile));
		f.println("#!/bin/sh");
		f.println("export LFC_HOME=lfc-biomed.in2p3.fr:/grid/biomed/testKetan");
		f.println();
		f.println("#Read the starting time");
		f.println("START=`date +%s`");
		f.println();
		f.println("#copy data to Workernode");

		for(int i=0;i<wfinput.length;i++){
			f.println("rm -f "+wfinput[i]);
			f.println("lcg-cp --vo biomed lfn:"+wfinput[i]+" file://$(pwd)/"+wfinput[i]);	
		}
		
		f.println();
		f.println("#export current path and run the executable");
		f.println("export PATH=.:$PATH");
		f.println("/bin/chmod 755 "+glb.getJdlconfigbean().getExecutable());
		f.println(glb.getJdlconfigbean().getExecutable()+" "+ "$*");
		f.println();
		for(int i=0;i<wfinput.length;i++){
			f.println("lcg-del --vo biomed -a lfn:"+wfinput[i]);
			//introduce a delay for updation of the lcg catalogue
			f.println("/bin/sleep 3");
			//f.println("lcg-cr --vo biomed -l lfn:"+wfinput+" -d tbn15.nikhef.nl file://$(pwd)/"+wfinput);
			f.println("lcg-cr --vo biomed -l lfn:"+wfinput[i]+" -d prod-se-01.pd.infn.it file://$(pwd)/"+wfinput[i]);
		}
		f.println("#Create the file outdata.txt");
		f.println("touch outdata.txt");
		//copy the outputdata link to this file
		f.println("echo "+wfinput+" >outdata.txt");
		f.println();
		f.println("#Read the ending time");
		f.println("STOP=`date +%s`");
		f.println("#Compute and print the difference which will be the execution time");
		f.println("TOTAL=`expr $STOP - $START`");
		f.println("echo \"Total running time: $TOTAL seconds\"");
		f.println();
		f.close();
		return wrapperfile.getName();
	}
}
