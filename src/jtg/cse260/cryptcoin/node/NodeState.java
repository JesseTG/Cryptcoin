package jtg.cse260.cryptcoin.node;

import com.starkeffect.cryptcoin.protocol.Message;

/**
 * The states a {@link Node} can be in.
 * 
 * @author jesse
 */
public enum NodeState
{
    /**
     * This {@code Node} has not yet attempted to connect to a peer.
     */
    IDLE,
    
    /**
     * This {@code Node} is in the process of connecting to a peer.
     */
    CONNECTING,
    
    /**
     * This {@code Node} is awaiting a {@link Message} from a peer.
     */
    WAITING,
    
    /**
     * This {@code Node} is sending a {@link Message} to a peer.
     */
    SENDING,
    
    /**
     * This {@code Node} is no longer connected to a peer, and can safely be disposed of.
     */
    CLOSED,
}