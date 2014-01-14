package jtg.cse260.cryptcoin.node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

import jtg.cse260.cryptcoin.Main;
import jtg.cse260.cryptcoin.block.BlockTree;
import jtg.cse260.cryptcoin.block.BlockTreeVertex;
import jtg.cse260.cryptcoin.gui.TopPanel;
import jtg.util.Singleton;
import jtg.util.SingletonSaver;

import com.starkeffect.cryptcoin.protocol.CoinAddress;
import com.starkeffect.cryptcoin.protocol.CoinAmount;
import com.starkeffect.util.KeyUtilities;

/**
 * @author jesse
 * 
 */
public class WalletManager implements Serializable, Singleton<WalletManager> {

    public static transient final File                     FILE = new File(Main.DIRECTORY, "wallet.wtf");

    private static transient SingletonSaver<WalletManager> saver;

    private DefaultListModel<Wallet>                       model;

    private static WalletManager                           instance;

    private static transient Timer                         balanceUpdater;

    public static WalletManager getInstance() {
        return instance;
    }

    static {
        saver = new SingletonSaver<>(FILE);
        instance = saver.load();
        if (instance == null) instance = new WalletManager();

        balanceUpdater = new Timer();
        balanceUpdater.scheduleAtFixedRate(instance.new BalanceUpdater(), 60 * 1000, 60 * 1000);
        saver.saveOnExit(instance);
    }

    private WalletManager() {
        this.model = new DefaultListModel<>();
    }

    public DefaultListModel<Wallet> getWalletModel() {
        return this.model;
    }

    public Wallet createWallet(String name, KeyPair keys) {
        Wallet w = new Wallet(name, keys);
        this.model.addElement(w);
        // Uses a Vector under the hood, which is thread-safe
        return w;
    }

    public String exportWallets() {
        StringBuilder out = new StringBuilder(1200);
        for (Object i : this.model.toArray()) {
            out.append(((Wallet)i).export() + '\n');
        }
        return out.toString().trim();
    }

    /**
     * Given a {@link File}, imports wallets from it and adds them to the wallet
     * model
     * 
     * @param file The file
     * @return The number of wallets imported
     */
    public int importWallets(File file) {
        try (Scanner input = new Scanner(file)) {
            if (!input.hasNextLine()) return 0;
            int amount = 0;
            String current;
            while (input.hasNextLine()) {
                current = input.nextLine();
                Wallet w = Wallet.importWallet(current);
                if (w != null && !this.model.contains(w)) {
                    this.model.addElement(w);
                    ++amount;
                }
            }

            return amount;
        }
        catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Returns the balance held by all wallets this client has
     * @return
     */
    public CoinAmount getTotalBalance() {
        long total = 0;
        for (Object i : WalletManager.this.model.toArray()) {
            total += ((Wallet)i).getBalance().longValue();
        }
        
        return new CoinAmount(total);
    }

    private class BalanceUpdater extends TimerTask {

        @Override
        public void run() {
            for (Object i : WalletManager.this.model.toArray()) {
                ((Wallet)i).updateBalance();
            }
            TopPanel.getInstance().updateBalance();
        }

    }
}
