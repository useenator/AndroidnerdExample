package com.example.useenator.androidasynctaskexample;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.useenator.androidasynctaskexample.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * //          Pattern 1, AsyncTask
 * <p/>
 * new AsyncTask<X, Void, Z>() {
 * protected Boolean doInBackground(X... params) {
 * //background task
 * }
 * <p/>
 * protected void onPostExecute(Z res) {
 * //UI callback
 * }
 * }.execute();
 * //        Pattern 2, Activity.runOnUiThread(Runnable)
 * <p/>
 * new Thread() {
 * public void run() {
 * //background task
 * <p/>
 * runOnUiThread(new Runnable() {
 * public void run() {
 * //UI callback
 * }
 * });
 * }
 * }.start();
 * //        Pattern 3, Handler.post(Runnable)
 * <p/>
 * new Thread() {
 * public void run() {
 * //background task
 * <p/>
 * handler.post(new Runnable() {
 * public void run() {
 * //UI callback
 * }
 * });
 * }
 * }.start();
 */

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener /* implements AdapterView.OnItemClickListener*/ {
    EditText mEditText;
    ListView mListView;
    String[] listOfImages;
    ProgressBar mProgressBar;
    LinearLayout mLoadingSection = null;

    Thread mThread;
    public static Handler mHandler;

    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditText = (EditText) findViewById(R.id.editText);
        mListView = (ListView) findViewById(R.id.url_listview);
        mListView.setOnItemClickListener(this);
        listOfImages = getResources().getStringArray(R.array.list_url);
        mProgressBar = (ProgressBar) findViewById(R.id.download_progressBar);
        mLoadingSection = (LinearLayout) findViewById(R.id.loading_section);

//        mThread = new Thread(new MyRunnable());
//        mThread.start();
//        mHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                mProgressBar.setProgress(msg.arg1);
//            }
//        };

//        mHandler = new Handler(new Handler.Callback() {
//            @Override
//            public boolean handleMessage(Message msg) {
//                mProgressBar.setProgress(msg.arg1);
//                return false;
//            }
//        });
        mHandler = new Handler();
    }

    /**
     * testing runnable with thread.
     */
    public void downloadImage(View view) {
        url = mEditText.getText().toString();
        Thread myThread = new Thread(new MyRunnable(url));
        myThread.start();//

        /**Pattern 1, AsyncTask
         */
        new AsyncTask<String, Void, Boolean>() {
            protected Boolean doInBackground(String... urls) {
                //background task
                downloadImageUsingThreads(url/*listOfImages[0]*/);
                return true;
            }

            protected void onPostExecute(Boolean result) {
                //UI callback
                mLoadingSection.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "from async task", Toast.LENGTH_LONG).show();
            }
        }.execute();

        /**Pattern 2, Activity.runOnUiThread(Runnable)
         */
//        new Thread() {
//            public void run() {
//                //background task
//
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        //UI callback
//                    }
//                });
//            }
//        }.start();

        /**Pattern 3, Handler.post(Runnable)
         */
//        new Thread() {
//            public void run() {
//                //background task
//                downloadImageUsingThreads(url/*listOfImages[0]*/);
//        ////////////////////////////////////////////////////////////////////
//                mHandler.post(new Runnable() {
//                    public void run() {
//                        //UI callback
//                        mLoadingSection.setVisibility(View.VISIBLE);
//                    }
//                });
        ////////////////////////////////////////////////////////////////////
//            }
//        }.start();


/**
 * Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
 */
//        File file = Environment
//                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//        Log.d("DIRECTORY_PICTURES", file.getAbsolutePath());
//
//        String url = listOfImages[0];
//        Uri uri = Uri.parse(url);
//        Log.d("uri LastPathSegment", uri.getLastPathSegment());
    }

    public boolean downloadImageUsingThreads(String url) {
        boolean successful = false;
        URL downloadURL = null;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        File file = null;

        try {

            downloadURL = new URL(url);
            //google recommand HttpURLConnection;not HttpClient
            httpURLConnection = (HttpURLConnection) downloadURL.openConnection();

            inputStream = httpURLConnection.getInputStream();

            String path = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .getAbsolutePath() + "/" + Uri.parse(url).getLastPathSegment();

            file = new File(path);
            Log.d("uri LastPathSegment", file.getAbsolutePath());
            fileOutputStream = new FileOutputStream(file);
            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = inputStream.read(buffer)) != -1) {//keep reading while read!=-1
                fileOutputStream.write(buffer, 0, read);//(buffer,start,{read/length})
                //Log.d("http", read + "");
            }
            successful = true;


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ////////////////// set mLoadingSection INVISIBLE /////////////////
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mLoadingSection.setVisibility(View.GONE);
                }
            });
            ////////////////// show loadin progress bar ////////////////////
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();//disconnect to save resources
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                }
            }

        }
        return successful;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mEditText.setText(listOfImages[position]);
    }

    /***
     * Async task is match better then thread in android ;match simpler and most adapted:
     */
    private class MyRunnable implements Runnable {
        public String url;

        public MyRunnable(String url) {
            this.url = url;
        }/*extends Thread{*/

        @Override
        public void run() {
            ////////////////// set mLoadingSection VISIBLE /////////////////
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mLoadingSection.setVisibility(View.VISIBLE);
                }
            });
            ////////////////// show loadin progress bar //////////////////

            downloadImageUsingThreads(url/*listOfImages[0]*/);
        }

    }


    private class MyAsyncTask extends AsyncTask<URL, Integer, Long> {

        @Override
        protected Long doInBackground(URL... params) {
            return null;
        }
    }//MyAsyncTask
}
