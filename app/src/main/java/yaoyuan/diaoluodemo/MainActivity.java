package yaoyuan.diaoluodemo;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LoveLayout mLoveLayout;
    private ScheduledThreadPoolExecutor heartExecutor;//����Ե����Ķ���Ϣ��ʾ��ִ����
    private Runnable heartRunnable = new Runnable() {//��ʱ��ִ�е�runnable
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoveLayout.addLove();
                }
            });
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mLoveLayout = (LoveLayout) findViewById(R.id.LoveLayout);
        showBottomHeartDelay();
    }

    @Override
    public void onClick(View v) {
    }

    /**
     * ���ͼ��ʱ������
     */
    private void showBottomHeartDelay() {
        heartExecutor = new ScheduledThreadPoolExecutor(1);
        heartExecutor.scheduleAtFixedRate(heartRunnable, 2 * 1000, 2 * 1000, TimeUnit.MILLISECONDS);
    }
}
