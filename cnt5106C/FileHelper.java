package cnt5106C;

import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;

public class FileHelper {
	private final Object lock = new Object();
	private RandomAccessFile rf;
	private int localIndex;
	private int fileSize;
	private int pieceSize;
	private int numOfPieces;
	private boolean hasFileInitially;
	private String fileName;
	private String path;
	FileHelper() throws IOException{
		localIndex = PeerProcess.index;
		fileSize = PeerProcess.fileSize;
		pieceSize = PeerProcess.pieceSize;
		numOfPieces = PeerProcess.numOfPieces;
		hasFileInitially = PeerProcess.peers.get(localIndex).hasFileInitially;
		fileName = PeerProcess.fileName;
		path = "./" + PeerProcess.peerId + "/" + fileName;
		if(hasFileInitially) {
			rf = new RandomAccessFile(path, "rw");
		}else {
			File folder = new File("./" + PeerProcess.peerId);
			folder.mkdir();
			File file = new File(path);
			file.createNewFile();
			FileWriter fw = new FileWriter(path);
			for(int i = 0; i < fileSize; i++) {
				fw.write((byte)0);	
			}
			fw.close();
			rf = new RandomAccessFile(path, "rw");
		}
	}
	
	public byte[] getFilePieceInByteArray(int index) throws IOException {
		synchronized(lock) {
			rf.seek(index*pieceSize);
			byte[] buffer = new byte[pieceSize];
			if(index == PeerProcess.numOfPieces - 1) {//last byte, might not be full
				int readSize = PeerProcess.fileSize - index * pieceSize;
				for(int i = 0; i < readSize; i++) {
					buffer[i] = rf.readByte();
				}
				return buffer;
			}else {
				for(int i = 0; i < pieceSize; i++) {
					buffer[i] = rf.readByte();
				}
				return buffer;
			}
		}
	}
	
	public void writeFilePieceInByteArray(int index, byte[] bytes) throws IOException {
		synchronized(lock) {
			rf.seek(index * pieceSize);
			if(index == PeerProcess.numOfPieces - 1) {//last byte, might not be full
				int writeSize = PeerProcess.fileSize - index * pieceSize;
				for(int i = 0; i < writeSize; i++) {
					rf.writeByte(bytes[i]);
				}
			}else {
				for(int i = 0; i < pieceSize; i++) {
					rf.writeByte(bytes[i]);
				}
			}
		}
	}
}
