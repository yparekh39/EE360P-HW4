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
    System.out.println("Entered run");
    try(
      PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
          BufferedReader in = new BufferedReader(
              new InputStreamReader(
                  socket.getInputStream()));
    ){
      
      //do client/server things here
      String inputLine, outputLine;
      while ((inputLine = in.readLine()) != null){
        String[] splitIn = inputLine.split(" ");
        //If an actual command, send request to all servers and wait for acks
        if(splitIn[0].equals("purchase") || splitIn[0].equals("cancel") || splitIn[0].equals("list") || splitIn[0].equals("search")){
          System.out.println("command was: " + splitIn[0]);
          ExecutorService executor = Executors.newCachedThreadPool();
          List<Callable<Integer>> requestTaskList = new ArrayList<Callable<Integer>>();
          //Set timestamp to be sent out/used
          myClock = Server.clock;
          //Create request thread to be sent to each server
          for(ServerInfo server : Server.servers){
            requestTaskList.add(new RequestThread(server.ipAddr, server.port, Server.myID, myClock));
          }
          System.out.println(requestTaskList.size());
          List<Future<Integer>> requestFutures = new ArrayList<Future<Integer>>();
          try{
            requestFutures = executor.invokeAll(requestTaskList);
          } catch (InterruptedException e){
            e.printStackTrace();
          }
          System.out.println(requestFutures.size());
          //Wait for response from all servers
          List<Integer> requestResponses = new ArrayList<Integer>();
          for(Future<Integer> future : requestFutures){
            try{
              Integer result = future.get();
              requestResponses.add(result);
            } catch (Exception e){
              System.out.println("FIX THIS CATCH STATEMENT");
            }
          }
          //Check to see if any servers crashed, and let others know if a server crashed
          for(int i = 0; i < requestResponses.size(); i++){
            if(requestResponses.get(i).intValue() == 0){
              for(ServerInfo server : Server.servers){
                try(Socket s = new Socket();){
                  try{
                    s.connect(new InetSocketAddress(server.ipAddr, server.port), 100);
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
          //Enqueue request
          Request req = new Request(Server.myID, myClock, inputLine);
          Server.enqueueRequest(req);
          //Check if we are head of queue - lock if not
          Server.queueLock.lock();
          try{
            while(!(Server.lamportQueue.get(0).serverID == Server.myID && Server.lamportQueue.get(0).timestamp == myClock)){
              System.out.println("We're locking");
              Server.otherRequestAhead.await();
            }
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            Server.queueLock.unlock();
          }
          System.out.println("We got past the lock");

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
        }
        
        else if (splitIn[0].equals("request")){
          //SEND ACK
          out.println("ack " + Server.getClock());
          //PUT REQUEST IN QUEUE
          Server.enqueueRequest(new Request(Integer.parseInt(splitIn[1]), Server.getClock()));
          //UPDATE CLOCK TO REFLECT MAXIMUM IN REQUEST CLOCK VS LOCAL CLOCK, THEN INCREMENT CLOCK
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