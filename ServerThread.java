import java.net.*;
import java.io.*;
import java.util.*; 
import java.util.concurrent.*;

public class ServerThread implements Runnable {
  Socket socket = null;

  public ServerThread(Socket socket){
    this.socket = socket;
  }

  public void run(){
    try(
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
          BufferedReader in = new BufferedReader(
              new InputStreamReader(
                  socket.getInputStream()));
    ) {
      //do client/server things here
      String inputLine, outputLine;
      while ((inputLine = in.readLine()) != null) {
        String[] splitIn = inputLine.split(" ");
        if (splitIn[0].equals("purchase")) {
          outputLine = Server.purchase(splitIn);
          out.println(outputLine);
          out.println("END");
        }
        else if (splitIn[0].equals("cancel")) {
          outputLine = Server.cancel(splitIn);
          out.println(outputLine);
          out.println("END");
        } 

        else if (splitIn[0].equals("search")) {
          outputLine = Server.search(splitIn);
          out.println(outputLine);
          out.println("END");
        } 
        else if (splitIn[0].equals("list")) {
          outputLine = Server.list(splitIn);
          out.println(outputLine);
          out.println("END");
        } 
        else {
          System.out.println("ERROR: No such command");
        }
      }
      socket.close();
    } catch (IOException e) {
          e.printStackTrace();
    }
  }
}