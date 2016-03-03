package com.silambarasan.smartstack.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.silambarasan.smartstack.R;


public class LoadingProgress extends Dialog
{



	String content = "";

	public LoadingProgress(Context context, String content)
	{
		super(context, R.style.TransparentProgressDialog);
		this.content = content;
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		Window window = this.getWindow();
		window.setBackgroundDrawableResource(android.R.color.transparent);
		this.setCancelable(false);
		this.setOnCancelListener(null);

		LayoutParams layoutParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View progressView = inflater.inflate(R.layout.layout_progress_view,
				new LinearLayout(context));
		TextView contentText = (TextView) progressView
				.findViewById(R.id.loadingContent);
		contentText.setText(content);

		this.addContentView(progressView, layoutParams);

	}


	@Override
	public void show()
	{
		super.show();
	}

}
