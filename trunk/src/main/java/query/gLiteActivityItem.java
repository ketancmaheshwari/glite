package query;
/*
 * This class is responsible for really showing up the activity at the T2 panel.
 * It takes the icon for the activity that will show up.
 */


import gLite.JDLConfigBean;
import gLite.gLiteActivity;
import gLite.gLiteActivityConfigurationBean;

import javax.swing.Icon;
import javax.swing.ImageIcon;


import net.sf.taverna.t2.partition.AbstractActivityItem;
import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class gLiteActivityItem extends AbstractActivityItem{
	
	public Icon getIcon() {
		
		return new ImageIcon(gLiteActivityItem.class.getResource("/glite.png"));
	}

	public int compareTo(ActivityItem o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String getType() {
		return "gLite";
	}
	
	@Override
	public String toString() {
		return getType();
	}
	
	@Override
	public Activity<?> getUnconfiguredActivity() {
		return new gLiteActivity();
	}

	@Override
	public Object getConfigBean() {
		gLiteActivityConfigurationBean bean = new gLiteActivityConfigurationBean();
		bean.setJdlconfigbean(new JDLConfigBean());
		/*"https://wms01.egee.cesga.es:7443/glite_wms_wmproxy_server",
        "https://wmslb101.grid.ucy.ac.cy:7443/glite_wms_wmproxy_server",
        "https://rb1.cyf-kr.edu.pl:7443/glite_wms_wmproxy_server",
        "https://cs3-egee.srce.hr:7443/glite_wms_wmproxy_server",
        "https://wms01.egee-see.org:7443/glite_wms_wmproxy_server",
        "https://egee-wms-01.cnaf.infn.it:7443/glite_wms_wmproxy_server",
        "https://glite-rb-00.cnaf.infn.it:7443/glite_wms_wmproxy_server",
        "https://prod-wms-01.pd.infn.it:7443/glite_wms_wmproxy_server",
        "https://lcgrb01.jinr.ru:7443/glite_wms_wmproxy_server",
        "https://lcgrb02.jinr.ru:7443/glite_wms_wmproxy_server",
        "https://wms01.lip.pt:7443/glite_wms_wmproxy_server",
        "https://rb03.pic.es:7443/glite_wms_wmproxy_server",
        "https://wms.pnpi.nw.ru:7443/glite_wms_wmproxy_server",
        "https://wms.grid.sara.nl:7443/glite_wms_wmproxy_server"*/
		//bean.setWMProxy("https://wms.pnpi.nw.ru:7443/glite_wms_wmproxy_server");
		//bean.setWMProxy("https://egee-wms-01.cnaf.infn.it:7443/glite_wms_wmproxy_server");
		//bean.setWMProxy("https://wms01.egee-see.org:7443/glite_wms_wmproxy_server");
		//bean.setWMProxy("https://lcgrb02.jinr.ru:7443/glite_wms_wmproxy_server");
		bean.setWMProxy("https://egee-wms-01.cnaf.infn.it:7443/glite_wms_wmproxy_server");
		bean.setUI("glite.unice.fr");
		bean.setCaDir("/etc/grid-security/certificates");
		bean.setProxyPath("/tmp/x509up_u501");
		bean.setVOMSDir("/home/ketan/gliteui-3.1/glite/etc/vomses");
		bean.setVOMSCertDir("/etc/grid-security/vomsdir/");
		bean.setVO("biomed");
		bean.setWMSDir("/home/ketan/gliteui-3.1/glite/etc/biomed/glite_wms.conf");
		bean.setJDLPath("/home/ketan/jlite/test/test.jdl");
		bean.setOutputPath("/tmp/");
		bean.setPollFrequency("20000");
		bean.setSE("hepgrid11.ph.liv.ac.uk");
		bean.getJdlconfigbean().setType("Job");
		bean.getJdlconfigbean().setJobType("Normal");
		bean.getJdlconfigbean().setNodeNumber("0");
		bean.getJdlconfigbean().setStdOut("stdout");
		bean.getJdlconfigbean().setStdErr("stderr");
		bean.getJdlconfigbean().setInputSandbox("\"blah.txt\"");
		bean.getJdlconfigbean().setOutputSandbox("\"stdout\",\"stderr\"");
		bean.getJdlconfigbean().setExecutable("/bin/echo");
		bean.getJdlconfigbean().setJDLArguments("$*");
		bean.getJdlconfigbean().setRequirements("(other.GlueCEStateStatus == \"Production\") && RegExp(\"nl\",other.GlueCEUniqueId)");
		bean.getJdlconfigbean().setRetryCount("3");
		bean.getJdlconfigbean().setInputsPath("/home/ketan/ManchesterWork/gliteworkflows/inputs/");
		return bean;
	}

}
