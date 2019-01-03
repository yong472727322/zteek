package com.zteek.entity;

import java.util.Date;

public class AllCookie {
     
	private String DomainS;
	private Date ExpirationDateS;
	private boolean HostOnlyS;
	private boolean HttpOnlyS;
	private String NameS;
	private String PathS;
	private boolean SecureS;
	private boolean SessionS;
	private String StoreIdS;
	private String ValueS;
	private int IdS;
	
	

	public AllCookie(String domainS, Date expirationDateS, boolean hostOnlyS, boolean httpOnlyS, String nameS,
			String pathS, boolean secureS, boolean sessionS, String storeIdS, String valueS, int idS) {
		super();
		DomainS = domainS;
		ExpirationDateS = expirationDateS;
		HostOnlyS = hostOnlyS;
		HttpOnlyS = httpOnlyS;
		NameS = nameS;
		PathS = pathS;
		SecureS = secureS;
		SessionS = sessionS;
		StoreIdS = storeIdS;
		ValueS = valueS;
		IdS = idS;
	}

	public String getDomainS() {
		return DomainS;
	}

	public void setDomainS(String domainS) {
		DomainS = domainS;
	}

	public Date getExpirationDateS() {
		return ExpirationDateS;
	}

	public void setExpirationDateS(Date expirationDateS) {
		ExpirationDateS = expirationDateS;
	}

	public boolean isHostOnlyS() {
		return HostOnlyS;
	}

	public void setHostOnlyS(boolean hostOnlyS) {
		HostOnlyS = hostOnlyS;
	}

	public boolean isHttpOnlyS() {
		return HttpOnlyS;
	}

	public void setHttpOnlyS(boolean httpOnlyS) {
		HttpOnlyS = httpOnlyS;
	}

	public String getNameS() {
		return NameS;
	}

	public void setNameS(String nameS) {
		NameS = nameS;
	}

	public String getPathS() {
		return PathS;
	}

	public void setPathS(String pathS) {
		PathS = pathS;
	}

	public boolean isSecureS() {
		return SecureS;
	}

	public void setSecureS(boolean secureS) {
		SecureS = secureS;
	}

	public boolean isSessionS() {
		return SessionS;
	}

	public void setSessionS(boolean sessionS) {
		SessionS = sessionS;
	}

	public String getStoreIdS() {
		return StoreIdS;
	}

	public void setStoreIdS(String storeIdS) {
		StoreIdS = storeIdS;
	}

	public String getValueS() {
		return ValueS;
	}

	public void setValueS(String valueS) {
		ValueS = valueS;
	}

	public int getIdS() {
		return IdS;
	}

	public void setIdS(int idS) {
		IdS = idS;
	}

	@Override
	public String toString() {
		return "AllCookie [DomainS=" + DomainS + ", ExpirationDateS=" + ExpirationDateS + ", HostOnlyS=" + HostOnlyS
				+ ", HttpOnlyS=" + HttpOnlyS + ", NameS=" + NameS + ", PathS=" + PathS + ", SecureS=" + SecureS
				+ ", SessionS=" + SessionS + ", StoreIdS=" + StoreIdS + ", ValueS=" + ValueS + ", IdS=" + IdS + "]";
	}
	
	
	
	
	
	
}
  