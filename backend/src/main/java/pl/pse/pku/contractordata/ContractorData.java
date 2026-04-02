package pl.pse.pku.contractordata;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contractor_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_user_id", nullable = false, unique = true, length = 36)
    private String keycloakUserId;

    @Column(name = "skrot_kontrahenta", nullable = false, length = 16)
    private String contractorAbbreviation;

    @Column(name = "nazwa_pelna_kontrahenta", nullable = false, length = 100)
    private String contractorFullName;

    @Column(name = "nazwa_skrocona_kontrahenta", nullable = false, length = 100)
    private String contractorShortName;

    @Column(name = "krs", length = 100)
    private String krs;

    @Column(name = "nip", length = 100)
    private String nip;

    @Column(name = "adres_siedziby", length = 100)
    private String registeredAddress;

    @Column(name = "kod_kontrahenta", length = 100)
    private String contractorCode;

    @Column(name = "numer_umowy", length = 100)
    private String agreementNumber;

    @Column(name = "data_umowy_od")
    private LocalDate agreementDateFrom;

    @Column(name = "data_umowy_do")
    private LocalDate agreementDateTo;
}
