package gLite;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

/**
 * An Activity providing glite functionality. This is the place where the logic
 * of submitting a job and getting its status back resides...
 * 
 */

public class gLiteActivity extends AbstractAsynchronousActivity<gLiteActivityConfigurationBean> {

	private gLiteActivityConfigurationBean configurationBean;

	@Override
	public void configure(gLiteActivityConfigurationBean configurationBean) throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		configurePorts(configurationBean);
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
				
				gLitExecutor glitexecutor = new gLitExecutor();
				try{
					
				for (String inputName : data.keySet()) {
					ActivityInputPort inputPort = getInputPort(inputName);
					Object input = referenceService.renderIdentifier(data.get(inputName), inputPort.getTranslatedElementClass(), callback.getContext());
					inputName = sanatisePortName(inputName);
					//set input
						glitexecutor.setInParams(inputName, input);
				}
				
				//Transfer data to the ui
				glitexecutor.dataTransfer(configurationBean);
				
				//generate the names of outputports and provide to the glitexecutor
				for (OutputPort outputPort : getOutputPorts()) {
					Object value="lfn:" + getRandomString();
					glitexecutor.setOutParams(outputPort,value);
					
				}
				
				// run
				glitexecutor.execute(configurationBean);
				
				// register outputs
				for (OutputPort outputPort : getOutputPorts()) {
					//datanamemap.put(outputPort.getName(), "lfn:" + getRandomString());
					//value = datanamemap.get(outputPort.getName());
					String name=outputPort.getName();
					Object value=glitexecutor.get(outputPort);
					if (value != null) {
						outputData.put(name, referenceService.register(value, outputPort.getDepth(), true, callback.getContext()));
					}
					// clear outputs
					// TODO
				}
				// send result to the callback
				callback.receiveResult(outputData, new int[0]);
			} catch (ReferenceServiceException e) {
				callback.fail(
						"Error " + this, e);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
			
			/**
			 * Removes any invalid characters from the port name. For
			 * example, xml-text would become xmltext.
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

	private static String getRandomString() {
		return java.util.UUID.randomUUID().toString().substring(0, 5);
	}

}
