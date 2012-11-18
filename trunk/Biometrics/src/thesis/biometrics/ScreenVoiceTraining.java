package thesis.biometrics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import thesis.biometrics.util.AppUtil;
import thesis.biometrics.util.IconContextMenu;
import thesis.biometrics.voice.VoiceHelper;
import thesis.biometrics.voice.VoiceTrainer;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenVoiceTraining extends Activity {

	private static final String TAG = ScreenVoiceTraining.class.getCanonicalName();
	private static final int MIN_VOICE_SAMPLE = 2;
	private static final int REQ_RECORD_VOICE = 7;
	
	private List<String> mVoicePaths;// = new ArrayList<String>();
	private MediaPlayer mPlayer;
	private ListView mListView;
	private VoiceAdapter mAdapter;
	private List<VoiceHelper> voiceInfoSet;
	
	private int TouchPosition;
	// For Menu 
	private final int CONTEXT_MENU_ID = 1;
	private IconContextMenu iconContextMenu = null;
	
	private final int MENU_ITEM_1_ACTION = 1;
	private final int MENU_ITEM_2_ACTION = 2;
	private final int MENU_ITEM_3_ACTION = 3;
	private final int MENU_ITEM_4_ACTION = 4;
	
	/**
	 * Change screen layout if you want, it depends on you.
	 * Notice that, this is application for algorithm testing, 
	 * not real application, so, you need to supply useful UI item
	 * for viewing easier.
	 */
	/**
	 * This is not okay, I've just help you some code
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_voice_training);
		
		ImageView bt;
		bt = (ImageView) findViewById(R.id.btnRecord);
		bt.setOnClickListener(OnClickButtonHandler);
		bt = (ImageView) findViewById(R.id.btnSave);
		bt.setOnClickListener(OnClickButtonHandler);
		
		bt = (ImageView) findViewById(R.id.btnSettings);
		bt.setOnClickListener(OnClickButtonHandler);
		bt = (ImageView) findViewById(R.id.btnModeRecognizing);
		bt.setOnClickListener(OnClickButtonHandler);
		
		if(voiceInfoSet == null){
			voiceInfoSet = new ArrayList<VoiceHelper>();
		}
		
		mListView = (ListView) findViewById(R.id.listview_voice);
        mAdapter = new VoiceAdapter(this);
        mListView.setAdapter(mAdapter);
        
        mListView.setOnItemLongClickListener(itemLongClickHandler);
       
        mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				TouchPosition = pos;
				showDialog(CONTEXT_MENU_ID);
				
				// For best GUI, diaplay a small dialog to play/pause/stop player
			}
        	
		});
        
        Resources res = getResources();
        
      //init the menu
        iconContextMenu = new IconContextMenu(this, CONTEXT_MENU_ID);
        iconContextMenu.addItem(res, "Delete",  MENU_ITEM_1_ACTION);
        iconContextMenu.addItem(res, "Delete all",  MENU_ITEM_2_ACTION);
//        iconContextMenu.addItem(res, "Play",  MENU_ITEM_3_ACTION);
//        iconContextMenu.addItem(res, "Stop",  MENU_ITEM_4_ACTION);
        
      //set onclick listener for context menu
        iconContextMenu.setOnClickListener(new IconContextMenu.IconContextMenuOnClickListener() {
			@Override
			public void onClick(int menuId) {
				switch(menuId) {
				case MENU_ITEM_1_ACTION:
					voiceInfoSet.remove(TouchPosition);
					mAdapter.notifyDataSetChanged();
					break;
				case MENU_ITEM_2_ACTION:
					int size = voiceInfoSet.size();
					for(int i = 0; i < size; i++){
						voiceInfoSet.remove(0);
					}
					mAdapter.notifyDataSetChanged();					
					break;
				case MENU_ITEM_3_ACTION:
					
					break;
				case MENU_ITEM_4_ACTION:
					Toast.makeText(getApplicationContext(), "You've clicked on menu item 4", 1000).show();
					break;
				}
			}
		});
   
	}
	
	/**
     * list item long click handler
     * used to show the context menu
     */
    private OnItemLongClickListener itemLongClickHandler = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			TouchPosition = position;
			showDialog(CONTEXT_MENU_ID);
			return true;
		}
	};

	/**
	 * create context menu
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == CONTEXT_MENU_ID) {
			return iconContextMenu.createMenu("Option");
		}
		return super.onCreateDialog(id);
	}
	
    View.OnClickListener OnClickButtonHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnRecord:
				/**
				 * TODO You have to show another screen or design a small layout (in center space) 
				 * for recording voice
				 */
				Intent intent = new Intent(ScreenVoiceTraining.this, ScreenVoiceRecording.class);
				startActivityForResult(intent, REQ_RECORD_VOICE);
				
				break;
			case R.id.btnSave:
				if (!AppUtil.isSDCardAvailable()) {
					Toast.makeText(ScreenVoiceTraining.this, "Please insert SD card to continue training!", Toast.LENGTH_SHORT).show();
					break;
				}
				saveVoice();
				break;
			case R.id.btnSettings:
				Intent i = new Intent(ScreenVoiceTraining.this, ScreenSettings.class);
				startActivity(i);
				break;
			case R.id.btnModeRecognizing:
				if (!AppUtil.isSDCardAvailable()) {
					Toast.makeText(ScreenVoiceTraining.this, "Please insert SD card to continue recognizer!", Toast.LENGTH_SHORT).show();
					break;
				}
				Intent intent1 = new Intent(ScreenVoiceTraining.this, ScreenVoiceRecognizing.class);
				startActivity(intent1);
				break;
			}
		}
	};
	
		
	@Override
	public void onCreateContextMenu(ContextMenu contextMenu,
	                                View v,
	                                ContextMenu.ContextMenuInfo menuInfo) {
	    AdapterView.AdapterContextMenuInfo info =
	            (AdapterView.AdapterContextMenuInfo) menuInfo;
	     String selectedWord = ((TextView) info.targetView).getText().toString();
//	    long selectedWordId = info.id;

	    contextMenu.setHeaderTitle(selectedWord);
	    contextMenu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST, Menu.NONE, "1");
	    contextMenu.add(Menu.CATEGORY_ALTERNATIVE, Menu.FIRST, Menu.NONE, "2");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(mPlayer != null){
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	/*private void fetchLocalVoices(){
		File folder = new File(AppConst.APP_FOLDER);
		if(!folder.exists()) AppUtil.createAppDirectory();

		File[] files = folder.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				//Filter file name depends on VOICE_EXTENSION, you can modify
				// VOICE_EXTENSION constant if you use other format. But, take care.
				if(pathname.getAbsolutePath().toLowerCase().endsWith(
						VoiceHelper.VOICE_EXTENSION))
					return true;
				return false;
			}
		});
		mVoicePaths = new ArrayList<String>();
		
		if(files == null) return;
		
		for(File f : files){
			mVoicePaths.add(f.getAbsolutePath());
			Log.i(TAG, ".Local voice = " + f.getAbsolutePath());
		}
	}
	*/		
	private void playVoiceSample(String path){
		if(mPlayer != null) mPlayer.reset();
		else mPlayer = new MediaPlayer();
		
		mPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		try {
			mPlayer.setDataSource(path);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void pauseVoicePlayer(){
		if(mPlayer!=null) mPlayer.pause();
	}
	
	private void stopVoicePlayer(){
		if(mPlayer!=null && mPlayer.isPlaying()){
			mPlayer.stop();
		}
	}
	
	private void saveVoice(){
		mVoicePaths = new ArrayList<String>();
		for(int i = 0 ; i< voiceInfoSet.size(); i++){
			mVoicePaths.add(voiceInfoSet.get(i).getPath());
		}
		if(voiceInfoSet.size() < MIN_VOICE_SAMPLE){
			Toast.makeText(this, "Please record 2 voices at least!!!", Toast.LENGTH_LONG).show();
			return;
		}		
		
		VoiceTrainer trainer = new VoiceTrainer(ScreenVoiceTraining.this);
		trainer.saveVoiceData(mVoicePaths);
		
		// Clean ListView
		int size = voiceInfoSet.size();
		for (int i = 0; i < size; i++){
			voiceInfoSet.remove(0);
//			mVoicePaths.remove(0);
		}
		mAdapter.notifyDataSetChanged();
		
	}
	
	  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    	super.onActivityResult(requestCode, resultCode, data);
	    	if(requestCode == REQ_RECORD_VOICE && resultCode==RESULT_OK){
	    		String newVoicePath = data.getExtras().getString("path");
	    		String newVoiceDuration = data.getExtras().getString("duration");
	    		
	    		Log.i("size", newVoicePath + "  "+newVoiceDuration);
	    		
	    		voiceInfoSet.add(new VoiceHelper(newVoicePath, newVoiceDuration));
	    		mAdapter.notifyDataSetChanged();
	    	}
	    }

	  private class VoiceAdapter extends BaseAdapter{

	    	private LayoutInflater mInflater;
	    	
	    	public VoiceAdapter(Context context){
	    		mInflater = LayoutInflater.from(context);
	    	}
	    	
			@Override
			public int getCount() {
				return voiceInfoSet.size();
			}

			@Override
			public Object getItem(int position) {
				return voiceInfoSet.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				VoiceViewHolder holder;
				if(convertView == null){
					convertView = mInflater.inflate(R.layout.view_listview_voice, null);
					holder = new VoiceViewHolder();
					holder.tvtDuration = (TextView) convertView.findViewById(R.id.txt_lv_duration);
					holder.tvtPath = (TextView) convertView.findViewById(R.id.txt_lv_path);
					convertView.setTag(holder);
				}else holder = (VoiceViewHolder)convertView.getTag();
				
				VoiceHelper voice = (VoiceHelper) getItem(position);
				holder.tvtDuration.setText(voice.getDuration());
				holder.tvtPath.setText(voice.getPath());
				return convertView;
			}
	    }
	      
	  static class VoiceViewHolder{
	    	TextView tvtDuration;
	    	TextView tvtPath;
	    }
	    
}
