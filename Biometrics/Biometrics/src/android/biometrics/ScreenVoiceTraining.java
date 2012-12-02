package android.biometrics;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.biometrics.util.AppConst;
import android.biometrics.util.AppUtil;
import android.biometrics.util.IconContextMenu;
import android.biometrics.voice.VoiceHelper;
import android.biometrics.voice.VoiceItem;
import android.biometrics.voice.VoiceTrainer;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenVoiceTraining extends Activity {

	private static final String TAG = ScreenVoiceTraining.class
			.getCanonicalName();

	private List<String> mVoicePaths;
	private MediaPlayer mPlayer;
	private ListView mListView;
	private VoiceAdapter mAdapter;
	private List<VoiceItem> voiceInfoSet;

	private int TouchPosition;
	// For Menu
	private final int CONTEXT_MENU_ID = 1;
	private IconContextMenu iconContextMenu = null;

	private final int MENU_ITEM_1_ACTION = 1;
	private final int MENU_ITEM_2_ACTION = 2;
	private final int MENU_ITEM_3_ACTION = 3;
	private final int MENU_ITEM_4_ACTION = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_voice_training);

		ImageView bt;
		bt = (ImageView) findViewById(R.id.btnRecord);
		bt.setOnClickListener(OnClickButtonHandler);
		bt = (ImageView) findViewById(R.id.btnSave);
		bt.setOnClickListener(OnClickButtonHandler);
//		bt = (ImageView) findViewById(R.id.btnSettings);
//		bt.setOnClickListener(OnClickButtonHandler);
//		bt = (ImageView) findViewById(R.id.btnModeRecognizing);
//		bt.setOnClickListener(OnClickButtonHandler);

		mVoicePaths = new ArrayList<String>();
		voiceInfoSet = new ArrayList<VoiceItem>();
		
		/**
		 * Should use this methods, because: u enter voice section, record some voice samples,
		 * but press Back suddenly -> clear all voices in UI.
		 * If implement this method, u still keep them, I've implement a method for remove all voices 
		 * if u do not need them anymore :: Press Menu, Clear All Voices.
		 */
		fetchLocalVoices();

		mListView = (ListView) findViewById(R.id.listview_voice);
		mAdapter = new VoiceAdapter(this);
		mListView.setAdapter(mAdapter);
		
		View emptyView = getLayoutInflater().inflate(R.layout.view_voice_empty, null);
		ViewGroup parent = (ViewGroup)mListView.getParent();
		parent.addView(emptyView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mListView.setEmptyView(emptyView);

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> adapter, View parent,
					int position, long id) {
				TouchPosition = position;
				showDialog(CONTEXT_MENU_ID);
				return true;
			}

		});

		mListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				TouchPosition = pos;
				showDialog(CONTEXT_MENU_ID);

				// TODO Play this voice
			}

		});

		Resources res = getResources();

		// init the menu
		iconContextMenu = new IconContextMenu(this, CONTEXT_MENU_ID);
		iconContextMenu.addItem(res, "Delete", MENU_ITEM_1_ACTION);
		iconContextMenu.addItem(res, "Delete all", MENU_ITEM_2_ACTION);
		// iconContextMenu.addItem(res, "Play", MENU_ITEM_3_ACTION);
		// iconContextMenu.addItem(res, "Stop", MENU_ITEM_4_ACTION);

		// set onclick listener for context menu
		iconContextMenu
				.setOnClickListener(new IconContextMenu.IconContextMenuOnClickListener() {
					public void onClick(int menuId) {
						switch (menuId) {
						case MENU_ITEM_1_ACTION:
							voiceInfoSet.remove(TouchPosition);
							mAdapter.notifyDataSetChanged();
							break;
						case MENU_ITEM_2_ACTION:
							int size = voiceInfoSet.size();
							for (int i = 0; i < size; i++) {
								voiceInfoSet.remove(0);
							}
							mAdapter.notifyDataSetChanged();
							break;
						case MENU_ITEM_3_ACTION:

							break;
						case MENU_ITEM_4_ACTION:
							Toast.makeText(getApplicationContext(),
									"You've clicked on menu item 4", 1000)
									.show();
							break;
						}
					}
				});

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST, Menu.NONE, getString(R.string.clear_all_voices));
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			AppUtil.clearTrainingSampleFromSdcard(false);
			
			mVoicePaths.clear();
			voiceInfoSet.clear();
			mAdapter.notifyDataSetChanged();
			break;
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == CONTEXT_MENU_ID) {
			return iconContextMenu.createMenu("Options");
		}
		return super.onCreateDialog(id);
	}

	View.OnClickListener OnClickButtonHandler = new View.OnClickListener() {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnRecord:
				if (!AppUtil.isSDCardAvailable()) {
					Toast.makeText(ScreenVoiceTraining.this,
							getString(R.string.toast_no_sdcard_available),
							Toast.LENGTH_SHORT).show();
					break;
				}
				Intent intent = new Intent(ScreenVoiceTraining.this,
						ScreenVoiceRecording.class);
				startActivityForResult(intent, AppConst.REQ_RECORD_VOICE);

				break;
			case R.id.btnSave:
				if (!AppUtil.isSDCardAvailable()) {
					Toast.makeText(ScreenVoiceTraining.this,
							getString(R.string.toast_no_sdcard_available),
							Toast.LENGTH_SHORT).show();
					break;
				}

				train();
				break;
//			case R.id.btnSettings:
//				Intent i = new Intent(ScreenVoiceTraining.this,
//						ScreenSettings.class);
//				startActivity(i);
//				break;
//			case R.id.btnModeRecognizing:
//				Intent intent1 = new Intent(ScreenVoiceTraining.this,
//						ScreenVoiceRecognizing.class);
//				startActivity(intent1);
//				break;
			}
		}
	};

	@Override
	public void onCreateContextMenu(ContextMenu contextMenu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		String selectedWord = ((TextView) info.targetView).getText().toString();
		// long selectedWordId = info.id;

		contextMenu.setHeaderTitle(selectedWord);
		contextMenu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST, Menu.NONE, "1");
		contextMenu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST, Menu.NONE, "2");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	private void fetchLocalVoices() {
		File folder = new File(AppConst.APP_FOLDER);
		if (!folder.exists())
			AppUtil.createAppDirectory();

		File[] files = folder.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().toLowerCase()
						.endsWith(VoiceHelper.VOICE_EXTENSION);
			}
		});

		
		
		if (files == null)
			return;
		
		MediaPlayer p = new MediaPlayer();
		for (File f : files) {
			String path = f.getAbsolutePath();
			mVoicePaths.add(path);
			try {
				p.setDataSource(path);
				p.prepare();
				voiceInfoSet.add(new VoiceItem(path, ""+p.getDuration()));
				
				Log.i(TAG, ".Local voice = " + p.getDuration()+" -> "+path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void train() {
		if (voiceInfoSet.size() < AppConst.MIN_VOICE_SAMPLE) {
			Toast.makeText(this, getString(R.string.toast_not_enough_voices, AppConst.MIN_VOICE_SAMPLE),
					Toast.LENGTH_LONG).show();
			return;
		}
		
		for (int i = 0; i < voiceInfoSet.size(); i++) {
			mVoicePaths.add(voiceInfoSet.get(i).getPath());
		}

		VoiceTrainer trainer = new VoiceTrainer(ScreenVoiceTraining.this);
		trainer.train(mVoicePaths);
		
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == AppConst.REQ_RECORD_VOICE && resultCode == RESULT_OK) {
			String newVoicePath = data.getExtras().getString("path");
			String newVoiceDuration = data.getExtras().getString("duration");

			voiceInfoSet.add(new VoiceItem(newVoicePath, newVoiceDuration));
			mAdapter.notifyDataSetChanged();
		}
		
	}

	private class VoiceAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public VoiceAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return voiceInfoSet.size();
		}

		public Object getItem(int position) {
			return voiceInfoSet.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			VoiceViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.view_listview_voice,
						null);
				holder = new VoiceViewHolder();
				holder.tvtDuration = (TextView) convertView
						.findViewById(R.id.txt_lv_duration);
				holder.tvtPath = (TextView) convertView
						.findViewById(R.id.txt_lv_path);
				convertView.setTag(holder);
			} else
				holder = (VoiceViewHolder) convertView.getTag();

			VoiceItem voice = (VoiceItem) getItem(position);
			holder.tvtDuration.setText(voice.getDuration());
			holder.tvtPath.setText(voice.getPath());
			return convertView;
		}
	}

	static class VoiceViewHolder {
		TextView tvtDuration;
		TextView tvtPath;
	}

}
