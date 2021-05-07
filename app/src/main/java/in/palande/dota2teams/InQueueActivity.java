package in.palande.dota2teams;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class InQueueActivity extends AppCompatActivity {

    private TextView waitingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_queue);

        waitingText = findViewById(R.id.waiting_text);
        waitingText.setText(waitingText.getText() + "9 PM");
    }
}