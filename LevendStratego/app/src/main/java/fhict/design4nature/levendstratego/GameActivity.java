package fhict.design4nature.levendstratego;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by fhict.
 */
public class GameActivity extends AppCompatActivity {

    private Button loseFlag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);

        loseFlag = (Button) findViewById(R.id.loseFlag);

    }

    public void loseFlag(View view) {
        if (view.getId() == loseFlag.getId()) {
            Intent baseActivity = new Intent(this, BackToBaseActivity.class);
            startActivity(baseActivity);
        }
    }
}
