package jtg.cse260.cryptcoin.gui;

import java.awt.Component;
import java.util.Comparator;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jtg.cse260.cryptcoin.Resources;
import jtg.cse260.cryptcoin.gui.tabs.CCTableModel;

import com.starkeffect.cryptcoin.protocol.CoinAddress;
import com.starkeffect.cryptcoin.protocol.CoinAmount;

public class CCTable extends JTable {
    public CCTable(CCTableModel model) {
        super(model);

        this.setBorder(null);
        this.setDragEnabled(false);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.columnModel.getColumn(0).setPreferredWidth(30);
        this.columnModel.getColumn(0).setMaxWidth(40);

        this.setAutoCreateRowSorter(true);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        switch (column) {
            case 0:
                return this.confirmedRenderer;
            case 1:
                return this.dateRenderer;
            case 2:
                return this.coinAmountRenderer;
            case 3:
                return this.coinAddressRenderer;
            default:
                return null;
        }
    }

    public void setModel(CCTableModel model) {
        super.setModel(model);

        this.columnModel.getColumn(0).setPreferredWidth(30);
        this.columnModel.getColumn(0).setMaxWidth(40);
    }
    
    private ConfirmedRenderer   confirmedRenderer   = new ConfirmedRenderer();
    private CoinAddressRenderer coinAddressRenderer = new CoinAddressRenderer();
    private DateRenderer        dateRenderer        = new DateRenderer();
    private CoinAmountRenderer  coinAmountRenderer  = new CoinAmountRenderer();

    private class CoinAmountRenderer extends DefaultTableCellRenderer {
        @Override
        public void setValue(Object value) {
            if (value instanceof CoinAmount) {
                this.setText(((CoinAmount)value).toString());
            }
            else if (value instanceof Long) {
                this.setText("CC " + (long)value);
            }
            else {
                this.setText("???");
            }
        }
    }

    private class ConfirmedRenderer extends DefaultTableCellRenderer {
        @Override
        public void setValue(Object value) {
            if (value == null || !(value instanceof Boolean)) {
                this.setText("?");
            }
            else {
                boolean b = (boolean)value;
                this.setText(null);
                this.setIcon(Resources.getIcon(b ? "transaction-confirmed" : "transaction-pending"));
                this.setAlignmentX(CENTER_ALIGNMENT);
                this.setAlignmentY(CENTER_ALIGNMENT);
                this.setHorizontalAlignment(CENTER);
            }
        }
    }

    private class CoinAddressRenderer extends DefaultTableCellRenderer {
        @Override
        public void setValue(Object value) {
            if (value == null || !(value instanceof CoinAddress)) {
                setText("UNKNOWN");
            }
            else {
                CoinAddress address = (CoinAddress)value;
                setText(String.format("%s (Address: %s)", address.getOwnerID(), address));
            }
        }
    }

    private class DateRenderer extends DefaultTableCellRenderer {
        @Override
        public void setValue(Object value) {
            if (value == null) {
                setText("UNKNOWN");
            }
            else if (value instanceof Date) {
                Date date = (Date)value;
                setText(String.format("%tc", date));
            }
        }
    }
}
