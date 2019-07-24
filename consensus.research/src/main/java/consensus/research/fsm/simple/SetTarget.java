package consensus.research.fsm.simple;

import akka.actor.ActorRef;

public final class SetTarget {
	private final ActorRef ref;

	public SetTarget(ActorRef ref) {
		this.ref = ref;
	}

	public ActorRef getRef() {
		return ref;
	}

	@Override
	public String toString() {
		return "SetTarget{" + "ref=" + ref + '}';
	}
}
