package query;

/*
 * This is Complete.
 */
import net.sf.taverna.t2.partition.ActivityQuery;
import net.sf.taverna.t2.partition.ActivityQueryFactory;

public class gLiteQueryFactory extends ActivityQueryFactory {
	@Override
	protected ActivityQuery createQuery(String property) {
		
		return new gLiteQuery(property);
	}

	@Override
	protected String getPropertyKey() {
		return null;
	}

}
