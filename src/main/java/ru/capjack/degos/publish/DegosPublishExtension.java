package ru.capjack.degos.publish;

public interface DegosPublishExtension {
	String NAME = "degosPublish";
	
	boolean isAsPrivate();
	
	void setAsPrivate(boolean value);
	
	String getUsername();
	
	void setUsername(String username);
	
	String getPassword();
	
	void setPassword(String password);
}
