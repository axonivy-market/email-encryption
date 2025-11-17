# #Email Verschlüsselung Demo

#Axon Efeus#Email Verschlüsselung Nutzen versieht du mit eine Sample Ausführung
für senden #verschlüsselt #Email von irgendwelchem dienstlichen Arbeitsgang.

Dieses Markt Stück:

- Gibt du eine Vorlage für senden #verschlüsselt #Email via #ein preconfigured
  Form.
- Ist gegründet weiter das OpenSSL Bibliothek, bietend offen-Quelle Ausführungen
  von die SSL und TLS Protokolle.

## Demo

In diesen Demo Antrag, du willst können senden direkt #ein #verschlüsselt #Email
von eure UI Form in #irgendein von euren dienstlichen Arbeitsgängen.

![#Verschlüsselt #Email #Screenshot](EncryptedEmailDemo.png "#Verschlüsselt
#Email #Screenshot")

1. Starte das SendEncryptedEmail Arbeitsgang.

2. Setz ein die #Email Form.

3. Gesetzt den öffentlichen Schlüssel von dem Telefonhörer.

4. Sende die #Email.

## Einrichtung

### Schaff #Ich Signiert S/MIMT Urkunde

Zuerst willst du brauchen zu installieren OpenSSL auf Fenster. Du können die
Bibliothek herunterladen von hier:
[OpenSSL](http://gnuwin32.sourceforge.net/packages/openssl.htm)

Jede die Befehle sollten sein gerannt von die Befehl #Eingabeaufforderung unter
das Installation Telefonbuch in den MÜLLEIMER Ordner:

```

C:\OpenSSL-Win64\bin

```

Zuerst, lass uns schaffen ein #Ich-Indiz Urkunde und eine persönliche Autorität
gültig für 365 Tage:

```

openssl req -x509 -newkey rsa:4096 -keyout encrypted.email.key -out encrypted.email.crt -sha256 -days 365

```

Setz ein alle die Auskunft in dem Zauberer zu schaffen die Urkunde benutzend den
persönlichen Schlüssel Passwort:

![Öffne SSL Befehl](OpenSSL.png "Öffne SSL Befehl")

Jetzt geschafft hast du ein #Ich-#mit Vorzeichen versehen Urkunde mit der
persönlichen Autorität, aber FRAU Aussicht, Donnervogel, und anderen #Email
Kunden benutzen die `p12` Urkunde Stil. Deswegen, lass uns schaffen diese Sorte
Urkunde:

```

openssl pkcs12 -export -inkey encrypted.email.key -in encrypted.email.crt -out encrypted.email.p12

```

Betritt das Passwort du geschafft hast für den persönlichen Schlüssel. Jetzt
hast du alle die Urkunden willst du brauchen. Zu hoffen jene Urkunden, du willst
brauchen zu installieren jene auf euren #Email Kunden.

### Installier ein S/MIMT Urkunde für FRAU Aussicht

Installierend die Urkunde für FRAU Aussicht ist beschrieben hier: [Installierend
ein s-mimen Urkunde mit
Aussicht](https://www.ssl.com/how-to/installing-an-s-mime-certificate-and-sending-secure-email-with-outlook-on-windows-10)

1. In FRAU Aussicht, #ausgewählt Datei von der hauptsächlichen Speisekarte, dann
   Klick Optionen.

2. Wähl aus **Trust Zentrum** an dem Boden von der Speisekarte auf das #links
   #Seite.

3. Klick das **Trust Zentrum Lagen** Knopf.

4. Wähl aus **#Email Sicherheit** #linksseitig differenzierbar-Hand Speisekarte
   von die **Trust Zentrum** Fenster.

![Aussicht Trust Zentrum](OutlookTrustCenter.png "Aussicht Trust Zentrum")

5. Klick das **Einfuhr/Ausfuhr** Knopf, unter **Digital IDs (Urkunden)**.

6. Herstellung sicher **Einfuhr #existierend Digital ID von einer Datei** ist
   überprüft, dann Klick **#Durchsuchen...**

![Importier Digital ID](ImportDigitalID.png "Einfuhr Digital ID")

7. Befahr zu das PKCS#12 feil, dann Klick **Öffnet**. Das filename Extension
   sollte sein .p12

8. Betritt das Passwort du benutztest #wann #herunterladen das PKCS#12 feil,
   dann Klick **OK**.

### Installier ein S/MIMT Urkunde auf Donnervogel #Email Kunden

1. Öffne **Konto Lagen**.

2. Wähl aus **Ende-Zu-Ende Verschlüsselung**.

3. Klick weiter **Bringt fertig S/MIMT Urkunden**.

![Donnervogel Urkunde Manager](ThunderbirdCertificateManager.png "Donnervogel
Urkunde Manager")

4. Klick weiter **Einfuhr...**.

5. Befahr zu das PKCS#12 feil, dann Klick **Öffnet**. Das filename Extension
   sollte sein `.p12`.

6. Betritt das Passwort du benutztest #wann #herunterladen das `PKCS#12` Datei,
   dann Klick **OK**.

