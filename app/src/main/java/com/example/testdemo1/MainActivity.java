package com.example.testdemo1;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private String text = "Hooray! It's snowing! It's time to make a snowman.James runs out. He makes a big pile of snow. He puts a big snowball on top. He adds a scarf and a hat. He adds an orange for the nose. He adds coal for the eyes and buttons.In the evening, James opens the door. What does he see? The snowman is moving! James invites him in. The snowman has never been inside a house. He says hello to the cat. He plays with paper towels.A moment later, the snowman takes James's hand and goes out.They go up, up, up into the air! They are flying! What a wonderful night!The next morning, James jumps out of bed. He runs to the door.He wants to thank the snowman. But he's gone.";
    private String text1 = "AppCompatActivity AppCompatActivityActivityActivityActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        XQJustifyTextView view = findViewById(R.id.just);
//        view.setText(text);
//        view.setText(text1);

        AlignTextView view1 = findViewById(R.id.just1);
//        view1.setText(text);
//        view1.setText(text1);
    }


}
