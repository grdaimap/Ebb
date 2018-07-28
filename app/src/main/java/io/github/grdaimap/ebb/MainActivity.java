package io.github.grdaimap.ebb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ListView today;
    private ArrayList<String> list;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.mainbutt);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"你点击了悬浮按钮",Toast.LENGTH_SHORT).show();
                TextView maintxt=(TextView)findViewById(R.id.editText);
                String str= maintxt.getText().toString();
                if(str.length()!=0){
                    Toast.makeText(MainActivity.this,"获取了值",Toast.LENGTH_SHORT).show();
                }

            }
        });
        today=(ListView)findViewById(R.id.daylist);
        list = new ArrayList<>();
        Locale aLocale = Locale.getDefault();
        SimpleDateFormat formatter   =   new   SimpleDateFormat   ("yyyy年MM月dd日   HH:mm:ss",aLocale);
        Date curDate =  new Date(System.currentTimeMillis());
        String   str   =   formatter.format(curDate);
        Toast.makeText(MainActivity.this,str,Toast.LENGTH_SHORT).show();
        Calendar calendar   =   new GregorianCalendar();
        Date date=new Date();
        calendar.add(Calendar.DATE,1);//把日期往后增加一天.整数往后推,负数往前移动
        date=calendar.getTime();   //这个时间就是日期往后推一天的结果
        Toast.makeText(MainActivity.this,date.toString(),Toast.LENGTH_SHORT).show();
        list.add(date.toString());
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        today.setAdapter(adapter);
    }
}

