/**
 * The thread responsible for receiving messages from another host.
 */

package cnt5106C;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageReceivingThread extends Thread{
	private Socket socket; //The socket connected to the remote host.
	private int remotePeerIndex; //Index of the specific remote host to communicate;
	private int localIndex; //Index of the local host.
	private List<DynamicPeerInfo> peers; //The peerInfo of the remote hosts.
	private ObjectInputStream input; //The input stream of the socket.
	private List<LinkedBlockingQueue<Message>> queues;//The message queue for thread communication.
	
	/**
	 * The constructor of the thread.
	 * @param socket
	 * @param remotePeerIndex
	 */
	public MessageReceivingThread(Socket socket, int remotePeerIndex){
		this.socket = socket;
		this.peers = PeerProcess.peers;
		this.queues = PeerProcess.messageQueues;
		this.remotePeerIndex = remotePeerIndex;
		this.localIndex = PeerProcess.index;
		try {
			input = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *Run the thread.
	 */
	public void run() {
		// PeerProcess.write("Message Receiving Thread start to work");
		while(true) {
			try {
				byte[] msg = (byte[]) input.readObject();
				// PeerProcess.write("Receive message from peer " + remotePeerIndex + ;
				// After we receive the msg, we put it into the specific queue, and let upstreamHandler decide how to deal with it.
				queues.get(remotePeerIndex).put(new Message(msg, PeerProcess.peers.get(remotePeerIndex).peerId, true));
			} catch(SocketException e) {
				//PeerProcess.write("terminate because of Exception");
				Terminator t = new Terminator();
				t.start();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
