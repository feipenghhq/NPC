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

package core

import spinal.core._
import spinal.lib._
import config._
import _root_.misc._
import _root_.bus.Axi4Lite._



case class CoreN(config: RiscCoreConfig) extends Component {
    val ibus = Axi4Lite(config.axi4LiteConfig)
    val dbus = Axi4Lite(config.axi4LiteConfig)

    val uIFU = IFU(config)
    val uIDU = IDU(config)
    val uEXU = EXU(config)

    uIFU.io.branchCtrl <> uEXU.io.branchCtrl
    uIFU.io.ibus <> ibus
    uIFU.io.trapCtrl <> uEXU.io.trapCtrl

    uIDU.io.ifuData <> uIFU.io.ifuData
    uIDU.io.rdWrCtrl <> uEXU.io.rdWrCtrl

    uEXU.io.iduData <> uIDU.io.iduData
    uEXU.io.dbus <> dbus

    val iduData = uIDU.io.iduData.payload

    val uCoreNDpi = CoreNDpi(config)
    uCoreNDpi.io.ebreak := iduData.cpuCtrl.ebreak
    uCoreNDpi.io.ecall := iduData.cpuCtrl.ecall
    uCoreNDpi.io.pc := iduData.pc

    val uIfuSram = Axi4LiteSramDpi(config)
    uIfuSram.io.ifetch := True
    uIfuSram.io.pc := uIFU.io.ifuData.payload.pc
    uIfuSram.io.bus <> ibus

    val uLsuSram = Axi4LiteSramDpi(config)
    uLsuSram.io.ifetch := False
    uLsuSram.io.pc := uEXU.io.iduData.payload.pc
    //uLsuSram.io.assignSomeByName(dbus)
    //uLsuSram.io.rdata.removeAssignments()
    uLsuSram.io.bus <> dbus
}

object CoreNVerilog extends App {
    val config = RiscCoreConfig(32, 0x80000000L, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(CoreN(config)).printPruned()
}