package com.library.ui.components;

import com.library.ui.theme.ThemeColors;

import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class RoundedTextField extends JTextField {
    private String placeholder = "";
    private Icon leadingIcon = null;
    private int cornerRadius = 10;
    private Color normalBorder = ThemeColors.BORDER;
    private Color focusBorder = ThemeColors.PRIMARY;
    private boolean isFocused = false;

    public RoundedTextField() {
        this("", 15);
    }

    public RoundedTextField(int columns) {
        this("", columns);
    }

    public RoundedTextField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
        init();
    }

    private void init() {
        setOpaque(false);
        setFont(ThemeColors.FONT_BODY);
        setForeground(ThemeColors.TEXT_DARK);
        setCaretColor(ThemeColors.PRIMARY);
        updateBorderInsets();

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    private void updateBorderInsets() {
        int leftPad = (leadingIcon != null) ? 36 : 12;
        setBorder(new EmptyBorder(8, leftPad, 8, 12));
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public void setLeadingIcon(Icon icon) {
        this.leadingIcon = icon;
        updateBorderInsets();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw a single anti-aliased rounded surface. Insetting the outline by
        // half its stroke width keeps all four corners equally smooth.
        g2.setColor(ThemeColors.BG_INPUT);
        g2.fill(new java.awt.geom.RoundRectangle2D.Float(0.5f, 0.5f,
                Math.max(0, width - 1.0f), Math.max(0, height - 1.0f), cornerRadius, cornerRadius));
        float strokeWidth = isFocused ? 1.6f : 1.0f;
        g2.setColor(isFocused ? focusBorder : normalBorder);
        g2.setStroke(new java.awt.BasicStroke(strokeWidth, java.awt.BasicStroke.CAP_ROUND,
                java.awt.BasicStroke.JOIN_ROUND));
        float inset = strokeWidth / 2.0f;
        g2.draw(new java.awt.geom.RoundRectangle2D.Float(inset, inset,
                Math.max(0, width - strokeWidth - 1), Math.max(0, height - strokeWidth - 1),
                cornerRadius, cornerRadius));

        // Draw leading icon
        if (leadingIcon != null) {
            int iconY = (height - leadingIcon.getIconHeight()) / 2;
            leadingIcon.paintIcon(this, g2, 10, iconY);
        }

        // Draw placeholder
        if (getText().isEmpty() && !placeholder.isEmpty() && !isFocused) {
            g2.setColor(ThemeColors.TEXT_LIGHT);
            g2.setFont(getFont());
            int textY = (height - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            int textX = (leadingIcon != null) ? 36 : 12;
            g2.drawString(placeholder, textX, textY);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
