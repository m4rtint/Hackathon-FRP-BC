package ca.frpbc.ui;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;
import android.widget.ImageView;

public final class ImageScaler {

	private ImageScaler() {}
	
	/**
	 * Scale an image view, respecting aspect ratio, so its larger dimension is equal to boundBoxInDp.
	 * 
	 * @param view
	 * @param boundBoxInDp
	 */
	public static void scaleImageView(ImageView view, int boundBoxInDp) {
		// Get the bitmap.
		BitmapDrawable drawing = (BitmapDrawable)view.getDrawable();
		Bitmap bitmap = (Bitmap)drawing.getBitmap();
				
		// Get the current dimensions.
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
				
		// Determine the scale.
		float xScale = boundBoxInDp / (float)width;
		float yScale = boundBoxInDp / (float)height;
		float scale = (xScale < yScale) ? xScale : yScale;
				
		// Create a scaling matrix.
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
				
		// Create a bitmap, make a drawable of it and apply it.
		Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		view.setImageBitmap(scaledBitmap);
				
		// Update the dimensions of the view.
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.width = scaledBitmap.getWidth();
		params.height = scaledBitmap.getHeight();
		view.setLayoutParams(params);
	}
}
