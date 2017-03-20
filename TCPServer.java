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
	        Thread thread = new Thread(new ServerThread(serverSocket.accept()));
	        thread.start();
	      }
	    } catch(IOException e){
	        e.printStackTrace();
	    }
	}
}