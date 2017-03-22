import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;

public class RequestThread implements Callable<Integer>{
	private int port;
	private String address;
	private int myServerID;
	private int myTimestamp;
	private boolean received = false;

	public RequestThread(String address, int port, int myServerID, int myTimestamp){
		this.address = address;
		this.port = port;
		this.myServerID = myServerID;
		this.myTimestamp = myTimestamp;
		this.received = false;
	}

	public Integer call(){
		try(
			Socket s = new Socket();
		){
			try{
				s.connect(new InetSocketAddress(address, port), 100);
			} catch(Exception e){
				return new Integer(0);
			}
			PrintWriter out =
          		new PrintWriter(s.getOutputStream(), true);
        	BufferedReader in =
          		new BufferedReader(
            		new InputStreamReader(s.getInputStream()));

          	String requestString = "request " + myServerID + " " + myTimestamp;
          	out.println(requestString);
          	String line;
          	Timer timer = new Timer()
          	while(while((line = in.readLine()) != null){
              
              String[] splitCommand = line.split(" ")
              //System.out.println(line);
              break;
            })

		} catch(Exception e){ e.printStackTrace() }

	}
}