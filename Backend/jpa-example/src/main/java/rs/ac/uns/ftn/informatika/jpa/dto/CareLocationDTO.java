package rs.ac.uns.ftn.informatika.jpa.dto;

public class CareLocationDTO {
    private Long id;
    private String naziv;
    private String lokacija;

    public CareLocationDTO() {}

    public CareLocationDTO(Long id, String naziv, String lokacija) {
        this.id = id;
        this.naziv = naziv;
        this.lokacija = lokacija;
    }

    public Long getId() { return id; }
    public String getNaziv() { return naziv; }
    public String getLokacija() { return lokacija; }
}
