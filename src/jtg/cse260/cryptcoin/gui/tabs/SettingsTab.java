package jtg.cse260.cryptcoin.gui.tabs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.starkeffect.cryptcoin.protocol.CoinAddress;

import jtg.cse260.cryptcoin.CompetitionManager;
import jtg.cse260.cryptcoin.gui.CCFrame;
import jtg.cse260.cryptcoin.gui.GUIConstants;
import jtg.cse260.cryptcoin.node.Wallet;
import jtg.util.InfoBox;

@SuppressWarnings("serial")
public class SettingsTab extends JPanel implements GUIConstants {
    private static final int[]    COL_WIDTHS  = {0, 0};
    private static final int[]    ROW_HEIGHTS = {48, 48, 48, 48, 48, 48};
    private static final double[] COL_WEIGHTS = {0.0, 1.0};
    private static final double[] ROW_WEIGHTS = {0.0, 0.0, 0.0, Double.MIN_VALUE};
    private GridBagLayout         layout;
    private JLabel                maxWalletConnectionsLabel;
    private JSpinner              maxWalletConnectionsSpinner;
    private JLabel                maxMinerConnectionsLabel;
    private JSpinner              maxMinerConnectionsSpinner;
    private JCheckBox             competitionModeCheckbox;
    private JButton               saveButton;
    private JLabel                targetAddressLabel;
    private JTextField            targetAddressField;
    private JLabel                troughWalletLabel;
    private JTextField            troughWalletField;

    private Wallet                trough;

    private static SettingsTab    instance    = new SettingsTab();

    public static SettingsTab getInstance() {
        return instance;
    }

    private SettingsTab() {
        this.setBorder(GUIConstants.DEFAULT_BORDER);

        this.layout = new GridBagLayout();
        {
            this.layout.columnWidths = SettingsTab.COL_WIDTHS;
            this.layout.rowHeights = SettingsTab.ROW_HEIGHTS;
            this.layout.columnWeights = SettingsTab.COL_WEIGHTS;
            this.layout.rowWeights = SettingsTab.ROW_WEIGHTS;
            this.setLayout(this.layout);
        }

        this.maxWalletConnectionsSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 127, 1));
        {
            GridBagConstraints maxWalletConnectionsSpinnerConstraints = new GridBagConstraints();
            {
                maxWalletConnectionsSpinnerConstraints.insets = DEFAULT_INSETS;
                maxWalletConnectionsSpinnerConstraints.fill = GridBagConstraints.HORIZONTAL;
                maxWalletConnectionsSpinnerConstraints.gridx = 1;
                maxWalletConnectionsSpinnerConstraints.gridy = 0;
            }
            this.add(this.maxWalletConnectionsSpinner, maxWalletConnectionsSpinnerConstraints);
        }

        this.maxWalletConnectionsLabel = new JLabel("Max Wallet Connections");
        {
            this.maxWalletConnectionsLabel.setLabelFor(this.maxWalletConnectionsSpinner);
            GridBagConstraints maxWalletConnectionsLabelConstraints = new GridBagConstraints();
            {
                maxWalletConnectionsLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                maxWalletConnectionsLabelConstraints.gridx = 0;
                maxWalletConnectionsLabelConstraints.gridy = 0;
            }
            this.add(this.maxWalletConnectionsLabel, maxWalletConnectionsLabelConstraints);
        }

        this.maxMinerConnectionsSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 127, 1));
        {
            GridBagConstraints maxMinerConnectionsSpinnerConstraints = new GridBagConstraints();
            {
                maxMinerConnectionsSpinnerConstraints.fill = GridBagConstraints.HORIZONTAL;
                maxMinerConnectionsSpinnerConstraints.insets = DEFAULT_INSETS;
                maxMinerConnectionsSpinnerConstraints.gridx = 1;
                maxMinerConnectionsSpinnerConstraints.gridy = 1;
            }
            this.add(this.maxMinerConnectionsSpinner, maxMinerConnectionsSpinnerConstraints);
        }

        this.maxMinerConnectionsLabel = new JLabel("Max Miner Connections");
        {
            this.maxMinerConnectionsLabel.setLabelFor(this.maxMinerConnectionsSpinner);
            GridBagConstraints maxMinerConnectionsLabelConstraints = new GridBagConstraints();
            {
                maxMinerConnectionsLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                maxMinerConnectionsLabelConstraints.gridx = 0;
                maxMinerConnectionsLabelConstraints.gridy = 1;
            }
            this.add(this.maxMinerConnectionsLabel, maxMinerConnectionsLabelConstraints);
        }

        this.competitionModeCheckbox = new JCheckBox("Competition Mode");
        {
            this.competitionModeCheckbox
                    .setToolTipText("Toggles the CSE 260 competition mode. A valid target address and trough key pair (all in base64) must be provided. You cannot pay others while competition mode is running.");
            this.competitionModeCheckbox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    boolean ticked = SettingsTab.this.competitionModeCheckbox.isSelected();
                    CoinAddress address;
                    String host = SettingsTab.this.targetAddressField.getText().trim();
                    if (ticked) {
                        try {
                            address = new CoinAddress(host, "Competition Target");
                            if (SettingsTab.this.trough.getAddress().equals(address)) {
                                InfoBox.error("The trough address and the target address must be different!",
                                        "Trough can't equal target");
                                SettingsTab.this.competitionModeCheckbox.setSelected(false);
                                return;
                            }
                        }
                        catch (Exception e1) {
                            InfoBox.error(String.format("%s doesn't look like a valid address!", host), "Invalid Address");
                            SettingsTab.this.competitionModeCheckbox.setSelected(false);
                            return;
                        }

                        CompetitionManager.init(SettingsTab.this.trough, address);
                        new Thread(CompetitionManager.getInstance(), "Competition").start();
                    }
                    else {
                        if (CompetitionManager.getInstance() != null) {
                            CompetitionManager.getInstance().stopRunning();
                        }
                    }

                    SettingsTab.this.targetAddressField.setEditable(!ticked);
                    PaymentsTab.getInstance().setEnabled(!ticked);
                }
            });

            GridBagConstraints competitionModeCheckboxConstraints = new GridBagConstraints();
            {
                competitionModeCheckboxConstraints.insets = GUIConstants.DEFAULT_INSETS;
                competitionModeCheckboxConstraints.gridx = 0;
                competitionModeCheckboxConstraints.gridy = 2;
            }
            this.add(this.competitionModeCheckbox, competitionModeCheckboxConstraints);
        }

        this.targetAddressLabel = new JLabel("Target Address");
        {
            GridBagConstraints targetAddressLabelConstraints = new GridBagConstraints();
            {
                targetAddressLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                targetAddressLabelConstraints.gridx = 0;
                targetAddressLabelConstraints.gridy = 3;
            }

            this.targetAddressField = new JTextField();
            {
                this.targetAddressField.setToolTipText("Where should I direct received funds to?");
                GridBagConstraints targetAddressFieldConstraints = new GridBagConstraints();
                {
                    targetAddressFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                    targetAddressFieldConstraints.insets = DEFAULT_INSETS;
                    targetAddressFieldConstraints.gridx = 1;
                    targetAddressFieldConstraints.gridy = 3;
                }
                this.add(this.targetAddressField, targetAddressFieldConstraints);
            }

            this.add(this.targetAddressLabel, targetAddressLabelConstraints);
            this.targetAddressLabel.setLabelFor(this.targetAddressField);

            this.troughWalletLabel = new JLabel("Trough Wallet");
            {
                GridBagConstraints troughWalletLabelConstraints = new GridBagConstraints();
                {
                    troughWalletLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                    troughWalletLabelConstraints.gridx = 0;
                    troughWalletLabelConstraints.gridy = 4;
                }
                this.add(this.troughWalletLabel, troughWalletLabelConstraints);

                this.troughWalletField = new JTextField();
                {
                    troughWalletField
                            .setToolTipText("When Competition Mode is activated, the currently-selcted wallet will be used as the trough.");
                    troughWalletField.setEditable(false);
                    {
                        GridBagConstraints troughWalletFieldConstraints = new GridBagConstraints();
                        {
                            troughWalletFieldConstraints.insets = DEFAULT_INSETS;
                            troughWalletFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                            troughWalletFieldConstraints.gridx = 1;
                            troughWalletFieldConstraints.gridy = 4;
                        }

                        this.add(this.troughWalletField, troughWalletFieldConstraints);
                    }
                }
            }
        }

        this.saveButton = new JButton("Save");
        {
            GridBagConstraints saveButtonConstraints = new GridBagConstraints();
            saveButtonConstraints.fill = GridBagConstraints.HORIZONTAL;
            {
                saveButtonConstraints.insets = DEFAULT_INSETS;
                saveButtonConstraints.gridx = 1;
                saveButtonConstraints.gridy = 5;
            }

            this.add(this.saveButton, saveButtonConstraints);
        }
    }

    public void setCompetitionTrough(Wallet wallet) {
        if (wallet == null || this.competitionModeCheckbox.isSelected()) return;

        this.trough = wallet;
        this.troughWalletField.setText(String.format("%s (%s)", wallet.getName(), wallet.getAddress()));
    }
}
