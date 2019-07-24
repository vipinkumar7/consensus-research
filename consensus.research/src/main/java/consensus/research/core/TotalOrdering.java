package consensus.research.core;

public class TotalOrdering extends ReplicatedStateMachine<Integer> {

	private int state = 0;

	@Override
	public Integer execute(Command<Integer> command) {
		
		switch (command.getType()) {
		case GET:
			state=state+1;
			break;

		default:
			break;
		}
		return state;
	}

}
