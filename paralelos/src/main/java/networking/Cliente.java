package networking;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.sun.management.OperatingSystemMXBean;

public class Cliente extends JFrame {
    private JButton switchButton;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static String clientIP = "25.13.41.150";

    public static void main(String[] args) throws IOException {
        String serverIp = "25.57.124.131";
        int serverPort = 9999;
        
        Cliente cliente = new Cliente();

        socket = new Socket(serverIp, serverPort);
        System.out.println("Conectado al servidor: " + serverIp);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(Cliente::sendMessages, 0, 3, TimeUnit.SECONDS);
    }

    private static void sendMessages() {
        try {
            String message = updateSystemMetrics();
            out.println(message);
            System.out.println("Mensaje enviado al servidor: " + message);
            System.out.println("Respuesta del servidor: " + in.readLine());
            String ip = in.readLine();
            String regex = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(ip);
            
            if (matcher.matches()) {
                if (ip != clientIP) {
                	cerrarConexionExistente();
                	
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void establecerNuevaConexion(String ip) {
        try {
            socket = new Socket(ip, 9999);
            System.out.println("Nueva conexión establecida.");
        } catch (IOException e) {
            System.out.println("Error al iniciar la nueva conexión: " + e.getMessage());
        }
    }
    
    private static void cerrarConexionExistente() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                System.out.println("Conexión existente cerrada.");
            } catch (IOException e) {
                System.out.println("Error al cerrar la conexión existente: " + e.getMessage());
            }
        } else {
            System.out.println("No hay conexión existente para cerrar.");
        }
    }
    
    private static String updateSystemMetrics() {
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
    	double rankScore = (cpuFree+memoryFreePercentage+diskFreePercentage+Runtime.getRuntime().availableProcessors()*100)/100;
        
        return clientIP+","+cpuFree+","+memoryFreePercentage+","+diskFreePercentage+","+rankScore+",false";
    }
    
    public Cliente() {
        super("Switch Button Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);

        JPanel panel = new JPanel();
        add(panel);

        switchButton = new JButton("Switch");
        switchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchRequest("Switch,"+clientIP);
            }
        });
        panel.add(switchButton);

        setVisible(true);
    }
    
    
    private void switchRequest(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    



}