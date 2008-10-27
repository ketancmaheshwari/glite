package query;

/*
 * This is fine.
 * 
 */

import net.sf.taverna.t2.partition.ActivityQuery;
public class gLiteQuery extends ActivityQuery{

public gLiteQuery(String property) {
	super(property);
}

@Override
public void doQuery() {
	add(new gLiteActivityItem());
}
	
}
