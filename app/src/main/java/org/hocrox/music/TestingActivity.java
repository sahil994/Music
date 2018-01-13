package org.hocrox.music;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class TestingActivity extends AppCompatActivity {
    private static YouTube youtube;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

        try {
            // Authorize the request.
            Credential credential = Auth.authorize(scopes, "addsubscription");

            // This object is used to make YouTube Data API requests.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
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
            SubscriptionSnippet snippet = new SubscriptionSnippet();
            snippet.setResourceId(resourceId);

            // Create a request to add the subscription and send the request.
            // The request identifies subscription metadata to insert as well
            // as information that the API server should return in its response.
            Subscription subscription = new Subscription();
            subscription.setSnippet(snippet);
            YouTube.Subscriptions.Insert subscriptionInsert =
                    youtube.subscriptions().insert("snippet,contentDetails", subscription);
            Subscription returnedSubscription = subscriptionInsert.execute();

            // Print information from the API response.
            System.out.println("\n================== Returned Subscription ==================\n");
            System.out.println("  - Id: " + returnedSubscription.getId());
            System.out.println("  - Title: " + returnedSubscription.getSnippet().getTitle());

        } catch (GoogleJsonResponseException e) {
            System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
            e.printStackTrace();

        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            System.err.println("Throwable: " + t.getMessage());
            t.printStackTrace();
        }
    }
    private static String getChannelId() throws IOException {

        String channelId = "";

        System.out.print("Please enter a channel id: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        channelId = bReader.readLine();

        if (channelId.length() < 1) {
            // If nothing is entered, defaults to "YouTube For Developers."
            channelId = "UCtVd0c0tGXuTSbU5d8cSBUg";
        }
        return channelId;
    }
}
