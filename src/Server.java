
import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server implements Runnable { // runnable allow the class to be passed to a thread
	private String message;
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private Random rand = new Random();
    private boolean gameStart = false;
    private boolean guessed = false;
	private int newValue;
	private int suit;
	private int value;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }
    @Override
    public void run() { // this is what is executed when you start the runnable class
        try {
            server = new ServerSocket(8088); // passing the port to server
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept(); // when accepting a socket
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutDown();
        }
    }
    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }
    public void shutDown() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutDown();
            }
        } catch (IOException e) {

        }
    }
    
    class ConnectionHandler implements Runnable { // what handles the client connections
    	private Server s = new Server();
        private Socket client;
        private BufferedReader in; // get information from client
        private PrintWriter out; // send to the client
        private String name;
        public ConnectionHandler(Socket client) {
            this.client = client;
        }
       

        @Override
        public void run() {

            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                name = client.getInetAddress().getHostAddress();
                System.out.println(name + " Connected");
                broadcast(name + " Joined the chat");
                while ((message = in.readLine()) != null) {
                    broadcast(name + ": " + message);
                    hlGame();
                   
               }
               
            }
            catch (IOException e){
               shutDown();
            }
        }
        public void sendMessage(String message) {
            out.println(message);
        }
        public void shutDown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       
        /*
        public void sendFile() throws UnknownHostException, IOException {
        	ImageIcon imageIcon = new ImageIcon("Images/0.0.png");
        	OutputStream outputStream = client.getOutputStream();
        	BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        	Image image = imageIcon.getImage();
        	BufferedImage bufferedImage = new BufferedImage(image.getWidth(null),
        			image.getHeight(null),BufferedImage.TYPE_INT_RGB);
        	Graphics g = bufferedImage.createGraphics();
        	g.drawImage(image,0,0,null);
        	g.dispose();
        	ImageIO.write(bufferedImage, "png", bos);
        	bos.close();
        	JOptionPane.showMessageDialog(null, "Sent");
        }
       */
    	public void hlGame(){
    		
    		if(message.startsWith("/help")) {
    			broadcast("SERVER: Help Information");
            	broadcast("SERVER: Type '/hlgame' to start Higher Lower Game");
            	broadcast("SERVER: Type '/Lower' to guess lower");
            	broadcast("SERVER: Type '/higher' to guess higher");
            	broadcast("SERVER: Type '/equal' to guess equal to");
            	broadcast("SERVER: Type '/endhl' to end the game");
    		}
    		
    		if(message.startsWith("/hlgame")) {
    			suit = rand.nextInt(0,3);
    			value = rand.nextInt(0,12);
    			broadcast("suit " + suit);
    			broadcast("value " + value);
    			System.out.println(suit + "" + value);
    			gameStart = true;
    			newValue = rand.nextInt(0,12);
    			broadcast("SERVER: Welcome to Higher Lower Game");
            	broadcast("SERVER: Is the new card going to be higher lower or equal");
            	
    		}
    		if(message.startsWith("/endhl")) {
    			gameStart = false;
    			broadcast("SERVER: Thanks for playing Higher Lower Game");
    		}
    		if(message.startsWith("/higher")&& gameStart) {
    			if(newValue > value) {
    				broadcast("SERVER: " + name + " is correct!");
                	broadcast("SERVER: Is the new card going to be higher lower or equal");
         			guessed = true;
         		}else {
         			broadcast("SERVER: " + name + " is incorrect! Try Again!");
         		}
         		if(guessed) {
         			value = newValue;
         			newValue = rand.nextInt(0,12);
         			suit = rand.nextInt(0,3);
         			broadcast("suit " + suit);
         			broadcast("value " + value);
             		guessed = false;
         		}
    		}
    		if(message.startsWith("/lower")&& gameStart) {
    			if(newValue < value) {
    				broadcast("SERVER: " + name + " is correct!");
                	broadcast("SERVER: Is the new card going to be higher lower or equal");
         			guessed = true;
         		}else {
         			broadcast("SERVER: " + name + " is incorrect! Try Again!");
         		}
         		if(guessed) {
         			value = newValue;
         			newValue = rand.nextInt(0,12);
         			suit = rand.nextInt(0,3);
         			broadcast("suit " + suit);
         			broadcast("value " + value);
         			System.out.println("Is the new card going to be higher lower or equal");
             		guessed = false;
         		}
    		}
    		if(message.startsWith("/equal")&& gameStart) {
    			if(newValue == value) {
    				broadcast("SERVER: " + name + " is correct!");
                	broadcast("SERVER: Is the new card going to be higher lower or equal");
         			guessed = true;
         		}else {
         			broadcast("SERVER: " + name + " is incorrect! Try Again!");
         		}
         		if(guessed) {
         			value = newValue;
         			newValue = rand.nextInt(0,12);
         			suit = rand.nextInt(0,3);
         			broadcast("suit " + suit);
         			broadcast("value " + value);
         			
         			System.out.println("Is the new card going to be higher lower or equal");
             		guessed = false;
         		}
    		}
    	}
    }  
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
    