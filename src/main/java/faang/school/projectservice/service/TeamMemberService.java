package faang.school.projectservice.service;

import faang.school.projectservice.model.TeamMember;
import faang.school.projectservice.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;

    public Optional<TeamMember> getTeamMemberByUserIdAndProjectId(long userId, long projectId) {
        return Optional.ofNullable(teamMemberRepository.findByUserIdAndProjectId(userId, projectId));
    }

}
