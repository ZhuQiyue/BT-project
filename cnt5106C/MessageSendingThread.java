/**
 * The thread responsible for sending messages to another host.
 */

package cnt5106C;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageSendingThread extends Thread{
	private Socket socket; //The socket connected to the remote host.
	private int remotePeerIndex; //Index of the specific remote host to communicate;
	private int localIndex; //Index of the local host.
	private List<DynamicPeerInfo> peers; //The peerInfo of the remote hosts.
	private ObjectOutputStream output;//The output stream of the socket.
	private List<LinkedBlockingQueue<Message>> queues;//The message queue for thread communication.
	
	public void send(byte[] msg) {
		try {
			output.writeObject(msg);
			output.flush();
			//PeerProcess.write("Send a message to peer " + remotePeerIndex );
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The constructor of the thread.
	 * @param socket
	 * @param remotePeerIndex
	 */
	public MessageSendingThread(Socket socket, int remotePeerIndex){
		this.socket = socket;
		this.peers = PeerProcess.peers;
		this.queues = PeerProcess.messageQueues;
		this.remotePeerIndex = remotePeerIndex;
		this.localIndex = PeerProcess.index;
		try {
			output = new ObjectOutputStream(socket.getOutputStream());
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *Run the thread.
	 */
	public void run() {
		// PeerProcess.write("MessageSendingThread start to work.");
		while(true) {
			try {
				Message message = queues.get(remotePeerIndex).take();//This is a blocking queue and supposed to be thread safe
				message.execute(this);//Core of this project
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
