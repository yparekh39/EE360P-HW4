import java.net.*;
import java.io.*;
import java.util.*; 
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class ServerThread implements Runnable {
  Socket socket = null;
  public int myClock = 0;

  public ServerThread(Socket socket){
    this.socket = socket;
  }

  public void run(){
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
        System.out.println("command was: " + inputLine);
        //If an actual command, send request to all servers and wait for acks
        if(splitIn[0].equals("purchase") || splitIn[0].equals("cancel") || splitIn[0].equals("list") || splitIn[0].equals("search")){
          //Signal CS request and receive ack
          signalRequestOrReleaseToOtherServers("request", inputLine, myClock);

          //Enqueue request
          Request req = new Request(Server.myID, myClock, inputLine);
          Server.enqueueRequest(req);
          Server.incrementClock();
          //Check if we are head of queue - lock if not
          Server.queueLock.lock();
          try{
            while(!(Server.lamportQueue.get(0).serverID == Server.myID && Server.lamportQueue.get(0).timestamp == myClock)){
              Server.otherRequestAhead.await();
            }
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            Server.queueLock.unlock();
          }

          if (splitIn[0].equals("purchase")) {
            String orderID = Server.myID + "-" + myClock;
            outputLine = Server.purchase(splitIn, orderID);
            out.println(outputLine);
            out.println("END");
          }
          else if (splitIn[0].equals("cancel")) {
            outputLine = Server.cancel(splitIn[1]);
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
          //Signal release and receive ack
          Request request = Server.dequeueRequest();
          signalRequestOrReleaseToOtherServers("release", inputLine, request.timestamp);
          Server.queueLock.lock();
          try{
            Server.otherRequestAhead.signalAll();
          } catch (Exception e){
            e.printStackTrace();
          } finally {
            Server.queueLock.unlock();
          }
        }
        else if(splitIn[0].equals("deq")){
          Request request = Server.dequeueRequest();
          signalRequestOrReleaseToOtherServers("release", "line", request.timestamp);
          Server.queueLock.lock();
          try{
            Server.otherRequestAhead.signalAll();
          } catch (Exception e){
            e.printStackTrace();
          } finally {
            Server.queueLock.unlock();
          }
          
          
        }
        
        else if (splitIn[0].equals("request")){
          //SEND ACK
          out.println("ack " + Server.getClock());
          //PUT REQUEST IN QUEUE
          Server.enqueueRequest(new Request(Integer.parseInt(splitIn[1]), Integer.parseInt(splitIn[2])));
          //UPDATE CLOCK TO REFLECT MAXIMUM IN REQUEST CLOCK VS LOCAL CLOCK, THEN INCREMENT CLOCK
          int localClk = Server.getClock();
          int requestClk = Integer.parseInt(splitIn[2]);
          if(localClk < requestClk)
            Server.setClock(requestClk + 1);
          else
            Server.setClock(localClk + 1);
        }
        else if (splitIn[0].equals("release")){
          //SEND ACK
          out.println("ack " + Server.getClock());
          //Dequeue
          Server.dequeueRequest();
          Server.queueLock.lock();
          try{
            Server.otherRequestAhead.signalAll();
          } catch (Exception e){
            e.printStackTrace();
          } finally {
            Server.queueLock.unlock();
          }
          //update Inventory and Order
          System.out.println(splitIn[splitIn.length-1]);
          String update = splitIn[splitIn.length-1];
          String[] splitUpdate = update.split(":");
          if(!splitUpdate[0].equals("NOCHANGE")){
            if(splitUpdate[0].equals("PURCHASE")){
              System.out.println("updating inventory on: " + Server.myID);
              String orderID = splitUpdate[1];
              String[] purchase = new String[4];
              for(int i = 0; i < 3; i++){
                purchase[i+1] = splitUpdate[i+2];
              }
              purchase[0] = "purchase";
              Server.purchase(purchase, orderID);
            } else if(splitUpdate[0].equals("CANCEL")){
              Server.cancel(splitUpdate[1]);
            }

          }
        }
        else if(splitIn[0].equals("crashed")){
          //remove crashed server from the queue and notify
          Server.removeCrashedServerRequests(Integer.parseInt(splitIn[1]));
          Server.queueLock.lock();
          try{
            Server.otherRequestAhead.signalAll();
          } catch (Exception e){
            e.printStackTrace();
          } finally {
            Server.queueLock.unlock();
          }
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

  public void signalRequestOrReleaseToOtherServers(String type, String command, int requestTimestamp){
    ExecutorService executor = Executors.newCachedThreadPool();
    List<Callable<Integer>> requestTaskList = new ArrayList<Callable<Integer>>();
    //Set timestamp to be sent out/used
    myClock = Server.getClock();
    //Create request thread to be sent to each server
    for(ServerInfo server : Server.servers){
      requestTaskList.add(new RequestOrReleaseThread(server.ipAddr, server.port, Server.myID, myClock, type, command, requestTimestamp));
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
      } catch (Exception e){ e.printStackTrace(); }
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
  }
}