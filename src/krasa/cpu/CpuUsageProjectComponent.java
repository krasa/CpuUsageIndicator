package krasa.cpu;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.MemoryUsagePanel;
import org.jetbrains.annotations.NotNull;

public class CpuUsageProjectComponent implements ProjectComponent {
	private final Project project;
	private CpuUsagePanel statusBarWidget;

	public CpuUsageProjectComponent(Project project) {
		this.project = project;
	}

	@Override
	public void initComponent() {
	}

	@Override
	public void disposeComponent() {
	}

	@Override
	@NotNull
	public String getComponentName() {
		return "CpuUsageProjectComponent";
	}

	@Override
	public void projectOpened() {
		final StatusBar statusBar = WindowManager.getInstance().getIdeFrame(project).getStatusBar();
		statusBarWidget = new CpuUsagePanel(project.getName());
		statusBar.addWidget(statusBarWidget, "before " + MemoryUsagePanel.WIDGET_ID);
	}

	@Override
	public void projectClosed() {
		if (statusBarWidget != null) {
			final StatusBar statusBar = WindowManager.getInstance().getIdeFrame(project).getStatusBar();
			statusBar.removeWidget(CpuUsagePanel.WIDGET_ID);
			Disposer.dispose(statusBarWidget);
		}
	}
}
