/** Author:  Clayton Judge
  * Course:  COMP 342 Data Communications and Networking
  * Date:    23 March 2021
  * Description: Server side of an FTP application
*/

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    private final static int port = 9001;   //port for the protocol to pass data through
    private static Path currentDirectory;   //directory the server is running in
    private static boolean quit = false;    //while loop control variable
    private static String input;            //command from client

    public static void main(String[] args) {
        currentDirectory = Paths.get(System.getProperty("user.dir"));
        try {
            //create a ServerSocket object connected to port 9001
            ServerSocket serverSocket = new ServerSocket(port);

            //create a Socket object so we can read and write to it
			//This socket is only created when a client connects to this server
            Socket mySocket = serverSocket.accept();

            //Connect an input stream so we can read from the socket
			DataInputStream inStream=new DataInputStream(mySocket.getInputStream());  
			//Connect an output stream so we can write to the socket
			DataOutputStream outStream=new DataOutputStream(mySocket.getOutputStream());  

            //print welcome message
            System.out.println("Welcome to GCC FTP server!");
            System.out.println("Waiting for client commands...");

            while(!quit){
                //Start by reading a message from the client
                input = inStream.readUTF();
                
                //make command uppercase for ease of interpretation
                input = input.toUpperCase();

                //execute the command
                switch (input) {
                    case "QUIT":
                        quit = true;
                        //tell client to quit
                        outStream.writeInt(-1);
                        break;
                    case "PWD":
                        sendPWD(outStream);
                        break;
                    case "LIST":
                        LIST(outStream);
                        break;
                    case "STOR":
                        STOR();
                        break;
                    case "RETR":
                        RETR();
                        break;
                    default:
                        outStream.writeInt(1);
                        outStream.writeUTF("ERROR: Invalid command");
                        break;
                }
            }

            System.out.println("Connection terminated by the client...");

            inStream.close();  
			outStream.close();
			mySocket.close();  
			serverSocket.close();

        } catch (IOException e) {
            System.out.println("ERROR: Connection terminated unexpectedly");
        }
        
    }
    //send the path stored in currentDirectory
    private static void sendPWD(DataOutputStream outStream) throws IOException{
        //send flag to client
        outStream.writeInt(0);
        outStream.writeUTF(currentDirectory.toString());
        outStream.flush();
    }
    //list the files in currentDirectory
    private static void LIST(DataOutputStream outStream) throws IOException{
        //send flag to client
        outStream.writeInt(1);
        //get files in currentDirectory
        String directoryContents[] = currentDirectory.toFile().list();

        outStream.writeInt(directoryContents.length);

        for (String fileName : directoryContents) {
            outStream.writeUTF(fileName);
        }
        outStream.flush();
    }

    private static void RETR(){
        //TODO: implement RETR
    }

    private static void STOR(){
        //TODO: implement STOR
    }
}