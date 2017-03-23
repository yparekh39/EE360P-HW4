import java.net.*;
import java.io.*;
import java.util.*; 
import java.util.concurrent.*;

public class ServerThread implements Runnable {
  Socket socket = null;
  int myClock = 0;

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
        if(!splitIn[0].equals("request") || !splitIn[0].equals("ack") || !splitIn[0].equals("release") || !splitIn[0].equals("crashed")){
          ExecutorService executor = Executors.newCachedThreadPool();
          List<Callable<Integer>> requestTaskList = new ArrayList<Callable<Integer>>();
          myClock = Server.clock;
          for(ServerInfo server : Server.servers){
            requestTaskList.add(new RequestThread(server.ipAddr, server.port, Server.myID, myClock));
          }
          List<Future<Integer>> requestFutures = new ArrayList<Callable<Integer>>();
          try{
            requestFutures = executor.invokeAll(requestTaskList);
          } catch (InterruptedException e){
            e.printStackTrace();
          }
          List<Integer> requestResponses = new ArrayList<Integer>();
          for(Future future : requestfutures){
            try{
              Integer result = future.get();
              requestResponses.add(result);
            }
          }
          for(int i = 0; i < requestResponses.size(); i++){
            if(requestResponses.get(i).intValue() == 0){
              for(ServerInfo server : Server.servers){
                try(Socket s = new Socket();){
                  try{
                    s.connect(new InetSocketAddress(address, port), 100);
                  } catch(Exception e){
                       //no response, unable to connect
                  }
                  PrintWriter outCrash =
                    new PrintWriter(s.getOutputStream(), true);
                  BufferedReader inCrash =
                    new BufferedReader(
                      new InputStreamReader(s.getInputStream()));

                    outCrash.println("crashed " + server.serverID);

                } catch(Exception e){ }
              }
            }
          }
        }
        if (splitIn[0].equals("purchase")) {
          //TODO: SPAWN THREAD REQUESTS TO SEND REQUESTS TO OTHER SERVERS
          //TODO: EACH THREAD WILL WAIT FOR THE ACK, UPDATE THE CLOCK
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
          //TODO: SEND ACK
          out.println("ack " + Server.getClock);
          //TODO: PUT REQUEST IN QUEUE
          Server.enqueueRequest(new Request(Server.myID + " ", Server.getClock() + " "));
          //TODO: UPDATE CLOCK TO REFLECT MAXIMUM IN REQUEST CLOCK VS LOCAL CLOCK, THEN INCREMENT CLOCK
          int localClk = Server.getClock();
          int requestClk = Integer.parseInt(splitIn[2]);
          if(localClk < requestClk)
            Server.setClock(requestClk + 1);
          else
            Server.setClock(localClk + 1);
        }
        else if(splitIn[0].equals("crashed")){
          //remove crashed server from the queue
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