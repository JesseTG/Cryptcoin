package jtg.cse260.cryptcoin.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.starkeffect.cryptcoin.protocol.Parameters;
import com.starkeffect.util.KeyUtilities;

import jtg.cse260.cryptcoin.Resources;
import jtg.cse260.cryptcoin.gui.tabs.PaymentsTab;
import jtg.cse260.cryptcoin.gui.tabs.SettingsTab;
import jtg.cse260.cryptcoin.gui.tabs.TransactionsTab;
import jtg.cse260.cryptcoin.gui.windows.WalletPropertiesWindow;
import jtg.cse260.cryptcoin.node.NodeManager;
import jtg.cse260.cryptcoin.node.Wallet;
import jtg.cse260.cryptcoin.node.WalletManager;
import jtg.cse260.cryptcoin.node.WalletNode;
import jtg.cse260.cryptcoin.block.BlockTree;

@SuppressWarnings("serial")
public class LeftPanel extends JPanel implements GUIConstants {

    private static final int[]      COL_WIDTHS     = {0};
    private static final int[]      ROW_HEIGHTS    = {32, 0, 0, 0, 0};
    private static final double[]   COL_WEIGHTS    = {1.0};
    private static final double[]   ROW_WEIGHTS    = {1.0, 0.0, 0.0, 0.0, 0.0};
    private static final Insets     DEFAULT_INSETS = new Insets(0, 0, 5, 0);
    private static final Parameters PARAMS         = Parameters.getInstance();

    private JList<Wallet>           walletList;
    private GridBagLayout           layout;
    private JTextField              walletAddressField;
    private JButton                 newWalletButton;
    private JButton                 walletPropertiesButton;
    private JLabel                  walletAddressLabel;
    private JScrollPane             walletPane;

    private static LeftPanel        instance       = new LeftPanel();

    public static LeftPanel getInstance() {
        return instance;
    }

    private LeftPanel() {
        this.layout = new GridBagLayout();
        {
            this.layout.columnWidths = LeftPanel.COL_WIDTHS;
            this.layout.rowHeights = LeftPanel.ROW_HEIGHTS;
            this.layout.columnWeights = LeftPanel.COL_WEIGHTS;
            this.layout.rowWeights = LeftPanel.ROW_WEIGHTS;
            this.setLayout(this.layout);
        }

        this.walletList = new JList<>();
        {
            this.walletList.setModel(WalletManager.getInstance().getWalletModel());
            this.walletList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            this.walletList.addListSelectionListener(new ListSelectionListener() {
                private Wallet lastSelected;

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    Wallet selected = LeftPanel.this.walletList.getSelectedValue();
                    if (selected != lastSelected) {
                        // If we did not just click on the wallet that's already
                        // selected...
                        LeftPanel.this.walletPropertiesButton.setEnabled(selected != null);
                        if (selected == null) {
                            // If no wallet is selected...
                            LeftPanel.this.walletAddressField.setText("");
                        }
                        else {
                            LeftPanel.this.walletAddressField.setText(selected.getAddress().toString());
                        }
                        TransactionsTab.getInstance().setWallet(selected);
                        SettingsTab.getInstance().setCompetitionTrough(selected);
                    }
                    lastSelected = selected;
                }
            });

            GridBagConstraints walletListConstraints = new GridBagConstraints();
            {
                walletListConstraints.insets = LeftPanel.DEFAULT_INSETS;
                walletListConstraints.fill = GridBagConstraints.BOTH;
                walletListConstraints.gridx = 0;
                walletListConstraints.gridy = 0;
            }

            this.walletPane = new JScrollPane(this.walletList);
            this.add(this.walletPane, walletListConstraints);
        }

        this.newWalletButton = new JButton("New Wallet", Resources.getIcon("money"));
        {
            GridBagConstraints newWalletButtonConstraints = new GridBagConstraints();
            {
                newWalletButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
                newWalletButtonConstraints.insets = LeftPanel.DEFAULT_INSETS;
                newWalletButtonConstraints.gridx = 0;
                newWalletButtonConstraints.gridy = 1;
            }
            this.newWalletButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    LeftPanel.this.createWallet();
                }

            });
            this.add(this.newWalletButton, newWalletButtonConstraints);
        }

        this.walletPropertiesButton = new JButton("Properties", Resources.getIcon("info"));
        {
            GridBagConstraints walletPropertiesButtonConstraints = new GridBagConstraints();
            {
                walletPropertiesButtonConstraints.insets = LeftPanel.DEFAULT_INSETS;
                walletPropertiesButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
                walletPropertiesButtonConstraints.gridx = 0;
                walletPropertiesButtonConstraints.gridy = 2;
            }
            this.walletPropertiesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.walletPropertiesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Wallet selected = LeftPanel.this.walletList.getSelectedValue();
                    if (selected != null) {
                        new WalletPropertiesWindow(selected);
                    }
                }
            });
            this.walletPropertiesButton.setEnabled(false);
            this.add(this.walletPropertiesButton, walletPropertiesButtonConstraints);
        }

        this.walletAddressLabel = new JLabel("Wallet Address");
        {
            GridBagConstraints walletAddressLabelConstraints = new GridBagConstraints();
            {
                walletAddressLabelConstraints.insets = LeftPanel.DEFAULT_INSETS;
                walletAddressLabelConstraints.gridx = 0;
                walletAddressLabelConstraints.gridy = 3;
            }
            this.add(this.walletAddressLabel, walletAddressLabelConstraints);
        }

        this.walletAddressField = new JTextField();
        {
            this.walletAddressField.setHorizontalAlignment(SwingConstants.CENTER);
            this.walletAddressField.setEditable(false);
            this.walletAddressField.setColumns(10);
            GridBagConstraints walletAddressTextFieldConstraints = new GridBagConstraints();
            {
                walletAddressTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                walletAddressTextFieldConstraints.gridx = 0;
                walletAddressTextFieldConstraints.gridy = 4;
            }
            this.add(this.walletAddressField, walletAddressTextFieldConstraints);
        }
    }

    /**
     * @return The {@link Wallet} that is currently selected by the user, or
     *         {@code null} if there are none.
     */
    public Wallet getSelectedWallet() {
        return this.walletList.getSelectedValue();
    }

    public void createWallet() {
        String name;
        do {
            name = JOptionPane.showInputDialog(LeftPanel.this, "What will you name this wallet?",
                    "Create New Wallet", JOptionPane.QUESTION_MESSAGE);
            if (name == null) {
                // If the user hit "Cancel"...
                return;
            }
            else {
                name = name.trim();
            }
        } while (name.length() <= 0);

        if (name != null) {
            try {
                WalletManager.getInstance().createWallet(name,
                        new KeyUtilities().createKeyPair());
            }
            catch (NoSuchAlgorithmException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }
}
