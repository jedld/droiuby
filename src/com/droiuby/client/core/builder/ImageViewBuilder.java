package com.droiuby.client.core.builder;

import org.jdom2.Element;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class ImageViewBuilder extends ViewBuilder {

	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return new ImageView(context);
	}

	@Override
	public View setParams(View child, Element e) {
		String src = e.getAttributeValue("src");
		ImageView img = (ImageView)child;
		if (src != null) {
			if (src.startsWith("@drawable:")) {
				String drawable = src.substring(10);
				int resId = builder.getDrawableId(drawable);
				if (resId != 0) {
					img.setImageResource(resId);
				}
			} else if (src.startsWith("@preload:")) {
				Drawable drawable = (Drawable) builder.findViewByName(src);
				if (drawable != null) {
					img.setImageDrawable(drawable);
				}
			} else {

				UrlImageViewHelper.setUrlDrawable(img, builder.normalizeUrl(src),
						"setImageDrawable");
			}
		}
		return super.setParams(child, e);
	}

	
}
