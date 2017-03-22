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
          //TODO: SEND REQUEST TO OTHER SERVERS
          //TODO: INCREMENT LOCAL CLOCK BY 1
          outputLine = Server.purchase(splitIn);
          out.println(outputLine);
          out.println("END");
        }
        else if (splitIn[0].equals("cancel")) {
          //TODO: SEND REQUEST TO OTHER SERVERS
          //TODO: INCREMENT LOCAL CLOCK BY 1
          outputLine = Server.cancel(splitIn);
          out.println(outputLine);
          out.println("END");
        } 

        else if (splitIn[0].equals("search")) {
          //TODO: SEND REQUEST TO OTHER SERVERS
          //TODO: INCREMENT LOCAL CLOCK BY 1
          outputLine = Server.search(splitIn);
          out.println(outputLine);
          out.println("END");
        } 
        else if (splitIn[0].equals("list")) {
          //TODO: SEND REQUEST TO OTHER SERVERS
          //TODO: INCREMENT LOCAL CLOCK BY 1
          outputLine = Server.list(splitIn);
          out.println(outputLine);
          out.println("END");
        }
        else if (splitIn[0].equals("request")){
          //TODO: UPDATE CLOCK TO REFLECT MAXIMUM IN REQUEST CLOCK VS LOCAL CLOCK, THEN INCREMENT CLOCK

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