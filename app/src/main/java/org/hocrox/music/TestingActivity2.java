package org.hocrox.music;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class TestingActivity2 extends AppCompatActivity {

    private static final String APPLICATION_NAME = "API Sample";

    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/youtube-java-quickstart");
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final List<String> SCOPES =
            Arrays.asList(YouTubeScopes.YOUTUBE_READONLY);
    private static HttpTransport HTTP_TRANSPORT;
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();
    Button clickMeButton;
    private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";
Credential mCredential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing2);
        clickMeButton = (Button) findViewById(R.id.button);
        try {
       HTTP_TRANSPORT = new NetHttpTransport();
//            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
        clickMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YouTube youtube = null;
                try {
                    youtube = getYouTubeService();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    YouTube.Channels.List channelsListByUsernameRequest = youtube.channels().list("snippet,contentDetails,statistics");
                    channelsListByUsernameRequest.setForUsername("GoogleDevelopers");

                    ChannelListResponse response = channelsListByUsernameRequest.execute();
                    Channel channel = response.getItems().get(0);
                    System.out.printf(
                            "This channel's ID is %s. Its title is '%s', and it has %s views.\n",
                            channel.getId(),
                            channel.getSnippet().getTitle(),
                            channel.getStatistics().getViewCount());
                } catch (GoogleJsonResponseException e) {
                    e.printStackTrace();
                    System.err.println("There was a service error: " +
                            e.getDetails().getCode() + " : " + e.getDetails().getMessage());
                } catch (Throwable t) {
                    t.printStackTrace();
                }

            }
        });

    }

    public  Credential authorize() throws IOException {
        // Load client secrets.

        // Build flow and trigger user authorization request.

//        FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
      //  DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore("addsubscription");

      new Authroizess().execute();


        return mCredential;
    }

    public  YouTube getYouTubeService() throws IOException {
        Credential credential = authorize();
        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }



    public  class Authroizess extends AsyncTask<Void,Void,Void>{


        @Override
        protected Void doInBackground(Void... params) {
            try {

                InputStream in =
                        TestingActivity2.class.getResourceAsStream("/client_secret.json");

                GoogleClientSecrets clientSecrets =
                        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

                GoogleAuthorizationCodeFlow flow =
                        new GoogleAuthorizationCodeFlow.Builder(
                                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                                .setAccessType("offline")
                                .build();
                LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();

                Credential credential = new AuthorizationCodeInstalledApp(
                        flow, localReceiver).authorize("user");

                Log.e("testing",""+credential.getRefreshToken());
           mCredential=credential;
            }catch (Exception e){

            }

            return null;
        }
    }



}
