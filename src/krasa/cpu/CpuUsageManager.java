package krasa.cpu;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.intellij.concurrency.JobScheduler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.sun.management.OperatingSystemMXBean;

public class CpuUsageManager {
	private static final Logger log = Logger.getInstance(CpuUsageManager.class);

	private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getPlatformMXBean(
			OperatingSystemMXBean.class);

	volatile static int system = 0;
	volatile static int process = 0;

	private static ScheduledFuture<?> scheduledFuture = JobScheduler.getScheduler().scheduleWithFixedDelay(
			CpuUsageManager::update, 1, 1, TimeUnit.SECONDS);

	private static java.util.List<CpuUsagePanel> cpuUsagePanelList = new CopyOnWriteArrayList<>();

	static void update() {
		try {
			// long start = System.currentTimeMillis();

			system = (int) (OS_BEAN.getSystemCpuLoad() * 100);
			process = (int) (OS_BEAN.getProcessCpuLoad() * 100); // this shit is expensive!!!
			// log.info("process" + process + " system=" + system);

			ApplicationManager.getApplication().invokeLater(() -> {
				for (CpuUsagePanel cpuUsagePanel : cpuUsagePanelList) {
					cpuUsagePanel.updateState();
				}
			});
			// System.err.println("updateValues " +(System.currentTimeMillis() - start));
		} catch (Exception e) {
			log.error(e);
		}
	}

	public static void unregister(CpuUsagePanel activatable) {
		log.info("unregistering " + activatable);
		cpuUsagePanelList.remove(activatable);
	}

	public static void register(CpuUsagePanel activatable) {
		log.info("registering " + activatable);
		cpuUsagePanelList.add(activatable);
	}
}
