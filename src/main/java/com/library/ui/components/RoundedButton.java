package com.library.ui.components;

import com.library.ui.theme.ThemeColors;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {
    private Color normalColor = ThemeColors.PRIMARY;
    private Color hoverColor = ThemeColors.PRIMARY_HOVER;
    private Color pressedColor = ThemeColors.PRIMARY_HOVER.darker();
    private Color textColor = Color.WHITE;
    private Color borderColor = null;
    private int cornerRadius = 10;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public RoundedButton(String text) {
        super(text);
        init();
    }

    public RoundedButton(String text, boolean outline) {
        super(text);
        if (outline) {
            this.normalColor = ThemeColors.BG_CARD;
            this.hoverColor = ThemeColors.PRIMARY_LIGHT;
            this.textColor = ThemeColors.PRIMARY;
            this.borderColor = ThemeColors.PRIMARY;
        }
        init();
    }

    public RoundedButton(String text, Color normalColor, Color hoverColor, Color textColor) {
        super(text);
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
        this.textColor = textColor;
        init();
    }

    private void init() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(textColor);
        setFont(ThemeColors.FONT_BODY_BOLD);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setColors(Color normal, Color hover, Color text) {
        this.normalColor = normal;
        this.hoverColor = hover;
        this.textColor = text;
        setForeground(text);
        repaint();
    }

    public void setBorderColor(Color border) {
        this.borderColor = border;
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color bg = normalColor;
        if (!isEnabled()) {
            bg = ThemeColors.BORDER;
            g2.setColor(ThemeColors.TEXT_MUTED);
        } else if (isPressed) {
            bg = pressedColor;
        } else if (isHovered) {
            bg = hoverColor;
        }

        g2.setColor(bg);
        g2.fill(new java.awt.geom.RoundRectangle2D.Float(0.5f, 0.5f,
                Math.max(0, width - 1.0f), Math.max(0, height - 1.0f), cornerRadius, cornerRadius));

        if (borderColor != null) {
            // Keep outline thickness and corner antialiasing consistent with input fields.
            float strokeWidth = 1.2f;
            g2.setColor(isHovered ? borderColor.darker() : borderColor);
            g2.setStroke(new java.awt.BasicStroke(strokeWidth, java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            float inset = strokeWidth / 2.0f;
            g2.draw(new java.awt.geom.RoundRectangle2D.Float(inset, inset,
                    Math.max(0, width - strokeWidth - 1), Math.max(0, height - strokeWidth - 1),
                    cornerRadius, cornerRadius));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
