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


case class CoreNSoC(config: RiscCoreConfig) extends Component {
    val ibus = Axi4Lite(config.axi4LiteConfig)
    val dbus = Axi4Lite(config.axi4LiteConfig)

    val core = CoreN(config)
    ibus <> core.io.ibus
    dbus <> core.io.dbus

    val pc = core.uIFU.io.ifuData.payload.pc.pull()

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

object CoreNSoCVerilog extends App {
    val config = RiscCoreConfig(32, 0x80000000L, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(CoreNSoC(config)).printPruned()
}