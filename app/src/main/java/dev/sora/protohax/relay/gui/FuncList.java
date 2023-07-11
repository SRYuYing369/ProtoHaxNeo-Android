package dev.sora.protohax.relay.gui;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

public class FuncList extends LinearLayout{
    private final Context mCtx;
    private String[] funcList;
    private Object[][] vList;

    public FuncList showFunc(String func){
        for(Object[] obj : vList){
            if(obj[1].equals(func)){
                LinearLayout ll = (LinearLayout)obj[0];
                ll.setVisibility(View.VISIBLE);
            }
        }
        int index = 0;
        for(Object[] obj : vList){
            final TextView tv = (TextView)obj[2];
            tv.setTextSize(17);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setTextColor(Color.parseColor("#A000A0"));
            tv.setShadowLayer(8,0,0,Color.parseColor("#800080"));
            tv.postDelayed(() -> {
            },index * 150L);
            index++;
        }
        setVisibility(View.VISIBLE);
        return this;
    }



    public FuncList closeFunc(String func){
        for(Object[] obj : vList){
            if(obj[1].equals(func)){
                final LinearLayout ll = (LinearLayout)obj[0];
                ll.postDelayed(() -> ll.setVisibility(View.GONE),300);
            }
        }
        int index = 0;
        for(Object[] obj : vList){
            final TextView tv = (TextView)obj[2];
            tv.postDelayed(() -> {
            },index * 300L);
            index++;
        }
        return this;
    }

    @SuppressLint("RtlHardcoded")
    public FuncList create(){
        LinearLayout mainLayout = new LinearLayout(mCtx);
        mainLayout.setGravity(Gravity.RIGHT);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        int index = 0;
        for(String function : funcList){

            LinearLayout textLayout = new LinearLayout(mCtx);

            ShapeDrawable drawable = new ShapeDrawable(new RoundRectShape(new float[] {10, 10, 10, 10, 10, 10, 10, 10}, null, null));
            drawable.getPaint().setColor(Color.parseColor("#4D000000"));

            textLayout.setBackground(drawable);

            final TextView mainView = new TextView(mCtx);
            mainView.setText(function);
            mainView.setGravity(Gravity.RIGHT);

            mainView.postDelayed(() -> {
            },index * 150L);
            textLayout.addView(mainView);
            textLayout.setGravity(Gravity.RIGHT);
            textLayout.setVisibility(View.GONE);
            mainLayout.addView(textLayout,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            vList[index][0] = textLayout;
            vList[index][1] = function;
            vList[index][2] = mainView;
            index++;
        }
        addView(mainLayout);
        return this;
    }

    public FuncList initFuncList(String[] mfl){
        Arrays.sort(mfl,(a,b) -> b.length() - a.length());
        funcList = mfl;
        vList = new Object[mfl.length][3];
        return this;
    }

    public FuncList(Context ctx){
        super(ctx);
        mCtx = ctx;
    }
}
