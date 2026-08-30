package faang.school.projectservice.service;

import faang.school.projectservice.model.Internship;
import faang.school.projectservice.model.InternshipStatus;
import faang.school.projectservice.repository.InternshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class InternshipServiceTest {

    @Mock
    private InternshipRepository internshipRepository;

    @InjectMocks
    private InternshipService internshipService;

    private Internship internship;

    @BeforeEach
    void setUp() {
        internship = new Internship();
        internship.setId(1L);
        internship.setName("Java Internship");
        internship.setStatus(InternshipStatus.IN_PROGRESS);
    }

    @Test
    void createInternship_ShouldSucceed() {
        when(internshipRepository.save(internship)).thenReturn(internship);

        Internship result = internshipService.createInternship(internship);

        assertNotNull(result);
        assertEquals(internship.getId(), result.getId());
        assertEquals("Java Internship", result.getName());
        verify(internshipRepository, times(1)).save(internship);
    }

    @Test
    void updateInternship_ShouldSucceed() {
        Internship updatedInternship = new Internship();
        updatedInternship.setId(1L);
        updatedInternship.setStatus(InternshipStatus.COMPLETED);

        when(internshipRepository.findById(1L)).thenReturn(Optional.of(internship));
        when(internshipRepository.save(any(Internship.class))).thenReturn(updatedInternship);

        Internship result = internshipService.updateInternship(updatedInternship);

        assertNotNull(result);
        assertEquals(InternshipStatus.COMPLETED, result.getStatus());

        ArgumentCaptor<Internship> captor = ArgumentCaptor.forClass(Internship.class);
        verify(internshipRepository, times(1)).save(captor.capture());
        Internship savedInternship = captor.getValue();

        assertEquals(1L, savedInternship.getId());
        assertEquals(InternshipStatus.COMPLETED, savedInternship.getStatus());
    }

    @Test
    void updateInternship_ShouldFail_WhenInternshipNotFound() {
        when(internshipRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> internshipService.updateInternship(internship)
        );

        assertEquals("Internship not found", exception.getMessage());
        verify(internshipRepository, times(1)).findById(1L);
        verify(internshipRepository, times(0)).save(any());
    }

    @Test
    void updateInternship_ShouldFail_WhenInternshipIsCompleted() {
        internship.setStatus(InternshipStatus.COMPLETED);
        when(internshipRepository.findById(1L)).thenReturn(Optional.of(internship));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> internshipService.updateInternship(internship)
        );

        assertEquals("Cannot update a completed internship.", exception.getMessage());
        verify(internshipRepository, times(1)).findById(1L);
        verify(internshipRepository, times(0)).save(any());
    }

    @Test
    void getInternshipById_ShouldSucceed() {
        when(internshipRepository.findById(1L)).thenReturn(Optional.of(internship));

        Internship result = internshipService.getInternshipById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(internshipRepository, times(1)).findById(1L);
    }

    @Test
    void getInternshipById_ShouldFail_WhenNotFound() {
        when(internshipRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> internshipService.getInternshipById(1L)
        );

        assertEquals("Internship not found", exception.getMessage());
        verify(internshipRepository, times(1)).findById(1L);
    }

    @Test
    void getInternships_ShouldReturnFilteredResults() {
        Internship internship2 = new Internship();
        internship2.setId(2L);
        internship2.setStatus(InternshipStatus.COMPLETED);

        when(internshipRepository.search(InternshipStatus.IN_PROGRESS, null, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(internship)));

        List<Internship> activeInternships = internshipService
                .getInternships(InternshipStatus.IN_PROGRESS, null, Pageable.unpaged()).getContent();

        assertEquals(1, activeInternships.size());
        assertEquals(1L, activeInternships.get(0).getId());
        verify(internshipRepository).search(InternshipStatus.IN_PROGRESS, null, Pageable.unpaged());
    }

    @Test
    void getInternships_ShouldReturnAllResults_WhenStatusIsNull() {
        Internship internship2 = new Internship();
        internship2.setId(2L);

        when(internshipRepository.search(null, null, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(internship, internship2)));

        List<Internship> allInternships = internshipService.getInternships(null, null, Pageable.unpaged()).getContent();

        assertEquals(2, allInternships.size());
        verify(internshipRepository).search(null, null, Pageable.unpaged());
    }
}
