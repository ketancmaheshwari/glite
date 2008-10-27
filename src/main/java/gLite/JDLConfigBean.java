package gLite;

public class JDLConfigBean {
	private String Type;
	private String JobType;
	private String Executable;
	private String StdOut;
	private String StdErr;
	private String InputSandbox;
	private String OutputSandbox;
	private String Arguments;
	private String NodeNumber;
	private String InputsPath;
	private String Requirements;
	private String RetryCount;
	private String Wrapper;
	
	public JDLConfigBean(String type, String jobType, String executable, String stdOut, String stdErr, String inputSandbox, String outputSandbox, String arguments,
			String nodeNumber, String inputsPath, String requirements, String retryCount, String wrapper) {
		super();
		Type = type;
		JobType = jobType;
		Executable = executable;
		StdOut = stdOut;
		StdErr = stdErr;
		InputSandbox = inputSandbox;
		OutputSandbox = outputSandbox;
		Arguments = arguments;
		NodeNumber = nodeNumber;
		InputsPath = inputsPath;
		Requirements = requirements;
		RetryCount = retryCount;
		Wrapper = wrapper;
	}
	
	
	public JDLConfigBean() {
		// TODO Auto-generated constructor stub
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getJobType() {
		return JobType;
	}
	public void setJobType(String jobType) {
		JobType = jobType;
	}
	public String getExecutable() {
		return Executable;
	}
	public void setExecutable(String executable) {
		Executable = executable;
	}
	public String getStdOut() {
		return StdOut;
	}
	public void setStdOut(String stdOut) {
		StdOut = stdOut;
	}
	public String getStdErr() {
		return StdErr;
	}
	public void setStdErr(String stdErr) {
		StdErr = stdErr;
	}
	public String getInputSandbox() {
		return InputSandbox;
	}
	public void setInputSandbox(String inputSandbox) {
		InputSandbox = inputSandbox;
	}
	public String getOutputSandbox() {
		return OutputSandbox;
	}
	public void setOutputSandbox(String outputSandbox) {
		OutputSandbox = outputSandbox;
	}
	public String getArguments() {
		return Arguments;
	}
	public void setArguments(String arguments) {
		Arguments = arguments;
	}
	public String getNodeNumber() {
		return NodeNumber;
	}
	public void setNodeNumber(String nodeNumber) {
		NodeNumber = nodeNumber;
	}
	public String getInputsPath() {
		return InputsPath;
	}
	public void setInputsPath(String inputsPath) {
		InputsPath = inputsPath;
	}
	public String getRequirements() {
		return Requirements;
	}
	public void setRequirements(String requirements) {
		Requirements = requirements;
	}
	public String getRetryCount() {
		return RetryCount;
	}
	public void setRetryCount(String retryCount) {
		RetryCount = retryCount;
	}
	public String getWrapper() {
		return Wrapper;
	}
	public void setWrapper(String wrapper) {
		Wrapper = wrapper;
	}
	

}
