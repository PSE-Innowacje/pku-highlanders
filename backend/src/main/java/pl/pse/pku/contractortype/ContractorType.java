package pl.pse.pku.contractortype;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.pse.pku.declarationtype.DeclarationType;

@Entity
@Table(name = "contractor_types")
@Getter
@Setter
@NoArgsConstructor
public class ContractorType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String symbol;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private boolean system;

    @ManyToMany
    @JoinTable(
        name = "contractor_type_declaration_types",
        joinColumns = @JoinColumn(name = "contractor_type_id"),
        inverseJoinColumns = @JoinColumn(name = "declaration_type_id")
    )
    private List<DeclarationType> declarationTypes = new ArrayList<>();

    public ContractorType(Long id, String symbol, String name, boolean system) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.system = system;
    }
}
