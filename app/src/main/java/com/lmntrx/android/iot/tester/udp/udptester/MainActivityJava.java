package com.lmntrx.android.iot.tester.udp.udptester;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivityJava extends AppCompatActivity {

    String ip = "";
    String port = "";
    String message = "";

    EditText messageEditText, ipEditText, portEditText;
    Button sendButton;

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ipEditText = findViewById(R.id.ipEditText);
        messageEditText = findViewById(R.id.messageEditText);
        portEditText = findViewById(R.id.portEditText);
        sendButton = findViewById(R.id.sendButton);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    new ReceiverThread().start();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                return "";
            }
        }.execute();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {


                if (ipEditText.getText().toString().isEmpty())
                    ipEditText.setText(R.string.sample_ip);

                if (portEditText.getText().toString().isEmpty())
                    portEditText.setText(R.string.sample_port);

                if (messageEditText.getText().toString().isEmpty())
                    messageEditText.setText(R.string.hello_world);

                ip = ipEditText.getText().toString();
                port = portEditText.getText().toString();
                message = messageEditText.getText().toString();


                new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... voids) {
                        try {
                            InetAddress inetAddress = InetAddress.getByName(ip);
                            new SenderThread(inetAddress, Integer.parseInt(port), message).start();
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (SocketException e) {
                            e.printStackTrace();
                        }
                        return "";
                    }
                }.execute();
            }
        });


    }


    public class SenderThread extends Thread{

        private DatagramSocket socket = new DatagramSocket();

        private InetAddress server;
        private int port;

        public SenderThread(InetAddress server, int port, String message) throws SocketException{
            this.socket.connect(server, port);
            this.port = port;
            this.server = server;
        }

        @Override
        public void run() {

            try {

                byte[] data = message.getBytes();
                DatagramPacket output = new DatagramPacket(data, data.length, server, port);
                socket.send(output);

                //Message sent

                Thread.yield();

            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }


    public class ReceiverThread extends Thread{

        private DatagramSocket socket = new DatagramSocket();

        public ReceiverThread() throws SocketException, UnknownHostException {
            socket.connect(InetAddress.getByName("localhost"), 3001);
        }

        @Override
        public void run() {

            try {
                int port = 11000;

                DatagramSocket dScoket = new DatagramSocket(port);
                byte[] buffer = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (true){

                    dScoket.receive(packet);
                    final String message = new String(buffer, 0, packet.getLength());

                    runOnUiThread(
                            new Runnable() {
                                @Override
                                public void run() {
                                    messageEditText.setText(message);
                                    Toast.makeText(MainActivityJava.this, "Message Received", Toast.LENGTH_SHORT).show();
                                    sendButton.callOnClick();
                                }
                            }
                    );

                    packet.setLength(buffer.length);

                }

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
