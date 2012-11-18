package thesis.biometrics.voice;

public class VoiceHelper {
	private String path;
	private String duration;
	
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
	public VoiceHelper(String path, String duration){
		setDuration(duration);
		setPath(path);
	}
}
