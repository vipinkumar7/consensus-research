package consensus.research.election;

public class Term implements Comparable<Term> {

	Integer current;

	public Term(Integer current) {

		this.current = current;
	}

	@Override
	public int compareTo(Term o) {
		if (this.current > o.current)
			return 1;
		if (this.current < o.current)
			return -1;
		else
			return 0;
	}

	public Term nextTerm() {
		return new Term(current + 1);
	}

	public static Term max(Term t1, Term t2) {
		if (t1.compareTo(t2) > 0)
			return t1;

		else
			return t2;

	}
	
	public Term copy() {
		return new Term(this.current);
	}
}
