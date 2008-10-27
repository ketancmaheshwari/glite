package view;

import gLite.gLiteActivity;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

public class gLiteActivityViewFactory implements ContextualViewFactory<gLiteActivity>{
	public boolean canHandle(Object object) {
		//This is here for the sake of it until i make out what the heck does it solves!
		return object.getClass().isAssignableFrom(gLiteActivity.class);
	}

	public gLiteContextualView getView(gLiteActivity activity) {
		gLiteContextualView view = new gLiteContextualView(activity);
		return view;
	}


}
