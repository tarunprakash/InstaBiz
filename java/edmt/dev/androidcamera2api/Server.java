package edmt.dev.androidcamera2api;

import android.content.Intent;
import android.provider.ContactsContract;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import android.app.Activity;
import android.content.ContentProviderOperation;
import java.util.ArrayList;
import android.provider.ContactsContract;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.ContactsContract;

import android.widget.TextView;
import android.widget.Toast;

public class Server extends Activity {
    private Intent intent = new Intent(".MainActivity");

    String DisplayName = "";
    String MobileNumber = "";
    String emailID = "";
    String company = "";

    MainActivity activity;
    ServerSocket serverSocket;
    String message = "";
    String[] payload;
    static final int socketServerPORT = 8080;

    BufferedReader in;

    public Server() {
    }

    public Server(MainActivity activity) {
        this.activity = activity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
        //thread.start();
    }

    public int getPort() {
        return socketServerPORT;
    }


    private class SocketServerThread extends Thread {

        int count = 0;

        @Override
        public void run() {
            try {
                // create ServerSocket using specified port
                serverSocket = new ServerSocket(socketServerPORT);

                while (true) {
                    // block the call until connection is created and return
                    // Socket object
                    Socket socket = serverSocket.accept();
                    count++;
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                    message = in.readLine();
                    payload = message.trim().split("\\?\\?\\s+");
                    for (int i = 0; i < payload.length; i++) {
                        if (payload[i].equals("."))
                            payload[i] = "";
                        else if (payload[i].contains("??."))
                            payload[i] = payload[i].substring(0, payload[i].length() - 3);

                    }
                    //activity.msg.setText("\nREADMA");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //activity.msg.setText("\n" + message);

                            DisplayName = "HEY"; //payload[0];
                            MobileNumber = "7329564059";// payload[1];
                            emailID = "121jwang@gmail.com";//payload[2];
                            company = "CUMVAULT";//payload[3];

                            //activity.msg.setText("TESTING");
                            ArrayList < ContentProviderOperation > ops = new ArrayList < ContentProviderOperation > ();

                            ops.add(ContentProviderOperation.newInsert(
                                    ContactsContract.RawContacts.CONTENT_URI)
                                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                    .build());

                            //------------------------------------------------------ Names
                            if (DisplayName != null) {
                                ops.add(ContentProviderOperation.newInsert(
                                        ContactsContract.Data.CONTENT_URI)
                                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                        .withValue(ContactsContract.Data.MIMETYPE,
                                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                        .withValue(
                                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                                DisplayName).build());
                            }

                            //------------------------------------------------------ Mobile Number
                            if (MobileNumber != null) {
                                ops.add(ContentProviderOperation.
                                        newInsert(ContactsContract.Data.CONTENT_URI)
                                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                        .withValue(ContactsContract.Data.MIMETYPE,
                                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber)
                                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                                        .build());
                            }
                            //------------------------------------------------------ Email
                            if (emailID != null) {
                                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                        .withValue(ContactsContract.Data.MIMETYPE,
                                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailID)
                                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                                        .build());
                            }

                            //------------------------------------------------------ Organization
                            if (!company.equals("")) {
                                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                        .withValue(ContactsContract.Data.MIMETYPE,
                                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                                        .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                                        .build());
                            }

                            // Asking the Contact provider to create a new contact
                            try {
                                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                    });

                    //startActivity(intent);
                    SocketServerReplyThread socketServerReplyThread =
                            new SocketServerReplyThread(socket, count);
                    socketServerReplyThread.run();

                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            InputStream inputStream;
            String msgReply = "Hello from Server, you are #" + cnt;

            try {
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(msgReply);
                printStream.close();

                //message += "replayed: " + msgReply + "\n";

                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //activity.msg.setText(payload[0] + " has been added to contacts!");
                    }
                });

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }

            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try
                    {
                        Toast.makeText(Server.this, payload[0] + " has been added to contacts!", Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e)
                    {

                    }

                }
            });
        }

    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Server running at : "
                                + inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    /*Thread thread = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    if (payload != null) {
                        activity.msg.setText("AYYYY");
                        String DisplayName = payload[0];
                        String MobileNumber = payload[1];
                        String emailID = payload[2];
                        String company = payload[3];


                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                        intent.putExtra(ContactsContract.Intents.Insert.NAME, DisplayName);
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, MobileNumber);
                        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, emailID);
                        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, company);
                        startActivity(intent);
                        payload = null;
                    }
                } catch (Exception e) {
                }
                ;

            }

        }
    };
    */
}