package rs.ac.uns.ftn.informatika.jpa.dto;

import java.util.Set;

public class CreateChatDTO {
    private String name;
    private Set<Long> participantIds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(Set<Long> participantIds) {
        this.participantIds = participantIds;
    }
}
