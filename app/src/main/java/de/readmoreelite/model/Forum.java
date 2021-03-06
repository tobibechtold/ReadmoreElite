package de.readmoreelite.model;


public class Forum {
	
	String titel;
	int id;
	int idKategorie;
	String beschreibung;
	RMThread letzterBeitrag;
	RMStatus read;
	
	public Forum() {
		
	}

	public Forum(String titel, int id, String beschreibung,
			RMThread letzterBeitrag) {
		super();
		this.titel = titel;
		this.id = id;
		this.beschreibung = beschreibung;
		this.letzterBeitrag = letzterBeitrag;
	}

	public RMStatus getRead() {
		return read;
	}

	public void setRead(RMStatus read) {
		this.read = read;
	}

	public int getIdKategorie() {
		return idKategorie;
	}

	public void setIdKategorie(int idKategorie) {
		this.idKategorie = idKategorie;
	}

	public String getTitel() {
		return titel;
	}

	public int getId() {
		return id;
	}

	public String getBeschreibung() {
		return beschreibung;
	}

	public RMThread getLetzterBeitrag() {
		return letzterBeitrag;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public void setLetzterBeitrag(RMThread letzterBeitrag) {
		this.letzterBeitrag = letzterBeitrag;
	}
	
	public String toString() {
		
		return this.titel;
	}
	
	
}
