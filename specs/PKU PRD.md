# ⚡ PKU — Product Requirements Document

---

## 1. 📋 Opis ogólny

Zbudować aplikację webową do **składania oświadczeń** w ramach rozliczeń opłat przesyłowych i pozaprzesyłowych przez Wytwórców / OSD / Odbiorców końcowych, którzy mają podpisane **Umowy Przesyłowe z OSP**.

---

## 2. 🔍 Problem
- Potrzeba aplikacji www do składania oświadczeń w ramach rozliczeń opłat przesyłowych i pozaprzesyłowych dla aktywnych Umów Przesyłania.
- Potrzeba wumożliwienia Administratorowi wprowadzenia danych Użytkownikach, którzy mogą logować się do aplikacji PKU
- Potrzeba wumożliwienia Administratorowi wprowadzenia danych o wzorcach oświadczeń i przypisaniu ich do:
	- typu kontrahenta,
	- typu opłaty.
- Potrzeba umożliwienia Administratorowi wprowadzenia danych dotyczących terminarza składania oświadczeń (informacja przez ile dni po rozpoczęciu miesiąca Kontrahent może złożyć oświadczenie.
- Potrzeba prezentacji Kotrahentowi w formie listy oświadczeń do złożenia w danym miesiącu i roku wraz z prezentacją terminarza, do kiedy może złożyć to oświadczenie.
- Potrzeba umożliwienia Kontrahentowi złożenia *oświadczenia dla opłaty*
- Potrzeba umożliwienia Kontrahentowi złożenia **korekty oświadczenia**.
- Potrzeba potwierdzenia złożenia oświadczenia z informacją, czy zostało wysłane.

---

## 3. 🎯 Cel

Wsparcie procesu rozliczania opłat przesyłowych i pozaprzesyłowych dla Umów Przesyłania podpisanych z OSP.

---

## 4. 👥 Użytkownicy docelowi

###4.1 Rola Kontrahent - uprawniająca do składania oświadczeń:

| Symbol | Typ kontrahenta |
|--------|----------------|
| 🏭 **OSDp** | Operator Systemu Dystrybucyjnego przyłączony do sieci PSE |
| 🏗️ **OSDn** | Operator Systemu Dystrybucyjnego nieprzyłączony do sieci PSE |
| 🏢 **OK** | Odbiorca końcowy |
| ⚡ **Wyt** | Wytwórca |
| 🔋 **Mag** | Magazyn |

###4.2 Rola Administrator - uprawniająca do obługi menu Administracja
---

## 5. 💰 Rodzaje opłat

Oświadczenia składane w ramach Umowy Przesyłania dotyczą następujących opłat:

### 5.1. Opłaty pozaprzesyłowe

| Kod | Nazwa opłaty |
|-----|-------------|
| **OP** | Opłata przejściowa |
| **OKO** | Opłata kogeneracyjna |
| **OZE** | Opłata OZE |
| **OM** | Opłata mocowa |

### 5.2. Opłaty przesyłowe

| Kod | Nazwa opłaty |
|-----|-------------|
| **OSS** | Opłata stała sieciowa |
| **OZS** | Opłata zmienna sieciowa |
| **OJ** | Opłata jakościowa |
| **OR** | Opłata rynkowa |

---

## 6. 🏷️ Użytkownicy, który będzie się logował do aplikacji PKU

###a) Wprowadzanie i edycja danych użytkownikó
Dane Kontrahenta logującego się do aplikacji PKU, który ma Umowę Przesyłania - dla uproszczenia w danych Kontrahenta dopisałam:
- jakiego jest **typu** na podstawie listy z punktu nr 4, 
- informację o jego Umowie - numerze i czasie jej obowiązywania od/do:

| Pole | Typ | Wymagalność | Ograniczenia |
|------|-----|-------------|--------------|
| Imię | tekst | obowiązkowe tylko dla roli "Administrator"| do 100 znaków |
| Nazwisko | tekst | obowiązkowe tylko dla roli "Administrator"| do 100 znaków |
| Email / login | tekst | obowiązkowe | do 100 znaków, walidacja email |
| Rola | wybór jednokrotny | obowiązkowe | ze słownika (np. *Administrator*, *Kontrahent*) |
| Skrót kontrahenta | tekst |Obowiązkowe dla roli "Kontrahent"|od 3 do 16 znaków|
| Nazwa pełna kontrahenta | tekst |Obowiązkowe dla roli "Kontrahent"|do 100 znaków|
| Nazwa skrócona kontrahenta | tekst |Obowiązkowe dla roli "Kontrahent"|do 100 znaków|
| KRS | tekst |Obowiązkowe dla roli "Kontrahent"|do 100 znaków|
| NIP | tekst |Obowiązkowe dla roli "Kontrahent"|do 100 znaków|
| Adres siedziby | tekst |Obowiązkowe dla roli "Kontrahent"|do 100 znaków|
| Kod kontrahenta | tekst |Obowiązkowe dla roli "Kontrahent"|do 100 znaków|
| Typ kontrahenta | wybór wielokrotny |Obowiązkowe dla roli "Kontrahent"|*OSDp*/ *OSDn*/ *Wytwórca*/ *Magazyn*/ *Odbiorca Końcowy*|
| Numer Umowy kontrahenta | tekst |Obowiązkowe dla roli "Kontrahent"|do 100 znaków|
| Data obowiązywania umowy od | data |Obowiązkowe dla roli "Kontrahent"|-|
| Data obowiązywania umowy do | data |Obowiązkowe dla roli "Kontrahent"|-|

###b) Widok listy

> 📌 **Menu → Użytkownicy**
> W menu użytkownicy dostępna lista rekordów z Email, rola, sortowanie domyślne po email
---

## 7. 🔢 Numery oświadczeń podstawowych — opłaty pozaprzesyłowe

Oświadczenia złożone **do daty terminu** określonego bramką *„Składanie oświadczeń rozliczeniowych"* otrzymują numer:

```
OSW / Typ_opłaty / Skrót_kontrahenta / rok_rozliczenia / miesiąc_rozliczenia / podokres / wersja
```

| Segment | Opis |
|---------|------|
| `OSW` | Skrót od „oświadczenie" |
| `Typ_opłaty` | `OP` / `OZE` / `OKO` / `OM` |
| `Skrót_kontrahenta` | do 20 znaków bez polskich liter |
| `rok_rozliczenia` | Rok, za który wykonywane jest rozliczenie |
| `miesiąc_rozliczenia` | Miesiąc, za który wykonywane jest rozliczenie |
| `podokres` | Numer podokresu danego miesiąca (od `01`) |
| `wersja` | Kolejny numer rozliczenia (od `01`) |

---

## 8. 🔢 Numery oświadczeń korygujących — opłaty pozaprzesyłowe

Oświadczenia złożone **po dacie terminu** określonego bramką *„Składanie oświadczeń rozliczeniowych"* otrzymują numer:

```
OSW / Typ_opłaty / Skrót_kontrahenta / rok_rozliczenia / miesiąc_rozliczenia / podokres / wersja / n_kor
```

> ℹ️ Dodatkowy segment `n_kor` oznacza skrót od „korekta" z uwzględnieniem numeru korekty. Typy opłat: `OP` / `OZE` / `OKO` / `OM`. Pozostałe segmenty — jak w pkt 7.

---

## 9. 🔢 Numery oświadczeń podstawowych — opłaty przesyłowe

Format numeru identyczny jak w pkt 7, z następującymi typami opłat:

| Kod | Nazwa opłaty |
|-----|-------------|
| `OZ` | Opłata zmienna (dla Energoserwis Kleszczów — wzór `ODO.ZS`) |
| `OJ` | Opłata jakościowa |
| `OR` | Opłata rynkowa |
| `ODO` | Opłata dodatkowa |

---

## 10. 🔢 Numery oświadczeń korygujących — opłaty przesyłowe

Format numeru jak w pkt 8 (z segmentem `n_kor`), z typami opłat:

| Kod | Nazwa opłaty |
|-----|-------------|
| `OJ` | Opłata jakościowa |
| `OPPEB` | Opłata za ponadumowny pobór energii biernej |
| `OPMO` | Opłata za przekroczenie mocy umownej |

---

## 11. 📊 Statusy oświadczenia

| Status | Opis |
|--------|------|
| ⬜ **Nie złożone** | Dla danego okresu nie wpłynęło oświadczenie |
| ⬜ **Robocze** | Dla danego okresu zapisano oświadczenie, ale jeszcze nie wysłano |
| 📤 **Złożone** | Oświadczenie złożone przez kontrahenta w PKU i dane wrzucone do pliku np. json |

---

## 12. 📅 Pozycje terminarza

| Pozycja | Typy |
|---------|------|
| Składanie oświadczenia rozliczeniowego | dzień kalendarzowy / dzień roboczy |
| Wystawienie faktury za świadczenie usług | dzień kalendarzowy / dzień roboczy |
| Składanie korygującego oświadczenia rozliczeniowego | dzień kalendarzowy / dzień roboczy |
| Wystawienie faktury za świadczenie usług po korekcie | dzień kalendarzowy / dzień roboczy |

---

## 13. 📆 Typ dla pozycji terminarza

- **Dzień kalendarzowy**
- **Dzień roboczy**

---

## 14. 📖 User Stories
- **a)** Jako administrator, chcę **wprowadzić wzór oświadczenia** z punktu 15.6 i **przypisać do niego wybrany typ Kontrahenta** z punktu 4
- **b)** Jako administrator, chcę **utworzyć terminarz** z punktu 12 i **przypisać do rodzaju opłaty** z punktu 5 i **do wybranego** typu Kontrahenta** z punktu 4 zakładając, że będzie się rozliczać w cyklu miesięcznym 
- **c)** Jako osoba składająca oświadczenie, chcę **zobaczyć terminarz** składania oświadczeń dla poszczególnych opłat pozaprzesyłowych i przesyłowych.
- **d)** Jako osoba składająca oświadczenie, chcę **zobaczyć jakie oświadczenia** mam do złożenia w bieżącym miesiącu i roku.
- **e)** Jako osoba składająca oświadczenie, chcę **uzupełnić i zapisać roboczo** oświadczenie uzupełniając dane ze wzoru oświadczenia.
- **f)** Jako osoba składająca oświadczenie, chcę **wysłać oświadczenie** czyli wrzucić dane do pliku np. json .
- **g)** Jako osoba składająca oświadczenie, chcę **otrzymać potwierdzenie** przyjęcia oświadczenia.
- **h)** Jako osoba składająca oświadczenie, chcę mieć możliwość **podglądu** złożonego oświadczenia.
- **i)** Jako osoba składająca oświadczenie, chcę mieć możliwość złożenia **oświadczenia korygującego**.

---

## 15. ✅ Funkcje obowiązkowe

### 15.1. 📊 Dashboard oświadczeń do obsłużenia

#### a) Zawartość dashboardu

Dashboard zawiera informacje o okresach wymagających złożenia oświadczenia:

- **Okresy rozliczeniowe** wymagające złożenia oświadczenia — z informacją o liczbie oświadczeń z podziałem na opłaty.
- **Wymagające złożenia oświadczenia** — okresy, dla których nie wpłynęło oświadczenie.

#### b) Nawigacja

> 📌 Kliknięcie w liczbę oświadczeń powoduje przejście do widoku **listy oświadczeń do obsługi**.

---

### 15.2. 📅 Terminarz rozliczeń dla rozliczania opłat

#### a) Podgląd terminarza — kolumny tabeli

| Kolumna | Opis |
|---------|------|
| Typ opłaty | — |
| Nazwa terminarza | — |
| Numer Umowy | — |
| Data od | — |
| Data do | — |

> 📌 Sortowanie domyślne: *Typ opłaty* rosnąco.

#### b) Filtry tabeli

| Filtr | Zachowanie |
|-------|-----------|
| **Rok** | Lista z wyborem roku |
| **Miesiąc** | Lista z wyborem miesiąca |
| **Kontrahent** | Jeśli użytkownik ma uprawnienie do jednego kontrahenta — wybrany domyślnie; w przeciwnym razie — wymagany wybór z listy |
| **Rodzaj opłaty** | Domyślnie puste (przesyłowe + pozaprzesyłowe) |

> ℹ️ Na formatce można filtrować i sortować po **wszystkich kolumnach** w tabeli.

#### c) Sekcja: Lista pozycji terminarza

W kontekście wybranego terminarza rozwija się sekcja z kolumnami:

| Kolumna | Opis |
|---------|------|
| Pozycja terminarza | — |
| Dzień | — |
| Godzina | — |
| Typ | — |

> ℹ️ Na formatce można filtrować i sortować po **wszystkich kolumnach** w tabeli.

---

### 15.3. 📋 Oświadczenia rozliczeniowe — lista

#### a) Struktura listy

Lista okresów rozliczeniowych podzielona na dwie części:

| Sekcja | Warunek wyświetlania |
|--------|---------------------|
| **Okresy, dla których nie złożono oświadczenia** | Status oświadczeń w okresie = *Nie złożone* |
| **Okresy, dla których złożono oświadczenie** | Status oświadczeń w okresie ≠ *Nie złożone* |

#### b) Kolumny tabeli

| Kolumna | Opis |
|---------|------|
| 🔘 Przycisk *„Złóż oświadczenie"* | Przejście do formularza składania oświadczenia |
| Typ opłaty | — |
| Numer umowy | — |
| Data od | — |
| Data do | — |
| Status oświadczeń w okresie | — |
| Data złożenia ostatniego oświadczenia | Data oświadczenia przesłanego jako ostatnie |
| Umowny termin złożenia oświadczenia | Termin z terminarza przypiętego do grupy rozliczeniowej |

> 📌 Sortowanie domyślne: *Typ opłaty* rosnąco.

#### c) Filtry dla tabeli

| Filtr | Zachowanie |
|-------|-----------|
| **Kontrahent** | Domyślnie wybrany (jeśli 1 kontrahent) lub wymagany wybór z listy |
| **Pokaż okresy, dla których** | Radiobutton: *nie złożono oświadczenia* / *złożono oświadczenie* / *wszystkie* (domyślnie) |

> ℹ️ Na formatce można filtrować i sortować po **wszystkich kolumnach** w tabeli.

#### d) Sekcja: Lista złożonych oświadczeń dla okresu rozliczeniowego

W kontekście wybranego rozliczenia rozwija się sekcja z kolumnami:

| Kolumna | Opis |
|---------|------|
| 👁️ Ikona podglądu | Przejście do formularza z podglądem złożonego oświadczenia |
| Numer oświadczenia | — |
| Data złożenia | — |
| Osoba składająca | — |
| Status oświadczenia | — |
| Data zmiany statusu | — |
| Złożone | *W terminie umownym* / *Po terminie umownym* |
| Przyjęte do rozliczenia | *podstawowego* (wersja 1) / *korygującego* (wersja > 1) — tylko dla statusu *Przyjęte do rozliczenia* |
| Numer rozliczenia | Widoczny dla statusu *Przyjęte do rozliczenia* |

> ℹ️ Na formatce można filtrować i sortować po **wszystkich kolumnach** w tabeli.

---

### 15.4. 📝 Składanie oświadczenia rozliczeniowego

#### a) Dane podstawowe oświadczenia *(sekcja zwijalna, tylko do odczytu)*

| Pole | Opis |
|------|------|
| Okres rozliczeniowy | Miesiąc rozliczeniowy z rokiem |
| Od / Do | Daty okresu rozliczeniowego |
| Umowa | Numer umowy |
| Typ opłaty | — |
| Typ kontrahenta | — |
| Kontrahent | Szczegóły zgodnie z pkt 6 |
| Status oświadczenia | — |
| Data złożenia oświadczenia | — |
| Data zmiany statusu | — |
| Osoba składająca oświadczenie | — |
| **Informacje o składającym:** Imię i nazwisko, E-mail, Telefon | — |
| Wersja oświadczenia | Licznik 1…n dla danego okresu |
| Złożone | *W terminie umownym* / *Po terminie umownym* |

#### b) Dane dotyczące opłaty *(tabela edytowalna)*

Zawartość tabeli zależna od typu opłaty i typu kontrahenta (zgodnie z regułami RB 3.3–3.17):

| Kolumna | Edytowalność |
|---------|-------------|
| Numer pozycji | 🔒 niedostępne |
| Kod pozycji | 🔒 niedostępne |
| Nazwa pozycji | 🔒 niedostępne |
| **Wartość** | ✏️ **dostępne do edycji** |
| Jednostka | 🔒 niedostępne |

#### c) Komentarz do oświadczenia

> ℹ️ Pole widoczne i obowiązkowe zgodnie z wzorcem wprowadzonym przez Administratora PKU. Kontrahent wpisuje opis, który chce przekazać.

#### d) Dostępne akcje

| Przycisk | Działanie |
|----------|----------|
| 🚫 **Anuluj** | Rezygnacja ze złożenia → powrót do listy oświadczeń |
| ✅ **Zapisz i wyślij** | Złożenie oświadczenia i dane wrzucone do pliku np. json |

> ⚠️ Przed wysłaniem wyświetlany jest komunikat potwierdzający:
> *„Czy na pewno chcesz podpisać i wysłać oświadczenie [numer] ? Anuluj / Zatwierdź"*
>
> - **Anuluj** — rezygnacja ze złożenia
> - **Zatwierdź** — zapisanie i dane wrzucone do pliku json, który można pobrać. Zatwierdzenie zmienia status na **Złożone**.

---

### 15.5. 👁️ Podgląd oświadczenia rozliczeniowego

#### a) Dane podstawowe oświadczenia *(sekcja zwijalna)*

Identyczne pola jak w pkt 15.4.a, z dodatkowymi polami (widocznymi tylko dla statusu *Przyjęte do rozliczenia*):

| Pole | Opis |
|------|------|
| Numer rozliczenia | — |
| Przyjęte do rozliczenia | *podstawowego* (wersja 1) / *korygującego* (wersja > 1) |

#### b) Dane dotyczące opłaty *(tabela — tylko do odczytu)*

| Kolumna |
|---------|
| Numer pozycji |
| Kod pozycji |
| Nazwa pozycji |
| Wartość |
| Jednostka |

#### c) Komentarz do oświadczenia

> ℹ️ Widoczne, jeżeli we wzorcu oświadczenia Administrator wskazał.

#### d) Powód odrzucenia

> ⚠️ Pole pojawia się **wyłącznie** dla oświadczeń w statusie *Odrzucone*.

#### e) Dostępne akcje

| Przycisk | Działanie |
|----------|----------|
| **Zamknij** | Zamknięcie podglądu → powrót do listy oświadczeń |

---

### 15.6. 📑 Dane oświadczeń wg typu opłaty i kontrahenta

> ℹ️ Dane pochodzą z wzorów utworzonych  **przez Administratora w PKU**. Poniższe tabele definiują pozycje formularza oświadczenia w zależności od kombinacji *typ opłaty × typ kontrahenta*.

---

#### a) Opłata przejściowa — OSDp / OSDn

| Nr | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|----|-----|-----------|---------------|:-----------:|:-----:|-------|
| 1 | IGDSUM | Number | Liczba odbiorców końcowych w gospodarstwach domowych *(suma 1.1–1.3)* | TAK | szt | z oświadczenia, bez edycji |
| 1.1 | IGD1i | Number | Zużywający < 500 kWh rocznie | TAK | szt | z oświadczenia, bez edycji |
| 1.2 | IGD2i | Number | Zużywający 500–1200 kWh rocznie | TAK | szt | z oświadczenia, bez edycji |
| 1.3 | IGD3i | Number | Zużywający > 1200 kWh rocznie | TAK | szt | z oświadczenia, bez edycji |
| 2 | OPSUM | Number (9,3) | Suma mocy umownych odbiorców końcowych *(suma 2.1–2.4)* | TAK | kW | z oświadczenia, bez edycji |
| 2.1 | PnNi | Number (9,3) | Przyłączeni do sieci nN kontrahenta | TAK | kW | z oświadczenia, bez edycji |
| 2.2 | PSNi | Number (9,3) | Przyłączeni do sieci SN kontrahenta | TAK | kW | z oświadczenia, bez edycji |
| 2.3 | PWN | Number (9,3) | Przyłączeni do sieci WN/NN kontrahenta | TAK | kW | z oświadczenia, bez edycji |
| 2.4 | Posi | Number (9,3) | Odbiorcy ≥ 400 GWh, ≥ 60% mocy umownej, koszt EE ≥ 15% produkcji | TAK | kW | z oświadczenia, bez edycji |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### b) Opłata OZE — OSDn / OSDp / OSDp + OK

| Nr | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|----|-----|-----------|---------------|:-----------:|:-----:|-------|
| 1 | OZESUM | Number (12,2) | Wielkość środków z tytułu opłaty OZE *(1.1 − 1.2)* | TAK | zł | z oświadczenia, bez edycji |
| 1.1 | OZEN | Number (12,2) | Wielkość należnych środków z tytułu opłaty OZE | TAK | zł | z oświadczenia, bez edycji |
| 1.2 | OZEPN | Number (12,2) | Wierzytelności nieściągalne z poprzednich okresów | TAK | zł | z oświadczenia, bez edycji |
| 2 | OZEE | Number (9,3) | Ilość energii — podstawa naliczania opłaty OZE | TAK | MWh | z oświadczenia, bez edycji |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### c) Opłata OZE — Odbiorcy końcowi / Magazyny

| Nr | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|----|-----|-----------|---------------|:-----------:|:-----:|-------|
| 1 | OZEil | Number (9,3) | Ilość energii — podstawa naliczania opłaty OZE | TAK | MWh | z oświadczenia, danych startowych lub SPR, bez edycji |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### d) Opłata OZE — Wytwórcy

| Nr | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|----|-----|-----------|---------------|:-----------:|:-----:|-------|
| 1 | OZEil | Number (9,3) | Planowana ilość energii — podstawa naliczania opłaty OZE | TAK | MWh | z danych startowych lub SPR, bez edycji |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### e) Opłata kogeneracyjna — OSDn / OSDp / OSDp + OK

| Nr  | Kod    | Typ danych    | Nazwa pozycji                                                 | Obowiązkowe | Jedn. | Uwagi                      |
| --- | ------ | ------------- | ------------------------------------------------------------- | :---------: | :---: | -------------------------- |
| 1   | OKOSUM | Number (12,2) | Wielkość środków z tytułu opłaty kogeneracyjnej *(1.1 − 1.2)* |     TAK     |  zł   | z oświadczenia, bez edycji |
| 1.1 | OKON   | Number (12,2) | Wielkość należnych środków                                    |     TAK     |  zł   | z oświadczenia, bez edycji |
| 1.2 | OKOPN  | Number (12,2) | Wierzytelności nieściągalne z poprzednich okresów             |     TAK     |  zł   | z oświadczenia, bez edycji |
| 1.3 | OKOO   | Number (12,2) | Wielkość pobranych środków                                    |     TAK     |  zł   | z oświadczenia, bez edycji |
| 2   | OKOE   | Number (9,3)  | Ilość energii — podstawa naliczania opłaty kogeneracyjnej     |     TAK     |  MWh  | z oświadczenia, bez edycji |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### f) Opłata kogeneracyjna — Odbiorcy końcowi / Wytwórcy / Magazyny

| Nr | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|----|-----|-----------|---------------|:-----------:|:-----:|-------|
| 1 | OKOE | Number (9,3) | Ilość energii — podstawa naliczania opłaty kogeneracyjnej | — | MWh | z oświadczenia, danych startowych lub SPR, bez edycji |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### g) Opłata mocowa — OSDn / OSDp / OSDp + OK / Wytwórcy / Magazyny

| Nr | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|----|-----|-----------|---------------|:-----------:|:-----:|-------|
| 1 | WSNOM | Number (12,2) | Wielkość środków z tytułu opłaty mocowej *(1.1 − 1.2)* | TAK | zł | z oświadczenia, bez edycji |
| 1.1 | WSNM | Number (12,2) | Wielkość należnych środków | TAK | zł | z oświadczenia, bez edycji |
| 1.2 | NOM | Number (12,2) | Wierzytelności nieściągalne z poprzednich okresów | TAK | zł | z oświadczenia, bez edycji |
| 2 | LOKsuma | Number | Liczba odbiorców końcowych (art. 89a ust. 1 pkt 1) *(suma 2.1–2.4)* | TAK | szt | z oświadczenia, bez edycji |
| 2.1 | LOKm1 | Number | Zużywający < 500 kWh rocznie | TAK | szt | z oświadczenia, bez edycji |
| 2.2 | LOKm2 | Number | Zużywający 500–1200 kWh rocznie | TAK | szt | z oświadczenia, bez edycji |
| 2.3 | LOKm3 | Number | Zużywający 1200–2800 kWh rocznie | TAK | szt | z oświadczenia, bez edycji |
| 2.4 | LOKm4 | Number | Zużywający > 2800 kWh rocznie | TAK | szt | z oświadczenia, bez edycji |
| 3 | WEM | Number (9,3) | Wolumen energii — podstawa naliczania opłaty mocowej | TAK | MWh | z oświadczenia, bez edycji; występuje warunkowo |
| 4 | WEnM | Number (9,3) | Wolumen energii po uwzględnieniu współczynników (art. 70a ust. 5) | TAK | MWh | z oświadczenia, bez edycji; występuje warunkowo |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### h) Opłata mocowa — Odbiorcy końcowi

| Nr | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|----|-----|-----------|---------------|:-----------:|:-----:|-------|
| 1 | WEM | Number (9,3) | Wolumen energii pobranej z sieci w godzinach opublikowanych (art. 74 ust. 4 pkt 2) | TAK | MWh | z danych startowych, bez edycji |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### i) Opłata jakościowa — Wytwórcy / Magazyny

| Nr      | Kod      | Typ danych   | Nazwa pozycji                                                                                      | Obowiązkowe | Jedn. | Uwagi                      |
| ------- | -------- | ------------ | -------------------------------------------------------------------------------------------------- | :---------: | :---: | -------------------------- |
| 1       | OSDpSUM  | Number (9,3) | Ilość EE dla odbiorców będących OSD *(suma 1.1 + 1.2)*                                             |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.1     | EosiSUM  | Number (9,3) | EE zużyta przez odbiorców specjalnych *(suma 1.1.1 + 1.1.2)*                                       |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.1.1   | Eosi'    | Number (9,3) | Odbiorcy specjalni przyłączeni do sieci OSDp                                                       |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.1.2   | Eososd   | Number (9,3) | Odbiorcy specjalni przyłączeni do sieci OSD bez miejsc dostarczania z sieci przesyłowej            |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2     | EokiSUM1 | Number (9,3) | Odbiorcy końcowi inni niż specjalni *(suma 1.2.1–1.2.7)*                                           |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.1   | Eoki'    | Number (9,3) | Odbiorcy końcowi korzystający z KSE, przyłączeni do sieci z usługami przesyłania OSP               |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.2   | Eokoi    | Number (9,3) | Odbiorcy końcowi przyłączeni do sieci OSDp                                                         |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.3   | Eokui    | Number (9,3) | EE zakupiona od osób trzecich na własny użytek przez przedsiębiorstwa przesyłowe/dystrybucyjne     |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.4   | Eokni    | Number (9,3) | Odbiorcy przyłączeni do sieci przedsiębiorstw niebędących operatorem                               |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.5   | Eokgi    | Number (9,3) | Odbiorcy przyłączeni do sieci/urządzeń wytwórcy przyłączonego do OSDp                              |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.6   | Eokmi    | Number (9,3) | Odbiorcy przyłączeni do urządzeń magazynowania EE                                                  |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.7   | EosdiSUM | Number (9,3) | Ilość EE Eosdi *(suma 1.2.7.1–1.2.7.6)*                                                            |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.7.1 | Eosdki   | Number (9,3) | Odbiorcy końcowi przyłączeni do sieci OSD bez miejsc dostarczania z sieci przesyłowej              |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.7.2 | Eosdui   | Number (9,3) | EE zakupiona od osób trzecich na własny użytek (OSD bez miejsc dostarczania)                       |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.7.3 | Eosdni   | Number (9,3) | Odbiorcy przyłączeni do sieci przedsiębiorstw niebędących operatorem (OSD bez miejsc dostarczania) |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.7.4 | Eosdgi   | Number (9,3) | Odbiorcy przyłączeni do urządzeń wytwórcy (OSD bez miejsc dostarczania)                            |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.7.5 | Eosdmi   | Number (9,3) | Odbiorcy przyłączeni do urządzeń magazynowania (OSD bez miejsc dostarczania)                       |     TAK     |  MWh  | z oświadczenia, bez edycji |
| 1.2.7.6 | Eosdoi   | Number (9,3) | EE na własny użytek OSDp bez miejsc dostarczania                                                   |     TAK     |  MWh  | z oświadczenia, bez edycji |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### j) Opłata jakościowa — Odbiorcy końcowi

| Nr | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|----|-----|-----------|---------------|:-----------:|:-----:|-------|
| 1 | OKSUM | Number (9,3) | Ilość EE do naliczania opłaty jakościowej *(poz. 2 + poz. 3 − poz. 4)* | TAK | MWh | z oświadczenia, bez edycji |
| 2 | Eok' | Number (9,3) | EE zakupiona od osób trzecich i zużyta na własne potrzeby | TAK | MWh | z oświadczenia, bez edycji |
| 3 | Eok'' | Number (9,3) | EE zużyta przez odbiorców przyłączonych do sieci odbiorcy | TAK | MWh | z oświadczenia, bez edycji |
| 4 | Eosd | Number (9,3) | EE, za którą opłatę wnosi odbiorca do OSD w ramach umów dystrybucyjnych | TAK | MWh | z oświadczenia, bez edycji |
| 5 | PKN OK | Number (9,3) | EE pobrana z sieci przesyłowej w MD Płock i zużyta przez PKN ORLEN | TAK | MWh | z oświadczenia, bez edycji; **tylko PKN ORLEN** |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### k) Opłata jakościowa — Wytwórcy, Magazyn, Wytwórca + Magazyn

| Nr | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|----|-----|-----------|---------------|:-----------:|:-----:|-------|
| 1 | WYTos | Number (9,3) | EE zużyta przez odbiorców specjalnych przyłączonych do sieci/urządzeń wytwórcy | TAK | MWh | z oświadczenia, bez edycji |
| 2 | WYTok | Number (9,3) | EE zużyta przez odbiorców końcowych przyłączonych do sieci/urządzeń wytwórcy (inni niż specjalni) | TAK | MWh | z oświadczenia, bez edycji |
| 1 | Eorlwytos | Number (9,3) | EE zużyta przez odbiorców specjalnych przyłączonych do sieci ORLEN jako wytwórcy | TAK | MWh | **tylko PKN ORLEN** |
| 2 | ORLSUM | Number (9,3) | Suma EE zużytej przez ORLEN + odbiorców końcowych *(suma 2.1 + 2.2)* | TAK | MWh | **tylko PKN ORLEN** |
| 3 | Eanwwyt | Number (9,3) | EE zużyta przez ANWIL S.A. jako odbiorcę przyłączonego do ORLEN jako wytwórcy | TAK | MWh | **tylko PKN ORLEN** |
| 4 | Eorlwytok | Number (9,3) | EE zużyta przez odbiorców końcowych innych niż ANWIL S.A. | TAK | MWh | **tylko PKN ORLEN** |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### l) Opłata rynkowa — wszyscy kontrahenci

| Nr | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|----|-----|-----------|---------------|:-----------:|:-----:|-------|
| 1 | Ewpi | Number (9,3) | Ilość EE przeznaczonej do wymiany między KSE a systemami państw spoza UE | TAK | MWh | z danych pomiarowych, bez edycji |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### m) Opłata zmienna sieciowa — wyjątki

| Źródło | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|--------|-----|-----------|---------------|:-----------:|:-----:|-------|
| oświadczenie | Ebel | Number (9,3) | EE zmierzona w polach liniowych 110kV Rogowiec Stary i Chabielice podczas postoju bloku 12 Bełchatów | TAK | MWh | **tylko PGE**, składnik dla ROGOWIEC |
| oświadczenie | Epi | Number (9,3) | EE pobrana przez Użytkownika podczas postoju bloku 12 Bełchatów | TAK | MWh | **tylko Energoserwis Kleszczów**, składnik dla ROGOWIEC |
| oświadczenie | PKNzm | Number (9,3) | EE pobrana w MD Płock, przepłynęła FPP nr 5 i 6, niezużyta na potrzeby ogólne bloku G1 | TAK | MWh | **tylko PKN ORLEN**, składnik MD_PŁOCK |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### n) Opłata dodatkowa — PGE

| Źródło | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|--------|-----|-----------|---------------|:-----------:|:-----:|-------|
| oświadczenie | Epol | Number (9,3) | EE zmierzona po stronie 110kV transformatorów TR1 i TR2 w stacji Połaniec | TAK | MWh | **tylko PGED**, odejmowane od Suma Opoi netto |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

#### o) Opłata za usługi przesyłania — Instytut Energetyki

| Źródło | Kod | Typ danych | Nazwa pozycji | Obowiązkowe | Jedn. | Uwagi |
|--------|-----|-----------|---------------|:-----------:|:-----:|-------|
| oświadczenie | Ih | Number (9,3) | Rzeczywista liczba godzin prób ATR3 w m-tym miesiącu | TAK | h | dodawane do Suma Opoi netto |

Czy możliwy do wprowadzenia **komentarz przez Kontrahenta TAK/ NIE** - jeżeli tak, to pole do edycji do 1000 znaków.
---

## 16. ⚙️ Wymagania niefunkcjonalne

### 16.1. 🧭 Menu aplikacji

```
📂 Administracja
├── 👤 Użytkownicy
├── 📋 Wzorce oświadczeń
├── 📅 Definiowanie terminarzy
└── 🔐 Użytkownicy
📂 PKU Rozliczenia
├── 📊 Dashboard oświadczeń do obsłużenia
├── 📅 Terminarz rozliczeń
└── 📋 Oświadczenia rozliczeniowe
```

> ℹ️ Osobne uprawnienia dla Kontrahenta do **przeglądania** i do **wykonywania akcji**.
> ℹ️ Osobne uprawnienia dla Administratora do **obsługi listy użytkowników, terminarzy i wzorców oświadczeń** 

### 16.2. 🔑 Uprawnienia do menu

| Rola | Zakres |
|------|--------|
| Każdy Kontrahent | 👁️ Podgląd oświadczeń rozliczeniowych, dashboardu i terminarzy |

### 16.3. 🔒 Uprawnienia do danych

- 👁️ Podgląd oświadczeń rozliczeniowych, dashboardu i terminarzy
- ✏️ Składanie oświadczeń podstawowych i korygujących

### 16.4. 🛡️ Bezpieczeństwo

- **Uwierzytelnianie** — login + hasło
- **Kontrola dostępu** — zgodnie z uprawnieniami do menu (pkt 16.2)

### 16.5. 🚀 Wydajność

> ℹ️ Brak wymagań.

---

## 17. 🔗 Ograniczenia / Zależności

> ℹ️ Brak wymagań dotyczących czasu, narzędzi, systemów, API ani dostępności danych.

---

## 18. 🚧 Poza zakresem

- Inne walidacje

---

## 19. 🏆 Demo i kryteria sukcesu

### Poziom I — Minimum Viable Demo

Pokazanie działającej aplikacji www z:

- ✅ co najmniej **2 typy użytkowników** (OSDp / OSDn / Wytwórca / Magazyn / Odbiorca końcowy)
- ✅ co najmniej **lista oświadczeń** zalogowanego Kontrahenta do złożenia na bieżący miesiąc i rok
- ✅ co najmniej **możliwość złożenia 2 typów oświadczeń** na wybrane opłaty Kontrahenta