package com.neteru.n_pass.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.Display;
import android.widget.Toast;

import com.neteru.n_pass.R;
import com.neteru.n_pass.classes.models.Account;
import com.neteru.n_pass.classes.models.Profil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Ark Noam on 10/09/2018.
 */

@SuppressWarnings("unused, WeakerAccess")
public class AppUtilities {
    private Context context;
    private final static String MAIL_RGX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private SharedPreferences preferences;
    private final static String START = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 $!?@*/:#",
                                ARRIVAL = "4L$YNTqCtODRdHVQ7h9Iop:/WcKs5@uJ8eSiABaEfM+Xk0g6r?j1*v2yPw#lGnzU3m!ZFxb";
    public final static int NB = 4320;
    public final static int[] COLORS = new int[]{
            R.color.dimgray,
            R.color.red,
            R.color.black,
            R.color.blue,
            R.color.darkorange,
            R.color.gray,
            R.color.green,
            R.color.skyblue,
            R.color.darkgray,
            R.color.darkgreen,
            R.color.darkpink
    };


    public AppUtilities(Context ctx){
        context = ctx;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static AppUtilities getInstance(Context ctx){
        return new AppUtilities(ctx);
    }

    public static boolean validateMail(String mail){

        Pattern pattern = Pattern.compile(MAIL_RGX);
        Matcher matcher = pattern.matcher(mail);

        return matcher.matches();

    }

    public String hashIt(String mdp){
        try {

            String utf_8 = "UTF-8";
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] theDigest = messageDigest.digest(mdp.getBytes(utf_8));
            StringBuilder buffer = new StringBuilder();
            for (byte aTheDigest : theDigest) {
                buffer.append(Integer.toString((aTheDigest & 0xff) + 0x100, 16).substring(1));
            }

            return buffer.toString();

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();

            Toast.makeText(context, R.string.error_occured, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static String generateRandomPassword(int length){
        String[] letterRes = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
        int[] numberRes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++ ){
            if (new Random().nextInt(2) == 0){
                if (new Random().nextInt(2) == 0){
                    password.append(letterRes[new Random().nextInt(letterRes.length)]);
                }else {
                    password.append(letterRes[new Random().nextInt(letterRes.length)].toUpperCase());
                }
            }else {
                password.append(numberRes[new Random().nextInt(numberRes.length)]);
            }
        }

        return password.toString();
    }

    public String generatePassword(){
        String[] letterRes = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
        int[] numberRes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        String[] symbolsRes = {"$","!","?","@","*","/",":","#"};
        String length_str = preferences.getString("gen_length", "9");
        int length = Integer.valueOf(length_str);
        length = length > 21 || length < 3 ? 9 : length;

        boolean
                gen_majuscules = preferences.getBoolean("gen_majuscules",true),
                gen_minuscules = preferences.getBoolean("gen_minuscules",true),
                gen_chiffres = preferences.getBoolean("gen_chiffres",true),
                gen_symboles = preferences.getBoolean("gen_symboles",false);

        Boolean[] gen = {gen_majuscules, gen_minuscules, gen_chiffres, gen_symboles};

        StringBuilder password = new StringBuilder();

        if (!(!gen_majuscules && !gen_minuscules && !gen_chiffres && !gen_symboles)) {
            for (int i = 0; i < length; i++) {
                boolean y = false;
                while (!y) {
                    int z = new Random().nextInt(gen.length);
                    if (gen[z]) {
                        switch (z) {
                            case 0:
                                password.append(letterRes[new Random().nextInt(letterRes.length)].toUpperCase());
                                break;
                            case 1:
                                password.append(letterRes[new Random().nextInt(letterRes.length)]);
                                break;
                            case 2:
                                password.append(numberRes[new Random().nextInt(numberRes.length)]);
                                break;
                            case 3:
                                password.append(symbolsRes[new Random().nextInt(symbolsRes.length)]);
                                break;
                        }
                        y = true;
                    }
                }
            }
        }else{
            Toast.makeText(context, R.string.change_gen_settings, Toast.LENGTH_SHORT).show();
        }

        return password.toString();
    }

    private static void shuffleArray(int[] array)
    {
        int index, temp;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    public static String getCryptKey(){
        int[] tempKeys = new int[START.length()];
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < START.length(); i++){ tempKeys[i] =  i; }

        shuffleArray(tempKeys);

        for (int i = 0; i < START.length(); i++){
            if (i == START.length() - 1){
                result.append(tempKeys[i]);
            }else {
                result.append(tempKeys[i]).append("-");
            }
        }

        return result.toString();
    }

    public static String[] encrypt(@NonNull String str, @Nullable String key){
        StringBuilder result = new StringBuilder();
        if (key == null){
            key = getCryptKey();
        }

        for (int y = 0; y < str.length(); y++){

            if (START.indexOf(str.charAt(y)) != -1){
                String[] strKeys = key.split("-");

                result.append(ARRIVAL.charAt(Integer.valueOf(strKeys[START.indexOf(str.charAt(y))])));

            }else {

                result.append(str.charAt(y));

            }

        }

        return new String[]{result.toString(), key};
    }

    public static String decrypt(@NonNull String str, @NonNull String key){
        StringBuilder result= new StringBuilder();
        String[] strKeys = key.split("-");

        for (int i = 0; i < str.length(); i++){

            if (ARRIVAL.indexOf(str.charAt(i)) != -1){

                for (int y = 0; y < strKeys.length; y++){
                    if (Integer.valueOf(strKeys[y]) == ARRIVAL.indexOf(str.charAt(i))){

                        result.append(START.charAt(y));

                    }
                }

            }else{

                result.append(str.charAt(i));

            }

        }
        return result.toString();
    }

    public static Account cryptAccount(Account account){
        String key = account.getKey().isEmpty() ? getCryptKey() : account.getKey();

        return new Account(
                encrypt(account.getAccountTitle().replace("'", "\""), key)[0],
                encrypt(account.getUsername().replace("'", "\""), key)[0],
                encrypt(account.getMail().replace("'", "\""), key)[0],
                encrypt(account.getType().replace("'", "\""), key)[0],
                encrypt(account.getMdp().replace("'", "\""), key)[0],
                encrypt(account.getNotes().replace("'", "\""), key)[0],
                encrypt(account.getDate().replace("'", "\""), key)[0],
                key);

    }

    public static List<Account> decryptAccountList(List<Account> accountList){
        List<Account> output = new ArrayList<>();

        for (Account account : accountList) {
            output.add(new Account(
                    decrypt(account.getAccountTitle().replace("\"", "'"), account.getKey()),
                    decrypt(account.getUsername().replace("\"", "'"), account.getKey()),
                    decrypt(account.getMail().replace("\"", "'"), account.getKey()),
                    decrypt(account.getType().replace("\"", "'"), account.getKey()),
                    decrypt(account.getMdp().replace("\"", "'"), account.getKey()),
                    decrypt(account.getNotes().replace("\"", "'"), account.getKey()),
                    decrypt(account.getDate().replace("\"", "'"), account.getKey()),
                    account.getKey()));
        }

        return output;
    }

    public static Profil cryptProfil(Profil profil){
        String key = profil.getKey().isEmpty() ? getCryptKey() : profil.getKey();

        return new Profil(
                   encrypt(profil.getMdp(), key)[0],
                   encrypt(profil.getMail(), key)[0],
                   key);
    }

    public static List<Profil> decryptProfilList(List<Profil> profilList){
        List<Profil> output = new ArrayList<>();

        for (Profil profil : profilList) {
            output.add(new Profil(
                    decrypt(profil.getMdp(), profil.getKey()),
                    decrypt(profil.getMail(), profil.getKey()),
                    profil.getKey()));
        }

        return output;
    }

    public static String getDate(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E dd.MM.yyyy '-' HH:mm", Locale.US);

        return dateFormat.format(date);
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = true;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm.isScreenOn();
        }
    }

    public static String getDbKey(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString("key", null);
    }

    /**
     * Génère un chiffre unique à partir d'une chaîne de caractères
     * @param in / Chaîne d'entrée
     * @return chiffre unique
     */
    public static int getDigitFromString(String in){

        String length = String.valueOf(in.length());
        int l = length.length();

        while (l > 1){

            int sum = 0;

            for (int i = 0; i < l; i++){
                sum += Character.getNumericValue(length.charAt(i));
            }

            length = String.valueOf(sum);

            l = length.length();
        }

        return Integer.valueOf(length);

    }

    /**
     * Recupération des premières lettres d'une chaîne de caractère
     * @param string / chaîne de départ
     * @param uppercase / détermine la casse de la chaîne de sortie
     * @return chaîne de sortie
     */
    public static String getFirstLetters(String string, Boolean uppercase){
        String[] sections = string.trim().split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        String result;

        for (String s: sections){ stringBuilder.append(s.substring(0, 1)); }

        if (stringBuilder.length() > 2){
            result = String.valueOf(stringBuilder.charAt(0)) + stringBuilder.charAt(stringBuilder.length() - 1);
        }else {
            result = stringBuilder.toString();
        }

        if (!uppercase){ return result; }

        return result.toUpperCase();
    }

    /**
     * Recupération des premières lettres d'une chaîne de caractère
     * @param string / Chaîne de départ
     * @return chaîne de sortie
     */
    public static String getFirstLetters(String string){
        String[] sections = string.trim().split(" ");
        StringBuilder stringBuilder = new StringBuilder();

        for (String s: sections){ stringBuilder.append(s.substring(0, 1)); }

        if (stringBuilder.length() > 2){
            return String.valueOf(stringBuilder.charAt(0)) + stringBuilder.charAt(stringBuilder.length() - 1);
        }

        return stringBuilder.toString().toUpperCase();
    }

}
