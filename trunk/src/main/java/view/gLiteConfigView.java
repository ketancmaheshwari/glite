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

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
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
	/** the configuration bean used to configure the activity */
	private gLiteActivityConfigurationBean configuration;

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
		ports.add("Input Ports", inputScroller);
		JScrollPane outputScroller = new JScrollPane(setOutputPanel());
		portEditPanel.add(outputScroller, panelConstraint);
		ports.add("Output Ports", outputScroller);

		return ports;
	}

	private JPanel setPropertiesPanel() {
		JPanel propertiesPanel = new JPanel();

		jLabelVO = new javax.swing.JLabel();
		jLabelCADir = new javax.swing.JLabel();
		jLabelVOMSDir = new javax.swing.JLabel();
		jLabelVOMSCertDir = new javax.swing.JLabel();
		jLabelProxyPath = new javax.swing.JLabel();
		jLabelUI = new javax.swing.JLabel();
		jLabelWMProxyURL = new javax.swing.JLabel();
		jLabelWMSDir = new javax.swing.JLabel();
		jLabelOutputPath = new javax.swing.JLabel();
		jLabelPollFrequency = new javax.swing.JLabel();
		jLabelSE = new javax.swing.JLabel();

		jTextFieldVO = new javax.swing.JTextField();
		jTextFieldCADir = new javax.swing.JTextField();
		jTextFieldVOMSDir = new javax.swing.JTextField();
		jTextFieldVOMSCertDir = new javax.swing.JTextField();
		jTextFieldProxyPath = new javax.swing.JTextField();
		jTextFieldUI = new javax.swing.JTextField();
		jTextFieldProxyURL = new javax.swing.JTextField();
		jTextFieldWMSDir = new javax.swing.JTextField();
		jTextFieldOutputPath = new javax.swing.JTextField();
		jTextFieldPollFrequency = new javax.swing.JTextField();
		jTextFieldSE = new javax.swing.JTextField();

		jCheckBoxExecLocal = new javax.swing.JCheckBox();
		
		jLabelVO.setText("VO");

		jLabelCADir.setText("CADir");

		jLabelVOMSDir.setText("VOMSDir");

		jLabelVOMSCertDir.setText("VOMSCertDir");

		jLabelProxyPath.setText("ProxyPath");

		jLabelUI.setText("UI");

		jLabelWMProxyURL.setText("WMProxyURL");

		jLabelWMSDir.setText("WMSDir");

		jLabelOutputPath.setText("Output Path:");

		jLabelPollFrequency.setText("Poll Frequency");

		jLabelSE.setText("SE");
		
		jCheckBoxExecLocal.setText("Execute Locally");
		jCheckBoxExecLocal.setToolTipText("Check this box if you want to execute this processor locally");

		jTextFieldWMSDir.setColumns(30);
		jTextFieldWMSDir.setToolTipText("Points to a simple text file containing the server URLs for your VO");

		jTextFieldVO.setColumns(30);
		jTextFieldVO.setToolTipText("Name of the VO");

		jTextFieldCADir.setColumns(30);
		jTextFieldCADir.setToolTipText("Should point to the location where CA certificates are installed, usually /etc/grid-security/certificates");

		jTextFieldVOMSDir.setColumns(30);
		jTextFieldVOMSDir.setToolTipText("Points to a simple text file with the location of VOMS server");

		jTextFieldVOMSCertDir.setColumns(30);
		jTextFieldVOMSCertDir.setToolTipText("Path of VOMS certifates installation");

		jTextFieldProxyPath.setColumns(30);
		jTextFieldProxyPath.setToolTipText("Path of the proxy certificate");

		jTextFieldUI.setColumns(30);
		jTextFieldUI.setToolTipText("Can be any number");

		jTextFieldProxyURL.setColumns(30);
		jTextFieldProxyURL.setToolTipText("WMProxy endpoint URL");

		jTextFieldOutputPath.setColumns(30);
		jTextFieldOutputPath.setToolTipText("Path where you want your output to be written (usually /tmp)");

		jTextFieldPollFrequency.setColumns(30);
		jTextFieldPollFrequency.setToolTipText("Milliseconds you want to poll for the job status (20000(20sec) is a good time!)");

		jTextFieldSE.setColumns(30);
		jTextFieldSE.setToolTipText("Grid Storage Element");

		GroupLayout layout = new GroupLayout(propertiesPanel);
		propertiesPanel.setLayout(layout);

		jTextFieldVO.setText(configuration.getVO());
		jTextFieldVO.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.setVO(jTextFieldVO.getText());
			}
		});

		jTextFieldCADir.setText(configuration.getCaDir());
		jTextFieldCADir.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.setCaDir(jTextFieldCADir.getText());
			}
		});

		jTextFieldVOMSDir.setText(configuration.getVOMSDir());
		jTextFieldVOMSDir.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.setVOMSDir(jTextFieldVOMSDir.getText());
			}
		});

		jTextFieldVOMSCertDir.setText(configuration.getVOMSCertDir());
		jTextFieldVOMSCertDir.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.setVOMSCertDir(jTextFieldVOMSCertDir.getText());
			}
		});

		jTextFieldProxyPath.setText(configuration.getProxyPath());
		jTextFieldProxyPath.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.setProxyPath(jTextFieldProxyPath.getText());
			}
		});

		jTextFieldUI.setText(configuration.getUI());
		jTextFieldUI.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.setUI(jTextFieldUI.getText());
			}
		});

		jTextFieldProxyURL.setText(configuration.getWMProxy());
		jTextFieldProxyURL.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.setWMProxy(jTextFieldProxyURL.getText());
			}
		});

		jTextFieldWMSDir.setText(configuration.getWMSDir());
		jTextFieldWMSDir.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				configuration.setWMSDir(jTextFieldWMSDir.getText());
			}
		});

		jTextFieldOutputPath.setText(configuration.getOutputPath());
		jTextFieldOutputPath.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {

			}

			public void focusLost(FocusEvent e) {
				configuration.setOutputPath(jTextFieldOutputPath.getText());
			}
		});

		jTextFieldPollFrequency.setText(configuration.getPollFrequency());
		jTextFieldPollFrequency.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.setPollFrequency(jTextFieldPollFrequency.getText());
			}
		});
		
		jCheckBoxExecLocal.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e){
				
			}
			public void focusLost(FocusEvent e) {
				configuration.setLocalexec(jCheckBoxExecLocal.isSelected());
				System.out.println(jCheckBoxExecLocal.isSelected());
			}
		});
		
		
		jTextFieldSE.setText(configuration.getSE());
		jTextFieldSE.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.setSE(jTextFieldSE.getText());
			}
		});

        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap(43, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabelWMProxyURL)
                                .addComponent(jLabelProxyPath)
                                .addComponent(jLabelVOMSCertDir)
                                .addComponent(jLabelVOMSDir)
                                .addComponent(jLabelCADir)
                                .addComponent(jLabelVO)
                                .addComponent(jLabelUI))
                            .addGap(38, 38, 38)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextFieldUI, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldVO, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldCADir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldVOMSDir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldVOMSCertDir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldProxyPath, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextFieldProxyURL, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(jLabelWMSDir)
                            .addGap(38, 38, 38)
                            .addComponent(jTextFieldWMSDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(jLabelOutputPath)
                            .addGap(38, 38, 38)
                            .addComponent(jTextFieldOutputPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabelSE)
                                .addComponent(jLabelPollFrequency))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGap(38, 38, 38)
                                    .addComponent(jTextFieldPollFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(38, 38, 38)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jCheckBoxExecLocal)
                                        .addComponent(jTextFieldSE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addContainerGap())
            );

            layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jTextFieldCADir, jTextFieldUI, jTextFieldProxyPath, jTextFieldVO, jTextFieldVOMSCertDir});

            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldVO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelVO))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldCADir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelCADir))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldVOMSDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelVOMSDir))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldVOMSCertDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelVOMSCertDir))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldProxyPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelProxyPath))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldUI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelUI))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldProxyURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelWMProxyURL))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldWMSDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelWMSDir))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextFieldOutputPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelOutputPath))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jTextFieldPollFrequency, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextFieldSE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabelSE)))
                        .addComponent(jLabelPollFrequency))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jCheckBoxExecLocal)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jTextFieldCADir, jTextFieldUI, jTextFieldProxyPath, jTextFieldVO, jTextFieldVOMSCertDir});

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
		jTextFieldInputSandbox.setToolTipText("Name of inputsandbox files:commaseparated and with double quotes");

		jTextFieldType.setColumns(25);
		jTextFieldType.setToolTipText("always job");

		jTextFieldJobType.setColumns(25);
		jTextFieldJobType.setToolTipText("Normal or MPICH");

		jTextFieldNodes.setColumns(25);
		jTextFieldNodes.setToolTipText(">0 in case of MPICH jobs");

		jTextFieldStdOut.setColumns(25);
		jTextFieldStdOut.setToolTipText("Name of the standard output file");

		jTextFieldStdErr.setColumns(25);
		jTextFieldStdErr.setToolTipText("Name of standard error file");

		jTextFieldOutputSandbox.setColumns(25);
		jTextFieldOutputSandbox.setToolTipText("Name of outputsandbox files:commaseparated and with double quotes");

		jLabelExecutable.setText("Executable");

		jTextFieldExecutable.setColumns(25);
		jTextFieldExecutable.setToolTipText("Name of the executable");

		jLabelRequirements.setText("Job Requirements");

		jTextFieldRequirements.setColumns(25);
		jTextFieldRequirements.setToolTipText("job requirements");

		jLabelRetryCount.setText("Retry Count");

		jTextFieldRetryCount.setColumns(25);
		jTextFieldRetryCount.setToolTipText("Retry Count");

		jLabelInputsPath.setText("Inputs Path");

		jTextFieldInputsPath.setColumns(25);
		jTextFieldInputsPath.setToolTipText("Complete path where all the files(sandbox, executable etc.) are to be found");

		jTextFieldArguments.setColumns(25);

		jLabelArguments.setText("Arguments");

		jTextFieldType.setText(configuration.getJdlconfigbean().getType());
		jTextFieldType.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setType(jTextFieldType.getText());
			}
		});

		jTextFieldJobType.setText(configuration.getJdlconfigbean().getJobType());
		jTextFieldJobType.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setJobType(jTextFieldJobType.getText());
			}
		});

		jTextFieldStdOut.setText(configuration.getJdlconfigbean().getStdOut());
		jTextFieldStdOut.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setStdOut(jTextFieldStdOut.getText());
			}
		});

		jTextFieldStdErr.setText(configuration.getJdlconfigbean().getStdErr());
		jTextFieldStdErr.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setStdErr(jTextFieldStdErr.getText());
			}
		});

		jTextFieldInputSandbox.setText(configuration.getJdlconfigbean().getInputSandbox());
		jTextFieldInputSandbox.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setInputSandbox(jTextFieldInputSandbox.getText());
			}
		});

		jTextFieldOutputSandbox.setText(configuration.getJdlconfigbean().getOutputSandbox());
		jTextFieldOutputSandbox.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setOutputSandbox(jTextFieldOutputSandbox.getText());
			}
		});

		jTextFieldArguments.setText(configuration.getJdlconfigbean().getJDLArguments());
		jTextFieldArguments.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setJDLArguments(jTextFieldArguments.getText());
			}
		});

		jTextFieldNodes.setText(configuration.getJdlconfigbean().getNodeNumber());
		jTextFieldNodes.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setNodeNumber(jTextFieldNodes.getText());
			}
		});

		jTextFieldRetryCount.setText(configuration.getJdlconfigbean().getRetryCount());
		jTextFieldRetryCount.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setRetryCount(jTextFieldRetryCount.getText());
			}
		});

		jTextFieldInputsPath.setText(configuration.getJdlconfigbean().getInputsPath());
		jTextFieldInputsPath.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setInputsPath(jTextFieldInputsPath.getText());
			}
		});

		jTextFieldExecutable.setText(configuration.getJdlconfigbean().getExecutable());
		jTextFieldExecutable.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setExecutable(jTextFieldExecutable.getText());
			}
		});

		jTextFieldRequirements.setText(configuration.getJdlconfigbean().getRequirements());
		jTextFieldRequirements.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				configuration.getJdlconfigbean().setRequirements(jTextFieldRequirements.getText());
			}
		});

		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createSequentialGroup().addGap(20, 20, 20).addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jLabelInputSandbox).addComponent(jLabelOutputSandbox)
												.addComponent(jLabelStdErr).addComponent(jLabelStdOut).addComponent(jLabelNodes).addComponent(jLabelJobType).addComponent(
														jLabelType)).addGap(18, 18, 18).addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jTextFieldInputSandbox,
												javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jTextFieldType, javax.swing.GroupLayout.Alignment.TRAILING,
												javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(
												jTextFieldJobType, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jTextFieldNodes,
												javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jTextFieldStdOut, javax.swing.GroupLayout.Alignment.TRAILING,
												javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(
												jTextFieldStdErr, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jTextFieldOutputSandbox,
												javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))).addGroup(
								javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addGroup(
										javax.swing.GroupLayout.Alignment.TRAILING,
										layout.createSequentialGroup().addComponent(jLabelExecutable).addGap(18, 18, 18).addComponent(jTextFieldExecutable,
												javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(
										layout.createSequentialGroup().addGroup(
												layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jLabelRequirements).addComponent(
														jLabelArguments)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE).addComponent(jTextFieldArguments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.PREFERRED_SIZE))).addGroup(
								layout.createSequentialGroup().addGap(118, 118, 118).addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addGroup(
												layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
														layout.createSequentialGroup().addGap(6, 6, 6).addComponent(jLabelInputsPath).addPreferredGap(
																javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jTextFieldInputsPath,
																javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(
														javax.swing.GroupLayout.Alignment.TRAILING,
														layout.createSequentialGroup().addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
																jLabelRetryCount).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
																jTextFieldRetryCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))).addComponent(jTextFieldRequirements,
												javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addContainerGap().addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldType, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelType)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldJobType, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelJobType)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldNodes, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelNodes)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldStdOut, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelStdOut)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldStdErr, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelStdErr)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldInputSandbox, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelInputSandbox)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldOutputSandbox, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelOutputSandbox)).addGap(9, 9, 9).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldExecutable, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelExecutable)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldArguments, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelArguments)).addGap(7, 7, 7).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldRequirements, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelRequirements)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldRetryCount, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelRetryCount)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jTextFieldInputsPath, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabelInputsPath)).addContainerGap(
						javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

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

			inputConstraint.gridy = inputGridy;
			final gLiteInputViewer gliteInputViewer = new gLiteInputViewer(inputBean, true);
			inputViewList.add(gliteInputViewer);

			inputConstraint.gridx = 0;
			final JTextField nameField = gliteInputViewer.getNameField();
			final JSpinner depthspinner = gliteInputViewer.getDepthSpinner();
			inputConstraint.weightx = 0.1;
			inputEditPanel.add(nameField, inputConstraint);
			
			inputConstraint.weightx = 0;
			inputConstraint.gridx = 1;
			inputEditPanel.add(depthspinner, inputConstraint);
			
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
		// for (OutputPort outputPort : activity.getOutputPorts()) {
		for (ActivityOutputPortDefinitionBean outputBean : configuration.getOutputPortDefinitions()) {
			// Dirty way to set the outputbean from outputPorts to reuse the
			// existing code of beanshells. may be a disaster
			// ActivityOutputPortDefinitionBean outputBean = new
			// ActivityOutputPortDefinitionBean();
			List<String> mimeTypes = new ArrayList<String>();
			mimeTypes.add("text/plain");
			outputBean.setMimeTypes(mimeTypes);
			outputConstraint.gridy = outputGridy;
			
			final gLiteOutputViewer gliteOutputViewer = new gLiteOutputViewer(outputBean, true);
			
			outputViewList.add(gliteOutputViewer);
			outputConstraint.gridx = 0;
			outputConstraint.weightx = 0.1;
			final JTextField nameField = gliteOutputViewer.getNameField();
			outputEditPanel.add(nameField, outputConstraint);
			outputConstraint.weightx = 0;
			outputConstraint.gridx = 1;
			final JSpinner depthSpinner = gliteOutputViewer.getDepthSpinner();
			outputEditPanel.add(depthSpinner, outputConstraint);
			outputConstraint.gridx = 2;
			outputBean.setDepth((Integer)depthSpinner.getValue());
			final JSpinner granularDepthSpinner = gliteOutputViewer.getGranularDepthSpinner();
			outputEditPanel.add(granularDepthSpinner, outputConstraint);
			outputBean.setGranularDepth((Integer)granularDepthSpinner.getValue());
			outputConstraint.gridx = 3;
			outputConstraint.gridx = 4;
			
			final JButton removeButton = new JButton("remove");
			removeButton.addActionListener(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					outputViewList.remove(gliteOutputViewer);
					outputEditPanel.remove(nameField);
					outputEditPanel.remove(depthSpinner);
					outputEditPanel.remove(granularDepthSpinner);
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
							outputEditPanel.remove(depthSpinner);
							outputEditPanel.remove(granularDepthSpinner);
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

					activityOutputPortDefinitionBean.setDepth((Integer) outputView.getDepthSpinner().getValue());
					activityOutputPortDefinitionBean.setGranularDepth((Integer) outputView.getGranularDepthSpinner().getValue());
					activityOutputPortDefinitionBean.setName(outputView.getNameField().getText());
					activityOutputPortDefinitionBean.setMimeTypes(outputView.getMimeTypeConfig().getMimeTypeList());

					outputView.getMimeTypeConfig().getMimeTypeList();

					outputBeanList.add(activityOutputPortDefinitionBean);
				}

				gLiteActivityConfigurationBean gliteActivityConfigurationBean = new gLiteActivityConfigurationBean();
				gliteActivityConfigurationBean.setJdlconfigbean(new JDLConfigBean());
				// set glb
				gliteActivityConfigurationBean.getJdlconfigbean().setType(jTextFieldType.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setJobType(jTextFieldJobType.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setExecutable(jTextFieldExecutable.getText());
				gliteActivityConfigurationBean.getJdlconfigbean().setJDLArguments(jTextFieldArguments.getText());
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
				gliteActivityConfigurationBean.setUI(jTextFieldUI.getText());
				gliteActivityConfigurationBean.setProxyPath(jTextFieldProxyPath.getText());
				gliteActivityConfigurationBean.setWMProxy(jTextFieldProxyURL.getText());
				gliteActivityConfigurationBean.setVOMSDir(jTextFieldVOMSDir.getText());
				gliteActivityConfigurationBean.setVOMSCertDir(jTextFieldVOMSCertDir.getText());
				gliteActivityConfigurationBean.setWMSDir(jTextFieldWMSDir.getText());

				gliteActivityConfigurationBean.setJDLPath(JDLfilepath);

				gliteActivityConfigurationBean.setOutputPath(jTextFieldOutputPath.getText());
				gliteActivityConfigurationBean.setPollFrequency(jTextFieldPollFrequency.getText());
				gliteActivityConfigurationBean.setSE(jTextFieldSE.getText());
				gliteActivityConfigurationBean.setLocalexec(jCheckBoxExecLocal.isSelected());
				
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
	private javax.swing.JLabel jLabelUI;
	private javax.swing.JLabel jLabelWMProxyURL;
	private javax.swing.JLabel jLabelWMSDir;
	private javax.swing.JLabel jLabelOutputPath;
	private javax.swing.JLabel jLabelPollFrequency;
	private javax.swing.JLabel jLabelSE;

	private javax.swing.JTextField jTextFieldCADir;
	private javax.swing.JTextField jTextFieldUI;
	private javax.swing.JTextField jTextFieldProxyPath;
	private javax.swing.JTextField jTextFieldProxyURL;
	private javax.swing.JTextField jTextFieldVO;
	private javax.swing.JTextField jTextFieldVOMSCertDir;
	private javax.swing.JTextField jTextFieldVOMSDir;
	private javax.swing.JTextField jTextFieldWMSDir;
	private javax.swing.JTextField jTextFieldOutputPath;
	private javax.swing.JTextField jTextFieldPollFrequency;
	private javax.swing.JTextField jTextFieldSE;
	private javax.swing.JCheckBox jCheckBoxExecLocal;

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
