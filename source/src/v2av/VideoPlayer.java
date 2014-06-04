package v2av;

import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.SurfaceHolder;

public class VideoPlayer 
{
	public static int DisplayRotation = 0;
	
	private SurfaceHolder mSurfaceH;
	private Bitmap mBitmap;
	private Matrix mMatrix = null;
	
//	private int mDisplayMode = 0;	//0,按比例显示,1全屏显示,2原尺寸显示
	private int mClearCanvas = 2;
	
	private int mRotation = 0;
	private int mBmpRotation = 0;
	
	private VideoDisplayMatrix mDisMatrix;
	
	private ByteBuffer _playBuffer;
	
	public VideoPlayer()
	{
	}
	
	public void zoomIn()
	{
		if(mDisMatrix == null)
			return;
		
		mDisMatrix.zoomIn();
		mMatrix = mDisMatrix.getDisplayMatrix();
		mClearCanvas = 0;
	}
	public void zoomOut()
	{
		if(mDisMatrix == null)
			return;
		
		mDisMatrix.zoomOut();
		mMatrix = mDisMatrix.getDisplayMatrix();
		mClearCanvas = 0;
	}
	public void translate(float dx, float dy)
	{
		if(mDisMatrix == null)
			return;
		
		mDisMatrix.translate(dx, dy);
		mMatrix = mDisMatrix.getDisplayMatrix();
		mClearCanvas = 0;
	}
	
	public void zoomTo(float scale, float cx, float cy, float durationMs)
	{
		mDisMatrix.zoomTo(scale, cx, cy, durationMs);
		mClearCanvas = -2;
	}
	
	private float mBaseScale;
	//取得全屏显示 时的尺度大小(按比例显示 )
	public float getBaseScale()
	{
		return mBaseScale;
	}
	
	public float getScale()
	{
		if (mDisMatrix == null)
		{
			return 0.0f;
		}
		
		return mDisMatrix.getScale();
	}
	
	private void UpdateMatrix()
	{	
		mDisMatrix.resetMatrix();
		mMatrix = mDisMatrix.getDisplayMatrix();
		mBaseScale = mDisMatrix.getScale();
	}
	
	public void SetViewSize(int w, int h)
	{	
		mClearCanvas = 0;
		
		if(mDisMatrix != null)
		{
			mDisMatrix.setViewSize(w, h);
			UpdateMatrix();
		}
	}
	
	public void SetSurface(SurfaceHolder holder)
	{
		mSurfaceH = holder;
	}
	
	public void SetRotation(int rotation)
	{
		if (mRotation == rotation)
		{
			return;
		}
		
		int temp = (rotation + 45) / 90 * 90;
    	temp = (temp + DisplayRotation) % 360;
    	
    	temp = 360 - temp;
    	
    	if (mRotation == temp)
		{
			return;
		}
    	
    	mRotation = temp;

		mClearCanvas = 0;

		if(mDisMatrix != null)
		{
			mDisMatrix.setRotation((mRotation + mBmpRotation) % 360);
			UpdateMatrix();
		}
	}
	
//	public void SetDisplayMode(int mode)
//	{
//		mDisplayMode = mode;
//		if(mDisMatrix != null)
//		{
//			UpdateMatrix();
//		}
//	}
	
	void Release()
	{
		if(mBitmap != null &&
				!mBitmap.isRecycled())
		{
			mBitmap.recycle();
		}
		
		mMatrix = null;
		mSurfaceH = null;
		mBitmap = null;
		mDisMatrix = null;
	}
	
	/*
	 * Called by native 
	 */
	@SuppressWarnings("unused")
	private void SetBitmapRotation(int rotation)
	{
		mBmpRotation = rotation;	
		mClearCanvas = 0;
		
		if(mDisMatrix != null)
		{
			mDisMatrix.setRotation((mRotation + mBmpRotation) % 360);
			UpdateMatrix();
		}
	}
	
	/*
	 * Called by native 
	 */
	@SuppressWarnings("unused")
	private void CreateBitmap(int width, int height)
	{
		Log.i("jni","call create bitmap " + width + " " + height);
		if (mBitmap != null && !mBitmap.isRecycled())
		{
			mBitmap.recycle();
		}
		
		//mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		mBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
		
		if(mDisMatrix == null)
			mDisMatrix = new VideoDisplayMatrix();
		
		mDisMatrix.setBitmap(mBitmap);
		mDisMatrix.setRotation((mRotation + mBmpRotation) % 360);
		
		mClearCanvas = 0;

		Canvas canvas = mSurfaceH.lockCanvas();
		if(canvas != null)
		{
			SetViewSize(canvas.getWidth(), canvas.getHeight());
			mSurfaceH.unlockCanvasAndPost(canvas);
		}
		
		_playBuffer = ByteBuffer.allocateDirect(width*height*4);
	}
	
	/*
	 * Called by native
	 */
	@SuppressWarnings("unused")
	private void OnPlayVideo()
	{
		mBitmap.copyPixelsFromBuffer(_playBuffer);
		_playBuffer.rewind();
		
		Canvas canvas = mSurfaceH.lockCanvas();
		if(canvas == null)
		{
			return;
		}
		if(mClearCanvas < 2)
		{
			canvas.drawRGB(0, 0, 0);
			++mClearCanvas;
		}
		if(mMatrix == null)
		{
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}
		else
		{
			canvas.drawBitmap(mBitmap, mMatrix, null);
		}
		
		mSurfaceH.unlockCanvasAndPost(canvas);
	}
}
