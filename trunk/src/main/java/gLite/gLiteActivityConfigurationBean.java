package gLite;



import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityPortsDefinitionBean;

/**
 * A configuration bean specific to the glite activity.
 * Must have all the properties of glite activity with proper types.
 */
public class gLiteActivityConfigurationBean extends ActivityPortsDefinitionBean {
	private String WMProxy;
	private String VO;
	private String CaDir;
	private String VOMSDir;
	private String VOMSCertDir;
	private String ProxyPath;
	//private String DelegationID;
	private String UI;
	private String WMSDir;
	private String JDLPath;
	private String OutputPath;
	private String PollFrequency;
	private String SE;
    private JDLConfigBean jdlconfigbean;
    
	public String getWMSDir() {
		return WMSDir;
	}

	public void setWMSDir(String dir) {
		WMSDir = dir;
	}

	public String getOutputPath() {
		return OutputPath;
	}

	public void setOutputPath(String outputPath) {
		OutputPath = outputPath;
	}

	public gLiteActivityConfigurationBean() {
		super();
	}


	public String getJDLPath() {
		return JDLPath;
	}

	public void setJDLPath(String jdlpath) {
		JDLPath = jdlpath;
	}

	public String getWMProxy() {
		return WMProxy;
	}

	public void setWMProxy(String proxy) {
		WMProxy = proxy;
	}

	public String getVO() {
		return VO;
	}

	public void setVO(String vo) {
		VO = vo;
	}

	public String getCaDir() {
		return CaDir;
	}

	public void setCaDir(String caDir) {
		CaDir = caDir;
	}

	public String getVOMSDir() {
		return VOMSDir;
	}

	public void setVOMSDir(String dir) {
		VOMSDir = dir;
	}

	public String getVOMSCertDir() {
		return VOMSCertDir;
	}

	public void setVOMSCertDir(String certDir) {
		VOMSCertDir = certDir;
	}

	public String getProxyPath() {
		return ProxyPath;
	}

	public void setProxyPath(String proxyPath) {
		ProxyPath = proxyPath;
	}

//	public String getDelegationID() {
	//	return DelegationID;
	//}

	//public void setDelegationID(String delegationID) {
	//	DelegationID = delegationID;
	//}

	public String getPollFrequency() {
		return PollFrequency;
	}

	public void setPollFrequency(String pollFrequency) {
		PollFrequency = pollFrequency;
	}

	public JDLConfigBean getJdlconfigbean() {
		return jdlconfigbean;
	}

	public void setJdlconfigbean(JDLConfigBean jdlconfigbean) {
		this.jdlconfigbean = jdlconfigbean;
	}

	public String getSE() {
		return SE;
	}

	public void setSE(String SE) {
		this.SE=SE;
	}

	public String getUI() {
		return UI;
	}

	public void setUI(String UI) {
		this.UI = UI;
	}


}
