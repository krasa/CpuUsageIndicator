package krasa.cpu.diagnostic;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PerformanceWatcherConfigurable implements Configurable {
	private PerformanceWatcherForm performanceWatcherForm;

	public PerformanceWatcherConfigurable() {
		performanceWatcherForm = new PerformanceWatcherForm();
	}

	@Nls
	@Override
	public String getDisplayName() {
		return "Frozen UI thread dumper";
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		return performanceWatcherForm.getRoot();
	}

	@Override
	public boolean isModified() {
		return performanceWatcherForm.isModified();
	}

	@Override
	public void apply() throws ConfigurationException {
		performanceWatcherForm.apply();
	}
}
