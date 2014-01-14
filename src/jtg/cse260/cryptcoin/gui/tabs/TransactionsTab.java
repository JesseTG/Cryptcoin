package jtg.cse260.cryptcoin.gui.tabs;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultRowSorter;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import jtg.cse260.cryptcoin.gui.CCTable;
import jtg.cse260.cryptcoin.node.Wallet;

import com.starkeffect.cryptcoin.protocol.CoinAddress;
import com.starkeffect.cryptcoin.protocol.CoinAmount;

@SuppressWarnings("serial")
public class TransactionsTab extends JSplitPane {

    private CCTable                   incomeTable;
    private CCTable                   expendituresTable;
    private JScrollPane               expendituresPanel;
    private JScrollPane               incomePanel;

    private static final CCTableModel NULL_INCOME_MODEL       = new CCTableModel(Wallet.COL_NAMES_FROM);
    private static final CCTableModel NULL_EXPENDITURES_MODEL = new CCTableModel(Wallet.COL_NAMES_TO);
    private static final String[]     LOADING_COLS            = {"Loading", "Loading", "Loading", "Loading"};
    private static final CCTableModel LOADING_MODEL           = new CCTableModel(LOADING_COLS);

    private static TransactionsTab    instance                = new TransactionsTab();

    private Wallet                    wallet;
    private WalletModelLoader         modelLoader;

    public static TransactionsTab getInstance() {
        return instance;
    }

    private TransactionsTab() {
        this.setResizeWeight(0.5);
        this.setOrientation(JSplitPane.VERTICAL_SPLIT);
        this.wallet = null;

        this.incomeTable = new CCTable(NULL_INCOME_MODEL);
        {
            this.incomeTable.setToolTipText("The Cryptcoins you have received.");
            this.incomePanel = new JScrollPane(this.incomeTable);
            this.setLeftComponent(this.incomePanel);
        }

        this.expendituresTable = new CCTable(NULL_EXPENDITURES_MODEL);
        {
            this.expendituresTable.setToolTipText("The Cryptcoins you have spent.");
            this.expendituresPanel = new JScrollPane(this.expendituresTable);
            this.setRightComponent(this.expendituresPanel);
        }
    }

    public void setWallet(Wallet wallet) {
        if (this.wallet != null && (wallet == null || wallet == this.wallet)) {
            // If we deselected a wallet or selected the same wallet...
            return;
        }
        this.wallet = wallet;

        if (this.modelLoader != null) {
            // If we're in the middle of loading a wallet model...
            this.modelLoader.cancel(false);
        }
        this.modelLoader = new WalletModelLoader();
        modelLoader.execute();

        this.revalidate();
    }

    private class WalletModelLoader extends SwingWorker<CCTableModel[], Object> {

        public WalletModelLoader() {
            super();
        }

        @Override
        protected CCTableModel[] doInBackground() {
            TransactionsTab.this.expendituresTable.setModel(LOADING_MODEL);
            TransactionsTab.this.incomeTable.setModel(LOADING_MODEL);
            return new CCTableModel[] {
                    TransactionsTab.this.wallet.getIncomeModel(),
                    TransactionsTab.this.wallet.getExpendituresModel(),
            };
        }

        @Override
        protected void done() {
            try {
                if (this.isCancelled() || !this.isDone()) return;
                CCTableModel[] results = this.get();
                TransactionsTab.this.incomeTable.setModel(results[0]);
                TransactionsTab.this.expendituresTable.setModel(results[1]);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
