package gLite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import view.gLiteConfigView;

/**
 * Unit tests for gliteActivity.
 * 
 */
public class gLiteActivityTest {

	private gLiteActivity activity;

	private gLiteActivityConfigurationBean configurationBean;

	@Before
	public void setUp() throws Exception {
		activity = new gLiteActivity();
		configurationBean = new gLiteActivityConfigurationBean();
		configurationBean.setJdlconfigbean(new JDLConfigBean());
		ActivityInputPortDefinitionBean inputPortBean = new ActivityInputPortDefinitionBean();
		inputPortBean.setDepth(0);
		inputPortBean.setName("example_input");
		inputPortBean.setHandledReferenceSchemes(new ArrayList<Class<? extends ExternalReferenceSPI>>());
		inputPortBean.setTranslatedElementType(String.class);
		inputPortBean.setAllowsLiteralValues(true);
		configurationBean.setInputPortDefinitions(Collections.singletonList(inputPortBean));
		
		ActivityOutputPortDefinitionBean outputPortBean = new ActivityOutputPortDefinitionBean();
		outputPortBean.setDepth(0);
		outputPortBean.setName("example_output");
		outputPortBean.setMimeTypes(new ArrayList<String>());
		configurationBean.setOutputPortDefinitions(Collections.singletonList(outputPortBean));
		
		
		configurationBean.setWMProxy("https://egee-wms-01.cnaf.infn.it:7443/glite_wms_wmproxy_server");
		//configurationBean.setWMProxy("https://wms01.egee-see.org:7443/glite_wms_wmproxy_server");
		configurationBean.setDelegationID("501");
		configurationBean.setCaDir("/etc/grid-security/certificates");
		configurationBean.setProxyPath("/tmp/x509up_u501");
		configurationBean.setVOMSDir("/home/ketan/glite-ui/glite/etc/vomses");
		configurationBean.setVOMSCertDir("/etc/grid-security/vomsdir/");
		configurationBean.setVO("biomed");
		configurationBean.setWMSDir("/home/ketan/glite-ui/glite/etc/wms/biomed");
		//configurationBean.setJDLPath("/home/ketan/jlite/test/test.jdl");
		configurationBean.setPollFrequency("10000");
		
		configurationBean.getJdlconfigbean().setType("Job");
		configurationBean.getJdlconfigbean().setJobType("Normal");
		configurationBean.getJdlconfigbean().setExecutable("/bin/echo");
		configurationBean.getJdlconfigbean().setArguments("Hello");
		configurationBean.getJdlconfigbean().setStdOut("std.out");
		configurationBean.getJdlconfigbean().setStdErr("std.err");
		configurationBean.getJdlconfigbean().setInputSandbox("\"blah.txt\"");
		configurationBean.getJdlconfigbean().setOutputSandbox("\"std.out\",\"std.err\"");
		configurationBean.getJdlconfigbean().setRetryCount("3");
		configurationBean.getJdlconfigbean().setRequirements("(other.GlueCEStateStatus == \"Production\") && RegExp(\"nl\",other.GlueCEUniqueId)");
		configurationBean.getJdlconfigbean().setNodeNumber("0");
		configurationBean.getJdlconfigbean().setInputsPath("/home/ketan/ManchesterWork/gliteworkflows/inputs/");
		//gLiteConfigView.createWrapper(configurationBean);
		//configurationBean.setJDLPath(gLiteConfigView.createJDL(configurationBean));
		
	}

	@Test
	public void testgliteActivity() {
		assertNotNull(new gLiteActivity());
	}

	@Ignore
	@Test
	public void testExecuteAsynch() throws Exception {
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("example_input", "local:string:mystring");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("output", String.class);

		activity.configure(configurationBean);
		//activity.executeAsynch(data, callback);
		Map<String,Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		//assertTrue(outputs.containsKey("example_output"));
		//assertEquals("hello_example", outputs.get("example_output"));
	}

	@Test
	public void testConfiguregliteActivityConfigurationBean() throws Exception {
		Set<String> expectedInputs = new HashSet<String>();
		expectedInputs.add("example_input");
		Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("example_output");

		activity.configure(configurationBean);
		
		Set<ActivityInputPort> inputPorts = activity.getInputPorts();
	/*	assertEquals(expectedInputs.size(), inputPorts.size());
		for (ActivityInputPort inputPort : inputPorts) {
			assertTrue("Wrong output : " + inputPort.getName(),
					expectedInputs.remove(inputPort.getName()));
		}*/
		
		Set<OutputPort> outputPorts = activity.getOutputPorts();
//		assertEquals(expectedOutputs.size(), outputPorts.size());
		/*for (OutputPort outputPort : outputPorts) {
			assertTrue("Wrong output : " + outputPort.getName(),
					expectedOutputs.remove(outputPort.getName()));
		}*/
	}

}
