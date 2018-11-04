import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public class Server {
    
    //static ServerSocket variable
    private static ServerSocket server;
    //socket server port on which it will listen
    private static int port = 9011;
    
    public static void main(String args[]) throws IOException, ClassNotFoundException{
        //create the socket server object
        server = new ServerSocket(port);
        //keep listens indefinitely until receives 'exit' call or program terminates
        while(true){
            System.out.println("Waiting for client request");
            //creating socket and waiting for client connection
            Socket socket = server.accept();
            System.out.println("CONNECTED");
            //read from socket to ObjectInputStream object
            //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            //ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            //convert ObjectInputStream object to String
            byte[] buffer = new byte[1024];
            
            InputStream in = socket.getInputStream();
            File output = new File("/Users/prakash/Desktop/Princeton Hackathon/saved.jpg");
            OutputStream out_ = new FileOutputStream(output);
            int read = -1;
            int i = 0;
            while ((read = in.read(buffer)) != -1)
            {
            	
            	out_.write(buffer, 0, read); 
            	System.out.println(read);
            
            	i++;

            }
            System.out.println(i);
            out_.close();
           
 
            socket.close();
        }
    }
}
