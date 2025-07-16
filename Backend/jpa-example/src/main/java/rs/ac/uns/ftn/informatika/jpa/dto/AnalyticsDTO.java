package rs.ac.uns.ftn.informatika.jpa.dto;

public class AnalyticsDTO {
    // Broj objava i komentara
    private long weeklyPosts;
    private long monthlyPosts;
    private long yearlyPosts;

    private long weeklyComments;
    private long monthlyComments;
    private long yearlyComments;

    // Procenti korisnika za grafik
    private double percentUsersWithPosts;      // Korisnici koji su napravili objavu
    private double percentUsersWithCommentsOnly; // Korisnici koji su napravili samo komentare
    private double percentInactiveUsers;        // Korisnici koji nisu napravili ništa

    // Dodatni podaci
    private long totalUsers;
    private long dailyActiveUsers;
    private double avgSessionTime;
    private long newPostsToday;
    private double engagementRate;

    public AnalyticsDTO() {}

    public long getWeeklyPosts() {
        return weeklyPosts;
    }

    public void setWeeklyPosts(long weeklyPosts) {
        this.weeklyPosts = weeklyPosts;
    }

    public long getMonthlyPosts() {
        return monthlyPosts;
    }

    public void setMonthlyPosts(long monthlyPosts) {
        this.monthlyPosts = monthlyPosts;
    }

    public long getYearlyPosts() {
        return yearlyPosts;
    }

    public void setYearlyPosts(long yearlyPosts) {
        this.yearlyPosts = yearlyPosts;
    }

    public long getWeeklyComments() {
        return weeklyComments;
    }

    public void setWeeklyComments(long weeklyComments) {
        this.weeklyComments = weeklyComments;
    }

    public long getMonthlyComments() {
        return monthlyComments;
    }

    public void setMonthlyComments(long monthlyComments) {
        this.monthlyComments = monthlyComments;
    }

    public long getYearlyComments() {
        return yearlyComments;
    }

    public void setYearlyComments(long yearlyComments) {
        this.yearlyComments = yearlyComments;
    }

    public double getPercentUsersWithPosts() {
        return percentUsersWithPosts;
    }

    public void setPercentUsersWithPosts(double percentUsersWithPosts) {
        this.percentUsersWithPosts = percentUsersWithPosts;
    }

    public double getPercentUsersWithCommentsOnly() {
        return percentUsersWithCommentsOnly;
    }

    public void setPercentUsersWithCommentsOnly(double percentUsersWithCommentsOnly) {
        this.percentUsersWithCommentsOnly = percentUsersWithCommentsOnly;
    }

    public double getPercentInactiveUsers() {
        return percentInactiveUsers;
    }

    public void setPercentInactiveUsers(double percentInactiveUsers) {
        this.percentInactiveUsers = percentInactiveUsers;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getDailyActiveUsers() {
        return dailyActiveUsers;
    }

    public void setDailyActiveUsers(long dailyActiveUsers) {
        this.dailyActiveUsers = dailyActiveUsers;
    }

    public double getAvgSessionTime() {
        return avgSessionTime;
    }

    public void setAvgSessionTime(double avgSessionTime) {
        this.avgSessionTime = avgSessionTime;
    }

    public long getNewPostsToday() {
        return newPostsToday;
    }

    public void setNewPostsToday(long newPostsToday) {
        this.newPostsToday = newPostsToday;
    }

    public double getEngagementRate() {
        return engagementRate;
    }

    public void setEngagementRate(double engagementRate) {
        this.engagementRate = engagementRate;
    }
}