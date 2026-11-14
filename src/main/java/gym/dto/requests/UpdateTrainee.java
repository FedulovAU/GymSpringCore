package gym.dto.requests;

import java.time.LocalDate;

/**
 * Represents a request to update an existing trainee's information.
 * @param id, mandatory, the unique identifier of the trainee to be updated.
 * @param firstName, optional. If null, the first name will not be updated.
 * @param lastName, optional. If null, the last name will not be updated.
 * @param password, optional. If null, the password will not be updated.
 * @param birthDate, optional. If null, the birth date will not be updated.
 */
public record UpdateTrainee(
        long id,
        String firstName,
        String lastName,
        String password,
        LocalDate birthDate) {
}
