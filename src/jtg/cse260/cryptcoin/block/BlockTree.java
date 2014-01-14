package jtg.cse260.cryptcoin.block;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jtg.cse260.cryptcoin.Main;
import jtg.cse260.cryptcoin.node.Node;
import jtg.util.Singleton;
import jtg.util.SingletonSaver;

import com.starkeffect.cryptcoin.protocol.Block;
import com.starkeffect.cryptcoin.protocol.CoinAddress;
import com.starkeffect.cryptcoin.protocol.CoinAmount;
import com.starkeffect.cryptcoin.protocol.CoinbaseTransaction;
import com.starkeffect.cryptcoin.protocol.Hash;
import com.starkeffect.cryptcoin.protocol.Parameters;
import com.starkeffect.cryptcoin.protocol.Transaction;
import com.starkeffect.cryptcoin.protocol.Transaction$Input;
import com.starkeffect.cryptcoin.protocol.Transaction$Output;

public class BlockTree implements Serializable, Singleton<BlockTree>
{
    /**
     * The ID of the serialized object
     */
    private static final long                          serialVersionUID = -3243581832334722464L;

    public static transient final File                 FILE             = new File(Main.DIRECTORY, "blox.wtf");

    private static transient Parameters                PARAMS           = Parameters.getInstance();

    private static transient Block                     GENESIS          = BlockTree.PARAMS.getGenesisBlock();

    private static transient SingletonSaver<BlockTree> saver;

    private Map<Hash, Block>                           blocks;

    private Map<Hash, Transaction>                     transactions;

    private Map<Block, Integer>                        blockHeights;

    private Block                                      mainHead;

    private int                                        mainHeight;

    private static BlockTree                           instance;

    static {
        BlockTree.saver = new SingletonSaver<>(BlockTree.FILE);
        BlockTree.instance = BlockTree.saver.load();
        if (BlockTree.instance == null) {
            BlockTree.instance = new BlockTree();
        }
        BlockTree.saver.saveOnExit(BlockTree.instance);
    }

    public static BlockTree getInstance() {
        return BlockTree.instance;
    }

    private BlockTree() {
        this.blocks = new ConcurrentHashMap<>();
        // We'll be iterating over the set of blocks while adding to it
        this.transactions = new ConcurrentHashMap<>();
        // Transactions, too
        this.blockHeights = new ConcurrentHashMap<>();

        this.blockHeights.put(GENESIS, 0);
        this.blocks.put(BlockTree.GENESIS.getHash(), BlockTree.GENESIS);
        this.mainHead = GENESIS;
        this.mainHeight = 0;
    }

    /**
     * Adds a {@link Block} to the tree.
     * 
     * @O 1
     * @param block The block to add
     * @return True if the block was added successfully
     */
    public synchronized boolean addBlock(final Block block) {
        if (this.blocks.containsKey(block.getHash())) {
            // If this block is already in the tree...
            return false;
        }

        if (!this.validateBlock(block)) {
            // If this block didn't validate...
            return false;
        }

        for (Transaction i : block.transactions) {
            this.transactions.put(i.getHash(), i);
        }

        this.blocks.put(block.getHash(), block);
        return true;
    }

    /**
     * Returns the balance of a given address.
     * 
     * @param address The address whose balance we're querying.
     * @O n (where n is the number of {@link Transaction}s stored)
     * @return The balance of the given address as a {@link CoinAmount}.
     */
    public CoinAmount getBalance(final CoinAddress address) {
        long amount = 0;

        // Build the outputs from this address first
        for (Transaction i : this.transactions.values()) {
            for (Transaction$Output j : i.outputs) {
                if (j.address.equals(address)) {
                    // If transaction i gave money to the given address...
                    amount += j.amount.longValue();
                }
            }

            if (i.inputs.length > 0) {
                // If this isn't a CoinBaseTransaction...
                for (Transaction$Input k : i.inputs) {
                    Transaction source = this.getTransaction(k.providerHash);
                    if (source != null) {
                        // If we have on record the source of the current
                        // input...
                        if (source.outputs[k.providerIndex].address.equals(address)) {
                            // If the source transaction gave money to the given
                            // address...
                            amount -= source.outputs[k.providerIndex].amount.longValue();
                        }
                    }
                }
            }
        }

        assert amount >= 0 : "Expected a nonnegative integer, got " + amount;

        return new CoinAmount(amount);
    }

    /**
     * Returns a {@link Block}, given its {@link Hash}, if it exists. Returns
     * null if not.
     * 
     * @param hash The hash of the {@code Block} desired
     * @return The {@code Block} if found, null if not
     */
    public Block getBlock(final Hash hash) {
        return this.blocks.get(hash);
    }

    /**
     * Given a {@link Transaction$Input}, return the {@link Transaction} that
     * contains it.
     * 
     * @O n (where n is the total amount of {@code Transaction$Input}s recorded)
     * @param input The {@code Transaction.Input} we're looking for the parent
     *        {@code Transaction} of.
     * @return The owning {@code Transaction} if found, {@code null} if not.
     */
    public Transaction getContainingTransaction(final Transaction$Input input) {
        for (Transaction i : this.transactions.values()) {
            if (i.inputs != null) {
                for (Transaction$Input j : i.inputs) {
                    if (j == input) { return i; }
                }
            }
        }

        return null;
    }

    /**
     * Given a {@link Transaction$Output}, return the {@link Transaction} that
     * contains it.
     * 
     * @O n (where n is the total amount of {@code Transaction$Output}s
     *    recorded)
     * @param output The {@code Transaction.Output} we're looking for the parent
     *        {@code Transaction} of.
     * @return The owning {@code Transaction} if found, {@code null} if not.
     */
    public Transaction getContainingTransaction(final Transaction$Output output) {
        for (Transaction i : this.transactions.values()) {
            // For each transaction we have a record of...
            for (Transaction$Output j : i.outputs) {
                // ...and for each output that transaction specifies...
                if (j == output) {
                    // If the current transaction has the given output...
                    return i;
                }
            }
        }

        return null;
    }

    /**
     * Given a transaction, returns the block that contains it (or null if not
     * found)
     * 
     * @O n
     * @param transaction
     * @return
     */
    public Block getContainingBlock(final Transaction transaction) {
        for (Block i : this.blocks.values()) {
            for (Transaction j : i.transactions) {
                if (transaction.getHash().equals(j.getHash())) { return i; }
            }
        }

        return null;
    }

    /**
     * Returns whatever {@link Block} is at the head of the main chain.
     * 
     * @return The head of the main chain
     */
    public Block getMainHead() {
        return this.mainHead;
    }

    /**
     * Finds all the blocks whose parents we do not have. No guarantees are made
     * about the order of the returned array.
     * 
     * @O n (where n is the number of blocks we have)
     * @return An array containing the blocks whose parents we don't have
     */
    public Block[] getOrphans() {
        Collection<Block> orphans = new HashSet<>();

        for (Block i : this.blocks.values()) {
            Block parent = this.getBlock(i.prevHash);
            if (parent == null) {
                orphans.add(i);
            }
        }

        return orphans.toArray(new Block[orphans.size()]);
    }

    public Transaction$Output[] getOutputsTo(final CoinAddress address) {
        Collection<Transaction$Output> outputsTo = new HashSet<>();

        for (Transaction i : this.transactions.values()) {
            // For all transactions we have...
            for (Transaction$Output j : i.outputs) {
                // For all outputs in said transaction...
                if (j.address.equals(address)) {
                    // If we find an output to the given address...
                    outputsTo.add(j);
                }
            }
        }

        return outputsTo.toArray(new Transaction$Output[outputsTo.size()]);
    }

    public Payment[] getPaymentsFrom(final CoinAddress address) {
        Collection<Payment> paymentsFrom = new HashSet<>();

        for (Block i : this.blocks.values()) {
            // For each block we have available...
            for (Transaction j : i.transactions) {
                // For each transaction in the current block...
                for (Transaction$Input k : j.inputs) {
                    // For each input to the current transaction...

                    if (address.equals(new CoinAddress(k.publicKey, ""))) {
                        // If it turns out we made this transaction...
                        for (Transaction$Output m : j.outputs) {
                            // Then for each output in the current
                            // transaction...
                            paymentsFrom.add(new Payment(i, j, m.address, address, m.amount));
                        }
                        break;
                    }
                }
            }
        }

        return paymentsFrom.toArray(new Payment[paymentsFrom.size()]);
    }

    /**
     * @O n (n is the number of blocks available)
     * @param address
     * @return
     */
    public Payment[] getPaymentsTo(final CoinAddress address) {
        Collection<Payment> paymentsTo = new HashSet<>();

        for (Block i : this.blocks.values()) {
            // For each block we have available...
            for (Transaction j : i.transactions) {
                // For each transaction in the current block...
                for (Transaction$Output k : j.outputs) {
                    // For each output in the current transaction...

                    if (k.address.equals(address)) {

                        // j is the transaction that paid the given address
                        // t is the transaction that paid j
                        Transaction t = this.getTransaction(j.inputs[0].providerHash);
                        if (t != null) {
                            paymentsTo
                                    .add(new Payment(i, t, k.address, t.outputs[j.inputs[0].providerIndex].address,
                                            k.amount));
                        }
                    }
                }
            }
        }

        return paymentsTo.toArray(new Payment[paymentsTo.size()]);
    }

    /**
     * Given a {@link Hash}, return the {@link Transaction} it represents, or
     * {@code null} if it wasn't found.
     * 
     * @O 1
     * @param hash The {@code Hash} of the {@code Transaction} to find
     * @return The desired {@code Transaction} if found, {@code null} if not
     */
    public Transaction getTransaction(final Hash hash) {
        return this.transactions.get(hash);
    }

    /**
     * Returns the exact outputs to the given address that have not been spent.
     * 
     * @param address
     * @return
     */
    public Transaction$Output[] getUnspentOutputs(final CoinAddress address) {
        Collection<Transaction$Output> outputsTo = new ArrayList<>();
        // The given address's income

        Collection<Transaction$Output> inputsFrom = new ArrayList<>();
        // The given address's expenditures

        for (Transaction i : this.transactions.values()) {
            // For each transaction on record...
            for (Transaction$Output j : i.outputs) {
                // For each output of this transaction...
                if (j.address.equals(address)) {
                    // If the current transaction paid money to the given
                    // address...
                    outputsTo.add(j);
                }
            }

            for (Transaction$Input k : i.inputs) {
                // For each money source of the current transaction...

                if (new CoinAddress(k.publicKey, "").equals(address)) {
                    Transaction source = this.getTransaction(k.providerHash);
                    inputsFrom.add(source.outputs[k.providerIndex]);
                }
            }
        }

        outputsTo.removeAll(inputsFrom);

        return outputsTo.toArray(new Transaction$Output[outputsTo.size()]);
    }

    /**
     * Returns true if the transaction behind the given hash is effectively
     * permanent.
     * 
     * @param hash
     * @return
     */
    public boolean isTransactionConfirmed(final Hash hash) {
        return false;
    }

    public boolean isTransactionConfirmed(final Transaction transaction) {
        return this.isTransactionConfirmed(transaction.getHash());
    }

    /**
     * Given a {@link Block}, return its height.
     * 
     * @param block The block whose height to compute
     * @O n (n is the height of the main chain)
     * @return The height of the given block in the main chain, or -1 if it's
     *         not in the main chain.
     */
    public int getBlockHeight(final Block block) {
        if (block == null) return -1;
        if (block.getHash().equals(GENESIS.getHash())) return 0;
        if (this.blockHeights.containsKey(block)) { return this.blockHeights.get(block); }

        Block current = block;
        int len = this.blocks.size();
        len += len / 2;
        for (int i = 0; i < len; ++i) {
            if (current == null) {
                // If we've come across a severed fork...
                return -1;
            }

            if (current.getHash().equals(GENESIS.getHash())) {
                // If we've reached the root successfully...
                this.blockHeights.put(block, i);
                return i;
            }

            current = this.getBlock(current.prevHash);
        }

        return -1;
    }

    /**
     * Given a block, attempts to declare it as the head of the main chain. Adds
     * it, too, if necessary.
     * 
     * If we cannot iterate from the given block's {@code prevHash} all the way
     * to the genesis block, this can't be the head of the block chain.
     * 
     * @O n (n is the number of blocks on record)
     * @param block The block to set as the head of the block chain
     * @return True if it was set as the head as a result of this method
     */
    public synchronized boolean setHead(final Block block) {
        if (block == null) return false;
        if (block.getHash().equals(this.mainHead.getHash())) return false;
        this.blocks.put(block.getHash(), block);

        for (Block i : this.blocks.values()) {
            // For each block we have...
            if (i.prevHash.equals(block.getHash())) return false;
            // If the current block refers to the given block as a parent...
        }

        Block current = block;
        int len = this.blocks.size();
        len += len / 2;
        // ^^^ ConcurrentHashMaps don't compute size in O(1)
        for (int i = 0; i < len; ++i) {
            // Just to guarantee this method terminates; if we have to iterate
            // more times than there are blocks, something's wrong.
            if (current == null) {
                // If we can't reach the root from the given block...
                return false;
            }
            if (current.getHash().equals(GENESIS.getHash())) {
                // If we've reached the root successfully...
                if (i <= this.mainHeight) {
                    // If we reached the root in less iterations than the head
                    // of the main chain...

                    return false;
                    // ...then this must be another fork.
                }
                else {
                    this.mainHead = block;
                    this.mainHeight = i;
                    return true;
                }
            }
            current = this.getBlock(current.prevHash);
        }

        return false;
    }

    /**
     * Validates the parts of a {@link Block} that depends on the state of the
     * block chain. Assumes that the given block was already validated by the
     * {@link Node} that received it.
     * 
     * @param block The {@code Block} to validate
     * @O 1
     * @return True if {@code block} is valid
     */
    private boolean validateBlock(final Block block) {
        for (Transaction i : block.transactions) {
            if (!this.validateTransaction(block, i)) { return false; }
        }

        int height = this.getBlockHeight(block);
        if (0 <= height && height <= PARAMS.DIFFICULTY_CHANGE_INTERVAL && block.difficulty != PARAMS.INITIAL_DIFFICULTY) {
            // If we can compute the height of a block, but its initial
            // difficulty is invalid...
            return false;
        }

        if (height > PARAMS.DIFFICULTY_CHANGE_INTERVAL) {
            // If we're beyond the first few blocks
            Block prev = this.getBlock(block.prevHash);
            if (height % PARAMS.DIFFICULTY_CHANGE_INTERVAL != 1 && block.difficulty != prev.difficulty) {
                // If we're not ready to change difficulty, but the parent's
                // difficult isn't the same as the current one...
                return false;
            }
        }

        return true;
    }

    /**
     * Validates the parts of the transaction that depend on the state of the
     * block chain.
     * 
     * @param block
     * @param transaction
     * @return
     */
    private boolean validateTransaction(final Block block, final Transaction transaction) {
        if (this.transactions.containsKey(transaction.getHash())) {
            // If we already have the given transaction...
            return false;
        }
        return true;
    }
}
