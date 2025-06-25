package iaqplotter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JuneCO2IAQPlotter {
    public static void main(String[] args) {
        String folderPath = "C:/Users/jesus.rodriguezm/Downloads/pantallazos/June/June";
        File folder = new File(folderPath);
        File[] files = folder.listFiles((dir, name) -> name.contains("CO2"));

        if (files == null || files.length == 0) {
            System.out.println("No CO2 files found.");
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
                double dailyAvg = dailySum / count;
                int rating = mapCO2ToIAQ(dailyAvg);
                dataset.addValue(rating, "CO2", "" + day);
            }
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Daily CO₂ IAQ Grade (June)",
                "Date",
                "IAQ Grade",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        CategoryPlot plot = lineChart.getCategoryPlot();
        SymbolAxis gradeAxis = new SymbolAxis("IAQ Grade", new String[]{"F", "D", "C", "B", "A", "S"});
        gradeAxis.setTickLabelsVisible(true);
        plot.setRangeAxis(gradeAxis);

        JFrame frame = new JFrame("CO₂ IAQ Ratings");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(lineChart));
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static int mapCO2ToIAQ(double value) {
        if (value < 600) return 5;       // S
        else if (value < 800) return 4;  // A
        else if (value < 1000) return 3; // B
        else if (value < 1200) return 2; // C
        else if (value < 1500) return 1; // D
        else return 0;                   // F
    }

    private static int extractDay(String fileName) {
        Pattern pattern = Pattern.compile("june-(\\d+)");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}
