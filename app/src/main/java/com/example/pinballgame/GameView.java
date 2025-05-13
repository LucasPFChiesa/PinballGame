package com.example.pinballgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread thread;

    private float ballX, ballY, ballRadius = 30;
    private float ballSpeedX = 5, ballSpeedY = 5;

    private float paddleWidth = 200;
    private float paddleHeight = 30;
    private float leftPaddleX, leftPaddleY;
    private float rightPaddleX, rightPaddleY;
    private boolean isLeftPressed = false;
    private boolean isRightPressed = false;

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        ballX = getWidth() / 2f;
        ballY = getHeight() / 3f;

        float paddleY = getHeight() - 150;
        leftPaddleX = 100;
        leftPaddleY = paddleY;
        rightPaddleX = getWidth() - paddleWidth - 100;
        rightPaddleY = paddleY;

        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) { }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    public void update() {
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        if (ballX < ballRadius || ballX > getWidth() - ballRadius) {
            ballSpeedX = -ballSpeedX;
        }

        if (ballY < ballRadius) {
            ballSpeedY = -ballSpeedY;
        }

        if (ballY + ballRadius >= leftPaddleY &&
                ballX > leftPaddleX && ballX < leftPaddleX + paddleWidth) {
            ballSpeedY = -Math.abs(ballSpeedY);
        }

        if (ballY + ballRadius >= rightPaddleY &&
                ballX > rightPaddleX && ballX < rightPaddleX + paddleWidth) {
            ballSpeedY = -Math.abs(ballSpeedY);
        }

        if (ballY > getHeight()) {
            ballX = getWidth() / 2f;
            ballY = getHeight() / 3f;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float x = event.getX();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (x < getWidth() / 2f) {
                    isLeftPressed = true;
                } else {
                    isRightPressed = true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (x < getWidth() / 2f) {
                    isLeftPressed = false;
                } else {
                    isRightPressed = false;
                }
                break;
        }
        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            canvas.drawColor(Color.BLACK);

            Paint paint = new Paint();
            paint.setAntiAlias(true);

            paint.setColor(Color.WHITE);
            canvas.drawCircle(ballX, ballY, ballRadius, paint);

            paint.setColor(isLeftPressed ? Color.YELLOW : Color.RED);
            canvas.drawRect(leftPaddleX, leftPaddleY,
                    leftPaddleX + paddleWidth, leftPaddleY + paddleHeight, paint);

            paint.setColor(isRightPressed ? Color.YELLOW : Color.RED);
            canvas.drawRect(rightPaddleX, rightPaddleY,
                    rightPaddleX + paddleWidth, rightPaddleY + paddleHeight, paint);
        }
    }

    class GameThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private GameView gameView;
        private boolean running;

        public GameThread(SurfaceHolder holder, GameView view) {
            this.surfaceHolder = holder;
            this.gameView = view;
        }

        public void setRunning(boolean isRunning) {
            this.running = isRunning;
        }

        @Override
        public void run() {
            while (running) {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();
                    synchronized (surfaceHolder) {
                        gameView.update();
                        gameView.draw(canvas);
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }

                try {
                    sleep(16);
                } catch (InterruptedException e) {}
            }
        }
    }
}
