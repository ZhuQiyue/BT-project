package cnt5106C;

import java.io.IOException;
import java.nio.ByteBuffer;

public class HaveHandler {
	public static Message construct(int remotePeerId, int index) throws IOException {
		ByteBuffer msg = ByteBuffer.allocate(4);
		msg.put(ByteBuffer.allocate(4).putInt(index).array());
		return Message.actualMessageWrapper(remotePeerId, 4, msg.array());
	}

	public static void handle(Message m) throws InterruptedException {
		int fileIndex = ByteBuffer.wrap(m.messagePayload).getInt();
		PeerProcess.write("received the 'have' message from " + m.remotePeerId + " for the piece " + fileIndex);
		PeerProcess.peers.get(m.remotePeerIndex).setFilePieceState(fileIndex, true);
		if(!PeerProcess.peers.get(m.remotePeerIndex).isLocalPeerInterestedInRemotePeer
				&& PeerProcess.peers.get(m.remotePeerIndex).isThereAnyInterestedFilePieces()) {
			PeerProcess.peers.get(m.remotePeerIndex).isLocalPeerInterestedInRemotePeer = true;
			PeerProcess.messageQueues.get(m.remotePeerIndex).add(InterestHandler.construct(m.remotePeerId, true));
		}
		PeerProcess.checkTermination();
	}
}
