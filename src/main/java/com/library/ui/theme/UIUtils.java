package com.library.ui.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Provides pixel-perfect vector icons and UI drawing utilities matching the design template.
 */
public class UIUtils {

    public static Icon createIcon(String type, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                g2.setColor(color);

                drawVectorIcon(g2, type, size);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    private static void drawVectorIcon(Graphics2D g2, String type, int size) {
        float strokeWidth = Math.max(1.5f, size / 10.0f);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type.toLowerCase()) {
            case "dashboard":
                int sq = size / 2 - 2;
                g2.drawRoundRect(1, 1, sq, sq, 3, 3);
                g2.drawRoundRect(size - sq - 2, 1, sq, sq, 3, 3);
                g2.drawRoundRect(1, size - sq - 2, sq, sq, 3, 3);
                g2.drawRoundRect(size - sq - 2, size - sq - 2, sq, sq, 3, 3);
                break;

            case "book":
                // Open book shape
                int w = size - 4;
                int h = size - 6;
                int midX = size / 2;
                g2.drawArc(2, 2, w / 2, h / 2, 0, -180);
                g2.drawArc(midX, 2, w / 2, h / 2, 0, -180);
                g2.drawLine(midX, 2, midX, size - 2);
                g2.drawLine(2, size / 4 + 1, 2, size - 3);
                g2.drawLine(size - 2, size / 4 + 1, size - 2, size - 3);
                g2.drawLine(2, size - 3, midX, size - 2);
                g2.drawLine(midX, size - 2, size - 2, size - 3);
                break;

            case "users":
            case "members":
                // Main user
                int r = size / 4;
                g2.drawOval(size / 2 - r, 2, r * 2, r * 2);
                g2.drawArc(2, size / 2 + 1, size - 4, size / 2, 0, 180);
                break;

            case "issuereturn":
            case "transaction":
                // Two horizontal looping arrows
                int arrowY1 = size / 3;
                int arrowY2 = size * 2 / 3;
                g2.drawLine(4, arrowY1, size - 4, arrowY1);
                g2.drawLine(size - 7, arrowY1 - 3, size - 4, arrowY1);
                g2.drawLine(size - 7, arrowY1 + 3, size - 4, arrowY1);

                g2.drawLine(size - 4, arrowY2, 4, arrowY2);
                g2.drawLine(7, arrowY2 - 3, 4, arrowY2);
                g2.drawLine(7, arrowY2 + 3, 4, arrowY2);
                break;

            case "category":
            case "categories":
                // Folder / tag icon
                g2.drawRoundRect(2, 4, size - 4, size - 6, 3, 3);
                g2.drawLine(2, 8, size - 2, 8);
                break;

            case "reports":
            case "chart":
                // Bar chart
                g2.drawLine(2, size - 2, size - 2, size - 2);
                g2.fillRect(4, size - size / 3, 3, size / 3 - 2);
                g2.fillRect(9, size - size * 2 / 3, 3, size * 2 / 3 - 2);
                g2.fillRect(14, 4, 3, size - 6);
                break;

            case "fines":
            case "coin":
                // Currency / coin icon
                g2.drawOval(2, 2, size - 4, size - 4);
                // Draw ₹ or $ symbol
                g2.drawString("₹", size / 4 + 1, size * 3 / 4 - 1);
                break;

            case "settings":
                // Cog wheel
                g2.drawOval(4, 4, size - 8, size - 8);
                g2.drawOval(size / 2 - 2, size / 2 - 2, 4, 4);
                g2.drawLine(size / 2, 1, size / 2, 4);
                g2.drawLine(size / 2, size - 4, size / 2, size - 1);
                g2.drawLine(1, size / 2, 4, size / 2);
                g2.drawLine(size - 4, size / 2, size - 1, size / 2);
                break;

            case "logout":
                g2.drawArc(2, 2, size - 6, size - 4, 60, 240);
                g2.drawLine(size / 2 - 1, size / 2, size - 2, size / 2);
                g2.drawLine(size - 5, size / 2 - 3, size - 2, size / 2);
                g2.drawLine(size - 5, size / 2 + 3, size - 2, size / 2);
                break;

            case "clock":
                g2.drawOval(2, 2, size - 4, size - 4);
                g2.drawLine(size / 2, size / 4, size / 2, size / 2);
                g2.drawLine(size / 2, size / 2, size * 3 / 4, size / 2);
                break;

            case "alert":
            case "warning":
                Path2D tri = new Path2D.Float();
                tri.moveTo(size / 2.0, 2.0);
                tri.lineTo(size - 2.0, size - 2.0);
                tri.lineTo(2.0, size - 2.0);
                tri.closePath();
                g2.draw(tri);
                g2.drawLine(size / 2, size / 3, size / 2, size * 2 / 3);
                g2.fillOval(size / 2 - 1, size * 3 / 4 + 1, 2, 2);
                break;

            case "search":
                int cr = size / 3;
                g2.drawOval(2, 2, cr * 2, cr * 2);
                g2.drawLine(cr * 2 - 1, cr * 2 - 1, size - 2, size - 2);
                break;

            case "bell":
                g2.drawArc(4, 4, size - 8, size - 6, 0, 180);
                g2.drawLine(2, size - 4, size - 2, size - 4);
                g2.drawLine(4, size / 2, 2, size - 4);
                g2.drawLine(size - 4, size / 2, size - 2, size - 4);
                g2.fillOval(size / 2 - 2, size - 3, 4, 3);
                break;

            case "plus":
                g2.drawLine(size / 2, 3, size / 2, size - 3);
                g2.drawLine(3, size / 2, size - 3, size / 2);
                break;

            case "edit":
            case "pencil":
                g2.drawLine(3, size - 3, 3, size - 6);
                g2.drawLine(3, size - 6, size - 5, 2);
                g2.drawLine(size - 5, 2, size - 2, 5);
                g2.drawLine(size - 2, 5, 6, size - 3);
                g2.drawLine(6, size - 3, 3, size - 3);
                break;

            case "trash":
            case "delete":
                g2.drawLine(3, 4, size - 3, 4);
                g2.drawRoundRect(5, 5, size - 10, size - 7, 2, 2);
                g2.drawLine(size / 2 - 1, 6, size / 2 - 1, size - 4);
                g2.drawLine(size / 2 + 2, 6, size / 2 + 2, size - 4);
                break;

            case "user":
                int ur = size / 3;
                g2.drawOval(size / 2 - ur / 2, 2, ur, ur);
                g2.drawArc(3, size / 2, size - 6, size / 2, 0, 180);
                break;

            case "lock":
                g2.drawRoundRect(3, size / 3 + 1, size - 6, size * 2 / 3 - 3, 3, 3);
                g2.drawArc(size / 4 + 1, 2, size / 2 - 2, size / 2, 0, 180);
                g2.fillOval(size / 2 - 1, size * 2 / 3 - 2, 3, 3);
                break;

            default:
                g2.drawOval(2, 2, size - 4, size - 4);
                break;
        }
    }
}
