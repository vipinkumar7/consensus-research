package consensus.research.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import consensus.research.election.Term;

public abstract class Entries {

	public final List<Entry> log;

	public Entries(List<Entry> log) {
		this.log = log;
	}

	public abstract void persist(List<Entry> entries);

	public List<Entry> append(List<Entry> entries) {
		return append(entries, log.size());
	}

	public List<Entry> append(List<Entry> entries, int size) {
		List<Entry> updated_log = size == 0 ? log.subList(0, 0) : log.subList(0, size > log.size() ? log.size() : size);// TODO
		updated_log.addAll(entries);
		persist(updated_log);
		return updated_log;
	}

	public Term termOf(int index) {
		if (index > 0) {
			return log.get(index - 1).getTerm();
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
		if (index > log.size()) {
			return false;
		} else {
			return true;
		}
	}

	public Entry get(int index) {
		return log.get(index );
	}

	public abstract Entries copy();

	public abstract Entries copy(List<Entry> en);

	public List<Entry> takeRight(int num) {
		List<Entry> en = new ArrayList<Entry>();
		if (num > log.size())
			en.addAll(log);
		else {
			int len = log.size() - 1;
			for (int i = len; i > (len - num); i--) {
				en.add(log.get(i));
			}
		}
		Collections.reverse(en);
		return en;
	}

	@Override
	public String toString() {
		return " Entries ( " + Arrays.toString(log.toArray()) + " )";
	}
}
