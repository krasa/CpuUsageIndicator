package krasa.cpu;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;

public class CpuUsageProjectComponent implements ProjectComponent {
	private final Project project;

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
		final CpuUsagePanel statusBarWidget = new CpuUsagePanel(project.getName());
		statusBar.addWidget(statusBarWidget);
	}

	@Override
	public void projectClosed() {
		// called when project is being closed
	}
}
