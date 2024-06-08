package Monitoreo;

import java.io.File;
import java.util.Scanner;

public class InfoUser {
  private String namePC;
  private String processorModel;
  private double processorSpeed;
  private int processorCores;
  private long diskSpace;
  private String osVersion;

  public void setNamePC() {
    namePC = System.getProperty("user.name");
  }

  public String getNamePC() {
    return namePC;
  }

  public void setProcessorModel() {
    processorModel = System.getenv("PROCESSOR_IDENTIFIER");
  }

  public String getProcessorModel() {
    return processorModel;
  }

  public void setProcessorSpeed() {
    processorSpeed = executeCommand("wmic cpu get CurrentClockSpeed") / 1000.0;
  }

  public double getProcessorSpeed() {
    return processorSpeed;
  }

  public void setProcessorCores() {
    processorCores = executeCommand("wmic cpu get NumberOfCores");
  }

  public int getProcessorCores() {
    return processorCores;
  }

  public void setDiskSpace() {
    File[] roots = File.listRoots();
    diskSpace = 0;
    for (File root : roots) {
      diskSpace += root.getTotalSpace();
    }
  }

  public long getDiskSpace() {
    return diskSpace;
  }

  public void setOsVersion() {
    osVersion = System.getProperty("os.version");
  }

  public String getOsVersion() {
    return osVersion;
  }

  public String[] infoComplete() {
    String[] info = {
        namePC,
        processorModel,
        Integer.toString(processorCores),
        Double.toString(processorSpeed),
        Long.toString(diskSpace),
        osVersion
    };
    return info;
  }

  private static int executeCommand(String command) {
    try {
      Process process = Runtime.getRuntime().exec(command);
      process.waitFor();
      Scanner scanner = new Scanner(process.getInputStream());
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.trim().matches("\\d+")) { // Verificar si la línea contiene solo números
          return Integer.parseInt(line.trim());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1; // Valor por defecto en caso de error
  }

}
