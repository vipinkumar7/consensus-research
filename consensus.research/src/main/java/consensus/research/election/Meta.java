package consensus.research.election;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import akka.actor.ActorRef;
import consensus.research.core.TotalOrdering;
import consensus.research.log.Entries;
import consensus.research.log.Entry;
import consensus.research.log.InMemoryEntries;
import consensus.research.log.Log;

public class Meta {

	private Term term;
	private Log log;
	private final TotalOrdering rsm;
	private List<ActorRef> nodes;
	private Votes votes = new Votes();
	private ActorRef leader;

	public Meta(List<ActorRef> nodes) {

		term = new Term(0);
		Entries entries = new InMemoryEntries(new ArrayList<Entry>());
		log = new Log(entries, nodes);
		rsm = new TotalOrdering();
		this.nodes = nodes;
	}

	public void leaderAppend(ActorRef ref, List<Entry> e) {
		List<Entry> en = log.getEntries().append(e);
		log = log.copy(en);
		log = log.resetNextFor(ref);
		log = log.matchFor(ref, log.getEntries().lastIndex());
	}

	public void append(List<Entry> e, Integer at) {
		log = log.copy(log.getEntries().append(e, at));
	}

	public void selectTerm(Term other) {
		if (other.compareTo(term) > 0) {
			term = other;
			votes = new Votes();
		}
	}

	public void nextTerm() {
		votes = new Votes();
		term = term.nextTerm();
	}

	public void setLeader(ActorRef ref) {
		leader = ref;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public Log getLog() {
		return log;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public TotalOrdering getRsm() {
		return rsm;
	}

	public List<ActorRef> getNodes() {
		return nodes;
	}

	public void setNodes(List<ActorRef> nodes) {
		this.nodes = nodes;
	}

	public Votes getVotes() {
		return votes;
	}

	public void setVotes(Votes votes) {
		this.votes = votes;
	}

	public ActorRef getLeader() {
		return leader;
	}

	@Override
	public String toString() {
		return " Meta(" + term.toString() + " , " + log.toString() + " , " + rsm.toString() + " , Nodes: " + Arrays.toString(nodes.toArray())
				+ " , " + votes.toString() + " , Leader: " + leader + ")";
	}
}
