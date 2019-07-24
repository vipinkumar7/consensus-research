package consensus.research.fsm.simple;

public final class Queue {
	private final Object obj;

	public Queue(Object obj) {
		this.obj = obj;
	}

	public Object getObj() {
		return obj;
	}

	@Override
	public String toString() {
		return "Queue{" + "obj=" + obj + '}';
	}
}
