[![Build Status](https://travis-ci.org/pchandar/fGen.svg?branch=master)](https://travis-ci.org/pchandar/fGen)

FGen is a feature generation library that can be used to obtained feature vectors from any given text.  Currently, we can use fGen to extract features for each each token in the text. More documentation coming soon.

# Demo Example
To compile type

```
$ sbt compile
```

The project currently has support for reading Clinical Trial from [clinicalTrials.gov](https://clinicaltrials.gov/).

To extract token feature for each token in the clinical trial's title, run the following demo

```
$ sbt -mem 5000 "run -a generate -c com.pchandar.examples.SampleCTGovConfig"
```

This should create a `fGen` folder in your home directory. A separate file is created for each  trial and the features for each token are separated by new line.
