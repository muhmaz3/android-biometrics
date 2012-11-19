package android.biometrics.voice;

public class VoiceItem {
	private String path;
	private String duration;
	
	public VoiceItem(String path, String duration){
		setDuration(duration);
		setPath(path);
	}
	
	public void setPath(String path){
		this.path = path;
	}
	
	public void setDuration(String duration){
		this.duration = duration;
	}
	
	public String getDuration(){
		return this.duration;
	}
	
	public String getPath(){
		return this.path;
	}
	
}
