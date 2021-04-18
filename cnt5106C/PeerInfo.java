/**
 * The component to read peerInfo.cfg.
 */

package cnt5106C;

import java.io.*;
import java.util.ArrayList;
import java.io.IOException;

public class PeerInfo {
	public static ArrayList<DynamicPeerInfo> readPeerInfo(int numOfPieces) {
		ArrayList<DynamicPeerInfo> peers = new ArrayList<DynamicPeerInfo>();
		try {
			BufferedReader configFileReader = new BufferedReader(
				new InputStreamReader(new FileInputStream("PeerInfo.cfg")));
			String line = null;
			int index = 0;
			while ((line = configFileReader.readLine()) != null) {
				line = line.trim();

				String[] tokens = line.split(" ");
				peers.add(new DynamicPeerInfo(
					Integer.parseInt(tokens[0].trim()),
					tokens[1].trim(),
					Integer.parseInt(tokens[2].trim()),
					tokens[3].trim().equals("1"),
					numOfPieces,
					index));
				index++;
			}
			configFileReader.close();
		} catch (IOException e) {
			System.err.println("Error loading peer info config file!");
			System.exit(-1);
		}
		return peers;
	}
}
