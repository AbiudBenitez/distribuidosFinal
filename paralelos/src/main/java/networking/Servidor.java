package networking;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Servidor extends JFrame {
    private static final int PUERTO = 9999;
    private static List<PrintWriter> clientes = new ArrayList<>();
	private static ArrayList<String[]> DataClientes = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable table;
    private Timer timer;


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PUERTO);
        System.out.println("Servidor iniciado en el puerto " + PUERTO);

        SwingUtilities.invokeLater(() -> {
            Servidor Server = new Servidor();
            Server.setVisible(true);
        });	
        
        while (true) {
            Socket clienteSocket = serverSocket.accept();
            System.out.println("Cliente conectado desde: " + clienteSocket.getInetAddress().getHostAddress());

            PrintWriter out = new PrintWriter(clienteSocket.getOutputStream(), true);
            clientes.add(out);

            // Crear un hilo para manejar la comunicación con el cliente
            new Thread(new ClienteHandler(clienteSocket, out)).start();
        }
    }

    private static class ClienteHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private Scanner in;

        public ClienteHandler(Socket socket, PrintWriter out) {
            this.socket = socket;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                String inputLine;
                while (in.hasNextLine()) {
               	 String[] sourceCliente = in.nextLine().split(",");
               	 if (sourceCliente.length == 2) {
               		 enviarATodos(sourceCliente[1]);
               	 } else {               		 
               		 if (IPSearch(sourceCliente[0])) {
               			 DataClientes.remove(searchIndex(sourceCliente[0]));
               		 }
               		 DataClientes.add(sourceCliente);
               		 for (String[] Cliente : DataClientes) {
               			 System.out.println("");
               			 for (String data : Cliente) {
               				 System.out.print(data+"|");
               			 }
               			 System.out.println("");
               			 System.out.println("-------------------------------------------------");
               		 }
               		 sortByScore();
               		 out.println("RECIBIDO");
               	 }
                }
            } catch (IOException e) {
                System.out.println("Error al leer el mensaje del cliente: " + e.getMessage());
            } finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error al cerrar el socket: " + e.getMessage());
                }
                // Eliminar el PrintWriter del cliente de la lista cuando se desconecta
                clientes.remove(out);
            }
        }
        
    }

    public static boolean IPSearch (String ip) {
        boolean encontrado = false;

        for (String[] arreglo : DataClientes) {
            if (arreglo.length > 0 && arreglo[0].equals(ip)) {
                encontrado = true;
                break;
            }
        }

        return encontrado;
    }
    
    public static int searchIndex (String ip) {
        for (int i = 0; i < DataClientes.size(); i++) {
            String[] arreglo = DataClientes.get(i);
            if (arreglo.length > 0 && arreglo[0].equals(ip)) {
                return i;
            }
        }
        return -1;
    }
    
    public static void sortByScore() {
    	DataClientes.sort(Comparator.comparingDouble(arr -> Double.parseDouble(arr[4])));
    }
    

    private static void enviarATodos(String mensaje) {
        for (PrintWriter cliente : clientes) {
            cliente.println(mensaje);
        }
    }
    
    
    public Servidor() {

        setTitle("Tabla de Clientes");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crear el DefaultTableModel con columnas
        String[] columnas = {"IP", "CPU FREE(%)", "MEMORY FREE(%)", "DISK FREE(%)", "RANKSCORE", "SERVIDOR"};
        tableModel = new DefaultTableModel(columnas, 0);

        // Crear la JTable con el DefaultTableModel
        table = new JTable(tableModel);

        // Agregar la JTable a un JScrollPane
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Crear el modelo de la tabla para información detallada
        String[] detailedColumnNames = { "dispositivo",
            "Procesador", "Velocidad", "Nucleos", "Almacenamiento",
            "OSVersion"
	        };

        Object[] row1 = {
        		System.getProperty("user.name"), getSystemInfo("wmic cpu get name"), getSystemInfo("wmic cpu get MaxClockSpeed"),
        		Runtime.getRuntime().availableProcessors(), new File("/").getTotalSpace() / (1024 * 1024 * 1024) + " GB",
        		getSystemInfo("wmic os get Version")
            };
                
        DefaultTableModel detailedModel = new DefaultTableModel(detailedColumnNames, 0);
        
        detailedModel.addRow(row1);

        JTable detailedTable = new JTable(detailedModel);
        JScrollPane detailedScrollPane = new JScrollPane(detailedTable);

        // Crear un panel para las tablas y agregar las tablas al panel
        JPanel tablePanel = new JPanel(new GridLayout(2, 1)); // 2 filas, 1 columna
        tablePanel.add(scrollPane);
        tablePanel.add(detailedScrollPane);

        // Crear el botón
        JButton button = new JButton("Acción");

        // Agregar ActionListener al botón
        button.addActionListener(e -> {
          JOptionPane.showMessageDialog(Servidor.this, "¡Ya eres el servidor!");
        });

        // Crear un panel para el botón y agregar el botón al panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(button);

        // Agregar el panel de las tablas y el panel del botón al marco
        getContentPane().add(tablePanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Crear y configurar el Timer para actualizar la tabla cada 5 segundos
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTable();
            }
        }, 0, 1000); // Actualizar cada 5 segundos
    }

    
    private void updateTable() {
        SwingUtilities.invokeLater(() -> {
            // Limpiar la tabla
            tableModel.setRowCount(0);

            // Agregar los datos de la lista de clientes a la tabla
            for (String[] cte : DataClientes) {
                tableModel.addRow(cte);
            }
        });
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

    
}
