package com.example.forensics_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;
import java.util.Random;

public class FlappyView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    // Exposed tunables for reverse engineering via smali
    public static float GRAVITY = 0.6f;
    public static float FLAP_POWER = -10f;
    public static float PIPE_SPEED = 6f;
    public static float GAP_SIZE_PX = 450f;
    public static float BIRD_TO_PIPE_WIDTH_RATIO = 0.4f;
    public static float SCORE_TEXT_SIZE_SP = 28f;
    private Thread gameThread;
    private boolean running = false;

    private float birdX;
    private float birdY;
    private float birdVelocity;
    private float gravity = GRAVITY;
    private float flapPower = FLAP_POWER;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap pipeBottomBitmap;
    private Bitmap pipeTopBitmap;
    private Bitmap birdBitmap;
    private Bitmap backgroundBitmap;
    private Bitmap pipeBottomBitmapScaled;
    private Bitmap pipeTopBitmapScaled;
    private Bitmap birdBitmapScaled;
    private Bitmap backgroundBitmapScaled;

    private float pipeX;
    private float pipeGapY;
    private float pipeSpeed = PIPE_SPEED;
    private float gapSizePx = GAP_SIZE_PX;
    private boolean scoredCurrentPipe = false;
    private int score = 0;

    private enum GameState { RUNNING, OVER }
    private GameState gameState = GameState.RUNNING;

    private final Random random = new Random();

    public FlappyView(Context context) {
        super(context);
        init();
    }

    public FlappyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
        setFocusable(true);
        pipeBottomBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bottompipe);
        // Optional assets from repo
        int topPipeId = getResources().getIdentifier("toppipe", "drawable", getContext().getPackageName());
        if (topPipeId != 0) {
            pipeTopBitmap = BitmapFactory.decodeResource(getResources(), topPipeId);
        }
        int birdId = getResources().getIdentifier("flappybird", "drawable", getContext().getPackageName());
        if (birdId != 0) {
            birdBitmap = BitmapFactory.decodeResource(getResources(), birdId);
        }
        int bgId = getResources().getIdentifier("flappybirdbg", "drawable", getContext().getPackageName());
        if (bgId != 0) {
            backgroundBitmap = BitmapFactory.decodeResource(getResources(), bgId);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        createScaledBitmaps();
        resetGame();
        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    public void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stop() {
        running = false;
        if (gameThread != null) {
            try { gameThread.join(); } catch (InterruptedException ignored) {}
            gameThread = null;
        }
    }

    private void createScaledBitmaps() {
        if (backgroundBitmap != null) {
            backgroundBitmapScaled = Bitmap.createScaledBitmap(backgroundBitmap, getWidth(), getHeight(), true);
        }
        if (pipeBottomBitmap != null) {
            int pipeBottomHeight = (int)(getHeight() * 0.45f);
            int pipeBottomWidth = (int)(pipeBottomBitmap.getWidth() * (pipeBottomHeight / (float)pipeBottomBitmap.getHeight()));
            pipeBottomBitmapScaled = Bitmap.createScaledBitmap(pipeBottomBitmap, pipeBottomWidth, pipeBottomHeight, true);
        }
        if (pipeTopBitmap != null && pipeBottomBitmapScaled != null) {
            // Match width to bottom pipe; maintain aspect ratio
            int pipeTopHeight = (int)(getHeight() * 0.45f);
            int pipeTopWidth = pipeBottomBitmapScaled.getWidth();
            pipeTopBitmapScaled = Bitmap.createScaledBitmap(pipeTopBitmap, pipeTopWidth, pipeTopHeight, true);
        }
        if (birdBitmap != null && pipeBottomBitmapScaled != null) {
            int birdWidth = (int)(pipeBottomBitmapScaled.getWidth() * BIRD_TO_PIPE_WIDTH_RATIO);
            float aspect = birdBitmap.getHeight() / (float) birdBitmap.getWidth();
            int birdHeight = Math.max(1, (int)(birdWidth * aspect));
            birdBitmapScaled = Bitmap.createScaledBitmap(birdBitmap, birdWidth, birdHeight, true);
        }
    }

    private void resetGame() {
        birdX = getWidth() * 0.25f;
        birdY = getHeight() * 0.5f;
        birdVelocity = 0f;
        pipeX = getWidth();
        pipeGapY = randomGapCenter();
        scoredCurrentPipe = false;
        score = 0;
        gameState = GameState.RUNNING;
    }

    private float randomGapCenter() {
        float margin = getHeight() * 0.15f;
        return margin + random.nextFloat() * (getHeight() - 2 * margin);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float deltaSec = (now - lastTime) / 1_000_000_000f;
            lastTime = now;
            update(deltaSec);
            drawFrame();
        }
    }

    private void update(float dt) {
        if (gameState != GameState.RUNNING) return;

        birdVelocity += gravity;
        birdY += birdVelocity;

        boolean hitFloor = birdBottom() >= getHeight();
        boolean hitCeiling = birdTop() <= 0;
        if (hitFloor || hitCeiling) {
            gameState = GameState.OVER;
            return;
        }

        pipeX -= pipeSpeed;
        if (pipeRight() < 0) {
            pipeX = getWidth();
            pipeGapY = randomGapCenter();
            scoredCurrentPipe = false;
        }

        if (!scoredCurrentPipe && pipeCenterX() < birdX) {
            score += 1;
            scoredCurrentPipe = true;
        }

        if (intersects(birdLeft(), birdTop(), birdRight(), birdBottom(),
                pipeLeft(), topPipeTop(), pipeRight(), topPipeBottom()) ||
            intersects(birdLeft(), birdTop(), birdRight(), birdBottom(),
                pipeLeft(), bottomPipeTop(), pipeRight(), bottomPipeBottom())) {
            gameState = GameState.OVER;
        }
    }

    private void drawFrame() {
        Canvas canvas = null;
        try {
            canvas = getHolder().lockCanvas();
            if (canvas == null) return;
            if (backgroundBitmapScaled != null) {
                canvas.drawBitmap(backgroundBitmapScaled, 0, 0, null);
            } else {
                canvas.drawColor(Color.rgb(135, 206, 235));
            }

            if (birdBitmapScaled != null) {
                float birdHalfW = birdBitmapScaled.getWidth() / 2f;
                float birdHalfH = birdBitmapScaled.getHeight() / 2f;
                canvas.drawBitmap(birdBitmapScaled, birdX - birdHalfW, birdY - birdHalfH, null);
            } else {
                paint.setColor(Color.YELLOW);
                canvas.drawCircle(birdX, birdY, 40, paint);
            }

            if (pipeBottomBitmapScaled != null) {
                float bottomY = bottomPipeTop();
                canvas.drawBitmap(pipeBottomBitmapScaled, pipeX, bottomY, null);
            }
            if (pipeTopBitmapScaled != null) {
                float topY = topPipeTop();
                canvas.drawBitmap(pipeTopBitmapScaled, pipeX, topY, null);
            }

            paint.setColor(Color.WHITE);
            paint.setTextSize(dpToPx(SCORE_TEXT_SIZE_SP));
            canvas.drawText("Score: " + score, 20, dpToPx(40), paint);

            if (gameState == GameState.OVER) {
                paint.setColor(Color.argb(160, 0, 0, 0));
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
                paint.setColor(Color.WHITE);
                paint.setTextSize(dpToPx(32));
                canvas.drawText("Game Over", getWidth() * 0.35f, getHeight() * 0.4f, paint);
                paint.setTextSize(dpToPx(20));
                canvas.drawText("Tap to try again", getWidth() * 0.33f, getHeight() * 0.5f, paint);
            }
        } finally {
            if (canvas != null) getHolder().unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameState == GameState.OVER) {
                resetGame();
                return true;
            }
            birdVelocity = flapPower;
            return true;
        }
        return super.onTouchEvent(event);
    }

    private float birdLeft() {
        float w = birdBitmapScaled != null ? birdBitmapScaled.getWidth() : 80f;
        return birdX - w / 2f;
    }

    private float birdRight() {
        float w = birdBitmapScaled != null ? birdBitmapScaled.getWidth() : 80f;
        return birdX + w / 2f;
    }

    private float birdTop() {
        float h = birdBitmapScaled != null ? birdBitmapScaled.getHeight() : 80f;
        return birdY - h / 2f;
    }

    private float birdBottom() {
        float h = birdBitmapScaled != null ? birdBitmapScaled.getHeight() : 80f;
        return birdY + h / 2f;
    }

    private float pipeLeft() { return pipeX; }
    private float pipeRight() {
        float w = pipeBottomBitmapScaled != null ? pipeBottomBitmapScaled.getWidth() : 0f;
        return pipeX + w;
    }
    private float pipeCenterX() { return (pipeLeft() + pipeRight()) / 2f; }
    private float topPipeTop() { return (pipeGapY - gapSizePx / 2f) - (pipeTopBitmapScaled != null ? pipeTopBitmapScaled.getHeight() : 0f); }
    private float topPipeBottom() { return (pipeGapY - gapSizePx / 2f); }
    private float bottomPipeTop() { return (pipeGapY + gapSizePx / 2f); }
    private float bottomPipeBottom() { return (pipeGapY + gapSizePx / 2f) + (pipeBottomBitmapScaled != null ? pipeBottomBitmapScaled.getHeight() : 0f); }

    private boolean intersects(float l1, float t1, float r1, float b1, float l2, float t2, float r2, float b2) {
        return l1 < r2 && r1 > l2 && t1 < b2 && b1 > t2;
    }

    private float dpToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}


