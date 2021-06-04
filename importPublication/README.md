## Build

## Run

### Add an article by DOI
Replace <<foo>> and <<bar>> by your users and password. Notice the space in front of that command
to not store them in your shell history.

```console
 export WIKIDATA_USER=<<foo>>
 export WIKIDATA_PASSWORD=<<bar>>
./gradlew :importPublication:run --args "10.1007/BF00598758"
```
