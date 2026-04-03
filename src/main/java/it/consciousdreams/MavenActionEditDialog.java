package it.consciousdreams;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.keymap.KeymapUtil;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;

import static java.awt.event.KeyEvent.*;

public class MavenActionEditDialog extends DialogWrapper {

    static final List<String[]> AVAILABLE_ICONS = List.of(
            new String[]{"/icons/maven_install.svg",            "!m  (skip tests)"},
            new String[]{"/icons/maven_install_with_tests.svg", "m   (with tests)"}
    );

    private final JTextField                labelField;
    private final JTextField                goalsField;
    private final ComboBox<String[]>        iconCombo;
    private final TextFieldWithBrowseButton customIconField;
    private final JTextField                shortcutField;
    private KeyStroke                       capturedKeystroke;

    public MavenActionEditDialog(@Nullable String label, @Nullable String goals,
                                 @Nullable String iconPath, @Nullable String shortcut) {
        super(true);
        labelField      = new JTextField(label != null ? label : "", 30);
        goalsField      = new JTextField(goals != null ? goals : "", 30);
        iconCombo       = buildIconCombo(iconPath);
        customIconField = buildCustomIconField(iconPath);
        shortcutField   = buildShortcutField(shortcut);
        setTitle(label == null ? "Add Maven Action" : "Edit Maven Action");
        init();
    }

    // ── Icon combo ────────────────────────────────────────────────────────────

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
                "Select SVG Icon", "Choose an SVG file to use as the button icon",
                null, FileChooserDescriptorFactory.createSingleFileDescriptor("svg")
        );
        if (iconPath != null && !iconPath.startsWith("/icons/")) {
            field.setText(iconPath);
        }
        return field;
    }

    // ── Shortcut field ────────────────────────────────────────────────────────

    private JTextField buildShortcutField(@Nullable String shortcut) {
        JTextField field = new JTextField(22);
        field.setEditable(false);
        field.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        if (shortcut != null && !shortcut.isEmpty()) {
            KeyStroke ks = KeyStroke.getKeyStroke(shortcut);
            if (ks != null) {
                capturedKeystroke = ks;
                field.setText(KeymapUtil.getKeystrokeText(ks));
            }
        }

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == VK_SHIFT || code == VK_CONTROL || code == VK_ALT
                        || code == VK_META || code == VK_UNDEFINED) return;

                if (code == VK_ESCAPE && e.getModifiersEx() == 0) {
                    capturedKeystroke = null;
                    field.setText("");
                    e.consume();
                    return;
                }
                capturedKeystroke = KeyStroke.getKeyStrokeForEvent(e);
                field.setText(KeymapUtil.getKeystrokeText(capturedKeystroke));
                e.consume();
            }
        });
        return field;
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(4);
        gbc.anchor = GridBagConstraints.WEST;

        addRow(panel, gbc, 0, "Label:",        labelField,       true);
        addRow(panel, gbc, 1, "Maven Goals:",  goalsField,       true);
        addRow(panel, gbc, 2, "Built-in icon:", iconCombo,       false);
        addRow(panel, gbc, 3, "Custom SVG:",   customIconField,  true);

        // Shortcut row: field + Clear button
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Shortcut:"), gbc);
        JPanel shortcutRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        shortcutRow.add(shortcutField);
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(ev -> {
            capturedKeystroke = null;
            shortcutField.setText("");
            shortcutField.requestFocusInWindow();
        });
        shortcutRow.add(clearBtn);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(shortcutRow, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("<html><small>" +
                "Click the Shortcut field and press a key combination. Esc = clear.<br>" +
                "Custom SVG overrides built-in icon when set.<br>" +
                "Goals example: <i>clean install -Dmaven.test.skip=true</i>" +
                "</small></html>"), gbc);

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row,
                        String labelText, JComponent field, boolean fillHorizontal) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        if (fillHorizontal) gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
        gbc.fill = GridBagConstraints.NONE;
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Override
    protected @Nullable ValidationInfo doValidate() {
        if (labelField.getText().trim().isEmpty())
            return new ValidationInfo("Label is required", labelField);
        if (goalsField.getText().trim().isEmpty())
            return new ValidationInfo("Maven Goals are required", goalsField);
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

    // ── Getters ───────────────────────────────────────────────────────────────

    public String  getLabel()    { return labelField.getText().trim(); }
    public String  getGoals()    { return goalsField.getText().trim(); }

    public String getIconPath() {
        String custom = customIconField.getText().trim();
        if (!custom.isEmpty()) return custom;
        String[] selected = (String[]) iconCombo.getSelectedItem();
        return selected != null ? selected[0] : "/icons/maven_install.svg";
    }

    public @Nullable String getShortcut() {
        return capturedKeystroke != null ? capturedKeystroke.toString() : null;
    }
}
