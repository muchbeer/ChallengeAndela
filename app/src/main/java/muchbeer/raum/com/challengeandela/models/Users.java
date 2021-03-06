package muchbeer.raum.com.challengeandela.models;

public class Users {

    private String name;
    private String phone;
    private String username;
    private String profile_image;
    private String user_id;
    private String security_level;
    private String messaging_token;
    private String department;


    public Users(String name, String phone, String profile_image, String user_id,
                 String security_level,  String messaging_token, String department) {
        this.name = name;
        this.phone = phone;
        this.profile_image = profile_image;
        this.user_id = user_id;
        this.security_level = security_level;
        this.messaging_token = messaging_token;
        this.department = department;
    }

    public Users(String name, String phone, String username) {
        this.name = name;
        this.phone = phone;
        this.username = username;
    }

    public Users() {

    }

    public String getSecurity_level() {
        return security_level;
    }

    public void setSecurity_level(String security_level) {
        this.security_level = security_level;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

   public String getMessaging_token() {
        return messaging_token;
    }

    public void setMessaging_token(String messaging_token) { this.messaging_token = messaging_token;  }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getUsername() { return username;  }

    public void setUsername(String username) { this.username = username;   }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", profile_image='" + profile_image + '\'' +
                ", user_id='" + user_id + '\'' +
                ", security_level='" + security_level + '\'' +
                ", messaging_token='" + messaging_token + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
