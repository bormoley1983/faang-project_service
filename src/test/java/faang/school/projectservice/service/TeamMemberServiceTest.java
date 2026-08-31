package faang.school.projectservice.service;

import faang.school.projectservice.model.TeamMember;
import faang.school.projectservice.repository.TeamMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TeamMemberService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeamMemberService")
class TeamMemberServiceTest {

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private TeamMemberService teamMemberService;

    @Test
    @DisplayName("returns Optional.of when team member exists")
    void returnsPresentWhenFound() {
        TeamMember member = new TeamMember();
        when(teamMemberRepository.findByUserIdAndProjectId(1L, 2L)).thenReturn(member);

        Optional<TeamMember> result = teamMemberService.getTeamMemberByUserIdAndProjectId(1L, 2L);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(member);
    }

    @Test
    @DisplayName("returns Optional.empty when team member not found")
    void returnsEmptyWhenNotFound() {
        when(teamMemberRepository.findByUserIdAndProjectId(1L, 2L)).thenReturn(null);

        Optional<TeamMember> result = teamMemberService.getTeamMemberByUserIdAndProjectId(1L, 2L);

        assertThat(result).isEmpty();
    }
}
