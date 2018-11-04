package edmt.dev.androidcamera2api;

import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

import android.content.Intent;
import android.os.Environment;
import android.provider.ContactsContract;

import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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

public class QRServer extends Activity {
    private Intent intent = new Intent(".MainActivity");
    Socket socket;
    String DisplayName = "";
    String MobileNumber = "";
    String emailID = "";
    String company = "";

    MainActivity activity;
    ServerSocket serverSocket;
    String message = "";
    String[] payload;
    static final int socketServerPORT = 8081;

    InputStream in;

    public QRServer() {
    }

    public QRServer(MainActivity activity) {
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

                    try {
                        socket = serverSocket.accept();
                        count++;
                        byte[] buffer = new byte[1024];
                        in = socket.getInputStream();
                        int read = -1;
                        String output = Environment.getExternalStorageDirectory()+ "/MyQR" + count + ".jpg";
                        File file = new File(output);
                        boolean deleted = file.delete();
                        OutputStream out_ = new FileOutputStream(output);
                        int i = 0;
                        while ((read = in.read(buffer)) != -1) {

                            out_.write(buffer, 0, read);
                            System.out.println(read);

                            i++;

                        }
                        System.out.println(i);
                        out_.close();
                        socket.close();

                    }
                    catch(Exception e){};
                }


                }
                catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                }
            }
        }
    }

    class SocketServerReplyThread extends Thread {

        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
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