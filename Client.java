import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Client {
  public static void main (String[] args) {
    List<ServerInfo> servers = new ArrayList<ServerInfo>();
    Scanner sc = new Scanner(System.in);
    int numServer = sc.nextInt();
    
    for (int i = 0; i < numServer; i++) {
      String serverString = sc.nextLine();
      String[] serverStrSplit = serverString.split(":");
      servers.add(new ServerInfo(serverStrSplit[0], Integer.parseInt(serverStrSplit[1])));
      // TODO: parse inputs to get the ips and ports of servers
    }

    while(sc.hasNextLine()) {
      String cmd = sc.nextLine();
      String[] tokens = cmd.split(" ");

      if (tokens[0].equals("purchase")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
      } else if (tokens[0].equals("cancel")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
      } else if (tokens[0].equals("search")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
      } else if (tokens[0].equals("list")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
      } else {
        System.out.println("ERROR: No such command");
      }
    }
  }

  
}
