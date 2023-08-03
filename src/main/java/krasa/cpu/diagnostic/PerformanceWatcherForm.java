package krasa.cpu.diagnostic;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.registry.RegistryValue;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PerformanceWatcherForm {
    private static final @NotNull Logger log = Logger.getInstance(PerformanceWatcherForm.class);

    private JPanel root;

    private JTextField unresponsiveIntervalField;
    private JButton resetToDefaultButton;
    private com.intellij.openapi.ui.ex.MultiLineLabel label;


    private final RegistryValue UNRESPONSIVE_INTERVAL_MS = Registry.get("performance.watcher.unresponsive.interval.ms");
    private static boolean broken;


    public PerformanceWatcherForm() {
        label.setText("For experts only:\nconfiguration for Performance Watcher (IDE's bundled automatic thread dumper for frozen UI)\n - you can configure it to dump even for shorter freezes than is the default 5s.");

        init();

        resetToDefaultButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (broken) {
                    return;
                }
                UNRESPONSIVE_INTERVAL_MS.resetToDefault();
                init();

            }
        });
    }

    private void init() {
        if (broken) {
            return;
        }
        try {
            int unresponsiveInterval = UNRESPONSIVE_INTERVAL_MS.asInteger();
            unresponsiveIntervalField.setText(String.valueOf(unresponsiveInterval));
        } catch (Exception e) {
            broken = true;
            log.error(e);
        }
    }

    public JPanel getRoot() {
        return root;
    }

    public boolean isModified() {
        if (broken) {
            return false;
        }
        int unresponsiveInterval = UNRESPONSIVE_INTERVAL_MS.asInteger();

        boolean modified = false;
        try {
            modified = unresponsiveInterval != Integer.parseInt(unresponsiveIntervalField.getText());
        } catch (NumberFormatException e) {
        }
        return modified;
    }

    public void apply() {
        if (broken) {
            return;
        }
        int unresponsiveInterval = Integer.parseInt(unresponsiveIntervalField.getText());

        if (unresponsiveInterval < 0) {
            throw new RuntimeException("Invalid values");
        }

        UNRESPONSIVE_INTERVAL_MS.setValue(unresponsiveInterval);
    }
}
