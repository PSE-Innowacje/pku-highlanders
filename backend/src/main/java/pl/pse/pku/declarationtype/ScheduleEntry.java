package pl.pse.pku.declarationtype;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "schedule_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declaration_type_id", nullable = false)
    private DeclarationType declarationType;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private int day;

    @Column(nullable = false)
    private int hour;

    @Column(name = "day_type", nullable = false)
    private String dayType;
}
