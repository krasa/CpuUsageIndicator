package krasa.cpu.diagnostic;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;

public class OpenPerformanceWatcherSettings extends DumbAwareAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		ShowSettingsUtil.getInstance().editConfigurable(getEventProject(e), "PerformanceWatcherSettings", new PerformanceWatcherConfigurable(), true);
	}
}
