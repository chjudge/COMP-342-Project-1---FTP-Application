/** Author:  Clayton Judge
  * Course:  COMP 342 Data Communications and Networking
  * Date:    23 March 2021
  * Description: Server side of an FTP application
*/
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    private final static int port = 9001;
    private static Path currentDirectory = Paths.get(System.getProperty("user.dir"));
    private static boolean quit = false;

    public static void main(String[] args) {
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

            String input, argument;

            while(!quit){
                //Start by reading a message from the client
                input = inStream.readUTF();
                argument = "";

                //get file name from RETR and STOR commands
                if(input.length() > 4 && input.charAt(4) == ' '){
                    argument = input.substring(5);
                    input = input.substring(0, 4);
                }
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
                        STOR(outStream, inStream, argument);
                        break;
                    case "RETR":
                        RETR(outStream, argument);
                        break;
                    default:
                        outStream.writeInt(0);
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

    private static void RETR(DataOutputStream outStream, String argument) throws IOException{
        //get file indicated in command
        File file = new File(argument);
        //create file input stream
        FileInputStream fileStream;
        try {
            fileStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            outStream.writeInt(0);
            outStream.writeUTF("ERROR: Invalid File Name");
            return;
        }

        //send flag to client
        outStream.writeInt(2);
        //send file name to client
        outStream.writeUTF(argument);
        //send file size to client
        outStream.writeLong(file.length());
        //send file 4 kilobytes at a time
        byte[] buffer = new byte[4*1024];
        int bytes;
        //add bytes to the buffer until the end of the file is reached
        while((bytes = fileStream.read(buffer)) != -1){
            outStream.write(buffer, 0, bytes);
            outStream.flush();
        }

        fileStream.close();
    }

    private static void STOR(DataOutputStream outStream, DataInputStream inStream, String argument) throws IOException{
        //send flag to client
        outStream.writeInt(3);
        //send file name to client
        outStream.writeUTF(argument);

        if(inStream.readByte() == 0){
            return;
        }

        FileOutputStream fileStream = new FileOutputStream(argument);
        //get file size from client
        long size = inStream.readLong();

        //recieve file 4 kilobytes at a time
        byte[] buffer = new byte[4*1024];
        int bytes = 0;

        while (size > 0 && (bytes = inStream.read(buffer, 0, buffer.length))!= -1) {
            fileStream.write(buffer, 0, bytes);
            size -= bytes;
        }

        fileStream.close();
    }
}