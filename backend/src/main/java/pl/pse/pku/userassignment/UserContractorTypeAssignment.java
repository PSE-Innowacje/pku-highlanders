package pl.pse.pku.userassignment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.pse.pku.contractortype.ContractorType;

@Entity
@Table(name = "user_contractor_type_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"keycloak_user_id", "contractor_type_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserContractorTypeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_user_id", nullable = false, length = 36)
    private String keycloakUserId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contractor_type_id", nullable = false)
    private ContractorType contractorType;
}
