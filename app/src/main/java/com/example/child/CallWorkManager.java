package com.example.child;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CallWorkManager extends Worker {
    public String []arrayNumber;
    public int []arrayCount;
    public String []arraySuspect;

    public static final String DATE_FORMAT_2 = "dd-MMM-yyyy";
     public int firstTime;
          public CallWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
              arrayNumber= new String[300];
              arrayCount=new int[300];
              arraySuspect=new String[300];


          }

    @NonNull
    @Override
    public Result doWork() {
        CallLogs();
        return Result.success();

    }




    public void CallLogs() {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuffer sb = new StringBuffer();
                    Cursor managedCursor = new CursorLoader(getApplicationContext(), CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls._ID + " DESC").loadInBackground();
                    int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
                    int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
                    int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
                    int name = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
                    int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
                    sb.append("Call Details :");
                    int i=0;
                    int k=0;

                    while (managedCursor.moveToNext()) {
                        String phNumber = managedCursor.getString(number);
                        String callType = managedCursor.getString(type);
                        String callDate = managedCursor.getString(date);
                        Date callDayTime = new Date(Long.valueOf(callDate));
                        String callDuration = managedCursor.getString(duration);
                        String nam=managedCursor.getString(name);
                        String dir = null;
                        int dircode = Integer.parseInt(callType);
                        firstTime=0;
                        String formateDate = new SimpleDateFormat("dd-MMM-yyyy").format(callDayTime);


                        if (Integer.valueOf(callDuration)>1200 && getCurrentDate().equals(formateDate)){
                            firstTime=1;
                            for (int j=0;j<arrayNumber.length;j++){
                               if (arrayNumber[j]!=null) {
                                   if (arrayNumber[j].equals(phNumber)) {
                                       firstTime = 2;
                                       arrayCount[j] = arrayCount[j] + 1;

                                       if (arrayCount[j] > 0) {
                                           arraySuspect[k] = phNumber;
                                           Log.i("suspected",arraySuspect[k]);
                                           k++;
                                       }

                                       break;
                                   }
                                  }
                               else {
                                   break;
                               }

                            }

                        }

                        if (firstTime==1){
                            arrayNumber[i]=phNumber;
                            arrayCount[i]=arrayCount[i]+1;
                            i++;

                        }
                        switch (dircode) {
                            case CallLog.Calls.OUTGOING_TYPE:
                                dir = "OUTGOING";
                                break;
                            case CallLog.Calls.INCOMING_TYPE:
                                dir = "INCOMING";
                                break;

                            case CallLog.Calls.MISSED_TYPE:
                                dir = "MISSED";
                                break;
                        }
                        sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                                + dir + " \nCall Date:--- " + callDayTime
                                + " \nCall duration in sec :--- " + callDuration
                                + " \n Name :--- " + nam);
                        sb.append("\n----------------------------------");
                    }

                    managedCursor.close();


                    if (arraySuspect[0]!=null) {

                        uploadSuspiciousCalls();
                    }

                    Log.i("callDetials","Detials "+sb);
                }

                catch (Exception ex){
                    Log.i("aaaaaaaaaaaaaaa","aMSg :"+ex.getMessage());
                }


            }
        });




    }

    public void uploadSuspiciousCalls(){
        DatabaseReference root = FirebaseDatabase.getInstance().getReference("CallReport/"+User.getParentsPhoneNumber()+"/"+User.getChildName()+"");
        root.child(getCurrentDate()).setValue("");
        DatabaseReference root1 = FirebaseDatabase.getInstance().getReference("CallReport/"+User.getParentsPhoneNumber()+"/"+User.getChildName()+"/"+getCurrentDate()+"");


        for (int l=0;l<arraySuspect.length;l++) {
              if (arraySuspect[l]!=null) {
                  root1.child(arraySuspect[l]).setValue("");
              }
              else {
                  break;
              }


          }
    }


    public static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_2);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);

    }




}
