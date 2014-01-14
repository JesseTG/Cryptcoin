package jtg.cse260.cryptcoin.gui;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import jtg.cse260.cryptcoin.Resources;
import jtg.cse260.cryptcoin.gui.tabs.MiningTab;
import jtg.cse260.cryptcoin.gui.tabs.NetworkTab;
import jtg.cse260.cryptcoin.gui.tabs.PaymentsTab;
import jtg.cse260.cryptcoin.gui.tabs.SettingsTab;
import jtg.cse260.cryptcoin.gui.tabs.TransactionsTab;

@SuppressWarnings("serial")
public class RightPanel extends JPanel {
    private TransactionsTab   transactionsTab;
    private PaymentsTab       paymentsTab;
    private SettingsTab       settingsTab;
    private JTabbedPane       tabs;
    private Component         networkTab;

    private static RightPanel instance = new RightPanel();

    public static RightPanel getInstance() {
        return instance;
    }

    private RightPanel() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.tabs = new JTabbedPane(JTabbedPane.TOP);
        {
            this.tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        }

        this.transactionsTab = TransactionsTab.getInstance();
        {
            this.tabs.addTab("Transactions", Resources.getIcon("transactions"), this.transactionsTab,
                    "See the record of the Cryptcoins that came in and out of this wallet");
        }

        this.paymentsTab = PaymentsTab.getInstance();
        {
            this.tabs.addTab("Payments", Resources.getIcon("payments"), this.paymentsTab,
                    "Pay somebody Cryptcoins in exchange for their goods and services.");
        }

        this.networkTab = new NetworkTab();
        {
            this.tabs.addTab("Network", Resources.getIcon("network"), this.networkTab,
                    "Look at your network connections.");
        }

        this.settingsTab = SettingsTab.getInstance();
        {
            this.tabs.addTab("Settings", Resources.getIcon("settings"), this.settingsTab, "Configure Cryptcoin.");
        }

        for (int i = 0; i < this.tabs.getTabCount(); ++i) {
            this.tabs.setDisplayedMnemonicIndexAt(i, 0);
        }

        this.add(this.tabs);
    }
    
    public void goToTab(final int index) {
        if (0 <= index && index < this.tabs.getTabCount()) {
            this.tabs.setSelectedIndex(index);
        }
    }
}
