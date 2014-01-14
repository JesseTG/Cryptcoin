package com.starkeffect.cryptcoin.demo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import com.starkeffect.cryptcoin.protocol.Block;
import com.starkeffect.cryptcoin.protocol.ConnectAckMessage;
import com.starkeffect.cryptcoin.protocol.ConnectMessage;
import com.starkeffect.cryptcoin.protocol.GetBlocksMessage;
import com.starkeffect.cryptcoin.protocol.Hash;
import com.starkeffect.cryptcoin.protocol.InventoryItem;
import com.starkeffect.cryptcoin.protocol.InventoryMessage;
import com.starkeffect.cryptcoin.protocol.Message;
import com.starkeffect.cryptcoin.protocol.NetworkAddress;
import com.starkeffect.cryptcoin.protocol.Parameters;
import com.starkeffect.cryptcoin.protocol.PingMessage;
import com.starkeffect.cryptcoin.protocol.PongMessage;

/**
 * This class contains a skeleton implementation of a Cryptcoin client
 * application. When the application is started, it makes a network connection
 * to a well-known server node, initiates downloading of the blockchain, and
 * responds to periodic pings from the server.
 * 
 * @author E. Stark
 * @version 20130802
 */

public class DemoNode implements Runnable {

    /** String identifying the author and version of this client. */
    private static final String VERSION = "Skeletal Cryptcoin client: E. Stark, 20130802";

    /**
     * Main method for the demonstration application.
     * 
     * @param args
     */
    public static void main(final String[] args) {
        new DemoNode();
    }

    /** Protocol parameters. */
    private final Parameters   parameters;

    /** Socket for communicating with server. */
    private Socket             serverSocket;

    /** OutputStream for sending messages to the server. */
    private ObjectOutputStream outStream;

    /** InputStream for receiving messages from the server. */
    private ObjectInputStream  inStream;

    /** "Genesis block": the well-known first block in the block chain. */
    private final Block        genesisBlock;

    /**
     * Initialize this node.
     */
    public DemoNode() {
        this.parameters = Parameters.getInstance();
        this.genesisBlock = this.parameters.getGenesisBlock();
        this.connectToServer();
    }

    /**
     * Close the connection to the server.
     */
    private void close() {
        try {
            this.serverSocket.close();
        }
        catch (IOException e) {
            // Ignore it
        }
    }

    /**
     * Initiate a connection to a well-known server node.
     */
    private void connectToServer() {
        List<NetworkAddress> list = this.parameters.getWellKnownNodes();
        for (NetworkAddress addr : list) {
            System.out.println("Attempting to connect to server at: " + addr);
            try {
                this.serverSocket = addr.connect();
                new Thread(this, "Connection Handler").start();
                return;
            }
            catch (Exception x) {
                System.out.println("Connection failed: " + x.getMessage());
                continue;
            }
        }
        throw new RuntimeException("All connection attempts failed");
    }

    /**
     * Handle a message received from the server.
     * 
     * @param msg
     *            The message received.
     */
    private void handleMessage(final Message msg) {
        if (msg instanceof ConnectAckMessage) {
            ConnectAckMessage amsg = (ConnectAckMessage) msg;
            System.out.println("Connection acknowledged");
            System.out.println("Remote software version: " + amsg.version);
            // Ask the server to send an inventory of all blocks in the
            // block chain, starting from the genesis block.
            this.send(new GetBlocksMessage(new Hash[] {this.genesisBlock.getHash()}, null));
        }
        else if (msg instanceof PingMessage) {
            // A response is required, otherwise the server will close the
            // connection.
            System.out.println("Received ping, sending pong");
            this.send(new PongMessage());
        }
        else if (msg instanceof InventoryMessage) {
            System.out.println("Received inventory:");
            InventoryMessage imsg = (InventoryMessage) msg;
            for (InventoryItem item : imsg.items) {
                if (item.type == InventoryItem.BLOCK_TYPE) {
                    System.out.println("Block: " + item.hash);
                }
                else if (item.type == InventoryItem.TRANSACTION_TYPE) {
                    System.out.println("Transaction: " + item.hash);
                }
            }
        }
        else {
            System.out.println("Received message: " + msg);
        }
    }

    /**
     * Wait for a message to arrive from the server and return it.
     */
    private Message recv() throws IOException, ClassNotFoundException {
        if (this.inStream == null) {
            this.inStream = new ObjectInputStream(this.serverSocket.getInputStream());
        }
        Object obj = this.inStream.readObject();
        if (obj instanceof Message) {
            return (Message) obj;
        }
        else {
            throw new IOException("Object received is not a message");
        }
    }

    /**
     * Main loop to handle the server connection.
     */
    @Override
    public void run() {
        try {
            System.out.println("Connection handler starting");
            this.send(new ConnectMessage(DemoNode.VERSION, 0));
            while (true) {
                Message msg = this.recv();
                this.handleMessage(msg);
            }
        }
        catch (Exception x) {
            System.out.println("I/O exception in receive: " + x.getMessage());
        }
        finally {
            System.out.println("Connection handler terminating");
            this.close();
        }
    }

    /**
     * Send a message to the server.
     * 
     * @param msg
     *            The message to be sent.
     */
    private void send(final Message msg) {
        try {
            if (this.outStream == null) {
                this.outStream = new ObjectOutputStream(this.serverSocket.getOutputStream());
            }
            this.outStream.writeObject(msg);
            this.outStream.flush();
        }
        catch (IOException x) {
            System.out.println("I/O exception in send: " + x.getMessage());
            this.close();
        }
    }

}
