package org.hocrox.music;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ActivityListResponse;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class Youtube extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    ProgressDialog mProgress;
    ArrayList videoList = new ArrayList();
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call YouTube Data API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {YouTubeScopes.YOUTUBE};

    YouTube youtube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        youtube = getYouTubeService();

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        mCallApiButton = new Button(this);
        mCallApiButton.setText(BUTTON_TEXT);
        mCallApiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallApiButton.setEnabled(false);
                mOutputText.setText("");
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });

        Button mSubscribe = new Button(this);
        mSubscribe.setText("Add Subscription");
        mSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    subscribeToChannel();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        activityLayout.addView(mSubscribe);

        activityLayout.addView(mCallApiButton);

        mOutputText = new TextView(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        mOutputText.setText(
                "Click the \'" + BUTTON_TEXT + "\' button to test the API.");
        activityLayout.addView(mOutputText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling YouTube Data API ...");

        setContentView(activityLayout);

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());


    }

    private YouTube getYouTubeService() {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();


        YouTube youTube = new YouTube.Builder(transport, jsonFactory, mCredential).setApplicationName(
                "youtube-cmdline-uploadvideo-sample").build();
        return youTube;
    }


    /*
     * Print information about all of the items in the playlist.
     *
     * @param size size of list
     *
     * @param iterator of Playlist Items from uploaded Playlist
     */
    private static void prettyPrint(int size, Iterator<PlaylistItem> playlistEntries) {
        System.out.println("=============================================================");
        System.out.println("\t\tTotal Videos Uploaded: " + size);
        System.out.println("=============================================================\n");

        while (playlistEntries.hasNext()) {
            PlaylistItem playlistItem = playlistEntries.next();
            System.out.println(" video name  = " + playlistItem.getSnippet().getTitle());
            System.out.println(" video id    = " + playlistItem.getContentDetails().getVideoId());
            System.out.println(" upload date = " + playlistItem.getSnippet().getPublishedAt());
            System.out.println("\n-------------------------------------------------------------\n");
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            mOutputText.setText("No network connection available.");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    SubscriptionSnippet snippet;

    public void subscribeToChannel() throws IOException {

        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");
        // Credential credential = Auth.authorize(scopes, "addsubscription");

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        try {
            // Authorize the request.
            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(transport, jsonFactory, mCredential).setApplicationName(
                    "youtube-cmdline-addsubscription-sample").build();

            // We get the user selected channel to subscribe.
            // Retrieve the channel ID that the user is subscribing to.
            String channelId = getChannelId();
            System.out.println("You chose " + channelId + " to subscribe.");

            // Create a resourceId that identifies the channel ID.
            ResourceId resourceId = new ResourceId();
            resourceId.setChannelId(channelId);
            resourceId.setKind("youtube#channel");

            // Create a snippet that contains the resourceId.

            snippet = new SubscriptionSnippet();
            Log.e("testing resource", "" + resourceId);
            snippet.setResourceId(resourceId);

            // Create a request to add the subscription and send the request.
            // The request identifies subscription metadata to insert as well
            // as information that the API server should return in its response.

            new Async().execute();

        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage() + ">>" + e.getCause());
            e.printStackTrace();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }


    }

    public class Async extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            /*Subscription subscription = new Subscription();
            subscription.setSnippet(snippet);

            try {
                YouTube.Subscriptions.Insert subscriptionInsert =
                        youtube.subscriptions().insert("snippet,contentDetails", subscription);
                Subscription returnedSubscription = null;
                returnedSubscription = subscriptionInsert.execute();
                System.out.println("\n================== Returned Subscription ==================\n");
                System.out.println("  - Id: " + returnedSubscription.getId());
                System.out.println("  - Title: " + returnedSubscription.getSnippet().getTitle());

            } catch (Exception e) {
                e.printStackTrace();
            }*/
          /*  YouTube youtube = getYouTubeService();
            try {
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("part", "snippet");
                parameters.put("regionCode", "IN");

                YouTube.VideoCategories.List videoCategoriesListRequest = youtube.videoCategories().list(parameters.get("part").toString());
                if (parameters.containsKey("regionCode") && parameters.get("regionCode") != "") {
                    videoCategoriesListRequest.setRegionCode(parameters.get("regionCode").toString());
                }

                VideoCategoryListResponse response = videoCategoriesListRequest.execute();
                Log.e("sahil videos",""+response);

                System.out.println(response);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
*/


            YouTube youtube = getYouTubeService();
            try {
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("part", "snippet,contentDetails");
                parameters.put("channelId", "UCfWcmXJSiWHN3MHTSE3piIQ");
                parameters.put("maxResults", "25");

                YouTube.Activities.List activitiesListRequest = youtube.activities().list(parameters.get("part").toString());
                if (parameters.containsKey("channelId") && parameters.get("channelId") != "") {
                    activitiesListRequest.setChannelId(parameters.get("channelId").toString());
                }

                if (parameters.containsKey("maxResults")) {
                    activitiesListRequest.setMaxResults(Long.parseLong(parameters.get("maxResults").toString()));
                }

                activitiesListRequest.setChannelId("UCfWcmXJSiWHN3MHTSE3piIQ");
                ActivityListResponse response = activitiesListRequest.execute();
                Log.e("sahil videos", "" + response);
                System.out.println(response);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.toString());
                    JSONArray jsonArray = jsonObject.getJSONArray("items");

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        JSONObject contentDetails = jsonObject1.getJSONObject("contentDetails");
                        JSONObject snipets = jsonObject1.getJSONObject("snippet");
                        JSONObject thumbnails = snipets.getJSONObject("thumbnails");
                        JSONObject aDefault = thumbnails.getJSONObject("default");
                        String url = aDefault.getString("url");


                        JavaModel javaModel = new JavaModel();
                        javaModel.setThumbnail(url);
                        javaModel.setVideoName(snipets.getString("channelTitle"));
                        videoList.add(javaModel);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } catch (IOException e1) {
                e1.printStackTrace();
            }











           /*
           YouTube youtube = getYouTubeService();
            try {
                HashMap<String, String> parameters = new HashMap<>();
            *//*    parameters.put("part", "snippet,contentDetails,statistics");
                parameters.put("id", "Ks-_Mh1QhMc");*//*
                parameters.put("part", "snippet");
                parameters.put("regionCode", "US");


                YouTube.VideoCategories.List videoCategoriesListRequest = youtube.videoCategories().list(parameters.get("part").toString());
                if (parameters.containsKey("regionCode") && parameters.get("regionCode") != "") {
                    videoCategoriesListRequest.setRegionCode(parameters.get("regionCode").toString());
                }

                VideoCategoryListResponse response = videoCategoriesListRequest.execute();
                System.out.println(response);









              *//*  YouTube.Videos.List videosListByIdRequest = youtube.videos().list(parameters.get("part").toString());
                if (parameters.containsKey("id") && parameters.get("id") != "") {
                    videosListByIdRequest.setId(parameters.get("id").toString());
                }

                VideoListResponse response = videosListByIdRequest.execute();*//*

              Log.e("testing",""+response);
                System.out.println(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
*/

/*
            try {
                YouTube youtube = getYouTubeService();

                String mime_type = "video*//*";
                String media_filename = "/try.mp4";
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("part", "snippet,status");
             //   parameters.put("key","AIzaSyCBMXTru8-CIM2ZQoLu4UF2ULelC7Mzdcg");

                Video video = new Video();
                VideoSnippet snippet = new VideoSnippet();
                snippet.set("categoryId", "22");
                snippet.set("description", "Description of uploaded video.");
                snippet.set("title", "Test video upload");
                snippet.setChannelId("UCfWcmXJSiWHN3MHTSE3piIQ");

                VideoStatus status = new VideoStatus();
                status.set("privacyStatus", "private");
                video.setSnippet(snippet);
                video.setStatus(status);
                 Log.e("user",""+mCredential.getSelectedAccount().name);
                InputStreamContent mediaContent = new InputStreamContent(mime_type,
                        YouTube.class.getResourceAsStream(media_filename));
                YouTube.Videos.Insert videosInsertRequest = youtube.videos().insert(parameters.get("part").toString(), video, mediaContent);
                MediaHttpUploader uploader = videosInsertRequest.getMediaHttpUploader();
                uploader.setDirectUploadEnabled(false);

                MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                    public void progressChanged(MediaHttpUploader uploader) throws IOException {
                        switch (uploader.getUploadState()) {
                            case INITIATION_STARTED:
                                System.out.println("Initiation Started");
                                break;
                            case INITIATION_COMPLETE:
                                System.out.println("Initiation Completed");
                                break;
                            case MEDIA_IN_PROGRESS:
                                System.out.println("Upload in progress");
                                System.out.println("Upload percentage: " + uploader.getProgress());
                                break;
                            case MEDIA_COMPLETE:
                                System.out.println("Upload Completed!");
                                break;
                            case NOT_STARTED:
                                System.out.println("Upload Not Started!");
                                break;
                        }
                    }
                };
                uploader.setProgressListener(progressListener);

               // videosInsertRequest.setKey("AIzaSyCBMXTru8-CIM2ZQoLu4UF2ULelC7Mzdcg");
                Log.e("token",""+mCredential.getToken());
                videosInsertRequest.setOauthToken(mCredential.getToken());
                Video response = videosInsertRequest.execute();

                Log.e("testing response",""+response);
                System.out.println(response);

            }catch (Exception e){
                Log.e("testing response","exception>>"+e.getCause()+">>>>"+e);

            }*/

            return null;
        }
    }

    private static String getChannelId() throws IOException {

        String channelId = "UCfWcmXJSiWHN3MHTSE3piIQ";


        System.out.print("Please enter a channel id: ");
      /*  BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        channelId = bReader.readLine();*/

        if (channelId.length() < 1) {
            // If nothing is entered, defaults to "YouTube For Developers."
            channelId = "UCtVd0c0tGXuTSbU5d8cSBUg";
        }
        Log.e("testingg", "" + channelId);

        return channelId;
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.youtube.YouTube mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {

            Log.e("testing credential", "" + credential.getSelectedAccountName());
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.youtube.YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("YouTube Data API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call YouTube Data API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch information about the "GoogleDevelopers" YouTube channel.
         *
         * @return List of Strings containing information about the channel.
         * @throws IOException
         */


        private List<String> getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            List<String> channelInfo = new ArrayList<String>();
            ChannelListResponse result = mService.channels().list("snippet,contentDetails,statistics")
                    .setForUsername("springdevelopers")
                    .execute();
            List<Channel> channels = result.getItems();
            if (channels != null) {
                for (int i = 0; i < channels.size(); i++) {

                    Log.e("testingg", "" + channels.get(i) + ">>>" + channels.get(i).getId() + ">>>" + channels.get(i).getSnippet().getTitle());
                    Channel channel = channels.get(0);
                    channelInfo.add("This channel's ID is " + channel.getId() + ". " +
                            "Its title is '" + channel.getSnippet().getTitle() + ", " +
                            "and it has " + channel.getStatistics().getViewCount() + " views.");

                  /*  HashMap<String, String> parameters = new HashMap<>();
                    parameters.put("part", "snippet,contentDetails,statistics");
                    parameters.put("id", "Ks-_Mh1QhMc");

                    YouTube.Videos.List videosListByIdRequest = youtube.videos().list(parameters.get("part").toString());
                    if (parameters.containsKey("id") && parameters.get("id") != "") {
                        videosListByIdRequest.setId(parameters.get("id").toString());
                    }

                    VideoListResponse response = videosListByIdRequest.execute();
*/
                    //Log.e("testing",""+response);
                }
            }
            return channelInfo;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                output.add(0, "Data retrieved using the YouTube Data API:");
                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            1001);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

}
