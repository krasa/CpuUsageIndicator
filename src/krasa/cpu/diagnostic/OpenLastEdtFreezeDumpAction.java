package krasa.cpu.diagnostic;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class OpenLastEdtFreezeDumpAction extends DumbAwareAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		Project eventProject = getEventProject(e);
		if (eventProject == null) {
			return;
		}


		Path dir = Paths.get(PathManager.getLogPath());
		try {
			Optional<Path> lastFilePath = Files.list(dir)
				.filter(f -> Files.isDirectory(f))
				.filter(f -> f.getFileName().toString().startsWith("threadDumps-freeze-"))
				.max((f1, f2) -> (int) (f1.toFile().lastModified() - f2.toFile().lastModified()));


			if (lastFilePath.isPresent()) {
				Optional<Path> first = Files.list(lastFilePath.get()).filter(path -> path.getFileName().toString().startsWith("threadDump-")).findFirst();
				first.ifPresent(path -> {
					VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(path.toFile());
					if (fileByIoFile != null) {
						FileEditorManager.getInstance(eventProject).openFile(fileByIoFile, true);
					}
				});
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
