package consensus.research.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import akka.actor.ActorRef;

public class Log {

	private Entries entries;
	private Map<ActorRef, Integer> nextIndex;
	private Map<ActorRef, Integer> matchIndex;
	private Integer commitIndex = 0;
	private Integer lastApplied = 0;

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

	}

	private Log(Entries entries, Map<ActorRef, Integer> nextIndex, Map<ActorRef, Integer> matchIndex,
			Integer commitIndex) {
		this.entries = entries;
		this.nextIndex = nextIndex;
		this.matchIndex = matchIndex;
		this.commitIndex = commitIndex;
	}

	private Log(Entries entries, Map<ActorRef, Integer> nextIndex, Map<ActorRef, Integer> matchIndex,
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
			new_nextIndex.put(node, to);
			return new Log(entries.copy(), new_nextIndex, new_matchIndex, commitIndex);
		} else {
			Map<ActorRef, Integer> new_matchIndex = new HashMap<ActorRef, Integer>(matchIndex);
			new_nextIndex.put(node, new_matchIndex.get(node) + 1);
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
		return entries;
	}

	public void setEntries(Entries entries) {
		this.entries = entries;
	}

	public Map<ActorRef, Integer> getNextIndex() {
		return nextIndex;
	}

	public void setNextIndex(Map<ActorRef, Integer> nextIndex) {
		this.nextIndex = nextIndex;
	}

	public Map<ActorRef, Integer> getMatchIndex() {
		return matchIndex;
	}

	public void setMatchIndex(Map<ActorRef, Integer> matchIndex) {
		this.matchIndex = matchIndex;
	}

	public Integer getCommitIndex() {
		return commitIndex;
	}

	public void setCommitIndex(Integer commitIndex) {
		this.commitIndex = commitIndex;
	}

	public Integer getLastApplied() {
		return lastApplied;
	}

	public void setLastApplied(Integer lastApplied) {
		this.lastApplied = lastApplied;
	}
}
