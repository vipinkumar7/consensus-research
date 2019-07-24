package consensus.research.election;

import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;

public class Votes {

	private ActorRef votedFor;
	private List<ActorRef> received = new ArrayList<>();

	public Votes() {

	}

	public Votes(ActorRef votedFor, List<ActorRef> received) {
		this.votedFor = votedFor;
		this.received = received;
	}

	public Votes gotVoteFrom(ActorRef ref) {
		List<ActorRef> new_received = new ArrayList<>();
		new_received.add(ref);
		return new Votes(votedFor, new_received);
	}

	public boolean majority(int size) {
		return (this.received.size()) >= Math.ceil(size / 2.0);
	}

	public Votes vote(ActorRef ref) {
		List<ActorRef> new_received = new ArrayList<>(received);
		if (votedFor != null) {
			return new Votes(votedFor, new_received);
		} else
			return new Votes(ref, new_received);
	}

	public ActorRef getVotedFor() {
		return votedFor;
	}

	public void setVotedFor(ActorRef votedFor) {
		this.votedFor = votedFor;
	}

	public List<ActorRef> getReceived() {
		return received;
	}

	public void setReceived(List<ActorRef> received) {
		this.received = received;
	}

}
