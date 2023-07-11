package dev.sora.protohax.relay.gui;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class GuiData {

    public static List<List> viewsList = new ArrayList<>();
    public static WindowManager currentWindowManager;

    public static LinearLayout currentLayout;

    public static FuncList fl;

    public static void removeOneViews(String funcName){
        if(null == currentWindowManager){
            Log.e("MiracleLight","currentWindowManager NullPointer");return;
        }
        for(List currentList : viewsList){
            if(null == currentList){
                Log.e("MiracleLight","currentList NullPointer");
                continue;
            }
            if(null == currentList.get(0)){
                Log.e("MiracleLight","Can't find view");
                return;
            } if(null == currentList.get(1)){
                Log.e("MiracleLight","Can't find module name");
                return;
            }
            String currentName = (String)currentList.get(1);
            if(currentName.equals(funcName)) {
                currentWindowManager.removeView((View) (currentList.get(0)));
            }
        }
        viewsList.clear();
        Log.i("MiracleLight","Views are Removed successfully.");
        return;
    }
    public static void removeAllViews(){
        if(null == currentWindowManager){
            Log.e("MiracleLight","currentWindowManager NullPointer");
            return;
        }
        for(List currentList : viewsList){
            if(null == currentList){
                Log.e("MiracleLight","currentList NullPointer");
                continue;
            }
            if(null == currentList.get(0)){
                Log.e("MiracleLight","Can't find view");
                return;
            }
            currentWindowManager.removeView((View) (currentList.get(0)));
        }
        viewsList.clear();
        Log.i("MiracleLight","Views are Removed successfully.");
        return;
    }
}
