/**
 * The dynamic data structure to save a single peer Information.
 */

package cnt5106C;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DynamicPeerInfo {
	public int index;//Index of the remote peer. Follow the order in peerInfo.cfg
	public boolean isConnected;//If the local host already established a TCP connection to remote host
	public boolean isLocalPeerChockingRemotePeer;//If the remote peer is chocked or not
	public boolean isRemotePeerChockingLocalPeer;//Necessary because we need to check before send a request
	public boolean isRemotePeerInterestedInLocalPeer;//If the remote peer is interested or not
	public boolean isLocalPeerInterestedInRemotePeer;//Necessary because we don't want to send duplicate interested message
	public int peerId; //The id of the remote peer, for example, 1001
	public String address; //The domain name address of the peer, for example, lin114-00.cise.ufl.edu
	public InetAddress ipAddress; //The ip address of this peer.
	public int port; //The port of the peer, for example, 6001
	public boolean hasFileInitially; //If the peer has the file initially, this suppose to be true.
	public List<Boolean> filePieces; //A BitSet that keep track of whether a peer has any piece or not. 
							  //If the bit at n is 1, then this peer has piece n right now. Do not access it directly
	public List<Integer> interestedFilePieces;//The filePieces remote peer has and local peer don't. never access it directly, although its public
	private int chunkCount = 0;
	private int totalFilePiecesReceived = 0;
	public boolean hasCompleteFile;
	public Object lock = new Object();
	
	/**
	 * The constructor of PeerInfo data structure.
	 * @param peerId
	 * @param address
	 * @param port
	 * @param hasFileInitially
	 * @param numOfPieces How many pieces in the file.
	 * @param index index of the remote host
	 */
	public DynamicPeerInfo(int peerId, String address, int port, boolean hasFileInitially, int numOfPieces, int index) {
		this.isConnected = false;
		this.peerId = peerId;
		this.address = address;
		isLocalPeerChockingRemotePeer = true;
		isRemotePeerChockingLocalPeer = true;
		isRemotePeerInterestedInLocalPeer = false;
		isLocalPeerInterestedInRemotePeer = false;
		try {
			ipAddress = InetAddress.getByName(address);//Get the real ip address from host name.
		}catch(UnknownHostException e) {
			e.printStackTrace();
		}
		this.port = port;
		this.index = index;
		this.hasFileInitially = hasFileInitially;
		hasCompleteFile = hasFileInitially;
		filePieces = new ArrayList<>();
		for(int i = 0; i < numOfPieces; i++) {
			if(hasFileInitially) {
				filePieces.add(true);
			}else {
				filePieces.add(false);
			}
		}
		interestedFilePieces = new ArrayList<Integer>();
	}

	public void incrementChunkCounter() {
		synchronized(lock){
			chunkCount++;
		}
	}			
	
	public void setFilePieceState(int fileIndex, boolean value) {
		if(PeerProcess.index == this.index) {
			// System.out.print("setting local file piece " + index);
			//setting local peer
			synchronized(lock) {
				filePieces.set(fileIndex, value);
				//We never loss a local file piece after we have it, so value must be true
				totalFilePiecesReceived++;
				if (totalFilePiecesReceived == PeerProcess.numOfPieces) {
					hasCompleteFile = true;
					PeerProcess.write("has downloaded the complete file");
				}
			}
			for(DynamicPeerInfo p: PeerProcess.peers) {
				synchronized(p.lock) {
					if(p.index != PeerProcess.index) {
						//If it is a remote peer
						for(int i = 0; i < p.interestedFilePieces.size(); i++) {
							if(p.interestedFilePieces.get(i) == fileIndex) {
								// PeerProcess.write("removing interest " + i);
								//Since we have the file piece right now, it is not interested any more
								p.interestedFilePieces.remove(i);
								break;
							}
						}
					}
				}
			}
		}else {
			synchronized(lock) {
				//setting remote peer
				filePieces.set(fileIndex, value);
				if(value == true) {
					totalFilePiecesReceived++;
					//PeerProcess.write(" " + peerId + " totalFilePiecesReceived " + totalFilePiecesReceived);
					if(totalFilePiecesReceived == PeerProcess.numOfPieces) {
						hasCompleteFile = true;
					}
				}
				if(value == true && PeerProcess.peers.get(PeerProcess.index).filePieces.get(fileIndex) == false) {
					//They have it, we don't, so we are interested in it
					interestedFilePieces.add(Integer.valueOf(fileIndex));
				}
			}
		}
	}
	
	public boolean getFilePieceState(int index) {
		boolean result;
		synchronized(lock){
			result = filePieces.get(index);
		}
		return result;
	}
	
	public boolean isFilePiecesEmpty() {
		boolean result = true;
		synchronized(lock){
			for(Boolean b: filePieces) {
				if(b == true) {
					result = false;
				}
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Integer> getInterestedList() {
		ArrayList<Integer> temp;
		synchronized(lock) {
			temp = (ArrayList<Integer>) interestedFilePieces;
		}
		return temp;
	}
	
	public Integer getAnInterestedIndex() {
		synchronized(lock) {
			if(interestedFilePieces.isEmpty()) {
				return null;
			}else {
				int requestIndex = interestedFilePieces.get((int)(Math.random() * interestedFilePieces.size()));
				return requestIndex;
			}
		}
	}
	
	public boolean isThereAnyInterestedFilePieces() {
		boolean result;
		synchronized(lock) {
			result = !interestedFilePieces.isEmpty();
		}
		return result;
	}
	
	public int getTotalFilePiecesWeReceived() {
		synchronized(lock) {
			return totalFilePiecesReceived;
		}
	}
	
	public void resetChunkCount() {
		synchronized(lock) {
			chunkCount = 0;
		}
	}
	
	public int getChunkCount() {
		synchronized(lock) {
			return chunkCount;
		}
	}
}