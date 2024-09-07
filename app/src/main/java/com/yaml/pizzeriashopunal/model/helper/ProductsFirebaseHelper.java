package com.yaml.pizzeriashopunal.model.helper;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yaml.pizzeriashopunal.model.Products;

import java.util.ArrayList;
import java.util.List;

public class ProductsFirebaseHelper {
    // Referencia a la base de datos de Firebase
    private DatabaseReference databaseReference;

    // Constructor que inicializa la referencia a la base de datos
    public ProductsFirebaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference("products");
    }

    // Interfaz para el callback al agregar un producto
    public interface AddProductCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Interfaz para el callback al eliminar un producto
    public interface DeleteProductCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Método para agregar un producto a la base de datos
    public void addProduct(Products product, AddProductCallback callback) {
        // Si el producto no tiene un ID, se genera uno nuevo
        if (product.getId() == null || product.getId().isEmpty()) {
            String newId = databaseReference.push().getKey();
            product.setId(newId);
        }

        // Agregar el producto a Firebase
        databaseReference.child(product.getId()).setValue(product)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Método para actualizar un producto en la base de datos
    public void updateProduct(Products product) {
        // Verificar si el ID del producto es nulo o está vacío
        if (product == null || product.getId() == null || product.getId().isEmpty()) {
            System.out.println("El ID del producto es nulo o vacío. No se puede actualizar el producto.");
            return; // Detener la ejecución si el ID es nulo o vacío
        }

        // Actualizar el producto en Firebase
        databaseReference.child(product.getId()).updateChildren(product.toMap());
    }

    // Método para eliminar un producto de la base de datos
    public void deleteProduct(String id, DeleteProductCallback callback) {
        // Verificar si el ID es nulo o está vacío
        if (id == null || id.isEmpty()) {
            callback.onError(new IllegalArgumentException("ID del producto es nulo o vacío."));
            return;
        }

        // Eliminar el producto de Firebase
        databaseReference.child(id).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    // Interfaz para el callback de obtención de productos
    public interface GetProductsCallback {
        void onProductsRetrieved(List<Products> products);
        void onError();
    }

    // Método para obtener todos los productos de la base de datos
    public void getAllProducts(final GetProductsCallback callback) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Products> products = new ArrayList<>();
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    Products product = productSnapshot.getValue(Products.class);
                    if (product != null) {
                        products.add(product);
                    }
                }
                callback.onProductsRetrieved(products);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError();
            }
        });
    }

    // Método para obtener un producto específico por ID
    public void getProductById(String productId, GetProductByIdCallback callback) {
        // Obtener una referencia al producto específico por ID
        DatabaseReference productReference = databaseReference.child(productId);

        // Añadir un oyente para obtener el producto
        productReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Obtener el producto
                Products product = dataSnapshot.getValue(Products.class);

                // Llamar al callback con el producto obtenido
                callback.onProductRetrieved(product);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // En caso de error, llamar al callback de error
                callback.onError(databaseError.toException());
            }
        });
    }

    // Interfaz para el callback de obtener un producto por ID
    public interface GetProductByIdCallback {
        void onProductRetrieved(@Nullable Products product);
        void onError(Exception e);
    }

    // Método para verificar si un producto existe en la base de datos
    public void checkIfProductExists(String productId, ProductExistsCallback callback) {
        DatabaseReference productRef = databaseReference.child(productId);

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean exists = snapshot.exists();
                callback.onProductExists(exists);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onError();
            }
        });
    }

    // Interfaz para el callback de existencia de un producto
    public interface ProductExistsCallback {
        void onProductExists(boolean exists);
        void onError();
    }
}
