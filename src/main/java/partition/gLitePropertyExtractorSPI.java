package partition;

import java.util.HashMap;
import java.util.Map;

import query.gLiteActivityItem;

import net.sf.taverna.t2.partition.PropertyExtractorSPI;

public class gLitePropertyExtractorSPI implements PropertyExtractorSPI {

	public Map<String, Object> extractProperties(Object target) {
		Map<String,Object> map = new HashMap<String, Object>();
		if (target instanceof gLiteActivityItem) {
			gLiteActivityItem item = (gLiteActivityItem)target;
			map.put("type", item.getType());
		}
		return map;
	}

}
