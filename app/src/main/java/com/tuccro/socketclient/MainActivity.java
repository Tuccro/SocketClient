package com.tuccro.socketclient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class MainActivity extends Activity {

    Socket socket;

    EditText editTextIP;
    EditText editTextPort;
    EditText editTextMessage;

    Button buttonConnect;
    Button buttonSendMessage;

    TextView textAnswer;

    LinearLayout layoutSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextIP = (EditText) findViewById(R.id.edit_text_ip_address);
        editTextPort = (EditText) findViewById(R.id.edit_text_port);
        editTextMessage = (EditText) findViewById(R.id.edit_text_out_message);

        buttonConnect = (Button) findViewById(R.id.button_connect);
        buttonSendMessage = (Button) findViewById(R.id.button_send);

        textAnswer = (TextView) findViewById(R.id.text_in_message);

        layoutSendMessage = (LinearLayout) findViewById(R.id.layout_send_message);

        buttonConnect.setOnClickListener(onClickListener);
        buttonSendMessage.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.button_connect:
                    ConnectionCreator creator = new ConnectionCreator(editTextIP.getText().toString(),
                            Integer.parseInt(editTextPort.getText().toString()));
                    creator.execute();
                    break;

                case R.id.button_send:
                    String message;
                    if ((message = editTextMessage.getText().toString()) != null) {
                        Messenger messenger = new Messenger(message);
                        messenger.execute();
                    }
                    break;
            }
        }
    };

    private class ConnectionCreator extends AsyncTask {

        String url;
        int port;

        public ConnectionCreator(String url, int port) {
            this.url = url;
            this.port = port;
        }

        @Override
        protected Object doInBackground(Object[] params) {

            int retries = 3;
            while (retries > 0) {
                try {
                    socket = new Socket(url, port);
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    retries--;
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            buttonConnect.setEnabled(false);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            buttonConnect.setEnabled(true);

            if (socket != null) {
                Toast toast = Toast.makeText(getApplicationContext(), "Connection Established", Toast.LENGTH_SHORT);
                toast.show();
                layoutSendMessage.setVisibility(View.VISIBLE);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Connection ERROR", Toast.LENGTH_SHORT);
                toast.show();
                layoutSendMessage.setVisibility(View.INVISIBLE);
            }
        }
    }

    private class Messenger extends AsyncTask {

        String messageOut;
        String messageIn = "";
        BufferedReader input;

        public Messenger(String messageOut) {
            this.messageOut = messageOut;
        }

        @Override
        protected Object doInBackground(Object[] params) {

            if (socket != null && !socket.isClosed()) {
                try {
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    serverMessageReader.start();

                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.print(messageOut);
                    out.close();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            textAnswer.setText(messageIn);
        }

        Thread serverMessageReader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Log.e("Message", String.valueOf(socket.isClosed()));
                        //if (input.ready()) {
                            messageIn = messageIn + input.readLine();
                            Log.e("Message", messageIn);
                            input.close();
                        //}
                    }

                } catch (IOException e) {
                    //e.printStackTrace();
                }

            }
        });
    }
}
