package rs.ac.uns.ftn.informatika.jpa.mapper;

import rs.ac.uns.ftn.informatika.jpa.dto.PostDetailDTO;
import rs.ac.uns.ftn.informatika.jpa.model.Post;

public class PostMapper {
    public static PostDetailDTO toPostDetailDTO(Post post) {
        PostDetailDTO dto = new PostDetailDTO();
        dto.setId(post.getId());
        dto.setDescription(post.getDescription());
        dto.setCreatedDate(post.getCreatedTime());
        dto.setLikes(post.getLikesCount());
        if (post.getUser() != null) {
            dto.setUserId(post.getUser().getId());
            dto.setUsername(post.getUser().getUsername());
        }
        if (post.getImage() != null) {
            dto.setImagePath(post.getImage().getPath());
        }
        if (post.getLocation() != null) {
            dto.setLatitude(post.getLocation().getLatitude());
            dto.setLongitude(post.getLocation().getLongitude());
            dto.setAddress(post.getLocation().getAddress());
        }
        return dto;
    }
}