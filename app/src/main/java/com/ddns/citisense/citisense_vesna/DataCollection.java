package com.ddns.citisense.citisense_vesna;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class DataCollection extends ActionBarActivity {
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);
        tv = (TextView) findViewById(R.id.asdf);
        tv.setText("asdasdasd");
        new Client().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_data_collection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class Client extends AsyncTask<Void, String, Void> {
        Socket socket;
        String ip = "192.168.1.99";
        Integer port = 9504;

        private BufferedReader mBufferIn;
        private PrintWriter mBufferOut;


        protected void connect() {
            while (true) {
                try {
                    socket = new Socket(ip, port);
                    mBufferOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            Log.d("asdfasdf", "connected");
        }

        protected void write(String msg) {
            mBufferOut.write(msg);
            mBufferOut.flush();
        }

        protected String read() {
            String response = "";
            try {
                int value=0;
                while(true)
                {
                    value = mBufferIn.read();
                    char c = (char)value;
                    response += c;
                    if (response.endsWith("OK")) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                connect();
            }
            return response;
        }

        @Override
        protected Void doInBackground(Void... params) {
            connect();
            String response;
            while(true) try {
                write("GET measurements\r\n");
                response = read();
                if (response.contains("JUNK")) {
                    write("\r\n\r\n\r\n\r\n\r\n");
                    Thread.sleep(1000);
                    write("GET measurements\r\n");
                    response = read();
                }
                if (!response.contains("JUNK") && !response.isEmpty()) {
                    publishProgress(response);
                }
                Thread.sleep(1000);

                if (response.contains("nocm nikol brejkat!!")) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            tv.setText(values[0]);
        }
    }
}
