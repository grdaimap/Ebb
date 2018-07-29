package io.github.grdaimap.ebb;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

    private static String CALANDER_URL = "content://com.android.calendar/calendars";
    private static String CALANDER_EVENT_URL = "content://com.android.calendar/events";
    private static String CALANDER_REMIDER_URL = "content://com.android.calendar/reminders";
    private ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBarFullTransparent();
        final ListView today;

        //设置墙纸为背景
        final WallpaperManager wallpaperManager = WallpaperManager
                .getInstance(this);
        final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        this.getWindow().setBackgroundDrawable(wallpaperDrawable);


        ArrayAdapter<String> adapter;
        today = (ListView) findViewById(R.id.daylist);
        list = new ArrayList<>();
        Locale aLocale = Locale.getDefault();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss", aLocale);
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
        Calendar calendar = new GregorianCalendar();
        Date date = new Date();
        for (int i = 2; i < 300; i *= 2) {
            calendar.add(Calendar.DATE, i - 1);
            date = calendar.getTime();
            String item = formatter.format(date);
            list.add(item);
        }
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, list);
        today.setAdapter(adapter);

        fetchPermission(0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.mainbutt);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "你点击了悬浮按钮", Toast.LENGTH_SHORT).show();
                TextView maintxt = (TextView) findViewById(R.id.editText);
                String str = maintxt.getText().toString();
                if (str.length() != 0) {
                    Toast.makeText(MainActivity.this, "获取了值", Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < list.size(); i++) {
                        addCalendarEvent(MainActivity.this, str, str, date2TimeStamp(list.get(i)));
                    }
                }
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(MainActivity.this, "你长按了悬浮按钮", Toast.LENGTH_SHORT).show();
                TextView maintxt = (TextView) findViewById(R.id.editText);
                final String str = maintxt.getText().toString();
                if (str.length() != 0) {
                    Toast.makeText(MainActivity.this, "获取了值", Toast.LENGTH_SHORT).show();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("确定删除所有" + str + "日程吗？");
                builder.setTitle("危险操作确认");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteCalendarEvent(MainActivity.this, str);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "已取消", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.create().show();
                return false;
            }
        });
        EditText mainar = (EditText) findViewById(R.id.editText);
        final TextView upper = (TextView) findViewById(R.id.uppertxt);
        mainar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    // 此处为得到焦点时的处理内容
                    today.setVisibility(View.INVISIBLE);
                    upper.setVisibility(View.INVISIBLE);
                } else {
                    // 此处为失去焦点时的处理内容
                    today.setVisibility(View.VISIBLE);
                    upper.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private int checkCalendarAccount(Context context) {
        Cursor userCursor = context.getContentResolver().query(Uri.parse(CALANDER_URL), null, null, null, null);
        try {
            if (userCursor == null)//查询返回空值
            {
                Toast.makeText(MainActivity.this, "获取日历账户失败", Toast.LENGTH_SHORT).show();
                return -1;
            }
            int count = userCursor.getCount();
            if (count > 0) {//存在现有账户，取第一个账户的id返回
                userCursor.moveToFirst();
                return userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID));
            } else {
                Toast.makeText(MainActivity.this, "获取日历账户失败", Toast.LENGTH_SHORT).show();
                return -1;
            }
        } finally {
            if (userCursor != null) {
                userCursor.close();
            }
        }
    }

    public void addCalendarEvent(Context context, String title, String description, long beginTime) {
        // 获取日历账户的id
        int calId = checkCalendarAccount(context);
        if (calId < 0) {
            // 获取账户id失败直接返回，添加日历事件失败
            return;
        }

        ContentValues event = new ContentValues();
        event.put("title", title);
        event.put("description", description);
        // 插入账户的id
        event.put("calendar_id", calId);

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(beginTime);//设置开始时间
        long start = mCalendar.getTime().getTime();
        mCalendar.setTimeInMillis(start + 8000000);//设置终止时间
        long end = mCalendar.getTime().getTime();

        event.put(CalendarContract.Events.DTSTART, start);
        event.put(CalendarContract.Events.DTEND, end);
        event.put(CalendarContract.Events.HAS_ALARM, 1);//设置有闹钟提醒
        event.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Shanghai");  //这个是时区，必须有，
        event.put(CalendarContract.Events.ALL_DAY, 1);
        //添加事件
        Uri newEvent = context.getContentResolver().insert(Uri.parse(CALANDER_EVENT_URL), event);
        if (newEvent == null) {
            // 添加日历事件失败直接返回
            Toast.makeText(MainActivity.this, "添加复习日程失败", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(MainActivity.this, "添加复习日程成功", Toast.LENGTH_SHORT).show();
    }

    public static long date2TimeStamp(String date) {
        try {
            Locale aLocale = Locale.getDefault();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss", aLocale);
            return Long.parseLong(String.valueOf(sdf.parse(date).getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void fetchPermission(int requestCode) {
        int checkSelfPermission;
        try {
            checkSelfPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return;
        }

        // 如果有授权，走正常插入日历逻辑
        if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            // 该方法的实现在文章的后面
            Toast.makeText(MainActivity.this, "准备完毕，可以开始设置复习日程了", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(MainActivity.this, "请授权Ebb读写日历", Toast.LENGTH_SHORT).show();
            // 如果没有授权，就请求用户授权
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR}, requestCode);
        }
    }

    public void deleteCalendarEvent(Context context, String title) {
        Cursor eventCursor = context.getContentResolver().query(Uri.parse(CALANDER_EVENT_URL), null, null, null, null);
        try {
            if (eventCursor == null){
                Toast.makeText(MainActivity.this, "不存在这样的日程", Toast.LENGTH_SHORT).show();//查询返回空值
                return;}
            if (eventCursor.getCount() > 0) {
                //遍历所有事件，找到title跟需要查询的title一样的项
                for (eventCursor.moveToFirst(); !eventCursor.isAfterLast(); eventCursor.moveToNext()) {
                    String eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
                    if (!TextUtils.isEmpty(title) && title.equals(eventTitle)) {
                        int id = eventCursor.getInt(eventCursor.getColumnIndex(CalendarContract.Calendars._ID));//取得id
                        Uri deleteUri = ContentUris.withAppendedId(Uri.parse(CALANDER_EVENT_URL), id);
                        int rows = context.getContentResolver().delete(deleteUri, null, null);
                        if (rows == -1) {
                            Toast.makeText(MainActivity.this, "删除失败", Toast.LENGTH_SHORT).show();//事件删除失败
                            return;
                        }else Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    }
                }
            }else Toast.makeText(MainActivity.this, "不存在账户？检查授权或日历", Toast.LENGTH_SHORT).show();
        } finally {
            if (eventCursor != null) {
                eventCursor.close();
            }
        }
    }

    public void gotoabout(View view) {
        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
        startActivity(intent);
    }
    protected void setStatusBarFullTransparent() {
        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= 19) {//19表示4.4
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

}

