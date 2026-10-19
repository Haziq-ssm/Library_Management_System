package com.library.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.util.prefs.Preferences;

public class ThemeColors {
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(ThemeColors.class);
    private static boolean darkMode = PREFERENCES.getBoolean("darkMode", false);

    // Primary Orange Accent
    public static Color PRIMARY;
    public static Color PRIMARY_HOVER;
    public static Color PRIMARY_LIGHT;
    public static Color PRIMARY_BORDER;

    // Layout Backgrounds
    public static Color BG_MAIN;
    public static Color BG_CARD;
    public static Color BG_SIDEBAR;
    public static Color BG_TOPBAR;
    public static Color BG_INPUT;

    // Borders & Dividers
    public static Color BORDER;
    public static Color BORDER_FOCUS;
    public static Color DIVIDER;

    // Typography
    public static Color TEXT_DARK;
    public static Color TEXT_MUTED;
    public static Color TEXT_LIGHT;
    public static Color TEXT_WHITE = Color.WHITE;

    // Badges & Alerts
    public static Color BADGE_SUCCESS_BG;
    public static Color BADGE_SUCCESS_FG;

    public static Color BADGE_WARNING_BG;
    public static Color BADGE_WARNING_FG;

    public static Color BADGE_DANGER_BG;
    public static Color BADGE_DANGER_FG;

    public static Color BADGE_INFO_BG;
    public static Color BADGE_INFO_FG;

    // Chart Colors
    public static Color CHART_ISSUED;
    public static Color CHART_RETURNED;
    public static Color CHART_GRID;

    // Standard Fonts
    private static final String FONT_FAMILY = "Segoe UI Variable";
    public static final Font FONT_HEADER_TITLE = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_SECTION_TITLE = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_CARD_TITLE = new Font(FONT_FAMILY, Font.BOLD, 15);
    public static final Font FONT_CARD_VALUE = new Font(FONT_FAMILY, Font.BOLD, 24);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_SUBTEXT = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_BADGE = new Font(FONT_FAMILY, Font.BOLD, 11);

    static {
        updatePalette();
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean enabled) {
        darkMode = enabled;
        PREFERENCES.putBoolean("darkMode", enabled);
        updatePalette();
    }

    private static void updatePalette() {
        PRIMARY = new Color(255, 122, 38);
        PRIMARY_HOVER = new Color(230, 101, 24);
        BORDER_FOCUS = PRIMARY;
        CHART_ISSUED = PRIMARY;
        CHART_RETURNED = new Color(255, 180, 128);

        if (darkMode) {
            PRIMARY_LIGHT = new Color(72, 47, 32);
            PRIMARY_BORDER = new Color(130, 82, 48);
            BG_MAIN = new Color(25, 28, 32);
            BG_CARD = new Color(34, 38, 43);
            BG_SIDEBAR = new Color(30, 34, 39);
            BG_TOPBAR = new Color(30, 34, 39);
            BG_INPUT = new Color(42, 47, 53);
            BORDER = new Color(79, 88, 98);
            DIVIDER = new Color(45, 50, 56);
            TEXT_DARK = new Color(231, 234, 238);
            TEXT_MUTED = new Color(170, 178, 188);
            TEXT_LIGHT = new Color(137, 147, 159);
            BADGE_SUCCESS_BG = new Color(34, 75, 59);
            BADGE_SUCCESS_FG = new Color(147, 226, 188);
            BADGE_WARNING_BG = new Color(84, 58, 35);
            BADGE_WARNING_FG = new Color(255, 194, 133);
            BADGE_DANGER_BG = new Color(83, 43, 45);
            BADGE_DANGER_FG = new Color(255, 164, 164);
            BADGE_INFO_BG = new Color(39, 59, 82);
            BADGE_INFO_FG = new Color(162, 198, 255);
            CHART_GRID = new Color(54, 60, 67);
        } else {
            PRIMARY_LIGHT = new Color(255, 243, 235);
            PRIMARY_BORDER = new Color(255, 204, 170);
            BG_MAIN = new Color(246, 248, 250);
            BG_CARD = Color.WHITE;
            BG_SIDEBAR = Color.WHITE;
            BG_TOPBAR = Color.WHITE;
            BG_INPUT = new Color(249, 250, 251);
            BORDER = new Color(205, 212, 220);
            DIVIDER = new Color(237, 240, 243);
            TEXT_DARK = new Color(34, 42, 53);
            TEXT_MUTED = new Color(98, 109, 122);
            TEXT_LIGHT = new Color(137, 149, 163);
            BADGE_SUCCESS_BG = new Color(222, 247, 236);
            BADGE_SUCCESS_FG = new Color(3, 84, 63);
            BADGE_WARNING_BG = new Color(254, 236, 220);
            BADGE_WARNING_FG = new Color(194, 65, 12);
            BADGE_DANGER_BG = new Color(253, 232, 232);
            BADGE_DANGER_FG = new Color(155, 28, 28);
            BADGE_INFO_BG = new Color(225, 239, 254);
            BADGE_INFO_FG = new Color(30, 66, 159);
            CHART_GRID = new Color(233, 237, 241);
        }
    }
}
