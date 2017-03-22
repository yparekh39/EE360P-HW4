import java.net.*;
import java.io.*;
import java.util.*; 
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Server {

  public static Map<String, Integer> inventory = new ConcurrentHashMap<String, Integer>();
  public static Map<Integer, Order> userOrders = new ConcurrentHashMap<Integer, Order>();
  public static AtomicInteger clock = new AtomicInteger(0);

  public static void main (String[] args) {
    Scanner sc = new Scanner(System.in);
    int myID = sc.nextInt();
    int numServer = sc.nextInt();
    String myAddress;
    int myPort = -1;
    String inventoryPath = sc.next();
    List<ServerInfo> servers = new ArrayList<ServerInfo>();

    System.out.println("[DEBUG] my id: " + myID);
    System.out.println("[DEBUG] numServer: " + numServer);
    System.out.println("[DEBUG] inventory path: " + inventoryPath);
    //Establish list of servers from input
    for (int i = 0; i < numServer; i++) {
      String str = sc.next();
      String[] serverStrSplit = str.split(":");
      
      if(i == myID -1){ 
        myAddress = serverStrSplit[0];
        myPort = Integer.parseInt(serverStrSplit[1]);
        continue; 
      }
      
      servers.add(new ServerInfo(serverStrSplit[0], Integer.parseInt(serverStrSplit[1])));
    }
    //Inventory Parsing
    File file = new File(inventoryPath);
    Scanner fileScanner = null;
    try{
      fileScanner = new Scanner(file);
    }catch(Exception e){
      System.out.println("ERROR: File Not Found");
      System.exit(-1);
    }

    while(fileScanner.hasNext()){
      String input = fileScanner.nextLine();
      String[] splitInput = input.split(" ");
      inventory.put(splitInput[0], Integer.parseInt(splitInput[1]));
    }
    //Create server socket
    TCPServer tcpServerThread = new TCPServer(myPort);
    Thread tcpServer = new Thread(tcpServerThread);
    tcpServer.start();
    
    // TODO: handle request from client
  }
  
  //TODO: MUTEX THESE

  //TODO: FIX ORDER NUMBER OPERATIONS TO SET ORDER NUMBER TO LAMPORT TIMESTAMP, NOT ATOMIC INTEGER COUNTER
  public static synchronized String purchase(String[] st){
    // purchase <user-name> <product-name> <quantity> – inputs the name of a customer, the
    // name of the product, and the number of quantity that the user wants to purchase. The client
    // sends this command to the server using the current mode of the appropriate protocol. If the store
    // does not have enough items, the server responds with message: ‘Not Available - Not enough
    // items’. If the store does not have the product, the server responds with message: ‘Not Available
    // - We do not sell this product’. Otherwise, an order is placed and the server replies a message:
    // 1
    // ‘You order has been placed, <order-id> <user-name> <product-name> <quantity>’. Note
    // that, the order-id is unique and automatically generated by the server. You can assume that
    // the order-id starts with 1. The server should also update the inventory.
      String username = st[1];
      String productName = st[2];
      Integer quantityRequested = Integer.parseInt(st[3]);

      //check inventory
      Integer productQuantity = inventory.get(productName);
      if(productQuantity == null){
        return "Not Available - We do not sell this product";
      }

      if(productQuantity.compareTo(quantityRequested) < 0){
        return "Not Available - Not enough items";
      }

      // FIX ORDER NUMBER ASSIGNMENT TO USE LAMPORT TIMESTAMP
      // int myOrderNumber = currentOrder.getAndIncrement();
      // Order order = new Order(username, myOrderNumber, productName, quantityRequested.intValue());
      // Integer newProductQuantity = new Integer(productQuantity.intValue() - quantityRequested.intValue());
      // userOrders.put(new Integer(myOrderNumber), order);
      // inventory.put(productName, newProductQuantity);
      // return ("Your order has been placed, " + myOrderNumber + " " + username + " " + productName + " " + quantityRequested.toString());
      return "FIX PURCHASE TO USE LAMPORT TIMESTAMPS FOR ORDER IDS";
  }

  public static synchronized String cancel(String[] st){
    // cancel <order-id> – cancels the order with the <order-id>. If there is no existing order
    // with the id, the response is: ‘<order-id> not found, no such order’. Otherwise, the server
    // replies: ‘Order <order-id> is canceled’ and updates the inventory
    Integer orderID = new Integer(Integer.parseInt(st[1]));
    Order toBeCancelled = userOrders.get(orderID);
    //Order not found
    if(toBeCancelled == null){
      return "" + orderID + " not found, no such order";
    }
    //Order found
    else{
      //Restore quantity cancelled to order
      String product = toBeCancelled.productName;
      Integer newAmountOfProduct = inventory.get(toBeCancelled.productName).intValue() + toBeCancelled.quantity;
      inventory.put(product, newAmountOfProduct);
      //Nullify order in userOrders
      userOrders.put(orderID, toBeCancelled.cancelOrder());
      return "Order " + orderID + " is canceled";
    }
  }

  public static synchronized String search(String[] st){
    // search <user-name> – returns all orders for the user. If no order is found for the user, the
    // system responds with a message: ‘No order found for <user-name>’. Otherwise, list all orders
    // of the users as <order-id>, <product-name>, <quantity>. Note that, you should print one
    // line per order.
    String user = st[1];
    List<Order> orders = new ArrayList<Order>();
    for(Integer orderID: userOrders.keySet()){
      String thisOrderUser = userOrders.get(orderID).user;
      boolean thisOrderCanceled = userOrders.get(orderID).canceled;
      //Order belongs to user
      if(thisOrderUser.equals(user) && !thisOrderCanceled){
        orders.add(userOrders.get(orderID));
      }
    }
    //User had no orders
    if(orders.size() == 0){
      return "No order found for " + user;
    }
    //User had orders
    else{
      //All orders added to string followed by new line character (except last order)
      String ordersStringsAggregated = "";
      for(int i = 0; i < orders.size()-1; i++){
        String orderToAdd = "" + orders.get(i).id + ", ";
        orderToAdd += orders.get(i).productName + ", ";
        orderToAdd += orders.get(i).quantity + "\n";
        ordersStringsAggregated += orderToAdd;
      }
      //Last order, no new line character at end
      ordersStringsAggregated += "" + orders.get(orders.size()-1).id + ", ";
      ordersStringsAggregated += orders.get(orders.size()-1).productName + ", ";
      ordersStringsAggregated += orders.get(orders.size()-1).quantity;
      return ordersStringsAggregated;
    }
  }
  
  public static synchronized String list(String[] st){
    // list – lists all available products with quantities of the store. For each product, you should
    // show ‘<product-name> <quantity>’. Note that, even if the product is sold out, you should
    // also print the product with quantity 0. In addition, you should print one line per product.
    Set<Map.Entry<String, Integer>> inventorySet = inventory.entrySet();
    String output = "";
    for(Map.Entry<String, Integer> entry : inventorySet){
      output += (entry.getKey() + " " + entry.getValue().toString() + "\n");
    }
    return output.substring(0, output.length()-1);
  }
}
