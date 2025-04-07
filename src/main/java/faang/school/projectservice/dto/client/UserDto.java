package faang.school.projectservice.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto{
    private Long id;
    private String username;
    private String email;
}


