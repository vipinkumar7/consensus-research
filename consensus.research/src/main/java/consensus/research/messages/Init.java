package consensus.research.messages;

import java.util.List;

import akka.actor.ActorRef;

public class Init extends Message {

	List<ActorRef> nodes;

	public Init(List<ActorRef> nodes) {
		this.nodes = nodes;
	}

	public List<ActorRef> getNodes() {
		return nodes;
	}

	public void setNodes(List<ActorRef> nodes) {
		this.nodes = nodes;
	}

}
