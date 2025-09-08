package rs.ac.uns.ftn.informatika.jpa.dto;

public class PostAdEligibleResponse {

    private Long id;
    private boolean adEligible;

    public PostAdEligibleResponse() { }

    public PostAdEligibleResponse(Long id, boolean adEligible) {
        this.id = id;
        this.adEligible = adEligible;
    }

    public Long getId() {
        return id;
    }

    public boolean isAdEligible() {
        return adEligible;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAdEligible(boolean adEligible) {
        this.adEligible = adEligible;
    }

    @Override
    public String toString() {
        return "PostAdEligibleResponse{id=" + id + ", adEligible=" + adEligible + '}';
    }
}
