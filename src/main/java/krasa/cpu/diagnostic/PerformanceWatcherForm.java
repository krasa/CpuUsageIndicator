package krasa.cpu.diagnostic;

import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.registry.RegistryValue;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PerformanceWatcherForm {
	private JPanel root;

	private JTextField attempsField;
	private JTextField unresponsiveIntervalField;
	private JTextField samplingIntervalField;
	private JButton resetToDefaultButton;
	private com.intellij.openapi.ui.ex.MultiLineLabel label;
	private JLabel samplingIntervalLabel;
	private JLabel samplingIntervalLabel2;


	private final RegistryValue ATTEMPS = Registry.get("performance.watcher.unresponsive.max.attempts.before.log");
	private final RegistryValue UNRESPONSIVE_INTERVAL_MS = Registry.get("performance.watcher.unresponsive.interval.ms");
	private final RegistryValue SAMPLING_INTERVAL_MS = Registry.get("performance.watcher.sampling.interval.ms");


	public PerformanceWatcherForm() {
		label.setText("For experts only:\nconfiguration for Performance Watcher (IDE's bundled automatic thread dumper for frozen UI)\n - you can configure it to dump even for shorter freezes than is the default 5s.");

		init();

		resetToDefaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ATTEMPS.resetToDefault();
				UNRESPONSIVE_INTERVAL_MS.resetToDefault();
				SAMPLING_INTERVAL_MS.resetToDefault();
				init();

			}
		});
	}

	private void init() {
		int attemps = ATTEMPS.asInteger();
		int unresponsiveInterval = UNRESPONSIVE_INTERVAL_MS.asInteger();
		int samplingInterval = SAMPLING_INTERVAL_MS.asInteger();

		attempsField.setText(String.valueOf(attemps));
		unresponsiveIntervalField.setText(String.valueOf(unresponsiveInterval));
		samplingIntervalField.setText(String.valueOf(samplingInterval));
	}

	public JPanel getRoot() {
		return root;
	}

	public boolean isModified() {
		int attemps = ATTEMPS.asInteger();
		int unresponsiveInterval = UNRESPONSIVE_INTERVAL_MS.asInteger();
		int samplingInterval = SAMPLING_INTERVAL_MS.asInteger();

		boolean modified = false;
		try {
			modified = attemps != Integer.parseInt(attempsField.getText()) ||
				unresponsiveInterval != Integer.parseInt(unresponsiveIntervalField.getText()) ||
				samplingInterval != Integer.parseInt(samplingIntervalField.getText());
		} catch (NumberFormatException e) {
		}
		return modified;
	}

	public void apply() {
		int attemps = Integer.parseInt(attempsField.getText());
		int unresponsiveInterval = Integer.parseInt(unresponsiveIntervalField.getText());
		int samplingInterval = Integer.parseInt(samplingIntervalField.getText());

		if (attemps < 0 || unresponsiveInterval < 0 || samplingInterval < 0) {
			throw new RuntimeException("Invalid values");
		}

		ATTEMPS.setValue(attemps);
		UNRESPONSIVE_INTERVAL_MS.setValue(unresponsiveInterval);
		SAMPLING_INTERVAL_MS.setValue(samplingInterval);
	}
}
