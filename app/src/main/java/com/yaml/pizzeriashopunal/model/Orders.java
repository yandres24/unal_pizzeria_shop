package com.yaml.pizzeriashopunal.model;

import java.util.HashMap;
import java.util.Map;

public class Orders {
    // Atributos de la clase Orden
    private String id; // Identificador único de la orden
    private String user_id; // Identificador único del usuario
    private String date; // Fecha de producto

    // Constructor vacío necesario para ciertas integraciones como Firebase
    public Orders() {
    }

    // Constructor para inicializar una orden solo con id y user id
    public Orders(String id, String user_id, String date) {
        this.id = id; // Asigna el nombre proporcionado
        this.user_id = user_id; // Asigna el precio proporcionado
        this.date = date; // Asigna la fecha de la orden
    }

    // Obtiene el ID de la orden
    public String getId() {
        return id;
    }

    // Establece el ID de la orden
    public void setId(String id) {
        this.id = id;
    }

    // Obtiene el ID del usuario de la orden
    public String getUser_id() {
        return user_id;
    }

    // Establece el ID del usuario de la orden
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    // Obtiene la fecha de la orden
    public String getDate() {
        return date;
    }

    // Establece la fecha de la orden
    public void setDate(String date) {
        this.date = date;
    }

    // Convierte las propiedades del producto en un mapa para almacenamiento o envío
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id); // Agrega el ID al mapa
        result.put("user_id", user_id); // Agrega el id del usuario al mapa
        result.put("date", date); // Agrega la fecha al mapa
        return result; // Devuelve el mapa con las propiedades de una orden
    }
}
