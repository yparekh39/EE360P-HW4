import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;

public class RequestThread implements Callable<Integer>{
	private int port;
	private String address;
	private int myServerID;
	private int myTimestamp;
	private Boolean timedOut = Boolean.valueOf(false);

	public RequestThread(String address, int port, int myServerID, int myTimestamp){
		this.address = address;
		this.port = port;
		this.myServerID = myServerID;
		this.myTimestamp = myTimestamp;
		this.timedOut = Boolean.valueOf(false);
	}

	//Returns 0 if cannot connect/no response, else returns Lamport Timestamp
	public Integer call(){
		try(
			Socket s = new Socket();
		){
			try{
				s.connect(new InetSocketAddress(address, port), 100);
			} catch(Exception e){
				return new Integer(0); //no response, unable to connect
			}
			PrintWriter out =
          		new PrintWriter(s.getOutputStream(), true);
        	BufferedReader in =
          		new BufferedReader(
            		new InputStreamReader(s.getInputStream()));

          	String requestString = "request " + myServerID + " " + myTimestamp;
          	out.println(requestString);
          	String line;
          	Timer timer = new Timer();
          	timer.schedule(new TimerTask() {
          		@Override
          		public void run(){
          			timedOut = Boolean.valueOf(true);
          		}
          	}, 100);
          	while((line = in.readLine()) != null || !timedOut.booleanValue()){
              
              String[] splitCommand = line.split(" ");
              if(splitCommand[0].equals("ack")){
              	//return future with Lamport Timestamp
              	return new Integer(Integer.parseInt(splitCommand[2]));
              }
              //System.out.println(line);
              break;
            }
            timer.cancel();
            return new Integer(0);
		} catch(Exception e){ 
			e.printStackTrace(); 
			return new Integer(0);
		}
	}
}