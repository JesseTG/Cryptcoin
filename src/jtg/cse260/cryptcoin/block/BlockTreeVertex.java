package jtg.cse260.cryptcoin.block;

import jtg.cse260.cryptcoin.block.BlockTreeVertex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.starkeffect.cryptcoin.protocol.Block;

/**
 * Represents an entry in the block tree. Encapsulates information about a block
 * so that we can cache it later.
 * 
 * @author jesse
 * 
 */
public class BlockTreeVertex implements Serializable
{
    private static final long serialVersionUID = 5332217401652111L;

    private Block             block;

    BlockTreeVertex(Block block) {
        this.block = block;
    }

    /**
     * Returns the {@link Block} owned by this {@code BlockTreeVertex}.
     * 
     * @return This vertex's {@code Block}.
     */
    public Block getBlock() {
        return this.block;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (this.getClass() != other.getClass()) return false;
        if (other == this) return true;
        
        return this.block.equals(((BlockTreeVertex)other).block);
    }

    @Override
    public int hashCode() {
        return this.block.hashCode();
    }
}
