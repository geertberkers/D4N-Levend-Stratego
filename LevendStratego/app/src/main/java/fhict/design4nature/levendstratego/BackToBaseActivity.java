package fhict.design4nature.levendstratego;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by fhict.
 */
public class BackToBaseActivity extends AppCompatActivity {

    private ImageView revive;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.back_to_base_activity);

        revive = (ImageView) findViewById(R.id.revive);

    }

    public void revive(View view) {
        if (view.getId() == revive.getId()) {
            Intent gameIntent = new Intent(this, GameActivity.class);
            startActivity(gameIntent);
        }
    }
}
