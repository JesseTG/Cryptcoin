package jtg.cse260.cryptcoin.block;

import java.util.Date;

import com.starkeffect.cryptcoin.protocol.Block;
import com.starkeffect.cryptcoin.protocol.CoinAddress;
import com.starkeffect.cryptcoin.protocol.CoinAmount;
import com.starkeffect.cryptcoin.protocol.Timestamp;
import com.starkeffect.cryptcoin.protocol.Transaction;

/**
 * Represents a payment of a {@link CoinAmount}. Not to be stored; meant to be
 * used in the GUIs.
 */
public class Payment implements Comparable<Payment> {
    private CoinAmount  amount;
    private CoinAddress to;
    private CoinAddress from;
    private Block       block;
    private Transaction transaction;

    public Payment(Block block, Transaction transaction, CoinAddress to, CoinAddress from, CoinAmount amount) {
        this.block = block;
        this.to = to;
        this.from = from;
        this.amount = amount;
        this.transaction = transaction;
    }

    /**
     * Returns the time this payment was made official.
     * 
     * @return
     */
    public Date getTime() {
        return new Date(this.block.timestamp.getTime());
    }

    public CoinAmount getAmount() {
        return this.amount;
    }

    public CoinAddress getSender() {
        return this.from;
    }

    public CoinAddress getRecipient() {
        return this.to;
    }

    public Block getBlock() {
        return this.block;
    }
    
    public Transaction getTransaction() {
        return this.transaction;
    }

    @Override
    public int compareTo(Payment o) {
        return Long.compare(this.block.timestamp.getTime(), o.block.timestamp.getTime());
    }
}
