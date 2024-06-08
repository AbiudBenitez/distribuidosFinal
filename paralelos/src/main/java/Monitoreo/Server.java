package Monitoreo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class Server extends JFrame {

    private JPanel metricsPanel, serverInfoPanel;
    private JTable table;
    private DefaultTableModel model;
    

    public Server() {
        initializeGUI();
        updateSystemMetrics();
        JFrame frame = new JFrame("Tabla de Dispositivos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crear la tabla
        JTable table = createTable();
        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);

        // Ajustar tamaño de la ventana y hacerla visible
        frame.setSize(600, 400);
        frame.setVisible(true);

        // Crear un temporizador para actualizar la tabla cada entre 5 y 10 segundos
        Timer timer = new Timer((2 + new Random().nextInt(3)) * 600, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTable(table);
            }
        });
        timer.start();
    }

    private void initializeGUI() {
        setTitle("Server Monitoring");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(new Dimension(800, 600));

        setupTablePanel();
        setupServerInfoPanel();

        add(metricsPanel, BorderLayout.CENTER);
        add(serverInfoPanel, BorderLayout.EAST);

        setVisible(true);
    }
    
    public static JTable createTable() {
        // Datos de ejemplo para la tabla
        Object[][] data = {
                {"Irian", "25.53.178.157"},
                {"LAPTOP-DH7OK6S", "25.57.124.131"},
                {"Andree", "25.53.225.158"},
                // Agrega más filas según sea necesario
        };

        // Nombres de las columnas
        String[] columnNames = {"Nombre del Dispositivo", "IP"};

        // Modelo de la tabla
        DefaultTableModel model = new DefaultTableModel(data, columnNames);

        // Tabla
        JTable table = new JTable(model);

        return table;
    }

    public static void updateTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Random random = new Random();
        
        // Obtener el número de filas en la tabla
        int rowCount = model.getRowCount();

        // Generar un nuevo orden aleatorio para las filas
        int[] order = new int[rowCount];
        for (int i = 0; i < rowCount; i++) {
            order[i] = i;
        }
        for (int i = rowCount - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = order[index];
            order[index] = order[i];
            order[i] = temp;
        }

        // Actualizar el modelo de la tabla con el nuevo orden
        for (int i = 0; i < rowCount; i++) {
            int oldIndex = i;
            int newIndex = order[i];
            if (oldIndex != newIndex) {
                for (int j = 0; j < table.getColumnCount(); j++) {
                    Object temp = model.getValueAt(oldIndex, j);
                    model.setValueAt(model.getValueAt(newIndex, j), oldIndex, j);
                    model.setValueAt(temp, newIndex, j);
                }
            }
        }
    }

    private void setupTablePanel() {
        metricsPanel = new JPanel(new BorderLayout());
        model = new DefaultTableModel(new Object[]{"Metric", "Value"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        metricsPanel.add(scrollPane, BorderLayout.CENTER);

        model.addRow(new Object[]{"CPU Free Percentage", "Calculating..."});
        model.addRow(new Object[]{"Memory Free", "Calculating..."});
        model.addRow(new Object[]{"Disk Free Percentage", "Calculating..."});
    }

    private void setupServerInfoPanel() {
        serverInfoPanel = new JPanel();
        serverInfoPanel.setLayout(new BoxLayout(serverInfoPanel, BoxLayout.Y_AXIS));

        JLabel lblServerName = new JLabel("Nombre del Servidor: " + System.getProperty("user.name"));
        JLabel lblProcessor = new JLabel("Procesador: " + getSystemInfo("wmic cpu get name"));
        JLabel lblProcessorSpeed = new JLabel("Velocidad del Procesador: " + getSystemInfo("wmic cpu get MaxClockSpeed") + " MHz");
        JLabel lblCores = new JLabel("Núcleos del Procesador: " + Runtime.getRuntime().availableProcessors());
        JLabel lblDisk = new JLabel("Capacidad de Almacenamiento: " + new File("/").getTotalSpace() / (1024 * 1024 * 1024) + " GB");
        JLabel lblOs = new JLabel("Versión de Windows: " + getSystemInfo("wmic os get Version"));

        serverInfoPanel.add(lblServerName);
        serverInfoPanel.add(lblProcessor);
        serverInfoPanel.add(lblProcessorSpeed);
        serverInfoPanel.add(lblCores);
        serverInfoPanel.add(lblDisk);
        serverInfoPanel.add(lblOs);
    }

    private String getSystemInfo(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && !line.contains("Name") && !line.contains("MaxClockSpeed") && !line.contains("Version")) {
                    return line.trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private void updateSystemMetrics() { 
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemCpuLoad() * 100;
        double cpuFree = 100 - cpuLoad;
        long freePhysicalMemorySize = osBean.getFreePhysicalMemorySize();
        long totalPhysicalMemorySize = osBean.getTotalPhysicalMemorySize();
        double memoryFreePercentage = (double) freePhysicalMemorySize / totalPhysicalMemorySize * 100;
        File disk = new File("/");
        long freeDiskSpace = disk.getFreeSpace();
        long totalDiskSpace = disk.getTotalSpace();
        double diskFreePercentage = (double) freeDiskSpace / totalDiskSpace * 100;

        SwingUtilities.invokeLater(() -> {
            model.setValueAt(String.format("%.2f%%", cpuFree), 0, 1);
            model.setValueAt(String.format("%.2f%%", memoryFreePercentage), 1, 1);
            model.setValueAt(String.format("%.2f%%", diskFreePercentage), 2, 1);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Server::new);
    }
}
