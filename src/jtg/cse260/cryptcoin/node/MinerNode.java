package jtg.cse260.cryptcoin.node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.starkeffect.cryptcoin.protocol.Block;
import com.starkeffect.cryptcoin.protocol.Message;
import com.starkeffect.cryptcoin.protocol.NetworkAddress;

import jtg.cse260.cryptcoin.node.Node;

public class MinerNode extends Node
{
    
    private String name;
    public MinerNode(NetworkAddress peer) {
        super(peer);
        this.name = peer.toString();
    }
    
    /**
     * Undertakes the computation necessary to mine a block. Very intensive,
     * hence MinerNode's extension of {@link Thread}.
     * 
     * @return Block
     */
    public Block mineBlock() {
        return null;
    }
}
