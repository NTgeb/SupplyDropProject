package com.example.supplydrop;

import android.util.Patterns;

import java.util.regex.Pattern;

public class Validator {

    public Validator(){//Constructor

    }

    public boolean email(String sEmail){
        return Patterns.EMAIL_ADDRESS.matcher(sEmail).matches(); //Pattern checker for email
    }

    public boolean phone(String sPhone){
        return Patterns.PHONE.matcher(sPhone).matches(); //Patter checker for Phone
    }
    public boolean password(String sPassword){
        //Check for a heavily restricted password
        /*Pattern PASSWORD_PATTERN = Pattern.compile("^" +
                        "(?=.*[@#$%^&+=])" +     // at least 1 special character
                        "(?=\\S+$)" +            // no white spaces
                        ".{8,30}" +              // at least 8 and at most 30 characters
                        "$");
        return PASSWORD_PATTERN.matcher(sPassword).matches();*/

        return minMaxLength(sPassword,8,30); // Only restricting the length
    }
    public boolean empty(String s){
        return s.isEmpty();
    }
    public boolean minLength(String s,int minLen){
        return s.length() <= minLen;
    }
    public boolean maxLength(String s,int maxLen){
        return s.length() >= maxLen;
    }
    public boolean minMaxLength(String s, int minLen,int maxLen){
        return minLength(s,minLen) && maxLength(s,maxLen);
    }
    public boolean alpha(String s){
        return s.chars().allMatch(Character::isLetter);
    }
    public boolean alphaNum(String s){
        return s.chars().allMatch(Character::isLetterOrDigit);
    }




}
