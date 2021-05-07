package in.palande.dota2teams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.internal.GoogleApiAvailabilityCache;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

public class Dota2TeamsHomeActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private ImageView imageProfile;
    private TextView name;
    private TextView email;
    private EditText dota2FriendId;
    private Button buttonCompleteProfile;
    private Button buttonSignout;

    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;

    private GoogleSignInResult result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dota2_teams_home);

        imageProfile = findViewById(R.id.profile_image);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        dota2FriendId = findViewById(R.id.edit_text_dota2_friend_ID);
        buttonCompleteProfile = findViewById(R.id.button_complete_profile);
        buttonSignout = findViewById(R.id.button_signout);


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        buttonCompleteProfile.setOnClickListener((view) -> {
            try {
                if (result.isSuccess()) {
                    Toast.makeText(Dota2TeamsHomeActivity.this, "Result isSuccess(): " + result.isSuccess(), Toast.LENGTH_LONG);

                    GoogleSignInAccount account = result.getSignInAccount();

                    assert account != null;
                    name.setText(account.getDisplayName());
                    email.setText(account.getEmail());

                    Picasso.get().load(account.getPhotoUrl()).placeholder(R.mipmap.ic_launcher).into(imageProfile);

                    RequestQueue requestQueue = Volley.newRequestQueue(this);
                    String base_URL = "http://192.168.1.7:5000";
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("display_name", account.getDisplayName());
                    jsonBody.put("email", account.getEmail());
                    jsonBody.put("player_id", Long.parseLong("" + dota2FriendId.getText()));
                    jsonBody.put("position_preference", 1);
                    jsonBody.put("id", account.getId());
                    jsonBody.put("id_token", account.getIdToken());
                    final String requestBody = jsonBody.toString();

                    Toast.makeText(Dota2TeamsHomeActivity.this, "URL: " + base_URL + "/api/register_player", Toast.LENGTH_LONG);
//                    https://drive.google.com/open?id=1UW07y3_4fRjLB2OnDnkSIQT3vzQ0UdSU&authuser=palande1996%40gmail.com&usp=drive_fs
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, base_URL + "/api/register_player", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("/api/register_player:", response);
                            Toast.makeText(Dota2TeamsHomeActivity.this, response, Toast.LENGTH_LONG).show();
                            if (response.startsWith("Success")) {
                                startActivity(new Intent(Dota2TeamsHomeActivity.this, InQueueActivity.class));
                                finish();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // As of f605da3 the following should work
                            NetworkResponse response = error.networkResponse;
                            if (error instanceof ServerError && response != null) {
                                try {
                                    String res = new String(response.data,
                                            HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                    // Now you can use any deserializer to make sense of data
//                                    JSONObject obj = new JSONObject(res);
                                    Log.i("/api/register_player:\n", res);
                                    Toast.makeText(Dota2TeamsHomeActivity.this, "There was some error, please try again!", Toast.LENGTH_LONG);
                                } catch (UnsupportedEncodingException e1) {
                                    // Couldn't properly decode data to string
                                    e1.printStackTrace();
                                } // returned data is not JSONObject?

                            }
                        }
                    }) {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            try {
                                return requestBody.getBytes("utf-8");
                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                                return null;
                            }
                        }

                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            String responseString = "";
                            if (response != null) {
                                responseString = new String(response.data);
                                // can get more details such as response.headers
                            }
                            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                        }
                    };

                    requestQueue.add(stringRequest);
                } else {
                    goToMainActivity();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        buttonSignout.setOnClickListener((view) -> {
            Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(status -> {
                if (status.isSuccess())
                    goToMainActivity();
                else
                    Toast.makeText(Dota2TeamsHomeActivity.this, "Logout Failed!", Toast.LENGTH_LONG);
            });
        });
    }

    private void goToMainActivity() {
        startActivity(new Intent(Dota2TeamsHomeActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();

            name.setText(account.getDisplayName());
            email.setText(account.getEmail());

            Picasso.get().load(account.getPhotoUrl()).placeholder(R.mipmap.ic_launcher).into(imageProfile);
        } else {
            goToMainActivity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);

        if (opr.isDone()) {
            result = opr.get();
            handleSignInResult(result);
        } else
            opr.setResultCallback(result -> handleSignInResult(result));
    }
}