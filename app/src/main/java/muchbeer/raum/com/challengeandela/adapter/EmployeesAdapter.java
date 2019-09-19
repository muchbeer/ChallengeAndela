package muchbeer.raum.com.challengeandela.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.ArrayList;

import muchbeer.raum.com.challengeandela.R;
import muchbeer.raum.com.challengeandela.messagefirebase.AdminActivity;
import muchbeer.raum.com.challengeandela.models.Users;

public class EmployeesAdapter extends RecyclerView.Adapter<EmployeesAdapter.ViewHolder> {

    private static final String TAG = "EmployeesAdapter";
    private ArrayList<Users> mUsers;
    private Context mContext;

    public EmployeesAdapter(Context context, ArrayList<Users> listUsers) {
        mUsers = listUsers;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //inflate the custom layout
        View view = inflater.inflate(R.layout.layout_employee_listitem, parent, false);

        //return a new holder instance
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageLoader.getInstance().displayImage(mUsers.get(position).getProfile_image(), holder.profileImage);
        holder.name.setText(mUsers.get(position).getName());
        holder.department.setText(mUsers.get(position).getDepartment());
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView profileImage;
        public TextView name, department;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = (ImageView) itemView.findViewById(R.id.profile_image);
            name = (TextView) itemView.findViewById(R.id.name);
            department = (TextView) itemView.findViewById(R.id.department);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: selected employee: " + mUsers.get(getAdapterPosition()));

                    //open a dialog for selecting a department
                    ((AdminActivity)mContext).setDepartmentDialog(mUsers.get(getAdapterPosition()));

                }
            });
        }
    }
}
