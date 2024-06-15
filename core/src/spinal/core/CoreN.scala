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


case class CoreN(config: RiscCoreConfig) extends Component {
    val ibus = IbusBundle(config)
    val dbus = DbusBundle(config)
    noIoPrefix()

    val uIfu = IFU(config)
    val uIdu = IDU(config)
    val uExu = EXU(config)
    val uRegisterFile = RegisterFile(config)

    uIfu.io.branchCtrl <> uExu.io.branchCtrl
    uIfu.io.ibus <> ibus
    uIfu.io.trapCtrl <> uExu.io.trapCtrl

    uIdu.io.ifuData <> uIfu.io.ifuData
    uIdu.io.rdWrCtrl <> uExu.io.rdWrCtrl

    uExu.io.iduData <> uIdu.io.iduData
    uExu.io.dbus <> dbus

    uRegisterFile.io.rs1Addr <> uIdu.io.iduData.payload.cpuCtrl.rs1Addr
    uRegisterFile.io.rs2Addr <> uIdu.io.iduData.payload.cpuCtrl.rs2Addr
    uRegisterFile.io.rdWrCtrl <> uExu.io.rdWrCtrl

    val iduData = uIdu.io.iduData.payload
    val uCoreNDPI = CoreNDPI(config)
    uCoreNDPI.io.ebreak := iduData.cpuCtrl.ebreak
    uCoreNDPI.io.ecall := iduData.cpuCtrl.ecall
    uCoreNDPI.io.pc := iduData.pc
    ibus.data := uCoreNDPI.io.inst
    uCoreNDPI.io.data_valid := dbus.valid
    uCoreNDPI.io.data_wen := dbus.write
    uCoreNDPI.io.data_wdata := dbus.wdata
    uCoreNDPI.io.data_addr := dbus.addr
    uCoreNDPI.io.data_wstrb := dbus.strobe
    dbus.rdata := uCoreNDPI.io.data_rdata
}

object CoreNVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(CoreN(config))
}