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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IAQCompositeScorer {
    static final String[] PARAMETERS = {"Temperature", "Noise", "CO2", "Luminosity", "Relative-Humidity"};

    public static void main(String[] args) {
        String folderPath = "C:/Users/jesus.rodriguezm/Downloads/pantallazos/June/June";
        Map<Integer, Integer> dailyScores = new TreeMap<>();

        for (String param : PARAMETERS) {
            File[] files = new File(folderPath).listFiles((dir, name) -> name.contains(param));
            if (files == null) continue;
            for (File file : files) {
                int day = extractDay(file.getName());
                double avg = calculateAverage(file);
                int score = categorize(param, avg);
                dailyScores.put(day, dailyScores.getOrDefault(day, 0) + score);
            }
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<Integer, Integer> entry : dailyScores.entrySet()) {
            String grade = scoreToGrade(entry.getValue());
            dataset.addValue(gradeToIndex(grade), "IAQ Score", String.valueOf(entry.getKey()));
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Composite IAQ Score in June",
                "Date",
                "Score",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        SymbolAxis yAxis = new SymbolAxis("Grade", new String[]{"F", "D", "C", "B", "A", "S"});
        yAxis.setTickLabelsVisible(true);
        plot.setRangeAxis(yAxis);

        JFrame frame = new JFrame("IAQ Composite Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static double calculateAverage(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            double sum = 0;
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 2) {
                    double value = Double.parseDouble(parts[1]);
                    sum += value;
                    count++;
                }
            }
            return count > 0 ? sum / count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    static int categorize(String parameter, double avg) {
        switch (parameter) {
            case "Temperature":
                if (avg <= 26) return 5;
                if (avg <= 28) return 4;
                if (avg <= 30) return 3;
                if (avg <= 32) return 2;
                if (avg <= 34) return 1;
                return 0;
            case "Noise":
                if (avg <= 30) return 5;
                if (avg <= 40) return 4;
                if (avg <= 50) return 3;
                if (avg <= 60) return 2;
                if (avg <= 70) return 1;
                return 0;
            case "CO2":
                if (avg <= 800) return 5;
                if (avg <= 1000) return 4;
                if (avg <= 1200) return 3;
                if (avg <= 1400) return 2;
                if (avg <= 1600) return 1;
                return 0;
            case "Luminosity":
                if (avg >= 500) return 5;
                if (avg >= 400) return 4;
                if (avg >= 300) return 3;
                if (avg >= 200) return 2;
                if (avg >= 100) return 1;
                return 0;
            case "Relative-Humidity":
                if (avg >= 40 && avg <= 60) return 5;
                if ((avg >= 35 && avg < 40) || (avg > 60 && avg <= 65)) return 4;
                if ((avg >= 30 && avg < 35) || (avg > 65 && avg <= 70)) return 3;
                if ((avg >= 25 && avg < 30) || (avg > 70 && avg <= 75)) return 2;
                if ((avg >= 20 && avg < 25) || (avg > 75 && avg <= 80)) return 1;
                return 0;
            default:
                return 0;
        }
    }

    static int extractDay(String fileName) {
        Matcher matcher = Pattern.compile("june-(\\d+)").matcher(fileName);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    static String scoreToGrade(int score) {
        if (score >= 24) return "S";
        if (score >= 20) return "A";
        if (score >= 15) return "B";
        if (score >= 10) return "C";
        if (score >= 5) return "D";
        return "F";
    }

    static int gradeToIndex(String grade) {
        switch (grade) {
            case "S": return 5;
            case "A": return 4;
            case "B": return 3;
            case "C": return 2;
            case "D": return 1;
            default: return 0;
        }
    }
}