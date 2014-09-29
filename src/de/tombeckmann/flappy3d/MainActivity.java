package de.tombeckmann.flappy3d;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import de.tombeckmann.flappy3d.SurfaceView;

public class MainActivity extends Activity {

    private SurfaceView mView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mView = new SurfaceView(this, getResources());

        setContentView(mView);
    }
}
