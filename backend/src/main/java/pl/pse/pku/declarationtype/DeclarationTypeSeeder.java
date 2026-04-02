package pl.pse.pku.declarationtype;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class DeclarationTypeSeeder implements ApplicationRunner {

    private final DeclarationTypeRepository repository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) return;

        seedOPa();
        seedOZEb();
        seedOZEc();
        seedOZEd();
        seedOKOe();
        seedOKOf();
        seedOMg();
        seedOMh();
        seedOJi();
        seedOJj();
        seedOJk();
        seedORl();
        seedOZSm();
        seedODOn();
        seedOUPo();

        log.info("Seeded 15 declaration types");
    }

    private void seed(String code, String name, String contractors, boolean hasComment, String[][] fields) {
        var dt = new DeclarationType();
        dt.setCode(code);
        dt.setName(name);
        dt.setContractorTypes(contractors);
        dt.setHasComment(hasComment);
        for (String[] f : fields) {
            var field = new DeclarationTypeField();
            field.setDeclarationType(dt);
            field.setPosition(f[0]);
            field.setFieldCode(f[1]);
            field.setDataType(f[2]);
            field.setFieldName(f[3]);
            field.setRequired("TAK".equals(f[4]));
            field.setUnit(f[5]);
            dt.getFields().add(field);
        }
        repository.save(dt);
    }

    private void seedOPa() {
        seed("OP.a", "Opłata przejściowa", "OSDp / OSDn", true, new String[][] {
            {"1", "IGDSUM", "Number", "Liczba odbiorców końcowych w gospodarstwach domowych (suma 1.1–1.3)", "TAK", "szt"},
            {"1.1", "IGD1i", "Number", "Zużywający < 500 kWh rocznie", "TAK", "szt"},
            {"1.2", "IGD2i", "Number", "Zużywający 500–1200 kWh rocznie", "TAK", "szt"},
            {"1.3", "IGD3i", "Number", "Zużywający > 1200 kWh rocznie", "TAK", "szt"},
            {"2", "OPSUM", "Number(9,3)", "Suma mocy umownych odbiorców końcowych (suma 2.1–2.4)", "TAK", "kW"},
            {"2.1", "PnNi", "Number(9,3)", "Przyłączeni do sieci nN kontrahenta", "TAK", "kW"},
            {"2.2", "PSNi", "Number(9,3)", "Przyłączeni do sieci SN kontrahenta", "TAK", "kW"},
            {"2.3", "PWN", "Number(9,3)", "Przyłączeni do sieci WN/NN kontrahenta", "TAK", "kW"},
            {"2.4", "Posi", "Number(9,3)", "Odbiorcy ≥ 400 GWh, ≥ 60% mocy umownej, koszt EE ≥ 15% produkcji", "TAK", "kW"},
        });
    }

    private void seedOZEb() {
        seed("OZE.b", "Opłata OZE", "OSDn / OSDp / OSDp + OK", true, new String[][] {
            {"1", "OZESUM", "Number(12,2)", "Wielkość środków z tytułu opłaty OZE (1.1 − 1.2)", "TAK", "zł"},
            {"1.1", "OZEN", "Number(12,2)", "Wielkość należnych środków z tytułu opłaty OZE", "TAK", "zł"},
            {"1.2", "OZEPN", "Number(12,2)", "Wierzytelności nieściągalne z poprzednich okresów", "TAK", "zł"},
            {"2", "OZEE", "Number(9,3)", "Ilość energii — podstawa naliczania opłaty OZE", "TAK", "MWh"},
        });
    }

    private void seedOZEc() {
        seed("OZE.c", "Opłata OZE", "Odbiorcy końcowi / Magazyny", true, new String[][] {
            {"1", "OZEil", "Number(9,3)", "Ilość energii — podstawa naliczania opłaty OZE", "TAK", "MWh"},
        });
    }

    private void seedOZEd() {
        seed("OZE.d", "Opłata OZE", "Wytwórcy", true, new String[][] {
            {"1", "OZEil", "Number(9,3)", "Planowana ilość energii — podstawa naliczania opłaty OZE", "TAK", "MWh"},
        });
    }

    private void seedOKOe() {
        seed("OKO.e", "Opłata kogeneracyjna", "OSDn / OSDp / OSDp + OK", true, new String[][] {
            {"1", "OKOSUM", "Number(12,2)", "Wielkość środków z tytułu opłaty kogeneracyjnej (1.1 − 1.2)", "TAK", "zł"},
            {"1.1", "OKON", "Number(12,2)", "Wielkość należnych środków", "TAK", "zł"},
            {"1.2", "OKOPN", "Number(12,2)", "Wierzytelności nieściągalne z poprzednich okresów", "TAK", "zł"},
            {"1.3", "OKOO", "Number(12,2)", "Wielkość pobranych środków", "TAK", "zł"},
            {"2", "OKOE", "Number(9,3)", "Ilość energii — podstawa naliczania opłaty kogeneracyjnej", "TAK", "MWh"},
        });
    }

    private void seedOKOf() {
        seed("OKO.f", "Opłata kogeneracyjna", "Odbiorcy końcowi / Wytwórcy / Magazyny", true, new String[][] {
            {"1", "OKOE", "Number(9,3)", "Ilość energii — podstawa naliczania opłaty kogeneracyjnej", "TAK", "MWh"},
        });
    }

    private void seedOMg() {
        seed("OM.g", "Opłata mocowa", "OSDn / OSDp / OSDp + OK / Wytwórcy / Magazyny", true, new String[][] {
            {"1", "WSNOM", "Number(12,2)", "Wielkość środków z tytułu opłaty mocowej (1.1 − 1.2)", "TAK", "zł"},
            {"1.1", "WSNM", "Number(12,2)", "Wielkość należnych środków", "TAK", "zł"},
            {"1.2", "NOM", "Number(12,2)", "Wierzytelności nieściągalne z poprzednich okresów", "TAK", "zł"},
            {"2", "LOKsuma", "Number", "Liczba odbiorców końcowych (art. 89a ust. 1 pkt 1) (suma 2.1–2.4)", "TAK", "szt"},
            {"2.1", "LOKm1", "Number", "Zużywający < 500 kWh rocznie", "TAK", "szt"},
            {"2.2", "LOKm2", "Number", "Zużywający 500–1200 kWh rocznie", "TAK", "szt"},
            {"2.3", "LOKm3", "Number", "Zużywający 1200–2800 kWh rocznie", "TAK", "szt"},
            {"2.4", "LOKm4", "Number", "Zużywający > 2800 kWh rocznie", "TAK", "szt"},
            {"3", "WEM", "Number(9,3)", "Wolumen energii — podstawa naliczania opłaty mocowej", "TAK", "MWh"},
            {"4", "WEnM", "Number(9,3)", "Wolumen energii po uwzględnieniu współczynników (art. 70a ust. 5)", "TAK", "MWh"},
        });
    }

    private void seedOMh() {
        seed("OM.h", "Opłata mocowa", "Odbiorcy końcowi", true, new String[][] {
            {"1", "WEM", "Number(9,3)", "Wolumen energii pobranej z sieci w godzinach opublikowanych (art. 74 ust. 4 pkt 2)", "TAK", "MWh"},
        });
    }

    private void seedOJi() {
        seed("OJ.i", "Opłata jakościowa", "OSDp", true, new String[][] {
            {"1", "OSDpSUM", "Number(9,3)", "Ilość EE dla odbiorców będących OSD (suma 1.1 + 1.2)", "TAK", "MWh"},
            {"1.1", "EosiSUM", "Number(9,3)", "EE zużyta przez odbiorców specjalnych (suma 1.1.1 + 1.1.2)", "TAK", "MWh"},
            {"1.1.1", "Eosi'", "Number(9,3)", "Odbiorcy specjalni przyłączeni do sieci OSDp", "TAK", "MWh"},
            {"1.1.2", "Eososd", "Number(9,3)", "Odbiorcy specjalni przyłączeni do sieci OSD bez miejsc dostarczania z sieci przesyłowej", "TAK", "MWh"},
            {"1.2", "EokiSUM1", "Number(9,3)", "Odbiorcy końcowi inni niż specjalni (suma 1.2.1–1.2.7)", "TAK", "MWh"},
            {"1.2.1", "Eoki'", "Number(9,3)", "Odbiorcy końcowi korzystający z KSE, przyłączeni do sieci z usługami przesyłania OSP", "TAK", "MWh"},
            {"1.2.2", "Eokoi", "Number(9,3)", "Odbiorcy końcowi przyłączeni do sieci OSDp", "TAK", "MWh"},
            {"1.2.3", "Eokui", "Number(9,3)", "EE zakupiona od osób trzecich na własny użytek przez przedsiębiorstwa przesyłowe/dystrybucyjne", "TAK", "MWh"},
            {"1.2.4", "Eokni", "Number(9,3)", "Odbiorcy przyłączeni do sieci przedsiębiorstw niebędących operatorem", "TAK", "MWh"},
            {"1.2.5", "Eokgi", "Number(9,3)", "Odbiorcy przyłączeni do sieci/urządzeń wytwórcy przyłączonego do OSDp", "TAK", "MWh"},
            {"1.2.6", "Eokmi", "Number(9,3)", "Odbiorcy przyłączeni do urządzeń magazynowania EE", "TAK", "MWh"},
            {"1.2.7", "EosdiSUM", "Number(9,3)", "Ilość EE Eosdi (suma 1.2.7.1–1.2.7.6)", "TAK", "MWh"},
            {"1.2.7.1", "Eosdki", "Number(9,3)", "Odbiorcy końcowi przyłączeni do sieci OSD bez miejsc dostarczania z sieci przesyłowej", "TAK", "MWh"},
            {"1.2.7.2", "Eosdui", "Number(9,3)", "EE zakupiona od osób trzecich na własny użytek (OSD bez miejsc dostarczania)", "TAK", "MWh"},
            {"1.2.7.3", "Eosdni", "Number(9,3)", "Odbiorcy przyłączeni do sieci przedsiębiorstw niebędących operatorem (OSD bez miejsc dostarczania)", "TAK", "MWh"},
            {"1.2.7.4", "Eosdgi", "Number(9,3)", "Odbiorcy przyłączeni do urządzeń wytwórcy (OSD bez miejsc dostarczania)", "TAK", "MWh"},
            {"1.2.7.5", "Eosdmi", "Number(9,3)", "Odbiorcy przyłączeni do urządzeń magazynowania (OSD bez miejsc dostarczania)", "TAK", "MWh"},
            {"1.2.7.6", "Eosdoi", "Number(9,3)", "EE na własny użytek OSDp bez miejsc dostarczania", "TAK", "MWh"},
        });
    }

    private void seedOJj() {
        seed("OJ.j", "Opłata jakościowa", "Odbiorcy końcowi", true, new String[][] {
            {"1", "OKSUM", "Number(9,3)", "Ilość EE do naliczania opłaty jakościowej (poz. 2 + poz. 3 − poz. 4)", "TAK", "MWh"},
            {"2", "Eok'", "Number(9,3)", "EE zakupiona od osób trzecich i zużyta na własne potrzeby", "TAK", "MWh"},
            {"3", "Eok''", "Number(9,3)", "EE zużyta przez odbiorców przyłączonych do sieci odbiorcy", "TAK", "MWh"},
            {"4", "Eosd", "Number(9,3)", "EE, za którą opłatę wnosi odbiorca do OSD w ramach umów dystrybucyjnych", "TAK", "MWh"},
            {"5", "PKN OK", "Number(9,3)", "EE pobrana z sieci przesyłowej w MD Płock i zużyta przez PKN ORLEN", "TAK", "MWh"},
        });
    }

    private void seedOJk() {
        seed("OJ.k", "Opłata jakościowa", "Wytwórcy / Magazyn / Wytwórca + Magazyn", true, new String[][] {
            {"1", "WYTos", "Number(9,3)", "EE zużyta przez odbiorców specjalnych przyłączonych do sieci/urządzeń wytwórcy", "TAK", "MWh"},
            {"2", "WYTok", "Number(9,3)", "EE zużyta przez odbiorców końcowych przyłączonych do sieci/urządzeń wytwórcy (inni niż specjalni)", "TAK", "MWh"},
            {"3", "Eorlwytos", "Number(9,3)", "EE zużyta przez odbiorców specjalnych przyłączonych do sieci ORLEN jako wytwórcy", "TAK", "MWh"},
            {"4", "ORLSUM", "Number(9,3)", "Suma EE zużytej przez ORLEN + odbiorców końcowych (suma 4.1 + 4.2)", "TAK", "MWh"},
            {"5", "Eanwwyt", "Number(9,3)", "EE zużyta przez ANWIL S.A. jako odbiorcę przyłączonego do ORLEN jako wytwórcy", "TAK", "MWh"},
            {"6", "Eorlwytok", "Number(9,3)", "EE zużyta przez odbiorców końcowych innych niż ANWIL S.A.", "TAK", "MWh"},
        });
    }

    private void seedORl() {
        seed("OR.l", "Opłata rynkowa", "Wszyscy kontrahenci", true, new String[][] {
            {"1", "Ewpi", "Number(9,3)", "Ilość EE przeznaczonej do wymiany między KSE a systemami państw spoza UE", "TAK", "MWh"},
        });
    }

    private void seedOZSm() {
        seed("OZS.m", "Opłata zmienna sieciowa — wyjątki", "PGE / Energoserwis Kleszczów / PKN ORLEN", true, new String[][] {
            {"1", "Ebel", "Number(9,3)", "EE zmierzona w polach liniowych 110kV Rogowiec Stary i Chabielice podczas postoju bloku 12 Bełchatów", "TAK", "MWh"},
            {"2", "Epi", "Number(9,3)", "EE pobrana przez Użytkownika podczas postoju bloku 12 Bełchatów", "TAK", "MWh"},
            {"3", "PKNzm", "Number(9,3)", "EE pobrana w MD Płock, przepłynęła FPP nr 5 i 6, niezużyta na potrzeby ogólne bloku G1", "TAK", "MWh"},
        });
    }

    private void seedODOn() {
        seed("ODO.n", "Opłata dodatkowa", "PGE", true, new String[][] {
            {"1", "Epol", "Number(9,3)", "EE zmierzona po stronie 110kV transformatorów TR1 i TR2 w stacji Połaniec", "TAK", "MWh"},
        });
    }

    private void seedOUPo() {
        seed("OUP.o", "Opłata za usługi przesyłania", "Instytut Energetyki", true, new String[][] {
            {"1", "Ih", "Number(9,3)", "Rzeczywista liczba godzin prób ATR3 w m-tym miesiącu", "TAK", "h"},
        });
    }
}
