package socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatServer {

    ServerSocket myServerSocket;
    boolean ServerOn = true;
    HashMap<Socket, String> newmap = new HashMap();

    public ChatServer() {
        try {
            myServerSocket = new ServerSocket(2003);
        } catch (IOException ioe) {
            System.out.println("Could not create server socket on port 2003. Quitting.");
            System.exit(-1);
        }

        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
//    System.out.println("It is now : " + formatter.format(now.getTime()));

        while (ServerOn) {
            try {
                Socket clientSocket = myServerSocket.accept();
                ClientServiceThread cliThread = new ClientServiceThread(clientSocket);
                cliThread.start();
            } catch (IOException ioe) {
                System.out.println("Exception found on accept. Ignoring. Stack Trace :");
                ioe.printStackTrace();
            }
        }
        try {
            myServerSocket.close();
            System.out.println("Server Stopped");
        } catch (Exception ioe) {
            System.out.println("Error Found stopping server socket");
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        new ChatServer();
    }

    class ClientServiceThread extends Thread {

        Socket myClientSocket;
        boolean m_bRunThread = true;
        public String[] listCommand
                = {
                    "", "quit"
                };

        public ClientServiceThread() {
            super();
        }

        ClientServiceThread(Socket s) {
            myClientSocket = s;
        }

        public void run() {

            String hostName = myClientSocket.getInetAddress().getHostName();
            System.out.println("Accepted Client - " + hostName);

//      client log in use username
//            while(ServerOn)
//            {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(myClientSocket.getInputStream()));
//          OutputStream listUser = null;
//        Writer out = new BufferedWriter(new OutputStreamWriter(listUser));        
                String MyName = in.readLine();
                newmap.put(myClientSocket, MyName);
                System.out.println("myClientSocket " + myClientSocket);
                System.out.println("newmap " + newmap);
            } catch (IOException ex) {
                System.out.println(ex);
                ex.getStackTrace();
                ex.getMessage();
            }

//      client input request messages within destination client
            try {
                DataInputStream dataInputStream = new DataInputStream(myClientSocket.getInputStream());
//                DataOutputStream dataOutputStream = new DataOutputStream(myClientSocket.getOutputStream());

                while (m_bRunThread) {
                    try {
                        String input = dataInputStream.readUTF();

                        JSONObject jsonObject = new JSONObject(input);
//                        String username = jsonObject.getString("username");
                        String message = jsonObject.getString("message");
//            String destination = jsonObject.getString("destination");

//            String output = listCommand(message);
                        System.out.println(hostName + " " + input);
                        JSONArray array = new JSONArray();
                        JSONObject send = new JSONObject();

//            Socket socketOnline = newmap.get(destination);
                        Socket socketOnline = null;
                        System.out.println(newmap);

                        if (socketOnline != null) {
//                            System.out.println("myClientSocket : " + myClientSocket);
                            send.put("username", myClientSocket.getInetAddress().toString());
//              array.put(message);
                            send.put("message", message);
//              array.getJSONArray(0);
                            System.out.println("response : " + (String.valueOf(send)));
//                            DataOutputStream recPersonOS = new DataOutputStream(socketOnline.getOutputStream());
                            System.out.println("send : " + send);
//
//                            recPersonOS.writeUTF(String.valueOf(send));
//                            recPersonOS.flush();

                        } else {
//              send.put("username : ", "Server");
//              array.put("" + destination + " not online");
//                            getKeyFromValue(newmap, newmap.get(myClientSocket), send);
                            send.put("username", newmap.get(myClientSocket));
                            send.put("message", message);
                            getKeyFromValue(newmap, newmap.get(myClientSocket), send);
//                            dataOutputStream.writeUTF(String.valueOf(send));
//                            dataOutputStream.flush();
                        }
                    } catch (EOFException e) {
                        System.out.println("Client " + myClientSocket.getInetAddress().getHostName() + " Disconnected");
                        newmap.values().remove(myClientSocket);
                        System.out.println(newmap);
                        m_bRunThread = false;

                        e.getStackTrace();
                        e.getMessage();
                    } catch (JSONException ex) {
                        Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);

                        ex.getStackTrace();
                        ex.getMessage();
                    }

                }
            } catch (EOFException e) {
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    myClientSocket.close();
                    System.out.println("...Stopped");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (NullPointerException e) {
                    System.out.println("Thread closed");
                }
//            }
            }
        }

        public String listCommand(String clientCommand) {
            String output = "";
            for (String listCommand : listCommand) {
                if (listCommand.equals(clientCommand)) {
                    output = listCommand;
                    break;
                } else {
                    output = "Command not registered";
                }
            }
            return output;
        }

        private void getKeyFromValue(HashMap<Socket, String> newmap1, String get, JSONObject send) throws IOException {
            for (Iterator<Socket> it = newmap1.keySet().iterator(); it.hasNext();) {
                Socket o = it.next();
                    DataOutputStream dataOutputStream = new DataOutputStream(o.getOutputStream());
                    dataOutputStream.writeUTF(String.valueOf(send));
                    dataOutputStream.flush();
                
            }
        }

    }

    public static Object getKeyFromValue(Map hm, Object value, JSONObject send) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
}
