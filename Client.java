/** Author:  Clayton Judge
  * Course:  COMP 342 Data Communications and Networking
  * Date:    23 March 2021
  * Description: Cient side of an FTP application
*/


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final static int    port = 9001;    //port for the protocol to pass data through
    private static boolean      quit = false;   //while loop control variable
    private static int          flag = 1;       //leading component of every message from the server, indicates the contents of the message
    private static String       command,        //command written by the user to the console
                                message;        //message from the server
    public static void main(String[] args) {
        try {
			//create a socket that connects to the localhost server running on port 9001
            Socket mySocket=new Socket("localhost", port);

			//Connect an input stream so we can read from the socket
			DataInputStream inStream=new DataInputStream(mySocket.getInputStream());  
			//Connect an output stream so we can write to the socket
			DataOutputStream outStream=new DataOutputStream(mySocket.getOutputStream());  

            //print welcome message
            System.out.println("Welcome to GCC FTP client!");

            Scanner sc = new Scanner(System.in);

            while(!quit){
                System.out.print("Command: ");
                //get command from user
                command = sc.nextLine();
                //send the command to the server
                outStream.writeUTF(command);  
                //flush the stream to make sure the data has been written 
                outStream.flush();  
                //get type of message coming from client
                flag = inStream.readInt();
                switch (flag) {
                    case -1:    //quit
                        quit = true;
                        break;
                    case 0:     //PWD
                        message = inStream.readUTF();
                        System.out.println(message);
                        break;
                    case 1:     //LIST
                        
                        break;
                    case 2:     //RETR
                        RETR();
                        break;
                    case 3:     //STOR
                        STOR();
                        break;
                    default:
                        break;
                }
                
            }

            sc.close();
            inStream.close();  
			outStream.close();
			mySocket.close();  


        } catch (IOException e) {
            System.out.println("ERROR: Connection terminated unexpectedly");
        }
    }

    private static void RETR(){
        //TODO: implement RETR
    }

    private static void STOR(){
        //TODO: implement STOR
    }
}
