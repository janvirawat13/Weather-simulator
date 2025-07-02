import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class q extends JFrame {
    private static final String DATA_FILE = "weather_data.dat";
    private WeatherData currentData = new WeatherData();
    private final DashboardPanel dashboardPanel = new DashboardPanel();
    private final ReportPanel reportPanel = new ReportPanel();

    public q() {
        super("Professional Weather Simulator");
        setupUI();
        loadData();
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Weather Report", reportPanel);
        tabbedPane.addTab("Input Data", createInputPanel());

        add(tabbedPane);
        new Timer(50, e -> {
            dashboardPanel.repaint();
            reportPanel.repaint();
        }).start();
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"Temperature (°C):", "Humidity (%):", "Wind Speed (km/h):", 
                          "Precipitation (mm):", "Pressure (hPa):", "UV Index:"};
        JTextField[] fields = new JTextField[labels.length];
        JCheckBox[] checkboxes = new JCheckBox[labels.length];

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("<html><b>Weather Parameters:</b></html>"), gbc);

        for (int i = 0; i < labels.length; i++) {
            final int idx = i;  // Final copy for lambda capture
            
            gbc.gridy = i + 1;
            gbc.gridx = 0;
            panel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            fields[i] = new JTextField(10);
            panel.add(fields[i], gbc);

            gbc.gridx = 2;
            checkboxes[i] = new JCheckBox("N/A");
            checkboxes[i].addItemListener(e -> 
                fields[idx].setEnabled(!checkboxes[idx].isSelected())
            );
            panel.add(checkboxes[i], gbc);
        }

        gbc.gridy = labels.length + 1;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton saveBtn = new JButton("Save Data");
        saveBtn.setBackground(new Color(70, 130, 180));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveBtn.addActionListener(e -> saveData(fields, checkboxes));
        panel.add(saveBtn, gbc);

        gbc.gridy++;
        JButton randomBtn = new JButton("Generate Random Data");
        randomBtn.addActionListener(e -> generateRandomData(fields, checkboxes));
        panel.add(randomBtn, gbc);

        return panel;
    }

    private void saveData(JTextField[] fields, JCheckBox[] checkboxes) {
        try {
            currentData = new WeatherData();
            if (!checkboxes[0].isSelected()) currentData.temperature = parseDouble(fields[0].getText(), -50, 50);
            if (!checkboxes[1].isSelected()) currentData.humidity = parseDouble(fields[1].getText(), 0, 100);
            if (!checkboxes[2].isSelected()) currentData.windSpeed = parseDouble(fields[2].getText(), 0, 200);
            if (!checkboxes[3].isSelected()) currentData.precipitation = parseDouble(fields[3].getText(), 0, 500);
            if (!checkboxes[4].isSelected()) currentData.pressure = parseDouble(fields[4].getText(), 800, 1100);
            if (!checkboxes[5].isSelected()) currentData.uvIndex = parseInt(fields[5].getText(), 0, 11);
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
                oos.writeObject(currentData);
            }
            JOptionPane.showMessageDialog(this, "Data saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateRandomData(JTextField[] fields, JCheckBox[] checkboxes) {
        Random rand = new Random();
        if (!checkboxes[0].isSelected()) fields[0].setText(String.format("%.1f", rand.nextDouble() * 50 - 10));
        if (!checkboxes[1].isSelected()) fields[1].setText(String.format("%.0f", rand.nextDouble() * 100));
        if (!checkboxes[2].isSelected()) fields[2].setText(String.format("%.1f", rand.nextDouble() * 50));
        if (!checkboxes[3].isSelected()) fields[3].setText(String.format("%.1f", rand.nextDouble() * 20));
        if (!checkboxes[4].isSelected()) fields[4].setText(String.format("%.1f", rand.nextDouble() * 200 + 900));
        if (!checkboxes[5].isSelected()) fields[5].setText(String.format("%d", rand.nextInt(11)));
    }

    private double parseDouble(String text, double min, double max) {
        double value = Double.parseDouble(text);
        if (value < min || value > max) 
            throw new NumberFormatException("Value must be between " + min + " and " + max);
        return value;
    }

    private int parseInt(String text, int min, int max) {
        int value = Integer.parseInt(text);
        if (value < min || value > max) 
            throw new NumberFormatException("Value must be between " + min + " and " + max);
        return value;
    }

    private void loadData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                currentData = (WeatherData) ois.readObject();
            } catch (Exception e) {
                System.err.println("Error loading data: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new q().setVisible(true));
    }

    // Weather data model
    private static class WeatherData implements Serializable {
        private static final long serialVersionUID = 1L;
        Double temperature = 20.0;
        Double humidity = 60.0;
        Double windSpeed = 10.0;
        Double precipitation = 0.0;
        Double pressure = 1013.0;
        Integer uvIndex = 3;
        final Date timestamp = new Date();
    }

    // Animated dashboard panel
    private class DashboardPanel extends JPanel {
        private float sunPosition = 0.3f;
        private boolean sunDirection = true;
        private final ArrayList<Cloud> clouds = new ArrayList<>();
        private final ArrayList<RainDrop> raindrops = new ArrayList<>();
        private final GradientPaint[] skyGradients = {
            new GradientPaint(0, 0, new Color(25, 118, 210), 0, 400, new Color(3, 169, 244)),
            new GradientPaint(0, 0, new Color(66, 66, 66), 0, 400, new Color(189, 189, 189)),
            new GradientPaint(0, 0, new Color(2, 119, 189), 0, 400, new Color(41, 182, 246))
        };

        public DashboardPanel() {
            setBackground(new Color(240, 248, 255));
            // Initialize clouds
            for (int i = 0; i < 5; i++) {
                clouds.add(new Cloud());
            }
            // Start animation timer
            new Timer(50, e -> animate()).start();
        }

        private void animate() {
            // Animate sun
            if (sunDirection) {
                sunPosition += 0.001f;
                if (sunPosition > 0.7f) sunDirection = false;
            } else {
                sunPosition -= 0.001f;
                if (sunPosition < 0.3f) sunDirection = true;
            }

            // Animate clouds
            for (Cloud cloud : clouds) {
                cloud.x += cloud.speed;
                if (cloud.x > getWidth()) {
                    cloud.x = -100;
                    cloud.y = 50 + (int)(Math.random() * 150);
                }
            }

            // Animate raindrops
            if (currentData.precipitation != null && currentData.precipitation > 0) {
                if (raindrops.size() < 100) {
                    raindrops.add(new RainDrop());
                }
                for (Iterator<RainDrop> it = raindrops.iterator(); it.hasNext();) {
                    RainDrop drop = it.next();
                    drop.y += drop.speed;
                    if (drop.y > getHeight()) it.remove();
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw animated sky background
            int skyIndex = (currentData.precipitation != null && currentData.precipitation > 0) ? 1 : 
                          (currentData.temperature != null && currentData.temperature > 25) ? 0 : 2;
            g2d.setPaint(skyGradients[skyIndex]);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Draw animated sun
            drawSun(g2d);

            // Draw animated clouds
            drawClouds(g2d);

            // Draw animated rain
            if (currentData.precipitation != null && currentData.precipitation > 0) {
                drawRain(g2d);
            }

            // Draw weather data
            drawWeatherData(g2d);
        }

        private void drawSun(Graphics2D g2d) {
            int sunSize = 80;
            int sunX = (int) (getWidth() * sunPosition);
            int sunY = 100;

            RadialGradientPaint sunGradient = new RadialGradientPaint(
                sunX, sunY, sunSize,
                new float[]{0.1f, 0.9f},
                new Color[]{Color.YELLOW, new Color(255, 165, 0)}
            );

            g2d.setPaint(sunGradient);
            g2d.fillOval(sunX - sunSize/2, sunY - sunSize/2, sunSize, sunSize);
        }

        private void drawClouds(Graphics2D g2d) {
            g2d.setColor(new Color(255, 255, 255, 200));
            for (Cloud cloud : clouds) {
                g2d.fill(cloud.getShape());
            }
        }

        private void drawRain(Graphics2D g2d) {
            g2d.setColor(new Color(200, 200, 255, 150));
            for (RainDrop drop : raindrops) {
                g2d.drawLine(drop.x, drop.y, drop.x, drop.y + 10);
            }
        }

        private void drawWeatherData(Graphics2D g2d) {
            int panelHeight = getHeight() - 250;
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRoundRect(50, panelHeight, getWidth() - 100, 200, 20, 20);

            g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Current Weather Conditions", 70, panelHeight + 40);

            g2d.setFont(new Font("SansSerif", Font.PLAIN, 18));
            int yPos = panelHeight + 80;
            if (currentData.temperature != null) 
                drawDataPoint(g2d, "Temperature: ", String.format("%.1f°C", currentData.temperature), 70, yPos);
            if (currentData.humidity != null) 
                drawDataPoint(g2d, "Humidity: ", String.format("%.0f%%", currentData.humidity), 350, yPos);
            if (currentData.windSpeed != null) 
                drawDataPoint(g2d, "Wind Speed: ", String.format("%.1f km/h", currentData.windSpeed), 600, yPos);
            
            yPos += 40;
            if (currentData.precipitation != null) 
                drawDataPoint(g2d, "Precipitation: ", String.format("%.1f mm", currentData.precipitation), 70, yPos);
            if (currentData.pressure != null) 
                drawDataPoint(g2d, "Pressure: ", String.format("%.1f hPa", currentData.pressure), 350, yPos);
            if (currentData.uvIndex != null) 
                drawDataPoint(g2d, "UV Index: ", currentData.uvIndex.toString(), 600, yPos);
        }

        private void drawDataPoint(Graphics2D g2d, String label, String value, int x, int y) {
            g2d.setColor(new Color(200, 200, 255));
            g2d.drawString(label, x, y);
            g2d.setColor(Color.WHITE);
            g2d.drawString(value, x + g2d.getFontMetrics().stringWidth(label), y);
        }
    }

    // Professional report panel
    private class ReportPanel extends JPanel {
        private float animationProgress = 0;
        private final Timer animationTimer;
        private final int[] maxValues = {50, 100, 100, 20, 200, 11};

        public ReportPanel() {
            animationTimer = new Timer(20, e -> {
                animationProgress = Math.min(1.0f, animationProgress + 0.02f);
                repaint();
                if (animationProgress >= 1.0f) {
                    ((Timer)e.getSource()).stop();
                }
            });

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent e) {
                    startAnimation();
                }
            });
        }

        public void startAnimation() {
            animationProgress = 0;
            if (animationTimer.isRunning()) {
                animationTimer.stop();
            }
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw professional background
            drawReportBackground(g2d);

            // Draw title
            g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
            g2d.setColor(new Color(30, 30, 70));
            String title = "Professional Weather Analysis Report";
            g2d.drawString(title, (getWidth() - g2d.getFontMetrics().stringWidth(title)) / 2, 50);

            // Draw timestamp
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentData.timestamp);
            g2d.drawString("Generated: " + time, getWidth() - 250, 30);

            // Draw charts
            drawCharts(g2d);
        }

        private void drawReportBackground(Graphics2D g2d) {
            g2d.setColor(new Color(245, 248, 250));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Draw watermark
            g2d.setColor(new Color(230, 240, 255));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 120));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            g2d.drawString("WEATHER", 100, getHeight() / 2);
        }

        private void drawCharts(Graphics2D g2d) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            int chartWidth = 100;
            int spacing = 50;
            int baseX = 100;
            int baseY = 400;
            int maxBarHeight = 200;

            String[] labels = {"Temp", "Humidity", "Wind", "Rain", "Pressure", "UV"};
            Double[] values = {
                currentData.temperature, 
                currentData.humidity,
                currentData.windSpeed,
                currentData.precipitation,
                currentData.pressure != null ? currentData.pressure - 900 : null,
                currentData.uvIndex != null ? currentData.uvIndex.doubleValue() : null
            };

            g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
            for (int i = 0; i < labels.length; i++) {
                int x = baseX + i * (chartWidth + spacing);
                
                // Draw chart background
                g2d.setColor(new Color(220, 230, 240));
                g2d.fillRect(x, 150, chartWidth, maxBarHeight);
                
                // Draw animated bar
                if (values[i] != null) {
                    int barHeight = (int) (maxBarHeight * (values[i] / maxValues[i]) * animationProgress);
                    Color barColor = getChartColor(i);
                    g2d.setColor(barColor);
                    g2d.fillRect(x, 150 + maxBarHeight - barHeight, chartWidth, barHeight);
                    
                    // Draw value text
                    g2d.setColor(Color.BLACK);
                    String valText = (i == 3 || i == 4) ? String.format("%.1f", values[i]) : String.format("%.0f", values[i]);
                    int textWidth = g2d.getFontMetrics().stringWidth(valText);
                    g2d.drawString(valText, x + (chartWidth - textWidth)/2, 130 + maxBarHeight - barHeight);
                }
                
                // Draw label
                g2d.setColor(new Color(50, 50, 50));
                g2d.drawString(labels[i], x + (chartWidth - g2d.getFontMetrics().stringWidth(labels[i]))/2, baseY);
            }
            
            // Draw analysis report
            drawAnalysisReport(g2d, baseY + 50);
        }

        private Color getChartColor(int index) {
            Color[] colors = {
                new Color(219, 68, 55),    // Temperature (red)
                new Color(15, 157, 88),    // Humidity (green)
                new Color(66, 133, 244),   // Wind (blue)
                new Color(171, 71, 188),   // Precipitation (purple)
                new Color(249, 171, 0),    // Pressure (orange)
                new Color(244, 67, 54)     // UV (red)
            };
            return colors[index];
        }

        private void drawAnalysisReport(Graphics2D g2d, int y) {
            g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
            g2d.setColor(new Color(30, 30, 70));
            g2d.drawString("Meteorological Analysis:", 100, y);
            
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
            y += 30;
            
            // Generate professional analysis based on data
            java.util.List<String> analysis = generateAnalysis();
            for (String line : analysis) {
                g2d.drawString("- " + line, 120, y);
                y += 25;
            }
        }

        private java.util.List<String> generateAnalysis() {
            java.util.List<String> analysis = new ArrayList<>();
            
            if (currentData.temperature != null) {
                if (currentData.temperature > 30) analysis.add("High temperature indicates potential heat stress");
                else if (currentData.temperature < 0) analysis.add("Freezing conditions require thermal protection");
                else analysis.add("Temperatures within comfortable range");
            }
            
            if (currentData.humidity != null) {
                if (currentData.humidity > 80) analysis.add("High humidity may cause discomfort");
                else if (currentData.humidity < 30) analysis.add("Low humidity may cause dehydration");
            }
            
            if (currentData.precipitation != null && currentData.precipitation > 5) {
                analysis.add("Significant precipitation observed - flood risk assessment recommended");
            }
            
            if (currentData.windSpeed != null && currentData.windSpeed > 30) {
                analysis.add("Strong winds detected - caution advised for outdoor activities");
            }
            
            if (currentData.uvIndex != null) {
                if (currentData.uvIndex > 8) analysis.add("Extreme UV radiation - skin protection essential");
                else if (currentData.uvIndex > 5) analysis.add("High UV index - sun protection recommended");
            }
            
            if (analysis.isEmpty()) analysis.add("Weather conditions appear normal with no significant anomalies");
            
            return analysis;
        }
    }

    // Animation helper classes
    private static class Cloud {
        int x = (int) (Math.random() * 500);
        int y = 50 + (int) (Math.random() * 150);
        int speed = 1 + (int) (Math.random() * 2);

        Shape getShape() {
            Ellipse2D.Double cloud = new Ellipse2D.Double(x, y, 60, 30);
            Ellipse2D.Double part1 = new Ellipse2D.Double(x + 10, y - 10, 30, 30);
            Ellipse2D.Double part2 = new Ellipse2D.Double(x + 30, y + 5, 40, 25);
            Area area = new Area(cloud);
            area.add(new Area(part1));
            area.add(new Area(part2));
            return area;
        }
    }

    private class RainDrop {
        int x = 50 + (int) (Math.random() * (dashboardPanel.getWidth() - 100));
        int y = -10;
        int speed = 5 + (int) (Math.random() * 10);
    }
}
