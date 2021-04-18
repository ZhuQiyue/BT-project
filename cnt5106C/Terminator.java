package cnt5106C;

public class Terminator extends Thread{
	public void run(){
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}//wait for all logs to be written
		//clear all queues
		int clearQueue = 0;
		while(clearQueue < PeerProcess.peers.size()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			clearQueue = 0;
			for(int i = 0; i < PeerProcess.peers.size(); i++) {
				if(PeerProcess.messageQueues.get(i).isEmpty()) {
					clearQueue++;
				}
			}
			//PeerProcess.write("queue " + clearQueue);
		}
		System.exit(0);
	}
}
