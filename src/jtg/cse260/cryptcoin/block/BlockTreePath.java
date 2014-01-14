package jtg.cse260.cryptcoin.block;

import jtg.cse260.cryptcoin.block.BlockTreeVertex;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;

import com.starkeffect.cryptcoin.protocol.Block;
import com.starkeffect.cryptcoin.protocol.Hash;
import com.starkeffect.cryptcoin.protocol.InventoryItem;
import com.starkeffect.cryptcoin.protocol.Transaction;

/**
 * A path of BlockTreeVertices. Guaranteed to be a subgraph of the BlockTree.
 * 
 * @author jesse
 */
public class BlockTreePath extends AbstractList<BlockTreeVertex>
{
    private BlockTreeVertex[] vertices;

    public BlockTreePath() {

    }

    /**
     * Gets a {@link BlockTreeVertex} on this BlockTreePath.
     * 
     * @param index The number of the {@link BlockTreeVertex} we want, with 0
     *            being the earliest.
     * @return BlockTreeVertex
     */
    public BlockTreeVertex get(final int index) {
        return this.vertices[index];
    }

    /**
     * Returns the Hash of each element in this BlockTreePath, with each index
     * in the new array corresponding to the block of origin (e.g. the first
     * Hash represents the first block, the second the second, etc.).
     * 
     * @return Hash[]
     */
    public Hash[] getHashes() {
        Hash[] hashes = new Hash[this.vertices.length];
        for (int i = 0; i < this.vertices.length; ++i) {
            hashes[i] = this.vertices[i].getBlock().getHash();
        }
        return hashes;
    }

    /**
     * Returns an array of the individual Blocks within this BlockTreePath.
     * 
     * @return Block[]
     */
    public Block[] getBlocks() {
        Block[] blocks = new Block[this.vertices.length];
        for (int i = 0; i < this.vertices.length; ++i) {
            blocks[i] = this.vertices[i].getBlock();
        }
        return blocks;
    }

    /**
     * Returns InventoryItems that correspond to each Block and Transaction
     * within this BlockTreePath. The returned array is ordered as follows; the
     * first InventoryItem is a Block, then the next few InventoryItems are the
     * {@link Transaction}s held within said Block. Then comes the next Block,
     * then its Transactions, etc. Rinse and repeat.
     * 
     * @return InventoryItem[]
     */
    public InventoryItem[] getInventoryData() {
        int len = 0;
        for (final BlockTreeVertex i : this.vertices) {
            len += i.getBlock().transactions.length + 1;
        }
        
        ArrayList<InventoryItem> items = new ArrayList<InventoryItem>(len);
        
        for (int i = 0; i < this.vertices.length; ++i) {
            Block b = this.vertices[i].getBlock();
            items.add(new InventoryItem(InventoryItem.BLOCK_TYPE, b.getHash()));
            
            for (final Transaction t : b.transactions) { 
                items.add(new InventoryItem(InventoryItem.TRANSACTION_TYPE, t.getHash()));
            }
        }
        
        return items.toArray(new InventoryItem[items.size()]);
    }

    @Override
    public int size() {
        return this.vertices.length;
    }
}
