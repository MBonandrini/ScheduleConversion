# ProjectConvert — GitHub Pages / Local MPXJ JARs

Deploy the contents of this folder to the root of the GitHub Pages repository.

Repository layout:

```
index.html
lib/
  projectconvert-browser.jar
  mpxj-16.7.0.jar
  ...dependency JARs...
```

`index.html` uses CheerpJ's `/app/` filesystem, so `/app/lib/mpxj-16.7.0.jar` maps to `https://<your-pages-site>/lib/mpxj-16.7.0.jar`. No MPXJ JAR is fetched from Maven Central or GitHub Releases at runtime.

The page reads the selected schedule into CheerpJ's `/str/` filesystem, MPXJ reads it using `UniversalProjectReader`, and the converter writes either Primavera XER or Microsoft Project MSPDI XML.

**Important:** every JAR named in `lib/REQUIRED-JARS.txt` must exist in `lib/`. The `projectconvert-browser.jar` bridge is included here. Third-party JAR binaries must be copied from the official MPXJ distribution or Maven Central before deployment.
