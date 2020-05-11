package com.jgh.androidssh.sshutils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jgh.androidssh.R;

import java.util.Properties;

/**
 * Controller for Jsch SSH sessions. All SSH
 * connections are run through this class.
 */
public class SessionController {

    private static final String TAG = "SessionController";

    /**
     * JSch Session
     */
    private Session mSession;
    /**
     * JSch UserInfo
     */
    private SessionUserInfo mSessionUserInfo;

    /**
     * Controls Shell interface
     */
    private ShellController mShellController;

    /**
     * Instance
     */
    private static SessionController sSessionController;


    private SessionController() {
    }

    public static SessionController getSessionController() {
        if (sSessionController == null) {
            sSessionController = new SessionController();
        }
        return sSessionController;
    }

    /**
     * Gets the JSch SSH session instance
     *
     * @return session
     */
    private Session getSession() {
        return mSession;
    }

    /**
     * @return
     */
    public static boolean exists() {
        return sSessionController != null;
    }

    /**
     * Checks if the session instance is connected.
     *
     * @return True if connected, false otherwise.
     */
    public static boolean isConnected() {
        Log.v(TAG, "session controller exists... " + exists());
        if (exists()) {
            Log.v(TAG, "disconnecting");
            Session session = getSessionController().getSession();
            if(session != null) {
                return session.isConnected();
            }
        }
        return false;
    }

    /**
     * Sets the user info for Session connection. User info includes
     * username, hostname and user password.
     *
     * @param sessionUserInfo Session User Info
     */
    public void setUserInfo(SessionUserInfo sessionUserInfo) {
        mSessionUserInfo = sessionUserInfo;
    }

    /**
     * Opens SSH connection to remote host.
     */
    public void connect(Activity activity) {
        /**
         * Thread for background tasks
         */
        Thread mThread;
        if (mSession == null) {
            mThread = new Thread(new SshRunnable(activity));
            mThread.start();
        } else if (!mSession.isConnected()) {
            mThread = new Thread(new SshRunnable(activity));
            mThread.start();
        }
    }

    /**
     * Execute command on remote server. If SSH is not open, SSH shell will be opened and
     * command executed.
     *
     * @param command command to execute on remote host.
     * @return command sent true, if not false
     */

    public boolean executePreGeneratedCommand(Handler handler, String command, TextView oilBurnedDisplay, Activity activity) {
        if (mSession == null || !mSession.isConnected()) {
            return false;
        } else {
            if (mShellController == null) {
                mShellController = new ShellController();
                try {
                    GetDataJob getDataJob = new GetDataJob(mShellController, handler, command, oilBurnedDisplay, activity);
                    getDataJob.execute();

                } catch (Exception e) {
                    Log.e(TAG, "Shell open exception " + e.getMessage());
                    //TODO fix general exception catching
                }
            }
            synchronized (mShellController) {
                mShellController.writeToOutput(command);
            }
        }
        return true;
    }

    /**
     * Runnable for beginning session. Opens JSch session with username, password and host information from
     * <b>mSessionUserInfo</b>.
     */
    public class SshRunnable extends Activity implements Runnable {

        Handler mHandler = new Handler();
        Activity activity;

        public SshRunnable(Activity activity) {
            this.activity = activity;
        }

        public void run() {
            JSch jsch = new JSch();
            mSession = null;
            try {
                mSession = jsch.getSession(mSessionUserInfo.getUser(), mSessionUserInfo.getHost(),
                        mSessionUserInfo.getPort()); // port 22

                mSession.setUserInfo(mSessionUserInfo);

                Properties properties = new Properties();
                properties.setProperty("StrictHostKeyChecking", "no");
                mSession.setConfig(properties);
                mSession.connect();

            } catch (JSchException jex) {
                Log.e(TAG, "JschException: " + jex.getMessage() +
                        ", Fail to get session " + mSessionUserInfo.getUser() +
                        ", " + mSessionUserInfo.getHost());
            } catch (Exception ex) {
                Log.e(TAG, "Exception:" + ex.getMessage());
            }

            if(mSession.isConnected()) {
                Log.d("SessionController", "Session connected? " + mSession.isConnected());
                String command = "cd BoilerMonitor";
                TextView oilBurnedDisplay = activity.findViewById(R.id.oilBurnedDisplay);

                SessionController.getSessionController().executePreGeneratedCommand(mHandler, command, oilBurnedDisplay, activity);
            }
        }
    }

    private class GetDataJob extends AsyncTask<String, Void, String> {

        ShellController mShellController;
        Handler handler;
        String command;
        TextView oilBurnedDisplay;
        Activity activity;

        GetDataJob(ShellController mShellController, Handler handler, String command, TextView oilBurnedDisplay, Activity activity) {
            this.mShellController = mShellController;
            this.handler = handler;
            this.command = command;
            this.oilBurnedDisplay = oilBurnedDisplay;
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String[] params) {
            try {
                mShellController.openShellPreGenerated(getSession(), handler, command, oilBurnedDisplay, activity);
            } catch (Exception e) {
                Log.e(TAG, "Shell open exception " + e.getMessage());
            }
            return "some message";
        }

        @Override
        protected void onPostExecute(String message) {
            //process message
        }
    }
}
