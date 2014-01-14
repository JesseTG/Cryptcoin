package jtg.cse260.cryptcoin.gui.tabs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.starkeffect.cryptcoin.protocol.NetworkAddress;
import com.starkeffect.cryptcoin.protocol.Parameters;

import jtg.cse260.cryptcoin.Resources;
import jtg.cse260.cryptcoin.gui.GUIConstants;
import jtg.cse260.cryptcoin.gui.windows.NodePropertiesWindow;
import jtg.cse260.cryptcoin.node.Node;
import jtg.cse260.cryptcoin.node.NodeManager;
import jtg.util.InfoBox;

import java.awt.Insets;

import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.border.MatteBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

@SuppressWarnings("serial")
public class NetworkTab extends JPanel implements GUIConstants {
    private static final Parameters PARAMS = Parameters.getInstance();
    private JList<Node>             nodeList;
    private GridBagLayout           layout;
    private JScrollPane             nodePane;
    private JButton                 nodePropertiesButton;
    private JButton                 addAddressButton;
    private JTextField              addAddressField;
    private JLabel                  hostNameLabel;
    private JLabel                  portLabel;
    private JSpinner                portSpinner;

    public NetworkTab() {
        this.layout = new GridBagLayout();
        {
            this.layout.columnWidths = new int[] {0, 0, 0, 0, 0, 0};
            this.layout.rowHeights = new int[] {0};
            this.layout.columnWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, 0.0};
            this.layout.rowWeights = new double[] {1.0, 0.0};
            this.setLayout(this.layout);
        }

        this.setBorder(GUIConstants.DEFAULT_BORDER);

        this.nodeList = new JList<>(NodeManager.getInstance().getNodeModel());
        {
            this.nodeList.setBorder(null);
            this.nodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            GridBagConstraints nodeListConstraints = new GridBagConstraints();

            {
                nodeListConstraints.fill = GridBagConstraints.BOTH;
                nodeListConstraints.gridwidth = 6;
                nodeListConstraints.gridx = 0;
                nodeListConstraints.gridy = 0;
                nodeListConstraints.insets = new Insets(0, 0, 5, 0);
            }

            this.nodeList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    nodePropertiesButton.setEnabled(nodeList.getSelectedIndex() >= 0);
                }
            });

            this.nodePane = new JScrollPane(this.nodeList);
            this.add(this.nodePane, nodeListConstraints);
        }

        this.nodePropertiesButton = new JButton("Properties", Resources.getIcon("info"));
        {
            GridBagConstraints nodePropertiesButtonConstraints = new GridBagConstraints();

            {
                nodePropertiesButtonConstraints.gridwidth = 1;
                nodePropertiesButtonConstraints.gridx = 0;
                nodePropertiesButtonConstraints.gridy = 1;
                nodePropertiesButtonConstraints.weightx = 0.1;
                nodePropertiesButtonConstraints.fill = GridBagConstraints.VERTICAL;
                nodePropertiesButtonConstraints.insets = DEFAULT_INSETS;
            }

            this.nodePropertiesButton.setEnabled(false);
            this.nodePropertiesButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Node selected = NetworkTab.this.nodeList.getSelectedValue();
                    if (selected != null) {
                        new NodePropertiesWindow(selected);
                    }
                }
            });
            this.add(this.nodePropertiesButton, nodePropertiesButtonConstraints);

            addAddressButton = new JButton("Add Peer", Resources.getIcon("add-peer"));
            {
                addAddressButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        NetworkAddress address;
                        InetAddress inet;
                        int port;
                        String host = NetworkTab.this.addAddressField.getText();
                        if (host == null || host.length() == 0) {
                            InfoBox.error("Please enter an address.", "No Address Given");
                            return;
                        }
                        try {
                            inet = InetAddress.getByName(host);
                            port = (int)NetworkTab.this.portSpinner.getValue();
                            address = new NetworkAddress(inet, port);
                        }
                        catch (UnknownHostException e1) {
                            InfoBox.error(
                                    "Couldn't connect to that address! Are you sure it's a valid hostname, and that there's a valid Cryptcoin on that address?",
                                    "Could Not Connect");
                            return;
                        }

                        if (NodeManager.getInstance().hasAddress(address)) {
                            InfoBox.error("I already have this address on record.", "No need to repeat yourself");
                        }
                        else {
                            NodeManager.getInstance().addAddress(address);
                            new Thread(NodeManager.getInstance().createWallet(address)).start();
                        }
                    }
                });

                GridBagConstraints addAddressButtonConstraints = new GridBagConstraints();

                {
                    addAddressButtonConstraints.fill = GridBagConstraints.VERTICAL;
                    addAddressButtonConstraints.insets = DEFAULT_INSETS;
                    addAddressButtonConstraints.gridx = 1;
                    addAddressButtonConstraints.gridy = 1;
                    addAddressButtonConstraints.weightx = 0.1;
                }
                add(addAddressButton, addAddressButtonConstraints);
            }
        }

        addAddressField = new JTextField();
        {
            GridBagConstraints addAddressFieldConstraints = new GridBagConstraints();
            addAddressFieldConstraints.weightx = 0.2;
            {
                addAddressFieldConstraints.insets = DEFAULT_INSETS;
                addAddressFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                addAddressFieldConstraints.gridx = 3;
                addAddressFieldConstraints.gridy = 1;
            }

            hostNameLabel = new JLabel("Host:");
            {
                GridBagConstraints hostNameLabelConstraints = new GridBagConstraints();
                {
                    hostNameLabelConstraints.fill = GridBagConstraints.VERTICAL;
                    hostNameLabelConstraints.insets = DEFAULT_INSETS;
                    hostNameLabelConstraints.anchor = GridBagConstraints.EAST;
                    hostNameLabelConstraints.gridx = 2;
                    hostNameLabelConstraints.gridy = 1;
                }
                add(hostNameLabel, hostNameLabelConstraints);
                add(addAddressField, addAddressFieldConstraints);
                hostNameLabel.setLabelFor(addAddressField);
                addAddressField.setColumns(32);
            }
        }

        portLabel = new JLabel("Port:");
        {
            GridBagConstraints portLabelConstraints = new GridBagConstraints();
            {
                portLabelConstraints.insets = DEFAULT_INSETS;
                portLabelConstraints.anchor = GridBagConstraints.EAST;
                portLabelConstraints.gridx = 4;
                portLabelConstraints.gridy = 1;
            }
            add(portLabel, portLabelConstraints);

            portSpinner = new JSpinner();
            {
                portSpinner
                        .setToolTipText("Select the port here (the part that comes after the colon); leave as default if unsure");
                portSpinner.setModel(new SpinnerNumberModel(PARAMS.DEFAULT_PORT, 1, 65553, 1));
                GridBagConstraints portSpinnerConstraints = new GridBagConstraints();

                {
                    portSpinnerConstraints.insets = DEFAULT_INSETS;
                    portSpinnerConstraints.gridx = 5;
                    portSpinnerConstraints.gridy = 1;
                    portSpinnerConstraints.weightx = 0.1;
                }
                this.portLabel.setLabelFor(this.portSpinner);
                add(portSpinner, portSpinnerConstraints);
            }
        }

    }
}
