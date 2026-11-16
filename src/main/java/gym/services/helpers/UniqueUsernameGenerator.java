package gym.services.helpers;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public class UniqueUsernameGenerator {
   private final ConcurrentHashMap<String, AtomicLong> duplicatesTracker;

   public UniqueUsernameGenerator(){
       this(List.of());
   }


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
