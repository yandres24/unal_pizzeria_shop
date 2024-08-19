package com.yaml.pizzeriashopunal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yaml.pizzeriashopunal.presenter.adapter.ProductsAdapter;
import com.yaml.pizzeriashopunal.model.helper.ProductsDatabaseHelper;
import com.yaml.pizzeriashopunal.model.helper.ProductsFirebaseHelper;
import com.yaml.pizzeriashopunal.model.Products;
import com.yaml.pizzeriashopunal.utils.monitor.NetworkMonitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);
    }
}