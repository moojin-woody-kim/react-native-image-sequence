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

public class RCTImageSequenceView extends ImageView {
    private Integer framesPerSecond = 24;
    private Boolean loop = true;
    private ArrayList<AsyncTask> activeTasks;
    private HashMap<Integer, Bitmap> bitmaps;
    private RCTResourceDrawableIdHelper resourceDrawableIdHelper;
    private RCTImageSequenceListener finishEventListener;

    public RCTImageSequenceView(Context context) {
        super(context);
        resourceDrawableIdHelper = new RCTResourceDrawableIdHelper();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final Integer index;
        private final String uri;
        private final Context context;

        public DownloadImageTask(Integer index, String uri, Context context) {
            Log.i("ReactNativeImageSequence", "DownloadImageTask");
            this.index = index;
            this.uri = uri;
            this.context = context;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Log.i("ReactNativeImageSequence", "doInBackground");
            if (this.uri.startsWith("http")) {
                return this.loadBitmapByExternalURL(this.uri);
            }
            
            return this.loadBitmapByLocalFile(this.uri);
            // return this.loadBitmapByLocalResource(this.uri);
        }

        private Bitmap loadBitmapByLocalResource(String uri) {
            Log.i("ReactNativeImageSequence", "loadBitmapByLocalResource");
            return BitmapFactory.decodeResource(this.context.getResources(), resourceDrawableIdHelper.getResourceDrawableId(this.context, uri));
        }

        private Bitmap loadBitmapByLocalFile(String uri) {
            Log.i("ReactNativeImageSequence", "loadBitmapByLocalFile");
            return BitmapFactory.decodeFile(uri);
        }

        private Bitmap loadBitmapByExternalURL(String uri) {
            Log.i("ReactNativeImageSequence", "loadBitmapByExternalURL");
            Bitmap bitmap = null;

            try {
                InputStream in = new URL(uri).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.i("ReactNativeImageSequence", "onPostExecute");
            if (!isCancelled()) {
                onTaskCompleted(this, index, bitmap);
            }
        }
    }

    private void onTaskCompleted(DownloadImageTask downloadImageTask, Integer index, Bitmap bitmap) {
        Log.i("ReactNativeImageSequence", "onTaskCompleted");
        if (index == 0) {
            // first image should be displayed as soon as possible.
            this.setImageBitmap(bitmap);
        }

        bitmaps.put(index, bitmap);
        activeTasks.remove(downloadImageTask);

        if (activeTasks.isEmpty()) {
            setupAnimationDrawable();
        }
    }

    public void setImages(ArrayList<String> uris) {
        Log.i("ReactNativeImageSequence", "setImages");
        if (isLoading()) {
            // cancel ongoing tasks (if still loading previous images)
            for (int index = 0; index < activeTasks.size(); index++) {
                activeTasks.get(index).cancel(true);
            }
        }

        activeTasks = new ArrayList<>(uris.size());
        bitmaps = new HashMap<>(uris.size());

        for (int index = 0; index < uris.size(); index++) {
            DownloadImageTask task = new DownloadImageTask(index, uris.get(index), getContext());
            activeTasks.add(task);

            try {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (RejectedExecutionException e){
                Log.e("react-native-image-sequence", "DownloadImageTask failed" + e.getMessage());
                break;
            }
        }
    }

    public void setFramesPerSecond(Integer framesPerSecond) {
        Log.i("ReactNativeImageSequence", "setFramesPerSecond");
        this.framesPerSecond = framesPerSecond;

        // updating frames per second, results in building a new AnimationDrawable (because we cant alter frame duration)
        if (isLoaded()) {
            setupAnimationDrawable();
        }
    }

    public void setLoop(Boolean loop) {
        Log.i("ReactNativeImageSequence", "setLoop");
        this.loop = loop;

        // updating looping, results in building a new AnimationDrawable
        if (isLoaded()) {
            setupAnimationDrawable();
        }
    }

    public void addFinishEventListener(RCTImageSequenceListener finishEventListener) {
        Log.i("================","addFinishEventListener");
        this.finishEventListener = finishEventListener;
    }

    private boolean isLoaded() {
        return !isLoading() && bitmaps != null && !bitmaps.isEmpty();
    }

    private boolean isLoading() {
        return activeTasks != null && !activeTasks.isEmpty();
    }

    private void setupAnimationDrawable() {
        Log.i("ReactNativeImageSequence", "setupAnimationDrawable");
        RCTImageSequenceAnimationDrawable animationDrawable = new RCTImageSequenceAnimationDrawable();
        for (int index = 0; index < bitmaps.size(); index++) {
            BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bitmaps.get(index));
            animationDrawable.addFrame(drawable, 1000 / framesPerSecond);
        }
        animationDrawable.setAnimationFinishListener(this.finishEventListener);
        animationDrawable.setOneShot(!this.loop);

        this.setImageDrawable(animationDrawable);
        animationDrawable.start();
    }

    
}



