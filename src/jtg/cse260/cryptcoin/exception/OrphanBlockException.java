package jtg.cse260.cryptcoin.exception;

import com.starkeffect.cryptcoin.protocol.Block;
import com.starkeffect.cryptcoin.protocol.Hash;

import jtg.cse260.cryptcoin.block.BlockTree;

/**
 * Thrown when the {@link BlockTree} attempts to add a {@link Block}, but no
 * parent with a hash equivalent to {@link Block#prevHash} exists.
 * 
 * @author jesse
 */
@SuppressWarnings("serial")
public class OrphanBlockException extends Exception {

    /**
     * @param message
     */
    public OrphanBlockException(String message) {
        super(message);
    }
    
}
