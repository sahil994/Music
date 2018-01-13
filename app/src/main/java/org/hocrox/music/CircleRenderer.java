package org.hocrox.music;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.Log;

public class CircleRenderer extends Renderer
{
    private Paint mPaint;
    private boolean mCycleColor;
    Paint mOrangePaint,mYellowPaint;
    /**
     * Renders the audio data onto a pulsing circle
     * 
     * @param paint - Paint to draw lines with
     */
    public CircleRenderer(Paint paint)
    {
        this(paint, false);
    }

    /**
     * Renders the audio data onto a pulsing circle
     * 
     * @param paint - Paint to draw lines with
     * @param cycleColor - If true the color will change on each frame
     */
    public CircleRenderer(Paint paint, boolean cycleColor)
    {
        super();
        mPaint = paint;
        mCycleColor = cycleColor;
         mOrangePaint =new Paint();
        mOrangePaint.setStrokeWidth(5f);
        mOrangePaint.setAntiAlias(true);
        mOrangePaint.setColor(Color.rgb(255, 101, 1));

        mYellowPaint =new Paint();
        mYellowPaint.setStrokeWidth(5f);
        mYellowPaint.setAntiAlias(true);
        mYellowPaint.setColor(Color.rgb(255, 230, 0));
    }
 boolean value=true;
    int cal=0;
    @Override
    public void onRender(Canvas canvas, AudioData data, Rect rect)
    {
        if(value){

            Log.e("callme","cllaa"+data.bytes.length);
            for (int i = 0; i <data.bytes.length/4; i++) {
                //      Log.e("testing bytes",""+data.bytes.length+">>>>"+2);
                cal=i;
                mPoints[i * 4] = i * 4 * 4;
                mPoints[i * 4 + 2] = i * 4 * 4;
                byte rfk = data.bytes[2 * i];
                byte ifk = data.bytes[2 * i + 1];
                Log.e("tres",""+rfk+">>>"+ifk);
                float magnitude = (rfk * rfk + ifk * ifk)*10;
                int dbValue = (int) (10 * Math.log10(magnitude));

            /*if (mTop)
            {
                mFFTPoints[i * 4 + 1] = 0;
                mFFTPoints[i * 4 + 3] = (dbValue * 2 - 10);
            }
            else



            {*/



                Path path1=new Path();
                path1.lineTo( mPoints[i * 4],mPoints[i * 4 + 2]);
                canvas.drawPath(path1,mPaint);



                mPoints[i * 4 + 1] = rect.height();

                Path path2=new Path();
                path2.lineTo(mPoints[i * 4 + 2], mPoints[i * 4 + 1]);
                canvas.drawPath(path2,mYellowPaint);



                mPoints[i * 4 + 3] = rect.height() - (dbValue * 4 - 60);


                Path path3=new Path();
                path2.lineTo(mPoints[i * 4 + 2], mPoints[i * 4 + 3]);
                canvas.drawPath(path3,mOrangePaint);



//            }
            }
            Paint paint=new Paint();
            int r = (int) Math.floor(128 * (Math.sin(colorCounter) + 1));
            int g = (int) Math.floor(128 * (Math.sin(colorCounter + 2) + 1));
            int b = (int) Math.floor(128 * (Math.sin(colorCounter + 4) + 1));
            paint.setColor(Color.argb(128, r, g, b));

   mPaint.setStrokeWidth(12f);
           mPaint.setShader(new LinearGradient(0,0,0,500,Color.parseColor("#008542"), Color.parseColor("#881202"), Shader.TileMode.MIRROR));
           canvas.drawLines(mPoints, mPaint);

            Log.e("dassdcscscs","callededf");
        }

       /* if (mCycleColor)
        {
            cycleColor();
        }

        for (int i = 0; i < data.bytes.length - 1; i++) {
            float[] cartPoint = {
                    (float) i / (data.bytes.length - 1),
                    rect.height() / 2 + ((byte) (data.bytes[i] + 128)) * (rect.height() / 2) / 128
            };

            float[] polarPoint = toPolar(cartPoint, rect);
            mPoints[i * 4] = polarPoint[0];
            mPoints[i * 4 + 1] = polarPoint[1];

            float[] cartPoint2 = {
                    (float) (i + 1) / (data.bytes.length - 1),
                    rect.height() / 2 + ((byte) (data.bytes[i + 1] + 128)) * (rect.height() / 2)
                            / 128
            };

            float[] polarPoint2 = toPolar(cartPoint2, rect);
            mPoints[i * 4 + 2] = polarPoint2[0];
            mPoints[i * 4 + 3] = polarPoint2[1];
        }
*/
    //    canvas.drawLines(mPoints, mPaint);

        // Controls the pulsing rate
        modulation += 0.04;
    }

    @Override
    public void onRender(Canvas canvas, FFTData data, Rect rect)
    {
        // Do nothing, we only display audio data
    }

    float modulation = 0;
    float aggresive = 0.33f;

    private float[] toPolar(float[] cartesian, Rect rect)
    {
        double cX = rect.width() / 2;
        double cY = rect.height() / 2;
        double angle = (cartesian[0]) * 2 * Math.PI;
        double radius = ((rect.width() / 2) * (1 - aggresive) + aggresive * cartesian[1] / 2)
                * (1.2 + Math.sin(modulation)) / 2.2;
        float[] out = {
                (float) (cX + radius * Math.sin(angle)),
                (float) (cY + radius * Math.cos(angle))
        };
        return out;
    }

    private float colorCounter = 0;

    private void cycleColor()
    {
        int r = (int) Math.floor(128 * (Math.sin(colorCounter) + 1));
        int g = (int) Math.floor(128 * (Math.sin(colorCounter + 2) + 1));
        int b = (int) Math.floor(128 * (Math.sin(colorCounter + 4) + 1));
        mPaint.setColor(Color.argb(128, r, g, b));
        colorCounter += 0.03;
    }
}