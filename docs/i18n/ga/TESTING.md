# Gaeilge (ga) — Testing

> [Roghnóir teanga](../../LANGUAGES.md) · Cuirtear an logánú seo ar fáil ar mhaithe le hinrochtaineacht. Má bhíonn difríocht ann, is í an fhoinse chanónach theicniúil nó dhlíthiúil Bhéarla atá i réim. Fanann `LICENSE` agus`NOTICE` na fréimhe ceangailteach.

## Cad a thugtar ar thástáil aonaid? / Cad is tástáil aonaid ann?

Sea: seiceálann an tástáil aonaid rang amháin nó riail aonair, seachtrach
gan líonra, bunachar sonraí agus VIES beo. Fast, cinntitheach, agus i ngach tógáil
is féidir rith.
Sea: fíoraíonn tástáil aonaid aicme nó riail amháin ina aonar, gan líonra seachtrach,
bunachar sonraí, nó VIES beo. Tá sé tapa, cinntitheach, agus oiriúnach do gach tógáil.
Teastaíonn **tástálacha imeasctha áitiúil agus comórtais** don mhodúl seo freisin
freisin toisc go bhfuil an teorainn ama, atriail, eitilt aonair, cealú agus iompar `close()` níos mó
is léir ó chomhoibriú na comhpháirte. Úsáideann siad freastalaí bréige loopback, uimh
glaoigh ar sheirbhís an AE.
Teastaíonn **tástálacha comhtháthaithe áitiúla agus comhairgeadra** don mhodúl seo freisin, mar gheall ar an teorainn ama,
tá comhpháirteanna iolracha i gceist le hathtriail, le heitilt aonair, le cealú agus le múchadh. iad seo
úsáideann tástálacha seachfhreastalaí lúbach agus ní ghlaonn siad ar sheirbhís phoiblí an AE riamh.

## Orduithe tapa / Quick commands

Pacáiste iomlán tástála cinntitheach / Full deterministic suite:

```bash
./mvnw test
```

Tástálacha aonaid amháin:

```bash
./mvnw -Dtest=VatFormatTest,ViesRequesterTest,ViesResponseMappingTest,ViesErrorTest,ViesAvailabilityTest,ViesClientBuilderTest,MiniJsonTest,TtlCacheTest test
```

Tástálacha áitiúla HTTP/comhairgeadra amháin / HTTP Áitiúil agus comhairgeadra amháin:

```bash
./mvnw -Dtest=ViesClientHttpTest test
```

Fíorú glan le giniúint JAR/Javadoc / Fíorú glan le déantáin:

```bash
./mvnw clean verify
./mvnw package
```

Tástáil shonrach amháin / Modh tástála amháin:

```bash
./mvnw -Dtest=ViesClientHttpTest#cancellationDoesNotLeakAsyncCapacity test
```

## Clúdach reatha / Current coverage

Tá **73 tástáil** sa phacáiste cinntitheach reatha:

- **tástáil 44 aonad** in ocht rang;
- **29 tástáil áitiúil HTTP/comhtháthaithe/comhairgeadra**;
- náid glaonna líonra seachtracha éigeantacha.
  Tá **73 tástáil** sa tsraith cinntitheach: 44 triail aonad, 29 tástáil áitiúil
  Tástálacha HTTP/comhtháthú/comhairgeadra, agus náid glaonna seachtracha éigeantacha.

## Catalóg tástála aonaid / catalóg tástála aonaid

### `VatFormatTest`- 8 dtástáil

| ID       | sprioc Ungáiris                                                  | cuspóir Béarla                                          |
| -------- | ---------------------------------------------------------------- | ------------------------------------------------------- |
| U-FMT-01 | An uimhir chánach iomlán a normalú                               | Normalaigh an t-aitheantóir CBL iomlán                  |
| U-FMT-02 | Bain spás/tréimhse/fleasc, caipitligh                            | Deighilteoirí stráice agus cás uachtair                 |
| U-FMT-03 | `GR`→`EL`mapáil                                                  | Léarscáil An Ghréig`GR`go VIES`EL`                      |
| U-FMT-04 | Diúltaigh null, bán, tír anaithnid agus fad mícheart             | Diúltaigh null/bán/anaithnid/drochfhad                  |
| U-FMT-05 | Foirmeacha na tíre ionadaí                                       | Formáidí na dtíortha ionadaíocha                        |
| U-FMT-06 | Cód tíre ar leith + uimhir API, réimír ceangailte                | Péire API le réimír ceangailte                          |
| U-FMT-07 | Cóid na dtíortha tacaithe                                        | Tacar tíre tacaithe                                     |
| U-FMT-08 | Tá foirm amháin ar a laghad de na 28 dtír a fhaigheann tacaíocht | Cruth amháin ar a laghad do na 28 gcód a dtacaítear leo |

### `ViesRequesterTest`- 4 thástáil

| ID       | sprioc Ungáiris                                      | cuspóir Béarla                             |
| -------- | ---------------------------------------------------- | ------------------------------------------ |
| U-REQ-01 | Comhlánaigh ón iarrthóir uimhir chánach féin         | Cruthaigh iarratasóir ó CBL iomlán         |
| U-REQ-02 | Canónú iarratasóir Gréagach                          | Canonicalize iarratasóir Gréigis           |
| U-REQ-03 | Cruthaitheoir péireáilte agus réimír i gceangal leis | Cruthaitheoir péire le réimír meaitseáilte |
| U-REQ-04 | Diúltú láithreach don iarrthóir lochtach             | Teip go tapa ar iarrthóir neamhbhailí      |

### `ViesResponseMappingTest`— 11 thástáil

| ID       | sprioc Ungáiris                                             | cuspóir Béarla                                  |
| -------- | ----------------------------------------------------------- | ----------------------------------------------- |
| U-MAP-01 | GET-stíl `Valid` le gach réimse                             | Léarscáil Freagra bailí ar stíl GET             |
| U-MAP-02 | POST-stíl `Valid`,`---` placeholder                         | Léarscáil POST-stíl Bailí agus áitshealbhóirí   |
| U-MAP-03 | Barántúla `Invalid`                                         | Údarásach léarscáile Neamhbhailí                |
| U-MAP-04 | Earráid neamhbhuan →`Unavailable`                           | Earráid neamhbhuan léarscáil go Níl sé ar fáil  |
| U-MAP-05 | Earráid ionchuir →`MalformedInput`                          | Earráid ionchuir Léarscáil VIES                 |
| U-MAP-06 | Diúltaigh JSON nach ábhar é                                 | Diúltaigh JSON nach ábhar é                     |
| U-MAP-07 | Ní féidir le Boole ar iarraidh a bheith `Invalid`           | Ní bheidh Boole ar iarraidh Neamhbhailí         |
| U-MAP-08 | Ní féidir le boolean teaghrán a bheith `Invalid`            | Diúltaíodh do bhailíocht neamh- Boole           |
| U-MAP-09 | Ní aimsímid am áitiúil le haghaidh dáta iniúchta in easnamh | Ná ceapadh stampa ama iniúchta in easnamh riamh |
| U-MAP-10 | Diúltaigh an dáta iniúchta mícheart                         | Diúltaigh stampa ama iniúchta neamhbhailí       |
| U-MAP-11 | Comhshó ceart an stampa ama fhritháireamh go UTC            | Parsáil stampa ama fhritháireamh go UTC         |

### `ViesErrorTest`- 6 thástáil

| ID       | sprioc Ungáiris                                   | cuspóir Béarla                                   |
| -------- | ------------------------------------------------- | ------------------------------------------------ |
| U-ERR-01 | Teachtaireacht líonra Ungáiris+Béarla             | Earráid líonra dhátheangach                      |
| U-ERR-02 | Ní féidir earráid ionchuir a aisghabháil          | Tá an earráid ionchuir buan                      |
| U-ERR-03 | Aicmiú athiarracht HTTP 408/429/5xx               | Aicmiú atriail HTTP                              |
| U-ERR-04 | Ní earráid í `Valid`/`Invalid`                    | Nochtann cinntí aon earráid                      |
| U-ERR-05 | Gach cód poiblí HU+EN agus luach atriail chobhsaí | Tá HU+EN ag gach cód poiblí agus beartas atriail |
| U-ERR-06 | Coimeád cód anaithnid gan atriail stoirme         | Caomhnaigh cód anaithnid gan atriail stoirme     |

### `ViesAvailabilityTest`- 2 thástáil

- cóip chosanta agus neamh-inaistritheacht na léarscáile ionchuir;
- diúltú láithreach do léarscáil an Bhallstáit ar neamhní.
  Cóip chosanta do-mháistrithe agus bailíochtú cruthaitheoir léarscáile neamhní.

### `MiniJsonTest`- 4 thástáil

- doiciméad tipiciúil VIES agus éalú Unicode;
- réad neadaithe / liosta / scálach / uimhir / null;
- éalaíonn JSON;
- diúltú d’ionchur mícheart, teasctha agus leanúnach.
  VIES JSON tipiciúla, luachanna neadaithe/scálaithe, éalaíonn, agus ionchur míchumtha cliseadh dúnta.

### `TtlCacheTest`- 6 thástáil

- buail roimh TTL, caill tar éis;
- teorainn bheacht TTL;
- neamhaird a dhéanamh ar TTL neamhdhearfach;
- teorainn méide cumraithe;
- díláithriú tosaíochta eilimint atá imithe in éag;
- 32 snáithe fíorúil a scríobh/léamh i gcomhthráth.
  Iompar TTL, teorainn éagtha bheacht, rialú méide, sampláil in éag-chéad, agus
  brú comhthráthach ó 32 snáithe fíorúil.

### `ViesClientBuilderTest`- 3 thástáil

- is féidir cumraíocht iomlán ard-ualach a thógáil;
- diúltaigh URL mícheart/teorainn/triail eile;
- diúltú nialas, diúltach agus ró-shreabhadh Tréimhse.
  Cumraíocht ard-ualach bailí móide URL, teorainn, triail arís, fad agus ró-shreabhadh
  bailíochtú.

## HTTP áitiúil, comhairgeadra agus saolré / Comhtháthú áitiúil agus comhairgeadra

`ViesClientHttpTest`- 29 tástáil ar phort randamach lúb ar ais:
| ID | Cás tástála |
|---|---|
| I-01 | 503 athiarracht faoi dhó, ansin rath / dhá 503 iarracht ansin rath |
| C-01 | 200 glaoiteoir async-eochair chéanna → 1 iarratas HTTP go díreach / 200 glaoiteoir asioncronnaithe den eochair chéanna → iarratas amháin |
| C-02 | Ní sháraíonn iarratais HTTP ghníomhacha an teorainn de 4 / fanann glaonna gníomhacha laistigh de 4 |
| C-03 | Asincréite ar feitheamh os cionn na teorann láithreach `CLIENT_OVERLOADED`|
| C-04 | Ní sceitheann cealú sa todhchaí cead, tá an toilleadh athchóirithe |
| C-05 | Ní stopann `close()` an t-aisghlao as sioncronú |
| C-06 |`admissionTimeout` teorainneacha scuaine |
| I-02 | Earráid léite Redis/taisce →`CACHE_ERROR`, null glao VIES |
| C-07 | Cead agus eitilt aonair | a scaoiltear roimh ghlaonna asioncronaithe as a chéile
| C-08 | Taisce async / rás dúnta →`CLIENT_CLOSED`, null HTTP |
| C-09 | Sioncronaigh taisce / rás dúnta →`CLIENT_CLOSED`, null HTTP |
| C-10 | Cuirtear isteach ar phost seiceadóir saincheaptha, ach ní dhúnann an seiceadóir |
| C-11 | Faigheann ceannaire Sync agus leantóir an toradh céanna `CLIENT_CLOSED`|
| C-12 | Ní itheann 100 leantóir comhionann sioncronaithe 100 sliotán ar feitheamh |
| C-13 | Tar éis diúltaithe don seiceadóir, athchóirítear stádas ceada agus eitilte |
| C-14 | Ceannaire sioncronaithe + leantóir async → iarratas HTTP amháin |
| C-15 | Dúnann an dara taisce-seiceáil an rás caillte, null HTTP |
| C-16 | Ag druidim le linn scríobh an taisce, is ionann toradh an cheannaire/leanúna sioncronaithe |
| C-17 | An cead ar feitheamh | scaoiltear freisin é roimh ghlao asynced slabhraithe le heochracha éagsúla
| C-18 | Cuirtear tasc seiceadóir saincheaptha scuaine ar ceal faoi dhúnadh iarbhír |
| C-19 | Ní chuireann bac ar aisghlaoch úsáideora bac ar ghlas gar/saolré |
| C-20 | Ceannaire Async + leantóir sioncronaithe → iarratas HTTP amháin |
| C-21 | Tugann `maxPendingSyncRequests` backpressure teoranta toirt |
| C-22 | Leanann async marfach `Error` freisin i dtreo todhchaí eisceachtúil agus láimhseálaí neamhghafa |

## Cad nach ndéanaimid tástáil réamhshocraithe? / Cad nach bhfuil sa tsraith réamhshocraithe?

## Tástáil deataigh Live VIES / Live VIES smoke test

Tá an tseirbhís bheo inathraithe agus teoranta ó thaobh rátaí de, mar sin ní féidir coinníoll CI éigeantach a bheith ann.
Is éard atá i gceist le tástáil láimhe deataigh ná `availability()` amháin ar a mhéad agus tástáil aitheanta, neamhrúnda nó
ceistigh an uimhir chánach a fuarthas ó athróg timpeallachta, comhairgeadra=1 agus bain triail eile as =0
socrú.
Tá Live VIES athraitheach agus ráta-teoranta, mar sin ní féidir leis an gnáth-CI a sheasamh. Rogha rogha an diúltaithe
ba cheart go ndéanfadh tástáil deataigh seiceáil infhaighteachta amháin agus bailíochtú amháin ar a mhéad, le
concurrency=1 agus atriail=0. Ná déan uimhreacha CBL iarratasóirí príobháideacha a cheangal ná a logáil riamh.

## Tástáil luchtaithe / Tástáil luchtaithe

Cuir i gcoinne bréige áitiúil nó do sheirbhís stáitse féin i gcónaí, gan seirbhís phoiblí riamh
in aghaidh VIES. Tá na riachtanais iomlána faisnéiseach; cruinneas, teorainn, p95/p99 agus cóid earráide
na geataí.
Dírigh i gcónaí ar sheirbhís bhréige áitiúil nó ar sheirbhís stáitse faoi úinéireacht, gan VIES poiblí riamh. Absalóideach
iarratais/an dara ceann faisnéiseach; cruinneas, teorainneacha, p95/p99, agus séimeantaic earráide
Tá na geataí.
Cásanna molta:

- go leor eochracha ar leith;
- Glaoiteoir te-eochair 100k ar shraith bheag eochracha / stampede te-eochair;
- ró-ualach agus aisghabháil os cionn na teorann;
- 200/429/503/teorainn ama/freagra measctha míchumtha;
- brú taisce thart ar an uasmhéid cumraithe.

## Tástáil soak agus chaos / Soak and chaos test

Ualach seasta 30-60 nóiméad, carn teoranta, JFR, dúnadh/atosú arís agus arís eile, foighne
spíc, athshocrú nasc, teip taisce agus cealú. An gcarn, snáitheanna gníomhacha,
líon ardchlár eitilte/ar feitheamh agus soicéid; níor cheart go mbeadh aon slad glas nó cead sceitheadh ​​ann.
Rith 30-60 nóiméad le carn seasta, JFR, saolré arís agus arís eile, spikes latency,
athshocrú nasc, teipeanna taisce, agus cealú. Carn / snáitheanna / eitilte / soicéid
Ní mór ardchlár a dhéanamh gan aon slad ná ligean do sceitheadh.
Sampla JFR / sampla JFR:

```bash
JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=target/vies-soak.jfr,settings=profile" ./mvnw test
```

## AMHÁIN javaslat / AMHÁIN molta

Píblíne íosta:

```bash
./mvnw --batch-mode clean verify
```

Maitrís molta: ar a laghad JDK 21 LTS; ní féidir JDK breise a shocrú ach ansin
tacaíocht má ritheann an tsraith chéanna orthu i ndáiríre.
Maitrís molta: ar a laghad JDK 21 LTS. Éileamh tacaíocht JDK breise ach amháin tar éis
ag rith na sraithe céanna ar na leaganacha sin.

## Riail aischéimnithí / Regression rule

Ba cheart go mbeadh tástáil cinntitheach ag gach fabht seasta arb í sin an deisiúchán
bheadh teipthe roimhe seo. Ba chóir go mbeadh latch/ bacainn mar thosaíocht le haghaidh tástála iomaíochta
sioncronú ; ní féidir le `sleep` seasta a bheith ach mar chúl-aschur vótaíochta gearr, ní oracle cruinnis.
Caithfidh gach fabht seasta tástáil cinntitheach a fháil ar theip air roimh an socrú.
Is fearr laistí / bacainní le haghaidh tástálacha comhairgeadra; d’fhéadfadh gur vótaíocht ghearr a bheadh i gcodladh seasta
Riamh backoff amháin, an oracle cruinneas.
