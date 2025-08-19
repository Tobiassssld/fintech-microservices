package com.example.fintech.common.context;

public class UserContext {
    private static final ThreadLocal<UserInfo> userHolder = new ThreadLocal<>();

    public static void setUser(UserInfo userInfo) {
        userHolder.set(userInfo);
    }

    public static UserInfo getUser() {
        return userHolder.get();
    }

    public static void clear() {
        userHolder.remove();
    }

    public static class UserInfo {
        private final Long userId;
        private final String username;
        private final String roles;

        public UserInfo(Long userId, String username, String roles) {
            this.userId = userId;
            this.username = username;
            this.roles = roles;
        }

        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRoles() { return roles; }
    }
}