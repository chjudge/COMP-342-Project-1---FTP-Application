/** Author:  Clayton Judge & Melva Loock
  * Course:  COMP 342 Data Communications and Networking
  * Date:    23 March 2021
  * Description: Client side of an FTP application
*/

//Server class must be run first (simultaneously)

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private final static int port = 9001;
    private static boolean quit = false;
    public static void main(String[] args) {
        try {
			//create a socket that connects to the localhost server running on port 9001
            Socket mySocket=new Socket("localhost",port);

			//Connect an input stream so we can read from the socket
			DataInputStream inStream=new DataInputStream(mySocket.getInputStream());  
			//Connect an output stream so we can write to the socket
			DataOutputStream outStream=new DataOutputStream(mySocket.getOutputStream());  

            //print welcome message
            System.out.println("Welcome to GCC FTP client!");

            int flag = 1;
            String command, message;
            Scanner sc = new Scanner(System.in);

            while(!quit){
                System.out.print("Command: ");
                //get command from user
                command = sc.nextLine();
                //Write the command to the socket
                outStream.writeUTF(command);  
                //flush the stream to make sure the data has been written 
                outStream.flush();  
                //get type of data coming from client
                flag = inStream.readInt();
                //quit if msgCount is negative
                switch (flag) {
                    case -1:    //quit
                        quit = true;
                        break;
                    case 0:     //PWD
                        message = inStream.readUTF();
                        System.out.println(message);
                        break;
                    case 1:     //LIST
                        int numMsg = inStream.readInt();
                        for (int i = 0; i < numMsg; i++) {
                            message = inStream.readUTF();
                            System.out.println(message);
                        }
                        break;
                    case 2:     //RETR
                        RETR(inStream);
                        break;
                    case 3:     //STOR
                        STOR(inStream, outStream);
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

    private static void RETR(DataInputStream inStream) throws IOException{
        //get name of file
        String fileName = inStream.readUTF();
        FileOutputStream fileStream = new FileOutputStream(fileName);
        //get size of file
        long size = inStream.readLong();
        
        //recieve file 4 kilobytes at a time
        byte[] buffer = new byte[4*1024];
        int bytes = 0;

        while (size > 0 && (bytes = inStream.read(buffer, 0, buffer.length))!= -1) {
            fileStream.write(buffer, 0, bytes);
            size -= bytes;
        }

        fileStream.close();

        //get name of file without extension
        fileName = fileName.substring(0, fileName.indexOf("."));

        System.out.println("Here is the " + fileName + " file contents!");
    }

    private static void STOR(DataInputStream inStream, DataOutputStream outStream) throws IOException{
        String fileName = inStream.readUTF();

        //get file indicated in command
        File file = new File(fileName);
        //create file input stream
        FileInputStream fileStream;
        try {
            fileStream = new FileInputStream(file);
            outStream.writeByte(1);
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: Invalid File Name");
            outStream.writeByte(0);
            return;
        }

        //send file size to server
        outStream.writeLong(file.length());

        byte[] buffer = new byte[4*1024];
        int bytes;
        //add bytes to the buffer until the end of the file is reached
        while((bytes = fileStream.read(buffer)) != -1){
            outStream.write(buffer, 0, bytes);
            outStream.flush();
        }

        fileStream.close();

        System.out.println("File stored correctly");
    }
}
