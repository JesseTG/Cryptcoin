package jtg.cse260.cryptcoin.node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.starkeffect.cryptcoin.protocol.Block;
import com.starkeffect.cryptcoin.protocol.CoinAddress;
import com.starkeffect.cryptcoin.protocol.CoinAmount;
import com.starkeffect.cryptcoin.protocol.Message;
import com.starkeffect.cryptcoin.protocol.NetworkAddress;

import jtg.cse260.cryptcoin.block.BlockTree;
import jtg.cse260.cryptcoin.node.Node;

/**
 * Represents a {@link Node} that stores information about the {@link BlockTree}
 * rather than creating new Cryptcoins.
 * 
 * @author jesse
 */
public class WalletNode extends Node
{
    /**
     * The address one would enter in order to credit this wallet.
     */
    private CoinAddress address;

    /**
     * The Cryptcoins available to spend in this wallet.
     */
    private CoinAmount  balance;

    private String      name;

    public WalletNode(NetworkAddress peer) {
        this(peer, peer.toString());
    }

    public WalletNode(NetworkAddress peer, String name) {
        super(peer);
        this.name = name;
    }

    /**
     * Returns the address of this wallet.
     * 
     * @return The {@link CoinAddress} that accepts money into this wallet.
     */
    public CoinAddress getCoinAddress() {
        return address;
    }

    /**
     * The amount of money available for use by this wallet.
     * 
     * @return The {@link CoinAmount} owned by this wallet.
     */
    public CoinAmount getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
