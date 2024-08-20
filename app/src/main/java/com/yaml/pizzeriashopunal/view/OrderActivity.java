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
import com.yaml.pizzeriashopunal.model.Orders;
import com.yaml.pizzeriashopunal.model.helper.OrdersDatabaseHelper;
import com.yaml.pizzeriashopunal.model.helper.OrdersFirebaseHelper;
import com.yaml.pizzeriashopunal.presenter.adapter.OrdersAdapter;
import com.yaml.pizzeriashopunal.utils.monitor.NetworkMonitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderActivity extends AppCompatActivity {
    // Componentes de la interfaz de usuario
    private EditText editTextId, editTextName, editTextPrice;
    private Button buttonAdd, buttonSichronized;
    private FloatingActionButton buttonGetFirebase, buttonGetSqlite;
    private ListView listViewOrders;

    // Objetos de ayuda
    private OrdersDatabaseHelper databaseHelper;
    private com.yaml.pizzeriashopunal.presenter.adapter.OrdersAdapter OrdersAdapter;
    private OrdersFirebaseHelper firebaseHelper;
    private NetworkMonitor networkMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        initializeUIComponents();
        initializeHelpers();
        setUpEventListeners();
        authenticateFirebaseUser();
    }

    // Inicializa los componentes de la interfaz de usuario
    private void initializeUIComponents() {
        editTextId = findViewById(R.id.editTextId);
        editTextName = findViewById(R.id.editTextUserId);
        editTextPrice = findViewById(R.id.editTextDate);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonGetFirebase = findViewById(R.id.buttonGetFirebase);
        buttonSichronized = findViewById(R.id.buttonSichronized);
        buttonGetSqlite = findViewById(R.id.buttonGetSqlite);
        listViewOrders = findViewById(R.id.listViewOrders);
        // Inicializar la base de datos y el adaptador
        databaseHelper = new OrdersDatabaseHelper(this);
        databaseHelper.addOrder(new Orders("1", "orden inicial", new Date().toString()));
        List<Orders> orders = databaseHelper.getAllOrders();
        OrdersAdapter = new OrdersAdapter(this, R.layout.list_item_order, orders);
        listViewOrders.setAdapter(OrdersAdapter);
    }

    // Inicializa los objetos de ayuda
    private void initializeHelpers() {
        networkMonitor = new NetworkMonitor(this);
        firebaseHelper = new OrdersFirebaseHelper();
    }

    // Configura los eventos de los botones
    private void setUpEventListeners() {
        buttonGetSqlite.setOnClickListener(v -> loadOrdersFromDatabase());
        buttonSichronized.setOnClickListener(v -> synchronizeData());
        buttonGetFirebase.setOnClickListener(v -> loadOrdersFromFirebase(true));
        buttonAdd.setOnClickListener(v -> handleAddOrUpdateOrder());
    }

    // Autentica al usuario de Firebase
    private void authenticateFirebaseUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                // Puedes usar el user.getUid() para identificar al usuario si es necesario
            } else {
                Toast.makeText(com.yaml.pizzeriashopunal.view.OrderActivity.this, "Error al iniciar sesión anónimo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Maneja la operación de agregar o actualizar ordero
    private void handleAddOrUpdateOrder() {
        if (buttonAdd.getText().toString().equalsIgnoreCase("Agregar")) {
            addOrder();
        } else {
            saveOrder();
        }
    }

    // Métodos relacionados con la carga de datos
    private void loadOrdersFromDatabase() {
        List<Orders> orders = databaseHelper.getAllOrders();
        updateOrderList(orders, false);
    }

    private void loadOrdersFromFirebase(boolean hideButtons) {
        firebaseHelper.getAllOrders(new OrdersFirebaseHelper.GetOrdersCallback() {
            @Override
            public void onOrdersRetrieved(List<Orders> orders) {
                updateOrderList(orders, hideButtons);
            }

            @Override
            public void onError() {
                Toast.makeText(com.yaml.pizzeriashopunal.view.OrderActivity.this, "Error al obtener ordenes de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderList(List<Orders> orders, boolean hideButtons) {
        OrdersAdapter.clear();
        OrdersAdapter.addAll(orders);
        OrdersAdapter.setHideButtons(hideButtons);
        OrdersAdapter.notifyDataSetChanged();
    }

    // Métodos relacionados con la sincronización de datos
    private void synchronizeData() {
        if (!networkMonitor.isNetworkAvailable()) {
            Toast.makeText(com.yaml.pizzeriashopunal.view.OrderActivity.this, "No hay conexión a internet", Toast.LENGTH_SHORT).show();
            return;
        }
        synchronizeAndRemoveData();
        synchronizeAndLoadData();
    }

    private void synchronizeAndLoadData() {
        List<Orders> ordersFromSQLite = databaseHelper.getAllOrders();
        synchronizeOrdersToFirebase(ordersFromSQLite);
        loadOrdersFromFirebase(true);
    }

    private void synchronizeAndRemoveData() {
        firebaseHelper.getAllOrders(new OrdersFirebaseHelper.GetOrdersCallback() {
            @Override
            public void onOrdersRetrieved(List<Orders> ordersFromFirebase) {
                List<Orders> ordersFromSQLite = databaseHelper.getAllOrders();
                Set<String> sqliteOrderIds = new HashSet<>();

                for (Orders sqliteOrder : ordersFromSQLite) {
                    sqliteOrderIds.add(sqliteOrder.getId());
                }

                List<Orders> ordersToDeleteFromFirebase = new ArrayList<>();
                for (Orders firebaseOrder : ordersFromFirebase) {
                    if (!sqliteOrderIds.contains(firebaseOrder.getId())) {
                        ordersToDeleteFromFirebase.add(firebaseOrder);
                    }
                }

                deleteOrdersFromFirebase(ordersToDeleteFromFirebase);
            }

            @Override
            public void onError() {
                Toast.makeText(com.yaml.pizzeriashopunal.view.OrderActivity.this, "Error al obtener orderos de Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void synchronizeOrdersToFirebase(List<Orders> ordersFromSQLite) {
        for (Orders Orders : ordersFromSQLite) {
            firebaseHelper.checkIfOrderExists(Orders.getId(), new OrdersFirebaseHelper.OrderExistsCallback() {
                @Override
                public void onOrderExists(boolean exists) {
                    if (exists) {
                        firebaseHelper.updateOrder(Orders);
                    } else {
                        firebaseHelper.addOrder(Orders, new OrdersFirebaseHelper.AddOrderCallback() {
                            @Override
                            public void onSuccess() {
                                // Ordero agregado exitosamente
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(com.yaml.pizzeriashopunal.view.OrderActivity.this, "Error al agregar ordero a Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onError() {
                    Toast.makeText(com.yaml.pizzeriashopunal.view.OrderActivity.this, "Error al verificar existencia del ordero en Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteOrdersFromFirebase(List<Orders> ordersToDeleteFromFirebase) {
        for (Orders orderToDelete : ordersToDeleteFromFirebase) {
            firebaseHelper.deleteOrder(orderToDelete.getId(), new OrdersFirebaseHelper.DeleteOrderCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(com.yaml.pizzeriashopunal.view.OrderActivity.this, "Ordero eliminado de Firebase", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(com.yaml.pizzeriashopunal.view.OrderActivity.this, "Error al eliminar ordero de Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        loadOrdersFromFirebase(true);
    }

    // Métodos relacionados con la manipulación de orderos
    private void addOrder() {
        if (areFieldsEmpty()) {
            return;
        }

        String userId = editTextName.getText().toString();
        String date = editTextPrice.getText().toString();
        Orders newOrder = new Orders("", userId, date);
        databaseHelper.addOrder(newOrder);
        loadOrdersFromDatabase();
        clearInputFields();
        Toast.makeText(this, "Ordero agregado exitosamente", Toast.LENGTH_SHORT).show();
    }

    private void saveOrder() {
        if (areFieldsEmpty()) {
            return;
        }

        String userId = editTextName.getText().toString();
        String date = editTextPrice.getText().toString();
        Orders newOrder = new Orders("", userId, date);
        databaseHelper.updateOrder(newOrder);
        loadOrdersFromDatabase();
        buttonAdd.setText("Agregar");
        clearInputFields();
        Toast.makeText(this, "Ordero actualizado exitosamente", Toast.LENGTH_SHORT).show();
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

    public void editOrder(Orders order) {
        editTextId.setText(order.getId());
        editTextName.setText(order.getUser_id());
        buttonAdd.setText("Guardar");
    }

    public void deleteOrder(Orders order) {
        if (order.isDeleted()) {
            Toast.makeText(this, "Ordero ya está eliminado", Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.deleteOrder(order.getId());
            loadOrdersFromDatabase();
            Toast.makeText(this, "Ordero eliminado exitosamente", Toast.LENGTH_SHORT).show();
        }
    }
}
