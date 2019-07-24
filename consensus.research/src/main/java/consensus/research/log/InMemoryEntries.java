package consensus.research.log;

import java.util.List;

public class InMemoryEntries extends Entries{

	public InMemoryEntries(List<Entry> log) {
		super(log);
	}

	@Override
	public void persist(List<Entry> entries) {
		
	}

	@Override
	public Entries copy() {
		return new InMemoryEntries(log);
	}

	@Override
	public Entries copy(List<Entry> en) {
		return new InMemoryEntries(en);
	}
}
