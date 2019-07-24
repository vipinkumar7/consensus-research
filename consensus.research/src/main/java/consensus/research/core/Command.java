package consensus.research.core;

public abstract class Command<T> {

	enum Type {
		GET
	}
	
	abstract Type getType();
}
