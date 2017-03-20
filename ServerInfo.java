public class ServerInfo {
    public String ipAddr;
    public int port;
    public ServerInfo(String ipAddr, Integer port){
      this.ipAddr = ipAddr;
      this.port = port.intValue();
    }
  }