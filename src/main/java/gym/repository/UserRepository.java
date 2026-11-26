package gym.repository;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final SessionFactory sessionFactory;

    public UserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Optional<Integer> findMaxUsernameSuffix(String baseUsername) {
        Integer maxSuffix = sessionFactory.getCurrentSession()
                .createSelectionQuery(
                        "select max(u.usernameSuffix) from UserEntity u where u.baseUsername = :baseUsername",
                        Integer.class)
                .setParameter("baseUsername", baseUsername)
                .getSingleResult();
        return Optional.ofNullable(maxSuffix);
    }
}