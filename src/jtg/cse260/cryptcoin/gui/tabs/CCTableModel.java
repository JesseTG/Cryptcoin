package jtg.cse260.cryptcoin.gui.tabs;

import java.util.Date;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.starkeffect.cryptcoin.protocol.CoinAddress;
import com.starkeffect.cryptcoin.protocol.CoinAmount;
import com.starkeffect.cryptcoin.protocol.Timestamp;

public class CCTableModel extends DefaultTableModel {

    public static transient final Class<?>[] COL_TYPES = {Boolean.class, Date.class, CoinAmount.class,
                                                       CoinAddress.class};

    public CCTableModel(String[] colNames) {
        super(new Object[0][5], colNames);
        
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return COL_TYPES[columnIndex];
    }

    @Override
    public boolean isCellEditable(final int row, final int column) {
        return false;
    }

    @Override
    public void addRow(Object[] rowData) {
        for (int i = 0; i < this.getRowCount(); ++i) {
            if (this.getValueAt(i, 1).equals(rowData[1])) return;
        }
        
        super.addRow(rowData);
    }
}
