package jtg.cse260.cryptcoin;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import jtg.cse260.cryptcoin.block.BlockTree;
import jtg.cse260.cryptcoin.gui.CCFrame;
import jtg.cse260.cryptcoin.node.WalletManager;

public class Main {
    /** The name of this Cryptcoin client. */
    public static final String NAME = "Cryptcoin-JTG";
    
    /** The version of this Cryptcoin client. */
    public static final String VERSION = "Dalton";
    
    /** The directory where all Cryptcoin files are stored. */
    public static final File DIRECTORY = new File(System.getProperty("user.home"), "cryptcoin");
    
    public static final Random RANDOM = new Random();
    
    public static final String FULL_NAME = String.format("%s (Release %s)", Main.NAME, Main.VERSION);
    
    public static void main(String[] args) {
        // To call the static initializers
        CCFrame.getInstance();
        BlockTree.getInstance();
        WalletManager.getInstance();
        
        if (!DIRECTORY.exists()) {
            DIRECTORY.mkdir();
        }
        CCFrame frame = CCFrame.getInstance();
    }
}
