package gLite;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

/**
 * A health checker for the glite activity.
 * This will do simple checks for correctness of types, parsing etc.
 */
public class gLiteActivityHealthChecker implements HealthChecker<gLiteActivity> {

	public boolean canHandle(Object subject) {
		return (subject instanceof gLiteActivity);
	}

	public HealthReport checkHealth(gLiteActivity activity) {
		return new HealthReport("glite Activity", "Health check not implemented", Status.WARNING);
	}

}