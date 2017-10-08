/*  Server program that will receive a message request from a client and engage
    in a two-way conversation until all the mail commands are processed in a way
    that the server can queue an e-mail message to be sent.

    Joseph Medina
 */

 //Needed imports for our server
 import java.net.*;
 import java.io.*;

 public class SMTPServer
 {
     public static void main(String [] args) throws IOException
     {
         //Create a server socket.
         ServerSocket serverTCPSocket= null;
         //Define a boolean to have the server listen for an incoming connection.
         boolean listening = true;

         try
         {
             //Try establishing a server socket on port 5090
             serverTCPSocket = new ServerSocket(5090);
         }
         catch (IOException exception)
         {
             System.err.println("Cannot listen on port 5090");
             System.exit(-1);
         }
         while(listening = true)
         {
             //While we are in listening mode on the server, listen for
             //incoming TCP socket connections, and start the connection with the client
             new SMTPMTServer(serverTCPSocket.accept()).start();
         }
            //Close the socket if we are no longer listening.
            serverTCPSocket.close();
     }
 }