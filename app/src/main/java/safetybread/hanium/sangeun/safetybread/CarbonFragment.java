package safetybread.hanium.sangeun.safetybread;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by sangeun on 2018-08-18.
 */

public class CarbonFragment extends Fragment {
    private String TAG = "CarbonFragment";
    private TextView concentrate;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_carbon, container, false);
        Bundle bundle = this.getArguments();
        concentrate = rootView.findViewById(R.id.carbon_textview);
        if (bundle != null) {
            String data = bundle.getString("data");

            concentrate.setText(data);
        } else {
            concentrate.setText("0.0");
        }
        return rootView;
    }

}
