package gym.data.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.With;

@Getter
@With
public class UserEntity {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String userName;
    private final Long userNameSuffix;
    private final String password;
    private final boolean isActive;

    @JsonCreator
    public UserEntity(
            @JsonProperty("id") Long id,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("userName") String userName,
            @JsonProperty("userNameSuffix") Long userNameSuffix,
            @JsonProperty("password") String password,
            @JsonProperty("isActive") boolean isActive) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.userNameSuffix = userNameSuffix;
        this.password = password;
        this.isActive = isActive;
    }

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }
}
