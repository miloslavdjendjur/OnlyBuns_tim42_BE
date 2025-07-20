package rs.ac.uns.ftn.informatika.jpa.dto;

import rs.ac.uns.ftn.informatika.jpa.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserProfileFullDTO {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String address;

    private int followersCount;
    //private List<String> followersUsernames;
    //private List<String> followingUsernames;

    private List<PostDetailDTO> userPosts;
    private boolean editable;

    public UserProfileFullDTO(User user, List<PostDetailDTO> userPosts) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.address = user.getAddress();
        this.followersCount = user.getNumberOfPeopleFollowing();
        //this.followersUsernames = user.getFollowers().stream()
                //.map(User::getUsername)
                //.collect(Collectors.toList());
        //this.followingUsernames = user.getFollowing().stream()
                //.map(User::getUsername)
                //.collect(Collectors.toList());
        this.userPosts = userPosts;
        this.editable = false;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getAddress() { return address; }
    public int getFollowersCount() { return followersCount; }
    /*public List<String> getFollowersUsernames() { return followersUsernames; }
    public List<String> getFollowingUsernames() { return followingUsernames; }*/
    public List<PostDetailDTO> getUserPosts() { return userPosts; }
    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }
}
