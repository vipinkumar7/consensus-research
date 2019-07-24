package consensus.research.core;

public abstract class ReplicatedStateMachine<T> {

	public abstract  T  execute(Command<T> command) ;
}
