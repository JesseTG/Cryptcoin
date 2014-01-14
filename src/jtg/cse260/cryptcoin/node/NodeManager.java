package jtg.cse260.cryptcoin.node;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import jtg.cse260.cryptcoin.Main;
import jtg.cse260.cryptcoin.block.BlockTree;
import jtg.util.MultiWriter;
import jtg.util.Singleton;

import com.starkeffect.cryptcoin.protocol.AddressesMessage;
import com.starkeffect.cryptcoin.protocol.AddressesMessage$Item;
import com.starkeffect.cryptcoin.protocol.Block;
import com.starkeffect.cryptcoin.protocol.BlockMessage;
import com.starkeffect.cryptcoin.protocol.GetDataMessage;
import com.starkeffect.cryptcoin.protocol.InventoryItem;
import com.starkeffect.cryptcoin.protocol.InventoryMessage;
import com.starkeffect.cryptcoin.protocol.Message;
import com.starkeffect.cryptcoin.protocol.NetworkAddress;
import com.starkeffect.cryptcoin.protocol.NotFoundMessage;
import com.starkeffect.cryptcoin.protocol.Parameters;
import com.starkeffect.cryptcoin.protocol.Timestamp;
import com.starkeffect.cryptcoin.protocol.Transaction;
import com.starkeffect.cryptcoin.protocol.TransactionMessage;

public class NodeManager implements Serializable, Singleton<BlockTree>
{
    private static final long        serialVersionUID     = -4331536546559906323L;
    private static final NodeManager instance;
    private static final int         TREE_FILLER_INTERVAL = 60 * 1000;
    private static final int         RECONNECT_INTERVAL   = 60 * 2000;
    private static final Parameters  PARAMS               = Parameters.getInstance();
    static {
        instance = new NodeManager();
    }

    /**
     * Gets the {@code NodeManager}.
     * 
     * @return The lone instance of the {@code NodeManager}.
     */
    public static NodeManager getInstance() {
        return NodeManager.instance;
    }

    /**
     * The addresses available to connect to
     */
    private Map<NetworkAddress, AddressAvailability> addresses;

    private PrintWriter                              globalLogger;

    private MultiWriter                              globalWriter;

    private DefaultListModel<Node>                   model;

    /**
     * The {@code Node}s that are currently active.
     */
    private Collection<Node>                         nodes;

    private Timer                                    treeFiller;
    private Timer                                    reconnector;

    /**
     * Private constructor.
     */
    private NodeManager() {
        this.nodes = new CopyOnWriteArraySet<>();
        this.addresses = new ConcurrentHashMap<>();
        this.model = new DefaultListModel<>();
        this.treeFiller = new Timer();
        this.reconnector = new Timer();
        this.globalWriter = new MultiWriter();
        this.globalLogger = new PrintWriter(this.globalWriter);

        List<NetworkAddress> list = NodeManager.PARAMS.getWellKnownNodes();
        for (final NetworkAddress n : list) {
            this.addresses.put(n, AddressAvailability.AVAILABLE);
        }

        this.treeFiller.schedule(new TreeFillerTask(), TREE_FILLER_INTERVAL, TREE_FILLER_INTERVAL);
        this.reconnector.scheduleAtFixedRate(new ReconnecterTask(), RECONNECT_INTERVAL, RECONNECT_INTERVAL);
    }

    public synchronized boolean addAddress(final NetworkAddress address) {
        if (this.addresses.containsKey(address)) { return false; }

        this.addresses.put(address, AddressAvailability.AVAILABLE);
        return true;
    }

    /**
     * Operation createMiner
     * 
     * @param address -
     * @return MinerNode
     */
    public synchronized MinerNode createMiner(final NetworkAddress address) {
        if (this.addresses.get(address) == AddressAvailability.USED) return null;
        MinerNode mn = new MinerNode(address);
        this.addresses.put(address, AddressAvailability.USED);
        this.nodes.add(mn);
        this.model.addElement(mn);
        this.globalWriter.addWriter(mn.log);
        return mn;
    }

    /**
     * Establishes a new connection to a remote node.
     * 
     * @param address The address to connect to
     * @return WalletNode
     */
    public synchronized WalletNode createWallet(final NetworkAddress address) {
        if (this.addresses.get(address) == AddressAvailability.USED) return null;
        WalletNode wn = new WalletNode(address);
        this.addresses.put(address, AddressAvailability.USED);
        this.nodes.add(wn);
        this.model.addElement(wn);
        this.globalWriter.addWriter(wn.log);
        return wn;
    }

    /**
     * Returns every {@link NetworkAddress} available to this client. No
     * guarantees are made about the order.
     * 
     * @return NetworkAddress[]
     */
    public NetworkAddress[] getAddresses() {
        return (NetworkAddress[])this.addresses.keySet().toArray();
    }

    /**
     * Returns every {@link NetworkAddress} not currently being used by a
     * {@link Node}. No guarantees are made about the order.
     * 
     * @return NetworkAddress[]
     */
    public NetworkAddress[] getAvailableAddresses() {
        Set<NetworkAddress> na = new HashSet<>(this.addresses.size());
        for (NetworkAddress i : this.addresses.keySet()) {
            if (this.addresses.get(i) == AddressAvailability.AVAILABLE) {
                na.add(i);
            }
        }

        return na.toArray(new NetworkAddress[na.size()]);
    }

    /**
     * Returns a {@link ListModel} containing the {@link Node}s available,
     * suitable for use in a GUI.
     * 
     * @return ListModel
     */
    public ListModel<Node> getNodeModel() {
        return this.model;
    }

    public boolean hasAddress(final NetworkAddress address) {
        return this.addresses.containsKey(address);
    }

    public synchronized void sendGlobalMessage(final Message message) {
        for (Node n : this.nodes) {
            if (n.getNodeState() == NodeState.WAITING) {
                n.send(message);
            }
        }
    }

    synchronized void handleMessage(final Node node, final PrintWriter logger, final Message message) {
        switch (message.getClass().getSimpleName()) {
            case "AddressesMessage":
                AddressesMessage addrmesg = (AddressesMessage)message;
                logger.println("Received addresses:");
                for (AddressesMessage$Item i : addrmesg.items) {
                    logger.printf("\t%s (Last activity: %s)%n", i.address, new Date(i.timestamp.getTime()));
                    if (!this.addresses.containsKey(i.address)) {
                        Node n = NodeManager.getInstance().createWallet(i.address);
                        new Thread(n, "Node connection to " + n.getNetworkAddress()).start();
                    }
                    this.addresses.put(i.address, AddressAvailability.AVAILABLE);
                }
                break;
            case "BlockMessage":
                Block block = ((BlockMessage)message).block;
                if (BlockTree.getInstance().addBlock(block)) {
                    logger.println("\tAdded to block chain");
                }

                if (BlockTree.getInstance().getBlock(block.prevHash) == null) {
                    // If we haven't seen this block's parent before...
                    node.send(new GetDataMessage(new InventoryItem[] {new InventoryItem(InventoryItem.BLOCK_TYPE,
                            block.prevHash)}));
                }

                break;
            case "GetAddressesMessage":
                logger.println("Sending addresses to peer: (timestamp retreival not implemented)");
                Iterator<NetworkAddress> it = this.addresses.keySet().iterator();
                ArrayList<AddressesMessage$Item> items = new ArrayList<>();
                for (int i = 0; i < NodeManager.PARAMS.ADDRESSES_MAX && it.hasNext(); ++i) {
                    // TODO: Get the time we last got a connection from the
                    // relevant NetworkAddress
                    NetworkAddress n = it.next();
                    items.add(new AddressesMessage$Item(n, new Timestamp()));
                    logger.printf("\t%s%n", n);
                }
                AddressesMessage addrMessage = new AddressesMessage(items.toArray(new AddressesMessage$Item[items
                        .size()]));
                node.send(addrMessage);
                break;
            case "GetBlocksMessage":
                logger.println("Peer requesting block data (not yet implemented)");
                break;
            case "GetDataMessage":
                GetDataMessage gdm = (GetDataMessage)message;
                Collection<InventoryItem> notFound = new ArrayList<>();
                for (InventoryItem i : gdm.items) {
                    if (i.type == InventoryItem.BLOCK_TYPE) {
                        Block b = BlockTree.getInstance().getBlock(i.hash);
                        if (b == null) {
                            notFound.add(i);
                        }
                        else {
                            node.send(new BlockMessage(b));
                        }
                    }
                    else if (i.type == InventoryItem.TRANSACTION_TYPE) {
                        Transaction t = BlockTree.getInstance().getTransaction(i.hash);
                        if (t == null) {
                            notFound.add(i);
                        }
                        else {
                            node.send(new TransactionMessage(t));
                        }
                    }
                }

                if (!notFound.isEmpty()) {
                    // If the peer asked us for at least one item we couldn't
                    // find...
                    node.send(new NotFoundMessage(notFound.toArray(new InventoryItem[notFound.size()])));
                }

                logger.println("Peer requesting data about particular items (not yet implemented)");
                break;
            case "InventoryMessage":
                InventoryMessage invmesg = (InventoryMessage)message;
                logger.println("Received inventory data:");

                Collection<InventoryItem> blockinv = new HashSet<>(invmesg.items.length / 4);
                Collection<Block> headCandidates = new ArrayList<>(4);
                for (InventoryItem i : invmesg.items) {
                    // For each inventory item we got...
                    logger.printf("\t%s (%s)%n", (i.type == InventoryItem.BLOCK_TYPE ? "Block" : "Transaction"),
                            i.hash);
                    if (i.type == InventoryItem.BLOCK_TYPE && blockinv.size() < NodeManager.PARAMS.INVENTORY_MAX) {
                        Block b = BlockTree.getInstance().getBlock(i.hash);
                        if (b == null) {
                            // If we don't have the block with the given hash...
                            blockinv.add(i);
                        }
                        else {
                            headCandidates.add(b);
                        }
                    }
                }

                if (!headCandidates.isEmpty()) {
                    // If we've been sent at least one blo
                    for (Block b : headCandidates) {
                        if (!BlockTree.getInstance().setHead(b)) {
                            break;
                        }
                    }
                }

                if (!blockinv.isEmpty()) {
                    // If we got at least one block...
                    node.send(new GetDataMessage(blockinv.toArray(new InventoryItem[blockinv.size()])));
                }
                break;
            default:
                logger.println("Received a message: " + message.getClass().getSimpleName());
        }
    }

    public synchronized void removeNode(final Node node) {
        this.addresses.put(node.getNetworkAddress(), AddressAvailability.AVAILABLE);
        this.model.removeElement(node);
        this.nodes.remove(node);
        this.globalWriter.removeWriter(node.log);
    }

    private static enum AddressAvailability {
        AVAILABLE,
        USED,
    }

    private class TreeFillerTask extends TimerTask {
        @Override
        public void run() {
            if (NodeManager.this.nodes.isEmpty()) { return; }
            Block[] missingParents = BlockTree.getInstance().getOrphans();
            if (missingParents.length == 0) { return; }

            NodeManager.this.globalLogger.println("GLOBAL: Filling in tree");

            int len = Math.min(missingParents.length, NodeManager.PARAMS.INVENTORY_MAX);
            InventoryItem[] items = new InventoryItem[len];
            for (int i = 0; i < len; ++i) {
                items[i] = new InventoryItem(InventoryItem.BLOCK_TYPE, missingParents[i].prevHash);
            }

            NodeManager.this.sendGlobalMessage(new GetDataMessage(items));
        }
    }

    /**
     * Task that reconnects all available addresses
     * 
     * @author jesse
     * 
     */
    private class ReconnecterTask extends TimerTask {
        @Override
        public void run() {
            NodeManager.this.globalLogger.println("GLOBAL: Reconnecting to all known peers");
            Collection<Node> toRemove = new HashSet<>();
            for (Node i : NodeManager.this.nodes) {
                // For each node available...
                if (i.getNodeState() == NodeState.CLOSED) {
                    // If the current node was disconnected...
                    toRemove.add(i);
                }
            }

            for (Node i : toRemove) {
                // For each node slated for cleanup...
                NodeManager.this.removeNode(i);
            }

            for (Node i : toRemove) {
                // For each node we just removed......
                new Thread(NodeManager.this.createWallet(i.getNetworkAddress())).start();
                
            }

        }
    }
}
