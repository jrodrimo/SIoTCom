package plotter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JuneTemperaturePlotter {
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
                double dailyAvg = dailySum / count;
                dataset.addValue(dailyAvg, "Temperature", "" + day);
            }
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Average Daily Temperature in June",
                "Date",
                "Temperature (Cº)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        JFrame frame = new JFrame("Temperature Graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(lineChart));
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
