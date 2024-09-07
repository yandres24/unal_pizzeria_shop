package com.yaml.pizzeriashopunal.model;

import java.util.HashMap;
import java.util.Map;

public class ProductsOrders {
    // Atributos de la clase Product Orden
    private String id; // Identificador único del producto
    private String order_id; // Identificador único de la orden por producto
    private String product_id; // Identificador único del usuario
    private int amount; // Fecha de producto
    private double price; // Fecha de producto
    private boolean deleted; // Indica si el producto ha sido eliminado

    // Constructor vacío necesario para ciertas integraciones como Firebase
    public ProductsOrders() {
    }

    // Constructor para inicializar una orden con su lista de productos
    public ProductsOrders(String id, String order_id, String product_id, int amount, double price) {
        this.id = id;
        this.order_id = order_id;
        this.product_id = product_id;
        this.amount = amount;
        this.price = price;
    }

    // Obtiene el ID de la orden
    public String getId() {
        return id;
    }

    // Establece el ID de la orden
    public void setId(String id) {
        this.id = id;
    }

    // Obtiene el ID de la orden
    public String getOrder_id() {
        return order_id;
    }

    // Establece el ID de la orden
    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    // Obtiene el ID del producto
    public String getProduct_id() {
        return product_id;
    }

    // Establece el ID del producto
    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    // Obtiene la cantidad de productos comprados
    public int getAmount() {
        return amount;
    }

    // Establece la cantidad de productos comprados
    public void setAmount(int amount) {
        this.amount = amount;
    }

    // Obtiene el precio del prodcuto adquirido
    public double getPrice() {
        return price;
    }

    // Establece el precio del prodcuto adquirido
    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isDeleted() {
        return deleted;
    }

    // Establece el estado de eliminación del producto
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    // Convierte las propiedades del producto en un mapa para almacenamiento o envío
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id); // Agrega el ID al mapa
        result.put("order_id", order_id); // Agrega el id de la orden
        result.put("product_id", product_id); // Agrega el id del producto
        result.put("amount", amount); // Agrega el valor del producto
        result.put("price", price); // Agrega el precio del producto
        return result; // Devuelve el mapa con las propiedades de una orden
    }
}
