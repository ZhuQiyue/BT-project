/**
 * The real Control Thread, running in parallel, used to decide who to choke, who to unchoke and stuffs like that.
 */

package cnt5106C;

import java.util.*;

public class DecisionMaker extends Thread {
	private ArrayList<Integer> preferredPeers = new ArrayList<Integer>();                       // An array that
																								// maintains preferred
																								// peerInfos.
	private int optUnchockedPeer = -1;

	private ArrayList<Integer> previousRequestList = new ArrayList<>();
	private ArrayList<Integer> requestingList = new ArrayList<>();
	
	/**
	 * Update preferred peers
	 */
	private class updatePreferredPeers extends TimerTask {
		public void run() {
			synchronized (preferredPeers) {
				if (PeerProcess.peers.size() > 0) {
					preferredPeers.clear();
				}
				
				if(PeerProcess.peers.get(PeerProcess.index).hasCompleteFile) {
					ArrayList<Integer> interestedPeers = PeerProcess.getInterestedPeers();
					Collections.shuffle(interestedPeers);
					for(int peerId: interestedPeers) {
						if(preferredPeers.size() < PeerProcess.preferredNeighborsCount) {
							preferredPeers.add(peerId);
						}
					}
				}else {
					ArrayList<PeerId_ChunkCount> peerId_ChunkCounts = new ArrayList<>();
					for(int peerId: PeerProcess.getInterestedPeers()) {
						peerId_ChunkCounts.add(new PeerId_ChunkCount(peerId, PeerProcess.peers.get(PeerProcess.getIndex(peerId)).getChunkCount()));
					}
					peerId_ChunkCounts.sort(null);
					
					for(PeerId_ChunkCount tuple: peerId_ChunkCounts) {
						if(preferredPeers.size() < PeerProcess.preferredNeighborsCount) {
							preferredPeers.add(tuple.peerId);
						}
					}
					
					for(DynamicPeerInfo p: PeerProcess.peers) {
						p.resetChunkCount();
					}
				}

				// iterate over all peers to check and send proper choke/unchoke msgs
				for (DynamicPeerInfo p : PeerProcess.peers) {
					if (p.isConnected) {
						if (preferredPeers.contains(p.peerId) && p.isLocalPeerChockingRemotePeer) {
							// PeerProcess.write("Unchoking peer " + p.peerId);
							PeerProcess.messageQueues.get(p.index).add(ChokeUnchokeHandler.construct(p.peerId, false));
							p.isLocalPeerChockingRemotePeer = false;
							continue;
						}
						if (!preferredPeers.contains(p.peerId) && !p.isLocalPeerChockingRemotePeer && optUnchockedPeer != p.peerId) {
							// PeerProcess.write("Choking peer " + p.peerId);
							PeerProcess.messageQueues.get(p.index).add(ChokeUnchokeHandler.construct(p.peerId, true));
							p.isLocalPeerChockingRemotePeer = true;
						}
					}
				}
				PeerProcess.write("has the preferred neighbors " + preferredPeers);
			}
		}

	}

	/**
	 * Optimistically unchoke a peer
	 */
	private class optimisiticUnchoke extends TimerTask {
		public void run() {
			synchronized (preferredPeers) {
				try {
					PeerProcess.checkTermination();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				ArrayList<Integer> chockedList = new ArrayList<>();
				for(DynamicPeerInfo p: PeerProcess.peers) {
					if(p.isLocalPeerChockingRemotePeer && PeerProcess.isPeerInterested(p.peerId)) {
						chockedList.add(p.index);
					}
				}
				if(!chockedList.isEmpty()) {
					int index = (int) (Math.random() * chockedList.size());
					optUnchockedPeer = PeerProcess.peers.get(chockedList.get(index)).peerId;
					PeerProcess.messageQueues.get(chockedList.get(index)).add(ChokeUnchokeHandler.construct(optUnchockedPeer, false));
					PeerProcess.peers.get(chockedList.get(index)).isLocalPeerChockingRemotePeer = false;
					PeerProcess.write("has the optimistically unchoked neighbor " + optUnchockedPeer);
				}
			}
		}
	}
	
	private class requestTimeout extends TimerTask {
		public void run() {
			synchronized(requestingList) {
				for(int i = 0; i < previousRequestList.size(); i++) {
					if(requestingList.contains(previousRequestList.get(i))) {
						requestingList.remove(Integer.valueOf(previousRequestList.get(i)));
					}
				}
				previousRequestList = requestingList;
			}
		}
	}
	
	public void removeRequest(int pieceIndex) {
		synchronized(requestingList) {
			requestingList.remove(Integer.valueOf(pieceIndex));
		}
	}
	
	public boolean addRequest(int pieceIndex) {
		synchronized(requestingList) {
			if(requestingList.contains(pieceIndex)) {
				return false;
			}else {
				requestingList.add(pieceIndex);
				return true;
			}
		}
	}

	/**
	 * Main method of decision maker as a thread.
	 */
	@Override
	public void run() {
		Timer timerUpdate = new Timer();// create a new Timer
		Timer timerOptUpdate = new Timer();
		Timer timerRequestTimeout = new Timer();
		TimerTask task1 = new optimisiticUnchoke();
		TimerTask task2 = new updatePreferredPeers();
		TimerTask task3 = new requestTimeout();
		timerUpdate.schedule(task1, 1000, PeerProcess.unchokingInterval * 1000);
		timerOptUpdate.schedule(task2, 6000, PeerProcess.optUnchokingInterval * 1000);
		timerRequestTimeout.schedule(task3, 0, 10000);
	}
}
