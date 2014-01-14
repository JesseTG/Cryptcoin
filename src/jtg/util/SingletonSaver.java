package jtg.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JOptionPane;

import jtg.cse260.cryptcoin.node.WalletManager;

/**
 * Utility class for saving singletons to a file and loading them again.
 * 
 * @author jesse
 */
public class SingletonSaver<T extends Singleton<T>> {

    protected File path;

    public SingletonSaver(File path) {
        this.path = path;
    }

    public void save(T singleton) {
        try (ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(path)))) {
            out.writeObject(singleton);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        }

    }

    public T load() {
        if (!path.exists()) {
            // If the given path doesn't actually exist...
            return null;
        }
        else if (!path.isFile()) {
            // Else if the given path isn't actually a file...
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(path)))) {
            return (T)in.readObject();
        }
        catch (IOException | ClassNotFoundException | ClassCastException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public void saveOnExit(final T instance) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                save(instance);
            }
        });
    }
}
