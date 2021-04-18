package cnt5106C;

import java.util.Random;

public class PeerId_ChunkCount implements Comparable<PeerId_ChunkCount>{
	public int peerId;
	public int chunkCount;
	PeerId_ChunkCount(int peerId, int chunkCount){
		this.peerId = peerId;
		this.chunkCount = chunkCount;
	}
	@Override
	public int compareTo(PeerId_ChunkCount o) {
		if(chunkCount < o.chunkCount) {
			return 1;//sort in descending order
		}else if(chunkCount == o.chunkCount) {//break the tie randomly
			//We want the tie to break randomly, but have the same result if we have the same input
			Random random = new Random(chunkCount + o.chunkCount);
			boolean b = random.nextBoolean();
			return b ? 1 : -1;
		}else {
			return -1;
		}
	}
}
