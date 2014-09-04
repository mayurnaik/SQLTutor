package objects;

public class UserTuple {
	private String email;
	private boolean admin;
	private String adminCode;
	private boolean dev;
	
	public UserTuple(final String email, final boolean admin, final String adminCode, final boolean dev) {
		this.email = email;
		this.admin = admin;
		this.adminCode = adminCode;
		this.dev = dev;
	}

	public UserTuple() {
		email = "";
		admin = false;
		adminCode = "";
		dev = false;
	}
	
	public String getAdminStatus() {
		return admin + " (" + adminCode + ")";
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getAdminCode() {
		return adminCode;
	}

	public void setAdminCode(String adminCode) {
		this.adminCode = adminCode;
	}

	public boolean isDev() {
		return dev;
	}

	public void setDev(boolean dev) {
		this.dev = dev;
	}
}
