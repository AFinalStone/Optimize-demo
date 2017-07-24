package demo.leakcanary.srain.in.leakcanarydemo;

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Test02Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test02);
        new Thread(){
            @Override
            public void run() {
                Context context = Test02Activity.this;
                System.out.print(context);
                SystemClock.sleep(15000);
            }
        }.start();
    }
}
