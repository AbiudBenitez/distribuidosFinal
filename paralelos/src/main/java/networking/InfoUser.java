package networking;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.CentralProcessor.ProcessorIdentifier;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.software.os.OperatingSystem.OSVersionInfo;

public class InfoUser {
  SystemInfo systemInfo = new SystemInfo();

  HardwareAbstractionLayer hardware = systemInfo.getHardware();
  CentralProcessor processor = hardware.getProcessor();
  GlobalMemory memory = hardware.getMemory();
  ProcessorIdentifier infoProcesor = processor.getProcessorIdentifier();
  ComputerSystem software = hardware.getComputerSystem();

  OperatingSystem softwareInfo = systemInfo.getOperatingSystem();
  OSProcess infoOs = softwareInfo.getCurrentProcess();
  OSVersionInfo versionSoftware = softwareInfo.getVersionInfo();

  private String namePC;
  private String processorModel;
  private double processorSpeed;
  private int processorCores;
  private long diskSpace;
  private long ram;
  private String osVersion;
  private static InetAddress direction;

  public static InetAddress getLocalHost() {
    try {
      direction = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return direction;
  }

  public void setNamePC() {
    namePC = infoOs.getUser();
  }

  public String getNamePC() {
    return namePC;
  }

  public void setRam() {
    ram = memory.getTotal();
  }

  public long getRam() {
    return ram;
  }

  public void setProcessorModel() {
    processorModel = infoProcesor.getName();
  }

  public String getProcessorModel() {
    return processorModel;
  }

  public void setProcessorSpeed() {
    processorSpeed = processor.getMaxFreq() / 1000000000;
  }

  public String getProcessorSpeed() {
    return Double.toString(processorSpeed);
  }

  public void setProcessorCores() {
    processorCores = processor.getLogicalProcessorCount();
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
    osVersion = versionSoftware.getVersion();
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
}
