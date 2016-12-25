package krasa.cpu;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.MemoryUsagePanel;
import org.jetbrains.annotations.NotNull;

public class CpuUsageProjectComponent implements ProjectComponent {
	private final Project project;
	private CpuUsagePanel statusBarWidget;
	private IdeFrame ideFrame;

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
		ideFrame = WindowManager.getInstance().getIdeFrame(this.project);
		final StatusBar statusBar = ideFrame.getStatusBar();
		statusBarWidget = new CpuUsagePanel(project.getName());
		statusBar.addWidget(statusBarWidget, "before " + MemoryUsagePanel.WIDGET_ID);
	}

	@Override
	public void projectClosed() {
		if (statusBarWidget != null) {
			final StatusBar statusBar = ideFrame.getStatusBar();
			if (statusBar != null) {
				statusBar.removeWidget(CpuUsagePanel.WIDGET_ID);
			}
			Disposer.dispose(statusBarWidget);
		}
	}
}
