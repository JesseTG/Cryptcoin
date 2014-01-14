package jtg.cse260.cryptcoin.node;

import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jtg.cse260.cryptcoin.block.BlockTree;
import jtg.cse260.cryptcoin.block.Payment;
import jtg.cse260.cryptcoin.gui.tabs.CCTableModel;

import com.starkeffect.cryptcoin.protocol.CoinAddress;
import com.starkeffect.cryptcoin.protocol.CoinAmount;
import com.starkeffect.cryptcoin.protocol.Parameters;
import com.starkeffect.cryptcoin.protocol.Transaction;
import com.starkeffect.cryptcoin.protocol.Transaction$Output;
import com.starkeffect.cryptcoin.protocol.Transaction$Input;
import com.starkeffect.util.KeyUtilities;

/**
 * A representation of a Cryptcoin wallet distinct from a {@link WalletNode}.
 * Use this in the GUI's list model instead of {@code WalletNode}, otherwise the
 * number of wallets is constrained by the number of connections.
 * 
 * @author jesse
 */
public class Wallet implements Serializable {
    private static final long                 serialVersionUID = 6280929523383575144L;
    private static transient final Parameters PARAMS           = Parameters.getInstance();
    public static transient final String[]    COL_NAMES_FROM   = {"OK", "Date", "Amount Received", "From"};
    public static transient final String[]    COL_NAMES_TO     = {"OK", "Date", "Amount Paid", "To"};

    private CoinAddress                       address;
    private transient CCTableModel            incomeModel;
    private transient CCTableModel            expendituresModel;
    private CoinAmount                        cachedBalance;
    private KeyPair                           keys;
    private String                            description;

    /**
     * Constructs a {@code Wallet} with the given name and key pair, and with no
     * description.
     * 
     * @param name
     * @param keys
     */
    public Wallet(String name, KeyPair keys) {
        this(name, "", keys);
    }

    /**
     * Constructs a {@code Wallet} with the given name, description, and key
     * pair.
     * 
     * @param name
     * @param description
     * @param keys
     */
    public Wallet(String name, String description, KeyPair keys) {
        this.address = new CoinAddress(keys.getPublic(), name);
        this.keys = keys;
        this.incomeModel = new CCTableModel(COL_NAMES_FROM);
        this.expendituresModel = new CCTableModel(COL_NAMES_TO);
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.getName(), this.getBalance());
    }

    public CoinAmount getBalance() {
        if (this.cachedBalance == null) {
            this.cachedBalance = BlockTree.getInstance().getBalance(this.address);
        }
        return this.cachedBalance;
    }

    public String getName() {
        return this.address.getOwnerID();
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PublicKey getPublicKey() {
        return this.keys.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.keys.getPrivate();
    }

    public Transaction createTransaction(CoinAddress recipient, long amount) {
        return this.createTransaction(recipient, new CoinAmount(amount));
    }

    public Transaction createTransaction(CoinAddress recipient, Transaction$Output[] outputs) {
        Transaction transaction = new Transaction();

        List<Transaction$Input> ins = new ArrayList<>();
        List<Transaction$Output> outs = new ArrayList<>(2);
        // Probably not too many outputs

        long total = 0;
        int len = Math.min(outputs.length, PARAMS.TRANSACTION_MAX_OUTPUTS);
        for (int i = 0; i < len; ++i) {
            // For each unspent output...
            total += outputs[i].amount.longValue();
            Transaction$Input in = this.outputToInput(outputs[i]);
            if (in == null) return null;
            ins.add(in);
        }

        Transaction$Output out = new Transaction$Output();
        out.address = recipient;
        out.amount = new CoinAmount(total);
        outs.add(out);

        transaction.inputs = ins.toArray(new Transaction$Input[ins.size()]);
        transaction.outputs = outs.toArray(new Transaction$Output[outs.size()]);

        PrivateKey[] pkeys = new PrivateKey[transaction.inputs.length];
        Arrays.fill(pkeys, this.keys.getPrivate());
        transaction.signInputs(pkeys);
        return transaction.verifyInputSignatures() ? transaction : null;
    }

    /**
     * Creates and returns a {@link Transaction} to the given addresses. The
     * {@code Wallet} may add its own {@link CoinAddress} for the purposes of
     * collecting change. If this {@code Wallet} can't afford the outputs,
     * returns {@code null}.
     * 
     * This method does not modify the {@link BlockTree}, it only creates a
     * {@code Transaction} that will eventually be added to it.
     * 
     * @param recipient
     * 
     * @param amount
     * 
     * @return A {@code Transaction} if there's sufficient money, {@code null}
     *         if not.
     * @O n
     */
    public Transaction createTransaction(CoinAddress recipient, CoinAmount amount) {
        Transaction$Output[] outputs = BlockTree.getInstance().getUnspentOutputs(this.address);

        // Sort the outputs by the CoinAmounts (to try and minimize the change)
        Arrays.sort(outputs, new Comparator<Transaction$Output>() {
            @Override
            public int compare(Transaction$Output o1, Transaction$Output o2) {
                return o1.amount.compareTo(o2.amount);
            }
        });

        Transaction transaction = new Transaction();
        List<Transaction$Input> ins = new ArrayList<>();
        List<Transaction$Output> outs = new ArrayList<>(2); // Probably not too
                                                            // many outputs

        long total = 0;
        int len = Math.min(outputs.length, PARAMS.TRANSACTION_MAX_OUTPUTS);
        for (int i = 0; i < len && total < amount.longValue(); ++i) {
            // For each unspent output, or at least until we find enough unspent
            // outputs
            total += outputs[i].amount.longValue();
            Transaction$Input in = this.outputToInput(outputs[i]);
            if (in == null) return null;
            ins.add(in);
        }

        if (total < amount.longValue()) return null;
        // If we couldn't find enough unspent outputs...

        Transaction$Output out = new Transaction$Output();
        out.address = recipient;
        out.amount = amount;
        outs.add(out);

        if (total > amount.longValue()) {
            // If we'll have to make some change...
            CoinAmount difference = new CoinAmount(total - amount.longValue());
            Transaction$Output change = new Transaction$Output();
            change.address = this.address;
            change.amount = difference;
            outs.add(change);
        }

        transaction.inputs = ins.toArray(new Transaction$Input[ins.size()]);
        transaction.outputs = outs.toArray(new Transaction$Output[outs.size()]);

        PrivateKey[] pkeys = new PrivateKey[transaction.inputs.length];
        Arrays.fill(pkeys, this.keys.getPrivate());
        transaction.signInputs(pkeys);
        return transaction.verifyInputSignatures() ? transaction : null;
    }

    /**
     * Returns true if this {@code Wallet} can be used to pay the given debt.
     * 
     * @param amount The amount of money owed
     * @return True if this wallet has enough money to pay the given debt
     */
    public boolean canAfford(long amount) {
        this.updateBalance();
        return this.getBalance().longValue() >= amount;
    }

    /**
     * Returns true if this {@code Wallet} can be used to pay the given debt.
     * 
     * @param amount The amount of money owed
     * @return True if this wallet has enough money to pay the given debt
     */
    public boolean canAfford(CoinAmount amount) {
        return this.canAfford(amount.longValue());
    }

    /**
     * Gets the {@link CoinAddress} this {@code Wallet} manages the money for.
     * 
     * @return This {@code Wallet}'s {@code CoinAddress}.
     */
    public CoinAddress getAddress() {
        return this.address;
    }

    public synchronized CCTableModel getIncomeModel() {
        if (this.incomeModel == null) this.incomeModel = new CCTableModel(COL_NAMES_FROM);

        Payment[] income = BlockTree.getInstance().getPaymentsTo(this.address);

        for (Payment i : income) {
            if (!i.getSender().equals(this.address)) {
                this.incomeModel.addRow(new Object[] {
                        false, i.getTime(),
                        i.getAmount(), i.getSender()});
            }
        }
        return this.incomeModel;
    }

    public synchronized CCTableModel getExpendituresModel() {
        if (this.expendituresModel == null) this.expendituresModel = new CCTableModel(COL_NAMES_TO);

        Payment[] expenditures = BlockTree.getInstance().getPaymentsFrom(this.address);

        for (Payment i : expenditures) {
            if (!i.getRecipient().equals(this.address)) {
                // If this isn't a payment to ourself (for change
                // purposes)...
                this.expendituresModel.addRow(new Object[] {
                        false, i.getTime(), i.getAmount(), i.getRecipient()});
            }
        }

        return this.expendituresModel;
    }

    /**
     * Given a {@link Transaction$Output}, creates a {@link Transaction$Input}
     * that specifies the {@link Transaction} that owns said output as a source.
     * 
     * @O n
     * @param output
     * @return
     */
    private Transaction$Input outputToInput(Transaction$Output output) {
        Transaction transaction = BlockTree.getInstance().getContainingTransaction(output);
        if (transaction == null) {
            // If we don't have a Transaction that specifies this output...
            return null;
        }
        else {
            Transaction$Input input = new Transaction$Input();
            input.providerHash = transaction.getHash();
            input.providerIndex = -1;
            for (int i = 0; i < transaction.outputs.length; ++i) {
                // For each output in the transaction we found...
                if (transaction.outputs[i] == output) {
                    input.providerIndex = i;
                    break;
                }
            }
            if (input.providerIndex == -1) return null;
            // If for some reason we never found the given output in the
            // discovered transaction...

            input.publicKey = this.keys.getPublic();
            return input;
        }
    }

    @Override
    /**
     * {@inheritDoc}
     * 
     * Compares the key pairs of each wallet.
     */
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (other.getClass() != this.getClass()) return false;
        Wallet w = (Wallet)other;
        return this.keys.getPublic().equals(w.keys.getPublic()) && this.keys.getPrivate().equals(w.keys.getPrivate());
    }

    /**
     * {@inheritDoc}
     * 
     * Computed with half of the public key's hash code and half of the private
     * key's hash code.
     */
    @Override
    public int hashCode() {
        int pub = this.keys.getPublic().hashCode();
        int pri = this.keys.getPrivate().hashCode();

        return (pub << 16) | (pri >>> 16);
    }

    /**
     * Exports this Wallet to a {@link String} that will be saved to a file and
     * reimported later
     * 
     * @return The {@code String}
     */
    public String export() {
        return String.format("%s %s %s [%s] [%s]", this.getAddress(),
                KeyUtilities.exportPublicKey(this.keys.getPublic()),
                KeyUtilities.exportPrivateKey(this.keys.getPrivate()),
                this.getName(), this.getDescription());
    }

    /**
     * Given a textual representation of a wallet, which is a single line of
     * text, return a Wallet object.
     * 
     * @param wallet
     * @return
     */
    public static Wallet importWallet(String wallet) {
        Wallet w;
        try {
            Pattern regex = Pattern.compile("^(\\S+) (\\S+) (\\S+) \\[(.+)\\] \\[(.*)\\]$");

            Matcher match = regex.matcher(wallet.trim());
            if (!match.matches() || match.groupCount() != 5) return null;
            // If this isn't a valid wallet line...

            w = new Wallet(match.group(4), match.group(5), new KeyPair(KeyUtilities.importPublicKey(match.group(2),
                    PARAMS.KEYPAIR_ALGORITHM), KeyUtilities.importPrivateKey(match.group(3), PARAMS.KEYPAIR_ALGORITHM)));
        }
        catch (Exception e) {
            return null;
        }

        return w;
    }

    public synchronized void updateBalance() {
        this.cachedBalance = BlockTree.getInstance().getBalance(this.address);
    }
}
