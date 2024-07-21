package com.yaml.pizzeriashopunal.helper;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yaml.pizzeriashopunal.model.ProductsOrders;

import java.util.ArrayList;
import java.util.List;

public class ProductsOrdersFirebaseHelper {
    // Referencia a la base de datos de Firebase
    private DatabaseReference databaseReference;

    // Constructor que inicializa la referencia a la base de datos
    public ProductsOrdersFirebaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference("ProductsOrders");
    }

    // Interfaz para el callback al agregar una orden
    public interface AddProductsOrdersCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Interfaz para el callback al eliminar una orden
    public interface DeleteProductsOrdersCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Método para agregar una orden a la base de datos
    public void addProductsOrders(ProductsOrders productsOrder, ProductsOrdersFirebaseHelper.AddProductsOrdersCallback callback) {
        // Si los productos por orden no tiene un ID, se genera uno nuevo
        if (productsOrder.getId() == null || productsOrder.getId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            productsOrder.setId(newId);
        }

        // Agregar los productos por orden a Firebase
        databaseReference.child(productsOrder.getId()).setValue(productsOrder)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Método para actualizar una orden en la base de datos
    public void updateProductsOrders(ProductsOrders productsOrder) {
        // Verificar si el ID de los productos por orden es nulo o está vacío
        if (productsOrder == null || productsOrder.getId() == null || productsOrder.getId().isEmpty()) {
            System.out.println("El ID de los productos por orden es nulo o vacío. No se puede actualizar los productos por orden.");
            return; // Detener la ejecución si el ID es nulo o vacío
        }

        // Actualizar los productos por orden en Firebase
        databaseReference.child(productsOrder.getId()).updateChildren(productsOrder.toMap());
    }

    // Método para eliminar una orden de la base de datos
    public void deleteProductsOrders(String id, ProductsOrdersFirebaseHelper.DeleteProductsOrdersCallback callback) {
        // Verificar si el ID es nulo o está vacío
        if (id == null || id.isEmpty()) {
            callback.onError(new IllegalArgumentException("ID de los productos por orden es nulo o vacío."));
            return;
        }

        // Eliminar los productos por orden de Firebase
        databaseReference.child(id).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Interfaz para el callback de obtención de usuarios
    public interface GetProductsOrdersCallback {
        void onProductsOrdersRetrieved(List<ProductsOrders> ProductsOrders);
        void onError();
    }

    // Método para obtener todos los usuarios de la base de datos
    public void getAllProductsOrders(final ProductsOrdersFirebaseHelper.GetProductsOrdersCallback callback) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<ProductsOrders> ProductsOrders = new ArrayList<>();
                for (DataSnapshot ProductsOrdersnapshot : dataSnapshot.getChildren()) {
                    ProductsOrders productsOrder = ProductsOrdersnapshot.getValue(ProductsOrders.class);
                    if (productsOrder != null) {
                        ProductsOrders.add(productsOrder);
                    }
                }
                callback.onProductsOrdersRetrieved(ProductsOrders);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError();
            }
        });
    }

    // Interfaz para el callback de obtener una orden por ID
    public interface GetProductsOrdersByIdCallback {
        void onProductsOrdersRetrieved(@Nullable ProductsOrders productsOrder);
        void onError(Exception e);
    }

    // Método para obtener una orden específico por ID
    public void getProductsOrdersById(String productsOrderId, ProductsOrdersFirebaseHelper.GetProductsOrdersByIdCallback callback) {
        // Obtener una referencia a los productos por orden específico por ID
        DatabaseReference productsOrderReference = databaseReference.child(productsOrderId);

        // Añadir un oyente para obtener los productos por orden
        productsOrderReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Obtener los productos por orden
                ProductsOrders productsOrder = dataSnapshot.getValue(ProductsOrders.class);

                // Llamar al callback con los productos por orden obtenida
                callback.onProductsOrdersRetrieved(productsOrder);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // En caso de error, llamar al callback de error
                callback.onError(databaseError.toException());
            }
        });
    }

    // Interfaz para el callback de existencia de un producto
    public interface ProductsOrdersExistsCallback {
        void onProductsOrdersExists(boolean exists);
        void onError();
    }

    // Método para verificar si una orden existe en la base de datos
    public void checkIfProductsOrdersExists(String productsOrderId, ProductsOrdersFirebaseHelper.ProductsOrdersExistsCallback callback) {
        DatabaseReference productsOrderRef = databaseReference.child(productsOrderId);

        productsOrderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean exists = snapshot.exists();
                callback.onProductsOrdersExists(exists);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError();
            }
        });
    }
}
