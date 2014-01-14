package jtg.cse260.cryptcoin.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.starkeffect.cryptcoin.protocol.CoinAmount;

import jtg.cse260.cryptcoin.node.WalletManager;

@SuppressWarnings("serial")
public class TopPanel extends JPanel {
    private GridBagConstraints constraints;
    private JLabel             balanceLabel;

    private static TopPanel instance = new TopPanel();
    
    public static TopPanel getInstance() {
        return instance;
    }
    
    private TopPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        this.constraints = new GridBagConstraints();
        {
            constraints.anchor = GridBagConstraints.SOUTHWEST;
            constraints.gridwidth = 8;
            constraints.insets = new Insets(5, 5, -3, 5);
            constraints.gridx = 0;
            constraints.gridy = 0;
        }

        this.balanceLabel = new JLabel("Balance: " + WalletManager.getInstance().getTotalBalance());
        this.add(balanceLabel);
    }

    GridBagConstraints getConstraints() {
        return this.constraints;
    }
    
    public void updateBalance() {
        this.balanceLabel.setText("Balance: " + WalletManager.getInstance().getTotalBalance());
    }
}
