/* ------------------------------------------------------------------------------------------------
 * Copyright (c) 2023. Heqing Huang (feipenghhq@gmail.com)
 *
 * Project: NRC
 * Author: Heqing Huang
 * Date Created: 6/9/2024
 *
 * ------------------------------------------------------------------------------------------------
 * IDU: Instruction Decoder Unit
 * ------------------------------------------------------------------------------------------------
 * Instantiate the Decoder and Register File
 * ------------------------------------------------------------------------------------------------
 */

package core

import spinal.core._
import spinal.lib._
import config._

case class IduBundle(config: RiscCoreConfig) extends Bundle {
    val cpuCtrl = CpuCtrl(config)
    val csrCtrl = CsrCtrl(config)
    val rs1Data = config.xlenBits
    val rs2Data = config.xlenBits
    val pc = config.xlenUInt
}

case class IDU(config: RiscCoreConfig) extends Component {
    val io = new Bundle {
        val ifuData = slave Stream(IfuBundle(config))
        val iduData = master Stream(IduBundle(config))
        val rdWrCtrl = slave Flow(RdWrCtrl(config))
    }

    // --------------------------------
    // Handshake
    // --------------------------------
    io.ifuData.ready <> io.iduData.ready
    io.iduData.valid <> io.ifuData.valid
    io.iduData.payload.pc <> io.ifuData.payload.pc

    // --------------------------------
    // Module instantiation
    // --------------------------------
    val dec = Decoder(config)
    dec.io.ifuData <> io.ifuData.payload
    dec.io.cpuCtrl <> io.iduData.payload.cpuCtrl
    dec.io.csrCtrl <> io.iduData.payload.csrCtrl

    val rf = RegisterFile(config)
    rf.io.rs1Addr <> dec.io.cpuCtrl.rs1Addr
    rf.io.rs2Addr <> dec.io.cpuCtrl.rs2Addr
    rf.io.rs1Data <> io.iduData.payload.rs1Data
    rf.io.rs2Data <> io.iduData.payload.rs2Data
    rf.io.rdWrCtrl <> io.rdWrCtrl
}

object IDUVerilog extends App {
    val config = RiscCoreConfig(32, 0x00000000, 32, hasRv32M = true, hasZicsr = true)
    Config.spinal.generateVerilog(IDU(config))
}