package consensus.research.election;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import akka.actor.ActorRef;

/**
 * 
 * @author kvipin
 *
 */
public class Votes {

	private final ActorRef votedFor;
	private final List<ActorRef> received;

	public Votes() {
		votedFor = null;
		received = new ArrayList<ActorRef>();
	}

	public Votes(ActorRef votedFor, List<ActorRef> received) {
		this.votedFor = votedFor;
		this.received = received;
	}

	public Votes gotVoteFrom(ActorRef ref) {
		List<ActorRef> new_received = new ArrayList<>(received);
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

	public List<ActorRef> getReceived() {
		return new ArrayList<>(received);
	}

	@Override
	public String toString() {
		return "[ Actor Votefor: " + votedFor + "Received " + Arrays.toString(received.toArray()) + "]";
	}

}
