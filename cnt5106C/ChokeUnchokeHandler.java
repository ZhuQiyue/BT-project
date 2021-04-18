/*
 * The handler to handle any functionalities about interested/not interested message.
 */

package cnt5106C;


import java.util.ArrayList;

import cnt5106C.Message.*;

public class ChokeUnchokeHandler {
	/**
	 * Construct a Interested message.
	 * @param remotePeerId the peerid of remote peer
	 * @return the message just has been constructed
	 */
	public static Message construct(int remotePeerId, boolean isChoke) {
		if (isChoke){
			// PeerProcess.write("sending choke to "+remotePeerId);
			return Message.actualMessageWrapper(remotePeerId, 0 , new byte[0]);
		}
		else {
			// PeerProcess.write("sending unchoke to "+remotePeerId);
			return Message.actualMessageWrapper(remotePeerId, 1 , new byte[0]);
		}
	}
	
	/**
	 * Handle a choke/unchoke message in a proper way.
	 * @param m the message to be handled
	 */
	public static void handle(Message m, boolean isChoke) throws Exception {
		//TODO this is just for testing
		if(isChoke){
			PeerProcess.write("is choked by " + m.remotePeerId);
			PeerProcess.peers.get(m.remotePeerIndex).isRemotePeerChockingLocalPeer = true;
		}
		else{
			PeerProcess.write("is unchoked by " + m.remotePeerId);
			PeerProcess.peers.get(m.remotePeerIndex).isRemotePeerChockingLocalPeer = false;
			// Send request if needed
			DynamicPeerInfo p = PeerProcess.peers.get(m.remotePeerIndex);
			Integer requestIndex;
			while((requestIndex = p.getAnInterestedIndex()) != null) {
				if(PeerProcess.dm.addRequest(requestIndex)) {
					PeerProcess.messageQueues.get(m.remotePeerIndex).add(RequestHandler.construct(m.remotePeerId, requestIndex));
					break;
				}
			}
		}
	}
}
