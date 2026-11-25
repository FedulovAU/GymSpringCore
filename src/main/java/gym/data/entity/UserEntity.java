package gym.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uq_users_username", columnNames = {"username"}),
        @UniqueConstraint(name = "uq_users_base_suffix", columnNames = {"base_username", "username_suffix"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private String username;

    @Column(nullable = false, updatable = false, length = 201)
    @Setter(AccessLevel.NONE)
    private String baseUsername;

    @Column(nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private int usernameSuffix;

    public UserEntity(
            String firstName,
            String lastName,
            String password,
            boolean isActive,
            String username,
            String baseUsername,
            int usernameSuffix) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.isActive = isActive;
        this.username = username;
        this.baseUsername = baseUsername;
        this.usernameSuffix = usernameSuffix;
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UserEntity other)) {
            return false;
        }

        return username.equals(other.getUsername());
    }
}
