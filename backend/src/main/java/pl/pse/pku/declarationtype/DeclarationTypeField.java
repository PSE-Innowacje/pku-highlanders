package pl.pse.pku.declarationtype;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "declaration_type_fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationTypeField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declaration_type_id", nullable = false)
    private DeclarationType declarationType;

    @Column(nullable = false, length = 20)
    private String position;

    @Column(nullable = false, length = 20)
    private String fieldCode;

    @Column(nullable = false, length = 20)
    private String dataType;

    @Column(nullable = false, length = 500)
    private String fieldName;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false, length = 10)
    private String unit;
}
