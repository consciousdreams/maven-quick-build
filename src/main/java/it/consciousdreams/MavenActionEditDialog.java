package it.consciousdreams;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.util.List;

public class MavenActionEditDialog extends DialogWrapper {

    static final List<String[]> AVAILABLE_ICONS = List.of(
            new String[]{"/icons/maven_install.svg",            "!m  (skip tests)"},
            new String[]{"/icons/maven_install_with_tests.svg", "m   (with tests)"}
    );

    private final JTextField labelField;
    private final JTextField goalsField;
    private final JComboBox<String[]> iconCombo;

    public MavenActionEditDialog(@Nullable String label, @Nullable String goals, @Nullable String iconPath) {
        super(true);
        labelField = new JTextField(label != null ? label : "", 30);
        goalsField = new JTextField(goals != null ? goals : "", 30);
        iconCombo  = buildIconCombo(iconPath);
        setTitle(label == null ? "Add Maven Action" : "Edit Maven Action");
        init();
    }

    private JComboBox<String[]> buildIconCombo(@Nullable String selectedPath) {
        JComboBox<String[]> combo = new JComboBox<>(AVAILABLE_ICONS.toArray(new String[0][]));
        combo.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof String[] entry) {
                    setIcon(IconLoader.getIcon(entry[0], MavenActionEditDialog.class));
                    setText(entry[1]);
                }
                return this;
            }
        });
        // pre-select matching entry
        if (selectedPath != null) {
            for (int i = 0; i < AVAILABLE_ICONS.size(); i++) {
                if (AVAILABLE_ICONS.get(i)[0].equals(selectedPath)) {
                    combo.setSelectedIndex(i);
                    break;
                }
            }
        }
        return combo;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Label:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(labelField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Maven Goals:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(goalsField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Icon:"), gbc);
        gbc.gridx = 1;
        panel.add(iconCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(new JLabel("<html><small>Example goals: <i>clean install -Dmaven.test.skip=true</i></small></html>"), gbc);

        return panel;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        if (labelField.getText().trim().isEmpty()) return new ValidationInfo("Label is required", labelField);
        if (goalsField.getText().trim().isEmpty()) return new ValidationInfo("Maven Goals are required", goalsField);
        return null;
    }

    public String getLabel()    { return labelField.getText().trim(); }
    public String getGoals()    { return goalsField.getText().trim(); }
    public String getIconPath() {
        String[] selected = (String[]) iconCombo.getSelectedItem();
        return selected != null ? selected[0] : "/icons/maven_install.svg";
    }
}
