package pl.pse.pku.contractortype;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contractor_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
}
