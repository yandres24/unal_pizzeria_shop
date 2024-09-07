package com.yaml.pizzeriashopunal.model.helper;

import static com.yaml.pizzeriashopunal.utils.constants.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.yaml.pizzeriashopunal.model.Products;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Método para crear las tablas de la base de datos
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableProductsSQL = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                "id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name" + " TEXT, " +
                "price" + " REAL, " +
                "amount" + " INTEGER)";
        db.execSQL(createTableProductsSQL);

        String createTableOrdersSQL = "CREATE TABLE " + TABLE_ORDERS + " (" +
                "id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id" + " TEXT, " +
                "date" + " REAL)";
        db.execSQL(createTableOrdersSQL);

        String createTableUsersSQL = "CREATE TABLE " + TABLE_USERS + " (" +
                "id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id" + " TEXT, " +
                "product_id" + " TEXT, " +
                "amount" + " INTEGER, " +
                "price" + " REAL)";
        db.execSQL(createTableUsersSQL);

        String createTableProductsOrdersSQL = "CREATE TABLE " + TABLE_PRODUCTSORDERS + " (" +
                "id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email" + " TEXT, " +
                "phone" + " TEXT, " +
                "username" + " TEXT, " +
                "lastname" + " TEXT, " +
                "date" + " TEXT)";
        db.execSQL(createTableProductsOrdersSQL);
    }

    // Método para manejar la actualización de la base de datos
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTSORDERS);
        onCreate(db);
    }

    // Método para insertar un registro en una determinada tabla
    public boolean insert(String tableName, ContentValues values) {
        Object id = values.get(COLUMN_ID);
        if(id != null) {
            if (isExist(id.toString(), tableName)) {
                Log.i("DatabaseHelper", "Registro con ID " + values.get(COLUMN_ID).toString() + " ya existe.");
                return true; // Detener la ejecución si el registro ya existe
            }
        }

        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.insert(tableName, null, values);
            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al agregar producto", e);
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    // Método para obtener todos los productos de la base de datos
    public List<ContentValues> getAll(String tableName) {
        List<ContentValues> returnList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(tableName, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String[] columnNames = cursor.getColumnNames();
            do {
                ContentValues values = new ContentValues();
                for(String column : columnNames) {
                    int index = cursor.getColumnIndex(column);
                    String value = cursor.getString(index);
                    values.put(column, value);
                }
                returnList.add(values);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return returnList;
    }

    // Método para actualizar un producto en la base de datos
    public boolean update(String idPk, String tableName, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsUpdated = db.update(tableName, values, COLUMN_ID + "=?", new String[]{String.valueOf(idPk)});
        db.close();
        if (rowsUpdated == 0) {
            Log.e("DatabaseHelper", "No se actualizó ninguna fila. ID del registro inválido.");
        }
        return true;
    }

    // Método para eliminar un registro de la base de datos
    public boolean delete(String idPk, String tableName) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsDeleted = db.delete(tableName, COLUMN_ID + "=?", new String[]{idPk});
            if (rowsDeleted > 0) {
                Log.i("DatabaseHelper", "Registro eliminado exitosamente.");
            } else {
                Log.e("DatabaseHelper", "No se eliminó ningún registro. ID no válido: " + idPk);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al eliminar el registro", e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return true;
    }

    // Método para verificar si un registro existe en la base de datos
    public boolean isExist(String idPk, String tableName) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean exists = false;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName + " WHERE " + COLUMN_ID + "=?", new String[]{idPk});
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                exists = count > 0;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error verificando existencia de un registro", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return exists;
    }
}
