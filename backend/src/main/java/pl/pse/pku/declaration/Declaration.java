package pl.pse.pku.declaration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.pse.pku.declarationtype.DeclarationType;

@Entity
@Table(name = "declarations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Declaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "declaration_number", nullable = false, unique = true)
    private String declarationNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeclarationStatus status;

    @Column(name = "keycloak_user_id", nullable = false, length = 36)
    private String keycloakUserId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "declaration_type_id", nullable = false)
    private DeclarationType declarationType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "field_values", columnDefinition = "text")
    private Map<String, String> fieldValues = new HashMap<>();

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "schedule_day")
    private Integer scheduleDay;
}
