package view;

import java.awt.Frame;
import javax.swing.Action;

import action.gLiteActivityConfigurationAction;
import gLite.gLiteActivity;
import gLite.gLiteActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

@SuppressWarnings("serial")
public class gLiteContextualView extends
HTMLBasedActivityContextualView<gLiteActivityConfigurationBean>{

	public gLiteContextualView(Activity<?> activity) {
		super(activity);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getRawTableRowsHtml() {
		String html = "<tr><th>Input Port Name</th><th>Depth</th></tr>";
		for (ActivityInputPortDefinitionBean bean : getConfigBean()
				.getInputPortDefinitions()) {
			html = html + "<tr><td>" + bean.getName() + "</td><td>"
					+ bean.getDepth() + "</td></tr>";
		}
		html = html
				+ "<tr><th>Output Port Name</th><th>Depth</th><th>Granular Depth</th></tr>";
		for (ActivityOutputPortDefinitionBean bean : getConfigBean()
				.getOutputPortDefinitions()) {
			html = html + "<tr></td>" + bean.getName() + "</td><td>"
					+ bean.getDepth() + "</td><td>" + bean.getGranularDepth()
					+ "</td></tr>";
		}
		return html;
	}

	@Override
	protected String getViewTitle() {
		return "gLite Contextual View";
	}
	
	public Action getConfigureAction(Frame owner) {
		return new gLiteActivityConfigurationAction(
				(gLiteActivity) getActivity(), owner);
	}

}
