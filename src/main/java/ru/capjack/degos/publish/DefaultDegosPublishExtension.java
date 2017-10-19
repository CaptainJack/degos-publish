package ru.capjack.degos.publish;

public class DefaultDegosPublishExtension implements DegosPublishExtension {
	private boolean asPrivate;
	private String  username;
	private String  password;
	
	public DefaultDegosPublishExtension(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	@Override
	public boolean isAsPrivate() {
		return asPrivate;
	}
	
	@Override
	public void setAsPrivate(boolean value) {
		asPrivate = value;
	}
	
	@Override
	public String getUsername() {
		return username;
	}
	
	@Override
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public String getPassword() {
		return password;
	}
	
	@Override
	public void setPassword(String password) {
		this.password = password;
	}
}
