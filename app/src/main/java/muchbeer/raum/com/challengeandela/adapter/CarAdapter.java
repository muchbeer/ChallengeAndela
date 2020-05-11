package muchbeer.raum.com.challengeandela.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import muchbeer.raum.com.challengeandela.InsertCarActivity;
import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.utility.CarDeals;
import muchbeer.raum.com.challengeandela.utility.FirebaseUtil;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildListener;
    private ImageView imageDeal;
    ArrayList<CarDeals> cDeals;

    private static final String LOG_TAG = CarAdapter.class.getSimpleName() ;
    public CarAdapter() {
        //FirebaseUtil.openFbReference("traveldeals");
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        this.cDeals = FirebaseUtil.mDeals;

        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                CarDeals td = dataSnapshot.getValue(CarDeals.class);
                Log.d(LOG_TAG, "Deal: " + td.getCarName());
                td.setId(dataSnapshot.getKey());
                cDeals.add(td);
                notifyItemInserted(cDeals.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildListener);
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.recycler_list, parent, false);
        return new CarViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        CarDeals deal = cDeals.get(position);
        holder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return cDeals.size();
    }

    public class CarViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView tvCar;
        TextView tvDescription;
        TextView tvPrice;
       // ImageView imageUrl;
        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCar = (TextView) itemView.findViewById(R.id.tvCar);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescripition);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            imageDeal = (ImageView) itemView.findViewById(R.id.itemImageUrl);
            itemView.setOnClickListener(this);

        }

        public void bind(CarDeals deal) {
            tvCar.setText(deal.getCarName());
            tvDescription.setText(deal.getDescription());
            tvPrice.setText(deal.getPrice());
            showImage(deal.getImageUrl());
        }
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Log.d( LOG_TAG, "Click "+ String.valueOf(position));

            CarDeals selectedDeal = cDeals.get(position);
            Intent intent = new Intent(view.getContext(), InsertCarActivity.class);

            intent.putExtra("Deal", selectedDeal);
            view.getContext().startActivity(intent);
        }

        private void showImage(String url) {
            if (url != null && url.isEmpty()==false) {
                Log.d(LOG_TAG, "If you can see image then meaning you can access this view: "+url);
                Picasso.get()
                        .load(url)
                        .resize(160, 160)
                        .centerCrop()
                    //    .placeholder(R.drawable.fail_image)
                        .error(R.drawable.fail_image)
                        .into(imageDeal);
            }
        }
}
}
