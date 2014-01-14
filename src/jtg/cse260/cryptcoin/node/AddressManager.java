package jtg.cse260.cryptcoin.node;

import java.io.File;

import jtg.cse260.cryptcoin.Main;
import jtg.util.Singleton;
import jtg.util.SingletonSaver;

public class AddressManager implements Singleton<AddressManager> {
    private static transient SingletonSaver<AddressManager> saver;
    
    private static AddressManager instance;
    
    public static transient final File FILE = new File(Main.DIRECTORY, "peers.wtf");
    
    static {
        saver = new SingletonSaver<>(FILE);
        instance = saver.load();
        if (instance == null) instance = new AddressManager();
        saver.saveOnExit(instance);
    }
    
    private AddressManager() {
        
    }
    
    public static AddressManager getInstance() {
        return instance;
    }
    
    
}
