package com.zalexdev.neternelsaddon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TextView loading = findViewById(R.id.loading);
        TextView driverListText = findViewById(R.id.driverList);
        CheckBox checkBox = findViewById(R.id.boot);
        checkBox.setChecked(new Prefs(this).getBoolean("boot"));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs prefs = new Prefs(MainActivity.this);
            prefs.putBoolean("boot", isChecked);
        });
        new Thread(() -> {
            ArrayList<String> pathList = customCommand("ls /sys/bus/usb/drivers");
            ArrayList<String> driverList = customCommand("ls /system/lib/modules/");
            ModulesAdapter adapter = new ModulesAdapter(MainActivity.this,MainActivity.this,driverList,pathList);
            runOnUiThread(() -> {
                recyclerView.setAdapter(adapter);
                loading.setVisibility(View.INVISIBLE);
                driverListText.setText("");
                for (String s : pathList){
                    driverListText.append(s + "\n");
                }
            });
        }).start();

    }

    public static ArrayList<String> customCommand(String command){
        ArrayList<String> result = new ArrayList<>();
        Process process = generateSuProcess();
        try {
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((command + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = br.readLine()) != null) {
                Log.d("OUTPUT", line);
                result.add(line);}
            br.close();
            BufferedReader br2 = new BufferedReader(new InputStreamReader(stderr));
            String lineError;
            while ((lineError = br2.readLine()) != null) {
                Log.e("ERROR", lineError);
                result.add(lineError);}
            br2.close();
        } catch (IOException ignored) {}
        process.destroy();
        return result;
    }
    public static Process generateSuProcess(){
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
            try { process = Runtime.getRuntime().exec("echo noroot");} catch (IOException ex) {ex.printStackTrace();}
        }
        return  process;
    }
    public static boolean contains(ArrayList<String> list, String item){
        for (String s : list){if (s.contains(item)){return true;}}
        return false;
    }
}