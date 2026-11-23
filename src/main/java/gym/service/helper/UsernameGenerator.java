package gym.service.helper;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsernameGenerator {
    public Result generate(String firstName, String lastName, Optional<Long> existedSuffix) {
        String userName;
        long suffix;

        if(existedSuffix.isEmpty()){
            userName = firstName + "_" + lastName;
            suffix = 0L;
        } else{
            suffix = existedSuffix.get() + 1;
            userName = firstName + "_" + lastName + "_" + suffix;
        }

        return new Result(userName, suffix);
    }
    public record Result(String username, long suffix) {}
}
