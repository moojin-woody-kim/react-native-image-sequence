package dk.madslee.imageSequence;

import com.facebook.react.common.MapBuilder;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import android.util.Log;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Map;


public class RCTImageSequenceManager extends SimpleViewManager<RCTImageSequenceView> {
    private RCTImageSequenceView rctImageSequenceView;

    @Override
    public String getName() {
        return "RCTImageSequence";
    }

    @Override
    protected RCTImageSequenceView createViewInstance(ThemedReactContext reactContext) {
        rctImageSequenceView = new RCTImageSequenceView(reactContext);
        onReceiveNativeEvent(reactContext, rctImageSequenceView);
        return rctImageSequenceView;
    }

    public void onReceiveNativeEvent(final ThemedReactContext reactContext, final RCTImageSequenceView view) {
        view.addFinishEventListener(new RCTImageSequenceListener()
        {
            @Override
            public void onAnimationFinished(){
                Log.i("RCT===================", "onAnimationFinished");
                WritableMap event = Arguments.createMap();
                event.putString("finish", "true");    
                reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(view.getId(), "onFinish", event);
            }
        });
    }

    /**
     * sets the speed of the animation.
     *
     * @param view
     * @param framesPerSecond
     */
    @ReactProp(name = "framesPerSecond")
    public void setFramesPerSecond(final RCTImageSequenceView view, Integer framesPerSecond) {
        view.setFramesPerSecond(framesPerSecond);
    }

    /**
     * @param view
     * @param images an array of ReadableMap's {uri: "http://...."} return value of the resolveAssetSource(....)
     */
    @ReactProp(name = "images")
    public void setImages(final RCTImageSequenceView view, ReadableArray images) {
        ArrayList<String> uris = new ArrayList<>();
        for (int index = 0; index < images.size(); index++) {
            ReadableMap map = images.getMap(index);
            uris.add(map.getString("uri"));
        }
        view.setImages(uris);
    }

    /**
     * sets if animations is looped indefinitely.
     *
     * @param view
     * @param loop
     */
    @ReactProp(name = "loop")
    public void setLoop(final RCTImageSequenceView view, Boolean loop) {
        view.setLoop(loop);
    }

    //dixsoft
    @Override
    public @Nullable Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
            "onFinish",
            MapBuilder.of("registrationName", "onFinish")
    );
        
    }
}
