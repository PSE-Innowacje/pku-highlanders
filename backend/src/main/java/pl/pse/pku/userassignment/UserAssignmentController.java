package pl.pse.pku.userassignment;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/kontrahent-users")
@RequiredArgsConstructor
public class UserAssignmentController {

    private final UserAssignmentService service;

    @GetMapping
    public List<KontrahentUserWithTypesDto> listKontrahentUsers() {
        return service.listKontrahentUsersWithTypes();
    }

    @PutMapping("/{keycloakUserId}/contractor-type")
    public KontrahentUserWithTypesDto updateAssignment(
            @PathVariable String keycloakUserId,
            @RequestBody UpdateAssignmentsRequest request) {
        return service.updateAssignment(keycloakUserId, request.contractorTypeId());
    }

    @PutMapping("/{keycloakUserId}/agreement-number")
    public void updateAgreementNumber(
            @PathVariable String keycloakUserId,
            @RequestBody UpdateAgreementNumberRequest request) {
        service.updateAgreementNumber(keycloakUserId, request.agreementNumber());
    }
}
