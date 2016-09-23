package io.geeteshk.hyper.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Pref;

public class EncryptActivity extends AppCompatActivity {

    TextView buttonOne, buttonTwo, buttonThree, buttonFour, buttonFive, buttonSix, buttonSeven, buttonEight, buttonNine, buttonZero;
    ImageView buttonDelete;
    LinearLayout dotsLayout;
    View dotOne, dotTwo, dotThree, dotFour;
    int dotCounter = 0;

    private int[] PIN = new int[4];
    private int[] ENTERED_PIN = new int[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(0xFFE64A19);
        }

        dotOne = findViewById(R.id.dotOne);
        dotTwo = findViewById(R.id.dotTwo);
        dotThree = findViewById(R.id.dotThree);
        dotFour = findViewById(R.id.dotFour);

        buttonOne = (TextView) findViewById(R.id.buttonOne);
        buttonTwo = (TextView) findViewById(R.id.buttonTwo);
        buttonThree = (TextView) findViewById(R.id.buttonThree);
        buttonFour = (TextView) findViewById(R.id.buttonFour);
        buttonFive = (TextView) findViewById(R.id.buttonFive);
        buttonSix = (TextView) findViewById(R.id.buttonSix);
        buttonSeven = (TextView) findViewById(R.id.buttonSeven);
        buttonEight = (TextView) findViewById(R.id.buttonEight);
        buttonNine = (TextView) findViewById(R.id.buttonNine);
        buttonZero = (TextView) findViewById(R.id.buttonZero);

        buttonOne.setOnClickListener(new NumberClickListener(1));
        buttonTwo.setOnClickListener(new NumberClickListener(2));
        buttonThree.setOnClickListener(new NumberClickListener(3));
        buttonFour.setOnClickListener(new NumberClickListener(4));
        buttonFive.setOnClickListener(new NumberClickListener(5));
        buttonSix.setOnClickListener(new NumberClickListener(6));
        buttonSeven.setOnClickListener(new NumberClickListener(7));
        buttonEight.setOnClickListener(new NumberClickListener(8));
        buttonNine.setOnClickListener(new NumberClickListener(9));
        buttonZero.setOnClickListener(new NumberClickListener(0));

        dotsLayout = (LinearLayout) findViewById(R.id.dots);
        buttonDelete = (ImageButton) findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dotCounter--;
                setDot(dotCounter, false);
                if (dotCounter == -1) {
                    dotCounter = 0;
                }
            }
        });

        String pin = Pref.get(this, "pin", "0000");
        for (int i = 0; i < 4; i++) {
            PIN[i] = Integer.valueOf(String.valueOf(pin.charAt(i)));
        }
    }

    private void setDot(int dotNumber, boolean active) {
        if (active) {
            switch (dotNumber) {
                case 0:
                    dotOne.setBackgroundResource(R.drawable.circle_selected);
                    break;
                case 1:
                    dotTwo.setBackgroundResource(R.drawable.circle_selected);
                    break;
                case 2:
                    dotThree.setBackgroundResource(R.drawable.circle_selected);
                    break;
                case 3:
                    dotFour.setBackgroundResource(R.drawable.circle_selected);
                    break;
            }
        } else {
            switch (dotNumber) {
                case 0:
                    dotOne.setBackgroundResource(R.drawable.circle_empty);
                    break;
                case 1:
                    dotTwo.setBackgroundResource(R.drawable.circle_empty);
                    break;
                case 2:
                    dotThree.setBackgroundResource(R.drawable.circle_empty);
                    break;
                case 3:
                    dotFour.setBackgroundResource(R.drawable.circle_empty);
                    break;
            }
        }
    }

    private void resetDots() {
        dotCounter = 0;
        dotOne.setBackgroundResource(R.drawable.circle_empty);
        dotTwo.setBackgroundResource(R.drawable.circle_empty);
        dotThree.setBackgroundResource(R.drawable.circle_empty);
        dotFour.setBackgroundResource(R.drawable.circle_empty);
    }

    private class NumberClickListener implements View.OnClickListener {

        private int number;

        NumberClickListener(int number) {
            this.number = number;
        }

        @Override
        public void onClick(View v) {
            ENTERED_PIN[dotCounter] = number;
            setDot(dotCounter, true);
            dotCounter++;

            if (dotCounter == 4) {
                boolean pinCheck = true;
                for (int i = 0; i < 4; i++) {
                    pinCheck = pinCheck && (ENTERED_PIN[i] == PIN[i]);
                }

                if (pinCheck) {
                    if (getIntent().hasExtra("project")) {
                        Intent intent = new Intent(EncryptActivity.this, ProjectActivity.class);
                        intent.putExtras(getIntent().getExtras());
                        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                        if (Build.VERSION.SDK_INT >= 21) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                        }

                        startActivity(intent);
                        finish();
                    } else {
                        setResult(1);
                        finish();
                    }
                } else {
                    dotsLayout.startAnimation(AnimationUtils.loadAnimation(EncryptActivity.this, R.anim.shake));
                    resetDots();
                }
            }
        }
    }
}
