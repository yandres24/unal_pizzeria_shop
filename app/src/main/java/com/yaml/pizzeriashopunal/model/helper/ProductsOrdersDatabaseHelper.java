package com.yaml.pizzeriashopunal.model.helper;

import static com.google.common.base.Strings.nullToEmpty;
import static com.yaml.pizzeriashopunal.utils.constants.TABLE_PRODUCTSORDERS;

import android.content.ContentValues;
import android.content.Context;

import com.yaml.pizzeriashopunal.model.ProductsOrders;

import java.util.ArrayList;
import java.util.List;

public class ProductsOrdersDatabaseHelper {
    private DatabaseHelper databaseHelper;
    public ProductsOrdersDatabaseHelper(Context context){
        databaseHelper = new DatabaseHelper(context);
    }

    // Método para agregar un producto de una orden a la base de datos
    public boolean addProductsOrders(ProductsOrders productsForOrder) {
        ContentValues values = new ContentValues();
        values.put("id", productsForOrder.getId());
        values.put("order_id", productsForOrder.getOrder_id());
        values.put("product_id", productsForOrder.getProduct_id());
        values.put("amount", productsForOrder.getAmount());
        values.put("price", productsForOrder.getPrice());
        return databaseHelper.insert(TABLE_PRODUCTSORDERS, values);
    }

    // Método para obtener todos los productos de una orden de la base de datos
    public List<ProductsOrders> getAllProductsForOrder() {
        List<ProductsOrders> getProductsOrders = new ArrayList<>();
        List<ContentValues> responseHelper = databaseHelper.getAll(TABLE_PRODUCTSORDERS);
        for(ContentValues itemList : responseHelper) {
            String amount = nullToEmpty(itemList.get("amount") != null ? (String) itemList.get("amount") : "0");
            String price = nullToEmpty(itemList.get("price") != null ? (String) itemList.get("price") : "0");
            ProductsOrders productsOrders = new ProductsOrders(itemList.get("id").toString(), itemList.get("order_id").toString(), itemList.get("product_id").toString(),
                    Integer.parseInt(amount), Double.parseDouble(price));
            getProductsOrders.add(productsOrders);
        }
        return getProductsOrders;
    }

    // Método para actualizar un producto de una orden en la base de datos
    public boolean updateProductsForOrder(ProductsOrders productsForOrder) {
        ContentValues values = new ContentValues();
        values.put("id", productsForOrder.getId());
        values.put("order_id", productsForOrder.getOrder_id());
        values.put("product_id", productsForOrder.getProduct_id());
        values.put("amount", productsForOrder.getAmount());
        values.put("price", productsForOrder.getPrice());
        return databaseHelper.update(productsForOrder.getId(), TABLE_PRODUCTSORDERS, values);
    }

    // Método para eliminar un producto de la base de datos
    public boolean deleteProduct(String id) {
        return databaseHelper.delete(id, TABLE_PRODUCTSORDERS);
    }

    // Método para verificar si un producto existe en la base de datos
    public boolean productsForOrderExists(String id) {
        return databaseHelper.isExist(id, TABLE_PRODUCTSORDERS);
    }
}
