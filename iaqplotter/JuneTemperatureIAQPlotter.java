package iaqplotter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JuneTemperatureIAQPlotter {

    private static final String[] IAQ_LEVELS = {"F", "D", "C", "B", "A", "S"};

    public static void main(String[] args) {
        String folderPath = "C:/Users/jesus.rodriguezm/Downloads/pantallazos/June/June";
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.contains("Temperature"));

        if (files == null || files.length == 0) {
            System.out.println("No temperature files found.");
            return;
        }

        Arrays.sort(files, (f1, f2) -> extractDay(f1.getName()) - extractDay(f2.getName()));
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (File file : files) {
            int day = extractDay(file.getName());
            double dailySum = 0;
            int count = 0;

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    try {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length == 2) {
                            double value = Double.parseDouble(parts[1]);
                            dailySum += value;
                            count++;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid line in " + file.getName() + ": " + line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (count > 0) {
                double avgTemp = dailySum / count;
                String iaqLevel = classifyTemperature(avgTemp);
                dataset.addValue(mapIAQToNumeric(iaqLevel), "IAQ Grade", "Day " + day);
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "IAQ Grade Based on Daily Temperature",
                "Day of June",
                "IAQ Grade",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        // Replace Y-axis with custom symbol axis for grades
        CategoryPlot plot = chart.getCategoryPlot();
        SymbolAxis gradeAxis = new SymbolAxis("IAQ Grade", IAQ_LEVELS);
        gradeAxis.setRange(-0.5, 5.5);
        gradeAxis.setTickLabelFont(new Font("SansSerif", Font.BOLD, 12));
        plot.setRangeAxis(gradeAxis);
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        JFrame frame = new JFrame("IAQ Grade Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static int extractDay(String fileName) {
        Pattern pattern = Pattern.compile("june-(\\d+)");
        Matcher matcher = pattern.matcher(fileName.toLowerCase());
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static String classifyTemperature(double temp) {
        if (temp >= 22 && temp < 24) return "S";
        else if (temp < 26) return "A";
        else if (temp < 28) return "B";
        else if (temp < 30) return "C";
        else if (temp < 32) return "D";
        else return "F";
    }

    private static int mapIAQToNumeric(String grade) {
        switch (grade) {
            case "F": return 0;
            case "D": return 1;
            case "C": return 2;
            case "B": return 3;
            case "A": return 4;
            case "S": return 5;
            default: return -1;
        }
    }
}
