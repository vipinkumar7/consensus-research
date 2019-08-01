package consensus.research.log;

import akka.actor.ActorRef;

public class InternalClientRef {

	private final ActorRef sender;
	private final Integer cid;

	public InternalClientRef(ActorRef sender, Integer cid) {
		this.cid = cid;
		this.sender = sender;
	}

	public ActorRef getSender() {
		return sender;
	}

	public Integer getCid() {
		return cid;
	}

	@Override
	public String toString() {
		return "[ Sender: " + sender + " Cid: " + cid + "]";
	}
}
