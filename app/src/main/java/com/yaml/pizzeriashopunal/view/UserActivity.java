package com.yaml.pizzeriashopunal.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yaml.pizzeriashopunal.R;
import com.yaml.pizzeriashopunal.model.Users;
import com.yaml.pizzeriashopunal.model.helper.UsersDatabaseHelper;
import com.yaml.pizzeriashopunal.model.helper.UsersFirebaseHelper;
import com.yaml.pizzeriashopunal.presenter.adapter.UsersAdapter;
import com.yaml.pizzeriashopunal.utils.monitor.NetworkMonitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserActivity extends AppCompatActivity {
    // Componentes de la interfaz de usuario
    private EditText editTextId, editTextEmail, editTextPhone, editTextUserName, editTextLastName, editTextDate;
    private Button buttonAdd, buttonSichronized;
    private FloatingActionButton buttonGetFirebase, buttonGetSqlite;
    private ListView listViewUsers;

    // Objetos de ayuda
    private UsersDatabaseHelper databaseHelper;
    private com.yaml.pizzeriashopunal.presenter.adapter.UsersAdapter UsersAdapter;
    private UsersFirebaseHelper firebaseHelper;
    private NetworkMonitor networkMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        initializeUIComponents();
        initializeHelpers();
        setUpEventListeners();
        authenticateFirebaseUser();
    }

    // Inicializa los componentes de la interfaz de usuario
    private void initializeUIComponents() {
        editTextId = findViewById(R.id.editTextId);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextUserName = findViewById(R.id.editTextUserName);
        editTextUserName = findViewById(R.id.editTextLastName);
        editTextDate = findViewById(R.id.editTextDate);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonGetFirebase = findViewById(R.id.buttonGetFirebase);
        buttonSichronized = findViewById(R.id.buttonSichronized);
        buttonGetSqlite = findViewById(R.id.buttonGetSqlite);
        listViewUsers = findViewById(R.id.listViewUsers);
        // Inicializar la base de datos y el adaptador
        databaseHelper = new UsersDatabaseHelper(this);
        //databaseHelper.addUser(new Users("1", "prueba", 100.0, 5));
        List<Users> users = databaseHelper.getAllUsers();
        UsersAdapter = new UsersAdapter(this, R.layout.list_item_user, users);
        listViewUsers.setAdapter(UsersAdapter);
    }

    // Inicializa los objetos de ayuda
    private void initializeHelpers() {
        networkMonitor = new NetworkMonitor(this);
        firebaseHelper = new UsersFirebaseHelper();
    }

    // Configura los eventos de los botones
    private void setUpEventListeners() {
        buttonGetSqlite.setOnClickListener(v -> loadUsersFromDatabase());
        buttonSichronized.setOnClickListener(v -> synchronizeData());
        buttonGetFirebase.setOnClickListener(v -> loadUsersFromFirebase(true));
        buttonAdd.setOnClickListener(v -> handleAddOrUpdateUser());
    }

    // Autentica al usuario de Firebase
    private void authenticateFirebaseUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                // Puedes usar el user.getUid() para identificar al usuario si es necesario
            } else {
                Toast.makeText(com.yaml.pizzeriashopunal.view.UserActivity.this, "Error al iniciar sesión anónimo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Maneja la operación de agregar o actualizar usuario
    private void handleAddOrUpdateUser() {
        if (buttonAdd.getText().toString().equalsIgnoreCase("Agregar")) {
            addUser();
        } else {
            saveUser();
        }
    }

    // Métodos relacionados con la carga de datos
    private void loadUsersFromDatabase() {
        List<Users> users = databaseHelper.getAllUsers();
        updateUserList(users, false);
    }

    private void loadUsersFromFirebase(boolean hideButtons) {
        firebaseHelper.getAllUsers(new UsersFirebaseHelper.GetUsersCallback() {
            @Override
            public void onUsersRetrieved(List<Users> users) {
                updateUserList(users, hideButtons);
            }

            @Override
            public void onError() {
                Toast.makeText(com.yaml.pizzeriashopunal.view.UserActivity.this, "Error al obtener usuarios de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserList(List<Users> users, boolean hideButtons) {
        UsersAdapter.clear();
        UsersAdapter.addAll(users);
        UsersAdapter.setHideButtons(hideButtons);
        UsersAdapter.notifyDataSetChanged();
    }

    // Métodos relacionados con la sincronización de datos
    private void synchronizeData() {
        if (!networkMonitor.isNetworkAvailable()) {
            Toast.makeText(com.yaml.pizzeriashopunal.view.UserActivity.this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }
        synchronizeAndRemoveData();
        synchronizeAndLoadData();
    }

    private void synchronizeAndLoadData() {
        List<Users> usersFromSQLite = databaseHelper.getAllUsers();
        synchronizeUsersToFirebase(usersFromSQLite);
        loadUsersFromFirebase(true);
    }

    private void synchronizeAndRemoveData() {
        firebaseHelper.getAllUsers(new UsersFirebaseHelper.GetUsersCallback() {
            @Override
            public void onUsersRetrieved(List<Users> usersFromFirebase) {
                List<Users> usersFromSQLite = databaseHelper.getAllUsers();
                Set<String> sqliteUserIds = new HashSet<>();

                for (Users sqliteUser : usersFromSQLite) {
                    sqliteUserIds.add(sqliteUser.getId());
                }

                List<Users> usersToDeleteFromFirebase = new ArrayList<>();
                for (Users firebaseUser : usersFromFirebase) {
                    if (!sqliteUserIds.contains(firebaseUser.getId())) {
                        usersToDeleteFromFirebase.add(firebaseUser);
                    }
                }

                deleteUsersFromFirebase(usersToDeleteFromFirebase);
            }

            @Override
            public void onError() {
                Toast.makeText(com.yaml.pizzeriashopunal.view.UserActivity.this, "Error al obtener usuarios de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void synchronizeUsersToFirebase(List<Users> usersFromSQLite) {
        for (Users Users : usersFromSQLite) {
            firebaseHelper.checkIfUserExists(Users.getId(), new UsersFirebaseHelper.UserExistsCallback() {
                @Override
                public void onUserExists(boolean exists) {
                    if (exists) {
                        firebaseHelper.updateUser(Users);
                    } else {
                        firebaseHelper.addUser(Users, new UsersFirebaseHelper.AddUserCallback() {
                            @Override
                            public void onSuccess() {
                                // Usero agregado exitosamente
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(com.yaml.pizzeriashopunal.view.UserActivity.this, "Error al agregar usuario a Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onError() {
                    Toast.makeText(com.yaml.pizzeriashopunal.view.UserActivity.this, "Error al verificar existencia del usuario en Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteUsersFromFirebase(List<Users> usersToDeleteFromFirebase) {
        for (Users userToDelete : usersToDeleteFromFirebase) {
            firebaseHelper.deleteUser(userToDelete.getId(), new UsersFirebaseHelper.DeleteUserCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(com.yaml.pizzeriashopunal.view.UserActivity.this, "Usero eliminado de Firebase", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(com.yaml.pizzeriashopunal.view.UserActivity.this, "Error al eliminar usuario de Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        loadUsersFromFirebase(true);
    }

    // Métodos relacionados con la manipulación de usuarios
    private void addUser() {
        if (areFieldsEmpty()) {
            return;
        }

        String email = editTextEmail.getText().toString();
        String phone = editTextPhone.getText().toString();
        String username = editTextUserName.getText().toString();
        String lastname = editTextLastName.getText().toString();
        String date = editTextDate.getText().toString();
        Users newUser = new Users("", email, phone, username, lastname, date);
        databaseHelper.addUser(newUser);
        loadUsersFromDatabase();
        clearInputFields();
        Toast.makeText(this, "Usero agregado exitosamente", Toast.LENGTH_SHORT).show();
    }

    private void saveUser() {
        if (areFieldsEmpty()) {
            return;
        }

        String email = editTextEmail.getText().toString();
        String phone = editTextPhone.getText().toString();
        String username = editTextUserName.getText().toString();
        String lastname = editTextLastName.getText().toString();
        String date = editTextDate.getText().toString();
        Users Users = new Users("", email, phone, username, lastname, date);
        databaseHelper.updateUser(Users);
        loadUsersFromDatabase();
        buttonAdd.setText("Agregar");
        clearInputFields();
        Toast.makeText(this, "Usero actualizado exitosamente", Toast.LENGTH_SHORT).show();
    }

    // Métodos de utilidad
    private boolean areFieldsEmpty() {
        if (editTextEmail.getText().toString().trim().isEmpty() || editTextPhone.getText().toString().trim().isEmpty() || editTextUserName.getText().toString().trim().isEmpty() || editTextLastName.getText().toString().trim().isEmpty() || editTextDate.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void clearInputFields() {
        editTextId.setText("");
        editTextEmail.setText("");
        editTextPhone.setText("");
        editTextUserName.setText("");
        editTextLastName.setText("");
        editTextDate.setText("");
    }

    public void editUser(Users user) {
        editTextId.setText(user.getId());
        editTextEmail.setText(user.getEmail());
        editTextPhone.setText(user.getPhone());
        editTextUserName.setText(user.getUsername());
        editTextLastName.setText(user.getLastname());
        editTextDate.setText(user.getDate());
        buttonAdd.setText("Guardar");
    }

    public void deleteUser(Users user) {
        if (user.isDeleted()) {
            Toast.makeText(this, "Usero ya está eliminado", Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.deleteUser(user.getId());
            loadUsersFromDatabase();
            Toast.makeText(this, "Usero eliminado exitosamente", Toast.LENGTH_SHORT).show();
        }
    }
}