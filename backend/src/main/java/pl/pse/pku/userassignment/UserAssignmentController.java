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

    @PutMapping("/{keycloakUserId}/contractor-types")
    public KontrahentUserWithTypesDto updateAssignments(
            @PathVariable String keycloakUserId,
            @RequestBody UpdateAssignmentsRequest request) {
        return service.updateAssignments(keycloakUserId, request.contractorTypeIds());
    }

    @PutMapping("/{keycloakUserId}/agreement-number")
    public void updateAgreementNumber(
            @PathVariable String keycloakUserId,
            @RequestBody UpdateAgreementNumberRequest request) {
        service.updateAgreementNumber(keycloakUserId, request.agreementNumber());
    }
}
