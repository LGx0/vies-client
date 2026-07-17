# Ελληνικά (el) — Υποστήριξη

**Languages:** [English](../../../SUPPORT.md) · [Ελληνικά](SUPPORT.md) · [Όλες οι γλώσσες](../../LANGUAGES.md)

> Ενημερωτική μετάφραση. Σε περίπτωση διαφοράς υπερισχύει το αγγλικό τεχνικό και νομικό πρωτότυπο. Μόνο τα ριζικά LICENSE και NOTICE είναι νομικά έγκυρα· η μετάφραση δεν αποτελεί άδεια. [LICENSE](../../../LICENSE), [NOTICE](../../../NOTICE).

Για αναπαραγώγιμο bug χρησιμοποιήστε το bug template, για ερωτήσεις τα GitHub Discussions και για ευπάθεια το `SECURITY.md`. Η κοινοτική υποστήριξη παρέχεται κατά το δυνατό χωρίς εγγυημένο χρόνο απόκρισης και δεν αποτελεί φορολογική, νομική ή λογιστική συμβουλή· ο integrator παραμένει υπεύθυνος.

Οι δωρεές είναι προαιρετικές και δεν αγοράζουν προτεραιότητα, SLA, εξαίρεση ασφάλειας ή δικαίωμα διακυβέρνησης. Το επαληθευμένο URL διαχειρίζεται το `.github/FUNDING.yml`.

Υποστηρίξτε το έργο μέσω [Buy Me a Coffee](https://buymeacoffee.com/lgx0). Η δωρεά είναι προαιρετική και δεν αγοράζει προτεραιότητα, SLA ή δικαίωμα διακυβέρνησης.

## Πρόσθετοι κανόνες διακυβέρνησης και ασφάλειας

Η μοναδική άδεια είναι Apache-2.0. Μόνο τα αγγλικά `LICENSE` και `NOTICE` είναι νομικά δεσμευτικά. Πριν από publish ελέγχονται δικαιώματα δημιουργού, πιθανή ιδιοκτησία εργοδότη ή πελάτη, προέλευση και άδεια third-party υλικού και απουσία secrets, προσωπικών και πελατειακών δεδομένων. Το έργο είναι ανεξάρτητο και δεν αποτελεί επίσημο προϊόν ή σύσταση Ευρωπαϊκής Επιτροπής, ΕΕ ή φορολογικής αρχής.

Αλλαγή public API, άδειας ή αρχιτεκτονικής συζητείται πρώτα σε issue. Ο contributor εργάζεται σε σύντομο branch από `main`, κάνει μικρή αλλαγή, προσθέτει deterministic regression test και εκτελεί πλήρες Maven verify. Unit test δεν χρησιμοποιεί δημόσιο δίκτυο, HTTP/concurrency test μόνο loopback mock και load στο public VIES απαγορεύεται. Shared state παραμένει thread-safe και memory-bounded· `Unavailable` ποτέ `Invalid`.

Ευπάθεια αναφέρεται ιδιωτικά με GitHub Security Advisory, έκδοση, περιβάλλον, reproduction, impact και ελάχιστο PoC χωρίς πραγματικά δεδομένα. Στόχοι: επιβεβαίωση σε επτά ημέρες και πρώτη αξιολόγηση σε δεκατέσσερις, όχι συμβατικό SLA. Σημαντικά είναι injection, TLS bypass, data leak, cache poisoning, requester misuse, unbounded resources, shutdown deadlock και λανθασμένο `Invalid`.

Για `main` απαιτούνται PR, approval, resolved conversations, CI, CodeQL και dependency review· force push και deletion απαγορεύονται. Release χρησιμοποιεί signed annotated tag και περιέχει binary, sources, Javadoc JAR και SHA-256. Secrets και GPG keys μόνο σε Actions. Οι δωρεές είναι προαιρετικές και δεν αγοράζουν priority, SLA ή governance. Οι συμμετέχοντες είναι σεβαστικοί· παρενόχληση, διάκριση, doxxing, εξαπάτηση, κλοπή πατρότητας και ανεύθυνο disclosure οδηγούν σε moderation.
