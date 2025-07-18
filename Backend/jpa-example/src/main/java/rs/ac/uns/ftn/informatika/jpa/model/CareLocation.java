package rs.ac.uns.ftn.informatika.jpa.model;

import javax.persistence.*;

@Entity
public class CareLocation {

    @Id
    private Long id;

    private String naziv;

    private String lokacija;

    public CareLocation() {}

    public CareLocation(Long id, String naziv, String lokacija) {
        this.id = id;
        this.naziv = naziv;
        this.lokacija = lokacija;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }

    public String getLokacija() { return lokacija; }
    public void setLokacija(String lokacija) { this.lokacija = lokacija; }
}
