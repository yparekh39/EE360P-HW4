public class Order{
	String user;
	int id;
	String productName;
	int quantity;
	boolean canceled;

	public Order(String user, int id, String productName, int quantity){
		this.user = user;
		this.id = id;
		this.productName = productName;
		this.quantity = quantity;
		this.canceled = false;
	}

	public String toString(){
		return (id + ", " + productName + ", " + quantity);
	}

	public Order cancelOrder(){
		this.canceled = true;
		return this;
	}
}