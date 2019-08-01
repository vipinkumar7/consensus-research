package consensus.research.messages;

import consensus.research.election.Term;

/**
 * 
 * @author kvipin
 *
 */
public class AppendFailure extends AppendReply {

	private final Term term;

	public AppendFailure(Term term) {
		this.term = term;
	}

	public Term getTerm() {
		return term.copy();
	}

	@Override
	public String toString() {
		return "Appendfail( " + term.toString() + ")";
	}

}
