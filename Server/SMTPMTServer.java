/* Makes our SMTP Server threaded. Accepts multiple clients and also implements
   the logic we need for our server to engage in a two-way conversation with a client.
   The server will maintain a state-system and know which state it is in when processing
   a mail request(using the 3-phase data transfer procedure.
    Joseph Medina
 */

 //Necessary imports
 import java.util.*;
 import java.io.*;
 import java.net.*;

 public class SMTPMTServer extends Thread
 {
     //Define a private socket allocated for a specific client thread.
     private Socket clientTCPSocket = null;

     //A constructor that will take a socket connection from SMTPServer
     //
     public SMTPMTServer(Socket socket)
     {
         //Create a new thread instance of the SMTP Server
        super("SMTPMTServer");
        clientTCPSocket = socket;
     }

     public void run()
     {
         try
         {
             //Obtain current IP address.
             InetAddress IP = InetAddress.getLocalHost();
             String clientIP = null;

             //Create a socket in that can read information, create a socket out to send information
             PrintWriter cSocketOut = new PrintWriter(clientTCPSocket.getOutputStream(), true);
             BufferedReader cSocketIn = new BufferedReader(new InputStreamReader(clientTCPSocket.getInputStream()));

             //Once everything is setup, send the client the 220 message stating connection established.
             cSocketOut.println("220: Connection established with " + IP);

             //Construct Strings to hold the data to send and the data to receive.
             String fromClient, toClient;
             //Data types needed for parsing the commands.
             String command = null;
             String data = null;
             int state = 0; //0 state is HELO, 1 is MAIL FROM, 2 is RCPT TO, 3 is DATA.

             //Construct a while loop that will assign whatever is in the input socket
             //to the fromClient string, continue reading this until a null is read or
             //a break occurs.

             while((fromClient = cSocketIn.readLine()) != null)
             {
                 //Check if the line read from the client is the QUIT command
                 if(state == 5)
                 {
                     if(fromClient.equalsIgnoreCase("QUIT"))
                     {
                         cSocketOut.println("201 " + IP + " disconnected.");
                         System.out.println("201 " + clientIP + " disconnected.");
                         cSocketIn.close();
                         cSocketOut.close();
                         clientTCPSocket.close();
                         break;
                     }
                     else
                     {
                         state = 0;
                     }
                 }

                 //Awaiting the HELO command
                 if(state == 0)
                 {
                     //More parse stuffs
                     String[] parseRequest = new String[100];
                     //Split the string with white spaces.
                     parseRequest = fromClient.split("\\s+");
                     //Set command and the data following.
                     command = parseRequest[0];
                     try
                     {
                         data = parseRequest[1];
                     }
                     catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException)
                     {
                         System.out.println("Invalid command syntax received.");
                         cSocketOut.println("You've entered incorrect command syntax,"
                                 + " Syntax is: HELO <sender mail domain>");
                         state = 0;
                     }

                     clientIP = data;

                     //Check if the first command is HELO
                     if(command.equals("HELO") && data != null)
                     {
                         System.out.println(command + " " + data);
                         cSocketOut.println("250 " + IP + " Hello " + data);
                         state = 1; //Modify the state to state 1.
                     }
                     //Incorrect command. Do not modify state.
                     else
                     {
                         cSocketOut.println("503 5.5.2 Send HELO first.");
                     }

                 }
                 //Awaiting the MAIL FROM command
                 else if(state == 1)
                 {
                     //More parse stuffs
                     String[] parseRequest = new String[100];
                     //Split the string with white spaces.
                     parseRequest = fromClient.split(":");
                     //Set command and the data following.
                     command = parseRequest[0];
                     try
                     {
                         data = parseRequest[1];
                     }
                     catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException)
                     {
                         System.out.println("Invalid command syntax received.");
                         cSocketOut.println("You've entered incorrect command syntax,"
                                            + " Syntax is: MAIL FROM: <senders e-mail>");
                         state = 1;
                     }
                     if(command.equals("MAIL FROM"))
                     {
                         System.out.println(command + ":" + data);
                         cSocketOut.println("250 2.1.0 " + data + " Sender OK.");
                         state = 2;
                     }
                     else
                     {
                         cSocketOut.println("503 5.5.2 Need MAIL FROM command.");
                     }
                 }
                 //Awaiting the RCPT TO command
                 else if(state == 2)
                 {
                     //More parse stuffs
                     String[] parseRequest = new String[100];
                     //Split the string with white spaces.
                     parseRequest = fromClient.split(":");
                     //Set command and the data following.
                     command = parseRequest[0];
                     try
                     {
                         data = parseRequest[1];
                     }
                     catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException)
                     {
                         System.out.println("Invalid command syntax received.");
                         cSocketOut.println("You've entered incorrect command syntax,"
                                 + " Syntax is: RCPT TO: <receivers email>");
                         state = 2;
                     }

                     if(command.equals("RCPT TO"))
                     {
                         System.out.println(command + ":" + data);
                         cSocketOut.println("250 2.1.5 " + data + " Recipient OK");
                         state = 3;
                     }
                     else
                     {
                         cSocketOut.println("503 5.5.2 Need RCPT TO command.");
                     }
                 }
                 //Awaiting the DATA
                 else if(state == 3)
                 {
                     //More parse stuffs
                     String[] parseRequest = new String[10];
                     //Split the string with white spaces.
                     parseRequest = fromClient.split("\\s+");
                     //Set command and the data following.
                     command = parseRequest[0];

                     if(command.equals("DATA"))
                     {
                         System.out.println(command);
                         cSocketOut.println("354 Start mail input");
                         state = 4;
                     }
                     else
                     {
                         cSocketOut.println("503 5.5.2 Need DATA command.");
                     }
                 }
                 //Receive entire message
                 else if(state == 4)
                 {
                     System.out.println(fromClient); //This will print the to line.
                     data = cSocketIn.readLine();
                     System.out.println(data);//This will print the from line.
                     data = cSocketIn.readLine();
                     System.out.println(data); //This will print out the subject line.

                     data = cSocketIn.readLine(); //Read the first line of the message contents.
                     System.out.println();

                     //Read message line per line.
                     do
                     {
                         data = cSocketIn.readLine();
                         if (data.equals("."))
                         {
                             break;
                         } else
                         {
                             System.out.println(data);
                         }
                     }
                     while(!data.equals("."));

                     System.out.println("Server processed message. Send 'QUIT' to quit, anything else to send another");
                     cSocketOut.println("250 Message received and to be delivered.");
                     state = 5;
                 }
             }
         }
         catch (IOException ioException)
         {
             ioException.printStackTrace();
         }
     }
 }