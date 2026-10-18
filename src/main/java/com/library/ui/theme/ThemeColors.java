package com.library.ui.theme;

import java.awt.Color;
import java.awt.Font;

public class ThemeColors {
    // Primary Orange Accent
    public static final Color PRIMARY = new Color(255, 107, 0);          // #FF6B00
    public static final Color PRIMARY_HOVER = new Color(230, 90, 0);     // #E65A00
    public static final Color PRIMARY_LIGHT = new Color(255, 243, 235);   // #FFF3EB
    public static final Color PRIMARY_BORDER = new Color(255, 204, 170); // #FFCCAA

    // Layout Backgrounds
    public static final Color BG_MAIN = new Color(248, 250, 252);        // #F8FAFC
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_SIDEBAR = Color.WHITE;
    public static final Color BG_TOPBAR = Color.WHITE;
    public static final Color BG_INPUT = new Color(249, 250, 251);        // #F9FAFB

    // Borders & Dividers
    public static final Color BORDER = new Color(229, 231, 235);          // #E5E7EB
    public static final Color BORDER_FOCUS = new Color(255, 107, 0);
    public static final Color DIVIDER = new Color(243, 244, 246);

    // Typography
    public static final Color TEXT_DARK = new Color(30, 41, 59);          // #1E293B
    public static final Color TEXT_MUTED = new Color(100, 116, 139);      // #64748B
    public static final Color TEXT_LIGHT = new Color(148, 163, 184);      // #94A3B8
    public static final Color TEXT_WHITE = Color.WHITE;

    // Badges & Alerts
    public static final Color BADGE_SUCCESS_BG = new Color(222, 247, 236); // #DEF7EC
    public static final Color BADGE_SUCCESS_FG = new Color(3, 84, 63);     // #03543F

    public static final Color BADGE_WARNING_BG = new Color(254, 236, 220); // #FEECDC
    public static final Color BADGE_WARNING_FG = new Color(194, 65, 12);   // #C2410C

    public static final Color BADGE_DANGER_BG = new Color(253, 232, 232);  // #FDE8E8
    public static final Color BADGE_DANGER_FG = new Color(155, 28, 28);    // #9B1C1C

    public static final Color BADGE_INFO_BG = new Color(225, 239, 254);    // #E1EFFE
    public static final Color BADGE_INFO_FG = new Color(30, 66, 159);      // #1E429F

    // Chart Colors
    public static final Color CHART_ISSUED = new Color(255, 107, 0);       // #FF6B00
    public static final Color CHART_RETURNED = new Color(255, 180, 128);   // Soft peach/orange
    public static final Color CHART_GRID = new Color(241, 245, 249);

    // Standard Fonts
    public static final Font FONT_HEADER_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SECTION_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_CARD_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_CARD_VALUE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SUBTEXT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);
}
