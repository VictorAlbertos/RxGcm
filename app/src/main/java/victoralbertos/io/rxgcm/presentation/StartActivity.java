package victoralbertos.io.rxgcm.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import victoralbertos.io.rxgcm.R;
import victoralbertos.io.rxgcm.presentation.nested_fragment.HostActivityNestedFragment;

/**
 * Created by victor on 12/05/16.
 */
public class StartActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        findViewById(R.id.bt_no_nested_fragment).setOnClickListener(v -> {
            startActivity(new Intent(StartActivity.this, HostActivityIssues.class));
        });

        findViewById(R.id.bt_nested_fragment).setOnClickListener(v -> {
            startActivity(new Intent(StartActivity.this, HostActivityNestedFragment.class));
        });
    }

}