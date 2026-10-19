package com.library.ui.components;

import com.library.ui.theme.ThemeColors;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class BadgeLabel extends JLabel {
    private Color badgeBackground = ThemeColors.BADGE_SUCCESS_BG;
    private Color badgeForeground = ThemeColors.BADGE_SUCCESS_FG;
    private int cornerRadius = 12;

    public BadgeLabel(String text) {
        super(text, JLabel.CENTER);
        init(text);
    }

    public BadgeLabel(String text, Color bg, Color fg) {
        super(text, JLabel.CENTER);
        this.badgeBackground = bg;
        this.badgeForeground = fg;
        init(null);
    }

    private void init(String text) {
        setOpaque(false);
        setFont(ThemeColors.FONT_BADGE);
        setBorder(new EmptyBorder(4, 10, 4, 10));
        if (text != null) {
            setBadgeType(text);
        } else {
            setForeground(badgeForeground);
        }
    }

    public void setBadgeType(String status) {
        setText(status);
        if (status == null) return;
        switch (status.trim().toLowerCase()) {
            case "available":
            case "active":
            case "paid":
            case "returned":
                badgeBackground = ThemeColors.BADGE_SUCCESS_BG;
                badgeForeground = ThemeColors.BADGE_SUCCESS_FG;
                break;

            case "issued":
            case "pending":
            case "partial":
            case "low stock":
                badgeBackground = ThemeColors.BADGE_WARNING_BG;
                badgeForeground = ThemeColors.BADGE_WARNING_FG;
                break;

            case "inactive":
            case "overdue":
            case "lost/damaged":
                badgeBackground = ThemeColors.BADGE_DANGER_BG;
                badgeForeground = ThemeColors.BADGE_DANGER_FG;
                break;

            case "student":
            case "faculty":
            case "staff":
                badgeBackground = ThemeColors.BADGE_INFO_BG;
                badgeForeground = ThemeColors.BADGE_INFO_FG;
                break;

            default:
                badgeBackground = ThemeColors.BG_INPUT;
                badgeForeground = ThemeColors.TEXT_MUTED;
                break;
        }
        setForeground(badgeForeground);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(badgeBackground);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width + 10, 70), Math.max(d.height, 22));
    }
}
