package it.ap.wesnoth;

import it.alessandropira.wesnoth112.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class Tutorial extends LinearLayout implements SurfaceHolder.Callback {
	private static final int IMG_SIZE = 96;
	private Bitmap background;
	private Button btnPrev, btnNext, btnSkip;
	private Rect bgRect;
	private Bitmap hand;
	private Bitmap handp;
	private Bitmap hand2;
	private Bitmap hand2p;
	private Bitmap kbd;
	private Bitmap mouse;
	private Bitmap mousebl;
	private Bitmap mousebr;
	private Bitmap pointer;
	private Bitmap osk_1, osk_2, osk_3, osk_4;
	private Updater updater;
	private Activity owner;
	private int dataPtr;
	private Screen data[][];
	private SurfaceView surface;
//	private TextView textView;
	private Thread endRun;

	private void initData() {
		Resources res = owner.getResources();
		data = new Screen[][] {
			{ new Screen(res.getString(R.string.ap_tut1), IMG_SIZE, IMG_SIZE, 10) },
			{ new Screen(res.getString(R.string.ap_tut2), IMG_SIZE, IMG_SIZE, 10) },
			{ new Screen(res.getString(R.string.ap_tut3), IMG_SIZE, IMG_SIZE, 10) },
			null,
			{
				new Screen(res.getString(R.string.ap_tut5), IMG_SIZE * 2 + 80, IMG_SIZE + 20, 10, new Sprite(hand, 0, 20), new Sprite(mouse, IMG_SIZE + 80, 10)),
				new Screen(res.getString(R.string.ap_tut5), IMG_SIZE * 2 + 80, IMG_SIZE + 20, 2, new Sprite(handp, 0, 0), new Sprite(hand, 0, 0), new Sprite(mouse, IMG_SIZE + 80, 10)),
				new Screen(res.getString(R.string.ap_tut5), IMG_SIZE * 2 + 80, IMG_SIZE + 20, 5, new Sprite(hand, 0, 20), new Sprite(mouse, IMG_SIZE + 80, 10)),
				new Screen(res.getString(R.string.ap_tut5), IMG_SIZE * 2 + 80, IMG_SIZE + 20, 2, new Sprite(hand, 0, 20), new Sprite(mouse, IMG_SIZE + 80, 10), new Sprite(mousebl, IMG_SIZE + 80, 10))
			},
			{
				new Screen(res.getString(R.string.ap_tut6), IMG_SIZE * 2 + 80, IMG_SIZE + 20, 10, new Sprite(hand, 0, 20), new Sprite(mouse, IMG_SIZE + 80, 10)),
				new Screen(res.getString(R.string.ap_tut6), IMG_SIZE * 2 + 80, IMG_SIZE + 20, 10, new Sprite(handp, 0, 0), new Sprite(hand, 0, 0), new Sprite(mouse, IMG_SIZE + 80, 10)),
				new Screen(res.getString(R.string.ap_tut6), IMG_SIZE * 2 + 80, IMG_SIZE + 20, 2, new Sprite(handp, 0, 0), new Sprite(hand, 0, 0), new Sprite(mouse, IMG_SIZE + 80, 10), new Sprite(mousebr, IMG_SIZE + 80, 10)),
				new Screen(res.getString(R.string.ap_tut6), IMG_SIZE * 2 + 80, IMG_SIZE + 20, 4, new Sprite(handp, 0, 0), new Sprite(hand, 0, 0), new Sprite(mouse, IMG_SIZE + 80, 10)),
				new Screen(res.getString(R.string.ap_tut6), IMG_SIZE * 2 + 80, IMG_SIZE + 20, 4, new Sprite(hand, 0, 20), new Sprite(mouse, IMG_SIZE + 80, 10)),
			},
			{ new Screen(res.getString(R.string.ap_tut6a), IMG_SIZE, IMG_SIZE, 10) },
			{ new Screen(res.getString(R.string.ap_tut6b), IMG_SIZE, IMG_SIZE, 10) },
			{ new Screen(res.getString(R.string.ap_tut7), IMG_SIZE, IMG_SIZE, 10) },
			null,
			{
				new Screen(res.getString(R.string.ap_tut8), IMG_SIZE, IMG_SIZE + 20, 10, new Sprite(kbd, 0, 0), new Sprite(hand, 0, 20)),
				new Screen(res.getString(R.string.ap_tut8), IMG_SIZE, IMG_SIZE + 20, 2, new Sprite(kbd, 0, 0), new Sprite(hand, 0, 0))
			},
			{ new Screen(res.getString(R.string.ap_tut9), IMG_SIZE, IMG_SIZE, 10) },
			{ new Screen(res.getString(R.string.ap_tut10), IMG_SIZE, IMG_SIZE, 10) },
			{ new Screen(res.getString(R.string.ap_tut10a), IMG_SIZE * 4 + 60, IMG_SIZE, 10, new Sprite(osk_1, 0, 0), new Sprite(osk_2, IMG_SIZE + 20, 0), new Sprite(osk_3, 2*(IMG_SIZE + 20), 0), new Sprite(osk_4, 3*(IMG_SIZE + 20), 0)) },
			{ new Screen(res.getString(R.string.ap_tut10b), IMG_SIZE, IMG_SIZE, 10) },
			{ new Screen(res.getString(R.string.ap_tut11), IMG_SIZE, IMG_SIZE, 10) },
		};

		int null1 = -1;
		int null2 = -1;

		for (int i=0; i<data.length; i++) {
			if (data[i] != null) {
				continue;
			}
			if (null1 < 0) {
				null1 = i;
				continue;
			}
			if (null2 < 0) {
				null2 = i;
				break;
			}
		}

		if (null1 >= 0) {
			data[null1] = new Screen [19];
			data[null1][0] = new Screen(res.getString(R.string.ap_tut4), IMG_SIZE * 2 + 160, IMG_SIZE + 20, 4, new Sprite(hand, 0, 20), new Sprite(pointer, IMG_SIZE + 80, 10));
			data[null1][1] = new Screen(res.getString(R.string.ap_tut4), IMG_SIZE * 2 + 160, IMG_SIZE + 20, 4, new Sprite(handp, 0, 0), new Sprite(hand, 0,  0), new Sprite(pointer, IMG_SIZE + 80, 10));
			for (int x=0; x<15; x++) {
				int x2 = x*4;
				data[null1][x+2] = new Screen(res.getString(R.string.ap_tut4), IMG_SIZE * 2 + 160, IMG_SIZE + 20, 1, new Sprite(handp, x2, 0), new Sprite(hand, x2,  0), new Sprite(pointer, IMG_SIZE + 80 + x2, 10));
			}
			data[null1][17] = new Screen(res.getString(R.string.ap_tut4), IMG_SIZE * 2 + 160, IMG_SIZE + 20, 4, new Sprite(handp, 60, 0), new Sprite(hand, 60,  0), new Sprite(pointer, IMG_SIZE + 80 + 60, 10));
			data[null1][18] = new Screen(res.getString(R.string.ap_tut4), IMG_SIZE * 2 + 160, IMG_SIZE + 20, 4, new Sprite(hand, 60,  20), new Sprite(pointer, IMG_SIZE + 80 + 60, 10));
		}

		if (null2 >= 0) {
			data[null2] = new Screen [19];
			data[null2][0] = new Screen(res.getString(R.string.ap_tut7a), IMG_SIZE + 60, IMG_SIZE + 20, 4, new Sprite(hand2, 0, 20));
			data[null2][1] = new Screen(res.getString(R.string.ap_tut7a), IMG_SIZE + 60, IMG_SIZE + 20, 4, new Sprite(hand2p, 0, 0), new Sprite(hand2, 0,  0));
			for (int x=0; x<15; x++) {
				int x2 = x*4;
				data[null2][x+2] = new Screen(res.getString(R.string.ap_tut7a), IMG_SIZE + 60, IMG_SIZE + 20, 1, new Sprite(hand2p, x2, 0), new Sprite(hand2, x2,  0));
			}
			data[null2][17] = new Screen(res.getString(R.string.ap_tut7a), IMG_SIZE + 60, IMG_SIZE + 20, 4, new Sprite(hand2p, 60, 0), new Sprite(hand2, 60,  0));
			data[null2][18] = new Screen(res.getString(R.string.ap_tut7a), IMG_SIZE + 60, IMG_SIZE + 20, 4, new Sprite(hand2, 60,  20));
		}

		endRun = null;
	}

	public Tutorial(Context context) {
		super(context);

		owner = (Activity)context;
		Resources res = context.getResources();
		hand = BitmapFactory.decodeResource(res, R.drawable.tut_hand);
		handp = BitmapFactory.decodeResource(res, R.drawable.tut_handp);
		hand2 = BitmapFactory.decodeResource(res, R.drawable.tut_hand2);
		hand2p = BitmapFactory.decodeResource(res, R.drawable.tut_hand2p);
		kbd = BitmapFactory.decodeResource(res, R.drawable.tut_kbd);
		mouse = BitmapFactory.decodeResource(res, R.drawable.tut_mouse);
		mousebl = BitmapFactory.decodeResource(res, R.drawable.tut_mousebtn_left);
		mousebr = BitmapFactory.decodeResource(res, R.drawable.tut_mousebtn_right);
		pointer = BitmapFactory.decodeResource(res, R.drawable.tut_pointer);
		background = BitmapFactory.decodeResource(res, R.drawable.wesnoth_ap_logo);
		osk_1 = BitmapFactory.decodeResource(res, R.drawable.tut_nextunit);
		osk_2 = BitmapFactory.decodeResource(res, R.drawable.tut_undo);
		osk_3 = BitmapFactory.decodeResource(res, R.drawable.tut_enemymoves);
		osk_4 = BitmapFactory.decodeResource(res, R.drawable.tut_bestenemymoves);
		bgRect = new Rect(0, 0, background.getWidth(), background.getHeight());

		this.dataPtr = -1;

		setBackgroundColor(0xff000000);
        setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams lp;

//	        textView = new TextView(context);
//	        textView.setMinLines(2);
//	        textView.setMaxLines(2);
//	        textView.setTextColor(0xffffffff);
//	        textView.setPadding(5, 5, 5, 5);
//	        textView.setGravity(Gravity.CENTER);
//	        textView.setText("ProvaTut");
//	        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//	        lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);
//	        textView.setLayoutParams(lp);
//	       
//	    addView(textView);

			surface = new SurfaceView(context);
			lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1.0f);
			surface.setLayoutParams(lp);

		addView(surface);
		
			LinearLayout layPrevNext = new LinearLayout(context);
			layPrevNext.setOrientation(LinearLayout.HORIZONTAL);
			layPrevNext.setGravity(Gravity.RIGHT);
			layPrevNext.setBackgroundColor(0xff000000);
	        lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);
	        layPrevNext.setLayoutParams(lp);
        
		    	btnSkip = new Button(context);
		        btnSkip.setText(res.getString(R.string.ap_tut_btn_skip));
		        btnSkip.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
		    layPrevNext.addView(btnSkip);

	        	btnPrev = new Button(context);
		        btnPrev.setText(res.getString(R.string.ap_tut_btn_prev));
		        btnPrev.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
		    layPrevNext.addView(btnPrev);

		    	btnNext = new Button(context);
		        btnNext.setText(res.getString(R.string.ap_tut_btn_next));
		        btnNext.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
		    layPrevNext.addView(btnNext);

	    btnPrev.setVisibility(INVISIBLE);
	    btnNext.setVisibility(INVISIBLE);
	    btnSkip.setVisibility(INVISIBLE);

		setBtnStyle(owner, btnPrev);
		setBtnStyle(owner, btnNext);
		setBtnStyle(owner, btnSkip);

		addView(layPrevNext);

		updater = new Updater(this, new Screen[0]);
		surface.getHolder().addCallback(this);

		btnNext.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				next();
				checkEnableBtns();
			}
		});
		btnPrev.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				prev();
				checkEnableBtns();
			}
		});
		btnSkip.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				end();
			}
		});
	}

	private static void setBtnStyle(Activity owner, Button btn) {
		if (btn.isEnabled()) {
			btn.setBackgroundColor(Color.BLACK);
			btn.setBackgroundResource(R.drawable.tut_button);
		    btn.setTextColor(Color.WHITE);
		} else {
			btn.setBackgroundColor(Color.BLACK);
			btn.setBackgroundResource(R.drawable.tut_button_dis);
		    btn.setTextColor(Color.GRAY);
		}
	}

	private synchronized void end() {
		dataPtr = -1;
		updater.setScreens(new Screen[0]);
		if (this.endRun != null) {
			this.endRun.start();
		}
	}
	public boolean isRunning() {
		return dataPtr >= 0;
	}
	public synchronized void setRunOnEnd(Thread endRun) {
		if (!isRunning()) {
			endRun.start();
		} else {
			this.endRun = endRun;
		}
	}
	public synchronized void start() {
		initData();
		if (data.length == 0) {
			data = new Screen[][] { new Screen[0] };
		}
		this.dataPtr = 0;
		updater.setScreens(data[0]);
		checkEnableBtns();
	}
	public synchronized void showIssue(String text) {
		data = new Screen[][] { { new Screen(text, IMG_SIZE, IMG_SIZE, 10) } };
		this.dataPtr = 0;
		updater.setScreens(data[0]);
		owner.runOnUiThread(new Runnable() { @Override public void run() {
			btnPrev.setVisibility(GONE);
			btnNext.setVisibility(GONE);
			btnSkip.setText(R.string.ok);
			btnSkip.setVisibility(VISIBLE);
		}});
	}
	private synchronized boolean hasNext() {
		return (dataPtr < data.length - 1);
	}
	private synchronized void next() {
		if (hasNext()) {
			updater.setScreens(data[++dataPtr]);
		}
	}
	private synchronized boolean hasPrev() {
		return (dataPtr > 0);
	}
	private synchronized void prev() {
		if (hasPrev()) {
			updater.setScreens(data[--dataPtr]);
		}
	}

	private void checkEnableBtns() {
		owner.runOnUiThread(new Runnable() { @Override public void run() {
			btnNext.setVisibility(VISIBLE);
			btnPrev.setVisibility(VISIBLE);
	
			boolean hp = hasPrev();
			boolean hn = hasNext();
	
			btnPrev.setEnabled(hp);
			btnNext.setEnabled(hn);
	
			setBtnStyle(owner, btnPrev);
			setBtnStyle(owner, btnNext);

			if (!hp) {
				btnSkip.setText(owner.getResources().getString(R.string.ap_tut_btn_skip));
				btnSkip.setVisibility(VISIBLE);
			} else if (!hn) {
				btnSkip.setText(owner.getResources().getString(R.string.ap_tut_btn_end));
				btnSkip.setVisibility(VISIBLE);
			} else {
				btnSkip.setVisibility(INVISIBLE);
			}
		}});
	}

	private interface DrawCallback {
		public void doDraw(Canvas canvas);
	}

	private void paint(DrawCallback cb) {
		Canvas canvas = null;
		SurfaceHolder surfaceHolder = null;

		try {
			surfaceHolder = surface.getHolder();
			canvas = surfaceHolder.lockCanvas(null);
			if (canvas != null) {
				synchronized (surfaceHolder) {
					cb.doDraw(canvas);
				}
			}
		} finally {
			if (canvas != null) {
				surfaceHolder.unlockCanvasAndPost(canvas);
			}
		}
	}

	public static class Sprite {
		private Bitmap bitmap;
		private int x, y;
		public Sprite(Bitmap bmp, int x, int y) {
			this.bitmap = bmp;
			this.x = x;
			this.y = y;
		}
	}

	public static class Screen {
		private Sprite sprites[];
		private String text;
		private int width, height, sleep;
		public Screen(String text, int width, int height, int sleep, Sprite ... sprites) {
			if (sleep <= 0) {
				sleep = 10;
			}
			this.text = text;
			this.width = width;
			this.height = height;
			this.sleep = sleep;
			this.sprites = sprites;
		}
	}

	private static class Updater implements Runnable {
		private volatile boolean stop;
		private Thread thread;
		private Tutorial parent;
		private Paint bg, bgTrasp, text;
		private Screen[] screens;

		public Updater(Tutorial parent, Screen ... screens) {
			this.stop = false;
			this.parent = parent;
			this.bg = new Paint();
			this.bg.setColor(0xff000000);
			this.bg.setAntiAlias(true);

			this.text = new Paint();
			this.text.setAntiAlias(true);
			this.text.setTextSize(24);
			this.text.setColor(Color.WHITE);
			this.text.setTextAlign(Paint.Align.CENTER);

			this.bgTrasp = new Paint();
			this.bgTrasp.setAntiAlias(true);
			this.bgTrasp.setColor(0x80000000);

			this.screens = screens;
			this.thread = null;
		}

		private void stop() {
			this.stop = true;
			if (thread != null) {
				thread.interrupt();
				try {
					thread.join();
				} catch (InterruptedException e) {
				}
			}
			thread = null;
		}

		public void setScreens(Screen ... screens) {
			stop();
			this.screens = screens;
			start();
		}

		public void start() {
			stop();
			thread = new Thread(this);
			thread.start();
		}

		private void drawBg(Canvas canvas, boolean trasp) {
			int cw = canvas.getWidth();
			int ch = canvas.getHeight();
			canvas.drawRect(0, 0, cw, ch, bg);

			if (parent.background != null) {
				int bw = parent.background.getWidth();
				int bh = parent.background.getHeight();
				double fac = Math.min((float)cw / bw, (float)ch / bh);
				int tw = (int)(bw * fac);
				int th = (int)(bh * fac);
				Rect target = new Rect((cw - tw)/2, (ch - th)/2, tw, th);
				canvas.drawBitmap(parent.background, parent.bgRect, target, trasp ? bgTrasp : bg);
			}
		}

		private static final char NL_CHAR = '|';

		private String substrChkTrim(String str, int start, int end) {
			if (end <= str.length()) {
				return str.substring(start, end).replace(NL_CHAR, ' ').trim();
			}
			return str.substring(start).replace(NL_CHAR, ' ').trim();
		}
		private int breakText(String txt, int idx, int maxw) {
			int ptr = idx;

			while (ptr < txt.length()) {
				int nextTok = txt.indexOf(' ', ptr);
				int nextNl = txt.indexOf(NL_CHAR, ptr);

				if (nextTok < 0) {
					nextTok = txt.length();
				}

				if (nextNl >= 0 && nextNl < nextTok) {
					if (text.measureText(substrChkTrim(txt, idx, nextNl)) > maxw) {
						break;
					}
					return nextNl + 1;
				}

				if (text.measureText(substrChkTrim(txt, idx, nextTok)) > maxw) {
					break;
				}
				ptr = nextTok + 1;
			}
			return ptr;

		}

		private int drawText(Canvas canvas, String txt) {
			int idx = 0;
			int maxw = canvas.getWidth();
			FontMetrics fm = text.getFontMetrics();
			float y = 0;
			float row = fm.leading + fm.descent - fm.ascent;

			while (idx < txt.length()) {
				y += row;
				int next = breakText(txt, idx, maxw);
				if (next <= idx) {
					break;
				}
				
				String str = substrChkTrim(txt, idx, next);
				canvas.drawText(str, maxw / 2, y, text);
				idx = next;
			}
			if (y < 3*row) {
				y = 3*row;
			}
			return (int)y;
		}

		@Override
		public void run() {
			this.stop = false;
			try {
				while (!stop) {
					if (screens.length == 0) {
						parent.owner.runOnUiThread(new Runnable() { @Override public void run() {
//							parent.textView.setText("");
							parent.btnNext.setVisibility(INVISIBLE);
							parent.btnPrev.setVisibility(INVISIBLE);
							parent.btnSkip.setVisibility(INVISIBLE);
						}});
						parent.paint(new DrawCallback() { @Override public void doDraw(Canvas canvas) {
							drawBg(canvas, false);
						}});
						break;
					}

					for (final Screen screen : screens) {
//						parent.owner.runOnUiThread(new Runnable() { @Override public void run() {
//							parent.textView.setText(screen.text);
//						}});
						parent.paint(new DrawCallback() { @Override public void doDraw(Canvas canvas) {
							drawBg(canvas, true);

							int text_size = drawText(canvas, screen.text);

							int ys = canvas.getHeight() - text_size;
							int dx = (canvas.getWidth() - screen.width) / 2;
							int dy = text_size + (ys - screen.height) / 3;
							for (Sprite sprite : screen.sprites) {
								canvas.drawBitmap(sprite.bitmap, sprite.x + dx, sprite.y + dy, null);
							}
						}});
						Thread.sleep(screen.sleep * 100);
						if (stop) {
							break;
						}
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		updater.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		updater.stop();
	}
}
