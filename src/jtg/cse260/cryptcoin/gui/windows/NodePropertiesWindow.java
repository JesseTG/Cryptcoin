package jtg.cse260.cryptcoin.gui.windows;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import jtg.cse260.cryptcoin.Resources;
import jtg.cse260.cryptcoin.gui.GUIConstants;
import jtg.cse260.cryptcoin.node.Node;
import jtg.cse260.cryptcoin.node.NodeManager;
import jtg.util.JTextAreaWriter;

public class NodePropertiesWindow extends JFrame implements GUIConstants {

    private static final Dimension PREFERRED_SIZE   = new Dimension(550, 350);
    private JPanel                 contentPane;
    private Node                   node;
    private GridBagLayout          layout;
    private JLabel                 addressLabel;
    private JLabel                 nodeAddressLabel;
    private JTextArea              logTextPane;

    private JTextAreaWriter        logPanelWriter;
    private JScrollPane            logTextScrollPane;
    private JButton                okButton;
    private JButton                terminateConnectionButton;
    private JLabel                 logLabel;
    private JButton                clearLogButton;

    /**
     * Create the frame.
     * 
     * @param node The {@link Node} whose properties we want to view
     */
    public NodePropertiesWindow(final Node node) {
        this.node = node;

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setMinimumSize(NodePropertiesWindow.PREFERRED_SIZE);
        this.setPreferredSize(NodePropertiesWindow.PREFERRED_SIZE);
        this.setTitle("Node Connection to " + node.toString());
        this.setName(this.getTitle());
        this.contentPane = new JPanel();
        this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.setContentPane(this.contentPane);

        this.layout = new GridBagLayout();
        {
            this.layout.columnWidths = new int[] {110, 0, 0};
            this.layout.rowHeights = new int[] {0, 0, 0, 0, 0};
            this.layout.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
            this.layout.rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        }
        this.setLayout(this.layout);

        this.addressLabel = new JLabel("Address:");
        {
            GridBagConstraints addressLabelConstraints = new GridBagConstraints();
            {
                addressLabelConstraints.anchor = GridBagConstraints.EAST;
                addressLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                addressLabelConstraints.gridx = 0;
                addressLabelConstraints.gridy = 0;
                this.add(this.addressLabel, addressLabelConstraints);
            }

            this.nodeAddressLabel = new JLabel(this.node.getNetworkAddress().toString());
            {
                GridBagConstraints nodeAddressLabelConstraints = new GridBagConstraints();
                nodeAddressLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                nodeAddressLabelConstraints.gridx = 1;
                nodeAddressLabelConstraints.gridy = 0;
                this.add(this.nodeAddressLabel, nodeAddressLabelConstraints);
            }

            this.addressLabel.setLabelFor(this.nodeAddressLabel);
        }

        this.logLabel = new JLabel("Log:");
        {
            GridBagConstraints logLabelConstraints = new GridBagConstraints();
            {
                logLabelConstraints.anchor = GridBagConstraints.SOUTHWEST;
                logLabelConstraints.insets = GUIConstants.DEFAULT_INSETS;
                logLabelConstraints.gridx = 0;
                logLabelConstraints.gridy = 1;
            }
            this.add(this.logLabel, logLabelConstraints);

            this.clearLogButton = new JButton("Clear Log", Resources.getIcon("clear-log"));
            {
                GridBagConstraints clearLogButtonConstraints = new GridBagConstraints();
                {
                    clearLogButtonConstraints.anchor = GridBagConstraints.EAST;
                    clearLogButtonConstraints.insets = GUIConstants.DEFAULT_INSETS;
                    clearLogButtonConstraints.gridx = 1;
                    clearLogButtonConstraints.gridy = 1;
                    this.add(this.clearLogButton, clearLogButtonConstraints);
                }

                this.clearLogButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        NodePropertiesWindow.this.logTextPane.setText(null);
                        NodePropertiesWindow.this.node.clearLog();
                    }
                });
            }

            this.logTextPane = new JTextArea(this.node.getLog());
            {
                this.logPanelWriter = new JTextAreaWriter(this.logTextPane);
                this.node.addLoggingDestination(this.logPanelWriter);
                this.logTextPane.setEditable(false);
                GridBagConstraints logTextPaneConstraints = new GridBagConstraints();
                {
                    logTextPaneConstraints.insets = GUIConstants.DEFAULT_INSETS;
                    logTextPaneConstraints.gridwidth = 2;
                    logTextPaneConstraints.fill = GridBagConstraints.BOTH;
                    logTextPaneConstraints.gridx = 0;
                    logTextPaneConstraints.gridy = 2;
                }

                this.logTextScrollPane = new JScrollPane(this.logTextPane);
                this.logTextScrollPane.setBorder(GUIConstants.DEFAULT_BORDER);
                this.add(this.logTextScrollPane, logTextPaneConstraints);
            }
        }

        this.terminateConnectionButton = new JButton("Terminate", Resources.getIcon("disconnect"));
        {
            terminateConnectionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    NodePropertiesWindow.this.node.close();
                    NodeManager.getInstance().removeNode(NodePropertiesWindow.this.node);

                }
            });
            GridBagConstraints terminateConnectionButtonConstraints = new GridBagConstraints();
            {
                terminateConnectionButtonConstraints.insets = GUIConstants.DEFAULT_INSETS;
                terminateConnectionButtonConstraints.gridx = 0;
                terminateConnectionButtonConstraints.gridy = 3;
                this.add(this.terminateConnectionButton, terminateConnectionButtonConstraints);
            }
        }

        this.okButton = new JButton("OK");
        {
            GridBagConstraints okButtonConstraints = new GridBagConstraints();
            {
                okButtonConstraints.anchor = GridBagConstraints.EAST;
                okButtonConstraints.gridx = 1;
                okButtonConstraints.gridy = 3;
                this.add(this.okButton, okButtonConstraints);
            }
            this.okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    NodePropertiesWindow.this.dispose();
                }
            });
        }
        this.pack();
        this.setVisible(true);
    }

    @Override
    public void dispose() {
        this.node.removeLoggingDestination(this.logPanelWriter);
        super.dispose();
    }
}
