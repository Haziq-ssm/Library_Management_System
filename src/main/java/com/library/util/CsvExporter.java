package com.library.util;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Handles exporting table models and reports to clean CSV files.
 */
public class CsvExporter {

    public static boolean exportTableToCsv(JTable table, Component parent, String defaultFileName) {
        if (table == null || table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent, "No data available to export.", "Empty Export", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export to CSV");
        fileChooser.setSelectedFile(new File(defaultFileName.endsWith(".csv") ? defaultFileName : defaultFileName + ".csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(parent);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileToSave), StandardCharsets.UTF_8))) {
            // Write BOM for Excel UTF-8 compatibility
            writer.write('\ufeff');

            TableModel model = table.getModel();
            int colCount = model.getColumnCount();

            // Write Headers
            for (int i = 0; i < colCount; i++) {
                String colName = model.getColumnName(i);
                if ("Actions".equalsIgnoreCase(colName)) continue; // Skip action button columns
                writer.write(escapeCsv(colName));
                if (i < colCount - 1 && !"Actions".equalsIgnoreCase(model.getColumnName(i + 1))) {
                    writer.write(",");
                }
            }
            writer.newLine();

            // Write Rows
            int rowCount = model.getRowCount();
            for (int r = 0; r < rowCount; r++) {
                for (int c = 0; c < colCount; c++) {
                    String colName = model.getColumnName(c);
                    if ("Actions".equalsIgnoreCase(colName)) continue;
                    Object value = model.getValueAt(r, c);
                    writer.write(escapeCsv(value != null ? value.toString() : ""));
                    if (c < colCount - 1 && !"Actions".equalsIgnoreCase(model.getColumnName(c + 1))) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }

            JOptionPane.showMessageDialog(parent,
                    "Data successfully exported to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            return true;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "Failed to export CSV: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "\"\"";
        String s = value.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\n") || s.contains("\r") || s.contains("\"")) {
            return "\"" + s + "\"";
        }
        return "\"" + s + "\"";
    }
}
