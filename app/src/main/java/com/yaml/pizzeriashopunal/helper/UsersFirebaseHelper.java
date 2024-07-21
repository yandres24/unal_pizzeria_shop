package com.yaml.pizzeriashopunal.helper;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yaml.pizzeriashopunal.model.Users;

import java.util.ArrayList;
import java.util.List;

public class UsersFirebaseHelper {
    // Referencia a la base de datos de Firebase
    private DatabaseReference databaseReference;

    // Constructor que inicializa la referencia a la base de datos
    public UsersFirebaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
    }

    // Interfaz para el callback al agregar una orden
    public interface AddUserCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Interfaz para el callback al eliminar una orden
    public interface DeleteUserCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Método para agregar una orden a la base de datos
    public void addUser(Users user, UsersFirebaseHelper.AddUserCallback callback) {
        // Si el usuario no tiene un ID, se genera uno nuevo
        if (user.getId() == null || user.getId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            user.setId(newId);
        }

        // Agregar el usuario a Firebase
        databaseReference.child(user.getId()).setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Método para actualizar una orden en la base de datos
    public void updateUser(Users user) {
        // Verificar si el ID de el usuario es nulo o está vacío
        if (user == null || user.getId() == null || user.getId().isEmpty()) {
            System.out.println("El ID de el usuario es nulo o vacío. No se puede actualizar el usuario.");
            return; // Detener la ejecución si el ID es nulo o vacío
        }

        // Actualizar el usuario en Firebase
        databaseReference.child(user.getId()).updateChildren(user.toMap());
    }

    // Método para eliminar una orden de la base de datos
    public void deleteUser(String id, UsersFirebaseHelper.DeleteUserCallback callback) {
        // Verificar si el ID es nulo o está vacío
        if (id == null || id.isEmpty()) {
            callback.onError(new IllegalArgumentException("ID de el usuario es nulo o vacío."));
            return;
        }

        // Eliminar el usuario de Firebase
        databaseReference.child(id).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Interfaz para el callback de obtención de usuarios
    public interface GetUsersCallback {
        void onUsersRetrieved(List<Users> Users);
        void onError();
    }

    // Método para obtener todos los usuarios de la base de datos
    public void getAllUsers(final UsersFirebaseHelper.GetUsersCallback callback) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Users> Users = new ArrayList<>();
                for (DataSnapshot Usersnapshot : dataSnapshot.getChildren()) {
                    Users user = Usersnapshot.getValue(Users.class);
                    if (user != null) {
                        Users.add(user);
                    }
                }
                callback.onUsersRetrieved(Users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError();
            }
        });
    }

    // Interfaz para el callback de obtener una orden por ID
    public interface GetUserByIdCallback {
        void onUserRetrieved(@Nullable Users user);
        void onError(Exception e);
    }

    // Método para obtener una orden específico por ID
    public void getUserById(String userId, UsersFirebaseHelper.GetUserByIdCallback callback) {
        // Obtener una referencia a el usuario específico por ID
        DatabaseReference userReference = databaseReference.child(userId);

        // Añadir un oyente para obtener el usuario
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Obtener el usuario
                Users user = dataSnapshot.getValue(Users.class);

                // Llamar al callback con el usuario obtenida
                callback.onUserRetrieved(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // En caso de error, llamar al callback de error
                callback.onError(databaseError.toException());
            }
        });
    }

    // Interfaz para el callback de existencia de un producto
    public interface UserExistsCallback {
        void onUserExists(boolean exists);
        void onError();
    }

    // Método para verificar si una orden existe en la base de datos
    public void checkIfUserExists(String userId, UsersFirebaseHelper.UserExistsCallback callback) {
        DatabaseReference userRef = databaseReference.child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean exists = snapshot.exists();
                callback.onUserExists(exists);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError();
            }
        });
    }
}
