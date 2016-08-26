package krasa.cpu;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import com.intellij.AbstractBundle;

public class CpuUsageBundle extends AbstractBundle {

  public static String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @NotNull Object... params) {
    return ourInstance.getMessage(key, params);
  }

  public static final String PATH_TO_BUNDLE = "messages.CpuUsageBundle";
  private static final AbstractBundle ourInstance = new CpuUsageBundle();

  private CpuUsageBundle() {
    super(PATH_TO_BUNDLE);
  }
}
