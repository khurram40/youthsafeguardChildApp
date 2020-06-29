package com.example.child;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.loader.content.CursorLoader;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MessageWorkManager extends Worker {

    public String []arrayNumber;
    public int []arrayCount;
    public String []arraySuspect;

    public static final String DATE_FORMAT_2 = "dd-MMM-yyyy";
    private Context context;


    public MessageWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        arrayNumber= new String[300];
        arrayCount=new int[300];
        arraySuspect=new String[300];
         this.context=context;



    }

    @NonNull
    @Override
    public Result doWork() {
        messagelogs();
        return Result.success();
    }


    public void messagelogs() {


         StringBuffer sb = new StringBuffer();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);


        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int indexDate = smsInboxCursor.getColumnIndex("date");
        int indexType = smsInboxCursor.getColumnIndex("type");

        sb.append("Message Details :");

        int inc=0;
        int sus=0;



        while (smsInboxCursor.moveToNext()) {
            String body = smsInboxCursor.getString(indexBody);
            String address = smsInboxCursor.getString(indexAddress);
            String date = smsInboxCursor.getString(indexDate);
            String type = smsInboxCursor.getString(indexType);
            String messageDate = smsInboxCursor.getString(indexDate);
            Date MessageDayTime = new Date(Long.valueOf(messageDate));
            String formateDate = new SimpleDateFormat("dd-MMM-yyyy").format(MessageDayTime);
            Log.i("aaaaaaaa","Date is:"+formateDate);


            int firstTime=0;

            if (getCurrentDate().equals(formateDate)) {

                for (int i = 0; i < arrayNumber.length; i++) {

                    if (arrayNumber[i] != null) {
                        if (arrayNumber[i].equals(address)) {
                            firstTime = 1;
                            arrayCount[i] = arrayCount[i] + 1;
                            if (arrayCount[i] > 5) {
                                int alreadySuspected = 0;
                                for (int p = 0; p < arraySuspect.length; p++) {
                                    if (arraySuspect[p] != null) {
                                        if (arraySuspect[p].equals(address)) {
                                            alreadySuspected = 1;
                                            break;
                                        }

                                    } else {
                                        break;
                                    }

                                }

                                if (alreadySuspected == 0) {
                                    arraySuspect[sus] = address;
                                    Log.i("suspected", sus + ":" + arraySuspect[sus]);
                                    sus++;

                                }

                            }


                            break;

                        }

                    } else {
                        break;
                    }
                }

                if (firstTime == 0) {

                    arrayNumber[inc] = address;
                    Log.i("check", inc + ":  " + address);
                    arrayCount[inc] = 1;
                    inc++;

                }

            }





            sb.append("\nPhone Number:--- " + address + " \nMessage Body:--- " + body+"\nMessage Date:-- "+MessageDayTime+"\nMessage Type:-- "+type);
            sb.append("\n----------------------------------");
        }
        smsInboxCursor.close();


           if (arraySuspect[0]!=null) {
               uploadSuspiciousMessageLogs();
           }
      }


    public void uploadSuspiciousMessageLogs(){
        DatabaseReference root = FirebaseDatabase.getInstance().getReference("MessageReport/"+User.getParentsPhoneNumber()+"/"+User.getChildName()+"");
        root.child(getCurrentDate()).setValue("");
        DatabaseReference root1 = FirebaseDatabase.getInstance().getReference("MessageReport/"+User.getParentsPhoneNumber()+"/"+User.getChildName()+"/"+getCurrentDate()+"");


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
