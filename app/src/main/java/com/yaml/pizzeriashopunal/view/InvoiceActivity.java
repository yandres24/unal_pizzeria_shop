package com.yaml.pizzeriashopunal.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yaml.pizzeriashopunal.R;
import com.yaml.pizzeriashopunal.model.ProductsOrders;
import com.yaml.pizzeriashopunal.model.helper.ProductsOrdersDatabaseHelper;
import com.yaml.pizzeriashopunal.model.helper.ProductsOrdersFirebaseHelper;
import com.yaml.pizzeriashopunal.presenter.adapter.ProductsOrdersAdapter;
import com.yaml.pizzeriashopunal.utils.monitor.NetworkMonitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InvoiceActivity extends AppCompatActivity {
    // Componentes de la interfaz de usuario
    private EditText editTextId, editTextName, editTextOrder, editTextProduct, editTextPrice, editTextAmount;
    private Button buttonAdd, buttonSichronized;
    private FloatingActionButton buttonGetFirebase, buttonGetSqlite;
    private ListView listViewProductsOrders;

    // Objetos de ayuda
    private ProductsOrdersDatabaseHelper databaseHelper;
    private com.yaml.pizzeriashopunal.presenter.adapter.ProductsOrdersAdapter ProductsOrdersAdapter;
    private ProductsOrdersFirebaseHelper firebaseHelper;
    private NetworkMonitor networkMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

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
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextOrder = findViewById(R.id.editTextAmount);
        editTextProduct = findViewById(R.id.editTextAmount);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonGetFirebase = findViewById(R.id.buttonGetFirebase);
        buttonSichronized = findViewById(R.id.buttonSichronized);
        buttonGetSqlite = findViewById(R.id.buttonGetSqlite);
        listViewProductsOrders = findViewById(R.id.listViewProductsOrders);
        // Inicializar la base de datos y el adaptador
        databaseHelper = new ProductsOrdersDatabaseHelper(this);
        //databaseHelper.addProduct(new ProductsOrdersOrders("1", "prueba", 100.0, 5));
        List<ProductsOrders> products = databaseHelper.getAllProductsForOrder();
        ProductsOrdersAdapter = new ProductsOrdersAdapter(this, R.layout.list_item_product, products);
        listViewProductsOrders.setAdapter(ProductsOrdersAdapter);
    }

    // Inicializa los objetos de ayuda
    private void initializeHelpers() {
        networkMonitor = new NetworkMonitor(this);
        firebaseHelper = new ProductsOrdersFirebaseHelper();
    }

    // Configura los eventos de los botones
    private void setUpEventListeners() {
        buttonGetSqlite.setOnClickListener(v -> loadProductsOrdersFromDatabase());
        buttonSichronized.setOnClickListener(v -> synchronizeData());
        buttonGetFirebase.setOnClickListener(v -> loadProductsOrdersFromFirebase(true));
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
                Toast.makeText(com.yaml.pizzeriashopunal.view.InvoiceActivity.this, "Error al iniciar sesión anónimo", Toast.LENGTH_SHORT).show();
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
    private void loadProductsOrdersFromDatabase() {
        List<ProductsOrders> products = databaseHelper.getAllProductsForOrder();
        updateProductList(products, false);
    }

    private void loadProductsOrdersFromFirebase(boolean hideButtons) {
        firebaseHelper.getAllProductsOrders(new ProductsOrdersFirebaseHelper.GetProductsOrdersCallback() {
            @Override
            public void onProductsOrdersRetrieved(List<ProductsOrders> products) {
                updateProductList(products, hideButtons);
            }

            @Override
            public void onError() {
                Toast.makeText(com.yaml.pizzeriashopunal.view.InvoiceActivity.this, "Error al obtener productos de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProductList(List<ProductsOrders> products, boolean hideButtons) {
        ProductsOrdersAdapter.clear();
        ProductsOrdersAdapter.addAll(products);
        ProductsOrdersAdapter.setHideButtons(hideButtons);
        ProductsOrdersAdapter.notifyDataSetChanged();
    }

    // Métodos relacionados con la sincronización de datos
    private void synchronizeData() {
        if (!networkMonitor.isNetworkAvailable()) {
            Toast.makeText(com.yaml.pizzeriashopunal.view.InvoiceActivity.this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }
        synchronizeAndRemoveData();
        synchronizeAndLoadData();
    }

    private void synchronizeAndLoadData() {
        List<ProductsOrders> productsFromSQLite = databaseHelper.getAllProductsForOrder();
        synchronizeProductsOrdersToFirebase(productsFromSQLite);
        loadProductsOrdersFromFirebase(true);
    }

    private void synchronizeAndRemoveData() {
        firebaseHelper.getAllProductsOrders(new ProductsOrdersFirebaseHelper.GetProductsOrdersCallback() {
            @Override
            public void onProductsOrdersRetrieved(List<ProductsOrders> productsFromFirebase) {
                List<ProductsOrders> productsFromSQLite = databaseHelper.getAllProductsForOrder();
                Set<String> sqliteProductIds = new HashSet<>();

                for (ProductsOrders sqliteProduct : productsFromSQLite) {
                    sqliteProductIds.add(sqliteProduct.getId());
                }

                List<ProductsOrders> productsToDeleteFromFirebase = new ArrayList<>();
                for (ProductsOrders firebaseProduct : productsFromFirebase) {
                    if (!sqliteProductIds.contains(firebaseProduct.getId())) {
                        productsToDeleteFromFirebase.add(firebaseProduct);
                    }
                }

                deleteProductsOrdersFromFirebase(productsToDeleteFromFirebase);
            }

            @Override
            public void onError() {
                Toast.makeText(com.yaml.pizzeriashopunal.view.InvoiceActivity.this, "Error al obtener productos de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void synchronizeProductsOrdersToFirebase(List<ProductsOrders> productsFromSQLite) {
        for (ProductsOrders ProductsOrdersOrders : productsFromSQLite) {
            firebaseHelper.checkIfProductsOrdersExists(ProductsOrdersOrders.getId(), new ProductsOrdersFirebaseHelper.ProductsOrdersExistsCallback() {
                @Override
                public void onProductsOrdersExists(boolean exists) {
                    if (exists) {
                        firebaseHelper.updateProductsOrders(ProductsOrdersOrders);
                    } else {
                        firebaseHelper.addProductsOrders(ProductsOrdersOrders, new ProductsOrdersFirebaseHelper.AddProductsOrdersCallback() {
                            @Override
                            public void onSuccess() {
                                // Producto de una orden de una orden agregado exitosamente
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(com.yaml.pizzeriashopunal.view.InvoiceActivity.this, "Error al agregar producto a Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onError() {
                    Toast.makeText(com.yaml.pizzeriashopunal.view.InvoiceActivity.this, "Error al verificar existencia del producto en Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteProductsOrdersFromFirebase(List<ProductsOrders> productsToDeleteFromFirebase) {
        for (ProductsOrders productToDelete : productsToDeleteFromFirebase) {
            firebaseHelper.deleteProductsOrders(productToDelete.getId(), new ProductsOrdersFirebaseHelper.DeleteProductsOrdersCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(com.yaml.pizzeriashopunal.view.InvoiceActivity.this, "Producto de una orden de una orden eliminado de Firebase", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(com.yaml.pizzeriashopunal.view.InvoiceActivity.this, "Error al eliminar producto de Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        loadProductsOrdersFromFirebase(true);
    }

    // Métodos relacionados con la manipulación de productos
    private void addProduct() {
        if (areFieldsEmpty()) {
            return;
        }

        String name = editTextName.getText().toString();
        double price = Double.parseDouble(editTextPrice.getText().toString());
        int amount = Integer.parseInt(editTextAmount.getText().toString());
        String order = editTextOrder.getText().toString();
        String product = editTextProduct.getText().toString();
        ProductsOrders newProduct = new ProductsOrders("", order, product, amount, price);
        databaseHelper.addProductsOrders(newProduct);
        loadProductsOrdersFromDatabase();
        clearInputFields();
        Toast.makeText(this, "Producto de una orden de una orden agregado exitosamente", Toast.LENGTH_SHORT).show();
    }

    private void saveProduct() {
        if (areFieldsEmpty()) {
            return;
        }

        String id = editTextId.getText().toString();
        String name = editTextName.getText().toString();
        double price = Double.parseDouble(editTextPrice.getText().toString());
        String order = editTextOrder.getText().toString();
        String product = editTextProduct.getText().toString();
        ProductsOrders ProductsOrdersOrders = new ProductsOrders(id, order, product, 1, price);
        databaseHelper.updateProductsForOrder(ProductsOrdersOrders);
        loadProductsOrdersFromDatabase();
        buttonAdd.setText("Agregar");
        clearInputFields();
        Toast.makeText(this, "Producto de una orden de una orden actualizado exitosamente", Toast.LENGTH_SHORT).show();
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

    public void editProduct(ProductsOrders productOrder) {
        editTextId.setText(productOrder.getId());
        editTextName.setText(productOrder.getAmount());
        editTextPrice.setText(String.valueOf(productOrder.getPrice()));
        buttonAdd.setText("Guardar");
    }

    public void deleteProduct(ProductsOrders productOrder) {
        if (productOrder.isDeleted()) {
            Toast.makeText(this, "Producto de una orden de una orden ya está eliminado", Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.deleteProduct(productOrder.getId());
            loadProductsOrdersFromDatabase();
            Toast.makeText(this, "Producto de una orden de una orden eliminado exitosamente", Toast.LENGTH_SHORT).show();
        }
    }
}