package jtg.cse260.cryptcoin.gui.tabs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import jtg.cse260.cryptcoin.gui.GUIConstants;

@SuppressWarnings("serial")
public class MiningTab extends JPanel implements GUIConstants {
    private JCheckBox mineCheckbox;

    public MiningTab() {
        this.setBorder(GUIConstants.DEFAULT_BORDER);
        
        GridBagLayout layout = new GridBagLayout();
        {
            layout.columnWidths = new int[] {0, 0, 0};
            layout.rowHeights = new int[] {0, 0, 0, 0};
            layout.columnWeights = new double[] {0.0, 0.0, Double.MIN_VALUE};
            layout.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
        }
        this.setLayout(layout);

        this.mineCheckbox = new JCheckBox("Mine!");
        {
            GridBagConstraints mineCheckboxConstraints = new GridBagConstraints();
            {
                mineCheckboxConstraints.gridx = 1;
                mineCheckboxConstraints.gridy = 2;
            }
            this.add(mineCheckbox, mineCheckboxConstraints);
        }
    }
}
