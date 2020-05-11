package com.jgh.androidssh.sshutils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jgh.androidssh.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Controller for SSH shell-like process between local device and remote SSH server.
 * Sustains an open channel to remote server and streams data between local device
 * and remote.
 * <p/>
 * Created by Jon Hough on 5/17/14.
 */
class ShellController {

    private static final String TAG = "ShellController";
    /**
     *
     */
    private BufferedReader mBufferedReader;

    /**
     *
     */
    private DataOutputStream mDataOutputStream;

    /**
     *
     */
    private String mSshText = null;


    ShellController() {
        //nothing
    }

    /**
     * Writes to the outputstream, to remote server. Input should ideally come from an EditText, to which
     * the shell response output will also be written, to simulate a shell terminal.
     *
     * @param command commands string.
     */
    void writeToOutput(String command) {
        if (mDataOutputStream != null) {
            ShellController.WriteDataJob writeDataJob = new WriteDataJob(mDataOutputStream, command);
            writeDataJob.execute();
        }
    }

    /**
     * Opens shell connection to remote server. Listens for user input in EditText Data Input Stream and
     * streams to remote server. Server responses are streamed back on background thread and
     * output to the EditText.
     *
     * @param handler  Handler for updating UI EditText on non-UI thread.
     * @throws JSchException
     * @throws java.io.IOException
     */

    void openShellPreGenerated(Session session, Handler handler, final String string, final TextView oilBurnedDisplay, final Activity activity) throws JSchException, IOException {
        if (session == null) throw new NullPointerException("Session cannot be null!");
        if (!session.isConnected()) throw new IllegalStateException("Session must be connected.");
        final Handler myHandler = handler;

        Channel mChannel = session.openChannel("shell");
        mChannel.connect();
        mBufferedReader = new BufferedReader(new InputStreamReader(mChannel.getInputStream()));
        mDataOutputStream = new DataOutputStream(mChannel.getOutputStream());
        new Thread(() -> {
            try {
                String line;
                while (true) {
                    while ((line = mBufferedReader.readLine()) != null) {
                        final String result = line;
                        if (mSshText == null) mSshText = result;
                            myHandler.post(() -> {
                                synchronized (string) {
                                    Log.d(TAG, "LINE : " + result);
                                    if(oilBurnedDisplay != null) {
                                        // splits the result returned by the python code on the raspberry pi.
                                        // the [0] index is the key so we know that the input follows is the correct input
                                        // the [1] index is the actual value we intend to display
                                        String[] splitter = result.split("\\|");
                                        if(splitter.length > 1) {
                                            String oilBurned = activity.getResources().getString(R.string.oil_burned_key);
                                            String gallons = activity.getResources().getString(R.string.gallons);
                                            if(splitter[0].equals(oilBurned)) {
                                                oilBurned = activity.getResources().getString(R.string.oil_burned);
                                                String output = oilBurned + ": " + splitter[1] + " " + gallons;
                                                oilBurnedDisplay.setText(output); // update the textView
                                            }
                                        }
                                    }
                                }
                            });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, " Exception " + e.getMessage() + "." + e.getCause() + "," + e.getClass().toString());
            }
        }).start();
    }

    private class WriteDataJob extends AsyncTask<String, Void, String> {

        DataOutputStream dataOutputStream;
        String command;

        WriteDataJob(DataOutputStream dataOutputStream, String command) {
            this.dataOutputStream = dataOutputStream;
            this.command = command;
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                dataOutputStream.writeBytes(command + "\r\n");
                dataOutputStream.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return "some message";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }
}


