package socket4;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatServer
{
  ServerSocket myServerSocket;
  boolean ServerOn = true;
  HashMap<String, Socket> newmap = new HashMap();

  public ChatServer()
  {
    try
    {
      myServerSocket = new ServerSocket(2003);
    }
    catch (IOException ioe)
    {
      System.out.println("Could not create server socket on port 2003. Quitting.");
      System.exit(-1);
    }

    Calendar now = Calendar.getInstance();
    SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
//    System.out.println("It is now : " + formatter.format(now.getTime()));

    while (ServerOn)
    {
      try
      {
        Socket clientSocket = myServerSocket.accept();
        ClientServiceThread cliThread = new ClientServiceThread(clientSocket);
        cliThread.start();
      }
      catch (IOException ioe)
      {
        System.out.println("Exception found on accept. Ignoring. Stack Trace :");
        ioe.printStackTrace();
      }
    }
    try
    {
      myServerSocket.close();
      System.out.println("Server Stopped");
    }
    catch (Exception ioe)
    {
      System.out.println("Error Found stopping server socket");
      System.exit(-1);
    }
  }

  public static void main(String[] args)
  {
    new ChatServer();
  }

  class ClientServiceThread extends Thread
  {

    Socket myClientSocket;
    boolean m_bRunThread = true;
    public String[] listCommand =
    {
      "", "quit"
    };

    public ClientServiceThread()
    {
      super();
    }

    ClientServiceThread(Socket s)
    {
      myClientSocket = s;
    }

    public void run()
    {

      String hostName = myClientSocket.getInetAddress().getHostName();
      System.out.println("Accepted Client - " + hostName);

      try
      {
        BufferedReader in = new BufferedReader(new InputStreamReader(myClientSocket.getInputStream()));
        String MyName = in.readLine();
        newmap.put(MyName, myClientSocket);
        System.out.println(newmap);
      }
      catch (IOException ex)
      {
        System.out.println(ex);
      }
      try
      {
        DataInputStream dataInputStream = new DataInputStream(myClientSocket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(myClientSocket.getOutputStream());

        while (m_bRunThread)
        {
          try
          {
            String input = dataInputStream.readUTF();

            JSONObject jsonObject = new JSONObject(input);
            String username = jsonObject.getString("username");
            String message = jsonObject.getString("message");
            String destination = jsonObject.getString("destination");
             
//            String output = listCommand(message);
            System.out.println("request :" + input);
            JSONArray array = new JSONArray();
            JSONObject send = new JSONObject();
            
            Socket socketOnline = newmap.get(destination);
            System.out.println(newmap);
            
            if (socketOnline != null)
            {
              send.put("username", username);
//              array.put(message);
              send.put("message", message);
//              array.getJSONArray(0);
              System.out.println(String.valueOf(send));
              DataOutputStream recPersonOS = new DataOutputStream(socketOnline.getOutputStream());
              System.out.println("send : " + send);

              recPersonOS.writeUTF(String.valueOf(send));
              recPersonOS.flush();

            }
            else
            {
              send.put("username", "Server");
              array.put("" + destination + " not online");
              send.put("message", array);
              dataOutputStream.writeUTF(String.valueOf(send));
              dataOutputStream.flush();
            }
          }
          catch (EOFException e)
          {
            System.out.println("Client " + myClientSocket.getInetAddress().getHostName() + " Disconnected");
            newmap.values().remove(myClientSocket);
            System.out.println(newmap);
            m_bRunThread = false;
          }
          catch (JSONException ex)
          {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
          }
          
        }
      }
      catch (EOFException e)
      {
        e.printStackTrace();
      }
      catch (IOException ex)
      {
        ex.printStackTrace();
      }
      finally
      {
        try
        {
          myClientSocket.close();
          System.out.println("...Stopped");
        }
        catch (IOException ioe)
        {
          ioe.printStackTrace();
        }
        catch (NullPointerException e)
        {
          System.out.println("Thread closed");
        }
      }
    }

    public String listCommand(String clientCommand)
    {
      String output = "";
      for (String listCommand : listCommand)
      {
        if (listCommand.equals(clientCommand))
        {
          output = listCommand;
          break;
        }
        else
        {
          output = "Command not registered";
        }
      }
      return output;
    }

  }
}
