package com.yaml.pizzeriashopunal.model;

import java.util.HashMap;
import java.util.Map;

public class Users {
    // Atributos de la clase Usuario
    private String id; // Identificador único del usuario
    private String email; // Correo electronico
    private String phone; // Numero de telefono
    private String username; // Nombres
    private String lastname; // Apellidos
    private String date; // Fecha de nacimiento
    private boolean deleted; // Indica si el producto ha sido eliminado

    // Constructor vacío necesario para ciertas integraciones como Firebase
    public Users() {
    }

    // Constructor para inicializar un usuario
    public Users(String id, String email, String phone, String username, String lastname, String date) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.lastname = lastname;
        this.date = date;
    }

    // Obtiene el ID de la orden
    public String getId() {
        return id;
    }

    // Establece el ID de la orden
    public void setId(String id) {
        this.id = id;
    }

    // Obtiene el correo electronico
    public String getEmail() {
        return email;
    }

    // Establece el correo electronico
    public void setEmail(String email) {
        this.email = email;
    }

    // Obtiene el numero de telefono
    public String getPhone() {
        return phone;
    }

    // Establece el numero de telefono
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Obtiene los nombres del usuario
    public String getUsername() {
        return username;
    }

    // Establece los nombres del usuario
    public void setUsername(String username) {
        this.username = username;
    }

    // Obtiene los apellidos del usuario
    public String getLastname() {
        return lastname;
    }

    // Establece los apellidos del usuario
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    // Obtiene la fecha de nacimiento del usuario
    public String getDate() {
        return date;
    }

    // Establece la fecha de nacimiento del usuario
    public void setDate(String date) {
        this.date = date;
    }

    public boolean isDeleted() {
        return deleted;
    }

    // Establece el estado de eliminación del producto
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id); // Agrega el ID al mapa
        result.put("email", email); // Agrega el email al mapa
        result.put("phone", phone); // Agrega el telefono al mapa
        result.put("username", username); // Agrega los nombres al mapa
        result.put("lastname", lastname); // Agrega los apellidos al mapa
        result.put("date", date); // Agrega la fecha de nacimiento al mapa
        return result; // Devuelve el mapa con las propiedades del producto
    }
}
