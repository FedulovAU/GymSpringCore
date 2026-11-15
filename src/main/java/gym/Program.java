package gym;

import gym.configuration.AppConfig;
import gym.data.storage.MemStorage;
import gym.data.storage.MemStorageSnapshot;
import gym.services.TraineeService;
import gym.services.TrainerService;
import gym.services.TrainingService;
import gym.services.dto.enums.TrainingType;
import gym.services.dto.requests.CreateTrainee;
import gym.services.dto.requests.CreateTrainer;
import gym.services.dto.requests.ScheduleTraining;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Program {

    static void main(String[] args){
        try(var context = new AnnotationConfigApplicationContext(AppConfig.class)){
        }
    }
}
