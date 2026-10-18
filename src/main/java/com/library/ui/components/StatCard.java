package com.library.ui.components;

import com.library.ui.theme.ThemeColors;
import com.library.ui.theme.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatCard extends RoundedPanel {

    private JLabel valLabel;
    private JLabel titleLabel;

    public StatCard(String title, String initialValue, String iconType) {
        super(14, ThemeColors.BG_CARD, ThemeColors.BORDER);
        setLayout(new BorderLayout(15, 0));
        setBorder(new EmptyBorder(16, 18, 16, 18));
        setPreferredSize(new Dimension(210, 84));

        // Circular icon container on left
        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeColors.PRIMARY_LIGHT);
                g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                g2.dispose();
            }
        };
        iconContainer.setOpaque(false);
        iconContainer.setPreferredSize(new Dimension(48, 48));
        iconContainer.setLayout(new GridBagLayout());

        Icon icon = UIUtils.createIcon(iconType, 22, ThemeColors.PRIMARY);
        JLabel iconLabel = new JLabel(icon);
        iconContainer.add(iconLabel);

        add(iconContainer, BorderLayout.WEST);

        // Text details on right
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeColors.FONT_SUBTEXT);
        titleLabel.setForeground(ThemeColors.TEXT_MUTED);

        valLabel = new JLabel(initialValue);
        valLabel.setFont(ThemeColors.FONT_CARD_VALUE);
        valLabel.setForeground(ThemeColors.TEXT_DARK);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(valLabel);
        textPanel.add(Box.createVerticalGlue());

        add(textPanel, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valLabel.setText(value);
        repaint();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
        repaint();
    }
}
