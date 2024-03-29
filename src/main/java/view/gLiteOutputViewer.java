package view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.MimeTypeConfig;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

public class gLiteOutputViewer extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The bean which defines this view */
	private ActivityOutputPortDefinitionBean bean;
	/** The name of the port */
	private JTextField nameField;
	/** The depth of the port */
	private JSpinner depthSpinner;
	/** The granular depth of the port */
	private JSpinner granularDepthSpinner;
	/** The mime types which the output port can handle */
	private JTextArea mimeTypeText;
	/** Whether the values in the bean can be edited */
	private boolean editable;
	private JPanel mimeTypePanel;
	private MimeTypeConfig mimeTypeConfig;
	private JButton addMimeButton;
	private JFrame mimeFrame;

	/**
	 * Sets the look and feel of the view through {@link #initView()} and sets
	 * the edit state using {@link #editable}
	 * 
	 * @param bean
	 *            One of the output ports of the overall activity
	 * @param editable
	 *            whether the values of the bean are editable
	 */
	public gLiteOutputViewer(ActivityOutputPortDefinitionBean bean,
			boolean editable) {
		this.bean = bean;
		this.editable = editable;
		setBorder(javax.swing.BorderFactory.createEtchedBorder());
		initView();
		setEditable(editable);
	}

	/**
	 * Uses {@link GridBagLayout} for the layout. Adds components to edit the
	 * name, depth and granular depth
	 */
	private void initView() {
		setLayout(new GridBagLayout());
		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;
		outerConstraint.weighty = 0;
		outerConstraint.weightx = 0.1;
		outerConstraint.fill = GridBagConstraints.BOTH;

		nameField = new JTextField(bean.getName());
		add(nameField, outerConstraint);

		outerConstraint.gridx = 1;
		SpinnerNumberModel depthModel = new SpinnerNumberModel(new Integer(bean
				.getDepth()), new Integer(0), new Integer(100), new Integer(1));
		depthSpinner = new JSpinner(depthModel);
		// depthSpinner.setValue(bean.getDepth());
		add(depthSpinner, outerConstraint);

		outerConstraint.gridx = 2;
		SpinnerNumberModel granularModel = new SpinnerNumberModel(new Integer(
				bean.getDepth()), new Integer(0), new Integer(100),
				new Integer(1));
		granularDepthSpinner = new JSpinner(granularModel);
		// granularDepthSpinner.setValue(bean.getGranularDepth());
		add(granularDepthSpinner, outerConstraint);

		outerConstraint.gridx = 3;
		
		outerConstraint.gridx = 4;
		mimeFrame = new JFrame();
		mimeTypeConfig = new MimeTypeConfig();
		if (bean.getMimeTypes() !=null) {
			mimeTypeConfig.setMimeTypeList(bean.getMimeTypes());
		}
		mimeFrame.add(mimeTypeConfig);
		mimeTypeConfig.setVisible(true);
		addMimeButton = new JButton("Add mime type");
//		addMimeButton.addActionListener(new AbstractAction() {
//
//			public void actionPerformed(ActionEvent e) {
//				mimeFrame.setVisible(true);
//			}
//			
//		});
		add(addMimeButton, outerConstraint);
	}

	/**
	 * Get the component which edits the name of the
	 * {@link ActivityOutputPortDefinitionBean}
	 * 
	 * @return
	 */
	public JTextField getNameField() {
		return nameField;
	}

	/**
	 * The component which allows the depth of the
	 * {@link ActivityOutputPortDefinitionBean} to be changed
	 * 
	 * @return
	 */
	public JSpinner getDepthSpinner() {
		return depthSpinner;
	}

	/**
	 * The component which allows the granular depth of the
	 * {@link ActivityOutputPortDefinitionBean} to be changed
	 * 
	 * @return
	 */
	public JSpinner getGranularDepthSpinner() {
		return granularDepthSpinner;
	}

	/**
	 * The mime types which are handled by this
	 * {@link ActivityOutputPortDefinitionBean}
	 * 
	 * @return
	 */
	public JTextArea getMimeTypeText() {
		return mimeTypeText;
	}

	/**
	 * The actual {@link ActivityOutputPortDefinitionBean} described by this
	 * view
	 * 
	 * @return
	 */
	public ActivityOutputPortDefinitionBean getBean() {
		return bean;
	}

	/**
	 * Can the bean be edited by this view?
	 * 
	 * @return
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * Set the editable state of the view
	 * 
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
		setEditMode();
	}

	/**
	 * Sets the depth, granular depth and name components to be editable
	 */
	public void setEditMode() {
		// this.addMimeTypeButton.setVisible(editable);
		// this.mimeDropList.setVisible(editable);
		this.depthSpinner.setEnabled(editable);
		this.granularDepthSpinner.setEnabled(editable);
		this.nameField.setEditable(editable);

	}

	/**
	 * The panel which has all the mime type components in it
	 * 
	 * @return
	 */
	public JPanel getMimeTypePanel() {
		return mimeTypePanel;
	}

	public MimeTypeConfig getMimeTypeConfig() {
		return mimeTypeConfig;
	}

	public JButton getAddMimeButton() {
		return addMimeButton;
	}

	public JFrame getMimeFrame() {
		return mimeFrame;
	}

}
