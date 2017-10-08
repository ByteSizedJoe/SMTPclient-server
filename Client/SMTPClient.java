/*  SMTP Client app that will send a request for a connection to an SMTP server,
    it will then wait on a response from the server to establish the connection.
    Once it's done, the client will gather the e-mail message information needed
    from the user to generate a message request. Once this is complete, it will send
    a request to the SMTP server.

    Joseph Medina
 */

 //Required imports
 import java.io.*;
 import java.net.*;
 import java.util.Scanner;
 import java.util.GregorianCalendar;
 import java.util.regex.*;

 public class SMTPClient
 {
     public String sendersEmail, receiversEmail, subject, emailContents;

  public static void main(String[] args) throws IOException
  {
      String hostName; //String to hold the host name of server we're connecting to.
      String returnStatus; //String to hold the response from the server.
      Socket TCPSocket = null; //Initialize a TCP socket.
      PrintWriter socketOut = null; //Initalize a PrintWriter for writing out a socket.
      BufferedReader socketIn = null; //Initalize a BufferedReader
      Scanner input = new Scanner(System.in); //Scanner to obtain hostname.

      //Ask the user for the server name.
      System.out.println("Enter the DNS name or IP address of the server: ");
      hostName = input.nextLine();

      //Setup start time for establishing connection.
      long startTime = new GregorianCalendar().getTimeInMillis();

      //Try to establish TCP/IP connection with the SMTP Server
      try
      {
          TCPSocket = new Socket(hostName, 5090);
          socketOut = new PrintWriter(TCPSocket.getOutputStream(), true);
          socketIn = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
          returnStatus = socketIn.readLine();   //Read in the servers response.
          System.out.println(returnStatus);     //Display the response from the server.
      }
      catch (UnknownHostException exception)
      {
          System.err.println("Unknown host: " + hostName);
          System.exit(1);
      }
      catch (IOException exception)
      {
          System.err.println("Unable to establish I/O to: " + hostName);
          System.exit(1);
      }

      //Setup end time for establishing connection.
      long endTime = new GregorianCalendar().getTimeInMillis();

      //Print the RTT for establishing a connection to the SMTP Server
      System.out.println("RTT of establishing connection: " + (endTime - startTime + "ms"));

      //Create string for while loop to keep looping through until user does not wish to continue.
      String continueConnection = "Yes";

      //Create new object to access getMessageInfo method.
      SMTPClient smtpClient = new SMTPClient();
      smtpClient.getMessageInfo();

      //Loop through building a messageRequest string
      while(continueConnection.equalsIgnoreCase("Yes"))
      {
          //Obtain current IP address.
          InetAddress IP = InetAddress.getLocalHost();

          //Construct the initial message request w/HELO command.
          String messageRequest = "HELO " + IP;
          //Measure start time of conversation.
          startTime = new GregorianCalendar().getTimeInMillis();
          //Send message to server.
          socketOut.println(messageRequest);
          //Wait for server response.
          returnStatus = socketIn.readLine();
          //Print out the servers response.
          System.out.println(returnStatus);
          //End time & RTT calculation
          endTime = new GregorianCalendar().getTimeInMillis();
          System.out.println("RTT: " + (endTime - startTime + "ms"));

          // The follow blocks of code follow the same format as comments above
          // Only with different message request commands.
          // No comments will follow for the three following blocks of code.
          messageRequest = "MAIL FROM:" + smtpClient.sendersEmail;
          startTime = new GregorianCalendar().getTimeInMillis();
          socketOut.println(messageRequest);
          returnStatus = socketIn.readLine();
          System.out.println(returnStatus);
          endTime = new GregorianCalendar().getTimeInMillis();
          System.out.println("RTT: " + (endTime - startTime + "ms"));

          messageRequest = "RCPT TO:" + smtpClient.receiversEmail;
          startTime = new GregorianCalendar().getTimeInMillis();
          socketOut.println(messageRequest);
          returnStatus = socketIn.readLine();
          System.out.println(returnStatus);
          endTime = new GregorianCalendar().getTimeInMillis();
          System.out.println("RTT: " + (endTime - startTime + "ms"));

          messageRequest = "DATA";
          startTime = new GregorianCalendar().getTimeInMillis();
          socketOut.println(messageRequest);
          returnStatus = socketIn.readLine();
          System.out.println(returnStatus);
          endTime = new GregorianCalendar().getTimeInMillis();
          System.out.println("RTT: " + (endTime - startTime + "ms"));

          //Now to send the entire mail message to the server
          startTime = new GregorianCalendar().getTimeInMillis();
          //Construct properly formatted mail message.
          messageRequest = "TO: " + smtpClient.receiversEmail;
          //Send message to SMTP server
          socketOut.println(messageRequest);
          messageRequest = "FROM: " + smtpClient.sendersEmail;
          socketOut.println(messageRequest);
          messageRequest = "SUBJECT: " + smtpClient.subject;
          socketOut.println(messageRequest);
          messageRequest = smtpClient.emailContents;
          smtpClient.emailContents = "";
          messageRequest = messageRequest.replaceAll("([\\n\\r]+\\s*)*$", "");
          System.out.println(messageRequest);
          socketOut.println(messageRequest);
          //Signal to the server that the message is finished
          //Read response that message went into send queue.
          returnStatus = socketIn.readLine();
          //Print out the response.
          System.out.println(returnStatus);
          //Display the RTT of sending the message.
          endTime = new GregorianCalendar().getTimeInMillis();
          System.out.println("RTT of sending mail message: " + (endTime - startTime + "ms"));

          //Ask the user if they want to continue.
          System.out.println("Would you like to send another messsage? (Yes/No)");
          continueConnection = input.nextLine();

          if(continueConnection.equalsIgnoreCase("Yes"))
          {
              //Obtain the input needed to construct another message.
              smtpClient.getMessageInfo();
              //Flush the input socket for use for the new message.
              socketIn = new BufferedReader(new InputStreamReader(TCPSocket.getInputStream()));
          }
          else if(continueConnection.equalsIgnoreCase("No"))
          {
              //Display message to server that client is no longer connected.
              socketOut.println("QUIT");
              returnStatus = socketIn.readLine();
              System.out.println(returnStatus);

              //Close all sockets, and close the TCP connection.
              socketOut.close();
              socketIn.close();
              TCPSocket.close();
          }
      }

  }

  //Create a method that will obtain all the information needed to make a message request.
  public void getMessageInfo()
  {
      Scanner messageInput = new Scanner(System.in);

      //Obtain the sender's email address
      System.out.println("Enter the senders e-mail address: ");
      sendersEmail = messageInput.nextLine();// + "\r\n";

      //Obtain the receiver's email address
      System.out.println("Enter the receivers e-mail address: ");
      receiversEmail = messageInput.nextLine();// + "\r\n";

      //Obtain the subject of email
      System.out.println("Enter the subject of the e-mail: ");
      subject = messageInput.nextLine();// + "\r\n";

      //Obtain the contents of the email address
      System.out.println("Enter the contents of your message: ");
      System.out.println("Note: To finish your input, enter a single .");
      emailContents = "\r\n";
      String msgComp = "";

      do
      {
          msgComp = messageInput.nextLine();
          emailContents += msgComp + "\r\n";
      }
      while(!msgComp.equals("."));

      System.out.println("Message input received.. sending to server..");
  }

 }
