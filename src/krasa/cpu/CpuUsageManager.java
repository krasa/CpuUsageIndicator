package krasa.cpu;

import com.intellij.concurrency.JobScheduler;
import com.intellij.openapi.diagnostic.Logger;
import com.sun.management.OperatingSystemMXBean;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CpuUsageManager {
	private static final Logger log = Logger.getInstance(CpuUsageManager.class);

	private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getPlatformMXBean(
		OperatingSystemMXBean.class);

	volatile static int system = 0;
	volatile static int process = 0;
	static boolean broken = false;

	private static ScheduledFuture<?> scheduledFuture = JobScheduler.getScheduler().scheduleWithFixedDelay(
		CpuUsageManager::update, 1, 1, TimeUnit.SECONDS);

	private static java.util.Set<CpuUsagePanel> cpuUsagePanelList = new CopyOnWriteArraySet<>();

	static synchronized void update() {
		try {
			system = (int) Math.round(OS_BEAN.getSystemCpuLoad() * 100);
			process = (int) Math.round(OS_BEAN.getProcessCpuLoad() * 100); // this shit is expensive!!!
			// log.info("process" + process + " system=" + system);

			boolean painted = false;
			for (CpuUsagePanel cpuUsagePanel : cpuUsagePanelList) {
				painted = cpuUsagePanel.update() || painted;
			}
			if (painted) {
				Toolkit.getDefaultToolkit().sync();
			}
			broken = false;
		} catch (Exception e) {
			if (broken) {
				log.error(e);
				throw e;
			} else {
				log.info(e);
				//one fail tolerance for strange errors
				broken = true;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
			}
		} catch (Throwable e) {
			log.error(e);
			throw e;
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
