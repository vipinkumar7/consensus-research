package consensus.research.log;

import java.util.List;

import consensus.research.election.Term;

public abstract class Entries {

	public List<Entry> log;

	public Entries(List<Entry> log) {
		this.log = log;
	}

	public abstract void persist(List<Entry> entries);

	public List<Entry> append(List<Entry> entries) {
		return append(entries, log.size());
	}

	public List<Entry> append(List<Entry> entries, int size) {
		List<Entry> updated_log = entries.subList(0, size);
		updated_log.addAll(entries);
		persist(updated_log);
		return updated_log;
	}

	public Term termOf(int index) {
		if (index > 0) {
			return log.get(index - 1).term;
		} else
			return new Term(0);
	}

	public int lastIndex() {
		return log.size();
	}

	public Term lastTerm() {
		return termOf(lastIndex());
	}

	public boolean hasEntryAt(int index) {
		if (index >= log.size()) {
			return false;
		} else {
			return true;
		}
	}

	public Entry get(int index) {
		return log.get(index - 1);
	}
	
	public abstract Entries copy();
	
	public abstract Entries copy(List<Entry> en);
}
