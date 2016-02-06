package com.pchandar.features

import cc.factorie.app.nlp.Token
import cc.factorie.variable.CategoricalVectorVar
import com.pchandar.nlp.NLPResources


class LexiconFeatures(nlpResource: NLPResources) {

  val lexicon = nlpResource.allTheLexicons

  // TODO : These lexicons are not exhaustive. check with factorie repo to grab the latest lexicons
  val addWikipedia = FactorieFeature("lexiconFeatures.addWikipedia", {
    (tokenSequence: IndexedSeq[Token], vf: Token => CategoricalVectorVar[String]) =>

      //lexicon.wikipedia.Person.tagText(tokenSequence,vf, "WIKI-PERSON")
      lexicon.wikipedia.Event.tagText(tokenSequence, vf, "WIKI-EVENT")
      lexicon.wikipedia.Location.tagText(tokenSequence, vf, "WIKI-LOCATION")
      lexicon.wikipedia.Organization.tagText(tokenSequence, vf, "WIKI-ORG")
      lexicon.wikipedia.ManMadeThing.tagText(tokenSequence, vf, "MANMADE")

      lexicon.wikipedia.Book.tagText(tokenSequence, vf, "WIKI-BOOK")
      lexicon.wikipedia.Business.tagText(tokenSequence, vf, "WIKI-BUSINESS")
      lexicon.wikipedia.Film.tagText(tokenSequence, vf, "WIKI-FILM")

      lexicon.wikipedia.LocationAndRedirect.tagText(tokenSequence, vf, "WIKI-LOCATION-REDIRECT")
      lexicon.wikipedia.PersonAndRedirect.tagText(tokenSequence, vf, "WIKI-PERSON-REDIRECT")
      lexicon.wikipedia.OrganizationAndRedirect.tagText(tokenSequence, vf, "WIKI-ORG-REDIRECT")
  })

  // TODO : These lexicons are not exhaustive. check with factorie repo to grab the latest lexicons
  val addIELS = FactorieFeature("lexiconFeatures.addIELS", {
    (tokenSequence: IndexedSeq[Token], vf: Token => CategoricalVectorVar[String]) =>

    lexicon.iesl.PersonFirst.tagText(tokenSequence, vf, "PERSON-FIRST")
    lexicon.iesl.PersonFirstHigh.tagText(tokenSequence, vf, "PERSON-FIRST-HIGH")
    lexicon.iesl.PersonFirstHighest.tagText(tokenSequence, vf, "PERSON-FIRST-HIGHEST")
    lexicon.iesl.PersonFirstMedium.tagText(tokenSequence, vf, "PERSON-FIRST-MEDIUM")

    lexicon.iesl.PersonLast.tagText(tokenSequence, vf, "PERSON-LAST")
    lexicon.iesl.PersonLastHigh.tagText(tokenSequence, vf, "PERSON-LAST-HIGH")
    lexicon.iesl.PersonLastHighest.tagText(tokenSequence, vf, "PERSON-LAST-HIGHEST")
    lexicon.iesl.PersonLastMedium.tagText(tokenSequence, vf, "PERSON-LAST-MEDIUM")

    lexicon.iesl.PersonHonorific.tagText(tokenSequence, vf, "PERSON-HONORIFIC")

    lexicon.iesl.Company.tagText(tokenSequence, vf, "COMPANY")
    lexicon.iesl.JobTitle.tagText(tokenSequence, vf, "JOB-TITLE")
    lexicon.iesl.OrgSuffix.tagText(tokenSequence, vf, "ORG-SUFFIX")

    lexicon.iesl.Country.tagText(tokenSequence, vf, "COUNTRY")
    lexicon.iesl.City.tagText(tokenSequence, vf, "CITY")
    lexicon.iesl.PlaceSuffix.tagText(tokenSequence, vf, "PLACE-SUFFIX")
    lexicon.iesl.Continents.tagText(tokenSequence, vf, "CONTINENT")

    lexicon.iesl.UsState.tagText(tokenSequence, vf, "USSTATE")
    lexicon.iesl.Demonym.tagText(tokenSequence, vf, "DEMONYM")
  })

}
