/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/9/2024
 *
 * ------------------------------------------------------------------------------------------------
 * CoreN: Risc V Core Non-pipeline
 * ------------------------------------------------------------------------------------------------
 */

package soc

import spinal.core._
import spinal.lib._
import config._
import _root_.misc._
import _root_.bus.Axi4Lite._
import core.CoreN
import core.CoreNVerilog.axi4LiteConfig


case class CoreNSoC(config: RiscCoreConfig) extends Component {
    val ibus = Axi4Lite(config.axi4LiteConfig)
    val dbus = Axi4Lite(config.axi4LiteConfig)

    val core = CoreN(config)
    ibus <> core.io.ibus
    dbus <> core.io.dbus

    val pc = core.uIFU.io.ifuData.payload.pc.pull()

    val uCoreNDpi = CoreNDpi(config)
    uCoreNDpi.io.ebreak := core.iduData.cpuCtrl.ebreak.pull()
    uCoreNDpi.io.ecall := core.iduData.cpuCtrl.ecall.pull()
    uCoreNDpi.io.pc := pc

    if (config.separateSram) {
        val uIfuSram = Axi4LiteSramDpi(config, 1, 0)
        uIfuSram.io.ifetch := True
        uIfuSram.io.pc := pc
        uIfuSram.io.bus <> ibus

        val uLsuSram = Axi4LiteSramDpi(config, 1, 0)
        uLsuSram.io.ifetch := False
        uLsuSram.io.pc := pc
        //uLsuSram.io.assignSomeByName(dbus)
        //uLsuSram.io.rdata.removeAssignments()
        uLsuSram.io.bus <> dbus
    } else {
        // arbitrate between the 2 buses
        val axiArbiter = Axi4LiteArbiter(config.axi4LiteConfig, 2, true)
        val sramAxi = Axi4Lite(config.axi4LiteConfig)
        axiArbiter.io.input <> Vec(ibus, dbus)
        axiArbiter.io.output <> sramAxi

        val sram = Axi4LiteSramDpi(config, 1, 0)
        sram.io.ifetch := ibus.ar.valid
        sram.io.pc := pc
        sram.io.bus <> sramAxi
    }
}

/** Blackbox for CoreNDpi.v */
case class CoreNDpi (config: RiscCoreConfig) extends BlackBox {
  val generic = new Generic {
    val XLEN = config.xlen
  }
  val io = new Bundle {
    val clk = in port Bool()
    val rst_b = in port Bool()
    val ebreak = in port Bool()
    val ecall = in port Bool()
    val pc = in port config.xlenUInt
  }
  noIoPrefix()
  mapClockDomain(clock = io.clk, reset = io.rst_b, resetActiveLevel = LOW)
}

object CoreNSoCVerilog extends App {
    val axi4LiteConfig = Axi4LiteConfig(addrWidth = 32, dataWidth = 32, axi4 = true)
    val config = RiscCoreConfig(32, 0x80000000L, 32, axi4LiteConfig=axi4LiteConfig)
    Config.spinal.generateVerilog(CoreNSoC(config)).printPruned()
}