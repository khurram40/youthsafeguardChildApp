package com.example.child;

public class User {

    public  static String parentsPhoneNumber,childName;


    public static  void setParentsPhoneNumber(String pn){

        parentsPhoneNumber=pn;
    }




    public static String getParentsPhoneNumber(){
        return parentsPhoneNumber;
    }


    public static  void setChildName(String cn){

        childName=cn;
    }

    public static String getChildName(){
        return childName;
    }



}
