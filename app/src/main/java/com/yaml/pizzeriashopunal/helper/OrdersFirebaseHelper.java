package com.yaml.pizzeriashopunal.helper;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yaml.pizzeriashopunal.model.Orders;
import com.yaml.pizzeriashopunal.model.Products;

import java.util.ArrayList;
import java.util.List;

public class OrdersFirebaseHelper {
    // Referencia a la base de datos de Firebase
    private DatabaseReference databaseReference;

    // Constructor que inicializa la referencia a la base de datos
    public OrdersFirebaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference("orders");
    }

    // Interfaz para el callback al agregar una orden
    public interface AddOrderCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Interfaz para el callback al eliminar una orden
    public interface DeleteOrderCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Método para agregar una orden a la base de datos
    public void addOrder(Orders order, OrdersFirebaseHelper.AddOrderCallback callback) {
        // Si la orden no tiene un ID, se genera uno nuevo
        if (order.getId() == null || order.getId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            order.setId(newId);
        }

        // Agregar la orden a Firebase
        databaseReference.child(order.getId()).setValue(order)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Método para actualizar una orden en la base de datos
    public void updateOrder(Orders order) {
        // Verificar si el ID de la orden es nulo o está vacío
        if (order == null || order.getId() == null || order.getId().isEmpty()) {
            System.out.println("El ID de la orden es nulo o vacío. No se puede actualizar la orden.");
            return; // Detener la ejecución si el ID es nulo o vacío
        }

        // Actualizar la orden en Firebase
        databaseReference.child(order.getId()).updateChildren(order.toMap());
    }

    // Método para eliminar una orden de la base de datos
    public void deleteOrder(String id, OrdersFirebaseHelper.DeleteOrderCallback callback) {
        // Verificar si el ID es nulo o está vacío
        if (id == null || id.isEmpty()) {
            callback.onError(new IllegalArgumentException("ID de la orden es nulo o vacío."));
            return;
        }

        // Eliminar la orden de Firebase
        databaseReference.child(id).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Interfaz para el callback de obtención de ordenes
    public interface GetOrdersCallback {
        void onOrdersRetrieved(List<Orders> orders);
        void onError();
    }

    // Método para obtener todos las ordenes de la base de datos
    public void getAllOrders(final OrdersFirebaseHelper.GetOrdersCallback callback) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Orders> orders = new ArrayList<>();
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    Orders order = orderSnapshot.getValue(Orders.class);
                    if (order != null) {
                        orders.add(order);
                    }
                }
                callback.onOrdersRetrieved(orders);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError();
            }
        });
    }

    // Interfaz para el callback de obtener una orden por ID
    public interface GetOrderByIdCallback {
        void onOrderRetrieved(@Nullable Orders order);
        void onError(Exception e);
    }

    // Método para obtener una orden específico por ID
    public void getOrderById(String orderId, OrdersFirebaseHelper.GetOrderByIdCallback callback) {
        // Obtener una referencia a la orden específico por ID
        DatabaseReference orderReference = databaseReference.child(orderId);

        // Añadir un oyente para obtener la orden
        orderReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Obtener la orden
                Orders order = dataSnapshot.getValue(Orders.class);

                // Llamar al callback con la orden obtenida
                callback.onOrderRetrieved(order);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // En caso de error, llamar al callback de error
                callback.onError(databaseError.toException());
            }
        });
    }

    // Interfaz para el callback de existencia de un producto
    public interface OrderExistsCallback {
        void onOrderExists(boolean exists);
        void onError();
    }

    // Método para verificar si una orden existe en la base de datos
    public void checkIfOrderExists(String orderId, OrdersFirebaseHelper.OrderExistsCallback callback) {
        DatabaseReference orderRef = databaseReference.child(orderId);

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean exists = snapshot.exists();
                callback.onOrderExists(exists);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError();
            }
        });
    }
}
