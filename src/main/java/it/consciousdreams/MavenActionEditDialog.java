package it.consciousdreams;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MavenActionEditDialog extends DialogWrapper {

    static final List<String[]> AVAILABLE_ICONS = List.of(
            new String[]{"/icons/maven_install.svg",            "!m  (skip tests)"},
            new String[]{"/icons/maven_install_with_tests.svg", "m   (with tests)"}
    );

    private final JTextField                labelField;
    private final JTextField                goalsField;
    private final ComboBox<String[]>        iconCombo;
    private final TextFieldWithBrowseButton customIconField;

    public MavenActionEditDialog(@Nullable String label, @Nullable String goals, @Nullable String iconPath) {
        super(true);
        labelField      = new JTextField(label != null ? label : "", 30);
        goalsField      = new JTextField(goals != null ? goals : "", 30);
        iconCombo       = buildIconCombo(iconPath);
        customIconField = buildCustomIconField(iconPath);
        setTitle(label == null ? "Add Maven Action" : "Edit Maven Action");
        init();
    }

    private ComboBox<String[]> buildIconCombo(@Nullable String selectedPath) {
        ComboBox<String[]> combo = new ComboBox<>(AVAILABLE_ICONS.toArray(new String[0][]));
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
        // pre-select built-in if path matches; otherwise leave at index 0
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

    private TextFieldWithBrowseButton buildCustomIconField(@Nullable String iconPath) {
        TextFieldWithBrowseButton field = new TextFieldWithBrowseButton();
        field.addBrowseFolderListener(
                "Select SVG Icon",
                "Choose an SVG file to use as the button icon",
                null,
                FileChooserDescriptorFactory.createSingleFileDescriptor("svg")
        );
        // pre-populate only when the stored path is a filesystem path (not a classpath resource)
        if (iconPath != null && !iconPath.startsWith("/icons/")) {
            field.setText(iconPath);
        }
        return field;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(4);
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
        panel.add(new JLabel("Built-in icon:"), gbc);
        gbc.gridx = 1;
        panel.add(iconCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Custom SVG:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(customIconField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("<html><small>Custom SVG overrides built-in icon when set.<br>" +
                "Goals example: <i>clean install -Dmaven.test.skip=true</i></small></html>"), gbc);

        return panel;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        if (labelField.getText().trim().isEmpty()) return new ValidationInfo("Label is required", labelField);
        if (goalsField.getText().trim().isEmpty()) return new ValidationInfo("Maven Goals are required", goalsField);
        String custom = customIconField.getText().trim();
        if (!custom.isEmpty()) {
            if (!custom.toLowerCase().endsWith(".svg"))
                return new ValidationInfo("Custom icon must be an SVG file", customIconField.getTextField());
            File file = new File(custom);
            if (!file.exists() || !file.isFile())
                return new ValidationInfo("File not found: " + custom, customIconField.getTextField());
        }
        return null;
    }

    public String getLabel() { return labelField.getText().trim(); }
    public String getGoals() { return goalsField.getText().trim(); }

    public String getIconPath() {
        String custom = customIconField.getText().trim();
        if (!custom.isEmpty()) return custom;
        String[] selected = (String[]) iconCombo.getSelectedItem();
        return selected != null ? selected[0] : "/icons/maven_install.svg";
    }
}
