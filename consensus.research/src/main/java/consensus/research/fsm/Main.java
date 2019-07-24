package consensus.research.fsm;

import java.io.IOException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Main {

	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create("raft");
		try {

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
