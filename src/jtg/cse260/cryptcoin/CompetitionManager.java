package jtg.cse260.cryptcoin;

import java.util.Arrays;

import com.starkeffect.cryptcoin.protocol.CoinAddress;
import com.starkeffect.cryptcoin.protocol.CoinAmount;
import com.starkeffect.cryptcoin.protocol.Parameters;
import com.starkeffect.cryptcoin.protocol.Transaction;
import com.starkeffect.cryptcoin.protocol.Transaction$Output;
import com.starkeffect.cryptcoin.protocol.TransactionMessage;

import jtg.cse260.cryptcoin.block.BlockTree;
import jtg.cse260.cryptcoin.node.NodeManager;
import jtg.cse260.cryptcoin.node.Wallet;

public class CompetitionManager implements Runnable {
    private static final Parameters   PARAMS = Parameters.getInstance();
    private boolean                   running;
    private Wallet                    trough;
    private CoinAddress               target;

    private static CompetitionManager instance;

    public static void init(Wallet trough, CoinAddress target) {
        if (instance != null) instance.stopRunning();

        instance = new CompetitionManager(trough, target);
    }

    public static CompetitionManager getInstance() {
        return instance;
    }

    private CompetitionManager(Wallet trough, CoinAddress target) {
        super();
        this.running = false;
        this.trough = trough;
        this.target = target;
    }

    @Override
    public void run() {
        this.running = true;
        System.out.println("Starting!");
        while (this.running) {
            Transaction$Output[] outputs = BlockTree.getInstance().getUnspentOutputs(trough.getAddress());
            System.out.println("Computing Unspent Outputs");
            if (outputs.length <= 0) continue;
            int ptr = 0;
            while (ptr < outputs.length) {
                // Until we've exhausted all unused outputs...
                int len = Math.min(outputs.length - ptr, PARAMS.TRANSACTION_MAX_OUTPUTS);
                Transaction$Output[] currentOutputs = Arrays.copyOfRange(outputs, ptr, ptr + len);

                Transaction t = this.trough.createTransaction(this.target, currentOutputs);
                NodeManager.getInstance().sendGlobalMessage(new TransactionMessage(t));
                ptr += currentOutputs.length;
            }
        }
    }

    public void stopRunning() {
        this.running = false;
        instance = null;
    }
}
