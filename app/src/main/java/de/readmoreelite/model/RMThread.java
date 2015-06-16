package de.readmoreelite.model;
import java.util.List;


public class RMThread {
	
	String titel;
	int id;
	int forumId;
	int anzahlSeiten;
	User erstelltVon;
	RMStatus read;
    String letzterBeitrag;
    String letzterBeitragDatum;
    int anzahlBeitraege;

    public int getAnzahlBeitraege() {
        return anzahlBeitraege;
    }

    public void setAnzahlBeitraege(int anzahlBeitraege) {
        this.anzahlBeitraege = anzahlBeitraege;
    }

    public String getLetzterBeitragDatum() {
        return letzterBeitragDatum;
    }

    public void setLetzterBeitragDatum(String letzterBeitragDatum) {
        this.letzterBeitragDatum = letzterBeitragDatum;
    }

    public RMThread() {

    }

    public String getLetzterBeitrag() {
        return letzterBeitrag;
    }

    public void setLetzterBeitrag(String letzterBeitrag) {
        this.letzterBeitrag = letzterBeitrag;
    }

	public RMStatus getRead() {
		return read;
	}

	public void setRead(RMStatus read) {
		this.read = read;
	}

	public int getAnzahlSeiten() {
		return anzahlSeiten;
	}

	public void setAnzahlSeiten(int anzahlSeiten) {
		this.anzahlSeiten = anzahlSeiten;
	}

	public int getForumId() {
		return forumId;
	}

	public void setForumId(int forumId) {
		this.forumId = forumId;
	}

	public String getTitel() {
		return titel;
	}

	public int getId() {
		return id;
	}

	public User getErstelltVon() {
		return erstelltVon;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setErstelltVon(User erstelltVon) {
		this.erstelltVon = erstelltVon;
	}

	public String toString() {
		
		return this.titel;
	}
}
