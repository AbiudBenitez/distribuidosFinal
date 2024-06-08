package Monitoreo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.*;

public class User extends JFrame implements ActionListener {

  JButton submit, salir;
  JTextField campo;
  private String[] allinfo;
  Socket socket = null;
  private static boolean res;
  String[] allinfonew;
  ObjectOutputStream oos = null;
  ObjectInputStream ois = null;

  public static void main(String[] args) throws Exception {
    User userGUI = new User();
    userGUI.GUI();
    userGUI.setSize(450, 250);
    userGUI.setVisible(true);
    userGUI.setResizable(false);
    userGUI.setAutoRequestFocus(false);
  }

  protected void GUI() {
    InfoUser info = new InfoUser();

    info.setNamePC();
    info.setProcessorSpeed();
    info.setProcessorModel();
    info.setProcessorCores();
    info.setDiskSpace();
    info.setOsVersion();
    allinfo = info.infoComplete();

    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.setTitle("Usuario");
    Container frame = this.getContentPane();

    frame.setLayout(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();

    JLabel welcome = new JLabel("Welcome " + info.getNamePC());
    welcome.setFont(new Font("Arial", Font.BOLD, 21));
    JLabel speed = new JLabel("Speed: " + info.getProcessorSpeed() + " GHz");
    JLabel processor = new JLabel("Processor: " + info.getProcessorModel());
    JLabel cores = new JLabel("Cores: " + info.getProcessorCores());
    JLabel disk = new JLabel("Space: " + info.getDiskSpace() / 1000000000 + " GB");
    JLabel os = new JLabel("Windows Edition: " + info.getOsVersion());
    JLabel text = new JLabel("Ingresa un mensaje para el servidor: ");
    campo = new JTextField(15);
    submit = new JButton("ENVIAR");
    submit.setMaximumSize(new Dimension(100, 25));
    salir = new JButton("SALIR");
    salir.setMaximumSize(new Dimension(100, 25));

    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.NONE;
    frame.add(welcome, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.weightx = 0.1;
    gbc.fill = GridBagConstraints.NONE;
    frame.add(disk, gbc);

    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.weightx = 0.1;
    gbc.fill = GridBagConstraints.NONE;
    frame.add(cores, gbc);

    gbc.gridx = 3;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.weightx = 0.1;
    gbc.fill = GridBagConstraints.NONE;
    frame.add(speed, gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 4;
    gbc.gridheight = 1;
    gbc.weightx = 0.1;
    gbc.fill = GridBagConstraints.NONE;
    frame.add(processor, gbc);

    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.gridwidth = 4;
    gbc.gridheight = 1;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.NONE;
    frame.add(text, gbc);

    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.gridheight = 1;
    gbc.gridwidth = 4;
    frame.add(campo, gbc);

    gbc.gridx = 0;
    gbc.gridy = 6;
    gbc.gridheight = 1;
    gbc.gridwidth = 4;
    frame.add(submit, gbc);

    gbc.gridx = 0;
    gbc.gridy = 7;
    gbc.gridheight = 1;
    gbc.gridwidth = 4;
    frame.add(salir, gbc);

    gbc.gridx = 0;
    gbc.gridy = 8;
    gbc.gridwidth = 4;
    gbc.gridheight = 1;
    gbc.weighty = 0.1;
    gbc.fill = GridBagConstraints.NONE;
    frame.add(os, gbc);

    submit.addActionListener(this);
    salir.addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == submit) {
      String msj = campo.getText();
      int tam = allinfo.length;
      allinfonew = new String[tam + 1];
      for (int i = 0; i < tam; i++) {
        allinfonew[i] = allinfo[i];
        System.out.println(allinfonew[i]);
      }
      allinfonew[tam] = msj;
      System.out.println(allinfonew[tam]);

      try {
        socket = new Socket("25.42.108.158", 5432);
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        oos.writeObject(allinfonew);
        DataInputStream flujo = new DataInputStream(socket.getInputStream());
        (new Thread(new HiloServidor(flujo))).start();

      } catch (Exception ex) {
        ex.printStackTrace();
      }
    } else if (e.getSource() == salir) {
      System.exit(0);
    }
  }

  public class HiloServidor extends Thread {
    DataInputStream flujoIn;

    protected HiloServidor(DataInputStream flujo) {
      flujoIn = flujo;
    }

    @Override
    public void run() {
      try {
        while (true) {

          res = ois.readBoolean();

          while (res) {
            socket.close();
          }
        }
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Conexion cerrada con el servidor",
            "Estado", JOptionPane.OK_OPTION);
      }
    }
  }
}
