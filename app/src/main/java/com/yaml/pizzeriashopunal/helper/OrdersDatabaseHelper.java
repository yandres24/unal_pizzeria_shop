package com.yaml.pizzeriashopunal.helper;

import static com.yaml.pizzeriashopunal.utils.constants.TABLE_ORDERS;
import static com.yaml.pizzeriashopunal.utils.constants.TABLE_PRODUCTS;

import android.content.ContentValues;
import android.content.Context;

import com.yaml.pizzeriashopunal.model.Orders;
import com.yaml.pizzeriashopunal.model.Users;

import java.util.ArrayList;
import java.util.List;

public class OrdersDatabaseHelper {
    private DatabaseHelper databaseHelper;
    public OrdersDatabaseHelper(Context context){
        databaseHelper = new DatabaseHelper(context);
    }

    // Método para agregar una orden a la base de datos
    public boolean addOrder(Orders order) {
        ContentValues values = new ContentValues();
        values.put("id", order.getId());
        values.put("user_id", order.getUser_id());
        values.put("date", order.getDate());
        return databaseHelper.insert(TABLE_ORDERS, values);
    }

    // Método para obtener todos las ordenes de la base de datos
    public List<Orders> getAllOrders() {
        List<Orders> getOrders = new ArrayList<>();
        List<ContentValues> responseHelper = databaseHelper.getAll(TABLE_ORDERS);
        for(ContentValues itemList : responseHelper) {
            Orders orders = new Orders(itemList.get("id").toString(), itemList.get("user_id").toString(), itemList.get("date").toString());
            getOrders.add(orders);
        }
        return getOrders;
    }

    // Método para actualizar una orden en la base de datos
    public boolean updateOrder(Orders order) {
        ContentValues values = new ContentValues();
        values.put("id", order.getId());
        values.put("user_id", order.getUser_id());
        values.put("date", order.getDate());
        return databaseHelper.update(order.getId(), TABLE_ORDERS, values);
    }

    // Método para eliminar un producto de la base de datos
    public boolean deleteOrder(String id) {
        return databaseHelper.delete(id, TABLE_ORDERS);
    }

    // Método para verificar si una orden existe en la base de datos
    public boolean orderExists(String id) {
        return databaseHelper.isExist(id, TABLE_ORDERS);
    }
}
