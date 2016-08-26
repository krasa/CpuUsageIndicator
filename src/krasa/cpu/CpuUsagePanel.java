package krasa.cpu;
/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import com.intellij.concurrency.JobScheduler;
import com.intellij.openapi.application.ApplicationManager;
import com.sun.management.OperatingSystemMXBean;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.update.Activatable;
import com.intellij.util.ui.update.UiNotifyConnector;

public class CpuUsagePanel extends JButton implements CustomStatusBarWidget {
	@NonNls
	public static final String WIDGET_ID = "Cpu";

	private static final Color USED_COLOR = JBColor.BLUE.darker();
	private static final Color UNUSED_COLOR = JBColor.BLUE.darker().darker().darker();

	private long system = 0;
	private long process = 0;
	private long myLastTotal = -1;
	private long myLastUsed = -1;
	private Image myBufferedImage;
	private boolean myWasPressed;
	private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getPlatformMXBean(
			OperatingSystemMXBean.class);

	public CpuUsagePanel() {
		setOpaque(false);
		setFocusable(false);

		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateState();
			}
		});

		setBorder(StatusBarWidget.WidgetBorder.INSTANCE);
		updateUI();

		new UiNotifyConnector(this, new Activatable() {
			private ScheduledFuture<?> myFuture;

			@Override
			public void showNotify() {
				myFuture = JobScheduler.getScheduler().scheduleWithFixedDelay(CpuUsagePanel.this::updateValues, 1, 1,
						TimeUnit.SECONDS);
			}

			@Override
			public void hideNotify() {
				if (myFuture != null) {
					myFuture.cancel(true);
					myFuture = null;
				}
			}
		});

	}

	@Override
	public void dispose() {
	}

	@Override
	public void install(@NotNull StatusBar statusBar) {
	}

	@Override
	@Nullable
	public WidgetPresentation getPresentation(@NotNull PlatformType type) {
		return null;
	}

	@Override
	@NotNull
	public String ID() {
		return WIDGET_ID;
	}

	public void setShowing(final boolean showing) {
		if (showing != isVisible()) {
			setVisible(showing);
			revalidate();
		}
	}

	@Override
	public void updateUI() {
		super.updateUI();
		setFont(getWidgetFont());
	}

	private static Font getWidgetFont() {
		return JBUI.Fonts.label(11);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void paintComponent(final Graphics g) {
		final boolean pressed = getModel().isPressed();
		final boolean stateChanged = myWasPressed != pressed;
		myWasPressed = pressed;

		if (myBufferedImage == null || stateChanged) {
			final Dimension size = getSize();
			final Insets insets = getInsets();

			myBufferedImage = UIUtil.createImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2 = (Graphics2D) myBufferedImage.getGraphics().create();

			final long max = 100;
			final long otherProcesses = system - process;

			final int totalBarLength = size.width - insets.left - insets.right;
			final int processUsageBarLength = (int) (totalBarLength * process / max);
			final int otherProcessesUsageBarLength = (int) (totalBarLength * otherProcesses / max);
			final int barHeight = Math.max(size.height, getFont().getSize() + 2);
			final int yOffset = (size.height - barHeight) / 2;
			final int xOffset = insets.left;

			// background
			g2.setColor(UIUtil.getPanelBackground());
			g2.fillRect(0, 0, size.width, size.height);

			// gauge (used)
			g2.setColor(USED_COLOR);
			g2.fillRect(xOffset, yOffset, processUsageBarLength, barHeight);

			// gauge (unused)
			g2.setColor(UNUSED_COLOR);
			g2.fillRect(xOffset + processUsageBarLength, yOffset, otherProcessesUsageBarLength, barHeight);

			// label
			g2.setFont(getFont());
			final String info = CpuUsageBundle.message("cpu.usage.panel.message.text", process, system);
			final FontMetrics fontMetrics = g.getFontMetrics();
			final int infoWidth = fontMetrics.charsWidth(info.toCharArray(), 0, info.length());
			final int infoHeight = fontMetrics.getAscent();
			UISettings.setupAntialiasing(g2);
			
			final Color fg = pressed ? UIUtil.getLabelDisabledForeground() : JBColor.foreground();
			g2.setColor(fg);
			g2.drawString(info, xOffset + (totalBarLength - infoWidth) / 2,
					yOffset + infoHeight + (barHeight - infoHeight) / 2 - 1);

			//border
			g2.setStroke(new BasicStroke(1));
			g2.setColor(JBColor.GRAY);
			g2.drawRect(0,0,size.width-1,size.height-1);

			g2.dispose();
		}

		UIUtil.drawImage(g, myBufferedImage, 0, 0, null);
		if (UIUtil.isRetina() && !UIUtil.isUnderDarcula()) {
			Graphics2D g2 = (Graphics2D) g.create(0, 0, getWidth(), getHeight());
			g2.scale(0.5, 0.5);
			g2.setColor(UIUtil.isUnderIntelliJLaF() ? Gray.xC9 : Gray.x91);
			g2.drawLine(0, 0, 2 * getWidth(), 0);
			g2.scale(1, 1);
			g2.dispose();
		}
	}

	@Override
	public Dimension getPreferredSize() {
		final Insets insets = getInsets();
		int width = getFontMetrics(getWidgetFont()).stringWidth("100% / 100%") + insets.left + insets.right
				+ JBUI.scale(2);
		int height = getFontMetrics(getWidgetFont()).getHeight() + insets.top + insets.bottom + JBUI.scale(2);
		return new Dimension(width, height);
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	private void updateState() {
		if (!isShowing()) {
			return;
		}

		if (system != myLastTotal || process != myLastUsed) {
			myLastTotal = system;
			myLastUsed = process;
			UIUtil.invokeLaterIfNeeded(() -> {
				myBufferedImage = null;
				repaint();
			});

			setToolTipText(CpuUsageBundle.message("cpu.usage.panel.statistics.message", process, system));
		}
	}

	private void updateValues() {
//		long start = System.currentTimeMillis();

		system = (long) (OS_BEAN.getSystemCpuLoad() * 100);
		process = (long) (OS_BEAN.getProcessCpuLoad() * 100); //this shit is expensive!!!

//		System.err.println("updateValues " +(System.currentTimeMillis() - start));
		ApplicationManager.getApplication().invokeLater(CpuUsagePanel.this::updateState);
	}

}
