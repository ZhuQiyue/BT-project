/*
 * The handler to handle any functionalities about interested/not interested message.
 */

package cnt5106C;

import cnt5106C.Message.*;

public class InterestHandler {
	/**
	 * Construct a Interested message.
	 * 
	 * @param remotePeerId the peerid of remote peer
	 * @return the message just has been constructed
	 */
	public static Message construct(int remotePeerId, boolean isInterested) {
		if (isInterested) {
			// PeerProcess.write("Sending interested message to "+remotePeerId);
			return Message.actualMessageWrapper(remotePeerId, 2, new byte[0]);
		} else {
			// PeerProcess.write("Sending not interested message to "+remotePeerId);
			return Message.actualMessageWrapper(remotePeerId, 3, new byte[0]);
		}
	}

	/**
	 * Handle a interested/not interested message in a proper way.
	 * 
	 * @param m the message to be handled
	 */
	public static void handle(Message m, boolean isInterested) throws Exception {
		if (isInterested) {
			PeerProcess.write("received the 'interested' message from " + m.remotePeerId);
			PeerProcess.peers.get(m.remotePeerIndex).isRemotePeerInterestedInLocalPeer = true;
			if(!PeerProcess.getInterestedPeers().contains(m.remotePeerId)) {
				PeerProcess.addInterestPeer(m.remotePeerId);
			}
		} else {
			PeerProcess.write("received the 'not interested' message from " + m.remotePeerId);
			PeerProcess.peers.get(m.remotePeerIndex).isRemotePeerInterestedInLocalPeer = false;
			PeerProcess.removeInterestPeer(m.remotePeerId);
		}
	}
}
