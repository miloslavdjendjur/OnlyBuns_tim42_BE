// src/main/java/rs/ac/uns/ftn/informatika/jpa/dto/AdEligibleRequest.java
package rs.ac.uns.ftn.informatika.jpa.dto;

public class AdEligibleRequest {

    private boolean eligible;

    public AdEligibleRequest() { }

    public AdEligibleRequest(boolean eligible) {
        this.eligible = eligible;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    @Override
    public String toString() {
        return "AdEligibleRequest{eligible=" + eligible + '}';
    }
}
