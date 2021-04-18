/**
 * The main process in every host to coordinate other components.
 */

package cnt5106C;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import java.io.IOException;
import java.net.*;

public class PeerProcess {
	public static int peerId; // The peerId of this process, reading from console.
	public static int index; // The index of this process, counting from up to down in peerInfo.cfg
	public static List<DynamicPeerInfo> peers; // An array that saves all peerInfos.
	public static String fileName; // The name of the file to be distributed.
	public static int fileSize; // The size of the file in bytes.
	public static int pieceSize; // The size of the piece in bytes.
	public static int numOfPieces; // The # of the pieces

	// An array of queues for all the threads to send message to each other.
	public static List<LinkedBlockingQueue<Message>> messageQueues = new ArrayList<>();
	
	public static DecisionMaker dm;

	protected static int preferredNeighborsCount; // The number of preferred neighbors.
	protected static int unchokingInterval; // The interval of switching unchocking neighbors.
	protected static int optUnchokingInterval; // The interval of switching optimistic unchocking neighbors.

	private static ArrayList<Integer> peerIdOfPeersWhoInterestedInLocalPieces = new ArrayList<Integer>();

	public static FileHelper fileHelper;
	public static Logger logger;
	
	public static void addInterestPeer(int peerId) {
		synchronized(peerIdOfPeersWhoInterestedInLocalPieces) {
			peerIdOfPeersWhoInterestedInLocalPieces.add(peerId);
		}
	}
	
	public static void removeInterestPeer(int peerId) {
		synchronized(peerIdOfPeersWhoInterestedInLocalPieces) {
			peerIdOfPeersWhoInterestedInLocalPieces.remove(Integer.valueOf(peerId));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<Integer> getInterestedPeers() {
		synchronized(peerIdOfPeersWhoInterestedInLocalPieces) {
			return (ArrayList<Integer>) peerIdOfPeersWhoInterestedInLocalPieces.clone();
		}
	}
	
	public static int getInterestedPeerSize() {
		synchronized(peerIdOfPeersWhoInterestedInLocalPieces) {
			return peerIdOfPeersWhoInterestedInLocalPieces.size();
		}
	}
	
	public static boolean isPeerInterested(int peerId) {
		synchronized(peerIdOfPeersWhoInterestedInLocalPieces) {
			return peerIdOfPeersWhoInterestedInLocalPieces.contains(Integer.valueOf(peerId));
		}
	}

	public static void write(String msg) {
		try {
			logger.log(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read common.cfg and PeerInfo.cfg into some data structures.
	 */
	private static void readConfigFiles() {
		Config.init();
		preferredNeighborsCount = Config.getNumberOfPreferredNeighbors();
		unchokingInterval = Config.getUnchokingInterval();
		optUnchokingInterval = Config.getOptimisticUnchokingInterval();
		fileName = Config.getFileName();
		fileSize = Config.getFileSize();
		pieceSize = Config.getPieceSize();
		if(fileSize % pieceSize == 0) {
			numOfPieces = fileSize / pieceSize;// How many pieces are there in a file.
		}else {
			numOfPieces = fileSize / pieceSize + 1;
		}

		peers = PeerInfo.readPeerInfo(numOfPieces);
	}

	/**
	 * Find the index of a process. We need it because we should connect all the
	 * processes before.
	 * 
	 * @param peerId of that process.
	 * @return index of that process.
	 */
	public static int getIndex(int peerId) {
		int index = 0;
		for (DynamicPeerInfo p : peers) {
			if (p.peerId == peerId) {
				break; // We find this process.
			} else {
				index++;
			}
		}
		return index;
	}
	
	public static void checkTermination() throws InterruptedException {
		int numberOfCompleteFiles = 0;
		for(DynamicPeerInfo p: peers) {
			if(p.hasCompleteFile) {
				numberOfCompleteFiles++;
				//write(p.peerId + " has completeFIle");
			}else {
				break;
			}
		}
		if(numberOfCompleteFiles == peers.size()) {
			//PeerProcess.write("terminate because of Checking");
			Terminator t = new Terminator();
			t.start();
		}
	}

	/**
	 * The main function for every peerProcess.
	 * 
	 * @param args an argument from console, which is the peerId of that particular
	 *             peerProcess
	 * @throws IOException When serverSocket doesn't work well.
	 */
	public static void main(String[] args) throws IOException {

		peerId = Integer.parseInt(args[0]); // Read PeerId from console arguments.
		readConfigFiles();
		logger = new Logger(args[0]);
		index = getIndex(peerId); // Find the index of this process.

		dm = new DecisionMaker();// The real controller, an individual thread to manage everything
		dm.start();

		fileHelper = new FileHelper();

		// We need to create sockets towards all the hosts before our index.
		for (int i = 0; i < index; i++) {
			try {
				// We need to create the socket towards remote port.
				write("makes a connection to Peer " + peers.get(i).peerId);
				Socket beforeSocket = new Socket(peers.get(i).address, peers.get(i).port);
				messageQueues.add(new LinkedBlockingQueue<Message>());// New message Queue for new thread.
				// Create the upstream handler.
				MessageSendingThread sendingThread = new MessageSendingThread(beforeSocket, i);
				sendingThread.start();
				// Create the downstream handler.
				MessageReceivingThread receivingThread = new MessageReceivingThread(beforeSocket, i);
				receivingThread.start();

				peers.get(i).isConnected = true;// Done connection, can send any message to it

				// We send a handshake message after TCP connection established
				messageQueues.get(i).put(HandshakeHandler.construct(peers.get(i).peerId));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		messageQueues.add(new LinkedBlockingQueue<Message>());// The queue for the local host itself.

		// We only need 1 serverSocket since we have only 1 port to listen for each
		// host.
		ServerSocket serverSocket = new ServerSocket(peers.get(index).port);
		// PeerProcess.write("ServerSocket at port " + peers.get(index).port + " started  listening");
		int counter = 1;// Keep track of while loops so we can calculate index of peers.
		try {
			while (true) {
				// This is only called by afterward peers. We will positively connect to peers
				// before, not listen to them.
				Socket afterwardSocket = serverSocket.accept();

				InetAddress ipAddress = afterwardSocket.getInetAddress();// Get the ipAddress of remote host from
																			// socket.
				// write("accepting a socket from " + ipAddress.getHostName());
				write("is connected from Peer " + peers.get(index + counter).peerId);
				messageQueues.add(new LinkedBlockingQueue<Message>());// New message Queue for new thread.
				// Create the upstream handler.
				MessageSendingThread sendingThread = new MessageSendingThread(afterwardSocket, index + counter);
				sendingThread.start();
				// Create the downstream handler.
				MessageReceivingThread receivingThread = new MessageReceivingThread(afterwardSocket, index + counter);
				receivingThread.start();

				peers.get(index + counter).isConnected = true;// Done connection, can send any message to it

				// We send a handshake message after TCP connection established
				messageQueues.get(index + counter).put(HandshakeHandler.construct(peers.get(index + counter).peerId));

				counter++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			serverSocket.close();
		}
	}
}
