package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    String htmlReceived = null;

    int currentCelebrityIndex; // index of celebrity image displayed
    List<String[]> imageUrlAndNameList = new LinkedList<>();

    Button button0;
    Button button1;
    Button button2;
    Button button3;

    ImageView imageView;

    public void onClick(View view) {
        Button buttonClicked = (Button)view;

        String nameOfCelebrityDisplayed = imageUrlAndNameList.get(currentCelebrityIndex)[1];
        String nameOnButtonClicked = buttonClicked.getText().toString();
        String toastMessage;
        if (nameOnButtonClicked.equals(nameOfCelebrityDisplayed)){
            toastMessage = "Correct!";

        } else {
            toastMessage = "Wrong! It was " + nameOfCelebrityDisplayed;
        }
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        createNewQuestion();
    }

    public static class DownloadHtmlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            Log.i("Info", "Received url from task execution");
            StringBuilder htmlDownload = new StringBuilder();
            URL url;
            HttpURLConnection httpURLConnection;
            try {
                Log.i("Info", "Html Download: " + "Starting");
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while(data != -1){
                    char current = (char)data;
                    htmlDownload.append(current);
                    data = reader.read();
                }
                Log.i("Info", "Html Download: " + "Completed!");
                return htmlDownload.toString();

            }catch (Exception e){
                e.printStackTrace();
                Log.i("Error", "Html Download: " + "Failed");
                Log.i("Error", e.toString());
                return "Html Download Failed";
            }
        }
    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... pictureUrls) {
            Log.i("Info", "Received picture url from task execution");
            Bitmap pictureDownload;
            URL url;
            HttpURLConnection httpURLConnection;

            try {
                Log.i("Info", "Picture Download: " + "Starting");
                url = new URL(pictureUrls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = httpURLConnection.getInputStream();
                pictureDownload = BitmapFactory.decodeStream(in);
                Log.i("Info", "Picture Download: " + "Completed!");
                return pictureDownload;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Error", "Picture Download: " + "Failed");
                Log.i("Error", e.toString());
                return null;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);

        DownloadHtmlTask task = new DownloadHtmlTask();
        try {
            Log.i("Info", "Url sending to Download Html Task...");
            htmlReceived = task.execute("https://www.imdb.com/list/ls052283250/").get();
            Log.i("Info", "Url sending: received " + htmlReceived);
        } catch (ExecutionException | InterruptedException e) {
            Log.i("Error", "Url sending: Didn't receive right response from task");
            e.printStackTrace();
        }

        assert htmlReceived != null;
        if (htmlReceived.equals("Html Download Failed")){
            Toast.makeText(this, "Load error: connect to internet then restart app!", Toast.LENGTH_LONG).show();
            imageView.setImageResource(R.drawable.network);
            return;
        }
        Log.i("Info", "Html Received: contains " + htmlReceived.toCharArray().length + " characters");

        imageUrlAndNameList = populateCelebrityList(htmlReceived);

        createNewQuestion();
    }

    /**
     * Contains method implementation to set up questions
     */
    private void createNewQuestion() {
        Log.i("Info", "New Question: Creating...");
        printOptionsOnButtons();
        Bitmap imageToDisplay = downloadPicture(currentCelebrityIndex);
        imageView.setImageBitmap(imageToDisplay);
        Log.i("Info", "New Question: Created Successfully");
    }

    /**
     * Shuffles options to make options random
     * Prints the name options on buttons
     */
    private void printOptionsOnButtons(){
        List<Integer> options = new ArrayList<>(getCelebrityListOfFour());
        Collections.shuffle(options);

        button0.setText(getNameFromOption(options.get(0)));
        button1.setText(getNameFromOption(options.get(1)));
        button2.setText(getNameFromOption(options.get(2)));
        button3.setText(getNameFromOption(options.get(3)));
    }


    /**
     * Receives index, gets celebrity from celebrity info array and returns a bitmap image
     *
     * @return celebrityListOfFour - list containing indices of four celebrity options
     */
    private List<Integer> getCelebrityListOfFour() {
        Log.i("Info", "Options List: Creating...");
        int sizeOfCelebrityList = imageUrlAndNameList.size();
        currentCelebrityIndex = (int)(Math.random() * sizeOfCelebrityList);

        List<Integer> celebrityListOfFour = new ArrayList<>();
        celebrityListOfFour.add(currentCelebrityIndex);

        while (celebrityListOfFour.size() < 4){
            int option = (int)(Math.random()* sizeOfCelebrityList);
            if (!celebrityListOfFour.contains(option)){
                celebrityListOfFour.add(option);
            }
        }
        Log.i("Info", "Options List: Created Successfully with" + celebrityListOfFour.size() + " options" );
        return celebrityListOfFour;
    }


    /**
     * Receives index, gets celebrity from celebrity info array and returns a bitmap image
     * @param index - index celebrity image to be shown
     *
     * @return StringBuffer - name of celebrity
     */
    private StringBuffer getNameFromOption(int index){
        return new StringBuffer(imageUrlAndNameList.get(index)[1]);
    }


    /**
     * Receives index, gets image url from celebrity info array and returns a bitmap image
     * @param index - index of celebrity image to be shown
     *
     * @return - List containing a Bitmap image
     */
    private Bitmap downloadPicture(int index){
        DownloadImageTask imageTask = new DownloadImageTask();
        try {
            Log.i("Info", "Sending picture url to Download Image Task");
            String pictureUrl = imageUrlAndNameList.get(index)[0];
            Log.i("Info", "Sent picture url: " + pictureUrl);
            return imageTask.execute(pictureUrl).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Reads html String and returns url to name pairs of celebrities in a list of String array
     * @param html - String of html doc downloaded from url
     *
     * @returns - List containing a String array of picture url to Name pairs (url String, name String)
     */

    private List<String[]> populateCelebrityList(String html) {
        Log.i("Info", "Populate List: Starting...");
        List<String[]> imageUrlAndNameList = new LinkedList<>();
        Pattern pattern = Pattern.compile("img alt=\"(.*?)\"\\Rheight=\"209\"\\Rsrc=\"(.*?).jpg\"", Pattern.MULTILINE);
        Log.i("Pattern", pattern.toString());
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            imageUrlAndNameList.add(new String[]{matcher.group(2), matcher.group(1)});
        }
        Log.i("Info", "Populate List: Complete");
        Log.i("Info", "Populate List: contains " + imageUrlAndNameList.size() + " pairs");

        return imageUrlAndNameList;
    }
}
