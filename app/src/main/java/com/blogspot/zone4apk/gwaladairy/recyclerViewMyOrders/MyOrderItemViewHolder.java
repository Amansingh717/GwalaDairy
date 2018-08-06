package com.blogspot.zone4apk.gwaladairy.recyclerViewMyOrders;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.zone4apk.gwaladairy.R;
import com.blogspot.zone4apk.gwaladairy.recyclerViewMyOrderProducts.MyOrderProduct;
import com.blogspot.zone4apk.gwaladairy.recyclerViewMyOrderProducts.MyOrderProductViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MyOrderItemViewHolder extends ArrayAdapter {
    private List<MyOrderItem> myOrderDetailsList;
    private int resource;
    private Context context;

    //FirebaseDatabase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;

    //DatabaseRefrence-------------
    DatabaseReference orderStatusDataBase;

    //adapter----------------------
    FirebaseRecyclerAdapter adapter;
    RecyclerView recyclerView;

    public MyOrderItemViewHolder(@NonNull Context context, @LayoutRes int resource, @NonNull List<MyOrderItem> myOrderDetails) {
        super(context, resource, myOrderDetails);
        this.context = context;
        this.resource = resource;
        this.myOrderDetailsList = myOrderDetails;
    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
        final MyOrderItem details = myOrderDetailsList.get(position);
        View view = LayoutInflater.from(context).inflate(resource, parent, false);

        TextView orderid = view.findViewById(R.id.textView_orderId);
        TextView orderTime = view.findViewById(R.id.textview_date_of_order);
        final TextView orderAddress = view.findViewById(R.id.text_Address);
        final TextView orderStatus = view.findViewById(R.id.orderStatus);
        final TextView cancleBtn = view.findViewById(R.id.cancel_btn);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("id", details.getOrderNo());
                Toast.makeText(context, "You clicked item no" + getPosition(details), Toast.LENGTH_SHORT).show();
            }
        });
        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderStatusDataBase.setValue("Order Cancelled");
            }
        });

        //Setting up order status
        orderStatusDataBase = FirebaseDatabase.getInstance().getReference()
                .child("OrderDatabase")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(details.getOrderNo())
                .child("paymentDetail")
                .child("status");

        orderStatusDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = dataSnapshot.getValue().toString();
                switch (status) {
                    case "Order Placed":
                        orderStatus.setText(status);
                        orderStatus.setTextColor(Color.BLUE);
                        cancleBtn.setText("CANCEL ORDER");
                        cancleBtn.setEnabled(true);
                        break;
                    case "Order Cancelled":
                        orderStatus.setText(status);
                        orderStatus.setTextColor(Color.RED);
                        cancleBtn.setText("ORDER CANCELLED");
                        cancleBtn.setEnabled(false);
                        break;
                    case "Order Delivered":
                        orderStatus.setText(status);
                        orderStatus.setTextColor(Color.MAGENTA);
                        cancleBtn.setText("ORDER DELIVERED");
                        cancleBtn.setEnabled(false);
                        break;
                }
                Log.i("Pos", String.valueOf(getPosition(details)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        orderid.setText("Order No. " + details.getOrderNo());
        orderTime.setText(details.getTime());
        orderAddress.setText(details.getAddress());

        mAuth = FirebaseAuth.getInstance();

        //Setting recycler view-----------------------------------------------------------
        recyclerView = view.findViewById(R.id.recyclerView_myOrder);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("OrderDatabase")
                .child(mAuth.getCurrentUser().getUid())
                .child(details.getOrderNo())
                .child("productsOrdered");
        databaseReference.keepSynced(true);
        Query query = databaseReference.limitToLast(50);
        FirebaseRecyclerOptions<MyOrderProduct> options =
                new FirebaseRecyclerOptions.Builder<MyOrderProduct>().setQuery(query, MyOrderProduct.class).build();

        adapter = new FirebaseRecyclerAdapter<MyOrderProduct, MyOrderProductViewHolder>(options) {
            @NonNull
            @Override
            public MyOrderProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_order_product, parent, false);
                return new MyOrderProductViewHolder(view, context);
            }

            @Override
            protected void onBindViewHolder(@NonNull final MyOrderProductViewHolder holder, int position, @NonNull final MyOrderProduct model) {
                holder.setText_name(model.getName());
                holder.setText_description(model.getDescription());
                holder.setText_price("\u20B9 " + String.valueOf(model.getPrice()));
                holder.setImage(model.getImage_url(), context);
                holder.setText_quantity(model.getQuantity());
                holder.setItemId(model.getItemId());
                holder.setPrice(model.getPrice());
                holder.setProductCount(model.getProduct_count());
            }
        };
        adapter.startListening();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder holder) {
                adapter.stopListening();
            }
        });
        return view;
    }


    @Nullable
    @Override
    public Object getItem(int position) {
        return myOrderDetailsList.get(position);
    }

}
