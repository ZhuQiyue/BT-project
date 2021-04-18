/**
 * The component to read common.cfg.
 */

package cnt5106C;

import java.util.*;
import java.io.*;

public class Config {
    
	private static Properties cfg;
	
	public static void init() {
		cfg = new Properties();
        try {
            InputStream in = new FileInputStream("Common.cfg");
            cfg.load(in);
        }
        catch(Exception e){
            e.printStackTrace();
        }
	}

    public static int getNumberOfPreferredNeighbors() {
        return Integer.parseInt(cfg.getProperty("NumberOfPreferredNeighbors"));
    }

    public static int getUnchokingInterval() {
        return Integer.parseInt(cfg.getProperty("UnchokingInterval"));
    }

    public static int getOptimisticUnchokingInterval() {
        return Integer.parseInt(cfg.getProperty("OptimisticUnchokingInterval"));
    }

    public static String getFileName() {
        return cfg.getProperty("FileName");
    }

    public static int getFileSize() {
        return Integer.parseInt(cfg.getProperty("FileSize"));
    }

    public static int getPieceSize() {
        return Integer.parseInt(cfg.getProperty("PieceSize"));
    }
}
