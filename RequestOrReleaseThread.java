import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;

public class RequestOrReleaseThread implements Callable<Integer>{
	private int port;
	private String address;
	private int myServerID;
	private int myTimestamp;
	private Boolean timedOut = Boolean.valueOf(false);
	private String type;
	private String command;
	private int requestTimestamp;

	public RequestOrReleaseThread(String address, int port, int myServerID, int myTimestamp, String type, String command, int requestTimestamp){
		this.address = address;
		this.port = port;
		this.myServerID = myServerID;
		this.myTimestamp = myTimestamp;
		this.timedOut = Boolean.valueOf(false);
		this.type = type;
		this.command = command;
		this.requestTimestamp = requestTimestamp;
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
          	String requestString = type + " " + myServerID + " " + myTimestamp;
          	if(type == "release") {
          		String[] splitCommand = command.split(" ");
          		if (splitCommand[0].equals("list") || splitCommand[0].equals("search")) {
          			requestString += " " + "NOCHANGE"; 
          		} else if (splitCommand[0].equals("purchase")) {
          			String orderID = myServerID + "-" + requestTimestamp;
          			String userName = splitCommand[1]; 
          			String itemPurchased = splitCommand[2];
          			String quantity = splitCommand[3];
          			requestString += " PURCHASE:" + orderID + ":" + userName + ":" + itemPurchased + ":" + quantity;
          		} else if (splitCommand[0].equals("cancel")) {
          			String orderID = splitCommand[1];
          			requestString += " CANCEL:" + orderID;
          		}
          	}
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
              
				String[] splitIn = line.split(" ");
				System.out.println(line);
				if(splitIn[0].equals("ack")){
					//UPDATE CLOCK TO REFLECT MAXIMUM IN REQUEST CLOCK VS LOCAL CLOCK, THEN INCREMENT CLOCK
					int localClk = Server.getClock();
					int requestClk = Integer.parseInt(splitIn[1]);
					if(localClk < requestClk)
						Server.setClock(requestClk + 1);
					else
						Server.setClock(localClk + 1);
	              	//return future with Lamport Timestamp
	              	return new Integer(Integer.parseInt(splitIn[1]));
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