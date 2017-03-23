import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;

public class Client {
  public static boolean serverCrashed = false;
  public static String cmd;

  public static void main (String[] args) {
    List<ServerInfo> servers = new ArrayList<ServerInfo>();
    Scanner sc = new Scanner(System.in);
    int numServer = Integer.parseInt(sc.nextLine());
    int currServerIndex = -1;
    
    
    for (int i = 0; i < numServer; i++) {
      String serverString = sc.nextLine();
      String[] serverStrSplit = serverString.split(":");
      servers.add(new ServerInfo(serverStrSplit[0], Integer.parseInt(serverStrSplit[1]), i+1));
      // TODO: parse inputs to get the ips and ports of servers
    }

    while(true) {
      //If a server hasn't crashed, wait for input as normal
      if(!serverCrashed)
        cmd = sc.nextLine();
      //Otherwise, we need to resubmit our old command
      else{
        System.out.println("We recognized that server crashed on loop");
        cmd = cmd;
        System.out.println(cmd);
      }

      try(
        Socket socket = new Socket();
      ){
        for(int i = 0; i < servers.size(); i++){
          try{
            socket.connect(new InetSocketAddress(servers.get(i).ipAddr, servers.get(i).port), 100);
            //Make note of who we connected to so we can ping them
            currServerIndex = i;
            System.out.println("Connected to server port: " + servers.get(currServerIndex).port);
            //currServerIP = servers.get(i).ipAddr;
            //currServerPort = servers.get(i).port;
          } catch(Exception e){
            servers.remove(i);
            continue;
          }
          break;
        }
        
        PrintWriter out =
          new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in =
          new BufferedReader(
            new InputStreamReader(socket.getInputStream()));

        String[] tokens = cmd.split(" ");
        String line;

        if (tokens[0].equals("purchase")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
          out.println("purchase " + tokens[1] + " " + tokens[2] + " " + tokens[3]);
            while((line = in.readLine()) != null){
              if(line.equals("END")){
                break;
              }
              System.out.println(line);
            }

        } else if (tokens[0].equals("cancel")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
            out.println("cancel " + tokens[1]);
            while((line = in.readLine()) != null){
              if(line.equals("END")){
                break;
              }
              System.out.println(line);
            }

        } else if (tokens[0].equals("search")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
            out.println("search " + tokens[1]);
            while((line = in.readLine()) != null){
              if(line.equals("END")){
                break;
              }
              System.out.println(line);
            }

        } else if (tokens[0].equals("list")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
            out.println("list");
            //START PING THREAD TO MONITOR SERVER'S LIVELIHOOD
            // Thread pThread = new Thread(new PingThread(currServerIP, currServerPort));
            // pThread.start();
            //WAIT FOR SERVER TO HELP US
            boolean successfulRead = false;
            while((line = in.readLine()) != null){
              if(line.equals("END")){
                break;
              }
              System.out.println(line);
              successfulRead = true;
            }
            if(!successfulRead){
              serverCrashed = true;
              System.out.println("Server crashed");
            }
            else
              serverCrashed = false;

        } else if(tokens[0].equals("deq")){
            out.println("deq");
        }else {
          System.out.println("ERROR: No such command");
        }
      } catch(Exception e){ e.printStackTrace();}
      System.out.println("Looping again, and maybe resubmitting because servercrashed = " + serverCrashed);
      if(serverCrashed){
        servers.remove(currServerIndex);
      }
    }

  }
}
