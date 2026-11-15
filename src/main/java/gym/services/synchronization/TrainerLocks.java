package gym.services.synchronization;

import org.springframework.stereotype.Component;

@Component
public class TrainerLocks extends BaseKeyedLocks<Long> { }
