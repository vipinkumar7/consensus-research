package consensus.research.fsm;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import consensus.research.messages.Init;

public class Main {

	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create("raft");
		try {
			List<ActorRef> refs = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				refs.add(system.actorOf(Raft.props(), "member" + i));
			}
			
			for (ActorRef ref : refs) {
				ref.tell(new Init(refs), ref);
			}
			
			ActorRef client = system.actorOf(Sequencer.props(), "client");
			
			
			
			System.out.println("Press ENTER to exit the system");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			system.terminate();
		}
	}
}
