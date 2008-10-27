//Activity ConfigAction!!
package action;

import gLite.gLiteActivity;
import gLite.gLiteActivityConfigurationBean;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import view.gLiteConfigView;

/*
 * GUI part of the configuration.
 */
@SuppressWarnings("serial")
public class gLiteActivityConfigurationAction extends ActivityConfigurationAction<gLiteActivity, gLiteActivityConfigurationBean>{
	
	private final Frame owner;
	
	public gLiteActivityConfigurationAction(gLiteActivity activity, Frame owner) {
		super(activity);
		this.owner = owner;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		final gLiteConfigView gliteconfigview = new gLiteConfigView((gLiteActivity)getActivity());
		final JDialog dialog = new JDialog(owner,true);
		dialog.add(gliteconfigview);
		dialog.setSize(550,450);
		gliteconfigview.setButtonClickedListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (gliteconfigview.isConfigurationChanged()) {
					configureActivity(gliteconfigview.getConfiguration());
				}
				dialog.setVisible(false);
			}
			
		});
		dialog.setVisible(true);
		
	}
}
