package consensus.research.messages;

public class ClientRequest extends Message {

	private int cid;
	private String command;

	public ClientRequest(int cid, String command) {
		this.cid = cid;
		this.command = command;
	}

	public int getCid() {
		return cid;
	}

	public void setCid(int cid) {
		this.cid = cid;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
	
	
}
