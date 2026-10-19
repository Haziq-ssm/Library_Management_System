package com.library.ui.components;

import com.library.ui.theme.ThemeColors;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

public class RoundedPanel extends JPanel {
    private int cornerRadius = 10;
    private Color backgroundColor = ThemeColors.BG_CARD;
    private Color borderColor = ThemeColors.BORDER;
    private boolean showBorder = true;

    public RoundedPanel() {
        super();
        setOpaque(false);
    }

    public RoundedPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    public RoundedPanel(int radius) {
        super();
        this.cornerRadius = Math.min(radius, 10);
        setOpaque(false);
    }

    public RoundedPanel(int radius, Color bg) {
        super();
        this.cornerRadius = Math.min(radius, 10);
        this.backgroundColor = bg;
        setOpaque(false);
    }

    public RoundedPanel(int radius, Color bg, Color border) {
        super();
        this.cornerRadius = Math.min(radius, 10);
        this.backgroundColor = bg;
        this.borderColor = border;
        this.showBorder = border != null;
        setOpaque(false);
    }

    public void setBackgroundColor(Color bg) {
        this.backgroundColor = bg;
        repaint();
    }

    public void setBorderColor(Color border) {
        this.borderColor = border;
        this.showBorder = border != null;
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = Math.min(radius, 10);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Fill background
        if (backgroundColor != null) {
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        // Draw border
        if (showBorder && borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
