package pl.pse.pku.declarationtype;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "declaration_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeclarationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contractorTypes;

    @Column(nullable = false)
    private boolean hasComment;

    @OneToMany(mappedBy = "declarationType", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<DeclarationTypeField> fields = new ArrayList<>();
}
