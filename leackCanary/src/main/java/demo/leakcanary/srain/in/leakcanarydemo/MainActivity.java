package demo.leakcanary.srain.in.leakcanarydemo;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_go_to_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToTest();
            }
        });
    }

    private void goToTest() {
        if(flag){
            Intent intent = new Intent(this, TestActivity.class);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this, Test02Activity.class);
            startActivity(intent);
        }
        flag = !flag;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_go_to_test) {
            goToTest();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
