package config
import spinal.core._

object Config {
  def spinal = SpinalConfig(
    targetDirectory = "src/gen",
    defaultConfigForClockDomains = ClockDomainConfig(
      resetActiveLevel = LOW
    ),
    onlyStdLogicVectorAtTopLevelIo = true
  )
}