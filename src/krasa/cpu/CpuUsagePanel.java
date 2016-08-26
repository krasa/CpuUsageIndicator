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

import static com.intellij.ui.ColorUtil.softer;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import org.apache.commons.lang.builder.ToStringBuilder;
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
	private static final Color IDE = UIUtil.isUnderDarcula() ? JBColor.BLUE.darker().darker().darker()
			: softer((JBColor.CYAN));
	private static final Color SYSTEM = UIUtil.isUnderDarcula() ? JBColor.BLUE.darker() : JBColor.CYAN.darker();
	private final String projectName;

	private volatile int myLastSystem = -1;
	private volatile int myLastProcess = -1;
	private volatile Image myBufferedImage;
	private volatile boolean myWasPressed;

	public CpuUsagePanel(String projectName) {
		this.projectName = projectName;

		setOpaque(false);
		setFocusable(false);

		addActionListener(e -> CpuUsageManager.update());

		setBorder(StatusBarWidget.WidgetBorder.INSTANCE);
		updateUI();

		new UiNotifyConnector(this, new Activatable() {

			@Override
			public void showNotify() {
				CpuUsageManager.register(CpuUsagePanel.this);
			}

			@Override
			public void hideNotify() {
				CpuUsageManager.unregister(CpuUsagePanel.this);
			}
		});

	}

	@Override
	public void dispose() {
		CpuUsageManager.unregister(CpuUsagePanel.this);
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
		Image bufferedImage = myBufferedImage;

		if (bufferedImage == null || stateChanged) {
			final Dimension size = getSize();
			final Insets insets = getInsets();

			bufferedImage = UIUtil.createImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g2 = (Graphics2D) bufferedImage.getGraphics().create();

			final int max = 100;
			final int otherProcesses = CpuUsageManager.system - CpuUsageManager.process;

			final int totalBarLength = size.width - insets.left - insets.right + 1;
			final int processUsageBarLength = totalBarLength * CpuUsageManager.process / max;
			final int otherProcessesUsageBarLength = totalBarLength * otherProcesses / max;
			final int barHeight = Math.max(size.height, getFont().getSize() + 2);
			final int yOffset = (size.height - barHeight) / 2;
			final int xOffset = insets.left;

			// background
			g2.setColor(UIUtil.getPanelBackground());
			g2.fillRect(0, 0, size.width, size.height);

			// gauge (ide)
			g2.setColor(IDE);
			g2.fillRect(xOffset, yOffset, processUsageBarLength, barHeight);

			// gauge (system)
			g2.setColor(SYSTEM);
			g2.fillRect(xOffset + processUsageBarLength, yOffset, otherProcessesUsageBarLength, barHeight);

			// label
			g2.setFont(getFont());
//			final String info = CpuUsageBundle.message("cpu.usage.panel.message.text", CpuUsageManager.process,
//					CpuUsageManager.system);
			final String info = fixedLengthString(String.valueOf(CpuUsageManager.process), 3) + "% / " + fixedLengthString(String.valueOf(CpuUsageManager.system), 3) + "%";
			
			final FontMetrics fontMetrics = g.getFontMetrics();
			final int infoWidth = fontMetrics.charsWidth(info.toCharArray(), 0, info.length());
			final int infoHeight = fontMetrics.getAscent();
			UISettings.setupAntialiasing(g2);

			final Color fg = pressed ? UIUtil.getLabelDisabledForeground() : JBColor.foreground();
			g2.setColor(fg);
			g2.drawString(info, xOffset + (totalBarLength - infoWidth) / 2,
					yOffset + infoHeight + (barHeight - infoHeight) / 2 - 1);

			// border
			g2.setStroke(new BasicStroke(1));
			g2.setColor(JBColor.GRAY);
			g2.drawRect(1, 0, size.width - 2, size.height - 1);

			g2.dispose();
			myBufferedImage = bufferedImage;
		}

		draw(g, bufferedImage);
	}

	/**
	 * it will probably be better synchronized, not sure
	 */
	private synchronized void draw(Graphics g, Image bufferedImage) {
		UIUtil.drawImage(g, bufferedImage, 0, 0, null);
		if (UIUtil.isRetina() && !UIUtil.isUnderDarcula()) {
			Graphics2D g2 = (Graphics2D) g.create(0, 0, getWidth(), getHeight());
			g2.scale(0.5, 0.5);
			g2.setColor(UIUtil.isUnderIntelliJLaF() ? Gray.xC9 : Gray.x91);
			g2.drawLine(0, 0, 2 * getWidth(), 0);
			g2.scale(1, 1);
			g2.dispose();
		}
	}

	public static String fixedLengthString(String string, int length) {
		return String.format("%1$" + length + "s", string);
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

	public boolean update() {
		boolean painted = false;
		if (!isShowing()) {
			//noinspection ConstantConditions
			return painted;
		}

		if (CpuUsageManager.system != myLastSystem || CpuUsageManager.process != myLastProcess) {
			myLastSystem = CpuUsageManager.system;
			myLastProcess = CpuUsageManager.process;
			myBufferedImage = null;

			Graphics graphics = getGraphics();
			if (graphics != null) {
				paintComponent(graphics);
				painted = true;
			}

			setToolTipText(CpuUsageBundle.message("cpu.usage.panel.statistics.message", CpuUsageManager.process,
					CpuUsageManager.system));
		}
		return painted;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("projectName", projectName)
				.toString();
	}
}
