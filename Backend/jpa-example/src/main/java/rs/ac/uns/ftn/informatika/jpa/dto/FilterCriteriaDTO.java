package rs.ac.uns.ftn.informatika.jpa.dto;

public class FilterCriteriaDTO {
    private Long adminId;
    private String name;
    private String surname;
    private String email;
    private Integer minPosts;
    private Integer maxPosts;
    private String sortField;
    private String sortOrder;
    private int page = 0;
    private int size = 5;

    public FilterCriteriaDTO(Long adminId,String name, String surname, String email, Integer minPosts, Integer maxPosts, String sortField,String sortOrder) {
        this.adminId = adminId;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.minPosts = minPosts;
        this.maxPosts = maxPosts;
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }
    public Long getAdminId() {
        return adminId;
    }
    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public Integer getMinPosts() {
        return minPosts;
    }
    public void setMinPosts(Integer minPosts) {
        this.minPosts = minPosts;
    }
    public Integer getMaxPosts() {
        return maxPosts;
    }
    public void setMaxPosts(Integer maxPosts) {
        this.maxPosts = maxPosts;
    }
    public String getSortField() {
        return sortField;
    }
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }
    public String getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
