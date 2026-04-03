package it.consciousdreams;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MavenQuickBuildConfigurable implements Configurable {

    private JPanel mainPanel;
    private JBTable table;
    private ActionsTableModel tableModel;

    @Override
    public @Nls String getDisplayName() {
        return "Maven Quick Build";
    }

    @Override
    public @Nullable JComponent createComponent() {
        tableModel = new ActionsTableModel();
        table = new JBTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);

        // Icon column
        table.getColumnModel().getColumn(0).setMinWidth(28);
        table.getColumnModel().getColumn(0).setMaxWidth(28);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, "", isSelected, hasFocus, row, col);
                if (value instanceof Icon icon) {
                    label.setIcon(icon);
                    label.setHorizontalAlignment(CENTER);
                }
                return label;
            }
        });
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(320);

        JPanel decoratedTable = ToolbarDecorator.createDecorator(table)
                .setAddAction(button -> addAction())
                .setRemoveAction(button -> removeAction())
                .setEditAction(button -> editAction())
                .createPanel();

        mainPanel = new JPanel(new BorderLayout(0, 8));
        mainPanel.add(new JLabel("Configure Maven toolbar buttons:"), BorderLayout.NORTH);
        mainPanel.add(decoratedTable, BorderLayout.CENTER);

        reset();
        return mainPanel;
    }

    private void addAction() {
        MavenActionEditDialog dialog = new MavenActionEditDialog(null, null, null);
        if (dialog.showAndGet()) {
            tableModel.addRow(new MavenActionConfig(
                    UUID.randomUUID().toString(),
                    dialog.getLabel(),
                    dialog.getGoals(),
                    dialog.getIconPath()
            ));
        }
    }

    private void editAction() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        MavenActionConfig config = tableModel.getRow(row);
        MavenActionEditDialog dialog = new MavenActionEditDialog(config.label, config.goals, config.iconPath);
        if (dialog.showAndGet()) {
            config.label    = dialog.getLabel();
            config.goals    = dialog.getGoals();
            config.iconPath = dialog.getIconPath();
            tableModel.fireTableRowsUpdated(row, row);
        }
    }

    private void removeAction() {
        int row = table.getSelectedRow();
        if (row >= 0) tableModel.removeRow(row);
    }

    @Override
    public boolean isModified() {
        if (tableModel == null) return false;
        List<MavenActionConfig> saved   = MavenQuickBuildSettings.getInstance().getActions();
        List<MavenActionConfig> edited  = tableModel.getRows();
        if (saved.size() != edited.size()) return true;
        for (int i = 0; i < saved.size(); i++) {
            if (!saved.get(i).equals(edited.get(i))) return true;
        }
        return false;
    }

    @Override
    public void apply() {
        MavenQuickBuildSettings.getInstance().setActions(tableModel.getRows());
    }

    @Override
    public void reset() {
        if (tableModel == null) return;
        tableModel.setRows(MavenQuickBuildSettings.getInstance().getActions());
    }

    @Override
    public void disposeUIResources() {
        mainPanel  = null;
        table      = null;
        tableModel = null;
    }

    private static class ActionsTableModel extends AbstractTableModel {
        private final List<MavenActionConfig> rows = new ArrayList<>();

        void setRows(List<MavenActionConfig> source) {
            rows.clear();
            for (MavenActionConfig c : source) rows.add(c.copy());
            fireTableDataChanged();
        }

        List<MavenActionConfig> getRows()   { return new ArrayList<>(rows); }
        MavenActionConfig       getRow(int i) { return rows.get(i); }

        void addRow(MavenActionConfig c) {
            rows.add(c);
            fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
        }

        void removeRow(int i) {
            rows.remove(i);
            fireTableRowsDeleted(i, i);
        }

        @Override public int getRowCount()    { return rows.size(); }
        @Override public int getColumnCount() { return 3; }

        @Override
        public String getColumnName(int col) {
            return switch (col) {
                case 0 -> "";
                case 1 -> "Label";
                default -> "Maven Goals";
            };
        }

        @Override
        public Object getValueAt(int row, int col) {
            MavenActionConfig c = rows.get(row);
            return switch (col) {
                case 0 -> DynamicMavenAction.loadIcon(c.iconPath);
                case 1 -> c.label;
                default -> c.goals;
            };
        }
    }
}
