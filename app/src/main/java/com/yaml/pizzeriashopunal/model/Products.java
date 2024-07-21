package com.yaml.pizzeriashopunal.model;

import java.util.HashMap;
import java.util.Map;

public class Products {
    // Atributos de la clase Product
    private String id; // Identificador único del producto
    private String name; // Nombre del producto
    private double price; // Precio del producto
    private int amount; // Indica la cantidad de productos
    private boolean deleted; // Indica si el producto ha sido eliminado

    // Constructor vacío necesario para ciertas integraciones como Firebase
    public Products() {
    }

    // Constructor con parámetros para inicializar un producto con un ID, nombre y precio y cantidad
    public Products(String id, String name, double price, int amount) {
        this.id = id; // Asigna el ID proporcionado
        this.name = name; // Asigna el nombre proporcionado
        this.price = price; // Asigna el precio proporcionado
        this.amount = amount; // Asigna la cantidad de articulos por productos
    }

    // Constructor sin ID, para inicializar un producto solo con nombre y precio
    public Products(String name, double price) {
        this.name = name; // Asigna el nombre proporcionado
        this.price = price; // Asigna el precio proporcionado
    }

    // Getters y setters para los atributos
    // Obtiene el ID del producto
    public String getId() {
        return id;
    }

    // Establece el ID del producto
    public void setId(String id) {
        this.id = id;
    }

    // Obtiene el nombre del producto
    public String getName() {
        return name;
    }

    // Establece el nombre del producto
    public void setName(String name) {
        this.name = name;
    }

    // Obtiene el precio del producto
    public double getPrice() {
        return price;
    }

    // Establece el precio del producto
    public void setPrice(double price) {
        this.price = price;
    }

    //Obtiene la cantidad de articulos de un producto
    public int getAmount() { return amount; }

    //Establece la cantidad de articulos de un producto
    public void setAmount(int amount) { this.amount = amount; }

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
        result.put("name", name); // Agrega el nombre al mapa
        result.put("price", price); // Agrega el precio al mapa
        result.put("amount", amount); // Agrega la cantidad al mapa
        return result; // Devuelve el mapa con las propiedades del producto
    }
}
