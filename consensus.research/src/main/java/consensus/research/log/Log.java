package consensus.research.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import akka.actor.ActorRef;

public class Log {

	private final Entries entries;
	private final Map<ActorRef, Integer> nextIndex;
	private final Map<ActorRef, Integer> matchIndex;
	private final Integer commitIndex;
	private final Integer lastApplied;

	public Log(Entries entries, List<ActorRef> nodes) {
		int nextIndex = entries.lastIndex() + 1;
		Map<ActorRef, Integer> nextIndices = new HashMap<>();
		Map<ActorRef, Integer> matchIndices = new HashMap<>();
		for (ActorRef n : nodes) {
			nextIndices.put(n, nextIndex);
		}
		for (ActorRef n : nodes) {
			matchIndices.put(n, 0);
		}
		this.nextIndex = nextIndices;
		this.matchIndex = matchIndices;
		this.entries = entries;
		this.commitIndex = 0;
		this.lastApplied = 0;
	}

	public Log(Entries entries, Map<ActorRef, Integer> nextIndex, Map<ActorRef, Integer> matchIndex,
			Integer commitIndex) {
		this.entries = entries;
		this.nextIndex = nextIndex;
		this.matchIndex = matchIndex;
		this.commitIndex = commitIndex;
		this.lastApplied = 0;
	}

	public Log(Entries entries, Map<ActorRef, Integer> nextIndex, Map<ActorRef, Integer> matchIndex,
			Integer commitIndex, Integer lastApplied) {
		this.entries = entries;
		this.nextIndex = nextIndex;
		this.matchIndex = matchIndex;
		this.commitIndex = commitIndex;
		this.lastApplied = lastApplied;
	}

	public Log decrementNextFor(ActorRef node) {
		Map<ActorRef, Integer> new_nextIndex = new HashMap<ActorRef, Integer>(nextIndex);
		new_nextIndex.put(node, nextIndex.get(node) - 1);
		Map<ActorRef, Integer> new_matchIndex = new HashMap<ActorRef, Integer>(matchIndex);
		return new Log(entries.copy(), new_nextIndex, new_matchIndex, commitIndex);
	}

	public Log resetNextFor(ActorRef node) {
		Map<ActorRef, Integer> new_nextIndex = new HashMap<ActorRef, Integer>(nextIndex);
		new_nextIndex.put(node, entries.lastIndex() + 1);
		Map<ActorRef, Integer> new_matchIndex = new HashMap<ActorRef, Integer>(matchIndex);
		return new Log(entries.copy(), new_nextIndex, new_matchIndex, commitIndex);
	}

	public Log matchFor(ActorRef node, Integer to) {
		Map<ActorRef, Integer> new_nextIndex = new HashMap<ActorRef, Integer>(nextIndex);
		if (to != null) {
			Map<ActorRef, Integer> new_matchIndex = new HashMap<ActorRef, Integer>(matchIndex);
			new_matchIndex.put(node, to);
			return new Log(entries.copy(), new_nextIndex, new_matchIndex, commitIndex);
		} else {
			Map<ActorRef, Integer> new_matchIndex = new HashMap<ActorRef, Integer>(matchIndex);
			new_matchIndex.put(node, matchIndex.get(node) + 1);
			return new Log(entries.copy(), new_nextIndex, new_matchIndex, commitIndex);
		}
	}

	public Log commit(int index) {
		Map<ActorRef, Integer> new_nextIndex = new HashMap<ActorRef, Integer>(nextIndex);
		Map<ActorRef, Integer> new_matchIndex = new HashMap<ActorRef, Integer>(matchIndex);
		return new Log(entries.copy(), new_nextIndex, new_matchIndex, index);
	}

	public Log applied() {
		Map<ActorRef, Integer> new_nextIndex = new HashMap<ActorRef, Integer>(nextIndex);
		Map<ActorRef, Integer> new_matchIndex = new HashMap<ActorRef, Integer>(matchIndex);
		return new Log(entries.copy(), new_nextIndex, new_matchIndex, commitIndex, lastApplied + 1);
	}

	public Log copy() {
		Map<ActorRef, Integer> new_nextIndex = new HashMap<ActorRef, Integer>(nextIndex);
		Map<ActorRef, Integer> new_matchIndex = new HashMap<ActorRef, Integer>(matchIndex);
		return new Log(entries.copy(), new_nextIndex, new_matchIndex, commitIndex, lastApplied);
	}

	public Log copy(List<Entry> en) {
		Map<ActorRef, Integer> new_nextIndex = new HashMap<ActorRef, Integer>(nextIndex);
		Map<ActorRef, Integer> new_matchIndex = new HashMap<ActorRef, Integer>(matchIndex);
		return new Log(entries.copy(en), new_nextIndex, new_matchIndex, commitIndex, lastApplied);
	}

	public Entries getEntries() {
		return entries.copy();
	}

	public Map<ActorRef, Integer> getNextIndex() {
		return new HashMap<ActorRef, Integer>(nextIndex);
	}

	public Map<ActorRef, Integer> getMatchIndex() {
		return new HashMap<ActorRef, Integer>(matchIndex);
	}

	public Integer getCommitIndex() {
		return commitIndex;
	}

	public Integer getLastApplied() {
		return lastApplied;
	}

	public Log copy(Map<ActorRef, Integer> new_nextIndex, Map<ActorRef, Integer> new_matchIndex) {
		return new Log(entries.copy(), new_nextIndex, new_matchIndex, commitIndex, lastApplied);
	}

	@Override
	public String toString() {
		return "Entries ( " + entries.toString() + " NextIndex:" + toStringMap(nextIndex) + "MatchIndex:" + toStringMap(matchIndex)
				+ " commitIndex: " + commitIndex + "LastApplied:" + lastApplied + ")";
	}
	
	private String toStringMap(Map<ActorRef, Integer> map) {
		StringBuilder builder=new StringBuilder();
		builder.append("{");
		Set<java.util.Map.Entry<ActorRef, Integer>> enSet=map.entrySet();
		for (java.util.Map.Entry<ActorRef, Integer> en:enSet) {
			builder.append(en.getKey()+" : "+ en.getValue());
		}
		builder.append("}");
		return builder.toString();
		
	}
}
