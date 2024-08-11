package com.yaml.pizzeriashopunal.presenter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.yaml.pizzeriashopunal.MainActivity;
import com.yaml.pizzeriashopunal.R;
import com.yaml.pizzeriashopunal.model.Products;

import java.util.List;

public class ProductsAdapter extends ArrayAdapter<Products> {

    // Recursos de diseño y contexto
    private int resourceLayout;
    private Context mContext;

    // Bandera para controlar la visibilidad de los botones
    private boolean hideButtons = false;

    // Constructor del adaptador
    public ProductsAdapter(Context context, int resource, List<Products> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    // Método para establecer si los botones deben ocultarse o mostrarse
    public void setHideButtons(boolean hide) {
        hideButtons = hide;
    }

    // Método para obtener la vista del adaptador para un elemento específico
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        // Si la vista es nula, inflar el diseño
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(resourceLayout, parent, false);
        }

        // Obtener el producto actual
        Products product = getItem(position);

        // Si el producto es válido, establecer los valores en la vista
        if (product != null) {
            // Obtener las vistas de nombre y precio
            TextView textViewName = view.findViewById(R.id.textViewName);
            TextView textViewPrice = view.findViewById(R.id.textViewPrice);

            // Obtener los botones de edición y eliminación
            Button buttonEdit = view.findViewById(R.id.buttonEdit);
            Button buttonDelete = view.findViewById(R.id.buttonDelete);

            // Establecer el nombre y el precio del producto en los TextView
            textViewName.setText(product.getName());
            textViewPrice.setText(String.format(mContext.getString(R.string.price_format), product.getPrice()));

            // Controlar la visibilidad de los botones según la variable hideButtons
            if (hideButtons) {
                buttonEdit.setVisibility(View.GONE);
                buttonDelete.setVisibility(View.GONE);
            } else {
                buttonEdit.setVisibility(View.VISIBLE);
                buttonDelete.setVisibility(View.VISIBLE);
            }

            // Asignar listeners a los botones
            buttonEdit.setOnClickListener(v -> {
                // Llamar al método editProduct de MainActivity con el producto a editar
                if (mContext instanceof MainActivity) {
                    ((MainActivity) mContext).editProduct(product);
                }
            });

            buttonDelete.setOnClickListener(v -> {
                // Llamar al método deleteProduct de MainActivity con el producto a eliminar
                if (mContext instanceof MainActivity) {
                    ((MainActivity) mContext).deleteProduct(product);
                }
            });
        }

        return view;
    }
}