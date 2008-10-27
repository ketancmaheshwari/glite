package partition;

import net.sf.taverna.t2.partition.ActivityPartitionAlgorithmSet;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;

public class gLitePartitionAlgorithmSetSPI extends ActivityPartitionAlgorithmSet{
	
	public gLitePartitionAlgorithmSetSPI() {
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("operation"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("category"));
		partitionAlgorithms.add(new LiteralValuePartitionAlgorithm("url"));
	}

}
