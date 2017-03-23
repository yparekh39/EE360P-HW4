import java.net.*;
import java.io.*;
import java.util.*; 
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class TCPServer implements Runnable{
	int tcpPort;

	public TCPServer(int port){
		this.tcpPort = port;
	}
	
	public void run(){
		try (ServerSocket serverSocket = new ServerSocket(tcpPort)){
	      while(true){
	      	//System.out.println("Waiting for new request");
	        Thread thread = new Thread(new ServerThread(serverSocket.accept()));
	        //System.out.println("Received request");
	        thread.start();
	        //System.out.println("Called start");
	      }
	    } catch(IOException e){
	        e.printStackTrace();
	    }
	}
}