package jtg.cse260.cryptcoin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

import com.starkeffect.cryptcoin.protocol.Parameters;
import com.starkeffect.util.KeyUtilities;

import jtg.cse260.cryptcoin.Resources;
import jtg.cse260.cryptcoin.node.WalletManager;
import jtg.util.InfoBox;

@SuppressWarnings("serial")
class Menu extends JMenuBar {
    private static final Parameters PARAMS = Parameters.getInstance();
    private JMenu                   file;
    private JMenu                   edit;
    private JMenu                   money;
    private JMenu                   about;
    private JFileChooser            fileChooser;

    public Menu() {
        super();
        this.fileChooser = new JFileChooser();

        this.file = new JMenu("File") {
            JMenuItem  saveKey       = new JMenuItem("Save Keys", Resources.getIcon("save-keys"));
            {
                this.saveKey.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fileChooser.setMultiSelectionEnabled(false);
                        int result = fileChooser.showSaveDialog(Menu.this);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            // If the user chose a file to save to...
                            try (FileWriter writer = new FileWriter(fileChooser.getSelectedFile())) {

                                writer.append(WalletManager.getInstance().exportWallets());
                            }
                            catch (IOException e1) {
                                InfoBox.error(e1.getMessage(), "Error!");
                            }
                        }
                    }
                });
            }
            JMenuItem  loadKey       = new JMenuItem("Load Keys", Resources.getIcon("load-keys"));
            {
                loadKey.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fileChooser.setMultiSelectionEnabled(true);
                        int result = fileChooser.showOpenDialog(Menu.this);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            // If the user chose files to load from...
                            for (File i : fileChooser.getSelectedFiles()) {
                                WalletManager.getInstance().importWallets(i);
                            }
                        }
                    }

                });
            }
            JSeparator fileSeparator = new JSeparator();
            JMenuItem  quit          = new JMenuItem("Quit", Resources.getIcon("quit"));
            {
                quit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        CCFrame.getInstance().dispose();
                        System.exit(0);
                    }
                });
            }

            {
                this.add(this.saveKey);
                this.add(this.loadKey);
                this.add(this.fileSeparator);
                this.add(this.quit);
            }
        };

        this.edit = new JMenu("Edit") {
            JMenuItem copy   = new JMenuItem("Copy", Resources.getIcon("copy"));
            {
                copy.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        CCFrame.getInstance().copy();
                    }
                });
            }

            JMenuItem cut    = new JMenuItem("Cut", Resources.getIcon("cut"));
            {
                cut.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        CCFrame.getInstance().cut();
                    }
                });
            }

            JMenuItem paste  = new JMenuItem("Paste", Resources.getIcon("paste"));
            {
                paste.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        CCFrame.getInstance().paste();
                    }
                });
            }
            JMenuItem delete = new JMenuItem("Delete", Resources.getIcon("delete"));
            {
                delete.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        CCFrame.getInstance().delete();
                    }
                });
            }

            {
                this.add(this.copy);
                this.add(this.cut);
                this.add(this.paste);
                this.add(this.delete);
            }
        };

        this.money = new JMenu("Money") {
            JMenuItem  newWallet = new JMenuItem("New Wallet", Resources.getIcon("money"));
            {
                newWallet.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LeftPanel.getInstance().createWallet();
                    }
                });
            }
            JSeparator separator = new JSeparator();
            JMenuItem  sendMoney = new JMenuItem("Send Money", Resources.getIcon("payments")); {
                sendMoney.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        RightPanel.getInstance().goToTab(1);
                    }
                });
            }

            {
                this.add(newWallet);
                this.add(separator);
                this.add(sendMoney);
            }
        };

        this.about = new JMenu("About") {
            JMenuItem about = new JMenuItem("About", Resources.getIcon("info"));
            {
                this.add(about);
            }
        };

        this.add(this.file);
        this.add(this.edit);
        this.add(this.money);
        this.add(this.about);
    }
}
