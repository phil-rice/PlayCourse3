package domain

import org.validoc.utilities.{TraceId, UnitSpec}

class EnrichMostPopularSpec extends UnitSpec with EnricherFixture[MostPopularQuery, MostPopular, Programme, EnrichedMostPopular] {

  behavior of "EnrichMostPopular"

  val programmeId1 = ProgrammeId("id1")
  val programmeId2 = ProgrammeId("id2")
  val mostPopular = MostPopular(List(programmeId1, programmeId2))

  val programme1 = Programme(programmeId1, "someStuff1")
  val programme2 = Programme(programmeId2, "someStuff2")
  val programmes = List(programme1, programme2)

  it should " have an enricher " in {
    setupEnricher { enricher =>
      enricher.apply(MostPopularQuery(TraceId("someTraceId")), mostPopular, programmes) shouldBe EnrichedMostPopular(programmes)
    }
  }
}
