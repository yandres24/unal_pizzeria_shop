package com.yaml.pizzeriashopunal.helper;

import static com.yaml.pizzeriashopunal.utils.constants.TABLE_PRODUCTS;
import static com.yaml.pizzeriashopunal.utils.constants.TABLE_USERS;

import android.content.ContentValues;
import android.content.Context;

import com.yaml.pizzeriashopunal.model.Products;
import com.yaml.pizzeriashopunal.model.Users;

import java.util.ArrayList;
import java.util.List;

public class UsersDatabaseHelper {
    private DatabaseHelper databaseHelper;
    public void initDataBase(Context context){
        databaseHelper = new DatabaseHelper(context);
    }

    // Método para agregar un usuario a la base de datos
    public boolean addUser(Users user) {
        ContentValues values = new ContentValues();
        values.put("id", user.getId());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("username", user.getUsername());
        values.put("lastname", user.getLastname());
        values.put("date", user.getDate());
        return databaseHelper.insert(TABLE_USERS, values);
    }

    // Método para obtener todos los usuarios de la base de datos
    public List<Users> getAllUsers() {
        List<Users> getUsers = new ArrayList<>();
        List<ContentValues> responseHelper = databaseHelper.getAll(TABLE_USERS);
        for(ContentValues itemList : responseHelper) {
            Users users = new Users(itemList.get("id").toString(), itemList.get("email").toString(), itemList.get("phone").toString(),
                    itemList.get("username").toString(), itemList.get("lastname").toString(), itemList.get("date").toString());
            getUsers.add(users);
        }
        return getUsers;
    }

    // Método para actualizar un usuario en la base de datos
    public boolean updateUser(Users user) {
        ContentValues values = new ContentValues();
        values.put("id", user.getId());
        values.put("email", user.getEmail());
        values.put("phone", user.getPhone());
        values.put("username", user.getUsername());
        values.put("lastname", user.getLastname());
        values.put("date", user.getDate());
        return databaseHelper.update(user.getId(), TABLE_USERS, values);
    }

    // Método para eliminar un usuario de la base de datos
    public boolean deleteUser(String id) {
        return databaseHelper.delete(id, TABLE_USERS);
    }

    // Método para verificar si un usuario existe en la base de datos
    public boolean userExists(String id) {
        return databaseHelper.isExist(id, TABLE_USERS);
    }
}
