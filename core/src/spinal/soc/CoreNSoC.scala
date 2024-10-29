/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NPC
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
import common._
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

    // DPI for verilator simulation
    val uCoreNDpi = CoreNDpi(config)
    uCoreNDpi.io.ebreak := core.iduData.cpuCtrl.ebreak.pull()
    uCoreNDpi.io.ecall := core.iduData.cpuCtrl.ecall.pull()
    uCoreNDpi.io.pc := pc

    // using two separate sram for instruction and data memory
    if (config.separateSram) {
        val uIfuSram = Axi4LiteRam(config, RamType.DPI)
        uIfuSram.io.ifetch := True
        uIfuSram.io.pc := pc
        uIfuSram.io.axi4l <> ibus

        val uLsuSram = Axi4LiteRam(config, RamType.DPI)
        uLsuSram.io.ifetch := False
        uLsuSram.io.pc := pc
        uLsuSram.io.axi4l <> dbus
    }
    // using single sram for instruction and data memory
    else {
        // arbitrate between the 2 buses
        val axiArbiter = Axi4LiteArbiter(config.axi4LiteConfig, 2, false)
        val sramAxi4l = Axi4Lite(config.axi4LiteConfig)
        axiArbiter.io.input <> Vec(ibus, dbus)
        axiArbiter.io.output <> sramAxi4l

        val sram = Axi4LiteRam(config, RamType.DPI)
        sram.io.ifetch := ibus.ar.valid
        sram.io.pc := pc
        sram.io.axi4l <> sramAxi4l
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
    val axi4LiteConfig = Axi4LiteConfig(addrWidth = 32, dataWidth = 32)
    val config = RiscCoreConfig(32, 0x80000000L, 32, separateSram=true, axi4LiteConfig=axi4LiteConfig,
                                ifuRreadyDelay=5, lsuBreadyDelay=5, lsuRreadyDelay=5)
    Config.spinal.generateVerilog(CoreNSoC(config)).printPruned()
}