package com.yaml.pizzeriashopunal.helper;

import static com.yaml.pizzeriashopunal.utils.constants.*;

import android.content.ContentValues;
import android.content.Context;

import com.yaml.pizzeriashopunal.model.Products;

import java.util.ArrayList;
import java.util.List;

public class ProductsDatabaseHelper {
    private DatabaseHelper databaseHelper;
    public ProductsDatabaseHelper(Context context){
        databaseHelper = new DatabaseHelper(context);
    }

    // Método para agregar un producto a la base de datos
    public boolean addProduct(Products product) {
        ContentValues values = new ContentValues();
        values.put("id", product.getId());
        values.put("name", product.getName());
        values.put("price", product.getPrice());
        return databaseHelper.insert(TABLE_PRODUCTS, values);
    }

    // Método para obtener todos los productos de la base de datos
    public List<Products> getAllProducts() {
        List<Products> getProducts = new ArrayList<>();
        List<ContentValues> responseHelper = databaseHelper.getAll(TABLE_PRODUCTS);
        for(ContentValues itemList : responseHelper) {
            Products products = new Products(itemList.get("id").toString(), itemList.get("name").toString(), (double)itemList.get("price"), (int)itemList.get("amount"));
            getProducts.add(products);
        }
        return getProducts;
    }

    // Método para actualizar un producto en la base de datos
    public boolean updateProduct(Products product) {
        ContentValues values = new ContentValues();
        values.put("id", product.getId());
        values.put("name", product.getName());
        values.put("price", product.getPrice());
        return databaseHelper.update(product.getId(), TABLE_PRODUCTS, values);
    }

    // Método para eliminar un producto de la base de datos
    public boolean deleteProduct(String id) {
        return databaseHelper.delete(id, TABLE_PRODUCTS);
    }

    // Método para verificar si un producto existe en la base de datos
    public boolean productExists(String id) {
        return databaseHelper.isExist(id, TABLE_PRODUCTS);
    }
}
