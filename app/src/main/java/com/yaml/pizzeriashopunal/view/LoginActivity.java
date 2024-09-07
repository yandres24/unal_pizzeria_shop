package com.yaml.pizzeriashopunal.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.yaml.pizzeriashopunal.R;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button googleLoginButton;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar los elementos de la interfaz de usuario
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        googleLoginButton = findViewById(R.id.google_login_button);

        // Inicializar la instancia de FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Configurar opciones de inicio de sesión de Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Crear un cliente de inicio de sesión de Google con las opciones configuradas
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configurar el listener para el botón de inicio de sesión
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el nombre de usuario y la contraseña ingresados
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                // Manejar el inicio de sesión
                handleLogin(username, password);
            }
        });

        // Configurar el listener para el botón de inicio de sesión de Google
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cerrar sesión de Google antes de iniciar sesión con Google
                signOut();
                // Iniciar sesión con Google
                signIn();
            }
        });
    }

    // Método para manejar el inicio de sesión con correo electrónico y contraseña
    private void handleLogin(String username, String password) {
        // Verificar que los campos no estén vacíos
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Iniciar sesión con Firebase Authentication utilizando el correo electrónico y la contraseña
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Verificar si el inicio de sesión fue exitoso
                        if (task.isSuccessful()) {
                            // Obtener el usuario actual de Firebase
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        } else {
                            // Mostrar mensaje de error si el inicio de sesión falló
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Método para iniciar sesión con Google
    private void signIn() {
        // Iniciar la intención de inicio de sesión con Google
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    // Método para cerrar sesión con Google
    private void signOut() {
        // Cerrar sesión del cliente de inicio de sesión de Google
        mGoogleSignInClient.signOut();
    }

    // Listener para el resultado del inicio de sesión de Google
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Verificar si el resultado del inicio de sesión fue exitoso
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    // Obtener la cuenta de Google desde la intención
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        // Obtener la cuenta de Google y autenticar con Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        // Mostrar mensaje de error si el inicio de sesión con Google falló
                        Toast.makeText(LoginActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    // Método para autenticar con Firebase utilizando la credencial de Google
    private void firebaseAuthWithGoogle(String idToken) {
        // Obtener la credencial de autenticación de Google
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        // Iniciar sesión con Firebase utilizando la credencial de Google
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Verificar si el inicio de sesión fue exitoso
                        if (task.isSuccessful()) {
                            // Obtener el usuario actual de Firebase
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            // Redirigir al usuario a la actividad de inicio de sesión exitoso
                            Intent intent = new Intent(LoginActivity.this, ProductActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Mostrar mensaje de error si el inicio de sesión falló
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
