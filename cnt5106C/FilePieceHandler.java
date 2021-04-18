package cnt5106C;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class FilePieceHandler {
	public static Message construct(int remotePeerId, int index) throws IOException {
		ByteBuffer msg = ByteBuffer.allocate(PeerProcess.pieceSize + 4);
		msg.put(ByteBuffer.allocate(4).putInt(index).array());
		msg.put(PeerProcess.fileHelper.getFilePieceInByteArray(index));
		return Message.actualMessageWrapper(remotePeerId, 7, msg.array());
	}

	public static void handle(Message m) throws IOException, InterruptedException {
		byte[] fileIndexInByte = Arrays.copyOfRange(m.messagePayload, 0, 4);
		byte[] realFile = Arrays.copyOfRange(m.messagePayload, 4, 4 + PeerProcess.pieceSize);
		int fileIndex = ByteBuffer.wrap(fileIndexInByte).getInt();
		DynamicPeerInfo selfPeer = PeerProcess.peers.get(PeerProcess.index);
		if(selfPeer.getFilePieceState(fileIndex) == true) {
			return;//In a rare scenario, the request timeout is reset, but the request
			//is not actually dropped, then we may receive duplicate file piece 
		}

		PeerProcess.write("has downloaded the piece " + fileIndex + " from " + m.remotePeerId
				+ ". Now the number of pieces it has is "
				+ (selfPeer.getTotalFilePiecesWeReceived() + 1));
		selfPeer.setFilePieceState(fileIndex, true);
		PeerProcess.fileHelper.writeFilePieceInByteArray(fileIndex, realFile);

		for (DynamicPeerInfo p : PeerProcess.peers) {
			if (p.isConnected) {
				//PeerProcess.write("send have message to " + p.peerId);
				PeerProcess.messageQueues.get(p.index).put(HaveHandler.construct(p.peerId, fileIndex));
				if (!p.isThereAnyInterestedFilePieces() && p.isLocalPeerInterestedInRemotePeer) {
					p.isLocalPeerInterestedInRemotePeer = false;
					PeerProcess.messageQueues.get(p.index).put(InterestHandler.construct(p.peerId, false));
				}
			}
		}
		
		PeerProcess.dm.removeRequest(fileIndex);

		DynamicPeerInfo rp = PeerProcess.peers.get(m.remotePeerIndex);
		rp.incrementChunkCounter();
		
		while(rp.isThereAnyInterestedFilePieces() && !rp.isRemotePeerChockingLocalPeer) {
			Integer requestIndex;
			if((requestIndex = rp.getAnInterestedIndex()) != null) {
				if(PeerProcess.dm.addRequest(requestIndex)) {
					PeerProcess.messageQueues.get(m.remotePeerIndex).add(RequestHandler.construct(m.remotePeerId, requestIndex));
					break;
				}
			}else {
				break;
			}
		}
		
		PeerProcess.checkTermination();
	}
}
