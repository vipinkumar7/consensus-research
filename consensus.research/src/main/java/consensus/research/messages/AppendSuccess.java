package consensus.research.messages;

import consensus.research.election.Term;

/**
 * 
 * @author kvipin
 *
 */
public class AppendSuccess extends AppendReply {

	private final Term term;
	private final int index;

	public AppendSuccess(Term term, int index) {
		this.term = term;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public Term getTerm() {
		return term.copy();
	}

	@Override
	public String toString() {
		return "AppendSuccess( " + term.toString() + " Index  : " + index + ")";
	}

}
