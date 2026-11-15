package gym.services.helpers;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks users with the same first and last name and generates a unique username for each user
 * by adding a number to the end of the username.
 */
@Component
public class UniqueUsernameGenerator {
   private final ConcurrentHashMap<String, AtomicLong> duplicatesTracker;

   public UniqueUsernameGenerator(){
       duplicatesTracker = new ConcurrentHashMap<>();
   }

   public String generateUniqueUserName(String firstName, String lastName){
       String baseUserName = firstName + "_" + lastName;
       var count = duplicatesTracker
               .computeIfAbsent(baseUserName, k -> new AtomicLong(0))
               .getAndIncrement();

       return count > 0 ? baseUserName + "_" + count : baseUserName;
   }
}
