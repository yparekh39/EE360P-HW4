import java.util.Scanner;

public class Client {
  public static void main (String[] args) {
    Scanner sc = new Scanner(System.in);
    int numServer = sc.nextInt();
    
    for (int i = 0; i < numServer; i++) {
      // TODO: parse inputs to get the ips and ports of servers
    }

    while(sc.hasNextLine()) {
      String cmd = sc.nextLine();
      String[] tokens = cmd.split(" ");

      if (tokens[0].equals("purchase")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
        out.println("purchase " + tokens[1] + " " + tokens[2] + " " + tokens[3]);
          while((line = in.readLine()) != null){
            if(line.equals("END")){
              break;
            }
            System.out.println(line);
          }

      } else if (tokens[0].equals("cancel")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
          out.println("cancel " + tokens[1]);
          while((line = in.readLine()) != null){
            if(line.equals("END")){
              break;
            }
            System.out.println(line);
          }

      } else if (tokens[0].equals("search")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
          out.println("search " + tokens[1]);
          while((line = in.readLine()) != null){
            if(line.equals("END")){
              break;
            }
            System.out.println(line);
          }

      } else if (tokens[0].equals("list")) {
        // TODO: send appropriate command to the server and display the
        // appropriate responses form the server
          out.println("list");
          while((line = in.readLine()) != null){
            if(line.equals("END")){
              break;
            }
            System.out.println(line);
          }
      } else {
        System.out.println("ERROR: No such command");
      }
    }
  }
}
