package jtg.cse260.cryptcoin.gui.windows;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import jtg.cse260.cryptcoin.gui.GUIConstants;
import jtg.cse260.cryptcoin.node.Wallet;

import com.starkeffect.util.KeyUtilities;

public class WalletPropertiesWindow extends JFrame implements GUIConstants {
    private JPanel                 contentPane;
    private JTextField             walletAddressField;
    private JTextField             walletDescriptionField;
    private Wallet                 wallet;
    private JLabel                 privateKeyLabel;
    private JTextField             privateKeyField;
    private static final Dimension PREFERRED_SIZE     = new Dimension(260, 180);
    private static final Border    EMPTY_BORDER       = new EmptyBorder(5, 5, 5, 5);
    private static final int       TEXT_FIELD_WIDTH   = 32;
    private static final String    PRIVATE_KEY_REVEAL = "Click to reveal";

    private static final int[]     COL_WIDTHS         = {0, 0};
    private static final int[]     ROW_HEIGHTS        = {0, 0, 0, 0, 0};
    private static final double[]  COL_WEIGHTS        = {1.0, 1.0};
    private static final double[]  ROW_WEIGHTS        = {0, 0, 0, 0, 0};
    private JTextField             publicKeyField;
    private JLabel                 publicKeyLabel;
    private JButton                closeButton;
    private JLabel                 descriptionLabel;
    private JLabel                 walletAddressLabel;
    private JLabel                 balanceLabel;
    private JLabel                 nameLabel;
    private JLabel                 walletNameLabel;
    private JLabel                 walletBalanceLabel;

    /**
     * Create the frame.
     * 
     * @param wallet The wallet this {@code WalletPropertiesWindow} should
     *        provide info about.
     */
    public WalletPropertiesWindow(final Wallet wallet) {
        super();
        this.wallet = wallet;
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setMinimumSize(WalletPropertiesWindow.PREFERRED_SIZE);
        this.contentPane = new JPanel();
        this.contentPane.setBorder(WalletPropertiesWindow.EMPTY_BORDER);
        this.setContentPane(this.contentPane);
        GridBagLayout layout = new GridBagLayout();
        {
            layout.columnWidths = WalletPropertiesWindow.COL_WIDTHS;
            layout.rowHeights = WalletPropertiesWindow.ROW_HEIGHTS;
            layout.columnWeights = WalletPropertiesWindow.COL_WEIGHTS;
            layout.rowWeights = WalletPropertiesWindow.ROW_WEIGHTS;
        }
        this.setLayout(layout);

        this.setTitle(String.format("%s (%s)", wallet.getName(), wallet.getAddress()));

        this.nameLabel = new JLabel("Name:");
        {
            this.nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
            GridBagConstraints nameLabelConstraints = new GridBagConstraints();
            {
                nameLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                nameLabelConstraints.gridx = 0;
                nameLabelConstraints.gridy = 0;
                nameLabelConstraints.anchor = GridBagConstraints.EAST;
                nameLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
            }
            this.add(this.nameLabel, nameLabelConstraints);

            this.walletNameLabel = new JLabel(this.wallet.getName());
            {
                this.nameLabel.setLabelFor(this.walletNameLabel);
                this.walletNameLabel.setToolTipText("The name of this wallet.");
                GridBagConstraints walletNameLabelConstraints = new GridBagConstraints();
                {
                    walletNameLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
                    walletNameLabelConstraints.insets = new Insets(5, 5, 5, 0);
                    walletNameLabelConstraints.gridx = 1;
                    walletNameLabelConstraints.gridy = 0;
                }
                this.add(this.walletNameLabel, walletNameLabelConstraints);
            }
        }

        this.balanceLabel = new JLabel("Balance:");
        {
            GridBagConstraints balanceLabelConstraints = new GridBagConstraints();
            {
                balanceLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                balanceLabelConstraints.gridx = 0;
                balanceLabelConstraints.gridy = 1;
                balanceLabelConstraints.anchor = GridBagConstraints.EAST;
                balanceLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
            }
            this.add(this.balanceLabel, balanceLabelConstraints);

            walletBalanceLabel = new JLabel(wallet.getBalance().toString());
            {
                this.balanceLabel.setLabelFor(walletBalanceLabel);
                walletBalanceLabel.setToolTipText("The amount of Cryptcoins you have on-hand.");
                GridBagConstraints walletBalanceLabelConstraints = new GridBagConstraints();
                {
                    walletBalanceLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
                    walletBalanceLabelConstraints.insets = new Insets(5, 5, 5, 0);
                    walletBalanceLabelConstraints.gridx = 1;
                    walletBalanceLabelConstraints.gridy = 1;
                }
                this.add(walletBalanceLabel, walletBalanceLabelConstraints);
            }
        }

        this.walletAddressLabel = new JLabel("Address:");
        {
            this.walletAddressLabel.setHorizontalAlignment(SwingConstants.LEFT);
            GridBagConstraints walletAddressLabelConstraints = new GridBagConstraints();
            {
                walletAddressLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                walletAddressLabelConstraints.gridx = 0;
                walletAddressLabelConstraints.gridy = 2;
                walletAddressLabelConstraints.anchor = GridBagConstraints.EAST;
                walletAddressLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
            }
            this.add(this.walletAddressLabel, walletAddressLabelConstraints);

            this.walletAddressField = new JTextField(this.wallet.getAddress().toString());
            {
                this.walletAddressField.setEditable(false);
                this.walletAddressField.setToolTipText("If you want to be paid, this is the code you give people.");
                this.walletAddressLabel.setLabelFor(this.walletAddressField);
                GridBagConstraints walletAddressFieldConstraints = new GridBagConstraints();
                {
                    walletAddressFieldConstraints.insets = new Insets(5, 5, 5, 0);
                    walletAddressFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                    walletAddressFieldConstraints.gridx = 1;
                    walletAddressFieldConstraints.gridy = 2;
                }
                this.add(this.walletAddressField, walletAddressFieldConstraints);
            }
        }

        this.descriptionLabel = new JLabel("Description:");
        {
            GridBagConstraints descriptionLabelConstraints = new GridBagConstraints();
            {
                descriptionLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                descriptionLabelConstraints.gridx = 0;
                descriptionLabelConstraints.gridy = 3;
                descriptionLabelConstraints.anchor = GridBagConstraints.EAST;
                descriptionLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
            }
            this.add(this.descriptionLabel, descriptionLabelConstraints);

            this.walletDescriptionField = new JTextField(wallet.getDescription());
            {
                this.walletDescriptionField.setToolTipText("Make a note about this wallet.");
                GridBagConstraints walletDescriptionFieldConstraints = new GridBagConstraints();
                {
                    walletDescriptionFieldConstraints.insets = GUIConstants.DEFAULT_INSETS;
                    walletDescriptionFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                    walletDescriptionFieldConstraints.gridx = 1;
                    walletDescriptionFieldConstraints.gridy = 3;
                }
                this.add(this.walletDescriptionField, walletDescriptionFieldConstraints);
            }
        }

        this.closeButton = new JButton("Close");
        {
            this.closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    WalletPropertiesWindow.this.dispose();
                }
            });

            GridBagConstraints closeButtonConstraints = new GridBagConstraints();
            {
                closeButtonConstraints.gridwidth = 2;
                closeButtonConstraints.insets = GUIConstants.DEFAULT_INSETS;
                closeButtonConstraints.gridx = 0;
                closeButtonConstraints.gridy = 6;
            }

            this.add(this.closeButton, closeButtonConstraints);
        }

        this.publicKeyLabel = new JLabel("Public Key:");
        {
            GridBagConstraints publicKeyLabelConstraints = new GridBagConstraints();
            {
                publicKeyLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
                publicKeyLabelConstraints.anchor = GridBagConstraints.EAST;
                publicKeyLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                publicKeyLabelConstraints.gridx = 0;
                publicKeyLabelConstraints.gridy = 4;
            }
            this.add(this.publicKeyLabel, publicKeyLabelConstraints);

            this.publicKeyField = new JTextField(KeyUtilities.exportPublicKey(wallet.getPublicKey()));
            {
                this.publicKeyLabel.setLabelFor(this.publicKeyField);
                this.publicKeyField
                        .setToolTipText("Half of your wallet's security. It won't hurt you if anyone gets this, but I still wouldn't pass it out.");
                this.publicKeyField.setEditable(false);
                this.publicKeyField.setColumns(WalletPropertiesWindow.TEXT_FIELD_WIDTH);
                GridBagConstraints publicKeyFieldConstraints = new GridBagConstraints();
                {
                    publicKeyFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                    publicKeyFieldConstraints.anchor = GridBagConstraints.EAST;
                    publicKeyFieldConstraints.insets = GUIConstants.DEFAULT_INSETS;
                    publicKeyFieldConstraints.gridx = 1;
                    publicKeyFieldConstraints.gridy = 4;
                }

                this.add(this.publicKeyField, publicKeyFieldConstraints);
            }
        }

        this.privateKeyLabel = new JLabel("Private Key:");
        {
            GridBagConstraints privateKeyLabelConstraints = new GridBagConstraints();
            {
                privateKeyLabelConstraints.anchor = GridBagConstraints.EAST;
                privateKeyLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
                privateKeyLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                privateKeyLabelConstraints.gridx = 0;
                privateKeyLabelConstraints.gridy = 5;
            }
            this.add(this.privateKeyLabel, privateKeyLabelConstraints);

            this.privateKeyField = new JTextField(PRIVATE_KEY_REVEAL);
            {
                this.privateKeyLabel.setLabelFor(this.privateKeyField);
                this.privateKeyField
                        .setToolTipText("Half of your wallet's security. Do NOT give this to anybody!");
                this.privateKeyField.setEditable(false);
                this.privateKeyField.setColumns(WalletPropertiesWindow.TEXT_FIELD_WIDTH);
                GridBagConstraints privateKeyFieldConstraints = new GridBagConstraints();
                {
                    privateKeyFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                    privateKeyFieldConstraints.anchor = GridBagConstraints.EAST;
                    privateKeyFieldConstraints.insets = GUIConstants.DEFAULT_INSETS;
                    privateKeyFieldConstraints.gridx = 1;
                    privateKeyFieldConstraints.gridy = 5;
                }

                this.privateKeyField.addMouseListener(new MouseAdapter() {
                    private boolean toggle = false;

                    @Override
                    public void mouseClicked(final MouseEvent e) {
                        if (!this.toggle) {
                            // If we haven't yet clicked this field...
                            String key = KeyUtilities.exportPrivateKey(WalletPropertiesWindow.this.wallet
                                    .getPrivateKey());
                            WalletPropertiesWindow.this.privateKeyField.setText(key);
                            this.toggle = true;
                        }
                    }

                });

                this.add(this.privateKeyField, privateKeyFieldConstraints);
            }
        }

        this.pack();
        this.setVisible(true);
    }

    @Override
    public void dispose() {
        this.wallet.setDescription(this.walletDescriptionField.getText().replaceAll("[\\[\\]]", ""));
        super.dispose();
    }
}
