package gym.services.helpers;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks users with the same first and last name and generates a unique username for each user
 * by adding a number to the end of the username: John_Smith, John_Smith_1, John_Smith_2, ...
 * Created as a bean in {@link gym.configuration.AppConfig}, seeded with the usernames already in storage.
 */
public class UniqueUsernameGenerator {
   // Base username (first_last) -> the next suffix to use, 0 means no suffix.
   private final ConcurrentHashMap<String, AtomicLong> duplicatesTracker;

   public UniqueUsernameGenerator(){
       this(List.of());
   }

   /**
    * @param existingUserNames usernames already taken, e.g. loaded from a snapshot.
    *                          New usernames continue after the highest suffix in use for each name,
    *                          so a deleted user's username is never handed out again.
    */
   public UniqueUsernameGenerator(Collection<String> existingUserNames){
       duplicatesTracker = new ConcurrentHashMap<>();
       existingUserNames.forEach(this::register);
   }

   public String generateUniqueUserName(String firstName, String lastName){
       String baseUserName = firstName + "_" + lastName;
       var count = duplicatesTracker
               .computeIfAbsent(baseUserName, k -> new AtomicLong(0))
               .getAndIncrement();

       return count > 0 ? baseUserName + "_" + count : baseUserName;
   }

   /**
    * Marks a username as taken. Names contain letters only, so underscores are always separators:
    * "John_Smith" is the base username, "John_Smith_2" is the base username with suffix 2.
    */
   private void register(String userName){
       String[] parts = userName.split("_");
       String baseUserName;
       long nextSuffix;
       if (parts.length == 3 && parts[2].matches("\\d+")) {
           baseUserName = parts[0] + "_" + parts[1];
           nextSuffix = Long.parseLong(parts[2]) + 1;
       } else {
           baseUserName = userName;
           nextSuffix = 1;
       }
       duplicatesTracker
               .computeIfAbsent(baseUserName, k -> new AtomicLong(0))
               .accumulateAndGet(nextSuffix, Math::max);
   }
}
