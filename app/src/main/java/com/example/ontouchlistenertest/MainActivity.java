package com.example.ontouchlistenertest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    //Все что видно на экране
    ConstraintLayout touchSurface;
    FrameLayout dot;
    TextView textView;
    TextView tvTotalDistance;
    TextView tvBonus;
    Toolbar toolbar;

    //переменные для пересчета дистанции
    private float previousPosX;
    private float previousPosY;
    private static double currentDistance;
    float currentPosX;
    float currentPosY;
    int count =0;

    //Общая дистанция. Уменьшается по мере прохождения
    public static double totalDistance;

    //Плотность экрана для точного расчета
    private int screenDencity;

    //Бонус при долгой игре увеличивается
    private double bonus = 1.0;

    //Нужно для расчета(должно быть 24.5)
    public final double oneInc = 26.35;

    public static final float DEFAULT_FINAL_DISTANCE = 10000000;

    public static final String TOTAL_DISTANCE = "TOTAL_DISTANCE";


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        window.setNavigationBarColor(Color.BLACK);

        screenDencity = getResources().getDisplayMetrics().densityDpi;
        touchSurface = findViewById(R.id.touch_s);
        textView = findViewById(R.id.textView1);
        dot = findViewById(R.id.textView);
        dot.setVisibility(View.INVISIBLE);
        tvBonus = findViewById(R.id.bonus_view);
        tvTotalDistance = findViewById(R.id.total_distance_view);

        //Toolbar Setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getPreferences(MODE_PRIVATE);
        totalDistance = (double)sharedPreferences.getFloat(TOTAL_DISTANCE,DEFAULT_FINAL_DISTANCE);



        //Вывод кон. дист из сохранения
        tvTotalDistance.setText(new DecimalFormat("#.####").format(totalDistance/1000000) + "km");



        touchSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        previousPosX = event.getX();
                        previousPosY = event.getY();
                        currentDistance = 0;

                        //Счетчик бегает за пальцем
                        dot.setX(event.getX() - dot.getWidth() / 2);
                        dot.setY(event.getY() - dot.getHeight() / 2);
                        dot.setVisibility(View.VISIBLE);
                        textView.setText(currentDistance / 10 + " cm");

                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {

                        if (totalDistance <= 0) {
                            finishGame();
                        }
                        else{
                        currentPosX = event.getX() + 30;
                        currentPosY = event.getY() + 30;
                        double a = calculateDistance(currentPosX, currentPosY, previousPosX, previousPosY);
                        currentDistance += a;
                        totalDistance -= a;





                            dot.setX(event.getX() - dot.getWidth() / 2);
                            dot.setY(event.getY() - dot.getHeight() / 2);

                            short divider = 10;
                            String measureString = " cm";
                            if (currentDistance > 1000) {
                                divider = 1000;
                                measureString = " m";
                            }
                            textView.setText(new DecimalFormat("##.##").format(currentDistance / divider) + measureString);

                            bonus = setBonus(currentDistance);
                            tvBonus.setText("x" + bonus);

                            //Конверитирует мм в км деля на 1000000
                            tvTotalDistance.setText(new DecimalFormat("#.####").format(totalDistance / 1000000) + "km");


                            previousPosY = currentPosY;
                            previousPosX = currentPosX;

                            if (count == 1000) {
                                saveTotalDistance(totalDistance);
                                count = 0;
                            } else {
                                count++;
                            }
                        }
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            saveTotalDistance(totalDistance);
                            dot.setVisibility(View.INVISIBLE);
                            bonus = 1.0;
                            tvBonus.setText("x" + bonus);
                            textView.setText("0 cm");
                            break;
                        }
                    }
                    return true;
                }
        });

    }

    private double calculateDistance(float x,float y,float x1,float y1){
        double distanceInPixels = Math.sqrt(Math.pow(x1-x,2) + Math.pow(y1-y,2));

        double distanceInMm = ((distanceInPixels * oneInc)/screenDencity)*bonus;

        return distanceInMm;
    }

    private double setBonus(double currDist){
        if(currDist >= 10000 && currDist < 20000){
            return 1.1;
        }
        else if(currDist >= 20000 && currDist < 30000){
             return 1.2;
        }else if(currDist >= 30000 && currDist < 50000){
             return 1.5;
        }else if(currDist >= 50000 && currDist < 100000){
             return 2;
        }else if(currDist >= 100000 && currDist < 200000){
             return 5;
        }else if(currDist >= 200000 && currDist < 500000){
             return 10;
        }else if(currDist >= 500000 && currDist < 1000000){
             return 30;
        }
        else{
            return 1.0;
        }
    }
    private void saveTotalDistance(double dist){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(TOTAL_DISTANCE,(float)dist);
        editor.apply();
        Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings: {
                callDialog();
                break;
            }
        }
        return true;
    }
    public void resetDistance(){
        totalDistance = DEFAULT_FINAL_DISTANCE;
        saveTotalDistance(totalDistance);
        tvTotalDistance.setText(new DecimalFormat("#.####").format(totalDistance/1000000) + "km");
        Toast.makeText(MainActivity.this,"Distance reset",Toast.LENGTH_SHORT).show();
    }

    private void callDialog(){
        new AlertDialog.Builder(this)
        .setTitle("Reset Distance?")
        .setMessage("Are you sure you want to reset the distance")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetDistance();
            }
        })
         .setNegativeButton("No", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {

             }
         })
         .show();
    }

    private void finishGame(){
        new AlertDialog.Builder(this)
                .setTitle("Congratulations!!!")
                .setMessage("You finished. Would you like to start again?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetDistance();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}

