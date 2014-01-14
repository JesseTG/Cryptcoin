package jtg.cse260.cryptcoin.node;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import jtg.cse260.cryptcoin.Main;
import jtg.util.MultiWriter;

import com.starkeffect.cryptcoin.protocol.Block;
import com.starkeffect.cryptcoin.protocol.BlockMessage;
import com.starkeffect.cryptcoin.protocol.CoinbaseTransaction;
import com.starkeffect.cryptcoin.protocol.ConnectAckMessage;
import com.starkeffect.cryptcoin.protocol.ConnectMessage;
import com.starkeffect.cryptcoin.protocol.ErrorMessage;
import com.starkeffect.cryptcoin.protocol.GetBlocksMessage;
import com.starkeffect.cryptcoin.protocol.Hash;
import com.starkeffect.cryptcoin.protocol.InventoryItem;
import com.starkeffect.cryptcoin.protocol.Message;
import com.starkeffect.cryptcoin.protocol.NetworkAddress;
import com.starkeffect.cryptcoin.protocol.NotFoundMessage;
import com.starkeffect.cryptcoin.protocol.Parameters;
import com.starkeffect.cryptcoin.protocol.PingMessage;
import com.starkeffect.cryptcoin.protocol.PongMessage;
import com.starkeffect.cryptcoin.protocol.Transaction;
import com.starkeffect.cryptcoin.protocol.Transaction$Output;
import com.starkeffect.cryptcoin.protocol.TransactionMessage;

/**
 * A connection to a Cryptcoin peer. Nodes should be very stupid. They should
 * only know how to connect to network addresses (which are given to them) and
 * act on certain simple messages (pinging, acknowledgement, etc.). Everything
 * else is delegated to the {@link MessageHandler}.
 * 
 * @author jesse
 */
public abstract class Node implements Runnable
{

    protected static final Parameters   PARAMS             = Parameters.getInstance();

    protected static final PingMessage  PING               = new PingMessage();

    /** So we don't have to create a new PongMessage for every ping */
    protected static final PongMessage  PONG               = new PongMessage();

    protected static final ErrorMessage NOT_A_SERVER_ERROR = new ErrorMessage("Not a server node: Look elsewhere");

    /**
     * A border for {@link ErrorMessage}s so the user can see errors more
     * clearly
     */
    private static final String         ERROR_DELIMITER    = "!!!!!!!!##============================================================##!!!!!!!!";

    /** The root of the block tree. */
    private static final Block          GENESIS            = Node.PARAMS.getGenesisBlock();

    /**
     * The state this Node is in.
     */
    private NodeState                   state;

    /**
     * The address of the peer this Node is connected to.
     */
    private final NetworkAddress        peer;

    /**
     * The actual {@link Socket} connection.
     */
    private Socket                      connection;
    private final StringWriter          stringLog;
    protected final MultiWriter         log;
    protected ObjectInputStream         messageIn;
    protected ObjectOutputStream        messageOut;
    protected final int                 nonce;

    protected PrintWriter               logger;
    private Timer                       pinger;

    private Timer                       disconnector;

    /**
     * Constructs a {@code Node} that will eventually connect to the given
     * {@link NetworkAddress}.
     * 
     * @param peer The {@code NetworkAddress} to connect to.
     */
    public Node(final NetworkAddress peer) {
        this.peer = peer;
        this.state = NodeState.IDLE;
        this.stringLog = new StringWriter();
        this.log = new MultiWriter(this.stringLog);
        this.logger = new PrintWriter(this.log);
        this.pinger = new Timer(peer + " pinger");
        this.nonce = Main.RANDOM.nextInt();
    }

    /**
     * Adds a {@link Writer} as another destination for this {@code Node}'s
     * logging output. this.pinger.
     * 
     * @param writer The additional {@code Writer} to send output to.
     */
    public void addLoggingDestination(final Writer writer) {
        this.log.addWriter(writer);
    }

    /**
     * Clears the contents of the log this {@code Node} has accumulated.
     */
    public void clearLog() {
        int len = this.stringLog.getBuffer().length();
        this.stringLog.getBuffer().delete(0, len);
    }

    /**
     * Closes this Node's connection to the peer.
     */
    public synchronized void close() {
        try {
            if (this.connection != null) {
                // If we actually connected to the Internet at all...
                this.connection.close();
                this.logger.printf("Network connection to %s closed%n", this.peer);
            }
            else {
                this.logger.println("Network connection was never actually established");
            }
        }
        catch (IOException e) {
            this.logger.printf("Problem in closing the connection to %s: %s%n", this.peer, e);
        }
        finally {
            this.state = NodeState.CLOSED;
            this.pinger.cancel();
            if (this.disconnector != null) {
                this.disconnector.cancel();
            }
        }
    }

    /**
     * Returns all of the text logged
     * 
     * @return The entire log
     */
    public String getLog() {
        return this.stringLog.toString();
    }

    /**
     * Returns the Internet address this Node is connected to.
     * 
     * @return The {@link NetworkAddress} of this Node's peer
     */
    public NetworkAddress getNetworkAddress() {
        return this.peer;
    }

    /**
     * Returns the current state of this Node.
     * 
     * @return The state of this Node as a {@link NodeState}
     */
    public NodeState getNodeState() {
        return this.state;
    }

    /**
     * Stops sending this {@code Node}'s logging output to to the given
     * {@link Writer}.
     * 
     * @param writer The {@code Writer} to stop logging to.
     */
    public void removeLoggingDestination(final Writer writer) {
        this.log.removeWriter(writer);
    }

    @Override
    public void run() {
        this.connect();

        try {
            this.send(new ConnectMessage(Main.FULL_NAME, this.nonce));
            while (true) {
                Message m = this.receive();
                this.handleMessage(m);
            }
        }
        catch (EOFException e) {
            this.logger.println("Connection terminated by the remote node");
        }
        catch (IOException e) {
            this.logger.println("I/O Error: " + e);
        }
        finally {
            this.close();
        }
    }

    /**
     * Sends the given message to the peer.
     * 
     * @param message The {@link Message} to send
     * @throws IllegalStateException If this {@code Node} is not connected to a
     *         peer.
     */
    public synchronized void send(final Message message) throws IllegalStateException {
        if (this.connection == null) {
            String mesg = String.format("Attempted to send message %s without being connected to a peer", message
                    .getClass().getSimpleName());
            this.logger.println(mesg);
            throw new IllegalStateException(mesg);
        }
        try {
            this.state = NodeState.SENDING;
            this.logger.printf("Sending %s to peer%n", message.getClass().getSimpleName());
            this.messageOut.writeObject(message);
            this.messageOut.flush();
            this.state = NodeState.WAITING;
            this.messageOut.reset();
        }
        catch (IOException e) {
            this.logger.printf("Error in sending %s: %s%n", message.getClass().getSimpleName(), e);
            this.close();
        }
    }

    protected void connect() {
        try {
            this.state = NodeState.CONNECTING;
            this.logger.println("Attempting to connect to " + this.peer);
            this.connection = this.peer.connect();
            this.messageOut = new ObjectOutputStream(this.connection.getOutputStream());
            this.messageIn = new ObjectInputStream(this.connection.getInputStream());
            this.pinger.scheduleAtFixedRate(new PingerTask(), Node.PARAMS.PING_INTERVAL, Node.PARAMS.PING_INTERVAL);
            this.logger.println("Connection successful");
        }
        catch (IOException e) {
            this.state = NodeState.CLOSED;
            this.logger.printf("Connection error: %s%n", e);
        }
    }

    /**
     * Delegates a given message to the {@link NodeManager}. The delegated
     * messages should rely on resources shared between {@code Node}s, including
     * {@link NetworkAddress}es, {@link InventoryItem}s, and the like.
     * 
     * @param message The actual {@link Message} to handle
     */
    protected void delegateToManager(final Message message) {
        NodeManager.getInstance().handleMessage(this, this.logger, message);
    }

    /**
     * Performs preliminary validation on a {@link Block}. The elements of this
     * {@code Block} that do not depend on other {@code Block}s are validated.
     * The elements validated include:
     * 
     * <ul>
     * <li>The presence of a block (and of a {@link Hash} for it)
     * <li>That the block's difficulty matches its number of leading zeroes
     * <li>The presence of a parent {@code Hash} (unless it's the genesis block)
     * <li>The presence of at least one transaction
     * <li>The correct amount and order of {@link CoinbaseTransaction}s
     * <li>That the block's difficulty is at least
     * {@link Parameters#MINIMUM_DIFFICULTY}
     * <li>That the block's timestamp is no more than
     * {@link Parameters#BLOCK_TIME_SKEW} milliseconds in the future
     * </ul>
     * 
     * Also logs output.
     * 
     * @param block The {@code Block} to validate
     * @return True if {@code block} passes preliminary validation.
     */
    protected boolean validateBlock(final Block block) {
        if (block == null) {
            // If we never actually got a block...
            this.logger.println("\tExpected a block, received nothing");
            return false;
        }

        if (block.getHash().equals(Node.GENESIS.getHash())) {
            // If this is the genesis block...
            this.logger.println("\tThis is the genesis block");
            return true;
        }

        if (block.getHash() == null) {
            // If this block has no valid hash...
            this.logger.println("\tNo hash could be computed");
            return false;
        }

        if (block.getHash().numZeroes() < block.difficulty) {
            // If this block has the wrong amount of leading zeroes...
            this.logger.printf("\tBlock hash has insufficient leading zeroes (got %d, expected at least %d)%n",
                    block.getHash().numZeroes(), block.difficulty);
            return false;
        }

        if (block.prevHash == null) {
            // If this block has no parent and it's not the genesis block...
            this.logger.println("\tNo parent hash found, and this isn't the genesis block");
            return false;
        }

        if (block.transactions == null || block.transactions.length <= 0) {
            // If this block has no transactions to its name...
            this.logger.println("\tNo transactions found");
            return false;
        }

        if (!(block.transactions[0] instanceof CoinbaseTransaction)) {
            // If the first transaction isn't a CoinbaseTransaction...
            this.logger.println("\tThe first transaction must be a CoinbaseTransaction");
            return false;
        }

        for (int i = 1; i < block.transactions.length; ++i) {
            // For each transaction in the block except the first...
            if (block.transactions[i] instanceof CoinbaseTransaction) {
                // If it's a CoinbaseTransaction when it shouldn't be...
                this.logger
                        .printf(
                                "\tOnly the first transaction in a block can be a CoinbaseTransaction (found one at index %d)%n",
                                i);
                return false;
            }
        }
        if (block.difficulty < Node.PARAMS.MINIMUM_DIFFICULTY) {
            // If this block's difficulty isn't at the minimum...
            this.logger.printf("\tExpected difficulty of at least %d, got %d%n", Node.PARAMS.MINIMUM_DIFFICULTY,
                    block.difficulty);
            return false;
        }

        long time = System.currentTimeMillis();
        if (block.timestamp.getTime() - time > Node.PARAMS.BLOCK_TIME_SKEW) {
            // If this block's timestamp comes from too far into the future...
            this.logger.printf("\tTimestamps can be no more than %d ms in the future (timestamp is %d ms ahead)%n",
                    Node.PARAMS.BLOCK_TIME_SKEW,
                    block.timestamp.getTime() - time);
            return false;
        }

        for (final Transaction t : block.transactions) {
            // For each of the given block's transactions...
            if (!this.validateTransaction(t)) {
                // If the transaction doesn't check out...
                return false;
            }
        }

        return true;
    };

    private void handleMessage(final Message message) {
        if (message == null) {
            // If we didn't actually get a message...
            this.logger.printf("Connection terminated by host, disconnecting");
            this.close();
            return;
        }
        switch (message.getClass().getSimpleName()) {
        // Normally I wouldn't do a switch/case like this, but we're not allowed
        // to change the names of or remove these classes, and this code won't
        // be used far enough into the future where this will be an issue.
            case "BlockMessage":
                Block block = ((BlockMessage)message).block;
                this.logger.println("Received a block: " + block.getHash());
                if (this.validateBlock(block)) {
                    this.delegateToManager(message);
                }
                else {
                    this.logger.println("Received block was invalid");
                }
                break;
            case "ConnectMessage":
                ConnectMessage connmesg = (ConnectMessage)message;
                this.logger.printf("Remote node \"%s\" requesting a connection (nonce %d)%n", connmesg.version,
                        connmesg.nonce);
                this.send(Node.NOT_A_SERVER_ERROR);
                break;
            case "ConnectAckMessage":
                ConnectAckMessage cackmesg = (ConnectAckMessage)message;
                this.logger.printf("Connection to peer acknowledged (peer client version: %s)%n", cackmesg.version);
                this.send(new GetBlocksMessage(new Hash[] {Node.GENESIS.getHash()}, null));
                break;
            case "ErrorMessage":
                this.logger.printf("%n%s%n", Node.ERROR_DELIMITER);
                this.logger.println("ERROR FROM PEER:");
                this.logger.println(((ErrorMessage)message).message);
                this.logger.printf("%s%n%n", Node.ERROR_DELIMITER);
                break;
            case "NotFoundMessage":
                NotFoundMessage nfmesg = (NotFoundMessage)message;
                this.logger.println("Requested data not found:");
                for (final InventoryItem i : nfmesg.items) {
                    this.logger.printf("\t%s (%s)%n", (i.type == InventoryItem.BLOCK_TYPE ? "Block" : "Transaction"),
                            i.hash);
                }
                break;
            case "PingMessage":
                this.logger.println("PING!");
                this.send(Node.PONG);
                break;
            case "PongMessage":
                this.logger.println("PONG!");
                if (this.disconnector != null) {
                    this.disconnector.cancel();
                    this.disconnector = null;
                }
                break;
            case "TransactionMessage":
                Transaction transaction = ((TransactionMessage)message).transaction;
                this.logger.println("Received a transaction: " + transaction.getHash());
                break;
            default:
                this.delegateToManager(message);
        }
    }

    /**
     * Receives a {@link Message} from this {@code Node}'s peer. Blocks until a
     * {@code Message} is available.
     * 
     * 
     * @throws IOException If this {@code Node} receives an object that's not a
     *         {@link Message}.Block;
     * 
     * @return The next {@code Message} received.
     */
    private Message receive() throws IOException {
        Object o = null;
        try {
            this.state = NodeState.WAITING;
            o = this.messageIn.readObject();
            if (o instanceof Message) {
                // If we actually got a message...
                return (Message)o;
            }
            else if (o == null) {
                return null;
            }
            else {
                throw new Exception();
            }
        }
        catch (Exception e) {
            String mesg = "Expected a valid message, received " + o;
            this.logger.println(mesg);
            throw new IOException(mesg);
        }
    }

    private boolean validateTransaction(final Transaction transaction) {
        if (transaction == null) {
            // If we never actually got a transaction...
            this.logger.println("\t\tExpected a valid transaction, received nothing");
            return false;
        }

        if (transaction.outputs == null || transaction.outputs.length <= 0
                || transaction.outputs.length > Node.PARAMS.TRANSACTION_MAX_OUTPUTS) {
            // If this transaction has no outputs or too many outputs...
            this.logger.printf("\t\tExpected between 1 and %d outputs, got %d%n", PARAMS.TRANSACTION_MAX_OUTPUTS,
                    transaction.outputs.length);
            return false;
        }

        if (transaction.inputs == null || (transaction.inputs.length < 0
                || transaction.inputs.length > Node.PARAMS.TRANSACTION_MAX_INPUTS)) {
            // If this transaction has negative inputs or too many inputs...
            this.logger.println("\t\tThis transaction has an invalid number of inputs");
            return false;
        }

        if (transaction.inputs.length == 0 && !(transaction instanceof CoinbaseTransaction)) {
            // If this transaction has no inputs, but isn't a
            // CoinbaseTransaction...
            logger.println("\t\tTransaction has no inputs but is not a CoinbaseTransaction");
            return false;
        }

        if (!transaction.verifyInputSignatures()) {
         // If this transaction's inputs weren't signed...
            logger.println("\t\tTransaction's inputs were not signed (attempted forgery?)");
            return false;
        
        }

        for (final Transaction$Output i : transaction.outputs) {
            // For each transaction output...
            if (i.amount == null || i.amount.longValue() < 0) {
                // If this transaction's output value makes no sense...
                logger.printf("\t\t\tExpected a transaction of positive value, got %s%n", i.amount);
                return false;
            }
            
            if (i.address == null) {
                logger.println("\t\t\tTransaction output doesn't specify a recipient");
            }
        }
        return true;
    }

    private class PingerTask extends TimerTask {
        @Override
        public void run() {
            Node.this.logger.println("Pinging peer");
            Node.this.send(Node.PING);
            Node.this.disconnector = new Timer(Node.this.peer + " disconnector");
            Node.this.disconnector.schedule(new TimerTask() {
                @Override
                public void run() {
                    // If the connection is fine, this should be cancelled in
                    // Node.handleMessage (case PongMessage).
                    Node.this.logger.println("Peer not responsive, disconnecting");
                    Node.this.close();
                }
            }, Node.PARAMS.PEER_TIMEOUT);
        }
    }
}
