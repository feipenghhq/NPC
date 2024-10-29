/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/9/2024
 *
 * ------------------------------------------------------------------------------------------------
 * YsyxSoC: Top Level for ysyxSoC
 * ------------------------------------------------------------------------------------------------
 */

package soc

import spinal.core._
import spinal.lib._
import core._
import config.RiscCoreConfig
import _root_.bus.Axi4Lite._

case class YsyxSoC(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val host = master(Axi4Lite(config.axi4LiteConfig))
        val device = slave(Axi4Lite(config.axi4LiteConfig))
        val interrupt = in port Bool()
    }

    io.host.updateSignalName("io_master")
    io.device.updateSignalName("io_slave")
    io.device <> io.device.getZero

    val ibus = Axi4Lite(config.axi4LiteConfig)
    val dbus = Axi4Lite(config.axi4LiteConfig)

    val axiArbiter = Axi4LiteArbiter(config.axi4LiteConfig, 2, false)
    axiArbiter.io.input <> Vec(ibus, dbus)
    axiArbiter.io.output <> io.host

    val core = CoreN(config)
    ibus <> core.io.ibus
    dbus <> core.io.dbus

    val uCoreNDpi = CoreNDpi(config)
    uCoreNDpi.io.ebreak := core.iduData.cpuCtrl.ebreak.pull()
    uCoreNDpi.io.ecall := core.iduData.cpuCtrl.ecall.pull()
    uCoreNDpi.io.pc := core.uIFU.pc.pull()
}

object YsyxConfig {
  def spinal = SpinalConfig(
    targetDirectory = "src/gen",
    defaultConfigForClockDomains = ClockDomainConfig(
      resetActiveLevel = HIGH
    ),
    onlyStdLogicVectorAtTopLevelIo = true
  )
}

object YsyxSoCVerilog extends App {
    val axi4LiteConfig = Axi4LiteConfig(addrWidth = 32, dataWidth = 64)
    val config = RiscCoreConfig(32, 0x20000000L, 32, axi4LiteConfig=axi4LiteConfig)
    YsyxConfig.spinal.generateVerilog(YsyxSoC(config)).printPruned()
}