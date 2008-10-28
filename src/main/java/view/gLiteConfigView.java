package view;

import gLite.JDLConfigBean;
import gLite.gLiteActivity;
import gLite.gLiteActivityConfigurationBean;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.help.CSH;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.embl.ebi.escience.scufl.InputPort;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

/**
 * 
 * @author ketan
 */

@SuppressWarnings("serial")
public class gLiteConfigView extends JPanel {
	/** A List of views over the input ports */
	private List<gLiteInputViewer> inputViewList;
	/** A List of views over the output ports */
	private List<gLiteOutputViewer> outputViewList;
	/** The activity which this view describes */
	private gLiteActivity activity;
	/** the configuration bean used to configure the activity */
	private gLiteActivityConfigurationBean configuration;
	private gLiteActivityConfigurationBean configBean;
	
	/**
	 * Holds the state of the OK button in case a parent view wants to know
	 * whether the configuration is finished
	 */
	private ActionListener buttonClicked;
	/** Remembers where the next input should be placed in the view */
	private int inputGridy;
	/**
	 * An incremental name of newInputPort + this number is used to name new
	 * ports
	 */
	private int newInputPortNumber = 0;
	/**
	 * An incremental name of newOutputPort + this number is used to name new
	 * ports
	 */
	private int newOutputPortNumber = 0;
	/** Remembers where the next output should be placed in the view */
	private int outputGridy;
	/** Parent panel for the outputs */
	private JPanel outerOutputPanel;
	/** parent panel for the inputs */
	private JPanel outerInputPanel;
	private JButton button;

	private boolean configChanged = false;

	/**
	 * Stores the {@link gLiteActivity}, gets its
	 * {@link gLiteActivityConfigurationBean}, sets the layout and calls
	 * {@link #initialise()} to get the view going
	 * 
	 * @param activity
	 *            the {@link gLiteActivity} that the view is over
	 */
	public gLiteConfigView(gLiteActivity activity) {
		this.activity = activity;
		configuration = activity.getConfiguration();
		setLayout(new GridBagLayout());
		initialise();
	}

	public gLiteActivityConfigurationBean getConfiguration() {
		return configuration;
	}

	public boolean isConfigurationChanged() {
		return configChanged;
	}

	/**
	 * Adds a {@link JButton} which handles the reconfiguring of the
	 * {@link gLiteActivity} through the altered
	 * {@link gLiteActivityConfigurationBean}. Sets up the initial tabs - Script
	 * (also sets the initial value), Ports & Dependencies and their initial
	 * values through {@link #setDependencies()}, {@link #setPortPanel()}
	 */
	private void initialise() {
		CSH.setHelpIDString(this, "view.gLiteConfigView");

		setSize(300, 300);
		AbstractAction okAction = getOKAction();
		button = new JButton(okAction);
		button.setText("OK");
		button.setToolTipText("Click to configure with the new values");

		inputViewList = new ArrayList<gLiteInputViewer>();
		outputViewList = new ArrayList<gLiteOutputViewer>();
		setBorder(javax.swing.BorderFactory.createTitledBorder(null, null, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		// These are components for the Propertypanel
		/*
		 * Set the tab for jdl script panel
		 */
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Properties", setPropertiesPanel());
		tabbedPane.addTab("Ports", setPortPanel());
		// tabbedPane.addTab("jdl", JDLEditPanel);
		tabbedPane.addTab("jdl", setJDLPanel());

		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;

		outerConstraint.fill = GridBagConstraints.BOTH;
		outerConstraint.weighty = 0.1;
		outerConstraint.weightx = 0.1;
		add(tabbedPane, outerConstraint);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		buttonPanel.add(button);
		JButton cancelButton = new JButton(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				configChanged = false;
				buttonClicked.actionPerformed(e);
			}
		});

		outerConstraint.gridx = 0;
		outerConstraint.gridy = 1;
		outerConstraint.fill = GridBagConstraints.NONE;
		outerConstraint.anchor = GridBagConstraints.LINE_END;
		outerConstraint.gridy = 2;
		outerConstraint.weighty = 0;
		cancelButton.setText("Cancel");
		buttonPanel.add(cancelButton);
		add(buttonPanel, outerConstraint);
	}

	/**
	 * Creates a {@link JTabbedPane} with the Output and Input ports
	 * 
	 * @return a {@link JTabbedPane} with the ports
	 */
	private JTabbedPane setPortPanel() {
		JTabbedPane ports = new JTabbedPane();

		JPanel portEditPanel = new JPanel(new GridLayout(0, 2));

		GridBagConstraints panelConstraint = new GridBagConstraints();
		panelConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		panelConstraint.gridx = 0;
		panelConstraint.gridy = 0;
		panelConstraint.weightx = 0.1;
		panelConstraint.weighty = 0.1;
		panelConstraint.fill = GridBagConstraints.BOTH;

		JScrollPane inputScroller = new JScrollPane(setInputPanel());
		portEditPanel.add(inputScroller, panelConstraint);

		panelConstraint.gridy = 1;
		ports.add("Inputs Ports", inputScroller);
		JScrollPane outputScroller = new JScrollPane(setOutputPanel());
		portEditPanel.add(outputScroller, panelConstraint);
		ports.add("Output Ports", outputScroller);

		return ports;
	}

	private JPanel setPropertiesPanel() {
		JPanel propertiesPanel = new JPanel();

		configBean = activity.getConfiguration();

		jLabelVO = new javax.swing.JLabel();
		jLabelCADir = new javax.swing.JLabel();
		jLabelVOMSDir = new javax.swing.JLabel();
		jLabelVOMSCertDir = new javax.swing.JLabel();
		jLabelProxyPath = new javax.swing.JLabel();
		jLabelDelegationID = new javax.swing.JLabel();
		jLabelWMProxyURL = new javax.swing.JLabel();
		jLabelWMSDir = new javax.swing.JLabel();
		jLabelOutputPath = new javax.swing.JLabel();
		jLabelPollFrequency = new javax.swing.JLabel();

		jTextFieldVO = new javax.swing.JTextField();
		jTextFieldCADir = new javax.swing.JTextField();
		jTextFieldVOMSDir = new javax.swing.JTextField();
		jTextFieldVOMSCertDir = new javax.swing.JTextField();
		jTextFieldProxyPath = new javax.swing.JTextField();
		jTextFieldDelegationID = new javax.swing.JTextField();
		jTextFieldProxyURL = new javax.swing.JTextField();
		jTextFieldWMSDir = new javax.swing.JTextField();
		jTextFieldOutputPath = new javax.swing.JTextField();
		jTextFieldPollFrequency = new javax.swing.JTextField();

		jLabelVO.setText("VO");

		jLabelCADir.setText("CADir");

		jLabelVOMSDir.setText("VOMSDir");

		jLabelVOMSCertDir.setText("VOMSCertDir");

		jLabelProxyPath.setText("ProxyPath");

		jLabelDelegationID.setText("DelegationID");

		jLabelWMProxyURL.setText("WMProxyURL");

		jLabelWMSDir.setText("WMSDir");

		jLabelOutputPath.setText("Output Path:");

		jLabelPollFrequency.setText("Poll Frequency");

		jTextFieldWMSDir.setColumns(30);

		jTextFieldVO.setColumns(30);

		jTextFieldCADir.setColumns(30);

		jTextFieldVOMSDir.setColumns(30);

		jTextFieldVOMSCertDir.setColumns(30);

		jTextFieldProxyPath.setColumns(30);

		jTextFieldDelegationID.setColumns(30);

		jTextFieldProxyURL.setColumns(30);

		jTextFieldOutputPath.setColumns(30);

		jTextFieldPollFrequency.setColumns(30);

		GroupLayout layout = new GroupLayout(propertiesPanel);
		propertiesPanel.setLayout(layout);

		jTextFieldVO.setText(configBean.getVO());
		jTextFieldVO.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.setVO(jTextFieldVO.getText());
			}
		});

		jTextFieldCADir.setText(configBean.getCaDir());
		jTextFieldCADir.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.setCaDir(jTextFieldCADir.getText());
			}
		});

		jTextFieldVOMSDir.setText(configBean.getVOMSDir());
		jTextFieldVOMSDir.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.setVOMSDir(jTextFieldVOMSDir.getText());
			}
		});

		jTextFieldVOMSCertDir.setText(configBean.getVOMSCertDir());
		jTextFieldVOMSCertDir.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.setVOMSCertDir(jTextFieldVOMSCertDir.getText());
			}
		});

		jTextFieldProxyPath.setText(configBean.getProxyPath());
		jTextFieldProxyPath.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.setProxyPath(jTextFieldProxyPath.getText());
			}
		});

		jTextFieldDelegationID.setText(configBean.getDelegationID());
		jTextFieldDelegationID.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.setDelegationID(jTextFieldDelegationID.getText());
			}
		});

		jTextFieldProxyURL.setText(configBean.getWMProxy());
		jTextFieldProxyURL.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.setWMProxy(jTextFieldProxyURL.getText());
			}
		});

		jTextFieldWMSDir.setText(configBean.getWMSDir());
		jTextFieldWMSDir.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				configBean.setWMSDir(jTextFieldWMSDir.getText());
			}
		});

		jTextFieldOutputPath.setText(configBean.getOutputPath());
		jTextFieldOutputPath.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				configBean.setOutputPath(jTextFieldOutputPath.getText());
			}
		});

		jTextFieldPollFrequency.setText(configBean.getPollFrequency());
		jTextFieldPollFrequency.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.setPollFrequency(jTextFieldPollFrequency.getText());
			}
		});

		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addContainerGap(15, Short.MAX_VALUE).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createSequentialGroup().addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jLabelWMProxyURL).addComponent(jLabelProxyPath)
												.addComponent(jLabelVOMSCertDir).addComponent(jLabelVOMSDir).addComponent(jLabelCADir).addComponent(jLabelVO).addComponent(
														jLabelDelegationID)).addGap(38, 38, 38).addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jTextFieldDelegationID,
												javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jTextFieldVO, javax.swing.GroupLayout.Alignment.TRAILING,
												javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(
												jTextFieldCADir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jTextFieldVOMSDir,
												javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jTextFieldVOMSCertDir, javax.swing.GroupLayout.Alignment.TRAILING,
												javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(
												jTextFieldProxyPath, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jTextFieldProxyURL,
												javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))).addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createSequentialGroup().addComponent(jLabelWMSDir).addGap(38, 38, 38).addComponent(jTextFieldWMSDir, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createSequentialGroup().addComponent(jLabelOutputPath).addGap(38, 38, 38).addComponent(jTextFieldOutputPath,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createSequentialGroup().addComponent(jLabelPollFrequency).addGap(38, 38, 38).addComponent(jTextFieldPollFrequency,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()));

		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] { jTextFieldCADir, jTextFieldDelegationID, jTextFieldProxyPath, jTextFieldVO,
				jTextFieldVOMSCertDir });

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addContainerGap().addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldVO, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelVO)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldCADir, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelCADir)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldVOMSDir, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelVOMSDir)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldVOMSCertDir, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelVOMSCertDir)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldProxyPath, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelProxyPath)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldDelegationID, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelDelegationID)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldProxyURL, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelWMProxyURL)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldWMSDir, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelWMSDir)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldOutputPath, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelOutputPath)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldPollFrequency, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelPollFrequency)).addContainerGap(27,
						Short.MAX_VALUE)));

		layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] { jTextFieldCADir, jTextFieldDelegationID, jTextFieldProxyPath, jTextFieldVO,
				jTextFieldVOMSCertDir });

		return propertiesPanel;
	}

	private JPanel setJDLPanel() {
		JPanel JDLPanel = new JPanel();
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(JDLPanel);
		JDLPanel.setLayout(layout);

		jLabelOutputSandbox = new javax.swing.JLabel();
		jLabelStdErr = new javax.swing.JLabel();
		jLabelStdOut = new javax.swing.JLabel();
		jLabelNodes = new javax.swing.JLabel();
		jLabelJobType = new javax.swing.JLabel();
		jLabelType = new javax.swing.JLabel();
		jLabelInputSandbox = new javax.swing.JLabel();
		jTextFieldInputSandbox = new javax.swing.JTextField();
		jTextFieldType = new javax.swing.JTextField();
		jTextFieldJobType = new javax.swing.JTextField();
		jTextFieldNodes = new javax.swing.JTextField();
		jTextFieldStdOut = new javax.swing.JTextField();
		jTextFieldStdErr = new javax.swing.JTextField();
		jTextFieldOutputSandbox = new javax.swing.JTextField();
		jLabelExecutable = new javax.swing.JLabel();
		jTextFieldExecutable = new javax.swing.JTextField();
		jLabelRequirements = new javax.swing.JLabel();
		jTextFieldRequirements = new javax.swing.JTextField();
		jLabelRetryCount = new javax.swing.JLabel();
		jTextFieldRetryCount = new javax.swing.JTextField();
		jLabelInputsPath = new javax.swing.JLabel();
		jTextFieldInputsPath = new javax.swing.JTextField();
		jTextFieldArguments = new javax.swing.JTextField();
		jLabelArguments = new javax.swing.JLabel();

		jLabelOutputSandbox.setText("OutputSandbox");

		jLabelStdErr.setText("StdErr");

		jLabelStdOut.setText("StdOut");

		jLabelNodes.setText("Nodes");

		jLabelJobType.setText("JobType");

		jLabelType.setText("Type");

		jLabelInputSandbox.setText("InputSandbox");

		jTextFieldInputSandbox.setColumns(25);

		jTextFieldType.setColumns(25);

		jTextFieldJobType.setColumns(25);

		jTextFieldNodes.setColumns(25);

		jTextFieldStdOut.setColumns(25);

		jTextFieldStdErr.setColumns(25);

		jTextFieldOutputSandbox.setColumns(25);

		jLabelExecutable.setText("Executable");

		jTextFieldExecutable.setColumns(25);

		jLabelRequirements.setText("Job Requirements");

		jTextFieldRequirements.setColumns(25);

		jLabelRetryCount.setText("Retry Count");

		jTextFieldRetryCount.setColumns(25);

		jLabelInputsPath.setText("Inputs Path");

		jTextFieldInputsPath.setColumns(25);

		jTextFieldArguments.setColumns(25);

		jLabelArguments.setText("Arguments");

		jTextFieldType.setText(configBean.getJdlconfigbean().getType());
		jTextFieldType.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setType(jTextFieldType.getText());
			}
		});
		
		jTextFieldJobType.setText(configBean.getJdlconfigbean().getJobType());
		jTextFieldJobType.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setJobType(jTextFieldJobType.getText());
			}
		});

		jTextFieldStdOut.setText(configBean.getJdlconfigbean().getStdOut());
		jTextFieldStdOut.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setStdOut(jTextFieldStdOut.getText());
			}
		});

		jTextFieldStdErr.setText(configBean.getJdlconfigbean().getStdErr());
		jTextFieldStdErr.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setStdErr(jTextFieldStdErr.getText());
			}
		});

		jTextFieldInputSandbox.setText(configBean.getJdlconfigbean().getInputSandbox());
		jTextFieldInputSandbox.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setInputSandbox(jTextFieldInputSandbox.getText());
			}
		});

		jTextFieldOutputSandbox.setText(configBean.getJdlconfigbean().getOutputSandbox());
		jTextFieldOutputSandbox.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setOutputSandbox(jTextFieldOutputSandbox.getText());
			}
		});

		jTextFieldArguments.setText(configBean.getJdlconfigbean().getArguments());
		jTextFieldArguments.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setArguments(jTextFieldArguments.getText());
			}
		});

	    jTextFieldNodes.setText(configBean.getJdlconfigbean().getNodeNumber());
		jTextFieldNodes.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setNodeNumber(jTextFieldNodes.getText());
			}
		});

		jTextFieldRetryCount.setText(configBean.getJdlconfigbean().getRetryCount());
		jTextFieldRetryCount.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setRetryCount(jTextFieldRetryCount.getText());
			}
		});

		jTextFieldInputsPath.setText(configBean.getJdlconfigbean().getInputsPath());
		jTextFieldInputsPath.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setInputsPath(jTextFieldInputsPath.getText());
			}
		});

		jTextFieldExecutable.setText(configBean.getJdlconfigbean().getExecutable());
		jTextFieldExecutable.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setExecutable(jTextFieldExecutable.getText());
			}
		});

		jTextFieldRequirements.setText(configBean.getJdlconfigbean().getRequirements());
		jTextFieldRequirements.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configBean.getJdlconfigbean().setRequirements(jTextFieldRequirements.getText());
			}
		});
		
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGap(20, 20, 20)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabelInputSandbox)
                                .addComponent(jLabelOutputSandbox)
                                .addComponent(jLabelStdErr)
                                .addComponent(jLabelStdOut)
                                .addComponent(jLabelNodes)
                                .addComponent(jLabelJobType)
                                .addComponent(jLabelType))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextFieldInputSandbox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldType, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldJobType, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldNodes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldStdOut, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldStdErr, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldOutputSandbox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabelExecutable)
                                .addGap(18, 18, 18)
                                .addComponent(jTextFieldExecutable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabelRequirements)
                                    .addComponent(jLabelArguments))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextFieldArguments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(118, 118, 118)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(jLabelInputsPath)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextFieldInputsPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabelRetryCount)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextFieldRetryCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addComponent(jTextFieldRequirements, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelType))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldJobType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelJobType))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldNodes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelNodes))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldStdOut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelStdOut))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldStdErr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelStdErr))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldInputSandbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelInputSandbox))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldOutputSandbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelOutputSandbox))
                    .addGap(9, 9, 9)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldExecutable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelExecutable))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldArguments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelArguments))
                    .addGap(7, 7, 7)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldRequirements, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelRequirements))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldRetryCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelRetryCount))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldInputsPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelInputsPath))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            return JDLPanel;
	}

	/**
	 * Loops through the {@link ActivityInputPortDefinitionBean} in the
	 * {@link gLiteActivityConfigurationBean} and creates a
	 * {@link gLiteInputViewer} for each one. Displays the name and a
	 * {@link JSpinner} to change the depth for each one and a {@link JButton}
	 * to remove it. Currently the individual components from a
	 * {@link gLiteInputViewer} are added rather than the
	 * {@link gLiteInputViewer} itself
	 * 
	 * @return panel containing the view over the input ports
	 */
	private JPanel setInputPanel() {
		final JPanel inputEditPanel = new JPanel(new GridBagLayout());
		inputEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Inputs"));

		final GridBagConstraints inputConstraint = new GridBagConstraints();
		inputConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		inputConstraint.gridx = 0;
		inputConstraint.gridy = 0;
		inputConstraint.weightx = 0.1;
		inputConstraint.fill = GridBagConstraints.BOTH;

		inputEditPanel.add(new JLabel("Name"), inputConstraint);
		inputConstraint.gridx = 1;
		inputEditPanel.add(new JLabel("Depth"), inputConstraint);

		inputGridy = 1;
		inputConstraint.gridx = 0;
		for (ActivityInputPortDefinitionBean inputBean : configuration.getInputPortDefinitions()) {
			// FIXME refactor this into a method
			/*
			 * ActivityInputPortDefinitionBean inputBean=new ActivityInputPortDefinitionBean();
			 * inputBean.setName(inputPort.getName());
			 * inputBean.setAllowsLiteralValues(true);
			 * inputBean.setDepth(inputPort.getDepth());
			 * inputBean.setHandledReferenceSchemes (inputPort.getHandledReferenceSchemes());
			 * List<String> mimeTypes = new ArrayList<String>(); mimeTypes.add("text/plain");
			 * inputBean.setMimeTypes(mimeTypes);
			 * inputBean.setTranslatedElementType(inputPort.getClass());
			 */
			inputConstraint.gridy = inputGridy;
			final gLiteInputViewer gliteInputViewer = new gLiteInputViewer(inputBean, true);
			inputViewList.add(gliteInputViewer);
			inputConstraint.gridx = 0;
			final JTextField nameField = gliteInputViewer.getNameField();
			inputConstraint.weightx = 0.1;
			inputEditPanel.add(nameField, inputConstraint);
			inputConstraint.weightx = 0.0;
			inputConstraint.gridx = 1;
			inputConstraint.gridx = 2;
			final JButton removeButton = new JButton("remove");
			removeButton.addActionListener(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					inputViewList.remove(gliteInputViewer);
					inputEditPanel.remove(nameField);
					inputEditPanel.remove(removeButton);
					inputEditPanel.revalidate();
					outerInputPanel.revalidate();
				}

			});
			inputEditPanel.add(removeButton, inputConstraint);
			inputGridy++;
		}
		outerInputPanel = new JPanel();
		outerInputPanel.setLayout(new GridBagLayout());
		GridBagConstraints outerPanelConstraint = new GridBagConstraints();
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 0;
		outerPanelConstraint.weightx = 0.1;
		outerPanelConstraint.weighty = 0.1;
		outerPanelConstraint.fill = GridBagConstraints.BOTH;
		outerInputPanel.add(new JScrollPane(inputEditPanel), outerPanelConstraint);
		outerPanelConstraint.weighty = 0;
		JButton addInputPortButton = new JButton(new AbstractAction() {
			// FIXME refactor this into a method
			public void actionPerformed(ActionEvent e) {
				ActivityInputPortDefinitionBean bean = new ActivityInputPortDefinitionBean();
				bean.setAllowsLiteralValues(true);
				bean.setDepth(0);
				List<Class<? extends ExternalReferenceSPI>> handledReferenceSchemes = new ArrayList<Class<? extends ExternalReferenceSPI>>();
				// handledReferenceSchemes.add(FileReference.class);
				bean.setHandledReferenceSchemes(handledReferenceSchemes);
				List<String> mimeTypes = new ArrayList<String>();
				mimeTypes.add("text/plain");
				bean.setMimeTypes(mimeTypes);
				bean.setName("newInputPort" + newInputPortNumber);
				newInputPortNumber++;
				bean.setTranslatedElementType(String.class);
				inputConstraint.gridy = inputGridy;
				final gLiteInputViewer gliteInputViewer = new gLiteInputViewer(bean, true);
				inputViewList.add(gliteInputViewer);
				inputConstraint.weightx = 0.1;
				inputConstraint.gridx = 0;
				final JTextField nameField = gliteInputViewer.getNameField();
				inputEditPanel.add(nameField, inputConstraint);
				inputConstraint.weightx = 0;
				inputConstraint.gridx = 1;
				final JSpinner depthSpinner = gliteInputViewer.getDepthSpinner();
				inputEditPanel.add(depthSpinner, inputConstraint);
				inputConstraint.gridx = 2;
				final JButton removeButton = new JButton("remove");
				removeButton.addActionListener(new AbstractAction() {

					public void actionPerformed(ActionEvent e) {
						inputViewList.remove(gliteInputViewer);
						inputEditPanel.remove(nameField);
						inputEditPanel.remove(depthSpinner);
						inputEditPanel.remove(removeButton);
						inputEditPanel.revalidate();
						outerInputPanel.revalidate();
					}

				});
				inputEditPanel.add(removeButton, inputConstraint);
				inputEditPanel.revalidate();

				inputGridy++;
			}

		});
		addInputPortButton.setText("Add Port");
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());

		JPanel filler = new JPanel();
		outerPanelConstraint.weightx = 0.1;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 0;

		buttonPanel.add(filler, outerPanelConstraint);

		outerPanelConstraint.weightx = 0;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 1;
		outerPanelConstraint.gridy = 0;

		buttonPanel.add(addInputPortButton, outerPanelConstraint);

		outerPanelConstraint.weightx = 0;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 1;
		outerPanelConstraint.fill = GridBagConstraints.BOTH;
		outerInputPanel.add(buttonPanel, outerPanelConstraint);

		return outerInputPanel;
	}

	/**
	 * Loops through the {@link ActivityInputPortDefinitionBean} in the
	 * {@link gLiteActivityConfigurationBean} and creates a
	 * {@link gLiteOutputViewer} for each one. Displays the name and a
	 * {@link JSpinner} to change the depth and granular depth for each one and
	 * a {@link JButton} to remove it. Currently the individual components from
	 * a {@link gLiteOutputViewer} are added rather than the
	 * {@link gLiteOutputViewer} itself
	 * 
	 * @return the panel containing the view of the output ports
	 */
	private JPanel setOutputPanel() {
		final JPanel outputEditPanel = new JPanel(new GridBagLayout());
		outputEditPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Outputs"));

		final GridBagConstraints outputConstraint = new GridBagConstraints();
		outputConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outputConstraint.gridx = 0;
		outputConstraint.gridy = 0;
		outputConstraint.weightx = 0.1;
		outputConstraint.weighty = 0.1;
		outputConstraint.fill = GridBagConstraints.BOTH;
		outputConstraint.weighty = 0;

		outputEditPanel.add(new JLabel("Name"), outputConstraint);
		outputConstraint.gridx = 1;
		outputEditPanel.add(new JLabel("Depth"), outputConstraint);
		outputConstraint.gridx = 2;
		outputEditPanel.add(new JLabel("GranularDepth"), outputConstraint);

		outputGridy = 1;
		outputConstraint.gridx = 0;
		for (OutputPort outputPort : activity.getOutputPorts()) {
			// for (ActivityOutputPortDefinitionBean outputBean :
			// configuration.getOutputPortDefinitions()) {
			// Dirty way to set the outputbean from outputPorts to reuse the
			// existing code of beanshells. may be a disaster
			ActivityOutputPortDefinitionBean outputBean = new ActivityOutputPortDefinitionBean();
			outputBean.setDepth(outputPort.getDepth());
			outputBean.setGranularDepth(outputPort.getGranularDepth());
			List<String> mimeTypes = new ArrayList<String>();
			mimeTypes.add("text/plain");
			outputBean.setMimeTypes(mimeTypes);
			outputBean.setName(outputPort.getName());
			outputConstraint.gridy = outputGridy;
			final gLiteOutputViewer gliteOutputViewer = new gLiteOutputViewer(outputBean, true);
			outputViewList.add(gliteOutputViewer);
			outputConstraint.gridx = 0;
			outputConstraint.weightx = 0.1;
			final JTextField nameField = gliteOutputViewer.getNameField();
			outputEditPanel.add(nameField, outputConstraint);
			outputConstraint.weightx = 0;
			outputConstraint.gridx = 1;

			outputConstraint.gridx = 2;
			outputConstraint.gridx = 3;
			outputConstraint.gridx = 4;
			final JButton removeButton = new JButton("remove");
			removeButton.addActionListener(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					outputViewList.remove(gliteOutputViewer);
					outputEditPanel.remove(nameField);
					outputEditPanel.remove(removeButton);
					outputEditPanel.revalidate();
					outerOutputPanel.revalidate();
				}

			});
			outputEditPanel.add(removeButton, outputConstraint);
			outputGridy++;
		}
		outerOutputPanel = new JPanel();
		outerOutputPanel.setLayout(new GridBagLayout());
		GridBagConstraints outerPanelConstraint = new GridBagConstraints();
		outerPanelConstraint.fill = GridBagConstraints.BOTH;
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 0;
		outerPanelConstraint.weightx = 0.1;
		outerPanelConstraint.weighty = 0.1;
		outerOutputPanel.add(new JScrollPane(outputEditPanel), outerPanelConstraint);
		outerPanelConstraint.weighty = 0;
		JButton addOutputPortButton = new JButton(new AbstractAction() {
			// FIXME refactor this into a method
			public void actionPerformed(ActionEvent e) {
				try {
					ActivityOutputPortDefinitionBean bean = new ActivityOutputPortDefinitionBean();
					bean.setDepth(0);
					bean.setGranularDepth(0);
					List<String> mimeTypes = new ArrayList<String>();
					mimeTypes.add("text/plain");
					bean.setMimeTypes(mimeTypes);
					bean.setName("newOutput" + newOutputPortNumber);
					final gLiteOutputViewer gliteOutputViewer = new gLiteOutputViewer(bean, true);
					outputViewList.add(gliteOutputViewer);
					outputConstraint.gridy = outputGridy;
					outputConstraint.gridx = 0;
					final JTextField nameField = gliteOutputViewer.getNameField();
					outputConstraint.weightx = 0.1;
					outputEditPanel.add(nameField, outputConstraint);
					outputConstraint.gridx = 1;
					outputConstraint.weightx = 0;
					final JSpinner depthSpinner = gliteOutputViewer.getDepthSpinner();
					outputEditPanel.add(depthSpinner, outputConstraint);
					outputConstraint.gridx = 2;
					final JSpinner granularDepthSpinner = gliteOutputViewer.getGranularDepthSpinner();
					outputEditPanel.add(granularDepthSpinner, outputConstraint);
					outputConstraint.gridx = 3;
					outputConstraint.gridx = 4;
					final JButton removeButton = new JButton("remove");
					removeButton.addActionListener(new AbstractAction() {

						public void actionPerformed(ActionEvent e) {
							outputViewList.remove(gliteOutputViewer);
							outputEditPanel.remove(nameField);
							outputEditPanel.remove(removeButton);
							outputEditPanel.revalidate();
						}

					});
					outputEditPanel.add(removeButton, outputConstraint);
					outputEditPanel.revalidate();
					newOutputPortNumber++;

					outputGridy++;
				} catch (Exception e1) {
					// throw it, log it??
				}
			}
		});
		addOutputPortButton.setText("Add Port");
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());

		JPanel filler = new JPanel();
		outerPanelConstraint.weightx = 0.1;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 0;

		buttonPanel.add(filler, outerPanelConstraint);

		outerPanelConstraint.weightx = 0;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 1;
		outerPanelConstraint.gridy = 0;

		buttonPanel.add(addOutputPortButton, outerPanelConstraint);

		outerPanelConstraint.weightx = 0;
		outerPanelConstraint.weighty = 0;
		outerPanelConstraint.gridx = 0;
		outerPanelConstraint.gridy = 1;
		outerPanelConstraint.fill = GridBagConstraints.BOTH;
		outerOutputPanel.add(buttonPanel, outerPanelConstraint);
		outerPanelConstraint.gridx = 1;
		outerPanelConstraint.gridy = 0;

		return outerOutputPanel;
	}

	public void setButtonClickedListener(ActionListener listener) {
		buttonClicked = listener;
	}
	
	/**
	 * Calls {@link gLiteActivity#configure(gLiteActivityConfigurationBean)}
	 * using a {@link gLiteActivityConfigurationBean} set with the new values in
	 * the view. After setting the values it uses the {@link #buttonClicked}
	 * {@link ActionListener} to tell any listeners that the new values have
	 * been set (primarily used to tell any parent components to remove the
	 * frames containing this panel)
	 * 
	 * @return the action which occurs when the OK button is clicked
	 */
	private AbstractAction getOKAction() {
		return new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				List<ActivityInputPortDefinitionBean> inputBeanList = new ArrayList<ActivityInputPortDefinitionBean>();
				for (gLiteInputViewer inputView : inputViewList) {
					ActivityInputPortDefinitionBean activityInputPortDefinitionBean = new ActivityInputPortDefinitionBean();

					activityInputPortDefinitionBean.setHandledReferenceSchemes(inputView.getBean().getHandledReferenceSchemes());
					activityInputPortDefinitionBean.setMimeTypes(inputView.getBean().getMimeTypes());
					activityInputPortDefinitionBean.setTranslatedElementType(inputView.getBean().getTranslatedElementType());
					activityInputPortDefinitionBean.setAllowsLiteralValues((Boolean) inputView.getLiteralSelector().getSelectedItem());
					activityInputPortDefinitionBean.setDepth((Integer) inputView.getDepthSpinner().getValue());
					activityInputPortDefinitionBean.setName(inputView.getNameField().getText());
					inputBeanList.add(activityInputPortDefinitionBean);
				}
				
				  List<ActivityOutputPortDefinitionBean> outputBeanList = new ArrayList<ActivityOutputPortDefinitionBean>();
				  for (gLiteOutputViewer outputView : outputViewList) {
					  ActivityOutputPortDefinitionBean activityOutputPortDefinitionBean = new ActivityOutputPortDefinitionBean();
				  
					  activityOutputPortDefinitionBean.setDepth((Integer)outputView.getDepthSpinner() .getValue());
					  activityOutputPortDefinitionBean.setGranularDepth((Integer) outputView .getGranularDepthSpinner().getValue());
					  activityOutputPortDefinitionBean.setName(outputView.getNameField().getText());
					  activityOutputPortDefinitionBean.setMimeTypes(outputView.getMimeTypeConfig().getMimeTypeList());
					  
					  outputView.getMimeTypeConfig().getMimeTypeList();
				  
				  // Edits edits = EditsRegistry.getEdits();
				  // FIXME add all the mime types as an annotation
				  
					  outputBeanList.add(activityOutputPortDefinitionBean);
				  }
				 
				gLiteActivityConfigurationBean gliteActivityConfigurationBean = new gLiteActivityConfigurationBean();
				gliteActivityConfigurationBean.setJdlconfigbean(new JDLConfigBean());
				//set glb		
				gliteActivityConfigurationBean.getJdlconfigbean().setType(jTextFieldType.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setJobType(jTextFieldJobType.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setExecutable(jTextFieldExecutable.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setArguments(jTextFieldArguments.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setStdOut(jTextFieldStdOut.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setStdErr(jTextFieldStdErr.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setInputSandbox(jTextFieldInputSandbox.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setOutputSandbox(jTextFieldOutputSandbox.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setRetryCount(jTextFieldRetryCount.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setRequirements(jTextFieldRequirements.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setNodeNumber(jTextFieldNodes.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setInputsPath(jTextFieldInputsPath.getText());
				
				gliteActivityConfigurationBean.setVO(jTextFieldVO.getText());
				gliteActivityConfigurationBean.setCaDir(jTextFieldCADir.getText());
				gliteActivityConfigurationBean.setDelegationID(jTextFieldDelegationID.getText());
				gliteActivityConfigurationBean.setProxyPath(jTextFieldProxyPath.getText());
				gliteActivityConfigurationBean.setWMProxy(jTextFieldProxyURL.getText());
				gliteActivityConfigurationBean.setVOMSDir(jTextFieldVOMSDir.getText());
				gliteActivityConfigurationBean.setVOMSCertDir(jTextFieldVOMSCertDir.getText());
				gliteActivityConfigurationBean.setWMSDir(jTextFieldWMSDir.getText());
				
				gliteActivityConfigurationBean.setJDLPath(JDLfilepath);
				
				gliteActivityConfigurationBean.setOutputPath(jTextFieldOutputPath.getText());
				gliteActivityConfigurationBean.setPollFrequency(jTextFieldPollFrequency.getText());				
				
				gliteActivityConfigurationBean.setInputPortDefinitions(inputBeanList);

				gliteActivityConfigurationBean.setOutputPortDefinitions(outputBeanList);				 

				configuration = gliteActivityConfigurationBean;
				configChanged = true;
				setVisible(false);
				buttonClicked.actionPerformed(e);
			}
		};
	}

	private javax.swing.JLabel jLabelVO;
	private javax.swing.JLabel jLabelCADir;
	private javax.swing.JLabel jLabelVOMSDir;
	private javax.swing.JLabel jLabelVOMSCertDir;
	private javax.swing.JLabel jLabelProxyPath;
	private javax.swing.JLabel jLabelDelegationID;
	private javax.swing.JLabel jLabelWMProxyURL;
	private javax.swing.JLabel jLabelWMSDir;
	private javax.swing.JLabel jLabelOutputPath;
	private javax.swing.JLabel jLabelPollFrequency;

	private javax.swing.JTextField jTextFieldCADir;
	private javax.swing.JTextField jTextFieldDelegationID;
	private javax.swing.JTextField jTextFieldProxyPath;
	private javax.swing.JTextField jTextFieldProxyURL;
	private javax.swing.JTextField jTextFieldVO;
	private javax.swing.JTextField jTextFieldVOMSCertDir;
	private javax.swing.JTextField jTextFieldVOMSDir;
	private javax.swing.JTextField jTextFieldWMSDir;
	private javax.swing.JTextField jTextFieldOutputPath;
	private javax.swing.JTextField jTextFieldPollFrequency;

	private javax.swing.JLabel jLabelExecutable;
	private javax.swing.JLabel jLabelArguments;
	private javax.swing.JLabel jLabelInputSandbox;
	private javax.swing.JLabel jLabelInputsPath;
	private javax.swing.JLabel jLabelJobType;
	private javax.swing.JLabel jLabelNodes;
	private javax.swing.JLabel jLabelOutputSandbox;
	private javax.swing.JLabel jLabelRequirements;
	private javax.swing.JLabel jLabelRetryCount;
	private javax.swing.JLabel jLabelStdErr;
	private javax.swing.JLabel jLabelStdOut;
	private javax.swing.JLabel jLabelType;

	private javax.swing.JTextField jTextFieldExecutable;
	private javax.swing.JTextField jTextFieldArguments;
	private javax.swing.JTextField jTextFieldInputSandbox;
	private javax.swing.JTextField jTextFieldInputsPath;
	private javax.swing.JTextField jTextFieldJobType;
	private javax.swing.JTextField jTextFieldNodes;
	private javax.swing.JTextField jTextFieldOutputSandbox;
	private javax.swing.JTextField jTextFieldRequirements;
	private javax.swing.JTextField jTextFieldRetryCount;
	private javax.swing.JTextField jTextFieldStdErr;
	private javax.swing.JTextField jTextFieldStdOut;
	private javax.swing.JTextField jTextFieldType;

	private String JDLfilepath;
}
