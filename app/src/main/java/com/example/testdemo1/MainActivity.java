package com.example.testdemo1;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private String text = "Hooray! It's snowing! It's time to make a snowman.James runs out. He makes a big pile of snow. He puts a big snowball on top. He adds a scarf and a hat. He adds an orange for the nose. He adds coal for the eyes and buttons.In the evening, James opens the door. What does he see? The snowman is moving! James invites him in. The snowman has never been inside a house. He says hello to the cat. He plays with paper towels.A moment later, the snowman takes James's hand and goes out.They go up, up, up into the air! They are flying! What a wonderful night!The next morning, James jumps out of bed. He runs to the door.He wants to thank the snowman. But he's gone.";
    private String text1 = "AppCompatActivity AppCompatActivityActivityActivityActivity";
    private String text2 = "For every layout expression, there is a binding adapter that makes the framework calls required to set the corresponding properties or listeners. For example, the binding adapter can take care of calling the setText() method to set the text property or call the setOnClickListener() method to add a listener to the click event. The most common binding adapters, such as the adapters for the android:text property used in the examples in this page, are available for you to use in the android.databinding.adapters package. For a list of the common binding adapters, see adapters. You can also create custom adapters, as shown in the following example:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        XQJustifyTextView view = findViewById(R.id.just);
        view.setText(text2);
//        view.setText(text1);

        AlignTextView view1 = findViewById(R.id.just1);
//        view1.setText(text);
//        view1.setText(text1);
    }


}
