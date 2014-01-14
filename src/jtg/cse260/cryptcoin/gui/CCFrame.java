package jtg.cse260.cryptcoin.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import com.starkeffect.cryptcoin.protocol.NetworkAddress;

import jtg.cse260.cryptcoin.CompetitionManager;
import jtg.cse260.cryptcoin.Main;
import jtg.cse260.cryptcoin.node.NodeManager;
import jtg.cse260.cryptcoin.node.WalletManager;
import jtg.cse260.cryptcoin.node.WalletNode;

@SuppressWarnings("serial")
public class CCFrame extends JFrame {
    private JTextComponent         selectedTextField;
    private static final Dimension PREFERRED_SIZE = new Dimension(740, 540);

    private LeftPanel              leftPanel;

    private RightPanel             rightPanel;
    private Menu                   menu;
    private TopPanel               topPanel;

    private JSplitPane             mainPanel;

    private static CCFrame         instance       = new CCFrame();

    public static CCFrame getInstance() {
        return instance;
    }

    /**
     * Create the application.
     */
    private CCFrame() {
        this.initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        this.setTitle(String.format("%s (%s)", Main.NAME, Main.VERSION));
        this.setMinimumSize(CCFrame.PREFERRED_SIZE);
        this.setPreferredSize(CCFrame.PREFERRED_SIZE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GridBagLayout layout = new GridBagLayout();
        {
            layout.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
            layout.rowHeights = new int[] {30, 0, -36, 0, 0, 0, 59, 0};
            layout.columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
            layout.rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
        }
        this.setLayout(layout);

        this.topPanel = TopPanel.getInstance();
        this.add(this.topPanel, this.topPanel.getConstraints());

        this.mainPanel = new JSplitPane();
        {
            this.mainPanel.setContinuousLayout(true);
            this.mainPanel.setResizeWeight(0.1);
            this.mainPanel.setBorder(null); // Default border that we don't want
            GridBagConstraints mainPanelConstraints = new GridBagConstraints();
            {
                mainPanelConstraints.insets = new Insets(10, 5, 25, 5);
                mainPanelConstraints.gridheight = 6;
                mainPanelConstraints.gridwidth = 8;
                mainPanelConstraints.fill = GridBagConstraints.BOTH;
                mainPanelConstraints.gridx = 0;
                mainPanelConstraints.gridy = 1;
            }
            this.add(this.mainPanel, mainPanelConstraints);
        }
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                CCFrame.this.setVisible(false);
            }
        });

        this.leftPanel = LeftPanel.getInstance();
        this.rightPanel = RightPanel.getInstance();

        this.mainPanel.setLeftComponent(this.leftPanel);
        this.mainPanel.setRightComponent(this.rightPanel);
        this.menu = new Menu();
        this.setJMenuBar(this.menu);

        this.pack();
        this.setVisible(true);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (NetworkAddress n : NodeManager.getInstance().getAvailableAddresses()) {
                    new Thread(NodeManager.getInstance().createWallet(n)).start();
                }
            }
        });
    }

    /**
     * Cuts the selected text from the currently-selected text component, if
     * any.
     */
    public void cut() {
        if (this.selectedTextField != null) {
            this.selectedTextField.cut();
        }
    }

    public void copy() {
        if (this.selectedTextField != null) {
            this.selectedTextField.copy();
        }
    }

    public void paste() {
        if (this.selectedTextField != null) {
            this.selectedTextField.paste();
        }
    }

    public void delete() {
        if (this.selectedTextField != null) {
            this.selectedTextField.setText("");
        }
    }
    
    @Override
    public void dispose() {
        CompetitionManager cm = CompetitionManager.getInstance();
        if (cm != null) cm.stopRunning();
        
        super.dispose();
    }

}
