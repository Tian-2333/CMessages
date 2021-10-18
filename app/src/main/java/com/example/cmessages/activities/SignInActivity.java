package com.example.cmessages.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmessages.R;
import com.example.cmessages.databinding.ActivitySignInBinding;
import com.example.cmessages.utilities.Constants;
import com.example.cmessages.utilities.PreferenceManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.UUID;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    SignInButton signInButton;
    GoogleSignInClient sClient;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        binding.gmsSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gmsCall();
            }
        });

        prepareGso();

        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent  =new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(binding.getRoot());
        setListeners();
    }

    private void prepareGso() {
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("532615131798-p81haq74vlie610kc2h1acfv1jvomgtu.apps.googleusercontent.com")
                .requestEmail()
                .build();
        sClient = GoogleSignIn.getClient(this, gso);
    }

    private void gmsCall() {
        Intent signInIntent = sClient.getSignInIntent();
        startActivityForResult(signInIntent, 999);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 999) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);

            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            //User has signed in
                            signInOrSignUpWithGoogle(user);
                            Toast.makeText(SignInActivity.this, "Sign In Successful. User: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());

                            //Sign in failed
                            Toast.makeText(SignInActivity.this, "Sign In Failed. Error: " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInOrSignUpWithGoogle(FirebaseUser user) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, user.getEmail())
                .get()
                .addOnCompleteListener(task-> {
                    if(task.isSuccessful() && task.getResult() != null &&
                            task.getResult().getDocuments().size() >0) {
                        //User already exists, sign in like normal
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent;
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        //User doesn't exist, create an account for them and sign them in
                        String defaultGooglePictureBase64 = "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k1PsaUUHRR/ezZBeair4el9CnLktVz2IXy8FXNxjnjp0LSyfxbsyy6ooRZXu9RQOOJPFfXMPiFIuqip6ytP4KmAda9qn/OLds4yNr+xx8e5QlMbRW65tvBo6DrVqvb9dIpSurtTFstuBQlwEAZbg/NANagJC5yTXGzKFVuw36jcVzU89U4gZHJNTFvyVAFSWHoxuRRyxeqgpkuLnEvzfKpKwT9JuYmN+GraWf/ScPYFDeXVf7N7fulKk89zKvIZLTBhijkKCCy9Lq0ZMFwZbrQSEkqS6I+ipheqNvHONBZ3pbP52tcucZ7wvBuh71IPCzNXb39X1c+arj5+Y0aupHVxTreGGaiXnlPi6gAABkb2+rMVf3RJStR7urg6sq8Iyf83v/9oADAMBAAIAAwAAABDzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz3LXfzzzzzzzzzzzzzzzzzzzzzzzzzLb5/32+3n3zzzzzzzzzzzzzzzzzzzznd3333333321/wB888888888888888884F999999999999s88888888888888885l999999y219995c8888888888888888/99998Wc88tv85c8888888888888888t/9996/wDPPPPPDHPPPPPPPPPPPPPPPPPK/wBb37Tzzzzzzzzzzzzzzzzzzzzzzzzzz/8A/s988888888888888888888888888/8Af/8A93zzzzw444444445nzzzzzzzzzzy3/8A/wD1vPPPPP8A/wD/AP8A/wD/AP6PPPPPPPPPPPKf/wD/APW88888/wD/AP8A/wD/AP8A/r88888888888/8Af/8A/Xzzzzz/AP8A/wD/AP8A/wD/AK888888888888//APKHvPPPPDDDDDN//wD+/wA88888888888r+98ud888888885d//AP8A/wA888888888884P888/88888885G/8A/wCxzzzzzzzzzzzzzxnzzzz/AO688wkt0/8A/wD/AM8888888888888tT888888cM8888+//AHPPPPPPPPPPPPPPPLk/PPPPPPPPPPPgH/PPPPPPPPPPPPPPPPLedvPPPPPPPPInvPPPPPPPPPPPPPPPPPPPDeNvfPOfJHfPPPPPPPPPPPPPPPPPPPPPPPPPTTX/ADzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz/xAApEQEAAQEFCAMBAQEAAAAAAAABEQAhMVFhkRAgMEBBcbHRgcHwoXDh/9oACAEDAQE/EP8ALhKCi6LRpvC0aRQkc1OItKthyPf/ACrrC52+bKJgwbRYE1d4drPFKW/G+z1UwB+x5UFQX0kLE/LaIFBlwXhSONNO1NXxj2pEYeSOhK0Df48O3viwgseHUz+v0cgCoL6DrW/LL3v2GJ8lWGL8m7ecdx9v62nqSvIyYrD+sfW65WApzYM36PelOW740u2vS58/V1CQJMT7PVC1kavOO4+39bT1JXkVM9aj7ECI3NKhi1KPsdDfkjfedKepK8lIr2p2S2ugr52I3gLaWXHQwP1/NwLG3XauDxqBHE9umr45sJYoQA3JFhZpvZ7Ws9rWe1rPa1ntaz2tZ7Ws9rWe1pgJ4gkMzdcjx1Hw4ihMzdMI4/i4gwzSgJuQ75145tuXFkWFmm2UWep6HbF/nelCltW48OyHTkUQK96RrAdakPsX0Za7GK8KjS/rwSV0jK93IhjbxVM9KRlgLjoe++72lj8b8gIKbILtyM4+U1/e7ImGguHyVcc7b+hVl2n+VKGhG7cxMfHHQSGn6g/mW+XBa0petdwYuqTY01xyLoJGnLzw78V/I60AuDkkEhonw+qchh4IKwV00MOtGgQcqRBNC261XhLtbSKBG0FsKvZ8eaVbF2q79uPXmkG+l7xpQdw0oBd/l/8A/8QAKhEBAAIAAwcCBwEAAAAAAAAAAQARITFBIDBAUWGRsRBxcIGhwdHh8fD/2gAIAQIBAT8Q+Fy1nE9SD6II5cUBaqCYDvM/Yq4vqKZTK40R2g1u+FWsWV+WI7V7lFaqW+WZ8EA0I5Rgb1GHAlrGOqMjbFyGI5jsurcoB0OBr+tsu0Wws1MreuXrgOKjtGMdW5QDocDlXj+HzYIt1GyAoG2GqAOhwVWqYvg+/aDnpJVYP0PoiohUuLsGg12w836tTEOep785gjE04sArSKrzW9jnEl98drpuxOm7E6bsTpuxOm7E6bsTpuxOm7E6bsQRxjWBX+y3nQReNmudA34sPU++86iLxs1zqG/8xvACtYqvMa2OcQV2w39HOP2d7QNFvvj5v1ocPk1ffl5jCqjTauXLly41/V+tizlre/8AN7domD5Pv3h0rWUeN9J+/QUbIVm5N63iCMo2LgZGHbe5V4/j8mV76nV/WwisgFm2VK1mMcVm/bYDXNPeLeLvrcOyismjOQPrmDCc0ofEcz8S8HuanvsciPL9fngLlmW2FkxbXYCNTC/lPP2/HoWscunWLeLwCApi4jLeCqzOX9wZP5/35idLXg7sZUqdznGcZACjhRaEPVNEiJn61cyIj6plpxfSnSgV8L//xAArEAEAAQEFBwUBAQEBAAAAAAABEQAhMUFRYTBAcYGRocEgULHR8BDh8ZD/2gAIAQEAAT8Q/wDN2bsrI+VIRM/JDV98lX4KuDmQ8VcY/JbUTa13hH3cJC+DsUtJolwUcYJXtTSNcDXrJ6VIyj/ATHriI0wA6DUEF8C9yHvS2YgR1R8tNIrprfIGJ5e4EyBKrAFIyBsug64+U1bCWeY1b3rFKrLa7ZUJZ0WRickpBBrJJX4c7NaLGkokTMfbLPxsnsXQOFrpU+U+DOJi6s7memZT2Aw4kVOeSy6PvcL/AGkqWSuAq0BsiWmTvwupVVWV3UURGExp3VOOJk78bqDNUvIns0DTDkIHnCrb8y/m6uvxvNvKmXWOuR160R+xI2LkYPsiNrYC/wCAzamf1gWZAwN6RXeXBxTAKGXfWW+AMD2OT0FgdCP1lXKABwAZb0iu8uDimAUAEgqlp4DA9jLsw/AKt+UkWZzq4v1tQVAJXAqBjXFR1irrDgD5avscA/DU/GGMLrEUiAheJGwRXeXBxTAKACQVS08BgeyJjghcYcPlwNmgAqYAxq2nJdI0v9YotB77z0tOq0OETi51D0wZ91lDrQ6r42hrJ2ijxFbBB43+k05RUIQj6EV3lwcUwCgAkFUtPAYHsk8QSQ3/AEZ6pSqqsreuyOwtgVjoMXbWmw4rSF4ZOGzcNiRY3jmaNIpAwboGL8L/ABFd5cHFMAoAJBVLTwGB7IyUASrgUzTezA48W157JAwsLscjlNOuVFjSAQBkG1NdcHIMkoCwZBW8Zb+41oAJBVLTwGB7LajHSYvrnUSr23PpaQOkIXbCc8oNu4FnkYX33e6ytT2S/vP8gBCZg+zSr9hc+oGULELDx4O7wfdrTY6gsO/oFheBpzUL5fv0SgIhl3e+DVKK8KbAPdowbWfA/wC9vSkkNpQxC9wHw4UyUDCJCfwrPxlbxuc7+Zl7vbnJ5nz630RlgL9Gk8JTLo2oeA0KgYC4C493t/mQcJs2AP1kxct8cg67T9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4r9R4o7iI4JO8a3fQbGxSFnrHjf7d5j8ThvE90upGxzvJuMb/AGhfaXzvDj8b9iIWQO2/yBk+y87wZ/G/YqVmHtv8t4tjut0J2Od7Nxjf5BzfYPG8a3fUbG1SVnrPnf5gkfMTxvNn8QDhNmwF8MGt6Xx1Om/2JxP4nHebM4PG+PW6MIWFu1ajRFEz2AOC0KAQC5HHZ6DpWg6VoOlaDpWg6VoOlaDpWg6VoOlaDpQkgSTX/bYCHKAGbVy+3gEbzGBaz4P/ADv6WwluoopcYJw+6RIoyqyv8K1NgG9L3K7kZ7rfYAtCzqV6bCXyQA0tvjerDZ6AtO/oPPfFp6Xk48f16JYkQW/vP2IUeEQ2I7mWg6hgAvaZy2BcDZ9uK7CAkh4j9LrvcLEd0v7z/Ju5Li9+iruZc3eoEaWzWPhxdni7nclIof8AsfjjsbMZ3Cu7Aee92J/MxffKrNNwt/2tOxtythG5MJ3MGzycbuO4IoDSa9zdD/KYos7FdhOHCaYGLyJeVBQAJgBAb2TICEbkonG/WLw5WnLZKHQhNrkM5r1zosaSiRMx2zURLB75D5paePbhgBgbG4Av+baukHN32YZJszNzx1CkRRITDZCZSyizisXbSiZ4ynJ+mzvOaEFSGnPzL5elOKuTlWa7GAbp6tXZXlmxebLvyYBgXmPD5cTZpQpEtEwouyQvgaXus0UlL7742uoUWMlgK6enR+pHeiAk4TLmeVFhrYP0BYdWsjm3XAXHL+jnsHhsUUuLubcc89/GEs7EamQnwrPuMf8AdqKMljUZHmBjpNXa3FH5q+uWh8FS0E4RHSabvd6S+sY9V3cyYZDVuObhQEzLsBYHsAFRNoFuAKvUwDgg5byWUM/1Y0MGBmuQVpwrtxXhkexPCtiB/oMyotNaFuaMTehqG+5rVqxYVjh4Pnp7JJix5gD+mrXx2Xk6OnzvKQjsGBmuBrUioZBYfAfPb2YElQORqzVbbwNMnfjfSIw2O7X99A7Ghm6HOK5/0G1ctPabVjtjsXc42OtMpJsSc8HR3O4YBMePDWmjb4Sx8nA6tFK+BwDIPa2QkIRJEpSW9t4uODlFWUpnmNS86RSQw37acwmGAOKsObTEQL5rea8HOs6wQt1Le8/cAFX4DsoaUSbwh4SQnel1A4hPSDUxEv8AQRHrav39gVHKzBR5LPao4Vi99WI6NPIz/ajsos7QBAcvdpO08j5UjM38kFX/AMkT4aFdzVeavwcH5VB2P+AKLCz/AM3f/9k=";
                        HashMap<String, Object> newUser = new HashMap<>();
                        newUser.put(Constants.KEY_NAME, user.getDisplayName());
                        newUser.put(Constants.KEY_EMAIL, user.getEmail());
                        newUser.put(Constants.KEY_PASSWORD, UUID.randomUUID().toString()); //To prevent having same password for everyone using GMS
                        newUser.put(Constants.KEY_IMAGE, defaultGooglePictureBase64);
                        database.collection(Constants.KEY_COLLECTION_USERS)
                                .add(newUser)
                                .addOnSuccessListener(documentReference -> {
                                    loading(false);
                                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                                    preferenceManager.putString(Constants.KEY_NAME, user.getDisplayName());
                                    preferenceManager.putString(Constants.KEY_IMAGE, defaultGooglePictureBase64);
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(exception -> {
                                    loading(false);
                                    showToast(exception.getMessage());
                                });
                    }

                });
    }

    private void setListeners() {
        binding.CreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
        binding.buttonLoginIn.setOnClickListener(v-> {
            if(isValidSignInDetails()){
                login();
            }
        });

    }

    private void login() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task-> {
                    if(task.isSuccessful() && task.getResult() != null &&
                    task.getResult().getDocuments().size() >0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent;
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }else{
                        loading(false);
                        showToast("Login failed");
                    }

                });

    }


    private void loading(Boolean isLoading){
        if(isLoading) {
            binding.buttonLoginIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonLoginIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private Boolean isValidSignInDetails(){
        if(binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Please enter your email");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Please enter a vaild email");
            return false;
        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Please enter your password");
            return false;
        }else{
            return true;
        }
    }
}