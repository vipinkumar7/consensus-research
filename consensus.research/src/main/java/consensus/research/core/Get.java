package consensus.research.core;

/**
 * 
 * @author kvipin
 * Get object implemented command 
 */
public class Get  extends Command<Integer>{
	
	
	@Override
	Type getType() {
		return Type.GET;
	}
}
