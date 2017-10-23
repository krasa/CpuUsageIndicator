package krasa.cpu.diagnostic;

import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class TakeThreadDumpAction extends DumbAwareAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		Project eventProject = getEventProject(e);
		if (eventProject == null) {
			return;
		}
		ThreadMXBean myThreadMXBean = ManagementFactory.getThreadMXBean();
		krasa.cpu.diagnostic.ThreadDump threadDump = krasa.cpu.diagnostic.ThreadDumper.getThreadDumpInfo(myThreadMXBean);
		String rawDump = threadDump.getRawDump();
		doCreateNewScratch(eventProject, rawDump);
	}

	private static void doCreateNewScratch(@NotNull Project project, @NotNull String text) {
		VirtualFile f = ScratchRootType.getInstance().createScratchFile(project, PathUtil.makeFileName("threadDump", "txt"), PlainTextLanguage.INSTANCE, text, ScratchFileService.Option.create_if_missing);
		if (f != null) {
			FileEditorManager.getInstance(project).openFile(f, true);
		}
	}

}
