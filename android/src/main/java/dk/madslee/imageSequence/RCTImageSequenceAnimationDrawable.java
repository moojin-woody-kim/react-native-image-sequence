package dk.madslee.imageSequence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;

import android.util.Log;

public class RCTImageSequenceAnimationDrawable extends AnimationDrawable
{
    private boolean finished = false;
    public  RCTImageSequenceListener animationFinishListener;

    public void setAnimationFinishListener(RCTImageSequenceListener animationFinishListener)
    { 
      Log.i("================", "setAnimationFinishListener");
        this.animationFinishListener = animationFinishListener;
    }

    @Override
    public boolean selectDrawable(int idx)
    {
        boolean ret = super.selectDrawable(idx);

        if ((idx != 0) && (idx == getNumberOfFrames() - 1))
        {
            if (!finished)
            {
                finished = true;
                Log.i("=================", "selectDrawable");
                if (animationFinishListener != null) animationFinishListener.onAnimationFinished();
            }
        }

        return ret;
    }
}