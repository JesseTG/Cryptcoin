package jtg.cse260.cryptcoin.gui.tabs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import com.starkeffect.cryptcoin.protocol.CoinAddress;
import com.starkeffect.cryptcoin.protocol.CoinAmount;
import com.starkeffect.cryptcoin.protocol.Transaction;
import com.starkeffect.cryptcoin.protocol.TransactionMessage;

import jtg.cse260.cryptcoin.Resources;
import jtg.cse260.cryptcoin.gui.CCFrame;
import jtg.cse260.cryptcoin.gui.GUIConstants;
import jtg.cse260.cryptcoin.gui.LeftPanel;
import jtg.cse260.cryptcoin.node.NodeManager;
import jtg.cse260.cryptcoin.node.Wallet;
import jtg.util.InfoBox;

@SuppressWarnings("serial")
public class PaymentsTab extends JPanel implements GUIConstants {
    private static final int[]    COL_WIDTHS  = {100, 0};
    private static final int[]    ROW_HEIGHTS = {48, 48, 48, 0, 0};
    private static final double[] COL_WEIGHTS = {0.0, 1.0};
    private static final double[] ROW_WEIGHTS = {0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};

    private GridBagLayout         layout;

    private JTextField            payToField;
    private JLabel                payToLabel;

    private JSpinner              amountSpinner;
    private JLabel                amountLabel;

    private JButton               sendButton;

    private static PaymentsTab    instance    = new PaymentsTab();

    public static PaymentsTab getInstance() {
        return instance;
    }

    private PaymentsTab() {
        this.setBorder(DEFAULT_BORDER);

        this.layout = new GridBagLayout();
        {
            this.layout.columnWidths = PaymentsTab.COL_WIDTHS;
            this.layout.rowHeights = PaymentsTab.ROW_HEIGHTS;
            this.layout.columnWeights = PaymentsTab.COL_WEIGHTS;
            this.layout.rowWeights = PaymentsTab.ROW_WEIGHTS;
            this.setLayout(this.layout);
        }

        this.payToField = new JTextField();
        {
            this.payToField.setToolTipText("Paste in the Cryptcoin address of the recipient.");
            GridBagConstraints payToFieldConstraints = new GridBagConstraints();
            {
                payToFieldConstraints.insets = GUIConstants.DEFAULT_INSETS;
                payToFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                payToFieldConstraints.gridx = 1;
                payToFieldConstraints.gridy = 0;
            }
            this.add(this.payToField, payToFieldConstraints);
        }

        this.payToLabel = new JLabel("Pay to:");
        {
            this.payToLabel.setLabelFor(this.payToField);
            GridBagConstraints payToLabelConstraints = new GridBagConstraints();
            {
                payToLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                payToLabelConstraints.anchor = GridBagConstraints.EAST;
                payToLabelConstraints.gridx = 0;
                payToLabelConstraints.gridy = 0;
            }
            this.add(this.payToLabel, payToLabelConstraints);
        }

        this.amountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        {
            this.amountSpinner.setToolTipText("Select the number of Cryptcoins to pay the recipient.");
            GridBagConstraints amountSpinnerConstraints = new GridBagConstraints();
            {
                amountSpinnerConstraints.ipadx = 64;
                amountSpinnerConstraints.anchor = GridBagConstraints.EAST;
                amountSpinnerConstraints.insets = GUIConstants.DEFAULT_INSETS;
                amountSpinnerConstraints.gridx = 1;
                amountSpinnerConstraints.gridy = 1;
            }
            this.add(this.amountSpinner, amountSpinnerConstraints);
        }

        this.amountLabel = new JLabel("Amount:");
        {
            this.amountLabel.setLabelFor(this.amountSpinner);
            GridBagConstraints amountLabelConstraints = new GridBagConstraints();
            {
                amountLabelConstraints.anchor = GridBagConstraints.EAST;
                amountLabelConstraints.insets = PaymentsTab.DEFAULT_INSETS;
                amountLabelConstraints.gridx = 0;
                amountLabelConstraints.gridy = 1;
            }
            this.add(this.amountLabel, amountLabelConstraints);
        }

        this.sendButton = new JButton("Send!", Resources.getIcon("payments"));
        {
            this.sendButton.setToolTipText("All done?");
            this.sendButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Wallet wallet = LeftPanel.getInstance().getSelectedWallet();
                    int amount = (int)PaymentsTab.this.amountSpinner.getValue();
                    if (wallet == null) {
                        InfoBox.warn("Please select a Wallet to draw money from!", "Select a Wallet");
                    }
                    else if (!wallet.canAfford(amount)) {
                        InfoBox.error("You cannot afford this payment!", "Insufficient Funds");
                    }
                    else {
                        CoinAddress address = null;
                        try {
                            address = new CoinAddress(PaymentsTab.this.payToField.getText().trim(), wallet.getName());
                        }
                        catch (IllegalArgumentException err) {
                            InfoBox.error("This does not look like a valid address!", "Invalid Address");
                            return;
                        }
                        if (address.equals(wallet.getAddress())) {
                            // If we're trying to pay to ourselves...

                            // Remember, it makes no sense for the user to do
                            // this, but transactions that do this are
                            // reasonable
                            InfoBox.warn("You don't need to pay yourself, this is your own money!", "Really?");
                            return;
                        }

                        boolean result = InfoBox.confirmYesNo(
                                new Object[] {
                                        String.format(
                                                "Sending %s CC to %s using Cryptcoins from the wallet \"%s\".%n",
                                                amount,
                                                address, wallet.getName()),
                                        "Is this OK?  This transaction cannot be undone!  If this turns out to be a mistake,"
                                                + " you'll have to argue for reimbursement yourself."
                                },
                                "Confirm");

                        if (result) {
                            // If the user does indeed want to send money...
                            final Transaction transaction = wallet.createTransaction(address, new CoinAmount(amount));
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        NodeManager.getInstance()
                                                .sendGlobalMessage(new TransactionMessage(transaction));
                                        PaymentsTab.this.payToField.setText("");
                                        PaymentsTab.this.amountSpinner.setValue(1);
                                        InfoBox.info(
                                                "Transaction sent successfully! Your wallet will be debited upon confirmation.",
                                                "Success!");
                                    }
                                    catch (Exception e) {
                                        InfoBox.error(new Object[] {"There was a problem in sending this transaction:",
                                                e.getMessage()}, "Error!");
                                    }
                                }
                            });
                        }
                    }
                }

            });
            GridBagConstraints sendButtonConstraints = new GridBagConstraints();
            {
                sendButtonConstraints.anchor = GridBagConstraints.SOUTHEAST;
                sendButtonConstraints.insets = GUIConstants.DEFAULT_INSETS;
                sendButtonConstraints.gridx = 1;
                sendButtonConstraints.gridy = 2;
            }
            this.add(sendButton, sendButtonConstraints);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        this.amountSpinner.setEnabled(enabled);
        this.payToField.setEditable(enabled);
        this.sendButton.setEnabled(enabled);
    }
}
