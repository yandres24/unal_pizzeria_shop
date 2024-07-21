package com.yaml.pizzeriashopunal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yaml.pizzeriashopunal.adapter.ProductsAdapter;
import com.yaml.pizzeriashopunal.helper.ProductsDatabaseHelper;
import com.yaml.pizzeriashopunal.helper.ProductsFirebaseHelper;
import com.yaml.pizzeriashopunal.model.Products;
import com.yaml.pizzeriashopunal.monitor.NetworkMonitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    // Componentes de la interfaz de usuario
    private EditText editTextId, editTextName, editTextPrice;
    private Button buttonAdd, buttonGetFirebase, buttonSichronized, buttonGetSqlite;
    private ListView listViewProducts;

    // Objetos de ayuda
    private ProductsDatabaseHelper databaseHelper;
    private ProductsAdapter ProductsAdapter;
    private ProductsFirebaseHelper firebaseHelper;
    private NetworkMonitor networkMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIComponents();
        initializeHelpers();
        setUpEventListeners();
        authenticateFirebaseUser();
    }

    // Inicializa los componentes de la interfaz de usuario
    private void initializeUIComponents() {
        editTextId = findViewById(R.id.editTextId);
        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonGetFirebase = findViewById(R.id.buttonGetFirebase);
        buttonSichronized = findViewById(R.id.buttonSichronized);
        buttonGetSqlite = findViewById(R.id.buttonGetSqlite);
        listViewProducts = findViewById(R.id.listViewProducts);

        // Inicializar la base de datos y el adaptador
        databaseHelper = new ProductsDatabaseHelper(this);
        List<Products> products = databaseHelper.getAllProducts();
        ProductsAdapter = new ProductsAdapter(this, R.layout.list_item_product, products);
        listViewProducts.setAdapter(ProductsAdapter);
    }

    // Inicializa los objetos de ayuda
    private void initializeHelpers() {
        networkMonitor = new NetworkMonitor(this);
        firebaseHelper = new ProductsFirebaseHelper();
    }

    // Configura los eventos de los botones
    private void setUpEventListeners() {
        buttonGetSqlite.setOnClickListener(v -> loadProductsFromDatabase());
        buttonSichronized.setOnClickListener(v -> synchronizeData());
        buttonGetFirebase.setOnClickListener(v -> loadProductsFromFirebase(true));
        buttonAdd.setOnClickListener(v -> handleAddOrUpdateProduct());
    }

    // Autentica al usuario de Firebase
    private void authenticateFirebaseUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                // Puedes usar el user.getUid() para identificar al usuario si es necesario
            } else {
                Toast.makeText(MainActivity.this, "Error al iniciar sesión anónimo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Maneja la operación de agregar o actualizar producto
    private void handleAddOrUpdateProduct() {
        if (buttonAdd.getText().toString().equalsIgnoreCase("Agregar")) {
            addProduct();
        } else {
            saveProduct();
        }
    }

    // Métodos relacionados con la carga de datos
    private void loadProductsFromDatabase() {
        List<Products> products = databaseHelper.getAllProducts();
        updateProductList(products, false);
    }

    private void loadProductsFromFirebase(boolean hideButtons) {
        firebaseHelper.getAllProducts(new ProductsFirebaseHelper.GetProductsCallback() {
            @Override
            public void onProductsRetrieved(List<Products> products) {
                updateProductList(products, hideButtons);
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, "Error al obtener productos de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProductList(List<Products> products, boolean hideButtons) {
        ProductsAdapter.clear();
        ProductsAdapter.addAll(products);
        ProductsAdapter.setHideButtons(hideButtons);
        ProductsAdapter.notifyDataSetChanged();
    }

    // Métodos relacionados con la sincronización de datos
    private void synchronizeData() {
        if (!networkMonitor.isNetworkAvailable()) {
            Toast.makeText(MainActivity.this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }
        synchronizeAndRemoveData();
        synchronizeAndLoadData();
    }

    private void synchronizeAndLoadData() {
        List<Products> productsFromSQLite = databaseHelper.getAllProducts();
        synchronizeProductsToFirebase(productsFromSQLite);
        loadProductsFromFirebase(true);
    }

    private void synchronizeAndRemoveData() {
        firebaseHelper.getAllProducts(new ProductsFirebaseHelper.GetProductsCallback() {
            @Override
            public void onProductsRetrieved(List<Products> productsFromFirebase) {
                List<Products> productsFromSQLite = databaseHelper.getAllProducts();
                Set<String> sqliteProductIds = new HashSet<>();

                for (Products sqliteProduct : productsFromSQLite) {
                    sqliteProductIds.add(sqliteProduct.getId());
                }

                List<Products> productsToDeleteFromFirebase = new ArrayList<>();
                for (Products firebaseProduct : productsFromFirebase) {
                    if (!sqliteProductIds.contains(firebaseProduct.getId())) {
                        productsToDeleteFromFirebase.add(firebaseProduct);
                    }
                }

                deleteProductsFromFirebase(productsToDeleteFromFirebase);
            }

            @Override
            public void onError() {
                Toast.makeText(MainActivity.this, "Error al obtener productos de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void synchronizeProductsToFirebase(List<Products> productsFromSQLite) {
        for (Products Products : productsFromSQLite) {
            firebaseHelper.checkIfProductExists(Products.getId(), new ProductsFirebaseHelper.ProductExistsCallback() {
                @Override
                public void onProductExists(boolean exists) {
                    if (exists) {
                        firebaseHelper.updateProduct(Products);
                    } else {
                        firebaseHelper.addProduct(Products, new ProductsFirebaseHelper.AddProductCallback() {
                            @Override
                            public void onSuccess() {
                                // Producto agregado exitosamente
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(MainActivity.this, "Error al agregar producto a Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onError() {
                    Toast.makeText(MainActivity.this, "Error al verificar existencia del producto en Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteProductsFromFirebase(List<Products> productsToDeleteFromFirebase) {
        for (Products productToDelete : productsToDeleteFromFirebase) {
            firebaseHelper.deleteProduct(productToDelete.getId(), new ProductsFirebaseHelper.DeleteProductCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Producto eliminado de Firebase", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(MainActivity.this, "Error al eliminar producto de Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        loadProductsFromFirebase(true);
    }

    // Métodos relacionados con la manipulación de productos
    private void addProduct() {
        if (areFieldsEmpty()) {
            return;
        }

        String name = editTextName.getText().toString();
        double price = Double.parseDouble(editTextPrice.getText().toString());
        Products newProduct = new Products(name, price);
        databaseHelper.addProduct(newProduct);
        loadProductsFromDatabase();
        clearInputFields();
        Toast.makeText(this, "Producto agregado exitosamente", Toast.LENGTH_SHORT).show();
    }

    private void saveProduct() {
        if (areFieldsEmpty()) {
            return;
        }

        String id = editTextId.getText().toString();
        String name = editTextName.getText().toString();
        double price = Double.parseDouble(editTextPrice.getText().toString());
        Products Products = new Products(id, name, price);
        databaseHelper.updateProduct(Products);
        loadProductsFromDatabase();
        buttonAdd.setText("Agregar");
        clearInputFields();
        Toast.makeText(this, "Producto actualizado exitosamente", Toast.LENGTH_SHORT).show();
    }

    // Métodos de utilidad
    private boolean areFieldsEmpty() {
        if (editTextName.getText().toString().trim().isEmpty() || editTextPrice.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void clearInputFields() {
        editTextId.setText("");
        editTextName.setText("");
        editTextPrice.setText("");
    }

    public void editProduct(Products product) {
        editTextId.setText(product.getId());
        editTextName.setText(product.getName());
        editTextPrice.setText(String.valueOf(product.getPrice()));
        buttonAdd.setText("Guardar");
    }

    public void deleteProduct(Products product) {
        if (product.isDeleted()) {
            Toast.makeText(this, "Producto ya está eliminado", Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.deleteProduct(product.getId());
            loadProductsFromDatabase();
            Toast.makeText(this, "Producto eliminado exitosamente", Toast.LENGTH_SHORT).show();
        }
    }
}