package faang.school.projectservice.controller.moment;

import faang.school.projectservice.dto.moment.MomentDto;
import faang.school.projectservice.dto.moment.MomentFilterDto;
import faang.school.projectservice.mapper.moment.MomentMapper;
import faang.school.projectservice.model.Moment;
import faang.school.projectservice.service.moment.MomentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
public class MomentController {

    private final MomentService momentService;
    private final MomentMapper momentMapper;

    @PostMapping("moment/create/{userId}/{projectId}")
    public ResponseEntity<MomentDto> createMoment(
            @PathVariable long userId,
            @PathVariable long projectId,
            @Valid @RequestBody MomentDto momentDtoRequest) {

        Moment momentRequest = momentMapper.toEntity(momentDtoRequest);

        Moment momentResponse = momentService.createMoment(userId, projectId, momentRequest, momentDtoRequest.getProjectIds());

        MomentDto momentDtoResponse = momentMapper.toDto(momentResponse);

        return ResponseEntity.ok(momentDtoResponse);
    }

    @PatchMapping("moment/update/{userId}/{momentId}")
    public ResponseEntity<MomentDto> updateMoment(
            @PathVariable long userId,
            @PathVariable long momentId,
            @Valid @RequestBody MomentDto momentDtoRequest) {

        Moment momentRequest = momentMapper.toEntity(momentDtoRequest);

        Moment updatedMoment = momentService.updateMoment(userId, momentId, momentRequest, momentDtoRequest.getProjectIds());

        MomentDto momentDtoResponse = momentMapper.toDto(updatedMoment);

        return ResponseEntity.ok(momentDtoResponse);
    }

    @PostMapping("moments/{projectId}")
    public ResponseEntity<List<MomentDto>> getAllMomentsByProjectId(
            @PathVariable long projectId,
            @RequestBody(required = false) MomentFilterDto filters) {

        List<Moment> moments = momentService.getAllMomentsByProjectId(projectId, filters);

        List<MomentDto> momentsDto = momentMapper.toDto(moments);

        return ResponseEntity.ok(momentsDto);
    }

    @GetMapping("/moment/{momentId}")
    public ResponseEntity<MomentDto> getMomentById(@PathVariable long momentId) {

        Moment moment = momentService.getMomentById(momentId);

        MomentDto momentDto = momentMapper.toDto(moment);

        return ResponseEntity.ok(momentDto);
    }
}
